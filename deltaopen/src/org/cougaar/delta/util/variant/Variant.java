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
 * Objects are wrapped with a Variant implementor for purposes
 * of general comparison algorithms and string conversions.
 * The Variant Interface will contain any one of the
 * possible datatypes (w/ formatting info).
 */
public interface Variant {

  /**
   * Returns an int equal to 0
   * if the Variant has the same "value", <0 if its "value"
   * is less than the Variant argument, and >0 otherwise.
   */
  int compareTo(Variant v);

  /**
   * Returns the value of the variant as
   * an Object.  If the value is an Object, then it is returned;
   * if it is a primitive type, such as int or float, then an
   * Object wrapper (Integer or Float) is supplied.
   */
  Object getValue ();

  /**
   * Returns the String representation of the Variant
   */
  String toString();

  /**
   * Assigns a format to the Variant to be used by the toString() method.
   */
  void setFormat(Format df);
}
