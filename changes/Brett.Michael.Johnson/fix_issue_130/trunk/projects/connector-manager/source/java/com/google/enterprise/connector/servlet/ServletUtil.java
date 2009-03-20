// Copyright (C) 2006-2009 Google Inc.
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

import com.google.enterprise.connector.common.JarUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Servlet constant and utility class.
 *
 * Non-instantiable class that holds constants and XML tag constants,
 * utilities for reading/writing XML string, etc.
 *
 */
public class ServletUtil {

  private static Logger LOGGER =
    Logger.getLogger(ServletUtil.class.getName());

  private ServletUtil() {
  }

  public static final String MANAGER_NAME =
      "Google Enterprise Connector Manager";

  public static final String MIMETYPE_XML = "text/xml";
  public static final String MIMETYPE_HTML = "text/html";
  public static final String MIMETYPE_TEXT_PLAIN = "text/plain";
  public static final String MIMETYPE_ZIP = "application/zip";

  public static final String PROTOCOL = "googleconnector://";
  public static final String DOCID = "/doc?docid=";

  public static final String QUERY_PARAM_LANG = "Lang";
  public static final String DEFAULT_LANGUAGE = "en";

  public static final String XMLTAG_RESPONSE_ROOT = "CmResponse";
  public static final String XMLTAG_STATUSID = "StatusId";
  public static final String XMLTAG_STATUS_MESSAGE = "StatusMsg";
  public static final String XMLTAG_STATUS_PARAMS = "CMParams";
  public static final String XMLTAG_STATUS_PARAM_ORDER = "Order";
  public static final String XMLTAG_STATUS_PARAM = "CMParam";

  public static final String XMLTAG_CONNECTOR_LOGS = "ConnectorLogs";
  public static final String XMLTAG_FEED_LOGS = "FeedLogs";
  public static final String XMLTAG_TEED_FEED = "TeedFeedFile";
  public static final String XMLTAG_LOG = "Log";
  public static final String XMLTAG_NAME = "Name";
  public static final String XMLTAG_SIZE = "Size";
  public static final String XMLTAG_LAST_MODIFIED = "LastModified";
  public static final String XMLTAG_VERSION = "Version";
  public static final String XMLTAG_INFO = "Info";

  public static final String XMLTAG_CONNECTOR_INSTANCES = "ConnectorInstances";
  public static final String XMLTAG_CONNECTOR_INSTANCE = "ConnectorInstance";
  public static final String XMLTAG_CONNECTOR_TYPES = "ConnectorTypes";
  public static final String XMLTAG_CONNECTOR_TYPE = "ConnectorType";
  public static final String XMLTAG_CONNECTOR_STATUS = "ConnectorStatus";
  public static final String XMLTAG_CONNECTOR_NAME = "ConnectorName";
  public static final String XMLTAG_STATUS = "Status";
  public static final String XMLTAG_CONFIG_FORM = "ConfigForm";
  public static final String XMLTAG_CONFIGURE_RESPONSE = "ConfigureResponse";
  public static final String XMLTAG_MESSAGE = "message";
  public static final String XMLTAG_FORM_SNIPPET = "FormSnippet";

  public static final String XMLTAG_MANAGER_CONFIG = "ManagerConfig";
  public static final String XMLTAG_FEEDERGATE = "FeederGate";
  public static final String XMLTAG_FEEDERGATE_HOST = "host";
  public static final String XMLTAG_FEEDERGATE_PORT = "port";

  public static final String XMLTAG_CONNECTOR_CONFIG = "ConnectorConfig";
  public static final String XMLTAG_UPDATE_CONNECTOR = "Update";
  public static final String XMLTAG_PARAMETERS = "Param";

  public static final String XMLTAG_AUTHN_REQUEST = "AuthnRequest";
  public static final String XMLTAG_AUTHN_CREDENTIAL = "Credentials";
  public static final String XMLTAG_AUTHN_USERNAME = "Username";
  public static final String XMLTAG_AUTHN_PASSWORD = "Password";
  public static final String XMLTAG_AUTHN_RESPONSE = "AuthnResponse";
  public static final String XMLTAG_SUCCESS = "Success";
  public static final String XMLTAG_FAILURE = "Failure";
  public static final String XMLTAG_AUTHZ_QUERY = "AuthorizationQuery";
  public static final String XMLTAG_CONNECTOR_QUERY = "ConnectorQuery";
  public static final String XMLTAG_IDENTITY = "Identity";
  public static final String XMLTAG_RESOURCE = "Resource";
  public static final String XMLTAG_AUTHZ_RESPONSE = "AuthorizationResponse";
  public static final String XMLTAG_ANSWER = "Answer";
  public static final String XMLTAG_DECISION = "Decision";

  public static final String XMLTAG_CONNECTOR_SCHEDULES = "ConnectorSchedules";
  public static final String XMLTAG_CONNECTOR_SCHEDULE = "ConnectorSchedule";
  public static final String XMLTAG_DISABLED = "disabled";
  public static final String XMLTAG_LOAD = "load";
  public static final String XMLTAG_DELAY = "RetryDelayMillis";
  public static final String XMLTAG_TIME_INTERVALS = "TimeIntervals";

  public static final String LOG_RESPONSE_EMPTY_REQUEST = "Empty request";
  public static final String LOG_RESPONSE_EMPTY_NODE = "Empty node";
  public static final String LOG_RESPONSE_NULL_CONNECTOR =
      "Null connector name";
  public static final String LOG_RESPONSE_NULL_CONNECTOR_TYPE =
      "Null connector type name";
  public static final String LOG_RESPONSE_NULL_SCHEDULE =
      "Null connector schedule";

  public static final String LOG_EXCEPTION_CONNECTOR_TYPE_NOT_FOUND =
      "Exception: the connector type is not found";
  public static final String LOG_EXCEPTION_CONNECTOR_NOT_FOUND =
      "Exception: the connector is not found";
  public static final String LOG_EXCEPTION_CONNECTOR_EXISTS =
      "Exception: the connector exists";
  public static final String LOG_EXCEPTION_INSTANTIATOR =
      "Exception: instantiator";
  public static final String LOG_EXCEPTION_PERSISTENT_STORE =
      "Exception: persistent store";
  public static final String LOG_EXCEPTION_THROWABLE =
      "Exception: throwable";
  public static final String LOG_EXCEPTION_CONNECTOR_MANAGER =
      "Exception: general";

  public static final String XML_SIMPLE_RESPONSE =
      "<CmResponse>\n" + "  <StatusId>0</StatusId>\n" + "</CmResponse>\n";

  public static final String DEFAULT_FORM =
    "<tr><td>Username</td><td>\n" +
    "<input type=\"text\" name=\"Username\" /></td></tr>\n" +
    "<tr><td>Password</td><td>\n" +
    "<input type=\"password\" name=\"Password\" /></td></tr>\n" +
    "<tr><td>Repository</td><td>\n" +
    "<input type=\"text\" name=\"Repository\" /></td></tr>\n";

  public static final String ATTRIBUTE_NAME = "name=\"";
  public static final String ATTRIBUTE_VALUE = " value=\"";
  public static final String ATTRIBUTE_VERSION = "version=\"";
  public static final char QUOTE = '"';

  private static final String[] XMLIndent = {
      "",
      "  ",
      "    ",
      "      ",
      "        ",
      "          ",
      "            ",
      "              "};

  private static DocumentBuilderFactory factory =
      DocumentBuilderFactory.newInstance();

  /**
   * Parse an XML String to a Document.
   *
   * @param fileContent the XML string
   * @param errorHandler The error handle for SAX parser
   * @return A result Document object, null on error
   */
  public static Document parse(String fileContent,
                               SAXParseErrorHandler errorHandler) {
    InputStream in = stringToInputStream(fileContent);
    return (in == null) ? null : parse(in, errorHandler);
  }

  /**
   * Get a root element from the XML request body.
   *
   * @param xmlBody String the XML request body
   * @param rootTagName String the root element tag name
   * @return a result Element object if successful, null on error
   */
  public static Element parseAndGetRootElement(String xmlBody,
                                               String rootTagName) {
    InputStream in = stringToInputStream(xmlBody);
    return (in == null) ? null : parseAndGetRootElement(in, rootTagName);
  }

  private static InputStream stringToInputStream(String fileContent) {
    try {
      return new ByteArrayInputStream(fileContent.getBytes("UTF-8"));
    } catch (java.io.UnsupportedEncodingException uee) {
      LOGGER.log(Level.SEVERE, "Really Unexpected", uee);
      return null;
    }
  }

  /**
   * Parse an input stream to a Document.
   *
   * @param in the input stream
   * @param errorHandler The error handle for SAX parser
   * @return A result Document object, null on error
   */
  public static Document parse(InputStream in,
                               SAXParseErrorHandler errorHandler) {
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setErrorHandler(errorHandler);
      Document document = builder.parse(in);
      return document;
    } catch (ParserConfigurationException pce) {
      LOGGER.log(Level.SEVERE, "Parse exception", pce);
    } catch (SAXException se) {
      LOGGER.log(Level.SEVERE, "SAX Exception", se);
    } catch (IOException ioe) {
      LOGGER.log(Level.SEVERE, "IO Exception", ioe);
    }
    return null;
  }

  /**
   * Get a root element from an XML input stream.
   *
   * @param in the input stream
   * @param rootTagName String the root element tag name
   * @return a result Element object if successful, null on error
   */
  public static Element parseAndGetRootElement(InputStream in,
                                               String rootTagName) {
    SAXParseErrorHandler errorHandler = new SAXParseErrorHandler();
    Document document = ServletUtil.parse(in, errorHandler);
    if (document == null) {
      LOGGER.log(Level.WARNING, "XML parsing exception!");
      return null;
    }

    NodeList nodeList =
      document.getElementsByTagName(rootTagName);
    if (nodeList == null || nodeList.getLength() == 0) {
      LOGGER.log(Level.WARNING, "Empty node: " + rootTagName);
      return null;
    }

    return (Element) nodeList.item(0);
  }

  /**
   * Get the attribute value of a given attribute name for
   * the first XML element of given name
   *
   * @param elem Element The parent XML element
   * @param name String name of the child text element
   * @param attrName String Attribute name
   * @return String attribute value of named child element
   */
  public static String getFirstAttribute(Element elem, String name,
                                         String attrName) {
    NodeList nodeList = elem.getElementsByTagName(name);
    if (nodeList.getLength() == 0) {
      return null;
    }

    return (((Element)nodeList.item(0)).getAttribute(attrName));
  }

  /**
   * Get the attribute values of a given name/value pair for
   * the first XML element of given name
   *
   * @param elem Element The parent XML element
   * @param name String name of the child text element
   * @return attribute name and value map of named child element
   *
   */
  public static Map getAllAttributes(Element elem, String name) {
    Map attributes = new TreeMap();
    NodeList nodeList = elem.getElementsByTagName(name);
    int length = nodeList.getLength();
    for (int n = 0; n < length; ++n) {
      attributes.put(((Element)nodeList.item(n)).getAttribute("name"),
                     ((Element)nodeList.item(n)).getAttribute("value"));
    }
    return attributes;
  }

  /**
   * Get text data of first XML element of given name
   *
   * @param elem Element The parent XML element
   * @param name String name of the child text element
   * @return text data of named child element
   */
  public static String getFirstElementByTagName(Element elem, String name) {
    NodeList nodeList = elem.getElementsByTagName(name);
    if (nodeList.getLength() == 0) {
      return null;
    }

    NodeList children = nodeList.item(0).getChildNodes();
    if (children.getLength() == 0 ||
        children.item(0).getNodeType() != Node.TEXT_NODE) {
      return null;
    }
    return children.item(0).getNodeValue();
  }

  /**
   * Get a list of all child text element of given name directly
   * under a given element
   *
   * @param elem the parent element
   * @param name the given name of searched child elements
   * @return a list of values of those child text elements
   */
  public static List getAllElementsByTagName(Element elem, String name) {
    NodeList nodeList = elem.getElementsByTagName(name);
    List result = new ArrayList();
    for (int i = 0; i < nodeList.getLength(); ++i) {
      NodeList children = nodeList.item(i).getChildNodes();
      if (children.getLength() == 0 ||
          children.item(0).getNodeType() != Node.TEXT_NODE) {
        continue;
      }
      result.add(children.item(0).getNodeValue());
    }
    return result;
  }

  /**
   * Write an XML response with only StatusId (int) to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param statusId int
   *
   */
  public static void writeResponse(PrintWriter out, int statusId) {
    writeRootTag(out, false);
    writeStatusId(out, statusId);
    writeRootTag(out, true);
  }

  /**
   * Write an XML response with full status to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param status ConnectorMessageCode
   *
   */
  public static void writeResponse(PrintWriter out,
                                   ConnectorMessageCode status) {
    writeRootTag(out, false);
    writeMessageCode(out, status);
    writeRootTag(out, true);
  }

  /**
   * Write the root XML tag <CMResponse> to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param endingTag boolean true if it is the ending tag
   *
   */
  public static void writeRootTag(PrintWriter out, boolean endingTag) {
    writeXMLTag(out, 0, ServletUtil.XMLTAG_RESPONSE_ROOT, endingTag);
  }

  /**
   * Write Connector Manager, OS, JVM version information.
   *
   * @param out where PrintWriter to be written to
   */
  public static void writeManagerSplash(PrintWriter out) {
    writeXMLElement(out, 1, ServletUtil.XMLTAG_INFO, getManagerSplash());
  }

  /**
   * Get Connector Manager, OS, JVM version information.
   */
  public static String getManagerSplash() {
    return ServletUtil.MANAGER_NAME + " "
      + JarUtils.getJarVersion(ServletUtil.class) + "; "
      + System.getProperty("java.vendor") + " "
      + System.getProperty("java.vm.name") + " "
      + System.getProperty("java.version") + "; "
      + System.getProperty("os.name") + " "
      + System.getProperty("os.version") + " ("
      + System.getProperty("os.arch") + ")";
  }

  /**
   * Write a statusId response to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param statusId int
   *
   */
  public static void writeStatusId(PrintWriter out,
                                   int statusId) {
    writeXMLElement(out, 1, ServletUtil.XMLTAG_STATUSID,
        Integer.toString(statusId));
  }

  /**
   * Write a partial XML status response to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param status ConnectorMessageCode
   *
   */
  public static void writeMessageCode(PrintWriter out,
                                      ConnectorMessageCode status) {
    writeStatusId(out, status.getMessageId());

    if (status.getMessage() != null && status.getMessage().length() > 1) {
      writeXMLElement(
          out, 1, ServletUtil.XMLTAG_STATUS_MESSAGE, status.getMessage());
    }

    if (status.getParams() == null) {
      return;
    }

    for (int i = 0; i < status.getParams().length; ++i) {
      String param = status.getParams()[i].toString();
      if (param == null || param.length() < 1) {
        continue;
      }
      out.println(indentStr(1)
          + "<" + XMLTAG_STATUS_PARAMS
          + " " + XMLTAG_STATUS_PARAM_ORDER + "=\"" + Integer.toString(i) + "\""
          + " " + XMLTAG_STATUS_PARAM + "=\"" + param
          + "\"/>");
    }
  }

  /**
   * Write a name value pair as an XML element to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param elemValue element value
   */
  public static void writeXMLElement(PrintWriter out, int indentLevel,
                                     String elemName, String elemValue) {
    out.println(indentStr(indentLevel)
        + "<" + elemName + ">" + elemValue + "</" + elemName + ">");
  }

  /**
   * Write a name value pair as an XML element to a StringBuffer.
   *
   * @param out where StringBuffer to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param elemValue element value
   */
  public static void writeXMLElement(StringBuffer out, int indentLevel,
                                     String elemName, String elemValue) {
    out.append(indentStr(indentLevel)).append("<").append(elemName).append(">");
    out.append(elemValue).append("</").append(elemName).append(">");
  }

  /**
   * Write a name as an empty XML element to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   */
  public static void writeEmptyXMLElement(PrintWriter out, int indentLevel,
                                          String elemName) {
    out.println(indentStr(indentLevel)
        + "<" + elemName + "></" + elemName + ">");
  }

  /**
   * Write an XML tag with attributes out to a PrintWriter.
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param attributes attributes
   * @param closeTag if true, close the tag with '/>'
   */
  public static void writeXMLTagWithAttrs(PrintWriter out, int indentLevel,
      String elemName, String attributes, boolean closeTag) {
    out.println(indentStr(indentLevel)
        + "<" + elemName + " " + attributes + ((closeTag)? "/>" : ">"));
  }

  /**
   * Write an XML tag with attributes out to a StringBuffer.
   *
   * @param out where StringBuffer to be written to
   * @param indentLevel the depth of indentation.
   * @param elemName element name
   * @param attributes attributes
   * @param closeTag if true, close the tag with '/>'
   */
  public static void writeXMLTagWithAttrs(StringBuffer out, int indentLevel,
      String elemName, String attributes, boolean closeTag) {
    out.append(indentStr(indentLevel)).append("<").append(elemName);
    out.append(" ").append(attributes).append((closeTag)? "/>" : ">");
  }

  /** Write an XML tag to a PrintWriter
   *
   * @param out where PrintWriter to be written to
   * @param indentLevel the depth of indentation
   * @param tagName String name of the XML tag to be added
   * @param endingTag String write a beginning tag if true or
   * an ending tag if false
   */
  public static void writeXMLTag(PrintWriter out, int indentLevel,
                                 String tagName, boolean endingTag) {
    out.println(indentStr(indentLevel)
        + (endingTag ? "</" : "<") + (tagName) + ">");
  }

  /** Write an XML tag to a StringBuffer
   *
   * @param out where StringBuffer to be written to
   * @param indentLevel the depth of indentation
   * @param tagName String name of the XML tag to be added
   * @param endingTag String write a beginning tag if true or
   * an ending tag if false
   */
  public static void writeXMLTag(StringBuffer out, int indentLevel,
                                 String tagName, boolean endingTag) {
    out.append(indentStr(indentLevel)).append(endingTag ? "</" : "<");
    out.append(tagName).append(">");
  }

  // A helper method to ident output string.
  private static String indentStr(int level) {
    if (level < XMLIndent.length) {
      return XMLIndent[level];
    } else {
      return XMLIndent[XMLIndent.length - 1]
          + indentStr(level + 1 - XMLIndent.length);
    }
  }

  private static final Pattern PREPEND_CM_PATTERN =
      Pattern.compile("\\bname\\b\\s*=\\s*[\"']");

  private static final Pattern STRIP_CM_PATTERN =
      Pattern.compile("(\\bname\\b\\s*=\\s*[\"'])CM_");

  /**
   * Given a String such as:
   * <Param name="CM_Color" value="a"/> <Param name="CM_Password" value="a"/>
   *
   * Return a String such as:
   * <Param name="Color" value="a"/> <Param name="Password" value="a"/>
   *
   * @param str String an XML string with PREFIX_CM as above
   * @return a result XML string without PREFIX_CM as above
   */
  public static String stripCmPrefix(String str) {
    Matcher matcher = STRIP_CM_PATTERN.matcher(str);
    String result = matcher.replaceAll("$1");
    return result;
  }

  /**
   * Inverse operation for stripCmPrefix.
   *
   * @param str String an XML string without PREFIX_CM as above
   * @return a result XML string with PREFIX_CM as above
   */
  public static String prependCmPrefix(String str) {
    Matcher matcher = PREPEND_CM_PATTERN.matcher(str);
    String result = matcher.replaceAll("$0CM_");
    return result;
  }

  /**
   * For Debugging: Write out the HttpServletRequest information.
   * This writes an XML stream to the response output that describes
   * most of the data received in the request structure.  It returns
   * true, so that you may call it from doGet() like:
   *   if (dumpServletRequest(req, res)) return;
   * without javac complaining about unreachable code with a straight
   * return.
   *
   * @param req An HttpServletRequest
   * @param res An HttpServletResponse
   * @returns true
   */
  public static boolean dumpServletRequest(HttpServletRequest req,
      HttpServletResponse res) throws IOException {
    res.setContentType(ServletUtil.MIMETYPE_XML);
    PrintWriter out = res.getWriter();
    ServletUtil.writeRootTag(out, false);
    ServletUtil.writeXMLTag(out, 2, "HttpServletRequest", false);
    ServletUtil.writeXMLElement(out, 3, "Method", req.getMethod());
    ServletUtil.writeXMLElement(out, 3, "AuthType", req.getAuthType());
    ServletUtil.writeXMLElement(out, 3, "ContextPath", req.getContextPath());
    ServletUtil.writeXMLElement(out, 3, "PathInfo", req.getPathInfo());
    ServletUtil.writeXMLElement(out, 3, "PathTranslated",
                                req.getPathTranslated());
    ServletUtil.writeXMLElement(out, 3, "QueryString", req.getQueryString());
    ServletUtil.writeXMLElement(out, 3, "RemoteUser", req.getRemoteUser());
    ServletUtil.writeXMLElement(out, 3, "RequestURI", req.getRequestURI());
    ServletUtil.writeXMLElement(out, 3, "RequestURL",
                                req.getRequestURL().toString());
    ServletUtil.writeXMLElement(out, 3, "ServletPath", req.getServletPath());
    ServletUtil.writeXMLTag(out, 3, "Headers", false);
    for (Enumeration names = req.getHeaderNames(); names.hasMoreElements(); ) {
      String name = (String)(names.nextElement());
      for (Enumeration e = req.getHeaders(name); e.hasMoreElements(); )
        ServletUtil.writeXMLElement(out, 4, name, (String)(e.nextElement()));
    }
    ServletUtil.writeXMLTag(out, 3, "Headers", true);
    ServletUtil.writeXMLTag(out, 2, "HttpServletRequest", true);
    ServletUtil.writeXMLTag(out, 2, "ServletRequest", false);
    ServletUtil.writeXMLElement(out, 3, "Protocol", req.getProtocol());
    ServletUtil.writeXMLElement(out, 3, "Scheme", req.getScheme());
    ServletUtil.writeXMLElement(out, 3, "ServerName", req.getServerName());
    ServletUtil.writeXMLElement(out, 3, "ServerPort",
                                String.valueOf(req.getServerPort()));
    ServletUtil.writeXMLElement(out, 3, "RemoteAddr", req.getRemoteAddr());
    ServletUtil.writeXMLElement(out, 3, "RemoteHost", req.getRemoteHost());
    Enumeration names;
    ServletUtil.writeXMLTag(out, 3, "Attributes", false);
    for (names = req.getAttributeNames(); names.hasMoreElements(); ) {
      String name = (String)(names.nextElement());
      ServletUtil.writeXMLElement(out, 4, name,
                                  req.getAttribute(name).toString());
    }
    ServletUtil.writeXMLTag(out, 3, "Attributes", true);
    ServletUtil.writeXMLTag(out, 3, "Parameters", false);
    for (names = req.getParameterNames(); names.hasMoreElements(); ) {
      String name = (String)(names.nextElement());
      String[] params = req.getParameterValues(name);
      for (int i = 0; i < params.length; i++)
        ServletUtil.writeXMLElement(out, 4, name, params[i]);
    }
    ServletUtil.writeXMLTag(out, 3, "Parameters", true);
    ServletUtil.writeXMLTag(out, 2, "ServletRequest", true);
    ServletUtil.writeRootTag(out, true);
    out.close();
    return true;
  }
  
  /**
   * Verify the request originated from either the GSA or
   * localhost.  Since the logs and the feed file may contain
   * proprietary customer information, we don't want to serve
   * them up to just anybody.
   *
   * @param gsaHost the GSA feed host
   * @param remoteAddr the IP address of the caller
   * @returns true if request came from an acceptable IP address.
   */
  public static boolean allowedRemoteAddr(String gsaHost, String remoteAddr) {
    try {
      InetAddress caller = InetAddress.getByName(remoteAddr);
      if (caller.isLoopbackAddress() ||
          caller.equals(InetAddress.getLocalHost())) {
        return true;  // localhost is allowed access
      }
      InetAddress[] gsaAddrs = InetAddress.getAllByName(gsaHost);
      for (int i = 0; i < gsaAddrs.length; i++) {
        if (caller.equals(gsaAddrs[i])) {
          return true;  // GSA is allowed access
        }
      }
    } catch (UnknownHostException uhe) {
      // Unknown host - fall through to fail.
    }
    return false;
  }
}