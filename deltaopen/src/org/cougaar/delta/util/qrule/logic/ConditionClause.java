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
 *  The ConditionClause class represents a collection of QRuleComparisions
 *  joined to gether by the "AND" QRuleLogicalTest.  The comparisons are
 *  aggregated by the attribute (QRuleAccessorOperand) to which they apply,
 *  forming AttributeRange objects.  AttributeRanges that contain a unique
 *  identifier for an entity are distinguished for any special treatment that
 *  may apply to them.
 *  <br><br>
 *  Conditions that compare two accessors must be given separate consideration,
 *  which may not be implemented below.
 */
public class ConditionClause {
  private Hashtable ranges = new Hashtable();
  private Vector uniqueIds = new Vector();
  private String name = null;
  private Vector detritus = new Vector();

  // check a comparison test for format and, if acceptable, add it to a list
  private void addComparison (Vector conds, QRuleComparison c) {
    // if it's not a comparison test, ignore it
    if (c == null)
      return;

    QRuleOperand left = c.getOperand1();
    QRuleOperand right = c.getOperand2();
    // For the moment, only comparisons where an accessor is on the left and
    // a literal is on the right are considered--all others are ignored
    if (left instanceof QRuleAccessorOperand &&
        right instanceof QRuleLiteralOperand)
    {
      conds.add(c);
    }
    else {
      detritus.add(c);
    }
  }

  /**
   *  Form a nameless ConditionClause from a list of comparison tests
   */
  public ConditionClause (QRuleLogicalTest qlt) {
    this(null, qlt);
  }

  /**
   *  Form a named ConditionClause from a list of comparison tests
   */
  public ConditionClause (String s, QRuleLogicalTest qlt) {

    Vector v = new Vector();
    for (Enumeration e = qlt.getOperands(); e.hasMoreElements(); ) {
      QRuleTest t = (QRuleTest) e.nextElement();
      QRuleComparison c = t.getComparison();
      addComparison(v, c);
    }

    name = s;

    AttributeFactory attFact = AttributeFactory.getInstance();

    Hashtable t = new Hashtable();
    for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
      QRuleComparison c = (QRuleComparison) e.nextElement();
      QRuleAccessorOperand attribute = (QRuleAccessorOperand) c.getOperand1();
      String attName = attribute.getInternalName();
      Vector conds = (Vector) t.get(attName);
      if (conds == null) {
        conds = new Vector();
        t.put(attName, conds);
      }
      conds.add(c);
    }

    for (Enumeration e = t.elements(); e.hasMoreElements(); ) {
      Vector conds = (Vector) e.nextElement();
      QRuleComparison qc = (QRuleComparison) conds.elementAt(0);
      QRuleAccessorOperand qrop = (QRuleAccessorOperand) qc.getOperand1();
      addRange(attFact.makeRangeFor(qrop, conds));
    }
  }

  /**
   *  add a new AttributeRange, distinguishing the unique ones as such
   *  @param range the AttributeRange to be included
   */
  public void addRange (AttributeRange range) {
    if (range == null)
      return;

    ranges.put(range.getAttribute().getInternalName(), range);
    if (range.isUnique())
      uniqueIds.addElement(range);
  }

  /**
   *  remove an AttributeRange
   *  @param name the name of the attribute whose constraints are to be removed
   */
  public void removeRange (String name) {
    AttributeRange range = (AttributeRange) ranges.remove(name);
    if (range != null && range.isUnique())
      uniqueIds.remove(range);
  }

  /**
   *  Retrieve the name of the clause that this ConditionClause represents
   */
  public String getName () {
    return name;
  }

  /**
   *  Return a list of the AttributeRanges in this clause that give a unique
   *  specification for an entity
   *  @return the unique attribute ranges
   */
  public Enumeration getUniqueIds () {
    return uniqueIds.elements();
  }

  /**
   *  retrieve the AttributeRange corresponding to the named attribute
   *  @param name the name of the attribute whose constraints are being fetched
   *  @return the constraints for the named attribute
   */
  public AttributeRange getRange (String name) {
    return (AttributeRange) ranges.get(name);
  }

  /**
   *  Report whether this condition clause "contains" another clause.
   *  Equivalently, report whether the other clause <it>implies</it> this one.
   *  The "contains" method reports true if and only if every AttributeRange
   *  specified in this clause contains the corresponding range (which must
   *  exist) in the other clause.
   *  @param clause the clause being compared to this one
   *  @return true if the argument logically implies this clause
   */
  public boolean contains (ConditionClause clause) {
    Enumeration keys = ranges.keys();
    while (keys.hasMoreElements()) {
      String k = (String) keys.nextElement();
      AttributeRange ours = (AttributeRange) ranges.get(k);
      AttributeRange theirs = (AttributeRange) clause.getRange(k);
      // if "theirs" is null, then the other clause allows the full range of
      // the attribute; this one has constraints, so it can't contain the other
      // clause (except in case of a tautology)
      if (theirs == null || !ours.contains(theirs))
        return false;
    }
    return true;
  }

  /**
   *  Report whether this condition clause excludes another clause.  In other
   *  words, report whether the two clauses are <it>mutually exclusive</it>.
   *  The "excludes" method returns true if and only if at least one of the
   *  ranges specified in this clause excludes the corresponding range (which
   *  must exist) in the other clause.
   */
  public boolean excludes (ConditionClause clause) {
    Enumeration keys = ranges.keys();
    while (keys.hasMoreElements()) {
      String k = (String) keys.nextElement();
      AttributeRange ours = (AttributeRange) ranges.get(k);
      AttributeRange theirs = (AttributeRange) clause.getRange(k);
      // if "theirs" is null, then the other clause allows the full range of
      // the attribute; hence, for the attribute in question, there is no way
      // ranges could be mutually exclusive (except in case of a contradiction)
      if (theirs != null && ours.excludes(theirs))
        return true;
    }
    return false;
  }

  // check the uniquely identified entities in this clause for multiples and
  // store the category names in a Vector for future comparison
  private Vector checkUids (Vector messages) {
    Vector ourUids = new Vector();
    Vector overflowUids = new Vector();
    for (Enumeration enu = getUniqueIds(); enu.hasMoreElements(); ) {
      String category =
        ((AttributeRange) enu.nextElement()).getAttribute().getUiCategory();
      if (!ourUids.contains(category))
        ourUids.add(category);
      else if (!overflowUids.contains(category)) {
        messages.add("Entity \"" + category +
          "\" is uniquely specified more than once");
        overflowUids.add(category);
      }
    }
    return ourUids;
  }

  /**
   *  A convenience method for calling the isConsistent method without supplying
   *  the null argument.
   */
  public Vector isConsistent () {
    return isConsistent(null);
  }

  /**
   *  Check for the logical consistency of this clause in the context of the
   *  clause provided (if any).  When a context is provided, it is assumed that
   *  the current clause is an exception in a rule with the context as its main
   *  clause.  If no context is supplied, then the current clause is presumed
   *  to be the main clause of the rule to which it belongs.
   *  @param context the clause to which this is an exception (if any)
   *  @return a Vector containing the error messages resulting from the check
   */
  public Vector isConsistent (ConditionClause context) {
    // store messages for return
    Vector messages = new Vector();

    // store the context's uniquely identified entities for future comparison
    Vector contextUids = new Vector();
    if (context != null)
      for (Enumeration enu = context.getUniqueIds(); enu.hasMoreElements(); )
        contextUids.add(
          ((AttributeRange) enu.nextElement()).getAttribute().getUiCategory());

    // check the current clause's unique ids
    Vector ourUids = checkUids(messages);

    Enumeration keys = ranges.keys();

    // check to see if any conditions are specified
    if (!keys.hasMoreElements() && detritus.size() == 0) {
      if (context == null)
        messages.add("no conditions found in the rule");
      else
        messages.add(
          "unqualified exception--rule is rendered completely ineffective");
    }

    // make sure the conditions found in this clause are okay
    while (keys.hasMoreElements()) {
      String k = (String) keys.nextElement();
      AttributeRange ours = (AttributeRange) ranges.get(k);
      String category = ours.getAttribute().getUiCategory();
      String uiName = ours.getAttribute().getUiName();

      // note whether the range is contradictory, tautological, or redundant
      if (ours.isContradictory())
        messages.add(uiName + " never satisfies these conditions");
      else if (ours.isRedundant())
        messages.add("some conditions on " + uiName + " are redundant");
      else if (ours.hasTautology())
        messages.add(uiName + " always satisfies some of these conditions");
      else if (ours.violatesDomain())
        messages.add("found inappropriate or malformed values for " + uiName);

      // if it's internally consistent, and a parent clause was provided,
      // check for conflicts with the parent context
      else if (messages.size() == 0 && context != null) {
        // no use fussing with unique entities
        if (contextUids.contains(category))
          messages.add("the constraints on \"" + uiName +
            "\" are irrelevant since the \"" + category +
            "\" is uniquely specified by the main conditions");

        // Compare with the parent's range for a given attribute.  If none is
        // defined or one is defined but is empty, then there is no useful work
        // to do here.
        AttributeRange theirs = context.getRange(k);
        if (theirs != null && !theirs.isContradictory()) {
          // If our range contains the parent's range, then the range does not
          // constrain the exception clause's domain in a meaningful way
          if (ours.contains(theirs))
            messages.add("the constraints on \"" + uiName +
              "\" are implied by the main conditions");
          // If our range excludes the parent's range, then the exception
          // and the main condition are mutually exclusive.  Hence, the
          // exception case will never apply.
          else if (ours.excludes(theirs))
            messages.add("the constraints on \"" + uiName +
              "\" are precluded by the main conditions");
        }
      }

      // see if the condition is irrelevant due to unique entities
      if (!ours.isUnique() && ourUids.contains(category))
        messages.add("conditions on " + uiName + " are irrelevant; the " +
          category + " is uniquely specified");
    }

    return messages;
  }
}
