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
import java.io.*;

/**
 *  UniversallDetailItem represents a single row of HTML output in a
 *  {@link UniversalDetail} display.  Any class which is to be included as a
 *  component in a {@link UniversalDetail} table should be a subclass of this one.
 */
public class UniversalDetailItem {
  /**
   *  The description of this row as displayed in the HTML output
   */
  protected String name = null;
  protected String explanation = "";

  /**
   *  Conceivably, one might want to have a {@link StringMap} to produce the row name
   *  for a given field.
   */
  protected StringMap nameMap = null;

  /**
   *  The name of the data property that supplies this row's content info
   */
  protected String field = null;

  /**
   *  In some cases, the content to be displayed cannot be obtained simply as
   *  a property of the data object.  For these, an algorithm in the form of
   *  a {@link StringMap} is supplied to calculate the value to be displayed
   */
  protected StringMap contentMap = null;

  /**
   *  By default, the displayed values are assumed to be literal text, so
   *  symbols used in the HTML syntax are escaped and newline characters are
   *  converted to HTML linebreak tags "<br>".  Setting the ueseHtml flag to
   *  true overrides the default behavior, causing text to be interpreted by
   *  the browser as HTML.
   */
  protected boolean usesHtml = false;

  /**
   *  Construct a new UniversalDetailItem.  Only subclasses are allowed to call
   *  the constructor for this class without specifying a value or a String map
   *  for the name and value of the item.
   */
  protected UniversalDetailItem () {
  }

  /**
   *  Construct this UniversalDetailItem with given HTML name and content field
   *  @param n the HTML descriptor for this row
   *  @param f the field name for this row's content
   */
  public UniversalDetailItem (String n, String f) {
    name = n;
    field = f;
  }
  /**
   *  Construct this UniversalDetailItem with given HTML name and content field
   *  @param n the HTML descriptor for this row
   *  @param e the help message string for <n>
   *  @param f the field name for this row's content
   */
  public UniversalDetailItem (String n, String e, String f) {
    name = n;
    explanation = e;
    field = f;
  }

  /**
   *  Construct a new UniversalDetailItem with given HTML name and content map
   *  @param n the HTML descriptor for this row
   *  @param m the {@link StringMap} that calculates this row's content
   */
  public UniversalDetailItem (String n, StringMap m) {
    contentMap = m;
    name = n;
  }
  /**
   *  Construct a new UniversalDetailItem with given HTML name and content map
   *  @param n the HTML descriptor for this row
   *  @param e the help message string for <n>
   *  @param m the StringMap that calculates this row's content
   */
  public UniversalDetailItem (String n, String e, StringMap m) {
    contentMap = m;
    explanation = e;
    name = n;
  }

  /**
   *  Construct this UniversalDetailItem with given map for the HTML name of
   *  this row and the name of the field providing its content
   *  @param n the {@link StringMap} that calculates the HTML descriptor for this row
   *  @param f the field name for this row's content
   */
  public UniversalDetailItem (StringMap n, String f) {
    nameMap = n;
    field = f;
  }

  /**
   *  Construct a new UniversalDetailItem with given maps for the HTML name
   *  and content of this row
   *  @param n a {@link StringMap} to calculate the HTML descriptor for this row
   *  @param m the {@link StringMap} that calculates this row's content
   */
  public UniversalDetailItem (StringMap n, StringMap c) {
    nameMap = n;
    contentMap = c;
  }

  public UniversalDetailItem (String n, VariantMap f) {
    name = n;
    contentMap = new VariantMapToString(f);
  }

  public UniversalDetailItem (String n, String e, VariantMap f) {
    name = n;
    explanation = e;
    contentMap = new VariantMapToString(f);
  }

  public UniversalDetailItem (StringMap n, VariantMap f) {
    nameMap = n;
    contentMap = new VariantMapToString(f);
  }

  /**
   *  Specify a value for the usesHtml flag.  By default, the flag is false,
   *  indicating that the content is not subjected to HTML interpretation.
   *  Setting the flag to true overrides the default behavior.
   *  @param b the new value of usesHtml
   */
  public void setUsesHtml (boolean b) {
    usesHtml = b;
  }

  /**
   *  Retrieve the value of the usesHtml flag.  The author knows of no reason
   *  why this would be of use to anyone, but it's here for completeness.
   *  @return the value of usesHtml
   */
  public boolean getUsesHtml () {
    return usesHtml;
  }

  /**
   *  Generate the HTML for a single row in a detail table.  The output
   *  generated by this method is given a consistent, standardized format, and,
   *  for this reason, it is recommended that all subclasses use this method
   *  to format their output.
   *  @param o a PrintWriter to take the output
   *  @param showName the name of the field to be shown in the table (left side)
   *  @param content the content of the field (right side)
   */
  protected void generateRow (PrintWriter o, String showName, String content) {
    o.println("  <tr>");
    o.println("    <td></td>");
    o.println("    <td></td>");
    //adds the explanation help message as a popup box upon the mouse passing over
    //the name text
    o.println("    <td align=left valign=top><a title=\"" + explanation + "\">" +
    "<p class=mo3>" + showName + "</p></a></td>");
    o.println("    <td></td>");
    o.println("    <td bgcolor=\"#cbd9dd\"></td>");
    o.println("    <td align=left valign=top bgcolor=\"#cbd9dd\">");
    o.println("      <p class=mo3>" + content + "</p></td>");
    o.println("    <td bgcolor=\"#cbd9dd\"></td>");
    o.println("    <td></td>");
    o.println("    <td></td>");
    o.println("  </tr>");
    o.println("  <tr>");
    o.println("    <td></td>");
    o.println("    <td></td>");
    o.println("    <td align=left valign=top colspan=6 bgcolor=\"#9ab9c6\">" +
      "<img src=\"/art/spacer.gif\" height=1 width=1></td>");
    o.println("  </tr>");
    o.flush();
  }

  protected void generateRow (PrintWriter o, String showName, String showExplanation, String content) {
    String oldExplanation = explanation;
    explanation = showExplanation;
    generateRow(o, showName, content);
    explanation = oldExplanation;
  }

  /**
   *  Generate the HTML representation for this line in the table.  This
   *  method should be called from the containing {@link UniversalDetailSection}
   *  @param o A PrintWriter to take the HTML output
   *  @param data the source for the content info
   */
  public void generateHtml (PrintWriter o, DataWrapper data) {
    String content = null;
    String showName = null;
    try {
      if (nameMap != null) {
        showName = nameMap.map(data);
      }
      else {
        showName = name;
      }
      if (contentMap != null) {
        content = contentMap.map(data);
      }
      else {
        Variant v = data.getProperty(field);
        if (v != null)
          content = v.toString();
        else
          content = "";
      }
      generateRow(o, showName, (usesHtml ? content : HTML.encode(content, true)));
    }
    catch (Exception oh_no) {
      System.out.println("UniversalDetailItem::generateHtml:  ERROR in " +
      (showName != null ? showName : (name != null ? name : "Unknown field")) +
      "--" + oh_no);
      oh_no.printStackTrace();
    }
  }

  /**
   *  Save data from HTML form elements into a data bean.  For instances
   *  properly belonging to this class, no form data is to be expected.
   *  However, subclasses, such as may be included in a {@link UniversalEditor}
   *  construct, will override this method to provide the desired functionality.
   *  @param r the HttpServletRequest containing the form data
   *  @param data the bean in which to store the submitted form
   */
  public void saveFormData (HttpServletRequest r, DataWrapper data) {
    // this element doesn't contain any editable data; do nothing
  }

  /**
   *  Validate data from the HTML form elements.  Instances that are proper
   *  members of this class will not have data to validate, but editable
   *  subclasses will override this method to exercise control over form
   *  submissions.
   *  @param r the HttpServletRequest containing the HTML form submission
   *  @return a message indicating why the validation failed, or null
   */
  public String validateFormData (HttpServletRequest r) {
    // no editable data is present--just return null
    return null;
  }
}
