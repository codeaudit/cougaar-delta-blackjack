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

import org.cougaar.delta.util.DBObject;
import java.beans.*;
import java.util.*;

/**
 *  Represents a Qualification Rule Test.  Currently, the test can take one of
 *  two forms:  a simple or "atomic" test (QRuleComparison), or a complex
 *  logical expression (QRuleLogicalTest).
 *  @author ALPINE (alpine-software@bbn.com)
 *  @version $Id: QRuleTest.java,v 1.1 2002-04-30 17:33:28 cerys Exp $
 */
public abstract class QRuleTest extends DBObject {
  /**
   *  This is the type value for QRuleLogicalTests representing the logical
   *  "AND" operator
   */
  public static final String LOGICAL_AND = "AND";

  /**
   *  This is the type value for QRuleLogicalTests representing the logical
   *  "OR" operator
   */
  public static final String LOGICAL_OR = "OR";

  /**
   *  This is the type value for QRuleLogicalTests representing the inverted
   *  logical "AND" operator
   */
  public static final String LOGICAL_NAND = "NAND";

  /**
   *  This is the type value for QRuleLogicalTests representing the inverted
   *  logical "OR" operator
   */
  public static final String LOGICAL_NOR = "NOR";

  // a reference to the containing rule
  private QRule rule = null;

  // a reference to the parent test, if any.  Only QRuleLogicalTests can have
  // subordinate QRuleTests
  private QRuleLogicalTest parent = null;

  /**
   *  Create a new QRuleTest with no properties
   */
  protected QRuleTest () {
  }

  /**
   *  Create a new QRuleTest within the context of a given QRule and parent
   *  test.  If the parent is null, then this test is presumed to be the root,
   *  and is set as such in the containing rule.
   *  @param r the containing rule
   *  @param p the parent logical operation in the rule test expression
   */
  protected QRuleTest (QRule r, QRuleLogicalTest p) {
    setRule(r);
    setParent(p);
    if (p == null && r != null)
      r.setTest(this);
  }

  /**
   *  Return this QRuleTest cast as an instance of QRuleComparison if this is,
   *  in fact, an instance of that class.  Otherwise, return null.
   *  @return the QRuleComparison, if this is one.
   */
  public QRuleComparison getComparison () {
    return null;
  }

  /**
   *  Return this QRuleTest cast as an instance of QRuleLogicalTest if this is,
   *  in fact, an instance of that class.  Otherwise, return null.
   *  @return the QRuleLogicalTest, if this is one.
   */
  public QRuleLogicalTest getLogicalTest () {
    return null;
  }

  /**
   *  Get the containing QRule for this test
   *  @return a qualification rule
   */
  public QRule getRule() {
    return rule;
  }

  /**
   *  Set the containing QRule for this test
   *  Note that a test can be part of only one rule.
   *  It is possible for a test to have a null rule.
   *  @param newRule the new rule for this test
   */
  public void setRule(QRule newRule) {
    rule = newRule;
  }

  /**
   *  Find the QRuleLogicalTest (logical operator) that sits above this one in
   *  the test hierarchy.
   *  @return the parent test
   */
  public QRuleLogicalTest getParent () {
    return parent;
  }

  /**
   *  Join this QRuleTest to an expression tree as a child of the given
   *  QRuleLogicalTest (logical operator), severing the link to the former
   *  parent in the process.
   *  @param p the new parent test
   */
  public void setParent (QRuleLogicalTest p) {
    if (parent != null)
      parent.removeOperand(this);
    parent = p;
    if (parent != null)
      parent.addOperand(this);
  }

  /**
   * returns true if this and <test2> have the same set of operands & operators
   */
   public boolean equivalent(QRuleTest test2) {
    boolean ret = true;
    //if both are logical tests, check that they have the same operands
    if(this.getComparison()==null && test2.getComparison() ==null) {
      Enumeration operands = this.getLogicalTest().getOperands();
      //check if are all of this's operands are in test 2
      int numOperands = 0;
      int numOperands2 = 0;
      while(operands.hasMoreElements()) {
        //count number of operands in this
        numOperands ++;
        boolean found = false;
        QRuleTest q = (QRuleTest) operands.nextElement();
        Enumeration operands2 = test2.getLogicalTest().getOperands();
        while(operands2.hasMoreElements()) {
          if(numOperands==1) {
            //count number of operands in test 2
            numOperands2++;
          }
          QRuleTest q2 = (QRuleTest) operands2.nextElement();
          if(q2.equivalent(q)) {
            found = true;
          }
        }
        ret = ret && found;
        if(!ret) {
          break;
        }
      }
      if(numOperands2 > numOperands || numOperands==0) {
        //check if all of test 2's operands are in this
        Enumeration operands2 = test2.getLogicalTest().getOperands();
        while(operands2.hasMoreElements()) {
          boolean found = false;
          QRuleTest q2 = (QRuleTest) operands2.nextElement();
          Enumeration operands1 = this.getLogicalTest().getOperands();
          while(operands1.hasMoreElements()) {
            QRuleTest q = (QRuleTest) operands1.nextElement();
            if(q.equivalent(q2)) {
              found = true;
              break;
            }
          }
          ret = ret && found;
          if(!ret) {
            break;
          }
        }
      }
    }
    //both are comparisons, check for equivalence operand1, operator, and operand2
    //since operand1 and the operator are created by the factory, can use .equals
    //operand2 may be created by the factory or it might be a literal
    //for literals, compare the toString and type
    else if(this.getLogicalTest()==null && test2.getLogicalTest()==null){
      QRuleComparison comp1 = this.getComparison();
      QRuleComparison comp2 = test2.getComparison();
      ret = ret && comp2.getOperator().equals(comp1.getOperator()) &&
        comp2.getOperand1().equals(comp1.getOperand1());
      if(comp1.getOperand2().isLiteral() && comp2.getOperand2().isLiteral()) {
        ret = ret &&
        comp2.getOperand2().toString().equals(comp1.getOperand2().toString()) &&
        comp2.getOperand2().getType().equals(comp1.getOperand2().getType()) ;
      }
      else if(!comp1.getOperand2().isLiteral() && !comp2.getOperand2().isLiteral()) {
        ret = comp2.getOperand2().equals(comp1.getOperand2());
      }
      else {
        ret = false;
      }
    }
    //if one is a comparison and one is not, they aren't equivalent
    else {
      ret = false;
    }
    return ret;
   }

  /**
   *  Create a JESS subexpression to represent the content of this test.  The
   *  containing rule or parent test will use this to assemble a larger
   *  expression or rule declaration.  The subclasses must provide the actual
   *  functionality.
   *  @return a String of JESS code encapsulating this test
   */
  public abstract String toJESS ();

  /**
   *  Create a JESS subexpression to represent the content of this test, with
   *  the logic optionally negated in the output.  The subclasses must provide
   *  the actual functionality.
   *  @param negate true if this condition should be negated in the output
   */
  public abstract String toJESS (boolean negate);

  /**
   *  Generate a pseudo-readable version of this QRuleTest.  The subclasses
   *  must provide the actual functionality.
   */
  public abstract String ruleToString ();
}


