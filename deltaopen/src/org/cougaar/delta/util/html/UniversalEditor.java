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

package org.cougaar.delta.util.html;

import org.cougaar.delta.util.variant.*;
import javax.servlet.http.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/**
 *  UniversalEditor is a subclass of {@link UniversalDetail} designed to provide
 *  the user with HTML form elements in which to enter or modify the properties
 *  of a data bean.  This class is able to populate the form with existing
 *  information and to retrieve values from the form when submitted by the user.
 */
public class UniversalEditor extends UniversalDetail {

  /**
   *  Create this UniversalEditor on top of the appropriate {@link UniversalDetail}
   *  @param t the title of this table
   */
  public UniversalEditor (String t) {
    super(t);
  }

  /**
   *  Create this UniversalEditor on top of the appropriate {@link UniversalDetail}
   *  @param m a {@link StringMap} to calculate the title of this table
   */
  public UniversalEditor (StringMap m) {
    super(m);
  }

  /**
   *  Generate the HTML representation for this table
   *  @param o a PrintWriter to take the output
   *  @param data the JavaBean from which to derive initial data for the form
   */
  public void generateHtml (PrintWriter o, DataWrapper data) {
    generateTitle(o, data);
    o.println("<form name=UniversalEditor method=GET>");
    o.println("<input name=command type=hidden value=SAVE></input>");
    generateTable(o, data);
    o.println("</form>");
  }

  /**
   *  Store data from the HTML form elements in the given JavaBean
   *  @param r the HttpServletRequest containing the form data
   *  @param data the JavaBean used to store the info
   */
  public void saveFormData (HttpServletRequest r, DataWrapper data) {
    // simply need to poll the components
    Enumeration enu = sections.elements();
    while (enu.hasMoreElements()) {
      ((UniversalDetailSection) enu.nextElement()).saveFormData(r, data);
    }
  }

  /**
   *  Validate data from the HTML form elements.  Each section in this editor
   *  is polled for the validity of its component elements.  The contents are
   *  determined valid if each section reports valid.
   *  @param r the HttpServletRequest containing the HTML form submission
   *  @return a message indicating why the validation failed, or null
   */
  public String validateFormData (HttpServletRequest r) {
    Enumeration enu = sections.elements();
    while (enu.hasMoreElements()) {
      String msg =
        ((UniversalDetailSection) enu.nextElement()).validateFormData(r);
      if (msg != null)
        return msg;
    }
    return null;
  }
}
