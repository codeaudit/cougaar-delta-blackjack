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
package org.cougaar.delta.util.html;

import java.util.*;

/**
 *  This class provides an Enumeration interface to an array.  Basically, it
 *  does what you expect, namely starting at element zero and iterating through
 *  the elements sequentially up to the end of the array.
 *  <br><br>
 *  Note:  changes in the array object will be reflected in the unreached
 *  elements in this enumeration.
 */
public class EnumeratedArray implements Enumeration {
  // the array being iterated
  private Object[] array = null;

  // the index of the next element to be returned
  private int n = 0;

  /**
   *  Construct an iterator for the given array of Objects
   *  @param a the Object array
   *  @throws NullPointerException if the argument is null
   */
  public EnumeratedArray (Object[] a) {
    if (a == null)
      throw new NullPointerException(
        "No array, no iterator.  That's just the way it is, see?");
    array = a;
  }

  /**
   *  Discover whether or not this iterator has reached the end of the array
   *  through which it is iterating.
   *  @return true if there are more array positions to explore, false otherwise
   */
  public boolean hasMoreElements () {
    return (n < array.length);
  }

  /**
   *  Get the next element from the array, and increment the counter in case
   *  there are other elements following.
   *  @return the next array element
   */
  public Object nextElement () {
    if (hasMoreElements())
      return array[n++];
    return null;
  }
}
