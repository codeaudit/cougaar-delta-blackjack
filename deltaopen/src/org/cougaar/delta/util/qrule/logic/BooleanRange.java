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

import org.cougaar.delta.util.qrule.*;
import java.util.*;

/**
 *  A BooleanRange is an AttributeRange specialized for boolean-valued
 *  attributes.  Needless to say, keeping track of boolean values is not a hard
 *  thing to accomplish.
 */
public class BooleanRange extends AttributeHolder {
  private Boolean requiredValue = null;

  // operators recognized by this AttributeRange
  private String IS = null;
  private String IS_NOT = null;

  /**
   *  Specify the operators used by the system to mean equality and inequality
   *  of boolean values.
   *  @param is the equality operator
   *  @param is_not the inequality operator
   */
  public void setOperatorNames (String is, String is_not) {
    IS = is;
    IS_NOT = is_not;
  }

  /**
   *  Create a mew AttributeRange responsible for the trivial task of managing
   *  a boolean-valued attribute.
   */
  public BooleanRange () {
  }

  protected void init () {
    // construct the range and set flags
    for (Enumeration enu = constraints.elements(); enu.hasMoreElements(); ) {
      QRuleComparison q = (QRuleComparison) enu.nextElement();
      String op = q.getOperator().getJessName();
      Object val = ((QRuleLiteralOperand) q.getOperand2()).getValue();
      if (val != null && val instanceof Boolean) {
        if (IS != null && op.equals(IS))
          noteValue(((Boolean) val).booleanValue());
        else if (IS_NOT != null && op.equals(IS_NOT))
          noteValue(!((Boolean) val).booleanValue());
      }
      else {
        violateDomain = true;
      }
    }
  }

  private void noteValue (boolean val) {
    if (requiredValue != null) {
      if (requiredValue.booleanValue() == val)
        redundant = true;
      else
        contradict = true;
    }

    if (val)
      requiredValue = Boolean.TRUE;
    else
      requiredValue = Boolean.FALSE;
  }

  public boolean contains (AttributeRange range) {
    if (range.isContradictory())
      return true;
    if (contradict)
      return false;
    if (!(range instanceof BooleanRange))
      return false;

    BooleanRange r = (BooleanRange) range;

    if (tautology)
      return true;
    if (r.tautology)
      return false;

    return requiredValue.equals(r.requiredValue);
  }

  public boolean excludes (AttributeRange range) {
    if (contradict || range.isContradictory())
      return true;
    if (!(range instanceof BooleanRange))
      return true;

    BooleanRange r = (BooleanRange) range;

    if (tautology || r.tautology)
      return false;

    return !requiredValue.equals(r.requiredValue);
  }
}
