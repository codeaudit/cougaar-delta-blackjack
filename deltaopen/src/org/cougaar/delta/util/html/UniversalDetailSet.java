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
 *  UniversalDetailSet is an extension of {@link UniversalDetailItem} designed to
 *  provide support for beans with Object members.  As long as the members
 *  in question adhere to the JavaBeans conventions, their data can be extruded
 *  and displayed in a {@link UniversalDetail} table.
 *  <br><br>
 *  The UniversalDetailSet supports an arbitrary level of nesting, providing
 *  access to finer structures within a bean.
 */
public class UniversalDetailSet extends UniversalDetailItem {
  // A Vector containing the child elements
  private Vector items = new Vector();

  // A VariantMap useful for finding aggregate properties that must be computed
  private VariantMap fieldMap = null;

  // a message to show when the requested aggregate object is null
  private String emptyMessage = null;

  /**
   *  Set the message to display in case of a null object.  If the message is
   *  set to null, no rows are generated.
   *  @param s the message
   */
  public void setEmptyMessage (String s) {
    emptyMessage = s;
  }

  /**
   *  Discover the message displayed by this detail set when no data is
   *  available.
   *  @return the default message
   */
  public String getEmptyMessage () {
    return emptyMessage;
  }

  /**
   *  Construct a new UniversalDetailSet which reads data from the named field
   *  @param field name of the field containing the data object
   */
  public UniversalDetailSet (String f) {
    field = f;
  }

  /**
   *  Construct a new UniversalDetailSet which reads data from an aggregate
   *  Object that is the result of a computation by the given mapping.
   *  @param v the {@link VariantMap} that calculates the aggregate property of the data
   */
  public UniversalDetailSet (VariantMap v) {
    fieldMap = v;
  }

  /**
   *  Add an externally constructed {@link UniversalDetailItem} to this UniversalDetailSet.
   *  The given {@link UniversalDetailItem} should be configured to act on the above-named
   *  field of the bean, rather than the bean itself.
   *  @param item a {@link UniversalDetailItem} to be displayed.
   */
  public void addItem (UniversalDetailItem item) {
    items.addElement(item);
  }

  /**
   *  Generate the HTML representation of the selected members of the named
   *  Object member.
   *  @param o a PrintWriter to take the output
   *  @param data the JavaBean from which content data is taken
   */
  public void generateHtml (PrintWriter o, DataWrapper data) {
    Variant v = null;
    if (field != null)
      v = data.getProperty(field);
    else if (fieldMap != null)
      v = fieldMap.map(data);
    if (v == null) {
      if (emptyMessage != null)
        generateRow(o, emptyMessage, "");
      return;
    }
    DataWrapper substructure = new DataWrapper(v.getValue());
    Enumeration enu = items.elements();
    while  (enu.hasMoreElements()) {
      ((UniversalDetailItem) enu.nextElement()).generateHtml(o, substructure);
    }
  }

  /**
   *  Save submitted form data in the given bean.  The {@link UniversalDetailSet}
   *  corresponds to a property of the bean, and it is in the named
   *  fields of this sub-object that the data is stored.
   *  @param r the HttpServletRequest containing the form data
   *  @param data the bean where the data will be stored
   */
  public void saveFormData (HttpServletRequest r, DataWrapper data) {
    DataWrapper substructure = null;
    try {
      // grab the substructure corresponding to this set
      substructure = new DataWrapper(
        ((VariantObject) data.getProperty(field)).getValue());
      // poll the components of this set
      Enumeration enu = items.elements();
      while (enu.hasMoreElements()) {
        ((UniversalDetailItem) enu.nextElement()).saveFormData(r, substructure);
      }
    }
    catch (Exception b_s) {
      System.out.println("Error in \"" + field + "\":  " +
        (r == null ? "HttpServletRequest is null; " : "") +
        (data == null ? "Data is null; " : "") +
        (substructure == null ? "substructure is null; " : ""));
    }
  }

  /**
   *  Validate data from the HTML form elements in this set.  Each element
   *  is polled for validity, and the set reports valid only if all of its
   *  elements report valid.
   *  @param r the HttpServletRequest containing the HTML form submission
   *  @return a message indicating why the validation failed, or null
   */
  public String validateFormData (HttpServletRequest r) {
    Enumeration enu = items.elements();
    while (enu.hasMoreElements()) {
      String msg =
        ((UniversalDetailItem) enu.nextElement()).validateFormData(r);
      if (msg != null)
        return msg;
    }
    return null;
  }
}
