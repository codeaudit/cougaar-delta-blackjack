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
package org.cougaar.delta.applet.ruleeditor;

import javax.swing.*;
import java.util.*;
import java.awt.*;

/**
 *  This type of layout manager is useful for laying out elements in a
 *  horizontal row.  Elements are divided into three categories, which are
 *  respectively left justified, centered, and right justified.  One or more of
 *  these categories may be empty.  All of the elements in the row are given
 *  their preferred width and a uniform height, which is either the largest of
 *  their preferred heights or the container height (in the case of STRETCH).
 *  <br><br>
 *  Note that when a RowLayout is created, it is bound to a particular target
 *  container.  Child elements should be added to the RowLayout and <em>NOT</em>
 *  to the target itself.  The RowLayout automatically adds children to (or
 *  removes them from) the target container when they are added to (or removed
 *  from) the RowLayout.
 */
public class RowLayout implements LayoutManager {
  // spacing parameters
  private int hDivider = 20;
  private int hSpace = 4;
  private int topSpace = 0;
  private int bottomSpace = 0;
  private int leftSpace = 0;
  private int rightSpace = 0;

  // the target container
  private Container target = null;

  /**
   *  Specify the amount of empty space, in pixels, to be inserted between
   *  adjacent components in the same region.
   *  @param s the horizontal spacing
   */
  public void setHorizontalSpace (int s) {
    hSpace = s;
  }

  /**
   *  Specify the minimal amount of empty space to be inserted between separate
   *  regions (e.g. left, center, and right).  The gap between the left and
   *  center components, for instance, depends on the size of the container and
   *  the components being laid out, but it will never be less than the set
   *  minimum.
   *  @param s the horizontal spacing between adjacent groups of elements
   */
  public void setDivisionSpace (int s) {
    hDivider = s;
  }

  /**
   *  Specify the amount of empty space inserted at the top of the container.
   *  @param s the top margin
   */
  public void setTopMargin (int s) {
    topSpace = s;
  }

  /**
   *  Specify the amount of empty space desired at the bottom of the container.
   *  If the height of the container is set to an amount less than its natural
   *  height, then components may protrude into this space.
   *  @param s the bottom margin
   */
  public void setBottomMargin (int s) {
    bottomSpace = s;
  }

  /**
   *  Specify the amount of empty space to be found on the left side of the
   *  container.
   *  @param s the left margin
   */
  public void setLeftMargin (int s) {
    leftSpace = s;
  }

  /**
   *  Specify the amount of empty space desired along the left edge of the
   *  container.  If the actual width of the container is set to an amount less
   *  than its natural width, then the laid out components may protrude into
   *  this space.
   */
  public void setRightMargin (int s) {
    rightSpace = s;
  }

  /**
   *  This is a convenience method for simultaneously setting all of the layout
   *  spacing parameters, namely the top, bottom, left, and right margins, the
   *  inter-component spacing, and the inter-group spacing.
   *
   *  @param t the new top margin
   *  @param b the new bottom margin
   *  @param l the new left margin
   *  @param r the new right margin
   *  @param h the new inter-component spacing
   *  @param d the new minimal spacing between divisions
   */
  public void setSpaceParameters (int t, int b, int l, int r, int h, int d) {
    topSpace = t;
    bottomSpace = b;
    leftSpace = l;
    rightSpace = r;
    hSpace = h;
    hDivider = d;
  }

  /**
   *  TOP is a constant used to indicate that elements should be aligned along
   *  the top of the container, if it is larger than its natural height.
   */
  public static final int TOP = 1;

  /**
   *  BOTTOM is a constant used to indicate that elements should be aligned
   *  along the bottom of the container if it is larger than its natural height.
   */
  public static final int BOTTOM = 2;

  /**
   *  CENTER is a constant used to indicate that elements should be centered
   *  vertically within the container if it is larger than its natural height.
   */
  public static final int CENTER = 3;

  /**
   *  STRETCH is a constant used to indicate that elements should be extended
   *  (or compressed) to fill all available vertical space in the container.
   */
  public static final int STRETCH = 4;

  // the vertical justification mode currently in effect
  private int vMode = 1;

  /**
   *  Specify the type of vertical justification to be used by this layout
   *  manager.  Only the values of TOP, BOTTOM, CENTER, and STRETCH are
   *  permitted as arguments to this method.  Any other is ignored.
   */
  public void setVerticalMode (int m) {
    if (1 <= m && m <= 4)
      vMode = m;
  }

  // vectors for the three categories of elements
  private Vector left = new Vector();
  private Vector center = new Vector();
  private Vector right = new Vector();
  private Vector[] all = new Vector[] {left, center, right};

  /**
   *  Add a new element to be laid out on the left side of the container.  The
   *  new component will be laid out to the right of other left side components
   *  and left of any center or right side components.
   *  @param c the component to be laid out on the left side
   */
  public void addLeft (Component c) {
    left.addElement(c);
    target.add(c);
  }

  /**
   *  Insert a new element among those laid out on the left side of the
   *  container.  Its position is the <it>n</it>th among those on the left.
   *  @param c the new component
   *  @param n the new component's position among the other left side elements
   */
  public void insertLeft (Component c, int n) {
    left.insertElementAt(c, n);
    target.add(c);
  }

  /**
   *  Add a new element to be laid out in the center of the container.  The new
   *  component will be laid out to the right of left side and any existing
   *  center elements but left of any right side components.
   *  @param c the component to be laid out in the center
   */
  public void addCenter (Component c) {
    center.addElement(c);
    target.add(c);
  }

  /**
   *  Insert a new element among those laid out in the center of the container.
   *  The position among the center components (counting from the leftmost
   *  among them) is <it>n</it>th.
   *  @param c the new component
   *  @param n the new component's position among the other center elements
   */
  public void insertCenter (Component c, int n) {
    center.insertElementAt(c, n);
    target.add(c);
  }

  /**
   *  Add a new element to be laid out on the right side of the container.  The
   *  new component will be laid out to the right of all others.
   *  @param c the component to be laid out on the right side
   */
  public void addRight (Component c) {
    right.addElement(c);
    target.add(c);
  }

  /**
   *  Insert a new element among those being laid out on the right side of the
   *  container.  The new element is inserted in the <it>n</it>th position
   *  among the right side elements (counting from the leftmost among them).
   *  @param c the new component
   *  @param n the new component's position among the other right side elements
   */
  public void insertRight (Component c, int n) {
    right.insertElementAt(c, n);
    target.add(c);
  }

  /**
   *  Construct a RowLayout to manage the children of the specified container.
   *  @param c the target container.
   */
  public RowLayout (Container c) {
    target = c;
    c.setLayout(this);
  }

  /**
   *  Construct a RowLayout to manage the children of the specified container.
   *  In case the container's height is different from its natural height, the
   *  provided vertical justification mode is used.
   *  @param c the target container
   *  @param m the vertical justification mode
   */
  public RowLayout (Container c, int m) {
    this(c);
    setVerticalMode(m);
  }

  /**
   *  Construct a RowLayout for the given container with the vertical
   *  justification and horizontal spacing parameters as provided.
   *  @param c the target container
   *  @param m the vertical justification mode
   *  @param h the horizontal spacing between adjacent elements
   *  @param d the minimal horizontal spacing between adjacent groups
   */
  public RowLayout (Container c, int m, int h, int d) {
    this(c, m);
    hSpace = h;
    hDivider = d;
  }

  /**
   *  This is called by the AWT infrastructure to notify the layout manager
   *  of a component being added to the container.  Elements should be added to
   *  the target container only by the RowLayout itself, so nothing needs to be
   *  done here.
   *  @param name Ignored.
   *  @param c Ignored.
   */
  public void addLayoutComponent (String name, Component c) {
  }

  /**
   *  This is called by the AWT infrastructure to notify the layout manager of
   *  a component being removed from the container.  Elements should be added
   *  to the target container only by the RowLayout itself, so nothing needs to
   *  be done here.
   */
  public void removeLayoutComponent (Component c) {
  }

  /**
   *  Remove a component from this layout.
   *  @param c the component to be removed
   */
  public void remove (Component c) {
    left.remove(c);
    center.remove(c);
    right.remove(c);
    target.remove(c);
  }

  /**
   *  Remove all components from this layout.
   */
  public void removeAll () {
    left.clear();
    center.clear();
    right.clear();
    target.removeAll();
  }

  /**
   *  Calculate the natural dimensions of this layout with the current set of
   *  child components.  The natural dimensions of the layout are the same as
   *  the reported minimum dimensions.
   *  <br><br>
   *  The argument container (as supplied by the AWT infrastructure)
   *  <em>ought</em> to be the same as the RowLayout's target container, but in
   *  any event, it is actually ignored.
   *  @param c Ignored.
   */
  public Dimension preferredLayoutSize (Container c) {
    return minimumLayoutSize(target);
  }

  /**
   *  Calculate the minimal comfortable dimensions of this layout with the
   *  current set of child components.
   *  <br><br>
   *  The argument container (as supplied by the AWT infrastructure)
   *  <em>ought</em> to be the same as the RowLayout's target container, but in
   *  any event, it is actually ignored.
   *  @param c Ignored.
   */
  public Dimension minimumLayoutSize (Container c) {
    Insets i = target.getInsets();
    int w = 0;
    int h = 0;
    int sections = 0;
    for (int k = 0; k < all.length; k++) {
      Vector v = all[k];
      if (v.size() > 0) {
        sections++;
        w += (v.size() - 1) * hSpace;
      }
      Enumeration enu = v.elements();
      while (enu.hasMoreElements()) {
        Dimension d = ((Component) enu.nextElement()).getPreferredSize();
        w += d.width;
        if (d.height > h)
          h = d.height;
      }
    }
    if (sections > 0)
      w += (sections - 1) * hDivider;
    return new Dimension(w + i.left + i.right + leftSpace + rightSpace,
      h + i.top + i.bottom + topSpace + bottomSpace);
  }

  /**
   *  Layout the child elements in the target container.
   *  <br><br>
   *  The argument container (as supplied by the AWT infrastructure)
   *  <em>ought</em> to be the same as the RowLayout's target container, but in
   *  any event, it is actually ignored.
   *  @param c Ignored.
   */
  public void layoutContainer (Container c) {
    Insets i = target.getInsets();
    Dimension size = target.getSize();
    int h = 0;

    // measure the left elements
    Enumeration enu = left.elements();
    int lw = 0;
    while (enu.hasMoreElements()) {
      Dimension d = ((Component) enu.nextElement()).getPreferredSize();
      lw += d.width;
      if (d.height > h)
        h = d.height;
    }
    if (left.size() > 0)
      lw += (left.size() - 1) * hSpace;

    // measure the center elements
    enu = center.elements();
    int cw = 0;
    while (enu.hasMoreElements()) {
      Dimension d = ((Component) enu.nextElement()).getPreferredSize();
      cw += d.width;
      if (d.height > h)
        h = d.height;
    }
    if (center.size() > 0)
      cw += (center.size() - 1) * hSpace;

    // measure the right elements
    enu = right.elements();
    int rw = 0;
    while (enu.hasMoreElements()) {
      Dimension d = ((Component) enu.nextElement()).getPreferredSize();
      rw += d.width;
      if (d.height > h)
        h = d.height;
    }
    if (right.size() > 0)
      rw += (right.size() - 1) * hSpace;

    // layout the left elements
    int y0 = i.top + topSpace;
    int y1 = size.height - i.bottom - bottomSpace;
    int x = i.left + leftSpace;
    enu = left.elements();
    while (enu.hasMoreElements()) {
      Component child = (Component) enu.nextElement();
      Dimension d = child.getPreferredSize();
      child.setBounds(x, getChildY(y0, h, y1),
        d.width, getChildHeight(y0, h, y1));
      x += d.width + hSpace;
    }

    // layout the right elements with sensitivity to overlap from the left
    // and/or center
    x = size.width - i.right - rightSpace - rw;  // nominal starting point
    int xMin = i.left + leftSpace + lw + cw;
    if (lw > 0)
      xMin += hDivider;
    if (cw > 0)
      xMin += hDivider;
    if (x < xMin)
      x = xMin;
    enu = right.elements();
    while (enu.hasMoreElements()) {
      Component child = (Component) enu.nextElement();
      Dimension d = child.getPreferredSize();
      child.setBounds(x, getChildY(y0, h, y1),
        d.width, getChildHeight(y0, h, y1));
      x += d.width + hSpace;
    }

    // layout the central elements with sensitivity to overlap from the sides
    x = (size.width + i.left + leftSpace - i.right - rightSpace - cw)/2;
    xMin = i.left + leftSpace + lw;
    if (lw > 0)
      xMin += hDivider;
    int xMax = size.width - i.right - rightSpace - rw - cw;
    if (rw > 0)
      xMax -= hDivider;
    if (x > xMax)
      x = xMax;
    if (x < xMin)
      x = xMin;
    enu = center.elements();
    while (enu.hasMoreElements()) {
      Component child = (Component) enu.nextElement();
      Dimension d = child.getPreferredSize();
      child.setBounds(x, getChildY(y0, h, y1),
        d.width, getChildHeight(y0, h, y1));
      x += d.width + hSpace;
    }
  }

  // utility method for calculating the vertical location of a child element
  private int getChildY (int top, int pref, int bottom) {
    if (vMode == TOP)
      return top;
    else if (vMode == BOTTOM)
      return bottom - pref;
    else if (vMode == CENTER)
      return (bottom + top - pref)/2;
    else if (vMode == STRETCH)
      return top;
    else
      return top;
  }

  // utility method for calculating the height of a child element
  private int getChildHeight (int top, int pref, int bottom) {
    if (vMode == STRETCH)
      return bottom - top;
    else
      return pref;
  }
}
