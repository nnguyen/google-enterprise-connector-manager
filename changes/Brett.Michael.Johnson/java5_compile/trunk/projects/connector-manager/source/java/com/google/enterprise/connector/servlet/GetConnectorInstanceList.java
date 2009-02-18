// Copyright (C) 2006-2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.common.JarUtils;
import com.google.enterprise.connector.manager.ConnectorStatus;
import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.scheduler.Schedule;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConnectorType;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Admin servlet to get a list of connector types.
 */
public class GetConnectorInstanceList extends HttpServlet {
  private static final Logger LOGGER =
    Logger.getLogger(GetConnectorInstanceList.class.getName());

  /**
   * Returns a list of connector types.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    doPost(req, res);
  }

  /**
   * Returns a list of connector types.
   *
   * @param req
   * @param res
   * @throws IOException
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();
    handleDoPost(manager, out);
    out.close();
  }

  /**
   * Handler for doGet in order to do unit tests.
   *
   * @param manager a Manager
   * @param out PrintWriter where the response is written
   */
  public static void handleDoPost(Manager manager, PrintWriter out) {
    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeManagerSplash(out);

    List <ConnectorStatus> connectorInstances = manager.getConnectorStatuses();
    if (connectorInstances == null || connectorInstances.size() == 0) {
      ServletUtil.writeStatusId(out,
          ConnectorMessageCode.RESPONSE_NULL_CONNECTOR);
      ServletUtil.writeRootTag(out, true);
      return;
    }

    ServletUtil.writeStatusId(out, ConnectorMessageCode.SUCCESS);
    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_INSTANCES,
        false);

    Iterator <ConnectorStatus> iter = connectorInstances.iterator();
    while (iter.hasNext()) {
      ConnectorStatus connectorStatus = iter.next();
      ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_CONNECTOR_INSTANCE,
          false);
      ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_CONNECTOR_NAME,
          connectorStatus.getName());
      String typeName = connectorStatus.getType();
      ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_CONNECTOR_TYPE,
          typeName);
      String version = null;
      try {
        ConnectorType connectorType = manager.getConnectorType(typeName);
        version = JarUtils.getJarVersion(connectorType.getClass());
      } catch (ConnectorTypeNotFoundException e) {
        // The JUnit tests might not have actual ConnectorTypes.
        LOGGER.warning("Connector type not found: " + typeName);
      }
      if (version != null && version.length() > 0) {
        ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_VERSION,
            version);
      }
      ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_STATUS, Integer
          .toString(connectorStatus.getStatus()));
      if (connectorStatus.getSchedule() == null) {
        LOGGER.log(Level.WARNING, connectorStatus.getName() + ": " +
            ServletUtil.LOG_RESPONSE_NULL_SCHEDULE);
        ServletUtil.writeEmptyXMLElement(out, 3,
            ServletUtil.XMLTAG_CONNECTOR_SCHEDULE);
      } else {
        ServletUtil.writeXMLElement(out, 3,
            ServletUtil.XMLTAG_CONNECTOR_SCHEDULE,
            Schedule.toLegacyString(connectorStatus.getSchedule()));
      }
      ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_CONNECTOR_INSTANCE,
          true);
    }

    ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_CONNECTOR_INSTANCES,
        true);
    ServletUtil.writeRootTag(out, true);
  }

}
