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

package org.cougaar.delta.applet;

import java.net.*;
import java.io.*;


/**
 * This class is used by an applet to communicate with a servlet.  Serialized objects and strings can be exchanged
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: AppletToServletChannel.java,v 1.1 2002-04-30 17:33:26 cerys Exp $
 */
public class AppletToServletChannel {

  protected String urlBase = null;
  private int numRetries = 1;
  private int retryCount;
  private boolean debug = false;

  // if the cookie string is non-null, then it is used for all requests to the
  // server.
  private String cookie = null;

  /**
   *  Specify the string used in the cookie property for connections to the
   *  servlets.  Presumably, these cookie values be set by the calling Applet
   *  and will contain the relevant session ID in use by the web server.  A
   *  null value indicates that this feature is not being used.
   *  @param s The new string of cookie values.
   */
  public void setCookie (String s) {
    cookie = s;
  }

  /**
   *  Report the cookie values currently in use by this channel, if any.
   *  @return the cookies.
   */
  public String getCookie () {
    return cookie;
  }

  // add the cookies to the headers of an HTTP request
  private void insertCookie (URLConnection conn) {
    if (cookie != null)
      conn.setRequestProperty("Cookie", cookie);
  }


  /**
   * Create a new servlet communication channel
   * @param the prefix to the servlet URL.  Other parameters are appended in the post and request methods.
   */
  public AppletToServletChannel(String urlBase)
  {
      this.urlBase = urlBase;
  }

  /**
   *  Send a request for information to the server and read the response.
   *  @param u The String representation of the URL suffix
   *  @param command A token telling the server what to do with the request
   *  @param message The body of the request
   *  @return A String representation of the response from the server
   */
  public String serverRequest (String u, String command, String message) {
    retryCount = getNumRetries();
    try {
      return serverRequest(new URL(urlBase + u), command, message);
    }
    catch (MalformedURLException mfe) {
      mfe.printStackTrace();
    }
    return null;
  }

  /**
   *  Send a request for information to the server and read the response.
   *  @param u The URL being queried
   *  @param command A token telling the server what to do with the request
   *  @param message The body of the request
   *  @return A String representation of the response from the server
   */
  protected String serverRequest (URL u, String command, String message) {
    try {
      URLConnection conn = u.openConnection();
      conn.setRequestProperty("Content-type", "text/plain");
      insertCookie(conn);
      conn.setDoInput(true);
      conn.setDoOutput(true);
      PrintWriter o = new PrintWriter(conn.getOutputStream());
      o.println("command=" + command);
      o.println(message);
      o.close();

      BufferedReader bufr = new BufferedReader(new InputStreamReader(
        conn.getInputStream()));
      StringBuffer response = new StringBuffer();
      String inLine;
      while ((inLine = bufr.readLine()) != null) {
        response.append(inLine);
      }
      bufr.close();
      return response.toString();
    }
    catch (java.io.StreamCorruptedException jrunBad) {
      dbg("Retry--" + retryCount);
      retryCount--;
      if (retryCount >= 0)
        return serverRequest(u, command, message);
    }
    catch (Exception oh_no) {
      System.out.println("AppletToServletChannel::serverRequest:  ERROR--" + oh_no);
    }
    return null;
  }

  /**
   *  Send a request for an input stream to a server.
   *  @param u The String representation of the URL suffix
   *  @param obj the information to send to the server.
   *  @return The stream response from the server.
   */
  public ObjectInputStream objectRequest (String u, AppletToServletParcel obj)
  {
    dbg("AppletToServletChannel::objectRequest: " + u + " parcel " + obj);
    retryCount = getNumRetries();
    try {
      return objectRequest(new URL(urlBase + u), obj);
    }
    catch (MalformedURLException mfe)
    {
      mfe.printStackTrace();
    }
    return null;
  }

  private byte [] makeByteArray(Object obj)
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      ObjectOutputStream oos = new ObjectOutputStream(out);
      oos.writeObject(obj);
      oos.flush();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    return out.toByteArray();
  }

  /**
   *  Send a request for an input stream to a server.
   *  @param u The URL to access.
   *  @param obj the information to send to the server.
   *  @return The stream response from the server.
   */
  protected ObjectInputStream objectRequest (URL u, AppletToServletParcel obj)
  {
    try {
      URLConnection conn = u.openConnection();
      conn.setRequestProperty("Content-type", "application/octet-stream");
      insertCookie(conn);
      conn.setDoInput(true);
      conn.setDoOutput(true);
      byte [] buf = makeByteArray(obj);
//      conn.setRequestProperty("Content-type", "application/octet-stream");
      conn.setRequestProperty("Content-length", ""+buf.length);
      DataOutputStream oos = new DataOutputStream(conn.getOutputStream());
      oos.write(buf);
      oos.flush();
      oos.close();

      //
      // read the response
      //
      return new ObjectInputStream(conn.getInputStream());
    }
    catch (java.io.IOException jrunBad) {
      dbg("Retry--" + retryCount);
      retryCount--;
      if (retryCount >= 0)
        return objectRequest(u, obj);
      else
      {
        System.out.println("AppletToServletChannel::objectRequest:  ERRPR--" + jrunBad);
        jrunBad.printStackTrace();
      }
    }
    catch (Exception oh_no) {
      System.out.println("AppletToServletChannel::objectRequest:  ERROR--" + oh_no);
      oh_no.printStackTrace();
    }
    return null;
  }

  /**
   *  Send an object to a server.
   *  @param u The String representation of the URL suffix
   *  @param obj object package to send to the server.
   *  @return The string response from the server.
   */
  public String objectPost (String u, AppletToServletParcel obj)
  {
    retryCount = getNumRetries();
    try {
      return objectPost(new URL(urlBase + u), obj);
    }
    catch (MalformedURLException mfe)
    {
      mfe.printStackTrace();
    }
    return null;
  }

  /**
   *  Send an object to a server.
   *  @param u The URL to access.
   *  @param obj object package to send to the server.
   *  @return The string response from the server.
   */
  protected String objectPost (URL u, AppletToServletParcel obj)
  {
    try {
      URLConnection conn = u.openConnection();
      conn.setRequestProperty("Content-type", "text/plain");
      insertCookie(conn);
      conn.setDoInput(true);
      conn.setDoOutput(true);
      ObjectOutputStream oos = new ObjectOutputStream(conn.getOutputStream());
      oos.writeObject(obj);
      oos.close();

      BufferedReader bufr = new BufferedReader(new InputStreamReader(
        conn.getInputStream()));
      StringBuffer response = new StringBuffer();
      String inLine = null;
      while ((inLine = bufr.readLine()) != null) {
        response.append(inLine);
      }
      bufr.close();
      return response.toString();
    }
    catch (java.io.StreamCorruptedException jrunBad) {
      dbg("Retry--" + retryCount);
      retryCount--;
      if (retryCount >= 0)
        return objectPost(u, obj);
    }
    catch (Exception oh_no) {
      System.out.println("AppletToServletChannel::objectPost:  ERROR--" + oh_no);
    }
    return null;
  }


  private static void exercise(String which)
  {
    AppletToServletChannel ch = new AppletToServletChannel("http://al:8000/servlet/ContractServlet");
    ch.setNumRetries(1);
    AppletToServletParcel box = new AppletToServletParcel(which, null);
    try {
    box = (AppletToServletParcel)(ch.objectRequest("?command=GETGEOGRAPHY", box)).readObject();
    }
    catch (Exception b_s) {
      System.out.println("ContractEditApplet::getLTAFromServlet:  ERROR--" + b_s);
    }
  }
  /**
   * A test stub
   */
  public static void main(String [] args)
  {
    for (int i=0; i<100; i++)
    {
      exercise("United States One");
      exercise("United States Two");
      System.out.print(".");
    }
  }

  /**
   * Set the count of how many times a server operation will be retried before giving up.
   * Default value is 1 (a failed operation will be retried once)
   * @param newNumRetries the new value
   */
  public void setNumRetries(int newNumRetries) {
    numRetries = newNumRetries;
  }

  /**
   * Get the count of how many times a server operation will be retried before giving up.
   * Default value is 1 (a failed operation will be retried once)
   * @return the value
   */
  public int getNumRetries() {
    return numRetries;
  }

  /**
   * Set whether or not debug text will be sent to System.out.  Default
   * is false.
   * @param newDebug if true, debug text will be sent to System.out
   */
  public void setDebug(boolean newDebug) {
    debug = newDebug;
  }

  /**
   * Get whether or not debug text will be sent to System.out.
   * @return if true, debug text will be sent to System.out
   */
  public boolean isDebug() {
    return debug;
  }

  private void dbg(String text)
  {
    if (debug)
      System.out.println(text);
  }
}
