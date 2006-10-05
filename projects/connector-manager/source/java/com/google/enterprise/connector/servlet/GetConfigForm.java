// Copyright (C) 2006 Google Inc.
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


package com.google.enterprise.connector.servlet;

import com.google.enterprise.connector.manager.Context;
import com.google.enterprise.connector.manager.Manager;
import com.google.enterprise.connector.persist.ConnectorTypeNotFoundException;
import com.google.enterprise.connector.spi.ConfigureResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Admin servlet to get the config form for
 * a given connector type and language.
 * 
 */
public class GetConfigForm extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(GetConfigForm.class.getName());

  /**
   * Returns the connector config form.
   * 
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   * 
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String status = ServletUtil.XML_RESPONSE_SUCCESS;
    String connectorTypeName =
        req.getParameter(ServletUtil.XMLTAG_CONNECTOR_TYPE);
    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();

    if (connectorTypeName == null || connectorTypeName.length() < 1) {
      status = ServletUtil.XML_RESPONSE_STATUS_NULL_CONNECTOR_TYPE;
      ServletUtil.writeSimpleResponse(out, status);
      LOGGER.severe(status);
      return;
    }
    String language = req.getParameter(ServletUtil.QUERY_PARAM_LANG);
    if (language == null || language.length() < 1) {
      LOGGER.info("language is null");
      language = ServletUtil.DEFAULT_LANGUAGE;
    }

    ServletContext servletContext = this.getServletContext();
    Manager manager = Context.getInstance(servletContext).getManager();

    try {
      ConfigureResponse configResponse =
          manager.getConfigForm(connectorTypeName, language);
      handleDoGet(out, status, configResponse);
    } catch (ConnectorTypeNotFoundException e1) {
      ServletUtil.writeSimpleResponse(out, e1.toString());
      LOGGER.info("Connector Type Not Found Exception");
      e1.printStackTrace();
    }

    out.close();
  }

  /**
   * Returns the connector config form.
   * 
   * @param req
   * @param res
   * @throws ServletException
   * @throws IOException
   * 
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doGet(req, res);
  }

  /**
   * Handler for doGet in order to do unit tests.
   * 
   * @param out
   * @param configResponse
   */
  public static void handleDoGet(PrintWriter out, String status,
      ConfigureResponse configResponse) {
    ServletUtil.writeConfigureResponse(out, status, configResponse);
  }
}
