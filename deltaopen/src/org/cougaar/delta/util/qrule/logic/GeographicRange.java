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
 *  A GeographicRange is an AttributeRange specialized for use with attributes
 *  containing geographic regions.  The logic embodied herein relies on the
 *  assumption that within a Geography, the regions are mutually exclusive.
 *  <br><br>
 *  The present implementation presumes that geography-region pairs are
 *  specified as colon-separated Strings, e.g., "United States Two:US West"
 */
public class GeographicRange extends AttributeHolder {
  private Vector inclusions = new Vector();
  private Vector exclusions = new Vector();
  private Vector geographies = new Vector();

  // operators recognized by this AttributeRange
  private String IN = null;
  private String NOT_IN = null;

  /**
   *  Specify the operators used by the system to represent location inside or
   *  outside of a geographical region.
   *  @param in the inside operator
   *  @param not_in the outside operator
   */
  public void setOperatorNames (String in, String not_in) {
    IN = in;
    NOT_IN = not_in;
  }

  /**
   *  Construct an AttributeRange designed to evaluate geographical regions
   */
  public GeographicRange () {
  }

  // called from the constructors--initialize this AttributeRange instance by
  // polling the provided constraints and incorporating them into this logical
  // model
  protected void init () {
    // construct the range and set flags
    for (Enumeration enu = constraints.elements(); enu.hasMoreElements(); ) {
      QRuleComparison q = (QRuleComparison) enu.nextElement();
      String op = q.getOperator().getJessName();
      GeoRegion geoReg = null;
      Object val = ((QRuleLiteralOperand) q.getOperand2()).getValue();
      if (val != null && val instanceof String &&
          (geoReg = new GeoRegion((String) val)).isValid())
      {
        if (IN != null && op.equals(IN))
          noteInclusion(geoReg);
        else if (NOT_IN != null && op.equals(NOT_IN))
          noteExclusion(geoReg);
      }
      else {
        violateDomain = true;
      }
    }
  }

  // utility function--find the members of the provided Vector whose Geography
  // field matches a specified name
  private static Vector getRegionsInGeo (String g, Vector v) {
    Vector ret = new Vector();
    for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
      GeoRegion gr = (GeoRegion) e.nextElement();
      if (gr.geo.equals(g))
        ret.add(gr);
    }
    return ret;
  }

  // record the fact that a condition confines the range to the given region
  private void noteInclusion (GeoRegion gr) {
    Vector sameGeoInc = getRegionsInGeo(gr.geo, inclusions);
    Vector sameGeoExc = getRegionsInGeo(gr.geo, exclusions);

    if (sameGeoInc.contains(gr))
      redundant = true;
    else
      inclusions.add(gr);

    if (sameGeoInc.size() > 0 && !sameGeoInc.contains(gr))
      contradict = true;

    if (sameGeoExc.contains(gr))
      contradict = true;

    if (sameGeoExc.size() > 0 && !sameGeoExc.contains(gr))
      redundant = true;

    if (!geographies.contains(gr.geo))
      geographies.add(gr.geo);
  }

  // record the fact that a condition cordons off the given region
  private void noteExclusion (GeoRegion gr) {
    Vector sameGeoInc = getRegionsInGeo(gr.geo, inclusions);
    Vector sameGeoExc = getRegionsInGeo(gr.geo, exclusions);

    if (sameGeoExc.contains(gr))
      redundant = true;
    else
      exclusions.add(gr);

    if (sameGeoInc.contains(gr))
      contradict = true;

    if (sameGeoInc.size() > 1 ||
        (sameGeoInc.size() == 1 && !sameGeoInc.contains(gr)))
    {
      redundant = true;
    }

    if (!geographies.contains(gr.geo))
      geographies.add(gr.geo);
  }

  // Instances of this class are the result of parsing the colon-separated
  // geography-region pairs into two separate strings.  An "equals" method is
  // provided for Vector operations, and the original String is saved to be
  // returned by the "toString" method.
  private static class GeoRegion {
    private boolean valid = false;
    private String generator = null;

    public String geo = null;
    public String reg = null;

    // construct a geography-region pair from a colon-separated string
    public GeoRegion (String both) {
      generator = both;
      int colon = generator.indexOf(':');
      if (colon != -1) {
        valid = true;
        geo = generator.substring(0, colon);
        reg = generator.substring(colon + 1);
      }
    }

    public boolean isValid () {
      return valid;
    }

    // compare this geography-region pair to another
    public boolean equals (Object o) {
      if (!(o instanceof GeoRegion))
        return false;
      GeoRegion other = (GeoRegion) o;
      return
        ((geo == null && other.geo == null) ||
          (geo != null && other.geo != null && geo.equals(other.geo))) &&
        ((reg == null && other.reg == null) ||
          (reg != null && other.reg != null && reg.equals(other.reg)));
    }

    // return the colon-separated String representation
    public String toString () {
      return generator;
    }
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
    if (!(range instanceof GeographicRange))
      return false;

    GeographicRange r = (GeographicRange) range;

    // this one contains the other if every Geography G referenced herein
    // satisfies one of the following:
    //   1.  The one of G's regions included here is also included in the other
    //   2.  None of G's regions is included here, and the one of G's regions
    //       included in the other is not excluded here
    //   3.  Neither range includes one of G's regions, but all such regions
    //       excluded here are excluded in the other
    for (Enumeration e = geographies.elements(); e.hasMoreElements(); ) {
      String g = (String) e.nextElement();
      Vector ourIncl = getRegionsInGeo(g, inclusions);
      Vector ourExcl = getRegionsInGeo(g, exclusions);
      Vector theirIncl = getRegionsInGeo(g, r.inclusions);
      Vector theirExcl = getRegionsInGeo(g, r.exclusions);
      if (ourIncl.size() > 0) {
        if (!theirIncl.contains(ourIncl.elementAt(0)))
          return false;
      }
      else if (theirIncl.size() > 0) {
        if (ourExcl.contains(theirIncl.elementAt(0)))
          return false;
      }
      else {
        if (!theirExcl.containsAll(ourExcl))
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
    if (!(range instanceof GeographicRange))
      return true;

    GeographicRange r = (GeographicRange) range;

    // this one excludes the other one if it includes a Region excluded in the
    // other (or vice versa) or they each include different Regions within the
    // same Geography.  Once again, contradictions have already been handled.
    for (Enumeration e = geographies.elements(); e.hasMoreElements(); ) {
      String g = (String) e.nextElement();
      Vector incl = getRegionsInGeo(g, inclusions);
      Vector excl = getRegionsInGeo(g, exclusions);
      Vector rIncl = getRegionsInGeo(g, r.inclusions);
      Vector rExcl = getRegionsInGeo(g, r.exclusions);
      if (incl.size() > 0) {
        Object gr = incl.elementAt(0);
        if (rExcl.contains(gr) || (rIncl.size() > 0 && !rIncl.contains(gr)))
          return true;
      }
      else if (rIncl.size() > 0) {
        if (excl.contains(rIncl.elementAt(0)))
          return true;
      }
    }

    return false;
  }

// - - - - - - - Testing Code - - - - - - - - - - - - - - - - - - - - - - - - -

  public static void main (String[] argv) {
    Vector v;

    try {
      GeographicRange r1 = new GeographicRange();
      r1.noteInclusion(new GeoRegion("bla:scissors"));
      // r1.noteInclusion(new GeoRegion("bla:pencil"));
      r1.noteExclusion(new GeoRegion("bla:eraser"));

      System.out.println("Symmary for r1:  ");
      summarize(r1);
      System.out.println();

      GeographicRange r2 = new GeographicRange();
      r2.noteInclusion(new GeoRegion("bla:scissors"));
      r2.noteExclusion(new GeoRegion("bla:pencil"));
      r2.noteExclusion(new GeoRegion("scum:eraser"));

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

  private static void summarize (GeographicRange r) {
    System.out.println("  Range is the intersection of");
    if (r.inclusions.size() == 0)
      System.out.println("    << none >>");
    for (Enumeration e = r.inclusions.elements(); e.hasMoreElements(); )
      System.out.println("    " + e.nextElement());
    System.out.println("  Minus the union of");
    if (r.exclusions.size() == 0)
      System.out.println("    << none >>");
    for (Enumeration e = r.exclusions.elements(); e.hasMoreElements(); )
      System.out.println("    " + e.nextElement());
    System.out.println("  Flags:");
    if (r.hasTautology()) System.out.println("    Tautology");
    if (r.isRedundant()) System.out.println("    Redundancy");
    if (r.isContradictory()) System.out.println("    Contradiction");
    if (r.violatesDomain()) System.out.println("    Domain Violation");
  }
}
