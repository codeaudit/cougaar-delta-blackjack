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
 *  A concrete class combining char data with the {@link Variant} interface.
 */
public class VariantChar implements Variant {
  java.lang.Character value;

  /**
   *  Create this VariantChar with the given underlying char value.
   *  @param the char value of this VariantChar
   */
  public VariantChar (char c) {
    value = new java.lang.Character(c);
  }

  /**
   *  Retrieve the value of this VariantChar as an Object type (Character).
   *  @return the Character object containing the char value
   */
  public Object getValue () {
    return value;
  }

  /**
   *  Formatting is not implemented for VariantChar.
   */
  public void setFormat (Format df) {
  }

  /**
   *  Compare this VariantChar to another by their underlying char values.
   *  @param v another VariantChar to compare with this one
   *  @return the difference (be it positive, negative, or zero) of the
   *    character codes of the two underlying char values
   */
  public int compareTo (Variant v) {
    char c1 = this.charValue();
    char c2 = ((VariantChar)v).charValue();

    // No case-sensitive sorting
    return (int)Character.toUpperCase(c1) - (int)Character.toUpperCase(c2);
  }

  /**
   *  Produce a String representation of this VariantChar
   *  @return a singleton String containing the char value of this VariantChar
   */
  public String toString () {
    return String.valueOf(charValue());
  }

  /**
   *  Retrieve the underlying char value of this VariantChar
   *  @return the char value
   */
  public char charValue () {
    return value.charValue();
  }
}
