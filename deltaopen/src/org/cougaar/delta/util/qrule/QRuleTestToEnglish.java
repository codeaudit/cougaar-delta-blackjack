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

import java.text.*;
import java.util.*;

/**
 *  The QRuleTestToEnglish class is designed to convert the individual tests
 *  within a QRule into English phrases and clauses, which can then be combined
 *  to form an English statement of the QRule.  Three classes that provide
 *  support (in the form of even smaller fragments of English) are
 *  QRuleOperatorToEnglish, QRuleAccessorToEnglish, and QRuleBooleanToEnglish.
 *  As the names suggest, these classes provide the English words associated
 *  with some of the components of a QRuleTest.
 */
public class QRuleTestToEnglish {
  // The format used for representing dollar amounts in the English sentences
  private static DecimalFormat dollar_format = new DecimalFormat("$0.00");
  // The format used for representing Dates in the English sentences
  private static SimpleDateFormat dateFormat = new SimpleDateFormat(
    "MM/dd/yyyy");

  // Some handy translators for individual parts of a test; i.e., operators
  // (opModeTable for normal operators and priorityOpTable for priority
  // conditions) and accessor operands (booleanTable for the boolean accessors
  // and operandTable for the non-boolean ones)
  private QRuleOperatorToEnglish opModeTable =
    new QRuleOperatorToEnglish(QRuleOperatorToEnglish.STANDARD);
  private QRuleOperatorToEnglish priorityOpTable =
    new QRuleOperatorToEnglish(QRuleOperatorToEnglish.PRIORITY);
  private QRuleAccessorToEnglish operandTable = new QRuleAccessorToEnglish();
  private QRuleBooleanToEnglish booleanTable = new QRuleBooleanToEnglish();

  /**
   *  Construct a new converter of QRuleTests to English.
   */
  public QRuleTestToEnglish () {
  }

  /**
   *  Generate the English translation of a QRuleTest, presumably for insertion
   *  into a sentence representing the entire QRule of which the test is a part.
   *  The default behavior of this method is to return an independent clause
   *  representing a statement of the comparison embodied in the QRuleTest.
   *  Two types of modification currently provided are described below.
   *  <br><br>
   *  The full_form flag causes the statement to appear with a reference to the
   *  "object" (being the LTA, item, requisition, or proposal) to which the
   *  tested property belongs.  For example, in reference to the "Weight", a
   *  test might read "the item's weight is greater than 50.0".
   *  <br><br>
   *  The predicate flag causes the statement to be rendered as a predicate
   *  rather than an independent clause.  Clearly, a subject will have to be
   *  provided externally for the result to be a complete sentence.  To
   *  continue using the example above, such a test might read "[the item] has
   *  Weight greater than 50.0", where the brackets indicate text generated
   *  elsewhere.
   *  <br><br>
   *  Incidentally, the "full_form" option takes precedence over the
   *  "predicate" option.
   *
   *  @param test the QRuleTest being rendered in English
   *  @param full_form a flag used to specify the full accessor reference
   *  @param predicate a flag used to specify the subjectless predicate phrase
   */
  public String translateTest (
      QRuleComparison t, boolean full_form, boolean predicate)
  {
    QRuleAccessorOperand left = (QRuleAccessorOperand) t.getOperand1();
    QRuleOperator op = t.getOperator();
    QRuleOperand right = t.getOperand2();

    return generateTestEnglish(left, op, right, full_form, predicate);
  }

  // Utility function that puts the string representation in quotation marks.
  // As a special case, it reports [blank] for the empty string
  private static String quoteAndReplaceBlanks (Object o) {
    if (o.toString().equals("")) return "[blank]";
    else return "\"" + o + "\"";
  }

  // This function gives the string representation of an object, returning
  // [blank] in case of the empty string.
  private static String replaceBlanks (Object o) {
    if (o.toString().equals("")) return "[blank]";
    else return o.toString();
  }

  // This function checks an object's string representation for numeric format
  // before returning that as its value.  If the string is empty, it returns
  // [blank], and if the string is not a number, it returns [invalid decimal]
  private static String replaceNonNumbers (Object o, boolean dollar) {
    String s = o.toString();
    if (s.equals(""))
      return "[blank]";
    double d;
    try {
      d = Double.valueOf(s).doubleValue();
    }
    catch (Exception e) {
      return "[invalid decimal]";
    }
    if (dollar)
      return dollar_format.format(d);
    else
      return String.valueOf(d);
  }

  private static String replaceNonDates (Object o) {
    if (o instanceof Date)
      return dateFormat.format((Date) o);
    String s = o.toString();
    if (s.length() == 0)
      return "[blank]";
    try {
      return dateFormat.format(dateFormat.parse(s));
    }
    catch (Exception bad_date) {
      return "[invalid date]";
    }
  }

  // This function checks an object's string representation for integer format
  // before returning it as its value.  If the string is empty, it returns
  // [blank], and if the string is not an integer, it returns [invalid integer]
  private static String replaceNonIntegers (Object o) {
    String s = o.toString();
    if (s.equals(""))
      return "[blank]";
    long l;
    try {
      l = Long.valueOf(s).longValue();
    }
    catch (Exception e) {
      return "[invalid integer]";
    }
    return String.valueOf(l);
  }

  // Render a literal operand in a fashion suitable for inclusion in an
  // English sentence.
  private static String literalToEnglish(
      QRuleLiteralOperand qop, String ostensibleType)
  {
    StringBuffer buf = new StringBuffer();
    String type = null;
    Object value = null;
    // check for missing values
    if (qop == null || (value = qop.getValue()) == null ||
        (type = qop.getType()) == null)
    {
      buf.append("<<NULL>>");
    }
    // "Vector" types are rendered as brace-delimited, comma-separated lists
    else if (type.equals("Vector")) {
      Vector v = (Vector) value;
      Enumeration enu = v.elements();
      buf.append("{");
      if (enu.hasMoreElements()) {
        Object elt = enu.nextElement();
        String delimiter = "";
        if (elt instanceof String)
          delimiter = "\"";
        buf.append(delimiter + elt + delimiter);
        while (enu.hasMoreElements()) {
          elt = enu.nextElement();
          buf.append(", " + delimiter + elt + delimiter);
        }
      }
      else
        buf.append("<<EMPTY>>");
      buf.append("}");
    }
    // check integers for valid numeric format and range
    else if (ostensibleType.equals("Integer")) {
      buf.append(replaceNonIntegers(qop));
    }
    // check floating point numbers for format and range
    else if (ostensibleType.equals("Float")) {
      buf.append(replaceNonNumbers(qop, false));
    }
    // check currency amounts for numeric format and range and render them
    // with a dollar sign and two digits following the decimal point
    else if (ostensibleType.equals("Currency")) {
      buf.append(replaceNonNumbers(qop, true));
    }
    else if (ostensibleType.equals("Date")) {
      buf.append(replaceNonDates(value));
    }
    // parse the geography:region pair and phrase it in English
    else if (ostensibleType.equals("PhysicalAddressType")) {
      String composite = qop.toString();
      int colon = composite.indexOf(":");
      if (colon == -1)
        buf.append(composite);
      else
        buf.append("Region ");
        buf.append(quoteAndReplaceBlanks(composite.substring(1 + colon)));
        buf.append(" of Geography ");
        buf.append(quoteAndReplaceBlanks(composite.substring(0, colon)));
    }
    // if all else fails, treat the value as a String
    else {
      buf.append(quoteAndReplaceBlanks(qop.toString()));
    }
    return buf.toString();
  }

  // generate and assemble English phrases for the left-hand operand, which
  // must be an accessor, the operator, and the right-hand operand to form
  // the English representation of the QRuleTest consisting of these parts
  private String generateTestEnglish (
      QRuleAccessorOperand left, QRuleOperator op, QRuleOperand right,
      boolean full_form, boolean predicate)
  {
    StringBuffer buf = new StringBuffer();
    String s;
    if (left.getType().equalsIgnoreCase("Boolean")) {
      buf.append(booleanTable.getOperand(
        ((QRuleAccessorOperand) left).getInternalName(),
        (predicate ? QRuleBooleanToEnglish.PREDICATE : QRuleBooleanToEnglish.FULL_FORM),
        ((Boolean) ((QRuleLiteralOperand) right).getValue()).booleanValue()));
    }
    else {
      // poll the boolean flags and the operator to determine the "form" of the
      // accessor operand.
      boolean articulating = opModeTable.isArticulating(op.getJessName());
      boolean negating = opModeTable.isVerbNegating(op.getJessName());
      int accForm = 0;
      if (full_form)
        accForm = QRuleAccessorToEnglish.FULL_FORM;
      else if (!predicate)
        accForm = QRuleAccessorToEnglish.STANDARD;
      else
        accForm = QRuleAccessorToEnglish.PREDICATE;

      buf.append(operandTable.getOperand(
        ((QRuleAccessorOperand) left).getInternalName(), accForm, articulating,
        negating));

      String leftName = left.getInternalName();

      boolean verbOperator = !predicate;
      if (leftName.equals("Priority") || leftName.equals("IPG"))
        s = priorityOpTable.getOperator(op.getJessName(), verbOperator);
      else
        s = opModeTable.getOperator(op.getJessName(), verbOperator);
      if (buf.length() > 0 && s.length() > 0)
        buf.append(" ");
      buf.append(s);

      String ostensibleType = left.getUiType();

      if (right.isLiteral()) {
        s = literalToEnglish((QRuleLiteralOperand) right, ostensibleType);
      }
      else {
        QRuleAccessorOperand qop = (QRuleAccessorOperand) right;
        s = operandTable.getOperand(qop.getInternalName(),
          QRuleAccessorToEnglish.FULL_FORM, false, false);
      }
      if (buf.length() > 0 && s.length() > 0)
        buf.append(" ");
      buf.append(s);
    }
    return buf.toString();
  }
}
