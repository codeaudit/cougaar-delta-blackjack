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

import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;

public class CascadingChoice extends JPanel implements ItemSelectable {
  // This string is a representation of the cascading menu's structure
  // see below for the syntax
  private String cascadeString = "";

  // As an alternative, use a MenuNode for configuration
  private MenuNode menuConfig = null;

  // Let's have some GUI components -- a button to activate this component and
  // a menu to provide the user with the various choices
  private JButton butt = null;
  private JPopupMenu pop = new JPopupMenu();

  // The usual configuration stuff -- A font and a foreground color for the
  // words, and a background color for the component
  private Font menuFont = new Font("Arial", Font.PLAIN, 12);
  private Color menuColor = Color.lightGray;
  private Color menuTextColor = Color.black;

  // store the X and Y coordinates of a mouse press; this is where the menu
  // will appear
  private int mouseX = 0;
  private int mouseY = 0;

  // an ear for listening to Menu events (from JPopupMenu "pop")
  private class MenuSelectionEar implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      setText(ae.getActionCommand());
    }
  }
  private ActionListener menuSelectEar = new MenuSelectionEar();

  // an ear for listening to button presses (from JButton "butt")
  private class MenuPopupEar implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      showMenu();
    }
  }
  private ActionListener buttonPressEar = new MenuPopupEar();

  // Not a mouseketeer's hat.  This ear listens for a mouse button being
  // pressed and records the location of the event.
  private class MouseClickLocator extends MouseAdapter {
    public void mousePressed (MouseEvent e) {
      mouseX = e.getX();
      mouseY = e.getY();
    }
  }
  private MouseListener mouseEar = new MouseClickLocator();

  /**
   *  Specify the font and the foreground and background colors used by the
   *  pop-up menu associated with this component.  If any of the arguments is
   *  null, then the associated property is left unchanged.
   *  @param f The font used for text in the menu
   *  @param bg the Color in which the menu is rendered
   *  @param fg the Color used for text in the menu
   */
  public void setMenuFontAndColor (Font f, Color bg, Color fg) {
    if (f != null)
      menuFont = f;
    if (bg != null)
      menuColor = bg;
    if (fg != null)
      menuTextColor = fg;
    resetCascade();
  }

  /**
   *  Specify the background color for this component.  The button pretty much
   *  occupies all the space in this JPanel, and its background color is set to
   *  match.
   *  @param c the new background color
   */
  public void setBackground (Color c) {
    super.setBackground(c);
    if (butt != null && c != null)
      butt.setBackground(c);
  }

  /**
   *  Specify the foreground color in which the button text is rendered.
   *  @param c the new foreground color
   */
  public void setForeground (Color c) {
    super.setForeground(c);
    if (butt != null && c != null)
      butt.setForeground(c);
  }

  /**
   *  Specify the font to be used for text appearing on the button
   *  @param f the font for this component
   */
  public void setFont (Font f) {
    super.setFont(f);
    if (butt != null && f != null)
      butt.setFont(f);
  }

  /**
   *  Bring into existence a new CascadingChoice instance.  The button is
   *  installed as the major component and wiring is set up to handle
   *  manipulation by the user.
   */
  public CascadingChoice () {
    super();
    setLayout(new BorderLayout());
    // butt = new JButton();
    butt = new ArrowButton();
    add(butt, BorderLayout.CENTER);
    butt.setText("<<NONE>>");
    butt.addActionListener(buttonPressEar);
    butt.addMouseListener(mouseEar);
  }

  // show the popup menu at the location of the mouse click
  private void showMenu () {
    pop.show(butt, mouseX, mouseY);
  }

  /**
   *  Discover the size this component prefers for itself.  In this case, the
   *  JButton is intended to be the only visible child component, so its
   *  preferred size is also the whole component's preferred size.
   *  @return the preferred size of this CascadingChoice component
   */
  public Dimension getPreferredSize () {
    return butt.getPreferredSize();
  }

  /**
   *  This inner class is responsible for holding the configuration of the
   *  owning instance's popup menu.  It can be called upon to generate a new
   *  one on demand.
   */
  public static class MenuNode {
    private Vector children = new Vector();
    private String label = null;

    /**
     *  Construct a new MenuNode with no label, indicating that this is the
     *  root of a node tree.  This is the only type of construction that can
     *  be invoked from outside the class.  Nodes with labels are added by
     *  calling the "addChild" method (q.v.)
     */
    public MenuNode () {
    }

    // Create a node with a given label
    private MenuNode (String s) {
      label = s;
    }

    /**
     *  Add a child node to the current node.  The child is created with the
     *  given label and inserted as a child into this node's structure.  A
     *  reference to the new MenuNode is returned in case the caller wants to
     *  insert children into it, etc.
     *  @param s the label applied to the new node.
     *  @return the newly created node.
     */
    public MenuNode addChild (String s) {
      MenuNode child = null;
      if (s == null)
        child = new MenuNode("");
      else
        child = new MenuNode(s);
      children.addElement(child);
      return child;
    }

    /**
     *  Discover whether this node is a leaf; i.e., whether it has no children
     *  @return true if this MenuNode has no child nodes
     */
    public boolean isLeaf () {
      return children.size() == 0;
    }

    /**
     *  Install in the target CascadingChoice a JPopupMenu whose JMenuItem/JMenu
     *  hierarchy reflects the structure of the MenuNode tree rooted at the
     *  current instance.  Graphical settings, such as the background color, the font,
     *  and the foreground color are drawn from the CascadingChoice, as is the
     *  menuSelectEar which is registered for events on the resulting menu.
     *
     *  @param cc the CascadingChoice for which this menu is being constructed
     */
    public void makeMenu (CascadingChoice cc) {
      JPopupMenu jpm = new JPopupMenu();
      Enumeration enu = children.elements();
      while (enu.hasMoreElements()) {
        MenuNode node = (MenuNode) enu.nextElement();
        if (node.isLeaf())
          jpm.add(node.makeMenuLeaf(cc));
        else
          jpm.add(node.makeSubMenu(cc));
      }
      cc.pop = jpm;
    }

    // Construct a JMenu subordinate to the JPopupMenu being constructed
    private JMenu makeSubMenu (CascadingChoice cc) {
      JMenu jim = new JMenu(label);
      jim.setBackground(cc.menuColor);
      jim.setForeground(cc.menuTextColor);
      jim.setFont(cc.menuFont);
      Enumeration enu = children.elements();
      while (enu.hasMoreElements()) {
        MenuNode node = (MenuNode) enu.nextElement();
        if (node.isLeaf())
          jim.add(node.makeMenuLeaf(cc));
        else
          jim.add(node.makeSubMenu(cc));
      }
      return jim;
    }

    // Construct a JMenuItem as leaf element in the menu tree.  This is one the
    // user can actually select.  In the process, register the CascadingChoice
    // element's listener component for events on the JMenuItem
    private JMenuItem makeMenuLeaf (CascadingChoice cc) {
      JMenuItem jimmi = new JMenuItem(label);
      jimmi.setBackground(cc.menuColor);
      jimmi.setForeground(cc.menuTextColor);
      jimmi.setFont(cc.menuFont);
      jimmi.addActionListener(cc.menuSelectEar);
      return jimmi;
    }
  }

  /**
   *  Parse the configuration String and give the popup menu the corresponding
   *  structure.  The syntax for the configuration String can be summarized
   *  by the grammar (starting with START)
   *  <ul>
   *    <li>START -> CHOICES
   *    <li>CHOICES -> ITEM*</li>
   *    <li>ITEM -> '{' TOKEN CHOICES '}'</li>
   *    <li>TOKEN -> in words, a string with '{', '}', and '\' escaped in by '\'</li>
   *  </ul>
   *  where each TOKEN represents a menu item.  If the TOKEN is followed by a
   *  nonempty CHOICES, then it is a submenu with items listed therein.
   *  <br><br>
   *  When this method is called, the menuConfig field is set to null so that
   *  there cannot be two competing configurations present
   *
   *  @param s the string representing the intended structure
   */
  public void setCascade (String s) {
    menuConfig = null;
    cascadeString = s;
    StringTokenizer tok =
      new StringTokenizer(s, LEFT + RIGHT + ESCAPE, true);

    if (tok.hasMoreTokens()) tok.nextToken();
    while (tok.hasMoreTokens()) {
      pop.add(populateMenu(tok));
      if (tok.hasMoreTokens()) tok.nextToken();
    }
  }

  /**
   *  Install the provided MenuNode configuration in this element and call upon
   *  it to configure the associated JPopupMenu.  Incidentally, set the
   *  cascadeString to null so that there are not two competing configurations
   *  present.
   *  @param root the root node of the configuration tree structure
   */
  public void setCascade (MenuNode root) {
    cascadeString = null;
    menuConfig = root;
    menuConfig.makeMenu(this);
  }

  /**
   *  Reconstruct the popup menu using the current configuration String or
   *  MenuNode configuration tree, whichever is not null.  This
   *  operation might be performed to reset colors and fonts, for instance.
   */
  public void resetCascade () {
    if (cascadeString != null)
      setCascade(cascadeString);
    else if (menuConfig != null)
      setCascade(menuConfig);
  }

  // The significant punctuation marks found in the configuration String
  private static String LEFT = "{";
  private static String RIGHT = "}";
  private static String ESCAPE = "\\";

  /**
   *  This static function is provided as a utility for classes creating a
   *  configuration string for a CascadingChoice component.  If any of the
   *  labels contain special characters, they are escaped in using the ESCAPE
   *  character currently in use in this class.
   *  @param s the raw String to be encoded
   *  @return the encoded String
   */
  public static String escapeSpecials (String s) {
    if (s == null)
      return null;
    StringTokenizer tok = new StringTokenizer(s, LEFT + RIGHT + ESCAPE, true);
    StringBuffer buf = new StringBuffer();
    while (tok.hasMoreTokens()) {
      String bit = tok.nextToken();
      if (bit.equals(LEFT) || bit.equals(RIGHT) || bit.equals(ESCAPE))
        buf.append(ESCAPE + bit);
      else
        buf.append(bit);
    }
    return buf.toString();
  }

  // go through the string looking for menu items, recursively creating submenu
  // objects as necessary
  private JMenuItem populateMenu (StringTokenizer tok) {
    String token = "";
    StringBuffer buf = new StringBuffer();
    while (tok.hasMoreTokens()) {
      token = tok.nextToken();
      if (token.equals(ESCAPE) && tok.hasMoreTokens())
        buf.append(tok.nextToken());
      else if (token.equals(LEFT))
        break;
      else if (token.equals(RIGHT))
        break;
      else
        buf.append(token);
    }
    if (!tok.hasMoreTokens() || !token.equals(LEFT)) {
      JMenuItem jimmi = new JMenuItem(buf.toString());
      jimmi.setBackground(menuColor);
      jimmi.setForeground(menuTextColor);
      jimmi.setFont(menuFont);
      jimmi.addActionListener(menuSelectEar);
      return jimmi;
    }

    JMenu jim = new JMenu(buf.toString());
    jim.setBackground(menuColor);
    jim.setForeground(menuTextColor);
    jim.setFont(menuFont);
    while (tok.hasMoreTokens()) {
      jim.add(populateMenu(tok));
      if (tok.hasMoreTokens())
        token = tok.nextToken();
      if (token.equals(RIGHT))
        break;
    }
    return jim;
  }

  // Partial support for ItemListeners.  Warning:  Some of the ItemEvent
  // information is not supplied.
  private Vector itemListeners = new Vector();
  private String lastSelected = null;

  /**
   *  Register an ItemListener's interest in this component.
   *  @param il the ItemListener in question
   */
  public void addItemListener (ItemListener il) {
    itemListeners.addElement(il);
  }

  /**
   *  Unregister an ItemListener who has lost interest in this component.
   *  @param il the ItemListener in question
   */
  public void removeItemListener (ItemListener il) {
    itemListeners.removeElement(il);
  }

  // notify the registered ItemListeners of a new selection
  private void fireItemListeners (ItemEvent ie) {
    Enumeration enu = itemListeners.elements();
    while (enu.hasMoreElements())
      ((ItemListener) enu.nextElement()).itemStateChanged(ie);
  }

  /**
   *  Added solely for compliance with the ItemSelectable interface.  Retrieve
   *  an array containing <it>all</it> selected Objects in this component.  Of
   *  course, exactly one thing can be selected at any given time, so the array
   *  is always of length one.
   *  @return the array of selections
   */
  public Object[] getSelectedObjects () {
    return new Object[] {butt.getText()};
  }

  /**
   *  Set the selected value of this component.  In the process, notify any
   *  interested listeners of the change.  Note:  the value does not have to
   *  be available in the menu for this operation to succeed.
   *  @param s the new selection
   */
  public void setText (String s) {
    lastSelected = butt.getText();
    butt.setText(s);
    fireItemListeners(new ItemEvent(
      this, ItemEvent.DESELECTED, lastSelected, ItemEvent.DESELECTED));
    fireItemListeners(new ItemEvent(
      this, ItemEvent.SELECTED, s, ItemEvent.SELECTED));
  }

  /**
   *  Retrieve the value currently assumed by this component.  This will be
   *  whatever String the button is using as its text.
   *  @return the currently selected value
   */
  public String getText () {
    return butt.getText();
  }

  // - - - - - - - Testing Scaffolding - - - - - - - - - - - - - - - - - - - - -

  public static void main (String[] argv) {
    Font ruleElementFont = new Font("Arial", Font.PLAIN, 14);
    JFrame frame = new JFrame();
    frame.setSize(400, 400);
    frame.getContentPane().setLayout(new FlowLayout());
    CascadingChoice fred = new CascadingChoice();
    // fred.setCascade("{Bla|la{Bla-1}{Bla-2}{Bla-3}}" +
    //   "{HarHa{Harrumph}{Har-Har{Ha-ha-ha}{Jar-Jar}}}");
    MenuNode root = new MenuNode();
    MenuNode child = root.addChild("bla");
    child.addChild("Bla-1");
    child.addChild("Bla-2");
    child = root.addChild("HarHa");
    child.addChild("Harrumph");
    MenuNode gc = child.addChild("Har-Har");
    gc.addChild("ha-ha-ha");
    gc.addChild("Jar-Jar");
    fred.setCascade(root);

    frame.getContentPane().add(fred);
    frame.validate();
    frame.setVisible(true);
  }
}
