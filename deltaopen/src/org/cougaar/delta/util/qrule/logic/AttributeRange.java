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

import org.cougaar.delta.util.qrule.QRuleAccessorOperand;
import java.util.*;

/**
 *  The AttributeRange interface is an abstraction of a collection of
 *  conditions (QRuleComparison) governing a single attribute.  The purpose
 *  is to facilitate a logical analysis of the rule containing them.  Thus,
 *  an instance is required to be able both to report any logical errors in its
 *  own collection and also to compare itself to other similar structures.
 *  <br><br>
 *  Classes that implement this interface will be specialized to handle the
 *  different types of attribute to which conditions may apply.
 */
public interface AttributeRange {
  /**
   *  Report the accessor object associated with the attribute governed by the
   *  conditions grouped herein.
   *  @return the QRuleAccessorOperand's name
   */
  public QRuleAccessorOperand getAttribute ();

  /**
   *  Report whether this set of conditions uniquely identifies an entity.
   *  (such as a Contract or Item)
   *  @return true if and only if this range uniquely identifies an entity.
   */
  public boolean isUnique ();

  /**
   *  Report whether there is at least one redundant condition; i.e., one that
   *  does not logically affect the rule.
   *  @return true if and only if this range is redundantly overspecified.
   */
  public boolean isRedundant ();

  /**
   *  Report whether the conditions are contradictory; i.e., that it is
   *  impossible to satisfy all the conditions simultaneously.
   *  @return true if and only if this range is empty.
   */
  public boolean isContradictory ();

  /**
   *  Report whether there is a tautological condition; i.e., one for which it
   *  is never possible for the condition not to be satisfied.
   *  @return true if and only if this range contains all permissible values
   */
  public boolean hasTautology ();

  /**
   *  Report whether there is a domain violation; i.e., a value reference
   *  outside the domain of possible values for the Attribute being represented
   */
  public boolean violatesDomain ();

  /**
   *  Compare the range of values satisfying these conditions with another
   *  similar range, and return true if and only if the other range is a subset
   *  of this one.
   *  @param range the other AttributeRange being compared to this one
   *  @return true if and only if the other range is a subset of this one
   */
  public boolean contains (AttributeRange range);

  /**
   *  Compare the range of values satisfying these conditions with another
   *  similar range, and return true if and only if the other range does not
   *  intersect this one.
   *  @param range the other AttributeRange being compared to this one
   *  @return true if and only if the other range does not intersect this one
   */
  public boolean excludes (AttributeRange range);

  /**
   *  Load a set of constraints to define the range of values embodied by this
   *  AttributeRange instance.
   *  @param att the attribute whose values are being confined
   *  @param c the constraints on the attribute
   */
  public void assumeConstraints (QRuleAccessorOperand att, Vector c);
}
