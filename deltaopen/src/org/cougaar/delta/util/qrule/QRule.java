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

package org.cougaar.delta.util.qrule;

import java.io.*;
import java.text.*;
import java.util.*;
import java.beans.*;
import org.cougaar.delta.util.*;

/**
 * Represents a Qualification Rule. These rules are used to qualify candidates
 * (ie, assign a rating such as "negative" or "positive"). A rule has an action,
 * which is the rating that may be assigned by the rule, and a test. The test may
 * be compound (composed of multiple tests). When the rule is run on a candidate,
 * if the test is true and the candidate's current qualification has a lower precedence
 * than this rule's action, the candidate's qualification is changed to this rule's
 * action, and the candidate's qualification reason is changed to this rule's name.
 * If the test is true, (whether or not the qualification gets changed) this rule's
 * name is added to the candidate's list of qualifications.
 * A rule may be active or inactive; inactive rules are not applied to candidates.
 * The rule's toJESS method returns the defrule string needed to define this rule in JESS
 * syntax; this includes the instructions for when to change a candidate's qualification,
 * qualification reason, and list of qualifications.
 * It is possible to alter QRule so that there are more qualification ratings, for
 * example, instead of just positive, neutral, and negative you could define additional
 * levels such as slightly positive, or special ratings such as "requires additional
 * information".
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: QRule.java,v 1.1 2002-04-30 17:33:28 cerys Exp $
 */
public class QRule extends DBObject {
  // An English-translation engine for QRules
  private static QRuleToEnglish englishConverter = new QRuleToEnglish();

  private boolean invokedStandalone = false;
  private QRuleLogicalTest rootTest = null;
  private String action;
  private String name;
  private transient PropertyChangeSupport propertyChangeListeners = new PropertyChangeSupport(this);
  private static DecimalFormat dollar_format = new DecimalFormat("$0.00");

  // a state flag for QRules:  active vs. inactive
  private boolean active = false;

  // Definitions of the possible rule actions
  public static final String POSITIVE = "Positive";
  public static final String NEUTRAL = "Neutral";
  public static final String NEGATIVE = "Negative";


  // Priority levels for actions (high number takes precedence)
  public static final int NEGATIVE_LEVEL = 3;
  public static final int POSITIVE_LEVEL = 1;
  public static final int NEUTRAL_LEVEL = 0;


  /**
   * Create a new Qualification Rule without any tests.
   * @param aName the unique name for this rule
   * @param anAction the action for this rule.  Must be one of the 3 predefined actions
   * @return a new QRule
   */
  public QRule(String aName, String anAction) {
    name = aName;
    action = anAction;
  }

  /**
   * A test stub
   */
  public static void main(String[] args) {
  }

  /**
   *  Report whether this rule is "live" or still in the testing phase.  Test
   *  rules should be applied only to special test candidates. This feature
   *  is not currently implemented, so always returns false.
   *  @return true if and only if this rule is a test rule
   */

  public boolean isTestRule () {
    return false;
  }

  /**
   *  Specify whether this rule is active, i.e., whether it should be applied
   *  to Candidates in the sourcing engine.
   *  @param a true if and only if the rule is allowed to fire on Candidates
   */
  public void setActive (boolean a) {
    active = a;
  }

  /**
   *  Report whether this rule is active, i.e., whether it should be applied
   *  to Candidates in the sourcing engine.
   *  @return true if and only if the rule is allowed to fire on Candidates
   */
  public boolean isActive () {
    return active;
  }

  /**
   * Get the human-readable name associated with this QRule
   * @return the name by which we know the QRule
   */
  public String getName() {
    return name;
  }

  /**
   * Set the human-readable name associated with this QRule
   * @param newName the name by which we know the QRule
   */
  public void setName(String newName) {
    String  oldName = name;
    name = newName;
    if (propertyChangeListeners != null)
      propertyChangeListeners.firePropertyChange("name", oldName, newName);
  }

  /**
   * Get all of the tests for this rule
   * @return the root of the test expression tree for this rule
   */
  public QRuleLogicalTest getTest() {
    return rootTest;
  }

  /**
   *  Set the root of the rule's expression tree.  The root must be a logical
   *  test, so if the one provided is a simple comparison, a logical expression
   *  ("AND", by default) is created to contain it.
   *  @param t the new root QRuleTest
   */
  public void setTest (QRuleTest t) {
    QRuleLogicalTest qlt = t.getLogicalTest();
    if (qlt != null) {
      rootTest = qlt;
    }
    else {
      qlt = new QRuleLogicalTest(this, null, QRuleTest.LOGICAL_AND);
      t.setParent(qlt);
    }
  }

  /**
   * Get the action associated with this rule
   * @return the rule's action
   */
  public String getAction() {
    return action;
  }

  /**
   * Set the action for this rule
   * @param newAction the new action for this rule
   */
  public void setAction(String newAction) {
    String oldAction = action;
    action = newAction;
    if (propertyChangeListeners != null)
      propertyChangeListeners.firePropertyChange("action", oldAction, newAction);
  }

  /**
   * Generates the JESS defrule form that corresponds to this QRule
   * @return the JESS representation of this rule
   */
  public String simpleToJESS() {
    Enumeration test_list;
    QRuleTest test;
    StringBuffer sb = new StringBuffer();

    // rule name
    sb.append ("(defrule " + getName() + " ");

    // match a proposal object
    sb.append ("(candidate (OBJECT ?candidate)) ");

    // Convert the QRuleTest at the root of this rule's expression tree to a
    // JESS expression
    String testJess = rootTest.toJESS();
    if (testJess != null && testJess.length() > 0) {
      sb.append("(test ");
      sb.append(testJess);
      sb.append(")");
    }

    // rule action
    String action = getAction();
    int level = 0;
    if (action.equals(NEGATIVE))
        level = NEGATIVE_LEVEL;
    else if (action.equals(POSITIVE))
        level = POSITIVE_LEVEL;
    else if (action.equals(NEUTRAL))
        level = NEUTRAL_LEVEL;

    sb.append("=> ");
    sb.append("(bind ?r (get ?candidate qualificationLevel)) ");
    sb.append("(call ?candidate addQualification \"" + getName() + "\") ");
    sb.append("(if (< ?r " + level + ") then ");
    sb.append(  "(set ?candidate qualification \"" + action + "\") ");
    sb.append(  "(set ?candidate qualificationLevel " + level + ") ");
    sb.append(  "(set ?candidate reason \"" + getName() + "\") ");
    sb.append("))");

    return sb.toString();
  }


  /**
   * Cache support for momoizing toJESS results for this QRule object
   */

  private class JESSCacheStruct {
    private JESSCacheStruct(long version, Vector toJESS) {
      this.version = version;
      this.toJESS = toJESS;
    }
    private long version;
    private Vector toJESS;
    private long getVersion() {return version;}
    private Vector getToJESS() {return toJESS;}
  }

  private transient JESSCacheStruct toJESSCache = null;

  private void clearToJESSCache() {
    toJESSCache = null;
  }

  /**
   * Generates the JESS defrule form that corresponds to this QRule
   * @return vector of JESS representations of this rule
   */
  public Vector toJESS() {
      Vector toJESS = this.toJESSInternal();
      return toJESS;
  }

  /**
   * Generates the JESS defrule form that corresponds to this QRule
   * @return vector of JESS representations of this rule
   */
  public Vector toJESSInternal() {
    Vector v = new Vector();
    v.add(simpleToJESS());
    return v;
  }


  /**
   * Generates the English sentence that corresponds to this QRule
   * @return an English-language sentence.
   */
  public String toEnglish () {
    return englishConverter.convert(this);
  }

  /**
   * Generate a psuedo-readable version of the rule
   * @return a concise string
   */
  public String toString() {
    StringBuffer buf = new StringBuffer("Rule: ");
    buf.append(getName());
    buf.append("; Action:  ");
    buf.append(getAction());
    buf.append("; Test:  ");
    if (rootTest != null)
      buf.append(rootTest.ruleToString());
    else
      buf.append("<<NONE>>");
    return buf.toString();
  }


  /**
   *  Find the literal value corresponding to the named accessor, if any.
   */
  private Object getLiteral (String internalName) {
    if (rootTest == null ||
        !rootTest.getLogicalOp().equals(QRuleTest.LOGICAL_AND))
    {
      return null;
    }

    Enumeration enu = rootTest.getOperands();
    while (enu.hasMoreElements()) {
      QRuleComparison t = ((QRuleTest) enu.nextElement()).getComparison();
      if (t != null) {
        QRuleAccessorOperand o = (QRuleAccessorOperand) t.getOperand1();
        if (o.getInternalName().equals(internalName) &&
            t.getOperand2().isLiteral())
        {
          QRuleLiteralOperand lo = (QRuleLiteralOperand) t.getOperand2();
          return lo.getValue();
        }
      }
    }
    return null;
  }


  /**
   * two QRules are equivalent if they have the same clauses. The names
   * can differ, and the order of the clauses can differ, and the creation
   * date can differ, whether it is a test rule can differ, whether it is
   * active can differ.
   */
  public boolean equivalent(QRule rule2) {
    boolean ret = true;
    //compare the action
    ret = ret && this.action.equals(rule2.getAction());
    //compare the clauses if necessary
    if(ret) {
      QRuleLogicalTest test2 = rule2.getTest();
      ret = ret && this.getTest().equivalent(test2);
    }
    return ret;
  }
}
