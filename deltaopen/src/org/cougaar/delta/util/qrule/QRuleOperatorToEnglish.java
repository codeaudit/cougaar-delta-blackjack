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

import java.util.Hashtable;

/**
 *  The purpose of this class is to provide ready access to the English
 *  translations of QRuleOperators used in QRules.  The public interface
 *  consists primarily of the "getOperator" method which takes an operator and
 *  renders an English description in a desired form.  Also included are two
 *  methods (described below) for testing attributes of the operator that are
 *  relevant to the way its operands are to be rendered in context.
 *  <br><br>
 *  Its other function, which takes place behind the scenes, is to construct
 *  (or, in the future, <i>retrieve</i>) a table of operator mappings that
 *  makes the conversion to English possible.
 */
public class QRuleOperatorToEnglish {
  // A table for mapping operator names to the information needed to render
  // them in English
  private Hashtable opTable = new Hashtable();

  /**
   *  Mode indicator for the standard operator set, rendered into English
   *  following the standard usage of the operators
   */
  public static final int STANDARD = 0;

  /**
   *  Mode indicator for the set of operators applicable to "priority" types
   *  (which currently include the requisitions attributes of "Priority" and
   *  "IPG").  All of these operators are included in the STANDARD set, but
   *  are rendered differently in this mode.
   */
  public static final int PRIORITY = 1;

  /**
   *  Construct this translator of QRuleOperators into English, presuming the
   *  STANDARD usage mode.
   */
  public QRuleOperatorToEnglish () {
    this(STANDARD);
  }

  /**
   *  Construct this translator of QRuleOperators into English, configured for
   *  the given rendering mode.
   *  @param mode the rendering mode, either STANDARD or PRIORITY
   */
  public QRuleOperatorToEnglish (int mode) {
    if (mode == PRIORITY)
      setupPriorityOpTable();
    else
      setupOperatorTable();
  }

  /**
   *  Add an operator to the table.
   *  @param key the name ("getJessName") of the operator being added
   *  @param value an Object embodying the possible phrasings of the operator
   */
  public void put (String key, OperatorStringizer value) {
    opTable.put(key, value);
  }

  /**
   *  Retrieve English translation info for a named operator from the table.
   *  @param key the jess name of the desired operator
   *  @return an Object encapsulating the English phrasing info
   */
  public OperatorStringizer get (String key) {
    return (OperatorStringizer) opTable.get(key);
  }

  /**
   *  Remove an Operator from the table.  This method is included purely for
   *  the sake of completeness.
   *  @param key the jess name of the operator being removed
   *  @return the object being removed
   */
  public OperatorStringizer remove (String key) {
    return (OperatorStringizer) opTable.remove(key);
  }

  /**
   *  Get the desired English phrasing associated with the named operator.  As
   *  of the moment, only two types are supported:  a verb form suitable for
   *  use in clauses and a verbless form suitable for use in predicates and
   *  prepositional phrases.
   *
   *  @param key the jess name of the operator in question
   *  @param verbForm if true, render this operator as a verb
   */
  public String getOperator (String key, boolean verbForm) {
    OperatorStringizer os = (OperatorStringizer) opTable.get(key);
    String ret = null;
    if (os != null)
      ret = os.map(verbForm);
    if (ret == null)
      return "<<NULL OPERATOR>>";
    else
      return ret;
  }

  /**
   *  Detect whether a given operator requires an indefinite article for its
   *  left-hand operand (in predicate forms)
   *  @param the jess name of the operator in question
   *  @return true if an article is needed, false otherwise.
   */
  public boolean isArticulating (String key) {
    OperatorStringizer os = (OperatorStringizer) opTable.get(key);
    if (os == null)
      return false;
    return os.isArticulating();
  }

  /**
   *  Detect whether a named operator transfers negativity to the phrasing of
   *  the left-hand operand (in predicate forms)
   *  @param the jess name of the operator in question
   *  @return true if the verb in predicate-form accessors should be inverted
   */
  public boolean isVerbNegating (String key) {
    OperatorStringizer os = (OperatorStringizer) opTable.get(key);
    return os.isVerbNegating();
  }

  /**
   *  An inner class to encapsulate the relationship between a QRuleOperator
   *  and the possible English phrases used to represent it.
   */
  private static class OperatorStringizer {
    // the verb form
    private String v;

    // the verbless form
    private String r;

    // a flag indicating whether this operator requires an indefinite article
    // on its left-hand operand (for predicate forms)
    private boolean a = false;

    // a flag indicating whether this operator transfers negativity to the verb
    // rendered as part of the left-hand operand (for predicate forms)
    private boolean v_neg = false;

    /**
     *  Encode the verb and verbless forms of an operator herein, assuming none
     *  of the special cases (indefinite article/negativity transfer) apply.
     *  @param verbForm the verb form of the operator
     *  @param noVerb the verbless form of the operator
     */
    public OperatorStringizer (String verbForm, String noVerb) {
      v = verbForm;
      r = noVerb;
    }

    /**
     *  Encode the verb and verbless forms of an operator, explicitly including
     *  or excluding the special cases.
     *  @param verbForm the verb form of the operator
     *  @param noVerb the verbless form of the operator
     *  @param articulating true if the operator insists on giving its
     *    left-hand (accessor) operand an indefinite article (predicate forms)
     *  @param verbNegating true if the operator transfers negatives to the
     *    phrasing of the left-hand (accessor) operand (predicate forms)
     */
    public OperatorStringizer (
        String verbForm, String noVerb, boolean articulating,
        boolean verbNegating)
    {
      this(verbForm, noVerb);
      a = articulating;
      v_neg = verbNegating;
    }

    /**
     *  Report whether this operator requires its left-hand accessor operand to
     *  have an indefinite article (in predicate forms).
     *  @return true if the indefinite article is required
     */
    public boolean isArticulating () {
      return a;
    }

    /**
     *  Report whether this operator is a negative comparison that transfers
     *  the negativity to the verb rendered as part of the predicate form of
     *  the left-hand accessor
     *  @return true if the operand's verb needs to be negated
     */
    public boolean isVerbNegating () {
      return v_neg;
    }

    /**
     *  Give the requested English phrasing (verb or verbless)
     *  @param verb if true, then return the verb form; otherwise return the
     *    verbless form.
     *  @return the English form of the operator
     */
    public String map (boolean verb) {
      if (verb)
        return v;
      else
        return r;
    }
  }

  // install the operators for standard usage
  private void setupOperatorTable () {
    put("=", new OperatorStringizer("is equal to", "equal to"));
    put(">", new OperatorStringizer("is greater than", "greater than"));
    put(">=", new OperatorStringizer("is greater than or equal to", "greater than or equal to"));
    put("<", new OperatorStringizer("is less than", "less than"));
    put("<=", new OperatorStringizer("is less than or equal to", "less than or equal to"));
    put("eq", new OperatorStringizer("is", ""));
    put("inCustomerGroup", new OperatorStringizer("belongs to", "belonging to"));
    put("includesAdviceCode", new OperatorStringizer("includes", "that includes"));
    put("inRegion", new OperatorStringizer("is in", "in"));
    put("isMember", new OperatorStringizer("is among", "among", true, false));
    put("<>", new OperatorStringizer("is not equal to", "not equal to"));
    put("neq", new OperatorStringizer("is not", "other than", true, false));
    put("inItemGroup", new OperatorStringizer("belongs to", "belongs to"));
    put("startsWith", new OperatorStringizer("starts with", "that starts with"));
    put("endsWith", new OperatorStringizer("ends with", "that ends with"));
    put("notInCustomerGroup", new OperatorStringizer("does not belong to", "not belonging to"));
    put("notIncludesAdviceCode", new OperatorStringizer("does not include", "that does not include"));
    put("notInRegion", new OperatorStringizer("is not in", "not in"));
    put("isNotMember", new OperatorStringizer("is not among", "among", true, true));
    put("notInItemGroup", new OperatorStringizer("does not belong to", "does not belong to"));
    put("dateBefore", new OperatorStringizer("is earlier than", "earlier than"));
    put("dateAfter", new OperatorStringizer("is after", "after"));
    put("dateSameDay", new OperatorStringizer("is the same day as", "same day as"));
  }

  // install the operators on "Priority" types
  private void setupPriorityOpTable () {
    put("=", new OperatorStringizer("is", ""));
    put(">", new OperatorStringizer("is below", "below"));
    put(">=", new OperatorStringizer("is not above", "not above"));
    put("<", new OperatorStringizer("is above", "above"));
    put("<=", new OperatorStringizer("is not below", "not below"));
    put("<>", new OperatorStringizer("is not", "other than"));
  }
}
