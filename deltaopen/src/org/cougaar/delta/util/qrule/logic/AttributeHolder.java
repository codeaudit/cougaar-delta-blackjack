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
 *  This is the base class for implementations of AttributeRange (q.v.).  The
 *  most important function embodied here is the logic of unique entity
 *  definition.
 */
public abstract class AttributeHolder implements AttributeRange {
  protected Vector constraints = null;
  protected QRuleAccessorOperand attribute = null;
  protected boolean unique = false;
  protected boolean redundant = false;
  protected boolean contradict = false;
  protected boolean tautology = false;
  protected boolean violateDomain = false;

  /**
   *  Construct an empty holder for attribute constraints
   */
  protected AttributeHolder () {
  }

  // check for an entity's unique identifier among these constraints
  private void checkUniqueness () {
    String attName = attribute.getInternalName();
    if (attName.equals("ContractID") || attName.equals("NSN") ||
        attName.equals("MfgCAGEPN"))
    {
      for (Enumeration enu = constraints.elements(); enu.hasMoreElements(); ) {
        QRuleOperator q = ((QRuleComparison) enu.nextElement()).getOperator();
        if (q.getJessName().equals("eq")) {
          unique = true;
          return;
        }
      }
    }
    unique = false;
  }

  /**
   *  populate this AttributeRange implementation with the value constraints
   *  @param att the attribute contained herein
   *  @param c a vector of constraints on that attribute
   */
  public void assumeConstraints (QRuleAccessorOperand att, Vector c) {
    attribute = att;
    constraints = c;
    init();
    checkUniqueness();
  }

  /**
   *  Initialize this AttributeRange by assimilating the constraints.  The
   *  details of this operation will depend on the nature of the attribute
   *  being modeled.
   */
  protected abstract void init ();

  /**
   *  Retrieve the attribute being modeled by this AttributeRange
   *  @return the attribute
   */
  public QRuleAccessorOperand getAttribute () {
    return attribute;
  }

  /**
   *  Tell whether this range specifies a unique entity
   *  @return true if it does; false otherwise
   */
  public boolean isUnique () {
    return unique;
  }

  /**
   *  Tell whether some of the conditions defining this AttributeRange are
   *  redundant.
   *  @return true if and only if some conditions are redundant
   */
  public boolean isRedundant () {
    return redundant;
  }

  /**
   *  Tell whether the conditions are contradictory; i.e., whether the set of
   *  values satisfying the conditions is empty.
   *  @return true if and only if the constraints are contradictory
   */
  public boolean isContradictory () {
    return contradict;
  }

  /**
   *  Tell whether some of the conditions are tautological.  The exact meaning
   *  of a condition's being a tautology is context dependent.
   *  @return true if and only if a trivial condition is detected
   */
  public boolean hasTautology () {
    return tautology;
  }

  /**
   *  Tell whether one of the conditions contains references to values not
   *  permitted for the attribute in question.  This flag will be raised in
   *  cases where literal values are malformed (i.e., cannot be interpreted as
   *  correct values) or lie outside the permitted range (e.g., negative values
   *  for an attribute that must be positive).
   *  @return true if and only if a domain violation is detected
   */
  public boolean violatesDomain () {
    return violateDomain;
  }
}
