/*
  * <copyright>
  *  Copyright 2002 BBNT Solutions, LLC
  *  under sponsorship of the Defense Advanced Research Projects Agency (DARPA)
  *  and the Defense Logistics Agency (DLA).
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the Cougaar Open Source License as published by
  *  DARPA on the Cougaar Open Source Website (www.cougaar.org).
  *
  *  THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
  *  PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
  *  IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
  *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
  *  ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
  *  HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
  *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
  *  TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
  *  PERFORMANCE OF THE COUGAAR SOFTWARE.
  * </copyright>
  */

package org.cougaar.delta.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.lang.*;
import java.sql.*;
import java.io.*;
import java.text.*;


import org.cougaar.delta.util.params.ParameterFileReader;
import org.cougaar.delta.util.Factory;
import org.cougaar.delta.util.html.TemplateProcessor;
import org.cougaar.delta.util.BatchSearchResult;

/**
 * This class is the base class for all FGI servlets.  It wrapps the
 * standard servlet methods to provide common initialization, header,
 * and footer services.  It also contains accessors to the database
 * connection pool.
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: BasicServlet.java,v 1.2 2002-05-06 16:18:28 cerys Exp $
 */
public abstract class BasicServlet extends HttpServlet {


  final static String URL_PATH_SEPARATOR ="/";

  /* used in fetching arguments from the .ini file */
  final static String CLASS_NAME                 = "DeltaServlet";

  /* arguments supplied via servlet init args */
  final static String COUGAAR_INSTALL_PATH_P         = "org.cougaar.install.path";
  final static String SERVLET_PARAMETER_FILE_P   = "delta.servlet.properties";


  /* arguments supplied via the servlet .ini file */
  final static String TEMPLATE_PATH_P            = "template.Path";
  final static String TEMPLATE_TAG_PREFIX_P      = "template.tag.Prefix";
  final static String ENABLE_TIMESTAMPS_P        = "servlet.EnableTimestamps";
  final static String USE_APPLET_TAG_P           = "servlet.useAppletTag";


  protected String template_path_;
  protected String alp_install_path_;
  protected String template_tag_prefix_;
  protected ParameterFileReader parameters_;
  protected boolean use_applet_tag_;

  protected boolean debugOn = true;
  private boolean enableTimestamps = false;

  protected String userData = "USERDATA"; // key into session table for user object
  protected String sessionCookieData = "sessionCookie"; // key for session cookies String

  protected TemplateProcessor header_tp = null;

  protected final static String BATCH_SEARCH_RESULT_KEY = "batch search result";

  /**
   * Output a string to System.out if debugOn is true.
   */
  protected void dbgOut(String text_out)
  {
    if (debugOn)
      System.out.println(text_out);
  }



  /**
   * This method generates a file path in URL format relative to the URL root given
   * @param root_url -- root url "http://<host machine>:<host machine port>/"
   *
   * @param path -- Enumeration of strings that together make up a path relative to the URL
   *                (i.e. 'foo,bar,blah'  will be resolved to
   *                "http://<host machine>:<host machine port>/foo/bar/blah").
   * @return a full URL path to the file
   */
  String resolvePath(String root_url, Enumeration path)
  {
    if (! root_url.endsWith(URL_PATH_SEPARATOR))
      root_url = root_url+URL_PATH_SEPARATOR;

    StringBuffer sb = new StringBuffer();
    while (path.hasMoreElements())
    {
      sb.append(path.nextElement());
      sb.append(URL_PATH_SEPARATOR);
    }
    String p = sb.toString();
    if (p.startsWith(URL_PATH_SEPARATOR))
      p = p.substring(1);

    return root_url + p;
  }


  /**
   * Ensure that a string contains only valid characters
   * @param str the string to check
   * @param validchars the string of valid characters
   * @param casesensitive if true, require identical letter case to match
   * @return true iff all letters in str are also in validchars
   */
  boolean checkchars(String str,String validchars, boolean casesensitive) {

    // If not case sensitive then convert both value and valid
    // characters to upper case
    if (casesensitive) {
      str.toUpperCase();
      validchars.toUpperCase();
    }

    // Go through each character in value until either end or hit an invalid char
    int charposn=0;
    while ((charposn<str.length())&&(validchars.indexOf(str.charAt(charposn))!=-1)) {
      charposn++;
    }

    // Check if stop was due to end of input string or invalid char and set return code
    // accordingly
    if (charposn==str.length())
      return(true);
    else
      return(false);
  }


  /**
   * Checks to make sure that this string is in a valid date format.
   * Like MM/DD/YY
   * @param datestr the string to check
   * @return true if the string represents a valid date
   */
  boolean isValidDate(String datestr)
  {
    String validch = "1234567890/";
    String month, day, year;
    Integer I;
    int m, d, y;
    int slash1, slash2;

    datestr.trim();
    if (datestr.length() == 0)
      return false;

    if (!checkchars(datestr, validch, false)) {
      // Invalid date entry
      return false;
    }


    slash1 = datestr.indexOf("/");
    slash2 = datestr.indexOf("/",slash1+1);
    if (slash1 <= 0 || slash2 <= 0)
    {
      // Invalid Entry
      return false;
    }

    month = datestr.substring(0,slash1);
    day = datestr.substring(slash1+1,slash2);
    year = datestr.substring(slash2+1,datestr.length());
    if ((month.length()<1 || month.length()>2) ||
      (day.length()<1 || day.length()>2) || (year.length()!=4))
    {
      // Invalid Date
      return false;
    }

    I = new Integer(month);
    m = I.intValue();
    I = new Integer(day);
    d = I.intValue();
    I = new Integer(year);
    y = I.intValue();
    //basic error checking
    if (m<1 || m>12 || d<1 || d>31 || y<0 || y>9999)
      return false;

    // months with 30 days
    if (d==31 && (m==4 || m==6 || m==9 || m==11)){
      return false;
    }

    // february, leap year
    if (m==2 && d>28){
      if (d > 29)
        return false;
      if ((y%4 == 0) || ((y%400==0) && (y%100!=0)))
        return false;
    }

    return true;
  }

    /**
     * Reads the initialization parameters from the .INI file
     *
     * @param cfg servlet configuration information
     */
  protected void getInitParameters(ServletConfig cfg) throws ServletException
  {
    alp_install_path_ = cfg.getInitParameter(COUGAAR_INSTALL_PATH_P);
    if (alp_install_path_ == null) {
      throw new ServletException("argument 'org.cougaar.install.path' undefined");
    }
    if (! alp_install_path_.endsWith(File.separator))
      alp_install_path_ = alp_install_path_ + File.separator;

    System.setProperty(COUGAAR_INSTALL_PATH_P, alp_install_path_);

    String parameters_file   = cfg.getInitParameter(SERVLET_PARAMETER_FILE_P);
    if (parameters_file == null)
      throw new ServletException("argument '"+SERVLET_PARAMETER_FILE_P+"' undefined");


   	parameters_ = ParameterFileReader.getInstance(parameters_file);
    template_tag_prefix_  = parameters_.getParameter(CLASS_NAME, TEMPLATE_TAG_PREFIX_P, "DELTA_");
    use_applet_tag_  = parameters_.getParameter(CLASS_NAME, USE_APPLET_TAG_P, false);
    enableTimestamps  = parameters_.getParameter(CLASS_NAME, ENABLE_TIMESTAMPS_P, false);

    String fs = File.separator;
    template_path_ = alp_install_path_ + ParameterFileReader.concatenate(parameters_.getParameterValues(CLASS_NAME, TEMPLATE_PATH_P), fs, "") + fs;

    if (template_path_.length() == 0)
      throw new ServletException("argument '"+TEMPLATE_PATH_P+"' undefined");

    if (! template_path_.endsWith(File.separator))
      template_path_ = template_path_ + File.separator;

  }

    /**
     * Called when the servlet is loaded into the server for the first time.
     * Initializes the database connection pool and calls
     * processInit()
     *
     * @param cfg the servlet configuration parameters
     *
     * @exception ServletException if the request could not be handled
     * @see org.cougaar.delta.servlet.FgiServlet#processInit()
     */
   public final void init(ServletConfig cfg) throws ServletException
  {
    super.init(cfg);
    String fs = File.separator;

    /* retrieve init arguments */
    getInitParameters(cfg);

    Factory.setParameters(parameters_);

    processInit(cfg);
  }

    /**
     * This method can be overridden by subclasses to do something when
     * the servlet is loaded into the server for the first time.
     *
     * @param cfg the servlet configuration parameters
     *
     * @exception ServletException if the request could not be handled
     * @see org.cougaar.delta.servlet.FgiServlet#processDo()
     */
  protected void processInit(ServletConfig cfg) throws ServletException
  {
  }

    /**
     * Called when the servlet is unloaded from the server.
     * Calls processDestroy() and destroys the connection pool if necessary
     *
     * @see org.cougaar.delta.servlet.FgiServlet#processDestroy()
     */
  public final void destroy()
  {
    processDestroy();
    super.destroy();
  }

    /**
     * Can be used by subclasses to do something when the servlet is unloaded from the server.
     *
     * @see org.cougaar.delta.servlet.FgiServlet#destroy()
     */
  protected void processDestroy()
  {
  }

  /**
   *  Subclasses that wish to specify HTTP headers other than the default
   *  "Expires:  0" should override this method.  Which headers
   *  should be included in the response may depend on the nature of the
   *  request.
   *  <br><br>
   *  Note:  for most transactions, it is important that pages are not served
   *  from the browser's cache, so caution is advised.
   *  @param request the HTTP request
   *  @param response the HTTP response
   */
  protected void setHttpHeaders (
      HttpServletRequest request, HttpServletResponse response)
  {
    response.setHeader("Expires", "0");
  }

    /**
     * Service the HTTP GET request.  Sets up header and session info and calls
     * processGet()
     *
     *
     * @param request HttpServletRequest that encapsulates the request to
     * the servlet
     * @param response HttpServletResponse that encapsulates the response
     * from the servlet
     *
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     * @see org.cougaar.delta.servlet.FgiServlet#processGet()
     */
  public final void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    try {
      cleanUpSessionConnection(request.getSession(true));
      setHttpHeaders(request, response);
//      checkAuthentication(request, response);
      generateGetHeader(request, response);
      processGet(request, response);
      generateGetFooter(request, response);
    } catch (IOException e)
    {
      e.printStackTrace();
      throw e;
    }
    catch (ServletException e)
    {
      e.printStackTrace();
      throw e;
    }
//    catch (FGIPermissionException e)
//    {
//      System.err.println("Access control violation: "+e.getMessage());
//      e.printStackTrace(System.err);
//    }
  }

  /**
   * Ensure that a session has been established and a user logged in
   */
   /*
  private void checkAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws FGIPermissionException
  {
    // This getSession must be done before any output is written or else
    // the session state gets messed up
    // This strangeness is documented in the man page for Interface javax.servlet.http.HttpServletRequest
    HttpSession session = request.getSession(true);

    if (isUseAuthentication())
    {
      if ((session == null) || (session.getValue(userData) == null)) // not logged in
      {
        generatePermissionDeniedPage(request, response);
        throw new FGIPermissionException("User not logged in");
      }
    }
  }*/

  /**
   * Generate and output page that says "You can't do that"
   * @param request the user's illegitimate request
   * @param response the HttpServletResponse passed to doGet or doPost
   *//*
  protected void generatePermissionDeniedPage(
      HttpServletRequest request, HttpServletResponse response)
  {
    try {
      TemplateProcessor tp = new TemplateProcessor(template_path_ + "permissionDenied.html");
      PrintWriter pw = new PrintWriter(response.getOutputStream());
      pw.println(tp.process().toString());
      pw.close();
    } catch (IOException e)
    {
      System.err.println("Error generating permission denied page:"+e.getMessage());
    }
  }*/


    /**
     * This method is for subclasses to service the HTTP GET request.
     * Override this to implement responses to GETs
     *
     * @param request HttpServletRequest that encapsulates the request to
     * the servlet
     * @param response HttpServletResponse that encapsulates the response
     * from the servlet
     *
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     * @see org.cougaar.delta.servlet.FgiServlet#doGet()
     */
  public void processGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
  }

  /**
   *  This method generates the header of the HTML response for a GET request.
   *  The default behavior is to return the standardized look and feel of DELTA
   *  pages, but subclasses can customize the behavior by overriding this
   *  method.
   *
   *  @param request the HttpServletRequest as passed in by the web server
   *  @param response the HttpServletResponse as passed in by the web server
   */
  public void generateGetHeader (
      HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    BuildHtmlHeader(new PrintWriter (response.getOutputStream()), getTitle());
  }

  /**
   *  This method generates the footer of the HTML response for a GET request.
   *  The default behavior is to return the standardized look and feel of DELTA
   *  pages, but subclasses can customize the behavior by overriding this
   *  method.
   *
   *  @param request the HttpServletRequest as passed in by the web server
   *  @param response the HttpServletResponse as passed in by the web server
   */
  public void generateGetFooter (
      HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    BuildHtmlFooter(new PrintWriter (response.getOutputStream()));
  }

    /**
     * Service the HTTP POST request.  Sets up header and session info and calls
     * processPost()
     *
     *
     * @param request HttpServletRequest that encapsulates the request to
     * the servlet
     * @param response HttpServletResponse that encapsulates the response
     * from the servlet
     *
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     * @see org.cougaar.delta.servlet.FgiServlet#processPost()
     */
  public final void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    try {
      setHttpHeaders(request, response);
//      checkAuthentication(request, response);
      generatePostHeader(request, response);
      processPost(request, response);
      generatePostFooter(request, response);
    } catch (IOException e)
    {
      e.printStackTrace();
      throw e;
    }
    catch (ServletException e)
    {
      e.printStackTrace();
      throw e;
    }
//    catch (FGIPermissionException e)
//    {
//      System.err.println("Access control violation: "+e.getMessage());
//    }
  }

    /**
     * This method is for subclasses to service the HTTP POST request.
     * Override this to implement responses to POSTs
     *
     * @param req HttpServletRequest that encapsulates the request to
     * the servlet
     * @param resp HttpServletResponse that encapsulates the response
     * from the servlet
     *
     * @exception IOException if detected when handling the request
     * @exception ServletException if the request could not be handled
     * @see org.cougaar.delta.servlet.FgiServlet#doPost()
     */
  public void processPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
  }

  /**
   *  This method generates the header of the HTML response for a POST request.
   *  The default behavior is to return the standardized look and feel of DELTA
   *  pages, but subclasses can customize the behavior by overriding this
   *  method.
   *
   *  @param request the HttpServletRequest as passed in by the web server
   *  @param response the HttpServletResponse as passed in by the web server
   */
  public void generatePostHeader (
      HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    request.getParameter("Blabbo"); // Dummy read to send continue back to the client.
    BuildHtmlHeader(new PrintWriter (response.getOutputStream()), getTitle());
  }

  /**
   *  This method generates the footer of the HTML response for a POST request.
   *  The default behavior is to return the standardized look and feel of DELTA
   *  pages, but subclasses can customize the behavior by overriding this
   *  method.
   *
   *  @param request the HttpServletRequest as passed in by the web server
   *  @param response the HttpServletResponse as passed in by the web server
   */
  public void generatePostFooter (
      HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    BuildHtmlFooter(new PrintWriter (response.getOutputStream()));
  }


  /**
   * Construct the HTML prologue common to all servlet pages
   * @param out the HTML output stream
   * @param title the HTML page title
   */
  protected void BuildHtmlHeader(PrintWriter out, String title)   throws IOException
  {
    out.println("<HTML>");
    out.println("<head>");
    out.println("<link rel=\"stylesheet\" href=\"/css/nny.css\" type=\"text/css\">");
    out.println("<TITLE>");
    out.println(title);
    out.println("</TITLE>");
    out.println("<SCRIPT LANGUAGE=\"JavaScript\" SRC=\"/javascript/data_validation.js\"></SCRIPT>");
    out.println("</head>");
    /*
    if (navbarTemplate != null)
    {
      if (header_tp == null)
      {
        header_tp = new TemplateProcessor(template_path_ + navbarTemplate);
      }
      out.println(header_tp.process().toString());
    }*/
    out.flush();
  }

  /**
   * Construct the HTML epilogue common to all servlet pages
   * @param out the HTML output stream
   */
  protected void BuildHtmlFooter(PrintWriter out) throws IOException
  {
    out.println("</BODY></HTML>");
    out.flush();
  }

  /**
   * Fetch the title for this HTML page.
   * Subclasses should implement this to return their title string.
   * @return The page title for this HTML page
   */
  public abstract String getTitle();

  /**
   * Set the name of the template file to be used for the navigation bar
   * @param newNavbarTemplate filename of the navigation bar template
   */
   /*
  public void setNavbarTemplate(String newNavbarTemplate) {
    // if (newNavbarTemplate == null) throw new NullPointerException();
    navbarTemplate = newNavbarTemplate;
    if (navbarTemplate != null) {
      try {
        header_tp = new TemplateProcessor(template_path_ + navbarTemplate);
      }
      catch (Exception oh_no) {
        System.out.println("FgiServlet::setNavbarTemplate:  file " +
          navbarTemplate + " not found");
      }
    }
    else header_tp = null;
  }*/

  /**
   * Get the name of the template file to be used for the navigation bar
   * @return the filename of the navigation bar template
   */
//  public String getNavbarTemplate() {
//    return navbarTemplate;
//  }

  /**
   * Set whether or not this servlet should require the user to be
   * authenticated (logged in) to the system before responding to requests.
   * @param newUseAuthentication if true, the user must login through the LoginServlet
   * before accessing this servlet
   *//*
  public void setUseAuthentication(boolean newUseAuthentication) {
    useAuthentication = newUseAuthentication;
  }*/

  /**
   * Determine whether or not this servlet should require the user to be
   * authenticated (logged in) to the system before responding to requests
   * @return true if the user must login before accessing this servlet.
   *//*
  public boolean isUseAuthentication() {
    return useAuthentication;
  }*/

  /**
   * Clients using POST for HTTP tunneling should use this to get parameters from
   * the URL.
   */
  protected String getParameter(HttpServletRequest request, String name)
  {
    String ret = request.getParameter(name);
    String query = request.getQueryString();
    String method = request.getMethod();
    if (ret == null && query != null && method.equalsIgnoreCase("POST")) {
      Hashtable h = HttpUtils.parseQueryString(query);
      String s[] = (String [])h.get(name);
      if (s != null && s.length > 0)
        ret = s[0];
   }
   return ret;
  }

  protected void setFgiHistoryRoot (String url, HttpSession sess) {
    Vector history = (Vector) sess.getValue("FgiServlet_History");
    if (history == null) {
      history = new Vector();
      sess.putValue("FgiServlet_History", history);
    }
    history.clear();
    history.add(url);
  }

  protected void addFgiHistorySite (String url, HttpSession sess) {
    Vector history = (Vector) sess.getValue("FgiServlet_History");
    if (history == null) {
      history = new Vector();
      sess.putValue("FgiServlet_History", history);
    }
    history.add(url);
  }

  protected void fgiHistoryBack (PrintWriter o, HttpSession sess) {
    Vector history = (Vector) sess.getValue("FgiServlet_History");
    int n;
    if (history != null && (n = history.size()) > 1) {
      o.println("<script>location.replace(\"" +
        history.elementAt(n - 2) + "\");</script>");
      o.flush();
      history.removeElementAt(n - 1);
    }
  }

  protected boolean isJrun = false;
  protected boolean jrunChecked = false;
  protected java.lang.reflect.Method setKeepAlive = null;
  protected Boolean [] falses = {Boolean.FALSE};
  private SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

  /**
   * This method is overridden to turn of the HTTP Keep-Alive header entry
   * if this is a JRun server. Otherwise, we get mysterious behavior.
   */
  protected void service(HttpServletRequest parm1, HttpServletResponse parm2) throws javax.servlet.ServletException, java.io.IOException {

    /*
     * This code set the keepAlive property to false if this is a JRunServletResponse object
     * and then calls the doPost method.
     */
    if (enableTimestamps) {
      String date = formatter.format(new java.util.Date());
      String query = parm1.getQueryString() == null ? "" : "/" + parm1.getQueryString();
      System.out.println("Received request: " + parm1.getRequestURI() + query + " at " + date);
    }

    String type = parm1.getContentType();
    if ((type != null) && type.equals("application/octet-stream"))
    {
      if (!jrunChecked) // remember whether we introspected already
      {
        Class c = parm2.getClass();
        jrunChecked = true;
        if (c.getName().endsWith("JRunServletResponse"))
        {
          Class [] parms = {boolean.class};
          try {
            setKeepAlive = c.getMethod("setKeepAlive", parms);
          } catch (Exception nsm)
          {
            System.out.println("Error getting setKeepAlive method " + nsm);
          }
        }
      }
      if (setKeepAlive != null)
      {
        try {
          setKeepAlive.invoke(parm2, falses);
        } catch (Exception ex)
        {
          System.out.println("Error turning off Keep-Alive" + ex);
        }
      }
      doPost(parm1, parm2);
    }
    else // anything except application/octet-stream
      super.service( parm1,  parm2);

    if (enableTimestamps) {
      String date = formatter.format(new java.util.Date());
      String query = parm1.getQueryString() == null ? "" : "/" + parm1.getQueryString();
      System.out.println("Completed request: " + parm1.getRequestURI() + query + " at " + date);
    }
  }

  /**
   * If there is a BatchSearchResult in this session which was not put there
   * by this servlet, close its connection
   */
  private void cleanUpSessionConnection(HttpSession sess) {
    BatchSearchResult bsr = (BatchSearchResult)sess.getValue(BATCH_SEARCH_RESULT_KEY);
    if(bsr!=null) {
      if(!bsr.getServletTitle().equals(getTitle())) {
        bsr.close();
      }
    }
  }

}
