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

import java.util.*;

/**
 *  A QRuleLogicalTest is a type of QRuleTest that represents a logical
 *  expression in terms of other QRuleTests.  The subordinate tests can be
 *  either simple tests or other logical operations, providing the ability to
 *  construct arbitrary logical expressions from the basic tests.
 *  <br><br>
 *  The operators currently available are "AND", "OR", "NAND", and "NOR", which
 *  provide the correct logical behavior for zero or more operand tests.  Note
 *  that unary "NOT" isn't included, but either "NAND" or "NOR" applied to a
 *  single operand has the same effect.
 */
public class QRuleLogicalTest extends QRuleTest {
  // Two JESS expressions that always evaluate to TRUE or FALSE.  These are
  // needed to make the JESS for degenerate logical operands mathematically
  // correct.
  private static final String JESS_TRUE = "(or TRUE TRUE)";
  private static final String JESS_FALSE = "(or FALSE FALSE)";

  // The logical operation that this test denotes
  private String logicOp = null;

  // the subordinate tests
  private Vector operands = new Vector();

  // the name associated with this logical test
  private String name = null;

  /**
   *  Create a new logical subexpression with the given logical operator, but
   *  no operands.
   *  @param op the logical operator
   */
  public QRuleLogicalTest (String op) {
    logicOp = op;
  }

  /**
   *  Create a new logical subexpression for a given rule and install it into
   *  the rule's test expression subordinate to the given parent test.  The
   *  logical operator for the new test is also supplied by the caller, but
   *  there are initially no operands.
   *  @param qr the rule to contain the new test
   *  @param p the logical operator of which the new test is an operand
   *  @param op the operator ascribed to the new test
   */
  public QRuleLogicalTest (QRule qr, QRuleLogicalTest p, String op) {
    super(qr, p);
    logicOp = op;
  }

  /**
   *  Return this QRuleTest as a QRuleLogicalTest
   *  @return this test
   */
  public QRuleLogicalTest getLogicalTest () {
    return this;
  }

  /**
   *  Set the type of operation performed by this test.  It should be one of
   *  the supported logical operators "AND", "OR", "NAND", or "NOR".
   *  @param op the new logical operator
   */
  public void setLogicalOp (String op) {
    logicOp = op;
  }

  /**
   *  Find the type of operation performed by this test.
   *  @return the logical operator
   */
  public String getLogicalOp () {
    return logicOp;
  }

  /**
   *  Obtain the name assigned to this test.  The name does not affect the
   *  structure of the containing rule.  Rather, it serves to allow users to
   *  recognize particular compound tests more easily.
   *  @return the clause's current name
   */
  public String getName () {
    return name;
  }

  /**
   *  Assign a name to this test.  The name does not affect the structure of
   *  the containing rule.  Rather, it serves to allow users to recognize
   *  particular compound tests more easily.
   *  @param s the new value for the clause's name attribute
   */
  public void setName (String s) {
    name = s;
  }

  /**
   *  Insert a new test as an operand of this logical expression.
   *  @param t the new operand
   */
  public void addOperand (QRuleTest t) {
    operands.addElement(t);
  }

  /**
   *  Remove a test from the list of operands subject to this logical operator
   *  @param t the operand to be removed
   */
  public void removeOperand (QRuleTest t) {
    operands.removeElement(t);
  }

  /**
   *  Retrieve the list of this test's operands in the form of an Enumeration.
   *  @return the operands belonging to this test.
   */
  public Enumeration getOperands () {
    return operands.elements();
  }

  /**
   *  Produce a String of JESS code that encapsulates the logic of this test
   *  applied to its operands.
   *  @return the JESS encoding of this test
   */
  public String toJESS () {
    return toJESS(false);
  }

  /**
   *  Produce a String of JESS code that encapsulates this test's operator
   *  being applied to its operands, with the logic optionally inverted.
   *  @param negate true if the JESS logic should be inverted in the output
   *  @return the JESS encoding of this test
   */
  public String toJESS (boolean negate) {
    String jessOp = null;
    if (logicOp.equals(QRuleTest.LOGICAL_NAND)) {
      jessOp = "and";
      negate = !negate;
    }
    else if (logicOp.equals(QRuleTest.LOGICAL_NOR)) {
      jessOp = "or";
      negate = !negate;
    }
    else if (logicOp.equals(QRuleTest.LOGICAL_AND)) {
      jessOp = "and";
    }
    else if (logicOp.equals(QRuleTest.LOGICAL_OR)) {
      jessOp = "or";
    }
    else {
      return "<<BAD LOGICAL OPERATOR:  " + logicOp + ">>";
    }

    if (operands.size() == 0) {
      return (jessOp.equals("and") ^ negate ? JESS_TRUE : JESS_FALSE);
    }
    else if (operands.size() == 1) {
      return ((QRuleTest) operands.elementAt(0)).toJESS(negate);
    }
    else {
      StringBuffer buf = new StringBuffer();
      if (negate)
        buf.append("(not ");
      buf.append("(");
      buf.append(jessOp);
      for (Enumeration enu = operands.elements(); enu.hasMoreElements(); ) {
        buf.append(" ");
        buf.append(((QRuleTest) enu.nextElement()).toJESS());
      }
      buf.append(")");
      if (negate)
        buf.append(")");
      return buf.toString();
    }
  }

  /**
   *  Generate a pseudo-readable String expression of this condition.
   *  @return the String representation
   */
  public String ruleToString () {
    boolean negate = false;
    String op = logicOp;
    if (logicOp.equals(LOGICAL_NAND)) {
      negate = true;
      op = LOGICAL_AND;
    }
    else if (logicOp.equals(LOGICAL_NOR)) {
      negate = true;
      op = LOGICAL_OR;
    }
    StringBuffer buf = new StringBuffer();
    if (negate)
      buf.append("NOT ");
    buf.append("(");
    Enumeration enu = getOperands();
    if (enu.hasMoreElements()) {
      buf.append(((QRuleTest) enu.nextElement()).ruleToString());
      while (enu.hasMoreElements()) {
        buf.append(" ");
        buf.append(op);
        buf.append(" ");
        buf.append(((QRuleTest) enu.nextElement()).ruleToString());
      }
    }
    else {
      buf.append("<<" + logicOp + ":  NO OPERANDS>>");
    }
    buf.append(")");
    return buf.toString();
  }
}
