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
 *  translations of QRuleAccessorOperands used in QRules.  Specifically, this
 *  class is configured to handle the non-boolean attributes of LTA, item,
 *  requisition, and proposal.  The public interface consists primarily of the
 *  "getOperand" method which takes an operand and renders an English
 *  description in a desired form.  Its other function, which takes place
 *  behind the scenes, is to construct (or, in the future, <i>retrieve</i>) a
 *  table of operand mappings that makes the conversion to English possible.
 */
public class QRuleAccessorToEnglish {
  /**
   *  Form indicator for the full form, including reference to the object to
   *  which the attribute belongs (e.g., LTA, item, requisition, proposal)
   */
  public static final int FULL_FORM = 1;

  /**
   *  Form indicator for the standard form, usually a noun representing the
   *  attribute.
   */
  public static final int STANDARD = 2;

  /**
   *  Form indicator for the possessive form, indicating ownership by the
   *  appropriate object, the text of which is supplied externally.  This form
   *  usually starts with "whose", and it is not currently used.
   */
  public static final int POSSESSIVE = 3;

  /**
   *  Form indicator for the prepositional phrase form, which, as the name
   *  suggests is usually a prepositional phrase modifying a noun supplied
   *  externally.  It usually starts with "with" or the like and is not
   *  currently used.
   */
  public static final int PREPOSITIONAL = 4;

  /**
   *  Form indicator for the subjectless predicate form.  The phrase usually
   *  starts with a verb appropriate to the attribute and its owner, and it
   *  is assumed that a subject noun is supplied externally.
   */
  public static final int PREDICATE = 5;

  /**
   *  Form indicator for the predicate form with the verb negated, as may be
   *  required by some negative operators.  The form is exactly the same as the
   *  predicate form except that a negative form of the verb is supplied.
   */
  public static final int NEGATIVE_PREDICATE = 6;

  // Store the mapping from accessor names to the English translation data
  private Hashtable opTable = new Hashtable();

  /**
   *  Construct this translator for accessor operands.  The major part of this
   *  task is constructing a table mapping names of the accessors to the data
   *  required for producing the various phrases and clauses used to represent
   *  it.  At this point, the information is coded directly into the class
   *  structure, but at some future time that will probably be replaced by a
   *  database table.
   */
  public QRuleAccessorToEnglish () {
    setupAccessorTable();
  }

  /**
   *  Add an accessor to the table.
   *  @param key the String name of the accessor, as per "getInternalName"
   *  @param value the information used to produce the English representation
   */
  public void put (String key, OperandStringizer value) {
    opTable.put(key, value);
  }

  /**
   *  Remove an accessor from this table.  This operation is included solely
   *  for the sake of completeness.
   *  @param key the name ("getInternalName") of the accessor being removed
   *  @return the object being removed
   */
  public OperandStringizer remove (String key) {
    return (OperandStringizer) opTable.remove(key);
  }

  /**
   *  Render an operand as an English phrase or clause in the requested form.
   *  Naturally, this only applies to accessors that it has in its mapping
   *  table.  The String "<<NULL ACCESSOR>>" is returned if the requested
   *  accessor is not one of those recognized.
   *
   *  @param key the internal name of the operand being so rendered
   *  @param form the indicator for the desired form
   *  @param articulate a flag which, if true, requests an indefinite article
   *     to be placed before the attribute noun.  This may be required in the
   *     predicate form by some operators
   *  @param negate a flag which, if true, requests the negative of the verb to
   *     be used (for predicate forms).  This may be required by some operators.
   *  @return the English description of the given accessor, as a String
   */
  public String getOperand (
      String key, int form, boolean articulate, boolean negate)
  {
//  System.out.println("getOperand "+key);
    OperandStringizer os = (OperandStringizer) opTable.get(key);
    String ret = null;
    if (os != null)
      ret = os.map(form, articulate, negate);
    if (ret == null)
      return "<<NULL ACCESSOR>>";
    else
      return ret;
  }

  /**
   *  An inner class used to store information about a single accessor operand
   *  in a table indexed by the internal names of the accessors for easy
   *  retrieval.
   */
  public static class OperandStringizer {
    // an array to store the Strings used to construct an English phrase
    private String[] forms = new String[7];

    /**
     *  Construct a new record containing English translation data for a
     *  QRuleAccessorOperand not of boolean type.  The argument to the
     *  constructor is an array of Strings which can be assembled into an
     *  English phrase.  Indices in this array correspond to:
     *  <ul>
     *    <li>0 - attribute description text</li>
     *    <li>1 - full-form prefix, usually a possessive form of the owner</li>
     *    <li>2 - standard form prefix, usually a definite article</li>
     *    <li>3 - possessive form prefix, usually "whose"</li>
     *    <li>4 - prepositional phrase prefix, usually "with" or some such</li>
     *    <li>5 - verb (affirmative), for the predicate form</li>
     *    <li>6 - verb (negative), for the predicate form</li>
     *  </ul>
     *  @param f an array of Strings containing English words</li>
     */
    public OperandStringizer (String[] f) {
      if (f != null)
        System.arraycopy(f, 0, forms, 0, Math.min(forms.length, f.length));
    }

    /**
     *  Render an English translation of an accessor operand in the form
     *  requested by the caller.
     *  @param form the form indicator (from the containing class, q.v.)
     *  @param articulate if true, use an indefinite article
     *  @param negate if true, use the negative form of the verb in a predicate
     */
    public String map (int form, boolean articulate, boolean negate) {
      StringBuffer buf = new StringBuffer();

      boolean articulable = false;
      if (form == PREDICATE)
        articulable = true;

      if (negate && form == PREDICATE)
        form = NEGATIVE_PREDICATE;

      buf.append(forms[form]);
      if (articulate && articulable) {
        if (buf.length() > 0)
          buf.append(" ");
        buf.append("a");
      }
      if (buf.length() > 0 && forms[0].length() > 0)
        buf.append(" ");
      buf.append(forms[0]);
      return buf.toString();
    }
  }

  /*
    This table is used to translate candidate object characteristics into english.
    It currently contains information for the sample accessors used for the SampleLoanCandidate
    example.
  */
  // construct the mapping of accessors to English words for this translator
  private void setupAccessorTable() {
//    System.out.println("Setting up accessor table");
    put("LoanAmount", new OperandStringizer(new String[] {"requested loan amount", " the customer's", "the", "whose", "with", "is", "is not"}));
    put("LoanYears", new OperandStringizer(new String[] {"requested loan payment schedule", " the customer's", "the", "whose", "with", "is", "is not"}));
    put("MonthlyPayment", new OperandStringizer(new String[] {"monthly requested loan payment", " the customer's", "the", "whose", "with", "is", "is not"}));
    put("MonthlyIncome", new OperandStringizer(new String[] {"monthly income", " the customer's", "the", "whose", "with", "is", "is not"}));
    put("TotalAssets", new OperandStringizer(new String[] {"total assets", " the customer's", "the", "whose", "with", "is", "is not"}));
    put("LiquidAssets", new OperandStringizer(new String[] {"liquid assets", " the customer's", "the", "whose", "with", "is", "is not"}));
    put("TotalDebt", new OperandStringizer(new String[] {"total debt", " the customer's", "the", "whose", "with", "is", "is not"}));
  }
}
