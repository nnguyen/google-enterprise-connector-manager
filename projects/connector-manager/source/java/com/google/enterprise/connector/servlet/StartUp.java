// Copyright 2006 Google Inc.
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

import com.google.enterprise.connector.instantiator.EncryptedPropertyPlaceholderConfigurer;
import com.google.enterprise.connector.logging.NDC;
import com.google.enterprise.connector.manager.Context;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * The main purpose of this servlet is to have its "init" method called when the
 * container starts up. This is by done by means of the web.xml file. But I also
 * gave it a get and post that do the same thing.
 *
 */
public class StartUp extends HttpServlet {
  private static final Logger LOGGER =
      Logger.getLogger(StartUp.class.getName());

  @Override
  public void init() {
    NDC.push("Init");
    try {
      LOGGER.info("init");
      ServletContext servletContext = this.getServletContext();
      doConnectorManagerStartup(servletContext);
      LOGGER.info("init done");
    } finally {
      NDC.remove();
    }
  }

  @Override
  public void destroy() {
    NDC.push("Shutdown");
    try {
      LOGGER.info("destroy");
      Context.getInstance().shutdown(true);
    } finally {
      NDC.remove();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    doPost(req, res);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    ServletContext servletContext = this.getServletContext();
    doConnectorManagerStartup(servletContext);
    res.setContentType(ServletUtil.MIMETYPE_HTML);
    PrintWriter out = res.getWriter();
    out.println("<HTML><HEAD><TITLE>Connector Manager Started</TITLE></HEAD>"
        + "<BODY>Connector manager has been successfully started.</BODY>"
        + "</HTML>");
    out.close();
    LOGGER.info("Connector Manager started.");
  }

  private void doConnectorManagerStartup(ServletContext servletContext) {
    LOGGER.info(ServletUtil.getManagerSplash());

    // read in and set initialization parameters
    String kp = servletContext.getInitParameter("keystore_passwd_file");
    EncryptedPropertyPlaceholderConfigurer.setKeyStorePasswdPath(kp);

    String ks = servletContext.getInitParameter("keystore_file");
    String realks = servletContext.getRealPath("/WEB-INF/" + ks);
    if (null == realks) {
      // Servlet container cannot translated the virtual path to a
      // real path, so use the given path.
      EncryptedPropertyPlaceholderConfigurer.setKeyStorePath(ks);
    } else {
      EncryptedPropertyPlaceholderConfigurer.setKeyStorePath(realks);
    }

    String kt = servletContext.getInitParameter("keystore_type");
    EncryptedPropertyPlaceholderConfigurer.setKeyStoreType(kt);

    String ka = servletContext.getInitParameter("keystore_crypto_algo");
    EncryptedPropertyPlaceholderConfigurer.setKeyStoreCryptoAlgo(ka);

    // Note: default context location is /WEB-INF/applicationContext.xml
    LOGGER.info("Making an XmlWebApplicationContext");
    XmlWebApplicationContext ac = new XmlWebApplicationContext();
    ac.setServletContext(servletContext);
    ac.refresh();

    Context context = Context.getInstance();
    String basePathConfigured = System.getProperty("connectorBasePath");
    String basePath = servletContext.getRealPath("/WEB-INF");
    if (basePathConfigured != null) {
      File serverBasePath = new File(servletContext.getRealPath("/"));
      String appName = serverBasePath.getName();
      basePath = basePathConfigured + File.separator + appName;
      LOGGER.info("Using custom commonDirPath: " + basePath);
    }
    context.setServletContext(ac, basePath);
    context.start();
  }
}
