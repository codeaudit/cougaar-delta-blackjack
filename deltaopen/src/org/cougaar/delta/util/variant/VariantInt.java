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
 *  A concrete class combining the int data type with the {@link Variant} interface.
 *  Provides a {@link Variant} wrapper around an Integer object.
 */
public class VariantInt implements Variant {
  Integer value;
  boolean isnull = false;

  public VariantInt(int i) {
    value = new Integer(i);
  }

  /**
   * This int has a value of zero but displays as ""
   */
  public VariantInt() {
    value = new Integer(0);
    isnull = true;
  }

  public Object getValue () {
    return value;
  }

  /**
   *  Formatting is not implemented for VariantInt.
   */
  public void setFormat(Format df) {
    return;
  }

  public int compareTo(Variant v) {
    int self, vint;

    self = value.intValue();
    vint = ((VariantInt)v).intValue();

    if (self < vint)
      return -1;
    else if (self > vint)
      return 1;
    else
      return 0;
  }

  public int intValue() {
    return value.intValue();
  }

  public String toString() {
    if (isnull)
      return "";
    else
      return String.valueOf(value.intValue());
  }
}
