// Copyright 2007-2009 Google Inc.
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

import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.servlet.AuthorizationParser.ConnectorQueries;
import com.google.enterprise.connector.servlet.AuthorizationParser.QueryResources;
import com.google.enterprise.connector.spi.AuthenticationIdentity;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * This class does the real work for the authorization servlet
 */
public class AuthorizationHandler {
  String xmlBody;
  Manager manager;
  PrintWriter out;
  int status;
  Map<AuthorizationResource, Boolean> results;

  AuthorizationHandler(String xmlBody, Manager manager, PrintWriter out) {
    this.xmlBody = xmlBody;
    this.manager = manager;
    this.out = out;
    results = new HashMap<AuthorizationResource, Boolean>();
  }

  /**
   * Factory method for testing.  Ensures that the results come back in a
   * predictable order.
   */
  static AuthorizationHandler makeAuthorizationHandlerForTest(String xmlBody,
      Manager manager, PrintWriter out) {
    AuthorizationHandler authorizationHandler = new AuthorizationHandler(
        xmlBody, manager, out);
    authorizationHandler.results =
        new TreeMap<AuthorizationResource, Boolean>();
    return authorizationHandler;
  }

  /**
   * Writes an answer for each resource from the request.
   */
  public void handleDoPost() {
    NDC.pushAppend("AuthZ");
    try {
      AuthorizationParser authorizationParser =
          new AuthorizationParser(xmlBody);
      status = authorizationParser.getStatus();
      if (status == ConnectorMessageCode.ERROR_PARSING_XML_REQUEST) {
        ServletUtil.writeResponse(out,status);
        return;
      }
      computeResultSet(authorizationParser);
      generateXml();
    } finally {
      NDC.pop();
    }
  }

  private void generateXml() {
    ServletUtil.writeRootTag(out, false);
    if (results.size() > 0) {
      ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHZ_RESPONSE, false);
      generateEachResultXml();
      ServletUtil.writeXMLTag(out, 1, ServletUtil.XMLTAG_AUTHZ_RESPONSE, true);
    }
    ServletUtil.writeStatusId(out, status);
    ServletUtil.writeRootTag(out, true);
  }

  private void generateEachResultXml() {
    for (Entry<AuthorizationResource, Boolean> e : results.entrySet()) {
      writeResultElement(e.getKey(), e.getValue());
    }
  }

  private void writeResultElement(AuthorizationResource resource,
      boolean permit) {
    ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_ANSWER, false);
    if (resource.isFabricated()) {
      // Just write out the element.
      ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_RESOURCE,
          resource.getUrl());
    } else {
      // Have to add the connector name attribute to the element.
      String attribute = ServletUtil.XMLTAG_CONNECTOR_NAME_ATTRIBUTE + "=\""
          + resource.getConnectorName() + "\"";
      ServletUtil.writeXMLTagWithAttrs(out, 3, ServletUtil.XMLTAG_RESOURCE,
          attribute, false);
      out.println(ServletUtil.indentStr(4) + resource.getUrl());
      ServletUtil.writeXMLTag(out, 3, ServletUtil.XMLTAG_RESOURCE, true);
    }
    ServletUtil.writeXMLElement(out, 3, ServletUtil.XMLTAG_DECISION,
                                (permit) ? "Permit" : "Deny");
    ServletUtil.writeXMLTag(out, 2, ServletUtil.XMLTAG_ANSWER, true);
  }

  private void computeResultSet(AuthorizationParser authorizationParser) {
    for (AuthenticationIdentity identity: authorizationParser.getIdentities()) {
      NDC.pushAppend(identity.getUsername());
      try {
        ConnectorQueries queries =
            authorizationParser.getConnectorQueriesForIdentity(identity);
        runManagerQueries(identity, queries);
      } finally {
        NDC.pop();
      }
    }
  }

  private void runManagerQueries(AuthenticationIdentity identity,
      ConnectorQueries urlsByConnector) {
    for (String connectorName : urlsByConnector.getConnectors()) {
      NDC.pushAppend(connectorName);
      try {
        QueryResources urlsByDocid = urlsByConnector.getQueryResources(connectorName);
        List<String> docidList = new ArrayList<String>(urlsByDocid.getDocids());
        Set<String> answerSet =
            manager.authorizeDocids(connectorName, docidList, identity);
        accumulateQueryResults(answerSet, urlsByDocid);
      } finally {
        NDC.pop();
      }
    }
  }

  private void accumulateQueryResults(Set<String> answerSet,
      QueryResources urlsByDocid) {
    for (String docid : urlsByDocid.getDocids()) {
      AuthorizationResource resource = urlsByDocid.getResource(docid);
      Boolean permit = answerSet.contains(docid) ? Boolean.TRUE : Boolean.FALSE;
      Object isDup = results.put(resource, permit);
      if (isDup != null) {
        //TODO (ziff): warning
      }
    }
  }
}