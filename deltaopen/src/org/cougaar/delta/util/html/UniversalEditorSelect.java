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
 *  UniversalEditorSelect is an extension of {@link UniversalEditorItem} designed to
 *  present the user with a selection box.
 */
public class UniversalEditorSelect extends UniversalEditorItem {
  private String[] choices = null;
  private boolean multiple = false;

  /**
   *  Create this UniversalEditorSelect on top of an appropriate
   *  {@link UniversalEditorItem} with the specified array of choices for the
   *  selection box.  By default, multiple selections are not allowed.
   *  @param n the name of this field as displayed
   *  @param f the bean property corresponding to this element
   *  @param p the name of the HTML form element
   *  @param vals the choices from which the user may select
   */
  public UniversalEditorSelect (String n, String f, String p, String[] vals) {
    super(n, f, p);
    choices = vals;
  }

  /**
   *  Create this UniversalEditorSelect on top of an appropriate
   *  {@link UniversalEditorItem} with the specified array of choices for the selection
   *  box.  The caller supplies a flag which determines whether multiple
   *  selections will be allowed for this element.
   *
   *  @param n the name of this field as displayed
   *  @param f the bean property corresponding to this element
   *  @param p the name of the HTML form element
   *  @param vals the choices from which the user may select
   *  @param multi true if multiple selections are to be allowed; false otherwise
   */
  public UniversalEditorSelect (
      String n, String f, String p, String[] vals, boolean multi)
  {
    this(n, f, p, vals);
    multiple = multi;
  }

  /**
   *  Generate the HTML printout for this element.  If editing is enabled for
   *  this element, then a form element is generated.  If not, then the
   *  superclass is called upon for assistance.
   *  @param o the PrintWriter to which output is sent
   *  @param data the JavaBean being edited
   */
  public void generateHtml (PrintWriter o, DataWrapper data) {
    if (editable) {
      try {
        StringBuffer contentBuf = new StringBuffer();
        contentBuf.append("<select " + (multiple ? "multiple " : "") +
          "name=\"" + paramName + "\">");
        // determine which, if any, of the options are selected
        // the results are stored in a hashtable for easy retrieval
        Hashtable t = new Hashtable();
        String sel = " SELECTED";
        String blank = "";
        String[] selections = null;
        for (int i = 0; i < choices.length; i++)
          t.put(choices[i], blank);
        Variant fieldValue = data.getProperty(field);
        if (fieldValue != null) {
          if (multiple)
            selections = (String[]) fieldValue.getValue();
          else
            selections = new String[] {fieldValue.toString()};
        }

        if (selections != null)
          for (int i = 0; i < selections.length; i++)
            t.put(selections[i], sel);

        // include the options in the HTML
        for (int i = 0; i < choices.length; i++) {
          contentBuf.append("<option value=\"" + choices[i] + "\"" +
            t.get(choices[i]) + ">" + choices[i]);
        }
        contentBuf.append("</select>");
        generateRow(o, name, contentBuf.toString());
      }
      catch (Exception oh_no) {
        System.out.println("UniversalEditorSelect::generateHtml:  Error--" + oh_no);
      }
    }
    else {
      super.generateHtml(o, data);
    }
  }

  /**
   *  Save edited form data in a JavaBean.
   *  @param r the HttpServletRequest containing the HTML form
   *  @param data the JavaBean in which to store the data
   */
  public void saveFormData (HttpServletRequest r, DataWrapper data) {
    // save as text
    if (multiple) {
      data.setProperty(field, new VariantObject(r.getParameterValues(paramName)));
    }
    else {
      data.setProperty(field, new VariantText(r.getParameter(paramName)));
    }
  }

  /**
   *  Validate data from the HTML form elements.  For instances of this class,
   *  it should not be possible for the user to submit an invalid entry.
   *  @param r the HttpServletRequest containing the HTML form submission
   *  @return a message indicating why the validation failed, or null
   */
  public String validateFormData (HttpServletRequest r) {
    return null;
  }
}
