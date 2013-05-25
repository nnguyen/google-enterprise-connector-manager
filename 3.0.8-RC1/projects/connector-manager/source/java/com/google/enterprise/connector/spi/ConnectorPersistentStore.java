// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.spi;

/**
 * Gives connectors access to a local storage area.
 *
 * @see ConnectorPersistentStoreAware
 * @since 2.8
 */
public interface ConnectorPersistentStore {

  /**
   * Gets a {@link LocalDocumentStore} through which the connector can access
   * the Connector Manager's persisted store of document metadata.
   *
   * @return {@code null}
   */
  public LocalDocumentStore getLocalDocumentStore();

  /**
   * Gets a {@link LocalDatabase} through which the connector can create and
   * alter private data.
   *
   * @return a {@link LocalDatabase}.
   */
  public LocalDatabase getLocalDatabase();
}