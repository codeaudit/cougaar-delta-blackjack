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

/**
 *  The QRuleComparison is a type of QRuleTest that represents a single
 *  comparison between two operands with respect to its component comparison
 *  operator.  Each instance may be one of many such comparisons in a complex
 *  logical expression (built up using instances of class QRuleLogicalTest
 *  (q.v.)) that serves as the rule's firing condition.
 */
public class QRuleComparison extends QRuleTest {
  // the left-hand operand in this comparison
  private QRuleOperand left = null;

  // the comparison operator in this test
  private QRuleOperator qrop = null;

  // the right-hand operand in this comparison
  private QRuleOperand right = null;

  /**
   *  Create a new comparison test with the given comparison operator and its
   *  left- and right-hand operands.
   *  @param l the left-hand operand
   *  @param o the comparison operator
   *  @param r the right-hand operand
   */
  public QRuleComparison (QRuleOperand l, QRuleOperator o, QRuleOperand r) {
    setUpComparison(l, o, r);
  }

  /**
   *  Create a new comparison test for a given rule and install it into the
   *  rule's test expression subordinate to the given parent test.  The
   *  comparison operator and the left- and right-hand operands are also
   *  supplied by the caller.
   *  @param qr the QRule to which this test will belong
   *  @param p the parent test, if any, or null if this is the root
   *  @param l the left-hand operand
   *  @param o the comparison operator
   *  @param r the right-hand operand
   */
  public QRuleComparison (QRule qr, QRuleLogicalTest p, QRuleOperand l,
      QRuleOperator o, QRuleOperand r)
  {
    super(qr, p);
    setUpComparison(l, o, r);
  }

  // install the comparison operator and its operands herein
  private void setUpComparison (QRuleOperand l, QRuleOperator o, QRuleOperand r)
  {
    left = l;
    qrop = o;
    right = r;
  }

  /**
   *  Return this QRuleTest as a QRuleComparison
   *  @return this test
   */
  public QRuleComparison getComparison () {
    return this;
  }

  /**
   *  Retrieve the left-hand operand in this comparison
   *  @return the left-hand operand
   */
  public QRuleOperand getOperand1 () {
    return left;
  }

  /**
   *  Specify the left-hand operand in this comparison
   *  @param op1 the new left-hand operand
   */
  public void setOperand1 (QRuleOperand op1) {
    left = op1;
  }

  /**
   *  Get the comparison operator used by this test
   *  @return the operator
   */
  public QRuleOperator getOperator () {
    return qrop;
  }

  /**
   *  Set the comparison operator to be used by this test
   *  @param op the new comparison operator
   */
  public void setOperator (QRuleOperator op) {
    qrop = op;
  }

  /**
   *  Retrieve the right-hand operand in this test
   *  @return the right-hand operand
   */
  public QRuleOperand getOperand2 () {
    return right;
  }

  /**
   *  Specify the right-hand operand to be used in this test
   *  @param op2 the new right-hand operand
   */
  public void setOperand2 (QRuleOperand op2) {
    right = op2;
  }

  /**
   *  Generate a portion of a JESS rule corresponding to this condition
   *  @return the JESS code in String form
   */
  public String toJESS () {
    StringBuffer sb = new StringBuffer("(");

    String op = getOperator().getJessName();
    if (op.equals("is"))
      op = "eq";
    else if (op.equals("isnot"))
      op = "neq";

    sb.append(op + " ");
    sb.append(getOperand1().toJESS() + " ");
    sb.append(getOperand2().toJESS());
    sb.append(")");
    return sb.toString();
  }

  /**
   *  Generate a portion of a JESS rule corresponding to this condition
   *  @param negate if true, then generate a negated version of the condition
   *  @return the JESS code in String form
   */
  public String toJESS (boolean negate) {
    if (negate)
      return "(not " + toJESS() + ")";
    else
      return toJESS();
  }

  /**
   * Generate a psuedo-readable version of the rule test
   * @return a concise string description of this rule test.
   */
  public String ruleToString() {
    return "(" +
      left.ruleToString() + " " + qrop.ruleToString() + " " + right.ruleToString() + ")";
  }
}
