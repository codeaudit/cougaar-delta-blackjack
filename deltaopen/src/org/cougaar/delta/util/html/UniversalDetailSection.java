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
import java.util.*;
import java.io.*;

/**
 *  UniversalDetailSection represents one section of a {@link UniversalDetail}
 *  table. Formatting for the section must be provided by the caller, and should
 *  consist of instances of {@link UniversalDetailItem} or its subclasses.
 */
public class UniversalDetailSection {
  /**
   *  The title of this section, if it is a constant
   */
  protected String title = null;

  /**
   *  The {@link StringMap} that computes the title, if it is a computed quantity
   */
  protected StringMap titleMap = null;

  /**
   *  A flag indicating whether this section should be included in a display.
   *  Naturally, it's true by default.
   */
  protected boolean active = true;

  /**
   *  A listing of {@link UniversalDetailItem} rows to be displayed in this section
   */
  protected Vector rows = null;

  /**
   *  Construct this UniversalDetailSection with a given title
   *  @param t title of this section
   */
  public UniversalDetailSection (String t) {
    title = t;
  }

  /**
   *  Construct this UniversalDetailSection with the given map for computing
   *  the title of this section.  A String title is still provided for
   *  searching and for use as a default.
   */
  public UniversalDetailSection (String t, StringMap m) {
    if (t == null)
      throw new IllegalArgumentException("Section title cannot be null");
    title = t;
    titleMap = m;
  }

  /**
   *  Construct this UniversalDetailSection with given title and formatting
   *  information
   *  @param t title of this section
   *  @param r a Vector containing the {@link UniversalDetailItem} rows in this section
   */
  public UniversalDetailSection (String t, Vector r) {
    title = t;
    rows = r;
  }

  /**
   *  Retrieve the title of this section
   *  @return section title
   */
  public String getTitle () {
    return title;
  }

  /**
   *  Specify the title of this section.  Note:  a (non-null) titleMap will
   *  take precedence over this static title in the display.
   *  @param t new title
   */
  public void setTitle (String t) {
    title = t;
  }

  /**
   *  Retrieve the title mapping of this section
   *  @return section title map
   */
  public StringMap getTitleMap () {
    return titleMap;
  }

  /**
   *  Specify a title mapping for this section
   *  @param t new title map
   */
  public void setTitleMap (StringMap m) {
    title = null;
    titleMap = m;
  }

  /**
   *  Set a flag which controls whether this section will display itself or not
   *  in a detail display
   *  @param a set to true if this section is active, false if inactive
   */
  public void setActive (boolean a) {
    active = a;
  }

  /**
   *  Find out whether or not this section is an active part of the display
   *  @return true if it is active, false otherwise
   */
  public boolean isActive () {
    return active;
  }

  /**
   *  Find out whether or not this section is relevant to the given data bean.
   *  By default, all sections are assumed to be relevant to all data beans,
   *  but subclasses may override this function to provide more specialized
   *  behavior.
   *  @param data the bean being examined
   *  @return true if this section is relevant to the given bean; false otherwise
   */
  protected boolean conditionallyActive (DataWrapper data) {
    return true;
  }

  /**
   *  Retrieve the format info used by this section
   *  @return the Vector of {@link UniversalDetailItem} rows
   */
  public Vector getRows () {
    return rows;
  }

  /**
   *  Specify the format info for this section
   *  @param v the new Vector of {@link UniversalDetailItem} rows
   */
  public void setRows (Vector v) {
    rows = v;
  }

  /**
   *  Add an item to this UniversalDetailSection.  The new item is added at the
   *  end of the list of those already included.
   *  @param item the {@link UniversalDetailItem} to be included in this section
   */
  public void addRow (UniversalDetailItem item) {
    if (rows == null)
      rows = new Vector();
    rows.addElement(item);
  }

  /**
   *  Give an HTML representation of this section with content data drawn from
   *  the object supplied.  This method will most likely be called from the
   *  generateHtml method of the containing {@link UniversalDetail} object
   *  @param o PrintWriter to take HTML output
   *  @param data the object containing information to be displayed
   */
  public void generateHtml (PrintWriter o, DataWrapper data) {
    if (active && conditionallyActive(data))
      constructHtml(o, data);
  }

  /**
   *  Do the actual work of generating the HTML output for this section.
   *  @param o a PrintWriter for issuing HTML output
   *  @param data the data bean being displayed
   */
  protected void constructHtml (PrintWriter o, DataWrapper data) {
    // print section header
    o.println("  <tr>");
    o.println("    <td></td>");
    o.println("    <td></td>");

    String displayTitle = null;
    if (titleMap != null)
      displayTitle = titleMap.map(data);
    if (displayTitle == null && title != null)
      displayTitle = title;

    if (displayTitle != null && displayTitle.length() > 0) {
      o.println("    <td align=left valign=top colspan=6>");
      o.println("      <p class=mo2><b>" + displayTitle + "</b></p></td>");
    }
    else {
      o.println("    <td colspan=6></td>");
    }
    o.println("  </tr>");
    o.println("  <tr>");
    o.println("    <td></td>");
    o.println("    <td colspan=7 height=1 bgcolor=black>" +
      "<img src=\"/art/spacer.gif\" height=1 width=1></td>");
    o.println("  </tr>");
    o.flush();

    // print items in this section
    Enumeration enu = rows.elements();
    while (enu.hasMoreElements()) {
      ((UniversalDetailItem) enu.nextElement()).generateHtml(o, data);
    }

    // print section footer
    o.println("  <tr><td height=10></td></tr>");
    o.flush();
  }

  /**
   *  Retrieve data from HTML form elements in this section and save them in
   *  the given data bean.  This method will most likely be called from the
   *  saveFormData method of the containing {@link UniversalEditor} object.
   *  @param r the HttpServletRequest which contains the submitted form data
   *  @param data the bean which will store the submitted information
   */
  public void saveFormData (HttpServletRequest r, DataWrapper data) {
    if (!(active && conditionallyActive(data)))
      return;

    // all we need to do is poll the components
    Enumeration enu = rows.elements();
    while (enu.hasMoreElements()) {
      ((UniversalDetailItem) enu.nextElement()).saveFormData(r, data);
    }
  }

  /**
   *  Validate data from the HTML form elements in this section.  Each element
   *  in this section is polled for validity, and the contents are determined
   *  valid if each element reports valid.
   *  @param r the HttpServletRequest containing the HTML form submission
   *  @return a message indicating why the validation failed, or null
   */
  public String validateFormData (HttpServletRequest r) {
    if (!(active))
      return null;

    Enumeration enu = rows.elements();
    while (enu.hasMoreElements()) {
      String msg =
        ((UniversalDetailItem) enu.nextElement()).validateFormData(r);
      if (msg != null)
        return msg;
    }
    return null;
  }
}
