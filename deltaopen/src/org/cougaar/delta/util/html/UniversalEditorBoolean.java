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
 *  UniversalEditorBoolean is an extension of {@link UniversalEditorItem} configured
 *  to handle boolean values.  It displays as a pair of radio buttons labeled,
 *  by default, "Yes" (for true) and "No" (for false).
 */
public class UniversalEditorBoolean extends UniversalEditorItem {
  private String trueLabel = "Yes";
  private String falseLabel = "No";

  /**
   *  Construct this UniversalEditorBoolean on top of the appropriate
   *  {@link UniversalEditorItem}.
   *  @param n the name of this row as displayed in the table
   *  @param f the field in the JavaBean corresponding to this row
   *  @param p the name of the HTML form element
   */
  public UniversalEditorBoolean (String n, String f, String p) {
    super(n, f, p);
  }

  /**
   *  Specify the label printed next to the radio button corresponding to
   *  the "true" state of this element.
   *  @param t the new true label
   */
  public void setTrueLabel (String t) {
    trueLabel = t;
  }

  /**
   *  Retrieve the label associated with the "true" state of this element
   *  @return "true" label
   */
  public String getTrueLabel () {
    return trueLabel;
  }

  /**
   *  Specify the label printed next to the radio button corresponding to
   *  the "false" state of this element.
   *  @param f the new false label
   */
  public void setFalseLabel (String f) {
    falseLabel = f;
  }

  /**
   *  Retrieve the label associated with the "false" state of this element
   *  @return "false" label
   */
  public String getFalseLabel () {
    return falseLabel;
  }

  /**
   *  print out an HTML representation of this element.
   *  @param o a PrintWriter to take the output
   *  @param data the bean being edited
   */
  public void generateHtml (PrintWriter o, DataWrapper data) {
    try {
      boolean val = ((VariantBoolean) data.getProperty(field)).getBoolean();
      if (editable) {
        StringBuffer contentBuf = new StringBuffer();
        contentBuf.append(
          "<input name=\"" + paramName + "\" type=radio value=true");
        if (val) {
          contentBuf.append(" checked");
        }
        contentBuf.append(
          ">" + trueLabel + "&nbsp;&nbsp;&nbsp;<input name=\"" + paramName +
          "\" type=radio value=false");
        if (!val) {
          contentBuf.append(" checked");
        }
        contentBuf.append(">" + falseLabel);
        generateRow(o, name, contentBuf.toString());
      }
      else {
        generateRow(o, name, (val ? trueLabel : falseLabel));
      }
    }
    catch (Exception oh_no) {
      System.out.println("UniversalEditorBoolean::generateHtml:  Error--" + oh_no);
    }
  }

  /**
   *  save the edited form element in the JavaBean
   *  @param r a HttpServletRequest containing the edited form
   *  @param data the JavaBean in which to store the data
   */
  public void saveFormData (HttpServletRequest r, DataWrapper data) {
    // save as boolean
    data.setProperty(field, new VariantBoolean(
      r.getParameter(paramName).equals("true")));
  }

  /**
   *  Validate data from the HTML form elements.  For instances of this class,
   *  it should not be possible for the user to submit an invalid entry.
   *  @param r the HttpServletRequest containing the HTML form submission
   *  @return null
   */
  public String validateFormData (HttpServletRequest r) {
    return null;
  }
}
