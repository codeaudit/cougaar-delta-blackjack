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

/**
 * A String version of a {@link Variant}.  A VariantText object holds a value that is
 * a String.
 */
public class VariantText implements Variant {
  String value;

  /**
   * Construct a VariantText object with the specified String.  The object's value
   * is set to an empty String if the argument is null, otherwise to the value of
   * the argument.
   */
  public VariantText(String s) {
    if (s == null)
      value = "";
    else
      value = s;
  }

  /**
   * Gets the VariantText object's value.
   * @return the String value as an Object
   */
  public Object getValue () {
    return value;
  }

  /**
   * The VariantText does not currently implement formatting.  Does nothing.
   */
  public void setFormat(Format df) {
    return;
  }

  /**
   * A case-insensitive compare.  The {@link Variant} argument must be of type VariantText.
   * @return a negative integer, zero, or a positive integer when the String value
   * of the specified Variant is greater than, equal to, or less than the String
   * value of this VariantText object, ignoring case.
   */
  public int compareTo(Variant v) {
    return value.compareToIgnoreCase(((VariantText)v).toString());
  }

  /**
   * Returns the VariantText object's value as a String.
   * @return the String value
   */
  public String toString() {
    return value;
  }
}
