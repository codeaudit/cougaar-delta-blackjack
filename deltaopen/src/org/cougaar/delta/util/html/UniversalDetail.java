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
 *  UniversalDetail is a class that can be configured (or subclassed) to
 *  give an HTML representation of a data record.  To be useful, the following
 *  must hold:
 *  <ul>
 *    <li>
 *      the class used to represent the data records must adhere to the
 *      JavaBeans accessor naming conventions.
 *    </li>
 *    <li>
 *      the record to be displayed must be ensconsed in a DataWrapper object.
 *    </li>
 *    <li>
 *      Formatting information is provided by the caller (or by a subclass)
 *      in the form of a vector of UniversalDetailSection objects (see below).
 *    </li>
 *  </ul>
 */
public class UniversalDetail {
  protected Vector sections = null;
  protected StringMap titleMap = null;
  protected String title = null;

  /**
   *  Construct a UniversalDetail with no initial configuration.
   *  Only subclasses are allowed to do this.
   */
  protected UniversalDetail () {
  }

  /**
   *  Construct a UniversalDetail object with a specific function that produces
   *  the table's title based on the contents of the data record
   *  @param sm StringMap to compute titles
   */
  public UniversalDetail (StringMap sm) {
    titleMap = sm;
    sections = new Vector();
  }

  /**
   *  Construct a UniversalDetail object with a specific static title
   *  @param t Table's title
   */
  public UniversalDetail (String t) {
    title = t;
    sections = new Vector();
  }

  /**
   *  Add a new section with the given title and return a reference for the
   *  caller to configure
   *  @param t the static title for the new section
   *  @return newly created UniversalDetailSection
   */
  public UniversalDetailSection addSection (String t) {
    UniversalDetailSection uds = new UniversalDetailSection(t);
    sections.addElement(uds);
    return uds;
  }

  /**
   *  Add the given UniversalDetailSection to the end of the sections Vector
   *  in this table format.
   *  @param the section to be added
   */
  public void addSection (UniversalDetailSection uds) {
    if (uds != null)
      sections.addElement(uds);
  }

  /**
   *  Add a new section with the given title at the given index and return a reference for the
   *  caller to configure
   *  @param t the static title for the new section
   *  @param index the index at which to add this section
   *  @return newly created UniversalDetailSection
   */
  public UniversalDetailSection insertSection (String t, int index) {
    if (index < sections.size()) {
      UniversalDetailSection uds = new UniversalDetailSection(t);
      sections.insertElementAt(uds, index);
      return uds;
    }
    return null;
  }

  /**
   *  Add the given UniversalDetailSection at index in the sections Vector
   *  in this table format.
   *  @param uds the section to be added
   *  @param index at which to add the section
   */
  public void insertSection (UniversalDetailSection uds, int index) {
    if (uds != null && sections.size()>index)
      sections.insertElementAt(uds, index);
  }

  /**
   *  Remove a section from this table and return a reference to it
   *  @param index in the list of sections of the section to be removed
   *  @return the UniversalDetailSection extracted from the table
   */
  public UniversalDetailSection removeSection (int i) {
    if (sections == null || sections.size() <= i)
      return null;
    UniversalDetailSection uds = (UniversalDetailSection) sections.elementAt(i);
    sections.removeElementAt(i);
    return uds;
  }

  /**
   *  Search through this table's sections for one with the given name and
   *  remove it from the list, returning a reference, as above.
   *  @param t title of the section to be removed
   *  @return the UniversalDetailSection extracted from the table
   */
  public UniversalDetailSection removeSection (String t) {
    Enumeration enu = sections.elements();
    while (enu.hasMoreElements()) {
      UniversalDetailSection uds = (UniversalDetailSection) enu.nextElement();
      String udsTitle = uds.getTitle();
      if (udsTitle != null && udsTitle.equals(t)) {
        sections.removeElement(uds);
        return uds;
      }
    }
    return null;
  }

  /**
   *  Generate the HTML for the title of this table
   *  @param o a PrintWriter to take the output
   *  @param data a wrapped data bean from which the title may be derived
   */
  protected void generateTitle (PrintWriter o, DataWrapper data) {
    // print title line
    String titleText = (titleMap == null ? title : titleMap.map(data));
    o.println("<center><p class=mo11>" + titleText + "</p></center>");
    o.flush();
  }

  /**
   *  Generate the HTML representation for this table.  The sections of the
   *  table are examined in the order in which they appear in the sections
   *  Vector, and each is called upon to supply its own HTML representation.
   *  @param o a PrintWriter to take the output
   *  @param data the bean containing the data being displayed
   */
  protected void generateTable (PrintWriter o, DataWrapper data) {
    // open table structure ...
    o.println("<table cellspacing=0 cellpadding=0 border=0 width=650>");
    // ... and set up column dimensions
    o.println("  <tr>");
    o.println("    <td align=left valign=top width=11></td>");
    o.println("    <td align=left valign=top width=29></td>");
    o.println("    <td align=left valign=top width=242></td>");
    o.println("    <td align=left valign=top width=2></td>");
    o.println("    <td align=left valign=top width=12></td>");
    o.println("    <td align=left valign=top width=332></td>");
    o.println("    <td align=left valign=top width=2></td>");
    o.println("    <td align=left valign=top width=20></td>");
    o.println("  </tr>");
    o.flush();

    // poll the sections for their contents
    Enumeration enu = sections.elements();
    while (enu.hasMoreElements()) {
      ((UniversalDetailSection) enu.nextElement()).generateHtml(o, data);
    }

    // close off table structure
    o.println("</table>");
    o.flush();
  }

  /**
   *  Write an HTML representation of this table using the given data record
   *  to supply content and the formatting provided by the member variable
   *  "sections".
   *  @param o a PrintWriter to take the output
   *  @param data the content information for this display
   */
  public void generateHtml (PrintWriter o, DataWrapper data) {
    generateTitle (o, data);
    generateTable (o, data);
  }
}
