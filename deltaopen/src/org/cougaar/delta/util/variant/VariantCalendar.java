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

package org.cougaar.delta.util.variant;

import java.text.Format;
import java.util.*;

/**
 * Provides a {@link Variant} wrapper around a java.util.GregorianCalendar object.
 */
public class VariantCalendar implements Variant {
  java.util.GregorianCalendar value;

  public VariantCalendar(int year, int month, int day) {
    value = new java.util.GregorianCalendar(year, month-1, day);
  }

  /**
   * Returns the GregorianCalendar held by this VariantCalendar as an Object.
   */
  public Object getValue () {
    return value;
  }

  /**
   * Not implemented.
   */
  public void setFormat(Format df) {
    // The Variant Calendar does not currently implement formatting
    return;
  }

  /**
   * Compares two java.util.GregorianCalendars.
   * @param v a {@link Variant} that is implemented as a VariantCalendar
   * @return 0 if the value of this VariantCalendar is null or equal to the value
   * of v, -1 if the value of this VariantCalendar is before v, or 1 if the value
   * of this VariantCalendar is after v.
   */
  public int compareTo(Variant v) {
    if (value == null)
      return 0;

    java.util.GregorianCalendar vdate = ((VariantCalendar)v).getCalendar();
    if (value.before(vdate))
      return -1;
    else if (value.after(vdate))
      return 1;
    else
      return 0;
  }

  /**
   * Returns the GregorianCalendar held by this VariantCalendar.
   */
  public java.util.GregorianCalendar getCalendar() {
    return value;
  }

  public String toString() {
    String dval;

    if (value == null)
      dval = "";
    else
      dval = (value.get(Calendar.MONTH)+1) + "/" + value.get(Calendar.DAY_OF_MONTH) + "/" + (value.get(Calendar.YEAR));

    return dval;
  }
}
