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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *  A concrete class combining Date data with the {@link Variant} interface.  This
 *  class is here to accomodate the Date fields found in LTA objects (q.v.).
 */
public class VariantDate implements Variant {
  private java.util.Date value;
  // For now, all dates are formatted as follows:
  private DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
  private boolean isnull=false;

  /**
   *  Create this VariantDate with Date value as provided
   *  @param d the Date
   */
  public VariantDate (java.util.Date d) {
    value = d;
  }

  /**
   *  Create this VariantDate with no value
   */
  public VariantDate () {
    value = new java.util.Date(0);
    isnull = true;
  }

  /**
   *  Retrieve the underlying Date value of this VariantDate
   *  @return the Date
   */
  public Object getValue () {
    return value;
  }

  /**
   *  Retrieve the underlying Date value as a Date type
   *  @return the Date
   */
  public java.util.Date dateValue () {
    return value;
  }

  /**
   * Sets the format for the Date.
   * @param df must be an instance of class DateFormat
   * @throw IllegalArgumentException if df is not an instance of DateFormat
   */
  public void setFormat (Format df) {
    if (df instanceof DateFormat)
    {
        sdf = (DateFormat) df;
    }
    else
    {
        throw new IllegalArgumentException("Invalid format");
    }
  }

  /**
   *  Compare this VariantDate to another
   *  @param v another VariantDate to compare to this one
   *  @return 1 if this is greater than v, -1 if this is less, and 0 otherwise
   */
  public int compareTo (Variant v) {
    long diff = value.getTime() - ((VariantDate) v).dateValue().getTime();
    if (diff > 0) return 1;
    if (diff < 0) return -1;
    return 0;
  }

  /**
   *  Generate a string representation of this VariantDate.  Currently, the
   *  format for the date is provided by the fixed format string sdf
   *  @return String representation
   */
  public String toString () {
    if (isnull)
      return "";
    else
      return sdf.format(value).toString();
  }
}
