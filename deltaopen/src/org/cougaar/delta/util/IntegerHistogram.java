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

package org.cougaar.delta.util;

import java.math.*;
import java.util.*;

/**
 *  A class intended to represent a histogram.  The main data element of this
 *  class is an array of Bins, which hold separate counts of the points falling
 *  in their respective domains.
 */
public class IntegerHistogram {
  private Bin[] bins = null;
  private boolean empty;

  /**
   *  Construct an IntegerHistogram with no initial configuration.
   */
  public IntegerHistogram () {
    empty = true;
  }

  /**
   *  Report the number of bins in this histogram
   *  @return the number of bins
   */
  public int size () {
    if (bins == null)
      return 0;
    else
      return bins.length;
  }

  /**
   *  Construct an array of Bins for this histogram according to the given
   *  parameters.  This method automatically constructs the specified number
   *  of Bins so that they are contiguous and of uniform width.
   *  <br><br>
   *  For an even number of Bins, the "center" provided by the caller becomes
   *  the right endpoint of the Bin just left of center (e.g., the fourth Bin
   *  of eight).  For an odd number, it is in the central Bin and is either
   *  the central point (in case of an odd binWidth) or the point just left
   *  of center (in case of an even binWidth).
   *
   *  @param n_bins the number of Bins to be constructed
   *  @param center the integer falling at the center of the histogram's domain
   *  @param binWidth the number of integer values in the domain of each Bin
   */
  public void setEvenlySpacedBins (int n_bins, int center, int binWidth) {
    if (n_bins < 1)
      throw new IllegalArgumentException("Must have a positive number of bins");
    if (binWidth <= 0)
      throw new IllegalArgumentException("Bins must have positive width");
    bins = new Bin[n_bins];
    int halfTotalWidth = (n_bins * binWidth + 1) / 2;
    int binRoot = center - halfTotalWidth;
    for (int i = 0; i < n_bins; i++) {
      bins[i] = new Bin(binRoot + 1, binRoot + binWidth);
      binRoot += binWidth;
    }
    empty = true;
  }

  /**
   *  Construct an array of Bins for this histogram.  This method automatically
   *  assigns a Bin of unit width to each integer value between left_end and
   *  right_end (inclusive).
   *  @param left_end the least integer value counted in this histogram
   *  @param right_end the greatest integer value counted in this histogram
   *  @throws IllegalArgumentException if right_end is left of left_end
   */
  public void setUnitBins (int left_end, int right_end) {
    int n_bins = right_end - left_end + 1;
    if (n_bins < 1)
      throw new IllegalArgumentException("Must have a positive number of bins");
    bins = new Bin[n_bins];
    int bin_value = left_end;
    for (int i = 0; i < n_bins; i++) {
      bins[i] = new Bin(bin_value, bin_value);
      bin_value++;
    }
    empty = true;
  }

  /**
   *  Reset the counters on the Bins in this histogram.  All accumulated data
   *  will be lost.
   */
  public void resetBins () {
    empty = true;
    for (int i = 0; i < bins.length; i++)
      bins[i].resetCount();
  }

  /**
   *  Tally up the points lying in the domains of the respective Bins.  all
   *  the Bins' counters will be adjusted accordingly.
   *  @param points an array of integer values to be counted
   */
  public void countBinHits (int[] points) {
    int[] ret = new int[bins.length];
    for (int i = 0; i < bins.length; i++) {
      ret[i] = bins[i].countHits(points);
    }
  }

  /**
   *  Report whether any points have been added to this histogram.
   */
  public boolean isEmpty () {
    return empty;
  }

  /**
   *  Scale the displayHeights of the Bins proportionally so that the largest
   *  one has the specified height.  In the case where all Bins have zero
   *  counts, all the displayHeights are set to zero.
   *  @param columnHeight the number of pixels of height for the largest bar
   */
  public void scaleHeights (int columnHeight) {
    int max = bins[0].hit_count;
    for (int i = 1; i < bins.length; i++)
      if (bins[i].hit_count > max)
        max = bins[i].hit_count;

    if (max > 0) {
      for (int i = 0; i < bins.length; i++)
        bins[i].setDisplayHeight(bins[i].getCount() * columnHeight / max);
    }
    else {
      for (int i = 0; i < bins.length; i++)
        bins[i].setDisplayHeight(0);
    }
  }

  /**
   *  Report the number of Bins in this histogram
   *  @return the number of Bins
   */
  public int length () {
    return bins.length;
  }

  /**
   *  Get one of the Bins from this histogram by its index in the array
   */
  public Bin getBin (int i) {
    return bins[i];
  }

  /**
   *  Represent one bin in a histogram array.
   */
  public class Bin {
    private int left_endpoint = 0;
    private int right_endpoint = 0;
    private int hit_count = 0;
    private int displayHeight = 0;
    private Vector pointVec = new Vector();

    /**
     *  Increment the hit counter
     */
    public void hit (int point) {
      empty = false;
      hit_count++;
      pointVec.addElement(new Integer(point));
    }

    /**
     *  Find the number of hits since the last reset
     *  @return the hit count
     */
    public int getCount () {
      return hit_count;
    }

    /**
     *  Get a listing of the points added to this bin
     *  @return an Enumeration of integers
     */
    public int[] getPoints () {
      int[] ret = new int[pointVec.size()];
      Enumeration enu = pointVec.elements();
      for (int i = 0; enu.hasMoreElements(); i++)
        ret[i] = ((Integer) enu.nextElement()).intValue();
      return ret;
    }

    /**
     *  Set the hit count back to zero
     */
    public void resetCount () {
      hit_count = 0;
      pointVec = new Vector();
    }

    /**
     *  Report the least integer value considered to be in this bin
     *  @return the left end point
     */
    public int getLeftEnd () {
      return left_endpoint;
    }

    /**
     *  Report the greatest integer value considered to be in this bin
     *  @return the right end point
     */
    public int getRightEnd () {
      return right_endpoint;
    }

    /**
     *  Set the height (in pixels) of the bar to be displayed for this bin in
     *  the graphical display
     *  @param h the height
     */
    public void setDisplayHeight (int h) {
      displayHeight = h;
    }

    /**
     *  Report the height (in pixels) of the bar that should be displayed for
     *  this bin in the graphical representation
     *  @return the height
     */
    public int getDisplayHeight () {
      return displayHeight;
    }

    /**
     *  Construct a Bin with the given left and right endpoints
     *  and an initial hit count.
     *  @param left the left endpoint of this bin
     *  @param right the right endpoint of this bin
     *  @param the initial hit count for this bin
     */
    public Bin (int left, int right, int count) {
      this(left, right);
      hit_count = count;
      if (hit_count > 0)
        empty = false;
    }

    /**
     *  Construct a Bin with the given endpoints and initial
     *  hit count of zero.
     *  @param left the left endpoint
     *  @param right the right endpoint
     */
    public Bin (int left, int right) {
      left_endpoint = left;
      right_endpoint = right;
    }

    /**
     *  Scan the provided array of integer values and tally up the ones that lie
     *  in this bin.
     *  @param points an array of integers
     *  @return the number of points counted since the last reset
     */
    public int countHits (int[] points) {
      for (int i = 0; i < points.length; i++) {
        int point = points[i];
        if (left_endpoint <= point && point <= right_endpoint)
          hit(point);
      }
      return hit_count;
    }

    /**
     *  Go through the provided integer values and tally up those which are
     *  found to lie in this bin.
     *  @param points an Enumeration containing integer values
     *  @return the number of points counted since the last reset
     */
    public int countHits (Enumeration points) {
      while (points.hasMoreElements()) {
        Object obj = points.nextElement();
        int point;
        if (obj instanceof Integer)
          point = ((Integer) obj).intValue();
        else if (obj instanceof Long)
          point = (int) ((Long) obj).longValue();
        else if (obj instanceof BigInteger)
          point = ((BigInteger) obj).intValue();
        else if (obj instanceof BigDecimal)
          point = ((BigDecimal) obj).intValue();
        else
          continue;

        if (left_endpoint <= point && point <= right_endpoint)
          hit(point);
      }
      return hit_count;
    }
  }
}
