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

package org.cougaar.delta.util.qrule.logic;

import java.util.*;

public abstract class NumericalRange extends AttributeHolder {
  // operators recognized by this AttributeRange
  protected String EQUAL = null;
  protected String NOT_EQUAL = null;
  protected String GREATER_THAN = null;
  protected String LESS_THAN = null;
  protected String GREATER_OR_EQUAL = null;
  protected String LESS_OR_EQUAL = null;

  /**
   *  Specify the names of the operators used by the system to represent the
   *  relations of (in)equality, greater than (or equal to), and less than (or
   *  equal to).
   *  @param eq the equality operator
   *  @param neq the inequality operator
   *  @param gt the greater than operator
   *  @param lt the less than operator
   *  @param ge the greater than or equal to operator
   *  @param le the less than or equal to operator
   */
  public void setOperatorNames (
      String eq, String neq, String gt, String lt, String ge, String le)
  {
    EQUAL = eq;
    NOT_EQUAL = neq;
    GREATER_THAN = gt;
    LESS_THAN = lt;
    GREATER_OR_EQUAL = ge;
    LESS_OR_EQUAL = le;
  }
}
