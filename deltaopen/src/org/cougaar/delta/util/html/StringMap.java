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

/**
 *  StringMap is an abstract interface for classes that produce strings as
 *  output.  The sole method, map, performs this calculation.
 *  <br><br>
 *  A class implementing StringMap can be customized to a given purpose, such
 *  as deriving a title or hyperlink from the contents of a given type of data
 *  object, and passed under the auspices of StringMap to a generalized String
 *  formatter.
 *  <br><br>
 *  {@link UniversalTable} and {@link UniversalDetail} (and associated classes) are configured
 *  to use StringMap implementors for formatting links and titles.
 */
public interface StringMap {
  /**
   *  Produce string output from the given data object.  The output is not
   *  necessarily a String representation of the object in question, but one
   *  which is associated with it and can be computed from its properties
   *  @param obj data Object
   *  @return A String derived from the properties of obj
   */
  public String map (Object obj);
}
