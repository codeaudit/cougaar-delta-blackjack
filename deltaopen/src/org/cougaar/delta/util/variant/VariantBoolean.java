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
 *  A concrete class combining boolean data with the {@link Variant} interface.
 */
public class VariantBoolean implements Variant {
  Boolean value;

  /**
   *  Create this VariantBoolean with the given boolean value.
   *  @param b the boolean value of this {@link Variant}
   */
  public VariantBoolean (boolean b) {
    value = new Boolean(b);
  }

  /**
   *  Retrieve the Boolean object containing the boolean value.
   *  @return the Boolean value
   */
  public Object getValue () {
    return value;
  }

  /**
   *  Formatting is not implemented for VariantBoolean.
   */
  public void setFormat (Format df) {
    return;
  }

  /**
   *  Provides a String representation of this VariantBoolean.
   *  @return "Yes" if the value is true, "No" if it is false
   */
  public String toString () {
    return (getBoolean() ? "Yes" : "No");
  }

  /**
   *  Retrieve the underlying boolean value of this VariantBoolean.
   *  @return the boolean value
   */
  public boolean getBoolean () {
    return value.booleanValue();
  }

  /**
   *  Compare this VariantBoolean to another based on their underlying boolean
   *  values.  For purposes of comparison, "true" is considered to be greater
   *  than "false".
   *  @param v a VariantBoolean to be compared with this one
   *  @return 1 if this is greater than v, -1 if this is less than v, or 0 otherwise
   */
  public int compareTo (Variant v) {
    if (value == null)
      return 0;

    boolean thisBoolean = getBoolean();
    boolean thatBoolean = ((VariantBoolean) v).getBoolean();
    if (thisBoolean && !thatBoolean)
      return 1;
    if (thatBoolean && !thisBoolean)
      return -1;
    return 0;
  }
}
