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
import java.text.NumberFormat;

/**
 *  A concrete class combining double data with the {@link Variant} interface.
 *  Provides a {@link Variant} wrapper around a Double object.
 */
public class VariantDouble implements Variant {
  java.lang.Double value;
  NumberFormat form = null;

  public VariantDouble (double f) {
    value = new java.lang.Double(f);
  }

  public Object getValue () {
    return value;
  }

  /**
   * Sets the format for the Double.
   * @param df must be an instance of class NumberFormat
   * @throw IllegalArgumentException if df is not an instance of NumberFormat
   */
  public void setFormat(Format df) {
    if (df instanceof NumberFormat)
      form = (NumberFormat) df;
    else
      throw new IllegalArgumentException("Invalid format");
  }

  public int compareTo(Variant v) {
    double self, vdouble;

    self = value.doubleValue();
    vdouble = ((VariantDouble)v).doubleValue();

    if (self < vdouble)
      return -1;
    else if (self > vdouble)
      return 1;
    else
      return 0;
  }

  public double doubleValue() {
    return value.doubleValue();
  }

  public String toString() {
    if (form == null)
      return value.toString();
    else
      return form.format(value);
  }
}
