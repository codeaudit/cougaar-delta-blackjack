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
 *  An AttributeRange implementor for Date-valued attributes.  In the context
 *  of a Date-valued attribute, two dates on the same day are considered equal.
 *  The GregorianCalendar class is used for doing date-arithmetic.
 */
public class DateRange extends AttributeHolder {
  private GregorianCalendar lateBound = null;
  private GregorianCalendar earlyBound = null;

  // operators recognized by this AttributeRange
  private String SAME_DAY = null;
  private String AFTER_DAY = null;
  private String BEFORE_DAY = null;

  /**
   *  Specify the values being used by the system to represent operators that
   *  relate two Dates.
   *  @param same the same day operator
   *  @param after the after-that-day operator
   *  @param before the before-that-day operator
   */
  public void setOperatorNames (String same, String after, String before) {
    SAME_DAY = same;
    AFTER_DAY = after;
    BEFORE_DAY = before;
  }

  /**
   *  Create a new AttributeRange capable of handling Date-valued fields.
   */
  public DateRange () {
  }

  protected void init () {
    // construct the range and set flags
    for (Enumeration enu = constraints.elements(); enu.hasMoreElements(); ) {
      QRuleComparison q = (QRuleComparison) enu.nextElement();
      String op = q.getOperator().getJessName();
      Object val = ((QRuleLiteralOperand) q.getOperand2()).getValue();
      if (val != null && val instanceof Date) {
        GregorianCalendar lit = calendarize((Date) val);
        if (SAME_DAY != null && op.equals(SAME_DAY)) {
          noteEarlyBound(lit);
          noteLateBound(lit);
        }
        else if (AFTER_DAY != null && op.equals(AFTER_DAY)) {
          lit.roll(Calendar.DATE, 1);
          noteEarlyBound(lit);
        }
        else if (BEFORE_DAY != null && op.equals(BEFORE_DAY)) {
          lit.roll(Calendar.DATE, -1);
          noteLateBound(lit);
        }
      }
      else {
        violateDomain = true;
      }
    }
  }

  // must be on or after the given day
  private void noteEarlyBound (GregorianCalendar day) {
    GregorianCalendar d = (GregorianCalendar) day.clone();

    if (earlyBound != null) {
      redundant = true;
      if (d.after(earlyBound)) {
        earlyBound = d;
        if (lateBound != null && !earlyBound.before(lateBound))
          contradict = true;
      }
    }
    else {
      earlyBound = d;
    }
  }

  // must be on or before the given day
  private void noteLateBound (GregorianCalendar day) {
    GregorianCalendar d = (GregorianCalendar) day.clone();
    d.roll(Calendar.DATE, 1);

    if (lateBound != null) {
      redundant = true;
      if (d.before(lateBound)) {
        lateBound = d;
        if (earlyBound != null && !earlyBound.before(lateBound))
          contradict = true;
      }
    }
    else {
      lateBound = d;
    }
  }

  private static GregorianCalendar calendarize (Date d) {
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(d);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    return c;
  }

  public boolean contains (AttributeRange range) {
    if (range.isContradictory())
      return true;
    if (contradict)
      return false;
    if (!(range instanceof DateRange))
      return false;

    DateRange r = (DateRange) range;

    if (earlyBound != null) {
      GregorianCalendar begin = (GregorianCalendar) earlyBound.clone();
      begin.roll(Calendar.MILLISECOND, -1);
      if (r.earlyBound == null || !r.earlyBound.after(begin))
        return false;
    }

    if (lateBound != null) {
      GregorianCalendar end = (GregorianCalendar) lateBound.clone();
      end.roll(Calendar.MILLISECOND, 1);
      if (r.lateBound == null || !r.lateBound.before(end))
        return false;
    }

    return true;
  }

  public boolean excludes (AttributeRange range) {
    if (contradict || range.isContradictory())
      return true;
    if (!(range instanceof DateRange))
      return true;

    DateRange r = (DateRange) range;

    return
      (earlyBound != null &&
        r.lateBound != null &&
        !earlyBound.before(r.lateBound)) ||
      (lateBound != null &&
        r.earlyBound != null &&
        !lateBound.after(r.earlyBound));
  }
}
