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
 *  This AttributeRange represents the constraints on an integer-valued
 *  attribute.  The standard comparison operators on the integers are all
 *  supported as range constraints.  In addition, integer-valued attributes
 *  with restricted domain sets can be represented by specifying upper and/or
 *  lower bounds on the attribute.
 */
public class IntegerRange extends NumericalRange {
  // Most integer ranges will be the integers belonging to of an interval of
  // real numbers (or a finite union of these).  An instance of Interval
  // represents one such set, which may be unbounded above and/or below.
  private static class Interval {
    public Long min = null;
    public Long max = null;

    public Interval (Long l, Long r) {
      min = l;
      max = r;
    }

    // see if another Interval is contained in this one
    public boolean contains (Interval i) {
      return
        (min == null ||
          (i.min != null && min.longValue() <= i.min.longValue())) &&
        (max == null ||
          (i.max != null && i.max.longValue() <= max.longValue()));
    }

    // see if another Interval does not intersect this one
    public boolean excludes (Interval i) {
      return
        (min != null && i.max != null && min.longValue() > i.max.longValue()) ||
        (max != null && i.min != null && max.longValue() < i.min.longValue());
    }
  }

  // store the intervals, the union of which form the designated range of an
  // Integer attribute
  private Vector intervals = new Vector();

  // store domain boundaries for this attribute, if any
  private long lowerBound = 0;
  private long upperBound = 0;
  private boolean hasLBound = false;
  private boolean hasUBound = false;

  /**
   *  Construct a new AttributeRange for an integer-valued attribute
   */
  public IntegerRange () {
    intervals.add(new Interval(null, null));
  }

  /**
   *  Confine the domain for this integer-valued attribute to those values
   *  between the provided min and max values (inclusive)
   *  @param min the minimum value or null if none
   *  @param max the maximum value or null if none
   */
  public void setDomain (Long min, Long max) {
    if (min != null) {
      lowerBound = min.longValue();
      hasLBound = true;
    }
    if (max != null) {
      upperBound = max.longValue();
      hasUBound = true;
    }
  }

  protected void init () {
    // construct the range and set flags
    for (Enumeration enu = constraints.elements(); enu.hasMoreElements(); ) {
      QRuleComparison q = (QRuleComparison) enu.nextElement();
      String op = q.getOperator().getJessName();
      QRuleLiteralOperand rhs = (QRuleLiteralOperand) q.getOperand2();
      Object val = ((QRuleLiteralOperand) q.getOperand2()).getValue();
      if (val != null && val instanceof Number) {
        long lit = ((Number) val).longValue();
        if (EQUAL != null && op.equals(EQUAL)) {
          noteMin(lit);
          noteMax(lit);
        }
        else if (GREATER_OR_EQUAL != null && op.equals(GREATER_OR_EQUAL))
          noteMin(lit);
        else if (GREATER_THAN != null && op.equals(GREATER_THAN))
          noteMin(lit + 1);
        else if (LESS_OR_EQUAL != null && op.equals(LESS_OR_EQUAL))
          noteMax(lit);
        else if (LESS_THAN != null && op.equals(LESS_THAN))
          noteMax(lit - 1);
        else if (NOT_EQUAL != null && op.equals(NOT_EQUAL))
          noteNotEquals(lit);
      }
      else {
        violateDomain = true;
      }
    }
  }

  // remove one value from this range
  private void noteNotEquals (long n) {
    if ((hasLBound && n < lowerBound) || (hasUBound && n > upperBound))
      violateDomain = true;

    if (contradict) {
      redundant = true;
      return;
    }

    Long max = ((Interval) intervals.elementAt(intervals.size() - 1)).max;
    if (max != null && n > max.longValue())
      redundant = true;

    Vector v = new Vector();
    for (Enumeration e = intervals.elements(); e.hasMoreElements(); ) {
      Interval i = (Interval) e.nextElement();
      if (i.max == null || n <= i.max.longValue()) {
        boolean left_of_it = i.min != null && n < i.min.longValue();
        boolean left_end = i.min != null && n == i.min.longValue();
        boolean right_end = i.max != null && n == i.max.longValue();
        if (left_of_it) {
          redundant = true;
          v.add(i);
        }
        else if (left_end && right_end) {
          // let the interval drop; it has been blotted out
        }
        else if (left_end) {
          i.min = new Long(n + 1);
          v.add(i);
        }
        else if (right_end) {
          i.max = new Long(n - 1);
          v.add(i);
        }
        else {
          v.add(new Interval(i.min, new Long(n - 1)));
          i.min = new Long(n + 1);
          v.add(i);
        }

        while (e.hasMoreElements())
          v.add(e.nextElement());
        break;
      }
      else {
        v.add(i);
      }
    }

    intervals = v;
    if (intervals.size() == 0)
      contradict = true;
  }

  // incorporate a new range minimum
  private void noteMin (long n) {
    if ((hasLBound && n < lowerBound) || (hasUBound && n > upperBound))
      violateDomain = true;

    if (hasLBound && n <= lowerBound)
      tautology = true;

    if (contradict) {
      redundant = true;
      return;
    }

    Interval leftmost = (Interval) intervals.elementAt(0);
    if (leftmost.min != null ||
        (leftmost.max != null && n - 1 > leftmost.max.longValue()))
    {
      redundant = true;
    }

    Vector v = new Vector();
    for (Enumeration e = intervals.elements(); e.hasMoreElements(); ) {
      Interval i = (Interval) e.nextElement();
      if (i.max == null || n <= i.max.longValue()) {
        if (i.min == null || i.min.longValue() < n)
          i.min = new Long(n);
        v.add(i);
        while (e.hasMoreElements())
          v.add(e.nextElement());
        break;
      }
    }

    intervals = v;
    if (intervals.size() == 0)
      contradict = true;
  }

  // incorporate a new range maximum
  private void noteMax (long n) {
    if ((hasLBound && n < lowerBound) || (hasUBound && n > upperBound))
      violateDomain = true;

    if (hasUBound && n >= upperBound)
      tautology = true;

    if (contradict) {
      redundant = true;
      return;
    }

    Interval rightmost = (Interval) intervals.elementAt(intervals.size() - 1);
    if (rightmost.max != null ||
        (rightmost.min != null && n + 1 < rightmost.min.longValue()))
    {
      redundant = true;
    }

    Vector v = new Vector();
    for (Enumeration e = intervals.elements(); e.hasMoreElements(); ) {
      Interval i = (Interval) e.nextElement();
      if (i.max == null || n <= i.max.longValue()) {
        if ((i.min == null || i.min.longValue() <= n) &&
            (i.max == null || n < i.max.longValue()))
        {
          i.max = new Long(n);
          v.add(i);
          break;
        }
      }
      else {
        v.add(i);
      }
    }

    intervals = v;
    if (intervals.size() == 0)
      contradict = true;
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

    if (!(range instanceof IntegerRange))
      return false;

    IntegerRange r = (IntegerRange) range;

    Enumeration ours = intervals.elements();
    Enumeration theirs = r.intervals.elements();

    Interval ourInt = (Interval) ours.nextElement();

    while (theirs.hasMoreElements()) {
      Interval theirInt = (Interval) theirs.nextElement();
      while (!ourInt.contains(theirInt)) {
        if (ours.hasMoreElements())
          ourInt = (Interval) ours.nextElement();
        else
          return false;
      }
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

    if (!(range instanceof IntegerRange))
      return true;

    IntegerRange r = (IntegerRange) range;

    for (Enumeration e = intervals.elements(); e.hasMoreElements(); )
      for (Enumeration f = r.intervals.elements(); f.hasMoreElements(); )
        if (!((Interval) e.nextElement()).excludes(((Interval) f.nextElement())))
          return false;

    return true;
  }

// - - - - - - - Testing Code - - - - - - - - - - - - - - - - - - - - - - - - -

  public static void main (String argv[]) {
    try {
      IntegerRange r1 = new IntegerRange();
      r1.noteMin(4);
      r1.noteMax(4);
      r1.noteNotEquals(1);

      System.out.println("Symmary for r1:  ");
      summarize(r1);
      System.out.println();

      IntegerRange r2 = new IntegerRange();
      r2.noteMin(5);
      r2.noteMax(6);
      r2.noteNotEquals(5);
      r2.noteNotEquals(9);

      System.out.println("Symmary for r2:  ");
      summarize(r2);
      System.out.println();

      System.out.println("checking for r1 containing r2 :  " + r1.contains(r2));
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

  private static void summarize (IntegerRange r) {
    System.out.println("  Domain Lower Bound:  " + r.hasLBound + " (" + r.lowerBound + ")");
    System.out.println("  Domain Upper Bound:  " + r.hasUBound + " (" + r.upperBound + ")");
    System.out.print("  Range values:  ");
    Enumeration e = r.intervals.elements();
    if (e.hasMoreElements()) {
      do {
        Interval i = (Interval) e.nextElement();
        System.out.print("[");
        if (i.min == null)
          System.out.print("-inf");
        else
          System.out.print(i.min);
        System.out.print(",");
        if (i.max == null)
          System.out.print("inf");
        else
          System.out.print(i.max);
        System.out.print("] ");
      } while (e.hasMoreElements());
    }
    else {
      System.out.println("empty");
    }
    System.out.println();
    System.out.println("  Tautology:  " + r.hasTautology());
    System.out.println("  Redundancy:  " + r.isRedundant());
    System.out.println("  Contradiction:  " + r.isContradictory());
    System.out.println("  Domain Violation:  " + r.violatesDomain());
  }
}
