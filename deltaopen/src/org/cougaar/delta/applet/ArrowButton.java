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

package org.cougaar.delta.applet;

import javax.swing.*;
import java.awt.*;

/**
 *  An ArrowButton is a form of JButton whose appearance is designed to mimic
 *  that of a JComboBox.  Text may be displayed as in a regular JButton, but
 *  an icon of an inverted triangle occupies the right side of the button's
 *  display area.
 */
public class ArrowButton extends JButton {
  /**
   *  A Triangle panel is a panel that renders itself with an inverted
   *  triangle icon centered on a solid background.
   */
  private class TriangleIcon implements Icon {
    private int x_offset = 10;
    private int myHeight = 4;
    private int myWidth = 9;

    /**
     *  Draw the icon at the specified coordinates.
     *  @param c a Component?--not used here.
     *  @param g the graphics context into which the icon is being painted
     *  @param x the x-coordinate of the icon
     *  @param y the y-coordinate of the icon
     */
    public void paintIcon(Component c, Graphics g, int x, int y) {
      int x1 = x + x_offset;
      int x2 = x1 + myHeight;
      int x3 = x2 + myHeight;
      int y1 = y;
      int y2 = y + myHeight;
      Polygon P = new Polygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y1}, 3);
      g.setColor(getForeground());
      g.fillPolygon(P);
      g.drawPolygon(P);
    }

    /**
     *  Report the apparent width of this icon, including a horizontal offset
     *  from any text appearing to its left.
     *  @return the width of this icon
     */
    public int getIconWidth() {
      return x_offset + myWidth;
    }

    /**
     *  Report this icon's height.
     *  @return the height of this icon
     */
    public int getIconHeight() {
      return myHeight;
    }

    /**
     *  Create a new TriangleIcon with the default icon size
     */
    public TriangleIcon () {
    }
  }

  /**
   *  Construct a button that mimics the appearance of a JComboBox.
   */
  public ArrowButton () {
    setFocusPainted(false);
    setOpaque(true);
    setHorizontalTextPosition(AbstractButton.LEFT);
    setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createEtchedBorder(Color.white, Color.darkGray),
      BorderFactory.createEmptyBorder(2, 4, 2, 4)));
    setText("");
    setIcon(new TriangleIcon());
  }
}
