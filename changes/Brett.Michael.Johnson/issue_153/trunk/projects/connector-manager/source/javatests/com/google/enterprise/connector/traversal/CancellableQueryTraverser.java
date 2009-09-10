// Copyright 2009 Google Inc.  All Rights Reserved.
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

package com.google.enterprise.connector.traversal;

/**
 * A mock query traverser that can be cancelled.
 */
public class CancellableQueryTraverser implements Traverser {
  boolean cancelled = false;

  public int runBatch(int batchHint) {
    // infinite loop
    while (!isCancelled()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ie) {
        // do nothing
      }
    }
    return -1;
  }

  public synchronized void cancelBatch() {
    cancelled = true;
  }

  public synchronized boolean isCancelled() {
    return cancelled;
  }
}
