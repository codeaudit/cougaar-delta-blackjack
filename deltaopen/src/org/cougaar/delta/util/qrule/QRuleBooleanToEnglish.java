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
 *  class is configured to handle the boolean attributes of LTA and item.  The
 *  public interface consists primarily of the "getOperand" method which takes
 *  an operand and renders an English description in a desired form.  Its other
 *  function, which takes place behind the scenes, is to construct (or, in the
 *  future, <i>retrieve</i>) a table of operand mappings that makes the
 *  conversion to English possible.
 *  <br><br>
 *  One note on the behavior of this class is in order.  Since the only values
 *  of a boolean are true and false, the rendering of the accessor is designed
 *  to incorporate the entire test of which the accessor operand is a part.
 *  This includes the operator, which is presumed to be equality, and the
 *  literal operand whose value, true or false, is manifested as the statement
 *  of the condition in affirmative or negative form, respectively.
 */
public class QRuleBooleanToEnglish {
  /**
   *  Form indicator for the full form of the operand, which is an independent
   *  clause containing a reference to the owner of the attribute.
   */
  public static final int FULL_FORM = 1;

  /**
   *  Form indicator for the predicate form
   */
  public static final int PREDICATE = 2;

  /**
   *  Form indicator for the participle form.  This usually starts with the
   *  present participle of the verb used to construct the predicate form.
   */
  public static final int PARTICIPLE = 3;

  // A table to embody the mapping from operands' internal names to their
  // English translation information
  private Hashtable opTable = new Hashtable();

  /**
   *  Construct this translator of boolean attributes into English.  Most of
   *  the work involved is the construction of a table for mapping the internal
   *  names (as in "getInternalName") of the operands to the information used
   *  to render them as English phrases or clauses.  Currently, the
   *  aforementioned information is coded directly into the class structure,
   *  but at some point in the future, this may be replaced with a database
   *  table.
   */
  public QRuleBooleanToEnglish () {
    setupBooleanAccessorTable();
  }

  /**
   *  Add an accessor of boolean type to the table
   *  @param key the internal name of the accessor
   *  @param value the data used to render the accessor in English
   */
  public void put (String key, BooleanOperandStringizer value) {
    opTable.put(key, value);
  }

  /**
   *  Remove an accessor from the table.  This is included solely for the sake
   *  of completeness.
   *  @param key the internal name of the accessor being removed
   *  @return the object being removed
   */
  public BooleanOperandStringizer remove (String key) {
    return (BooleanOperandStringizer) opTable.remove(key);
  }

  /**
   *  Convert an operand (and associated operator and literal value) into an
   *  English phrase or clause, if supported.  If the operator's name is not
   *  found in the table, return "<<NULL BOOLEAN ACCESSOR>>".
   *  @param key the internal name of the operand being translated
   *  @param form the indicator of the desired form
   *  @param affirmative boolean value being compared to the operand
   *  @return the English rendition of the boolean test
   */
  public String getOperand (String key, int form, boolean affirmative) {
    BooleanOperandStringizer os = (BooleanOperandStringizer) opTable.get(key);
    String ret = null;
    if (os != null)
      ret = os.map(form, affirmative);
    if (ret == null)
      return "<<NULL BOOLEAN ACCESSOR>>";
    else
      return ret;
  }

  /**
   *  An inner class to encapsulate the relationship between a boolean type
   *  QRuleAccessorOperand and various corresponding English phrases.
   */
  public static class BooleanOperandStringizer {
    // An array that stores the words used to make English phrases or clauses
    private String[] forms = new String[6];

    /**
     *  Construct a new English generator for a boolean accessor operand.  The
     *  argument to the constructor is an array of Strings, which contain the
     *  English words used to construct various forms of phrasing for the
     *  corresponding operand.  The indices in the array are:
     *  <ul>
     *    <li>0 - the owner; e.g., "the contract"</li>
     *    <li>1 - the adjective; e.g., "Highly Managed"</li>
     *    <li>2 - the verb, affirmative; e.g., "is"</li>
     *    <li>3 - the verb, negative; e.g., "is not"</li>
     *    <li>4 - the participle, affirmative; e.g., "that is"
     *            (okay, so it's not always a participle)</li>
     *    <li>5 - the participle, negative; e.g., "that is not"</li>
     *  </ul>
     *  @param f the array of Strings of English words
     */
    public BooleanOperandStringizer (String[] f) {
      if (f != null)
        System.arraycopy(f, 0, forms, 0, Math.min(forms.length, f.length));
    }

    // map the form and value indicators into an index for the verb form.
    private int getVerbIndex(int form, boolean affirmative) {
      int n = 2;
      if (form == PARTICIPLE) n += 2;
      if (!affirmative) n ++;
      return n;
    }

    /**
     *  Map the form and value indicators into a suitable English phrase or
     *  clause.
     *  @param form the form indicator (as in the containing class, q.v.)
     *  @param affirmative give the affirmative (negative) form if true (false)
     */
    public String map (int form, boolean affirmative) {
      StringBuffer buf = new StringBuffer();
      if (form == FULL_FORM)
        buf.append(forms[0]);
      int i = getVerbIndex(form, affirmative);
      if (buf.length() > 0 && forms[i].length() > 0)
        buf.append(" ");
      buf.append(forms[i]);
      if (buf.length() > 0 && forms[1].length() > 0)
        buf.append(" ");
      buf.append(forms[1]);
      return buf.toString();
    }
  }

  /*
    This table is used to translate candidate object characteristics into english.
    It currently contains information for the sample accessors used for the SampleLoanCandidate
    example.
  */
  // construct a table for the boolean accessor operands, few though they be
  private void setupBooleanAccessorTable () {
    put("HasRecentDefault", new BooleanOperandStringizer(new String[] {" the customer", "recently defaulted on a loan", "has", "has not", "that is", "that is not"}));

  }
}
