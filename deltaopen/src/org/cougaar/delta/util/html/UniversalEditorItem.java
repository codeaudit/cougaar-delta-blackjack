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
 *  An instance of UniversalEditorItem represents an editable field in
 *  a {@link UniversalEditor} display.  Other classes of editable elements should
 *  subclass this one.
 */
public class UniversalEditorItem extends UniversalDetailItem {

  /**
   *  A flag to determine whether the user is allowed to edit this field.  By
   *  default, it is set to true.
   */
  protected boolean editable = true;

  /**
   *  The maximum number of characters allowed in this item's text field (if
   *  applicable).  A negative value indicates no length restriction.
   */
  protected int maxLength = -1;

  /**
   *  The name of this field in the HTML form.
   */
  protected String paramName = "";

  /**
   *  The type of this field in the HTML form.  Default value is "text".
   */
  protected String paramType = "text";

  /**
   *  The event handler associated with this field in the HTML form.
   */
  protected String eventHandler = "";

  /**
   *  A map for performing validation checking
   */
  protected StringMap validationMap = null;

  /**
   *  Create a new UniversalEditorItem on top of the appropriate
   *  {@link UniversalDetailItem}, with the additional specification of the
   *  HTML form element name.
   *  @param n the name of the field as displayed in the table
   *  @param f the name of the bean property that supplies this row with data
   *  @param p the name of the form element displayed in this row
   */
  public UniversalEditorItem (String n, String f, String p) {
    super(n, f);
    paramName = p;
  }

  /**
   *  Create a new UniversalEditorItem on top of the appropriate
   *  {@link UniversalDetailItem}, with the additional specification of the
   *  HTML form element name.
   *  @param n the name of the field as displayed in the table
   *  @param f the name of the bean property that supplies this row with data
   *  @param p the name of the form element displayed in this row
   *  @param type the type of the form element displayed in this row
   *  @param anEventHandler an event handler associated with the form element in this row
   */
  public UniversalEditorItem (String n, String f, String p, String type, String anEventHandler) {
    super(n, f);
    paramName = p;
    paramType = type;
    eventHandler = anEventHandler;
  }

  /**
   *  Create a new UniversalEditorItem on top of the appropriate
   *  {@link UniversalDetailItem}, with the additional specification of the
   *  HTML form element name.
   *  @param n the name of the field as displayed in the table
   *  @param f StringMap to calculate the content for this row
   *  @param p the name of the form element displayed in this row
   */
  public UniversalEditorItem (String n, StringMap m, String p) {
    super(n, m);
    paramName = p;
  }

  /**
   *  Create a new UniversalEditorItem on top of the appropriate
   *  {@link UniversalDetailItem}, with the additional specification of the
   *  HTML form element name and the length limitation.
   *  @param n the name of the field as displayed in the table
   *  @param f the name of the bean property that supplies this row with data
   *  @param p the name of the form element displayed in this row
   *  @param len the maximum length of the input.
   */
  public UniversalEditorItem (String n, String f, String p, int len) {
    super(n, f);
    paramName = p;
    maxLength = len;
  }

  /**
   *  Create a new UniversalEditorItem on top of the appropriate
   *  {@link UniversalDetailItem}, with the additional specification of the
   *  HTML form element name and the length limitation.
   *  @param n the name of the field as displayed in the table
   *  @param f {@link StringMap} to calculate the content for this row
   *  @param p the name of the form element displayed in this row
   *  @param len the maximum length of the input.
   */
  public UniversalEditorItem (String n, StringMap m, String p, int len) {
    super(n, m);
    paramName = p;
    maxLength = len;
  }

  /**
   *  Retrieve the value of the editable flag.
   *  @return true if this element is editable, false otherwise.
   */
  public boolean isEditable () {
    return editable;
  }

  /**
   *  Specify whether or not this element is editable
   *  @param e the new value for the editable flag
   */
  public void setEditable (boolean e) {
    editable = e;
  }

  /**
   *  Retrive the maximum length of inputs for this element (in case of text
   *  fields).
   *  @return the maximum number of characters.
   */
  public int getMaxLength () {
    return maxLength;
  }

  /**
   *  Specify the maximum length for input into this element (in case of text
   *  fields).
   *  @param n the number of characters allowed.
   */
  public void setMaxLength (int n) {
    maxLength = n;
  }

  /**
   *  Specify the validation criterion.
   *  @param m the {@link StringMap} instance that determines whether values are valid
   */
  public void setValidationMap (StringMap m) {
    validationMap = m;
  }

  /**
   *  Get a reference to the {@link StringMap} this editable element is using to
   *  validate its data.
   */
  public StringMap getValidationMap () {
    return validationMap;
  }

  /**
   *  Generate the HTML representation for this element.  If editing is
   *  enabled for this element, a form element (TextField) is produced.  If
   *  not, then the generateHtml method of the superclass is invoked.  In other
   *  words, the field is printed as a {@link UniversalDetailItem}.
   *  <br><br>
   *  Subclasses will more than likely override this method to provide
   *  specialized functionality.
   *  @param o a PrintWriter to take the output
   *  @param data the JavaBean supplying the initial data (or being edited)
   */
  public void generateHtml (PrintWriter o, DataWrapper data) {
    if (editable) {
      try {
        String content;
        String valueIndicator = "";
        Variant p = data.getProperty(field);
        if (p == null)
          p = new VariantText("");
        String value = p.toString();
        if (value.length() > 0)
          valueIndicator = " value=" + HTML.encode(value);

        String lengthLimit = "";
        if (maxLength > -1)
          lengthLimit = " maxlength=\"" + maxLength + "\"";

        content = "<input name=\"" + paramName + "\" type=" + paramType + " size=40" +
          valueIndicator + lengthLimit + " " + eventHandler + "></input>";
        generateRow(o, name, content);
      }
      catch (Exception oh_no) {
        System.out.println("UniversalEditorItem::generateHtml:  Error--" + oh_no);
      }
    }
    else {
      super.generateHtml(o, data);
    }
  }

  /**
   *  Retrieve submitted data from this element and store it in the appropriate
   *  place within the given JavaBean.
   *  @param r the HttpServletRequest with the form data to be saved.
   *  @param data the bean being edited.
   */
  public void saveFormData (HttpServletRequest r, DataWrapper data) {
    // save as text
    data.setProperty(field, new VariantText(r.getParameter(paramName)));
  }

  /**
   *  Validate data from the HTML form elements.  If a validation map has been
   *  assigned to this element, it is used to determine the validity of the
   *  submitted value or a message indicating why it is invalid.  If no map has
   *  been assigned, then presumably, no validation is required.
   *  @param r the HttpServletRequest containing the HTML form submission
   *  @return a message indicating why the validation failed, or null
   */
  public String validateFormData (HttpServletRequest r) {
    if (validationMap != null)
      return validationMap.map(r.getParameter(paramName));
    return null;
  }
}
