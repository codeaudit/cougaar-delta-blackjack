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

/**
 *  DecimalRange is an implementor of AttributeRange designed to represent
 *  ranges of floating point values.  It supports the standard equality and
 *  inequality comparison operators on the real number system for constraining
 *  the range of values.  This class also supports attributes with restricted
 *  domains of definition, though the domain set must be an interval.
 */
public class DecimalRange extends NumericalRange {
  // maintain domain boundaries
  private boolean hasLb = false;
  private boolean attainsLb = false;
  private double lowerBound = 0.0;
  private boolean hasUb = false;
  private boolean attainsUb = false;
  private double upperBound = 0.0;

  // maintain range boundaries
  private boolean hasMin = false;
  private boolean attainsMin = false;
  private double rangeMin = 0.0;
  private boolean hasMax = false;
  private boolean attainsMax = false;
  private double rangeMax = 0.0;

  // keep track of explicitly excluded real values
  private Vector notEquals = new Vector();

  /**
   *  Construct a new AttributeRange for managing a floating point attribute
   */
  public DecimalRange () {
  }

  /**
   *  Confine the values of this attribute to a specified interval.  Values
   *  outside this domain are considered invalid.
   *  @param min the left endpoint of the interval (or null if none)
   *  @param atMin true if the left endpoint (if any) is included in the domain
   *  @param max the right endpoint of the interval (or null if none)
   *  @param atMax true if the right endpoint (if any) is included in the domain
   */
  public void setDomain (Double min, boolean atMin, Double max, boolean atMax) {
    if (min != null) {
      hasLb = true;
      lowerBound = min.doubleValue();
      attainsLb = atMin;
    }
    if (max != null) {
      hasUb = true;
      upperBound = max.doubleValue();
      attainsUb = atMax;
    }
  }

  protected void init () {
    // construct the range and set flags
    for (Enumeration enu = constraints.elements(); enu.hasMoreElements(); ) {
      QRuleComparison q = (QRuleComparison) enu.nextElement();
      String op = q.getOperator().getJessName();
      Object val = ((QRuleLiteralOperand) q.getOperand2()).getValue();
      if (val != null && val instanceof Number) {
        double lit = ((Number) val).doubleValue();
        if (EQUAL != null && op.equals(EQUAL)) {
          noteMin(lit, true);
          noteMax(lit, true);
        }
        else if (GREATER_OR_EQUAL != null && op.equals(GREATER_OR_EQUAL)) {
          noteMin(lit, true);
        }
        else if (GREATER_THAN != null && op.equals(GREATER_THAN)) {
          noteMin(lit, false);
        }
        else if (LESS_OR_EQUAL != null && op.equals(LESS_OR_EQUAL)) {
          noteMax(lit, true);
        }
        else if (LESS_THAN != null && op.equals(LESS_THAN)) {
          noteMax(lit, false);
        }
        else if (NOT_EQUAL != null && op.equals(NOT_EQUAL)) {
          noteNotEquals(lit);
        }
      }
      else {
        violateDomain = true;
      }
    }
  }

  // explicitly remove a given value from the range
  private void noteNotEquals (double n) {
    if ((hasLb && (n < lowerBound || (!attainsLb && n == lowerBound))) ||
        (hasUb && (n > upperBound || (!attainsUb && n == upperBound))))
    {
      violateDomain = true;
    }

    if ((hasMin && (n < rangeMin || (!attainsMin && n == rangeMin))) ||
        (hasMax && (n > rangeMax || (!attainsMax && n == rangeMax))))
    {
      redundant = true;
    }
    else if ((!hasMin || rangeMin < n) && (!hasMax || rangeMax > n)) {
      Double N = new Double(n);
      if (notEquals.contains(N))
        redundant = true;
      else
        notEquals.add(N);
    }
    else {
      if (hasMin && attainsMin && n == rangeMin)
        attainsMin = false;
      if (hasMax && attainsMax && n == rangeMax)
        attainsMax = false;
      if (hasMin && hasMax && !(attainsMin && attainsMax) &&
          rangeMin == rangeMax)
      {
        contradict = true;
      }
    }
  }

  private void noteMin (double n, boolean closed) {
    if ((hasLb && (n < lowerBound ||
          (n == lowerBound && !attainsLb && closed))) ||
        (hasUb && (n > upperBound ||
          (n == upperBound && !attainsUb && closed))))
    {
      violateDomain = true;
    }

    if (hasMin) {
      redundant = true;
      if (n == rangeMin) {
        attainsMin = attainsMin && closed;
      }
      else if (n > rangeMin) {
        rangeMin = n;
        attainsMin = closed;
      }
    }
    else {
      hasMin = true;
      rangeMin = n;
      attainsMin = closed;
    }

    // check for now-redundant exclusions and contradictions
    for (Enumeration e = notEquals.elements(); e.hasMoreElements(); ) {
      double neVal = ((Double) e.nextElement()).doubleValue();
      if (neVal < rangeMin || (!attainsMin && neVal == rangeMin))
        redundant = true;
      if (hasMax && rangeMax == rangeMin && rangeMin == neVal)
        contradict = true;
    }
    // check to see if the range is now empty
    if (hasMax &&
        (rangeMin > rangeMax ||
          (rangeMin == rangeMax && !(attainsMax && attainsMin))))
    {
      contradict = true;
    }
    // check to see if the new condition is a tautology
    if (hasLb &&
        (rangeMin < lowerBound ||
          (rangeMin == lowerBound && (attainsMin || !attainsLb))))
    {
      tautology = true;
    }
  }

  private void noteMax (double n, boolean closed) {
    if ((hasLb && (n < lowerBound ||
          (n == lowerBound && !attainsLb && closed))) ||
        (hasUb && (n > upperBound ||
          (n == upperBound && !attainsUb && closed))))
    {
      violateDomain = true;
    }

    if (hasMax) {
      redundant = true;
      if (n == rangeMax) {
        attainsMax = attainsMax && closed;
      }
      else if (n < rangeMax) {
        rangeMax = n;
        attainsMax = closed;
      }
    }
    else {
      hasMax = true;
      rangeMax = n;
      attainsMax = closed;
    }

    // check for now-redundant exclusions and contradictions
    for (Enumeration e = notEquals.elements(); e.hasMoreElements(); ) {
      double neVal = ((Double) e.nextElement()).doubleValue();
      if (neVal > rangeMax || (!attainsMax && neVal == rangeMax))
        redundant = true;
      if (hasMin && rangeMin == rangeMax && rangeMax == neVal)
        contradict = true;
    }
    // check to see if the range is now empty
    if (hasMin &&
        (rangeMin > rangeMax ||
          (rangeMax == rangeMin && (!attainsMax || !attainsMin))))
    {
      contradict = true;
    }
    // check to see if the new condition is a tautology
    if (hasUb &&
        (rangeMax > upperBound ||
          (rangeMax == upperBound && (attainsMax || !attainsUb))))
    {
      tautology = true;
    }
  }

  private boolean containsValue (double n) {
    return
      (!hasMin || n > rangeMin || (attainsMin && n == rangeMin)) &&
      (!hasMax || n < rangeMax || (attainsMax && n == rangeMax)) &&
      !notEquals.contains(new Double(n));
  }

  /**
   *  Compare the range of values satisfying these conditions with another
   *  similar range, and return true if and only if the other range is a subset
   *  of this one.
   *
   *  @param range the other AttributeRange being compared to this one
   *  @return true if and only if the other range is a subset of this one
   */
  public boolean contains (AttributeRange range) {
    if (range.isContradictory())
      return true;
    if (contradict)
      return false;

    if (!(range instanceof DecimalRange))
      return false;

    DecimalRange r = (DecimalRange) range;

    // check for holes
    for (Enumeration e = notEquals.elements(); e.hasMoreElements(); ) {
      if (r.containsValue(((Number) e.nextElement()).doubleValue()))
        return false;
    }

    // check for the left edge
    if (hasMin &&
        (!r.hasMin ||
          (rangeMin > r.rangeMin) ||
          (rangeMin == r.rangeMin &&
            !attainsMin &&
            r.containsValue(rangeMin))))
    {
      return false;
    }

    // check for the right edge
    if (hasMax &&
        (!r.hasMax ||
          (rangeMax < r.rangeMax) ||
          (rangeMax == r.rangeMax &&
            !attainsMax &&
            r.containsValue(rangeMax))))
    {
      return false;
    }

    return true;
  }

  /**
   *  Compare the range of values satisfying these conditions with another
   *  similar range, and return true if and only if the other range does not
   *  intersect this one.
   *  @param range the other AttributeRange being compared to this one
   *  @return true if and only if the other range does not intersect this one
   */
  public boolean excludes (AttributeRange range) {
    if (contradict || range.isContradictory())
      return true;
    if (!(range instanceof DecimalRange))
      return true;
    DecimalRange r = (DecimalRange) range;

    // find the intersection range
    double commonMin = 0;
    double commonMax = 0;

    if (hasMin) {
      if (r.hasMin)
        commonMin = Math.max(rangeMin, r.rangeMin);
      else
        commonMin = rangeMin;
    }
    else if (r.hasMin) {
      commonMin = r.rangeMin;
    }
    else {
      // if neither set is bounded below, the intersection must be nonempty
      return false;
    }

    if (hasMax) {
      if (r.hasMax)
        commonMax = Math.min(rangeMax, r.rangeMax);
      else
        commonMax = rangeMax;
    }
    else if (r.hasMax) {
      commonMax = r.rangeMax;
    }
    else {
      // if neither set is bounded above, the intersection must be nonempty
      return false;
    }

    // check for empty intersection
    return
      commonMin > commonMax ||
      (commonMin == commonMax &&
        (!containsValue(commonMin) || !r.containsValue(commonMin)));
  }

// - - - - - - - Testing Code - - - - - - - - - - - - - - - - - - - - - - - - -

  public static void main (String argv[]) {
    try {
      DecimalRange r1 = new DecimalRange();
      r1.noteMax(9.0, true);
      r1.noteMin(9.0, true);
      r1.noteMax(9.0, true);
      r1.noteMin(9.0, false);

      System.out.println("Symmary for r1:  ");
      summarize(r1);
      System.out.println();

      DecimalRange r2 = new DecimalRange();
      r2.noteMax(6.0, false);
      r2.noteNotEquals(3.0);

      System.out.println("Symmary for r2:  ");
      summarize(r2);
      System.out.println();

      System.out.println("checking for r1 containing r2 :  " + r1.contains(r2));
      System.out.println("checking for r2 containing r1 :  " + r2.contains(r1));
      System.out.println("checking for r1 excluding r2 :  " + r1.excludes(r2));
      System.out.println("checking for r2 excluding r1 :  " + r2.excludes(r1));

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

  private static void summarize (DecimalRange r) {
    System.out.println("  Domain Lower Bound:  " + r.hasLb + " (" + r.lowerBound + (r.attainsLb ? " attained" : "") + ")");
    System.out.println("  Domain Upper Bound:  " + r.hasUb + " (" + r.upperBound + (r.attainsUb ? " attained" : "") + ")");
    System.out.println("  Range Minimum:  " + r.hasMin + " (" + r.rangeMin + (r.attainsMin ? " attained" : "") + ")");
    System.out.println("  Range Maximum:  " + r.hasMax + " (" + r.rangeMax + (r.attainsMax ? " attained" : "") + ")");
    System.out.print("  Range Exclusions: ");
    for (Enumeration e = r.notEquals.elements(); e.hasMoreElements(); )
      System.out.print(" " + e.nextElement());
    System.out.println();
    System.out.println("  Tautology:  " + r.hasTautology());
    System.out.println("  Redundancy:  " + r.isRedundant());
    System.out.println("  Contradiction:  " + r.isContradictory());
    System.out.println("  Domain Violation:  " + r.violatesDomain());
  }
}
