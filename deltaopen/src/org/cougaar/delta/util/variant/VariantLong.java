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
 *  A concrete class combining the long data type with the {@link Variant} interface.
 *  Provides a {@link Variant} wrapper around a Long object.
 */
public class VariantLong implements Variant {
  Long value;

  public VariantLong(long i) {
    value = new Long(i);
  }

  public Object getValue () {
    return value;
  }

  /**
   *  Formatting is not implemented for VariantLong.
   */
  public void setFormat(Format df) {
    return;
  }

  public int compareTo(Variant v) {
    long self, vlong;

    self = value.longValue();
    vlong = ((VariantLong) v).longValue();

    if (self < vlong)
      return -1;
    else if (self > vlong)
      return 1;
    else
      return 0;
  }

  public long longValue() {
    return value.longValue();
  }

  public String toString() {
    return String.valueOf(value.longValue());
  }
}
