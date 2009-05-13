// Copyright 2006-2009 Google Inc.  All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.scheduler;

import com.google.enterprise.connector.instantiator.Instantiator;
import com.google.enterprise.connector.instantiator.InstantiatorException;
import com.google.enterprise.connector.monitor.Monitor;
import com.google.enterprise.connector.persist.ConnectorNotFoundException;
import com.google.enterprise.connector.traversal.BatchResult;
import com.google.enterprise.connector.traversal.Traverser;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduler that schedules connector traversal.  This class is thread safe.
 * Must initialize TraversalScheduler before running it.
 *
 * <p> This facility includes a schedule thread that runs a loop.
 * Each iteration it asks the instantiator for the schedule
 * for each Connector Instance and runs batches for those that
 * are
 * <OL>
 * <LI> scheduled to run.
 * <LI> have not exhausted their quota for the current time interval.
 * <LI> are not currently running.
 * </OL>
 * The implementation must handle the situation that a Connector
 * Instance is running.
 */
public class TraversalScheduler implements Scheduler {
  public static final String SCHEDULER_CURRENT_TIME = "/Scheduler/currentTime";

  private static final Logger LOGGER =
    Logger.getLogger(TraversalScheduler.class.getName());

  private final Instantiator instantiator;
  private final Monitor monitor;
  private final ThreadPool threadPool;
  private final HostLoadManager hostLoadManager;
  //Guarded by this
  private final Map<String, TaskHandle> taskHandles = new  HashMap<String, TaskHandle>();
  // Set of connectors removed.  Needed so that we only schedule connectors
  // that haven't been removed.  Protected by instance lock.
  private final Set<String> removedConnectors = new HashSet<String>();



  private boolean isInitialized;  // Protected by instance lock.
  private boolean isShutdown;     // Protected by instance lock.

  /**
   * Create a scheduler object.
   *
   * @param instantiator
   * @param monitor
   * @param threadPool
   */
  public TraversalScheduler(Instantiator instantiator, Monitor monitor,
      ThreadPool threadPool) {
    this.instantiator = instantiator;
    this.monitor = monitor;
    this.threadPool = threadPool;
    this.isInitialized = false;
    this.isShutdown = false;
    this.hostLoadManager = new HostLoadManager(instantiator);
  }

  public synchronized void init() {
    if (isInitialized) {
      return;
    }
    isInitialized = true;
    isShutdown = false;
    new Thread(this, "TraversalScheduler").start();
  }

  public synchronized void shutdown(boolean interrupt, long timeoutInMillis) {
    LOGGER.info("Shutdown initiated...");
    if (isShutdown) {
      return;
    }
    try {
      threadPool.shutdown(interrupt, timeoutInMillis);
    } catch (InterruptedException ie) {
      // TODO(strellis): how should this be handled.
      LOGGER.log(Level.SEVERE, "TraversalScheduler shutdown interrupted: ", ie);
    }
    instantiator.shutdown();
    isInitialized = false;
    isShutdown = true;
  }

  private void updateMonitor() {
    // TODO: Change this when we figure out what we really want to monitor.
    Map<String, Date> vars = new HashMap<String, Date>();
    vars.put(SCHEDULER_CURRENT_TIME, new Date());
    monitor.setVariables(vars);
  }

  private boolean shouldRun(Schedule schedule) {
    Calendar now = Calendar.getInstance();
    int hour = now.get(Calendar.HOUR_OF_DAY);
    for (ScheduleTimeInterval interval : schedule.getTimeIntervals()) {
      int startHour = interval.getStartTime().getHour();
      int endHour = interval.getEndTime().getHour();
      if (0 == endHour) {
        endHour = 24;
      }
      if ((hour >= startHour) && (hour < endHour)) {
        return !hostLoadManager.shouldDelay(schedule.getConnectorName());
      }
    }
    return false;
  }

  /**
   * Determines whether scheduler should run.  Assumes caller holds instance
   * lock.
   *
   * @return true if we are in a running state and scheduler should run or
   *         continue running.
   */
  private synchronized boolean isRunningState() {
    return isInitialized && !isShutdown;
  }

  /**
   * Call this method when a connector is removed.  Assumes ScheduleStore has
   * already been updated to reflect the schedule change.  This causes the
   * scheduler to gracefully interrupt any work that is done on this connector.
   *
   * @param connectorName name of the connector instance
   */
  public synchronized void removeConnector(String connectorName) {
      TaskHandle taskHandle = taskHandles.get(connectorName);
      if (taskHandle != null) {
        taskHandle.cancel();
      }

      removedConnectors.add(connectorName);

      // Tell the load manager to forget about this connector.
      hostLoadManager.removeConnector(connectorName);
  }

  private Traverser getTraverser(String connectorName) {
    Traverser traverser = null;
    try {
      traverser = instantiator.getTraverser(connectorName);
    } catch (ConnectorNotFoundException cnfe) {
      cnfe.printStackTrace();
    } catch (InstantiatorException ie) {
      ie.printStackTrace();
    }
    return traverser;
  }

  private void scheduleBatches() {
    for (String connectorName : instantiator.getConnectorNames()) {
      scheduleABatch(connectorName);
    }
  }

  private void scheduleABatch(String connectorName) {
    synchronized (this) {
      removedConnectors.clear();
    }
    String scheduleStr = null;
    try {
      scheduleStr = instantiator.getConnectorSchedule(connectorName);
    } catch (ConnectorNotFoundException e) {
      // Looks like the connector just got deleted.  Don't schedule it.
      return;
    }
    Schedule schedule = new Schedule(scheduleStr);
    if (schedule.isDisabled()) {
      return;
    }
    TaskHandle handle = taskHandles.get(connectorName);
    if (handle != null && !handle.isDone()) {
      // LOGGER.finer("Traversal work for connector "
      //              + connectorName + " is still running.");
      return;
    }
    if (shouldRun(schedule)) {
      BatchResultRecorder resultRecorder =  new TraversalBatchResultRecorder(schedule);
      Traverser traverser = getTraverser(connectorName);
      if (traverser == null) {
        return;
      }
      int batchHint = hostLoadManager.determineBatchHint(connectorName);
      if (batchHint <= 0) {
        return;
      }
      Cancelable batch = new CancelableBatch(traverser, connectorName, resultRecorder, batchHint);
      synchronized (this) {
        if(!removedConnectors.contains(connectorName)) {
          taskHandles.put(connectorName, threadPool.submit(batch));
        }
      }
    }
  }

  public void run() {
    while (true) {
      try {
        synchronized (this) {
          if (!isRunningState()) {
            LOGGER.info("TraversalScheduler thread is stopping due to "
                + "shutdown or not being initialized.");
            return;
          }
        }
        scheduleBatches();
        updateMonitor();
        // Give someone else a chance to run.
        try {
          synchronized (this) {
            wait(1000);
          }
        } catch (InterruptedException e) {
          // May have been interrupted for shutdown.
        }
      } catch (Throwable t) {
        LOGGER.log(Level.SEVERE,
            "TraversalScheduler caught unexpected Throwable: ", t);
      }
    }
  }

  private class TraversalBatchResultRecorder implements BatchResultRecorder {
    private final Schedule schedule;
    TraversalBatchResultRecorder(Schedule schedule) {
      this.schedule = schedule;
    }
    @Override
    public void recordResult(BatchResult result) {
      String connectorName = schedule.getConnectorName();
      int retryDelayMillis = schedule.getRetryDelayMillis();
      switch (result.getDelayPolicy()) {
        case POLL:
          hostLoadManager.connectorFinishedTraversal(connectorName, retryDelayMillis);
          if (retryDelayMillis == Schedule.POLLING_DISABLED) {
            disableConnectorInstance(schedule);
            schedule.setDisabled(true);
          }
          break;

        case ERROR:
          hostLoadManager.connectorFinishedTraversal(connectorName, Traverser.ERROR_WAIT_MILLIS);
          break;

        case IMMEDIATE:
          // This means the batch did not complete so we leave the
          //    hostLoadManager in peace.
          break;

        default:
          throw new IllegalArgumentException("result = " + result);
      }
    }

    private void disableConnectorInstance(Schedule schedule) {
      String connectorInstanceName = schedule.getConnectorName();
      schedule.setDisabled(true);
      try {
      instantiator.setConnectorSchedule(connectorInstanceName, schedule.toString());
      LOGGER.info("Traversal complete. Automatically pausing "
        + "traversal for connector " + connectorInstanceName);
      } catch (ConnectorNotFoundException cnfe) {
        LOGGER.log(Level.INFO, "Connector removed during schedule save: "
            + connectorInstanceName, cnfe);
      }
    }
  }
}