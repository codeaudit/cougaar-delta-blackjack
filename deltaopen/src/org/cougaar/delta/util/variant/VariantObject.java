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
 *  The VariantObject is a concrete class implementing the Variant interface
 *  and containing an arbitrary Object as its data.  Mainly, the purpose of
 *  this class is to allow any Object to be wrapped in a Variant and handled
 *  by methods that expect a Variant.
 */
public class VariantObject implements Variant {
  Object value;

  /**
   *  Retrieve the Object value of this VariantObject
   *  @return the underlying Object
   */
  public Object getValue () {
    return value;
  }

  /**
   *  Construct this VariantObject containint the Object provided
   *  @param o the Object
   */
  public VariantObject (Object o) {
    value = o;
  }

  /**
   *  Compare this VariantObject to another.  This method is here only to
   *  complete the interface implementation.  All comparisons return zero,
   *  indicating "equality"
   */
  public int compareTo (Variant v) {
    return 0;
  }

  /**
   *  Produce a String representation of this VariantObject.  In this case,
   *  simply delegate to the underlying Object itself
   *  @return String representation
   */
  public String toString () {
    return value.toString();
  }

  /**
   *  Formatting is not applicable to this class.
   */
  public void setFormat (Format df) {
  }
}
