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
 *  vertical column.  Elements are stretched to span the entire width of the
 *  container and laid out one above the next with their natural heights (by
 *  default) or stretchable heights as specified at the time of insertion.  The
 *  heights of the components can also be constrained by minimum and maximum
 *  values, if desired.
 *  <br><br>
 *  Note that when a VerticalLayout is created, it is bound to a particular
 *  target container.  Child elements should be added to the VerticalLayout and
 *  <em>NOT</em> to the target itself.  The VerticalLayout automatically adds
 *  children to (or removes them from) the target container when they are added
 *  to (or removed from) the VerticalLayout.
 */
public class VerticalLayout implements LayoutManager {
  /**
   *  RELAX is a constant used to indicate that an element should be allowed to
   *  have its natural height (subject to max or min constraints)
   */
  public static final int RELAX = 0;

  /**
   *  STRETCH is a constant used to indicate that an element should be
   *  stretched vertically.  The actual height is subject to max and min
   *  constraints, if applicable, and only takes up space not being used by
   *  other components.
   */
  public static final int STRETCH = 1;

  /**
   *  STRETCH_MIN is a constant used to indicate that an element should be
   *  stretched vertically.  The actual height is subject to max and min
   *  constraints, if applicable, and is also subject to the minimum height
   *  reported by the component's layout.
   */
  public static final int STRETCH_MIN = 2;

  // the Container whose children are managed by this VerticalLayout
  protected Container target = null;

  // the list of components belonging to the target container
  private Vector list = new Vector();

  /**
   *  Create a new VerticalLayout to manage the children of the specified
   *  Container.
   *  @param c the Container managed by this VerticalLayout
   */
  public VerticalLayout (Container c) {
    target = c;
    target.setLayout(this);
  }

  /**
   *  Add a Component to the bottom of this layout.  In the layout, its height
   *  will be set to its natural height.
   *  @param c the new Component in this layout.
   */
  public void add (Component c) {
    list.addElement(new Wrapper(c, 0, -1, RELAX));
    target.add(c);
  }

  /**
   *  Add a Component to the bottom of this layout with the specified minimum
   *  height, maximum height, and sizing mode.
   *  @param c The Component being added to the layout
   *  @param min the minimum height for this Component
   *  @param max the maximum height for this Component (-1 indicates no limit)
   *  @param mode the sizing mode, RELAX, STRETCH, or STRETCH_MIN
   */
  public void add (Component c, int min, int max, int mode) {
    list.addElement(new Wrapper(c, min, max, mode));
    target.add(c);
  }

  /**
   *  Insert a Component in the specified position among the existing
   *  Components.  When laid out, the Component will be set to its natural
   *  height.
   *  @param c the Component being added
   *  @param i the index at which the new Component is inserted
   */
  public void addAt (Component c, int i) {
    list.insertElementAt(new Wrapper(c, 0, -1, RELAX), i);
    target.add(c);
  }

  /**
   *  Insert a Component at the given position with minimum and maximum heights
   *  and the sizing mode as supplied in the call.
   *  @param c the component being inserted
   *  @param min the minimum height for this component
   *  @param max the maximum height for this component
   *  @param mode the sizing mode, either RELAX or STRETCH
   *  @param i the index at which the new Component is inserted
   */
  public void addAt (Component c, int min, int max, int mode, int i) {
    list.insertElementAt(new Wrapper(c, min, max, mode), i);
    target.add(c);
  }

  /**
   *  Remove a Component from the layout.
   *  @param c the Component to be removed.
   */
  public void remove (Component c) {
    target.remove(c);
    list.remove(new Wrapper(c, 1, 0, -1));
  }

  /**
   *  Remove all of the components being managed by this layout
   */
  public void removeAll () {
    target.removeAll();
    list.removeAllElements();
  }

  /**
   *  This is called by the AWT infrastructure to notify the layout manager of
   *  a component being removed from the container.  Elements should be added
   *  to the target container only by the VerticalLayout itself, so nothing
   *  needs to be done here.
   */
  public void removeLayoutComponent (Component c) {
  }

  /**
   *  This is called by the AWT infrastructure to notify the layout manager
   *  of a component being added to the container.  Elements should be added to
   *  the target container only by the VerticalLayout itself, so nothing needs
   *  to be done here.
   *  @param name Ignored.
   *  @param c Ignored.
   */
  public void addLayoutComponent (String name, Component c) {
  }

  /**
   *  Calculate the natural dimensions of this layout with the current set of
   *  child components.  The natural dimensions of the layout are the same as
   *  the reported minimum dimensions.
   *  <br><br>
   *  The argument container (as supplied by the AWT infrastructure)
   *  <em>ought</em> to be the same as the VerticalLayout's target container,
   *  but in any event, it is actually ignored.
   *  @param c Ignored.
   */
  public Dimension preferredLayoutSize (Container c) {
    return minimumLayoutSize(target);
  }

  // Bracket a value between its specified maximum and minimum values.  If the
  // max value is -1, then no upper limit is applied.
  private static int bracket(int min, int max, int v) {
    if (max == -1)
      return Math.max(min, v);
    else
      return Math.max(min, Math.min(max, v));
  }

  /**
   *  Calculate the minimal comfortable dimensions of this layout with the
   *  current set of child components.
   *  <br><br>
   *  The argument container (as supplied by the AWT infrastructure)
   *  <em>ought</em> to be the same as the VerticalLayout's target container,
   *  but in any event, it is actually ignored.
   *  @param c Ignored.
   */
  public Dimension minimumLayoutSize (Container c) {
    Insets i = target.getInsets();
    int w = 0;
    int h = 0;
    Enumeration enu = list.elements();
    while (enu.hasMoreElements()) {
      Wrapper wrap = (Wrapper) enu.nextElement();
      Dimension d = wrap.component.getPreferredSize();
      if (d.width > w)
        w = d.width;
      if (wrap.vMode == RELAX || wrap.vMode == STRETCH_MIN) {
        h += bracket(wrap.minHeight, wrap.maxHeight, d.height);
      }
      else {
        h += wrap.minHeight;
      }
    }
    return new Dimension(w + i.left + i.right, h + i.top + i.bottom);
  }

  /**
   *  Layout the child elements in the target container.
   *  <br><br>
   *  The argument container (as supplied by the AWT infrastructure)
   *  <em>ought</em> to be the same as the VerticalLayout's target container,
   *  but in any event, it is actually ignored.
   *  @param c Ignored.
   */
  public void layoutContainer (Container c) {
    Dimension minSize = minimumLayoutSize(target);
    Dimension actualSize = target.getSize();
    int stretchHeight = Math.max(0, actualSize.height - minSize.height);
    Insets i = target.getInsets();
    Enumeration enu = list.elements();
    int x = i.left;
    int y = i.top;
    int w = target.getSize().width - i.left - i.right;
    int h = 0;
    while (enu.hasMoreElements()) {
      Wrapper wrap = (Wrapper) enu.nextElement();
      Dimension d = wrap.component.getPreferredSize();
      if (wrap.vMode == RELAX) {
        h = bracket(wrap.minHeight, wrap.maxHeight, d.height);
      }
      else if (wrap.vMode == STRETCH_MIN) {
        int minH = bracket(wrap.minHeight, wrap.maxHeight, d.height);
        h = bracket(wrap.minHeight, wrap.maxHeight, minH + stretchHeight);
        stretchHeight -= h - minH;
      }
      else {
        h = bracket(wrap.minHeight, wrap.maxHeight,
          wrap.minHeight + stretchHeight);
        stretchHeight -= h - wrap.minHeight;
      }
      wrap.component.setBounds(x, y, w, h);
      y += h;
    }
  }

  // Wrapper instances keep a child Component together with its sizing
  // parameters.  The equals method is needed solely for comparisons when
  // removing a Component (the Vector contains Wrappers, and when its remove
  // method is called, it looks for one that ".equals" one containing the
  // Component in question).
  private static class Wrapper {
    Component component = null;
    int minHeight = 0;
    int maxHeight = -1;
    int vMode = RELAX;

    public Wrapper (Component c, int min, int max, int v) {
      component = c;
      minHeight = min;
      maxHeight = max;
      vMode = v;
    }

    public boolean equals (Object o) {
      if (o instanceof Wrapper)
        return ((Wrapper) o).component == component;
      else
        return false;
    }
  }
}
