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

import java.io.*;

/**
 * This class is used to ferry objects between an Applet and a Servlet.
 *
 * @see AppletToServletChannel
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: AppletToServletParcel.java,v 1.1 2002-04-30 17:33:26 cerys Exp $
 */
public class AppletToServletParcel implements Serializable {
  /**
   * A command that is meaningful to the server.
   */
  public String command = null;
  /**
   * The serializable object to be transmitted to and/or from the server.
   */
  public Serializable parcel = null;

  /**
   * Create an uninitialized parcel
   */
  public AppletToServletParcel () {
  }

  /**
   * Create an initialized parcel.
   * @param c the command.
   * @param p the serializable object payload
   */
  public AppletToServletParcel (String c, Serializable p) {
    command = c;
    parcel = p;
  }

  /**
   * Serialize this object
   * @param oos the output stream to serialize to
   * @exception IOException
   */
  private void writeObject (java.io.ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
  }

  /**
   * De-serialize this object
   * @param ois the input stream to serialize from
   * @exception ClassNotFoundException
   * @exception IOException
   */
  private void readObject (ObjectInputStream ois)
      throws ClassNotFoundException, IOException
  {
    ois.defaultReadObject();
  }

  public String toString()
  {
    return "Command: " + command + " Parcel: " + parcel;
  }
}
