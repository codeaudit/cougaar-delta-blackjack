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
 *  The UniversalSectionArray class provides the ability to create a listing
 *  of similar section blocks within a {@link UniversalDetail} display.  It can be
 *  inserted into the detail display as a {@link UniversalDetailSection}, but rather
 *  than one section, it produces zero or more depending on the length of the
 *  array, Vector, or Enumeration it is processing.  Naturally, the sections
 *  appear consecutively in the detail display.
 */
public class UniversalSectionArray extends UniversalDetailSection {
  // Instances of this class must have an associated field from which the
  // array, Vector, or Enumeration of data modules may be drawn.  Each such
  // module is represented as a section in the corresponding detail display
  private String field = null;

  // The "field" can also be a calculated value provided for each data bean by
  // the following VariantMap
  private VariantMap fieldMap = null;

  /**
   *  Build this UniversalSectionArray on top of the appropriate
   *  {@link UniversalDetailSection}, with the sections all having the given title.
   *  This element produces one section in the table for each element in the
   *  named field.
   *  @param t the String that the sections use as their titles.
   *  @param f the name of the field whose data elements are to be displayed
   */
  public UniversalSectionArray (String t, String f) {
    super(t);
    field = f;
  }

  /**
   *  Build this UniversalSectionArray on top of the appropriate
   *  {@link UniversalDetailSection}, with the sections all having the given title.
   *  This element produces one section in the table for each element returned
   *  by the provided mapping.
   *  @param t the String that the sections use as their titles.
   *  @param f the mapping that produces the displayed data elements
   */
  public UniversalSectionArray (String t, VariantMap f) {
    super(t);
    fieldMap = f;
  }

  /**
   *  Build this UniversalSectionArray on top of the appropriate
   *  {@link UniversalDetailSection}, with the sections deriving their titles from
   *  application of the given {@link StringMap}.  This element produces one section
   *  in the table for each element in the named field.
   *  @param m the {@link StringMap} applied to the sections for computing the titles.
   *  @param f the name of the field whose data elements are to be displayed
   */
  public UniversalSectionArray (StringMap m, String f) {
    super("", m);
    field = f;
  }

  /**
   *  Build this UniversalSectionArray on top of the appropriate
   *  {@link UniversalDetailSection}, with the sections deriving their titles from
   *  application of the given {@link StringMap}.  This element produces one section
   *  in the table for each element in the mapped "field".
   *  @param m the {@link StringMap} applied to the sections for computing the titles.
   *  @param f the mapping that produces the displayed data elements
   */
  public UniversalSectionArray (StringMap m, VariantMap f) {
    super("", m);
    fieldMap = f;
  }

  /**
   *  Give an HTML representation of this array of sections with content data
   *  drawn from the supplied bean.  Output is sent to a PrintWriter supplied
   *  by the caller.  This method will most likely be called from the like
   *  method of a {@link UniversalDetail}.
   *
   *  @param o PrintWriter to take HTML output
   *  @param data the object containing information to be displayed
   */
  protected void constructHtml (PrintWriter o, DataWrapper data) {
    Variant p = null;
    if (field != null)
      p = data.getProperty(field);
    else if (fieldMap != null)
      p = fieldMap.map(data);
    if (p != null) {
      Object a = p.getValue();
      Enumeration enu = null;
      if (a instanceof Enumeration)
        enu = (Enumeration) a;
      else if (a instanceof Vector)
        enu = ((Vector) a).elements();
      else if (a instanceof Object[])
        enu = new EnumeratedArray((Object[]) a);
      else
        System.out.println("\"" + field +
          "\" is not an array, a Vector, or an Enumeration");

      // Now poll the contents and generate a section for each element found
      while (enu.hasMoreElements()) {
        DataWrapper elt = new DataWrapper(enu.nextElement());
        super.constructHtml(o, elt);
      }
    }
  }

  /**
   *  Retrieve data from HTML form elements in these sections and save them in
   *  the given data bean.  This method would most likely be called from the
   *  saveFormData method of a containing {@link UniversalEditor} object.
   *  <br><br>
   *  Currently, this feature is not supported.
   *
   *  @param r the HttpServletRequest which contains the submitted form data
   *  @param data the bean which will store the submitted information
   */
  public void saveFormData (HttpServletRequest r, DataWrapper data) {
    // Not Supported -- do nothing
  }

  /**
   *  Not used.
   *  Validate data from the HTML form elements in this section.  This class
   *  does not support editable components, so <b><i>this function always just
   *  returns null</i></b>.
   *  @param r the HttpServletRequest containing the HTML form submission
   *  @return null
   */
  public String validateFormData (HttpServletRequest r) {
    return null;
  }
}
