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
 *  The purpose of the QRuleToEnglish class is to provide an engine for
 *  inspecting a QRule and producing an English statement of that rule.
 */
public class QRuleToEnglish {

  // We need to have a QRuleTest-to-English translator
  private QRuleTestToEnglish testToEnglish = null;

  /**
   *  Create a new QRule-to-English translator
   */
  public QRuleToEnglish () {
    testToEnglish = new QRuleTestToEnglish();
  }

  /**
   *  Generates the English sentence that corresponds to the given QRule
   *  @param q the QRule to be converted to English
   *  @return a sentence in the English language.
   */
  public String convert (QRule q) {
    // extract the rule's action property
    String action = q.getAction();

    Vector conditionss = new Vector();
    Vector exceptions = new Vector();

    QRuleLogicalTest qlt = q.getTest();
    if (qlt == null || !qlt.getLogicalOp().equals(QRuleTest.LOGICAL_AND))
      return "";

    partitionTests(qlt.getOperands(), conditionss, exceptions);

    StringBuffer english = new StringBuffer();

    english.append(writeRule(action, conditionss));

    // Include the exception clauses, if any
    if (exceptions.size() > 0) {
      english.append("\n\n");
      english.append(
        writeExceptions(exceptions));
    }

    // Capitalize the sentence and place a period at the end, etc.
    return punctuateAsSentence(english.toString());
  }

  private void partitionTests (Enumeration tests, Vector conditions, Vector exceptions)
  {
    while (tests.hasMoreElements()) {
      QRuleTest t = (QRuleTest) tests.nextElement();
      QRuleComparison c = t.getComparison();
      if (c == null) {
        QRuleLogicalTest qlt = t.getLogicalTest();
        if (qlt != null && qlt.getLogicalOp().equals(QRuleTest.LOGICAL_NAND))
          exceptions.addElement(t);
      }
      else
        conditions.addElement(t);
    }
  }

  // Find the accessor operand in a test, if there is one.  There should always
  // be one (or more), and it should be on the left, but things might go wrong.
  // If the left operand is an accessor, it is returned.  Otherwise check the
  // right operand.  If no accessor is found, return null.
  private static QRuleAccessorOperand getAccessorOperand (QRuleComparison t) {
    if (t == null)
      return null;
    QRuleOperand left = t.getOperand1();
    QRuleOperand right = t.getOperand2();
    if (!left.isLiteral())
      return (QRuleAccessorOperand) left;
    else if (!right.isLiteral())
      return (QRuleAccessorOperand) right;
    else
      return null;
  }

  // This method is supposed to be complementary to getAccessorOperand,
  // returning the operand not returned by getAccessorOperand.  In all normal
  // cases, this method should select the right operand.  If both operators are
  // literals, it returns null.
  private static QRuleOperand getOtherOperand (QRuleComparison t) {
    if (t == null)
      return null;
    QRuleOperand left = t.getOperand1();
    QRuleOperand right = t.getOperand2();
    if (!left.isLiteral())
      return right;
    else if (!right.isLiteral())
      return left;
    else
      return null;
  }


  // Handle the case of a global rule.  The sentence takes its form
  // from among:
  //   1.  (action phrase) if (other conditions)
  //   2.  (degenerate action phrase)
  // depending on whether there are any tests at all in this rule
  private String writeRule (String action, Vector conditions) {
    if (conditions.size() == 0)
      return actionPhrase(action, true, false);

    return actionPhrase(action, false, false) + " when" +
      actionQualifier(conditions);
  }


  // Create an English representation of the exception clauses associated with
  // the rule.  In most cases, these will be phrased as exceptions (i.e.,
  // "EXCEPT IF ..."), but if the rule is a SERVES-ONLY rule, then they should
  // be phrased as inclusions.
  private String writeExceptions (Vector v) {
    StringBuffer buf = new StringBuffer();
    buf.append("EXCEPT IF");

    int n = v.size();
    Enumeration enu = v.elements();
    if (n == 1) {
      buf.append(" ");
      buf.append(writeExceptionClause((QRuleLogicalTest) enu.nextElement()));
    }
    else {
      buf.append("\n1.  ");
      QRuleLogicalTest t = (QRuleLogicalTest) enu.nextElement();
      buf.append(writeExceptionClause(t));
      for (int i = 2; i < n; i++) {
        t = (QRuleLogicalTest) enu.nextElement();
        buf.append(";\n");
        buf.append(i);
        buf.append(".  ");
        buf.append(writeExceptionClause(t));
      }
      t = (QRuleLogicalTest) enu.nextElement();
      buf.append(";\nOR\n");
      buf.append(n);
      buf.append(".  ");
      buf.append(writeExceptionClause(t));
    }
    return buf.toString();
  }

  private String writeExceptionClause (QRuleLogicalTest root) {
    Vector phrases = new Vector();
    Enumeration enu = root.getOperands();
    if (!enu.hasMoreElements())
      return "[Unqualified Exception]";
    while (enu.hasMoreElements()) {
      QRuleComparison c = ((QRuleTest) enu.nextElement()).getComparison();
      if (c != null)
        phrases.addElement(testToEnglish.translateTest(c, true, false));
    }
    return punctuateAsList(phrases);
  }


  // Use the QRuleTests pertaining to the Requisition and Proposal to produce a
  // description of the conditions under which this rule applies
  private String actionQualifier (Vector otherTests) {
    Vector phrases = new Vector();
    Enumeration enu = otherTests.elements();
    while (enu.hasMoreElements())
      phrases.addElement(
        testToEnglish.translateTest((QRuleComparison) enu.nextElement(),
          true, false));

    return punctuateAsList(phrases);
  }

  // Convert the rule's action property into a phrase that can be inserted into
  // an English sentence.  The procedure is parametrized by the three types of
  // rules, which are "LTA", "Item", and "Global".  Furthermore, the phrase
  // can be rendered in degenerate form or conjugated for a plural subject.
  private String actionPhrase (
      String action, boolean degenerate, boolean pluralConjugation)
  {
    if (action == null)
      return "?";

    if (action.equals(QRule.NEGATIVE))
        return (degenerate ? "no" : "a") + " request should " +
          (degenerate ? "" : "not ") + "be approved";
    if (action.equals(QRule.POSITIVE))
        return (degenerate ? "every" : "a") + " request should be approved";
    if (action.equals(QRule.NEUTRAL))
        return (degenerate ? "every" : "a") +
          " request may be approved";

    return "<<action=" + action + ">>";
  }

  // Capitalize the first word and place a period at the end.  Also, search
  // through the text for occurrences of "a" that should be converted to "an"
  private String punctuateAsSentence (String s) {
    if (s == null || s.length() == 0)
      return "Oh, nothing, really.";

    // search for the word "a" and replace it with "an", if necessary
    StringBuffer buf = new StringBuffer();
    int n = s.length();
    int j = 0;
    while (true) {
      int k = s.indexOf(" a ", j);
      if (k < 0 || n < k + 4) {
        buf.append(s.substring(j));
        break;
      }
      else {
        buf.append(s.substring(j, k));
        j = k + 3;
        buf.append(" a");
        char c = Character.toLowerCase(s.charAt(k + 3));
        if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u')
           buf.append('n');
        buf.append(' ');
      }
    }
    buf.setCharAt(0, Character.toUpperCase(buf.charAt(0)));
    if (buf.charAt(buf.length() - 1) == ',')
      buf.setCharAt(buf.length() - 1, '.');
    else
      buf.append('.');
    return buf.toString();
  }

  // Organize the elements of the vector as they should be in an English
  // sentence (depending on the number), "<i_0>", "<i_0> and <i_1>", or
  // "<i_0>, <i_1>, ..., and <i_(n-1)>".
  // This method should never be called on an empty Vector.  If this happens,
  // "<<NIL>>" is returned.
  private String punctuateAsList (Vector v) {
    int n = v.size();
    if (n == 0)
      return "<<NIL>>";
    if (n == 1)
      return (String) v.elementAt(0);
    if (n == 2)
      return v.elementAt(0) + " and " + v.elementAt(1);
    StringBuffer buf = new StringBuffer();
    Enumeration enu = v.elements();
    for (int i = 0; i < n - 1; i++) {
      buf.append(enu.nextElement() + ", ");
    }
    buf.append("and " + enu.nextElement());
    return buf.toString();
  }

  // Utility function that puts the string representation in quotation marks.
  // As a special case, it reports [blank] for the empty string
  private static String quoteAndReplaceBlanks (Object o) {
    if (o.toString().equals("")) return "[blank]";
    else return "\"" + o + "\"";
  }
}
