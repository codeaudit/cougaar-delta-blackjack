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
 *  This class is an AttributeRange implementor for group membership
 *  comparisons.  The groups are referenced by String names, and no knowledge
 *  of actual group membership is applied in the calculations.  Redundancies
 *  and/or contradictions are noted only if the same group is mentioned twice,
 *  where, clearly, it is a contradiction to say that a member is both in and
 *  not in the same group, and it is redundant to say either statement twice.
 */
public class DiscreteGroupRange extends AttributeHolder {
  private Vector inclusions = new Vector();
  private Vector exclusions = new Vector();

  // operators recognized by this AttributeRange
  private String IN = null;
  private String NOT_IN = null;

  /**
   *  Specify the operators denoting membership or nonmembership in a group
   *  @param in the membership operator
   *  @param not_in the nonmembership operator
   */
  public void setOperatorNames (String in, String not_in) {
    IN = in;
    NOT_IN = not_in;
  }

  /**
   *  Create a new AttributeRange for handling an attribute being constrained
   *  by group membership
   */
  public DiscreteGroupRange () {
  }

  // called from the constructors--initialize this AttributeRange instance by
  // polling the provided constraints and incorporating them into this logical
  // model
  protected void init () {
    // construct the range and set flags
    for (Enumeration enu = constraints.elements(); enu.hasMoreElements(); ) {
      QRuleComparison q = (QRuleComparison) enu.nextElement();
      String op = q.getOperator().getJessName();
      Object val = ((QRuleLiteralOperand) q.getOperand2()).getValue();
      if (val != null && val instanceof String) {
        String v = (String) val;
        if (IN != null && op.equals(IN))
          noteInclusion(v);
        else if (NOT_IN != null && op.equals(NOT_IN))
          noteExclusion(v);
      }
      else {
        violateDomain = true;
      }
    }
  }

  private void noteInclusion (String group) {
    if (inclusions.contains(group))
      redundant = true;
    else
      inclusions.add(group);

    if (exclusions.contains(group))
      contradict = true;
  }

  private void noteExclusion (String group) {
    if (exclusions.contains(group))
      redundant = true;
    else
      exclusions.add(group);

    if (inclusions.contains(group))
      contradict = true;
  }

  /**
   *  Compare the range of values satisfying these conditions with another
   *  similar range, and return true if and only if the other range is a subset
   *  of this one.
   *  @param range the other AttributeRange being compared to this one
   *  @return true if and only if the other range is a subset of this one
   */
  public boolean contains (AttributeRange range) {
    if (range.isContradictory())
      return true;
    if (contradict)
      return false;
    if (!(range instanceof DiscreteGroupRange))
      return false;

    DiscreteGroupRange r = (DiscreteGroupRange) range;

    // since there is no a priori information about relationships among the
    // groups, the only way to tell that one contains another is to verify that
    // every constraint in effect on the one also constrains the other.
    return
      r.inclusions.containsAll(inclusions) &&
      r.exclusions.containsAll(exclusions);
  }

  /**
   *  Compare the range of values satisfying these conditions with another
   *  similar range, and return true if and only if the other range does not
   *  intersect this one.
   *  @param range the other AttributeRange being compared to this one
   *  @return true if and only if the other range does not intersect this one
   */
  public boolean excludes (AttributeRange range) {
    if (contradict || range.isContradictory())
      return true;
    if (!(range instanceof DiscreteGroupRange))
      return true;

    DiscreteGroupRange r = (DiscreteGroupRange) range;

    // since there is no a priori information about relationships among the
    // groups, the only way to tell that two are mutually exclusive is to find
    // one group included in one and excluded in the other.
    for (Enumeration e = inclusions.elements(); e.hasMoreElements(); )
      if (r.exclusions.contains(e.nextElement()))
        return true;

    for (Enumeration e = exclusions.elements(); e.hasMoreElements(); )
      if (r.inclusions.contains(e.nextElement()))
        return true;

    return false;
  }
}
