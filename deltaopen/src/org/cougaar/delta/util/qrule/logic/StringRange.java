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

public class StringRange extends AttributeHolder {
  private Vector inclusions = null;
  private Vector exclusions = null;

  private int lengthMax = 0;
  private boolean hasLengthMax = false;
  private int lengthMin = 0;
  private boolean hasLengthMin = false;

  private String charSet = null;
  private boolean invertedCharSet = false;
  private String initialSet = null;
  private boolean invertedInitials = false;

  // operators recognized by this AttributeRange
  private String EQUALS = null;
  private String NOT_EQUALS = null;
  private String IN = null;
  private String NOT_IN = null;

  /**
   *  Specify the names of the operators used in the system to represent
   *  String equality/inequality and membership/nonmembership in a literal
   *  set of Strings.
   *  @param eq the equality operator
   *  @param neq the inequality operator
   *  @param in the membership operator
   *  @param not_in the nonmembership operator
   */
  public void setOperatorNames (
      String eq, String neq, String in, String not_in)
  {
    EQUALS = eq;
    NOT_EQUALS = neq;
    IN = in;
    NOT_IN = not_in;
  }

  /**
   *  Construct a new AttributeRange for an integer-valued attribute
   */
  public StringRange () {
  }

  /**
   *  Configure this StringRange so that not all String values are permitted.
   *  Here, a restricted domain is defined by its permitted character sets.
   *  @param initials a String containing the characters allowed to be first
   *  @param invInit a flag indicating, if true, that the initials set is inverted
   *  @param chars a String containing the characters permitted after the first
   *  @param invChars a flag indicating, if true, that the chars set is inverted
   */
  public void setDomain (
      String initials, boolean invInit, String chars, boolean invChars)
  {
    initialSet = initials;
    invertedInitials = invInit;
    charSet = chars;
    invertedCharSet = invChars;
  }

  /**
   *  Initialize this AttributeRange instance by polling the provided
   *  constraints and incorporating them into a logical model of a set of
   *  Strings
   *  @param a the attribute whose values are being considered
   *  @param c a Vector containing the constraints on the attribute's value
   */
  protected void init () {
    // construct the range and set flags
    for (Enumeration enu = constraints.elements(); enu.hasMoreElements(); ) {
      QRuleComparison q = (QRuleComparison) enu.nextElement();
      String op = q.getOperator().getJessName();
      Object val = ((QRuleLiteralOperand) q.getOperand2()).getValue();
      if (EQUALS != null && op.equals(EQUALS)) {
        if (val instanceof String)
          noteInclusion((String) val);
        else
          violateDomain = true;
      }
      else if (NOT_EQUALS != null && op.equals(NOT_EQUALS)) {
        if (val instanceof String)
          noteExclusion((String) val);
        else
          violateDomain = true;
      }
      else if (IN != null && op.equals(IN)) {
        if (val instanceof Vector)
          noteInclusion((Vector) val);
        else
          violateDomain = true;
      }
      else if (NOT_IN != null && op.equals(NOT_IN)) {
        if (val instanceof Vector)
          noteExclusion((Vector) val);
        else
          violateDomain = true;
      }
    }
  }

  // Sift through the strings in the Vector and find whether they all conform
  // to the domain restrictions in effect for this AttributeRange.  If not, the
  // domain violation flag is raised.
  private void checkDomain (Vector v) {
    if (violateDomain)
      return;
    for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
      Object val = e.nextElement();
      if (!(val instanceof String)) {
        violateDomain = true;
        return;
      }

      String element = (String) val;
      // TO DO:  check for length restrictions

      if (initialSet != null && element.length() > 0 &&
          (invertedInitials ^ (initialSet.indexOf(element.charAt(0)) == -1)))
      {
        violateDomain = true;
        return;
      }

      if (charSet != null) {
        String body;
        if (element.length() == 0 || initialSet == null)
          body = element;
        else
          body = element.substring(1);
        for (int i = 0; i < body.length(); i++)
          if (invertedCharSet ^ (charSet.indexOf(body.charAt(i)) == -1)) {
            violateDomain = true;
            return;
          }
      }
    }
  }

  private void noteInclusion (String s) {
    Vector v = new Vector();
    v.add(s);
    noteInclusion(v);
  }

  // note that one constraint asserts that the attribute must take its value
  // from among the given set of strings
  private void noteInclusion (Vector v) {
    checkDomain(v);

    if (contradict)
      return;

    if (v.size() == 0) {
      contradict = true;
      exclusions = null;
      if (inclusions == null)
        inclusions = new Vector();
      else
        inclusions.clear();
    }
    else if (inclusions != null) {
      redundant = true;
      Vector newInclusions = new Vector();
      for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
        String element = e.nextElement().toString();
        if (inclusions.contains(element))
          newInclusions.add(element);
      }
      inclusions = newInclusions;
      if (inclusions.size() == 0)
        contradict = true;
    }
    else if (exclusions != null) {
      redundant = true;
      inclusions = (Vector) v.clone();
      inclusions.removeAll(exclusions);
      exclusions = null;
      if (inclusions.size() == 0)
        contradict = true;
    }
    else {
      inclusions = new Vector();
      for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
        String element = e.nextElement().toString();
        if (inclusions.contains(element))
          redundant = true;
        else
          inclusions.add(element);
      }
    }
  }

  private void noteExclusion (String s) {
    Vector v = new Vector();
    v.add(s);
    noteExclusion(v);
  }

  // note that one constraing asserts that the attribute may not take its value
  // from among the given set of Strings
  private void noteExclusion (Vector v) {
    checkDomain(v);
    if (v.size() == 0)
      tautology = true;
    if (contradict)
      return;
    if (inclusions != null) {
      redundant = true;
      inclusions.removeAll(v);
      if (inclusions.size() == 0)
        contradict = true;
    }
    else {
      if (exclusions == null)
        exclusions = new Vector();
      for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
        String element = e.nextElement().toString();
        if (!exclusions.contains(element))
          exclusions.add(element);
        else
          redundant = true;
      }
    }
  }

  private boolean containsString (String s) {
    return
      (inclusions == null && exclusions == null) ||
      (inclusions != null && inclusions.contains(s)) ||
      (exclusions != null && !exclusions.contains(s));
  }

  /**
   *  Compare the range of values satisfying these conditions with another
   *  similar range, and return true if and only if the other range is a subset
   *  of this one.
   *  @param range the other AttributeRange being compared to this one
   *  @return true if and only if the other range is a subset of this one
   */
  public boolean contains (AttributeRange range) {
    if (range.isContradictory())
      return true;
    if (contradict)
      return false;
    if (!(range instanceof StringRange))
      return false;

    // eliminate the case of no constraints
    if (inclusions == null && exclusions == null)
      return true;

    StringRange r = (StringRange) range;
    if (r.inclusions != null) {
      for (Enumeration e = r.inclusions.elements(); e.hasMoreElements(); )
        if (!containsString(e.nextElement().toString()))
          return false;
      return true;
    }
    else if (r.exclusions != null) {
      // if exclusions is null, then this is a finite set; hence, it can't
      // contain an infinite set
      if (exclusions == null)
        return false;
      for (Enumeration e = exclusions.elements(); e.hasMoreElements(); )
        if (r.containsString(e.nextElement().toString()))
          return false;
      return true;
    }
    else {
      return false;
    }
  }

  /**
   *  Compare the range of values satisfying these conditions with another
   *  similar range, and return true if and only if the other range does not
   *  intersect this one.
   *  @param range the other AttributeRange being compared to this one
   *  @return true if and only if the other range does not intersect this one
   */
  public boolean excludes (AttributeRange range) {
    if (range.isContradictory() || contradict)
      return true;
    if (!(range instanceof StringRange))
      return true;

    // eliminate the case of no constraints
    if (inclusions == null && exclusions == null)
      return false;

    StringRange r = (StringRange) range;
    if (r.inclusions != null) {
      for (Enumeration e = r.inclusions.elements(); e.hasMoreElements(); )
        if (containsString(e.nextElement().toString()))
          return false;
      return true;
    }
    else if (r.exclusions != null) {
      // two co-finite sets cannot be mutually exclusive
      if (inclusions == null)
        return false;
      for (Enumeration e = inclusions.elements(); e.hasMoreElements(); )
        if (r.containsString(e.nextElement().toString()))
          return false;
      return true;
    }
    else {
      return false;
    }
  }

// - - - - - - - Testing Code - - - - - - - - - - - - - - - - - - - - - - - - -

  private static final String lc = "abcdefghijklmnopqrstuvwxyz";
  private static final String cap = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String num = "0123456789";

  public static void main (String[] argv) {
    Vector v;

    try {
      StringRange r1 = new StringRange();
      r1.setDomain(cap, false, lc, false);
      v = new Vector();
      v.add("Bla");
      // v.add("Yurgh");
      v.add("Scum");
      r1.noteInclusion(v);
      v.add("Stuff");
      r1.noteInclusion(v);

      System.out.println("Symmary for r1:  ");
      summarize(r1);
      System.out.println();

      StringRange r2 = new StringRange();
      r2.setDomain(cap, false, lc, false);
      v = new Vector();
      v.add("Bla");
      v.add("Scum");
      v.add("cracksmoker");
      r2.noteExclusion(v);
      v = new Vector();
      v.add("Yurgh");
      v.add("Bla");
      r2.noteInclusion(v);

      System.out.println("Symmary for r2:  ");
      summarize(r2);
      System.out.println();

      System.out.println("checking for r1 containing r2 :  " + r1.contains(r2));
      System.out.println("checking for r2 containing r1 :  " + r2.contains(r1));
      System.out.println("checking for r1 excluding r2 :  " + r1.excludes(r2));

      System.in.read(new byte[2]);
    }
    catch (Exception ioe) {
      ioe.printStackTrace();
    }
    finally {
      /*
      try {
      Thread.sleep(5000);
      }catch(Exception bs){}
      */
    }
  }

  private static void summarize (StringRange r) {
    Vector v = (r.inclusions == null ? r.exclusions : r.inclusions);
    if (v == null)
      System.out.println("  Range is all-inclusive");
    else if (r.inclusions == null)
      System.out.println("  Range does not include:");
    else
      System.out.println("  Range includes:");
    if (v != null)
      for (Enumeration e = v.elements(); e.hasMoreElements(); )
        System.out.println("    " + e.nextElement());
    System.out.println("  Tautology:  " + r.hasTautology());
    System.out.println("  Redundancy:  " + r.isRedundant());
    System.out.println("  Contradiction:  " + r.isContradictory());
    System.out.println("  Domain Violation:  " + r.violatesDomain());
  }
}
