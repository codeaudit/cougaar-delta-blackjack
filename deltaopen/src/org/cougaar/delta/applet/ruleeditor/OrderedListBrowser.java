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

import org.cougaar.delta.applet.BasicPanel;
import org.cougaar.delta.util.Code;
import org.cougaar.delta.util.variant.*;
import org.cougaar.delta.applet.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;

/**
 *  The OrderedListBrowser class provides the implementation for a Frame-based
 *  selection tool.  String options are presented in alphabetical order, and
 *  the user can either use the mouse to scroll and/or select among them or
 *  type into the provided text field.  When typing, the partially-matched
 *  selection is automatically scrolled to the top of the display window, and
 *  may be selected using the mouse.
 *  <br><br>
 *  Selected values are reported via an ActionListener/ActionEvent mechanism.
 */
public class OrderedListBrowser extends JFrame {
  private String[] choices = null;
  private String[] toolTips = null;
  private JList list = null;
  private RestrictedJTextField field = null;
  private JButton okay = null;
  private JButton cancel = null;
  private JScrollPane listScroll = null;
  private JTextArea footLine = null;
  private boolean testInstance = false;

  /**
   *  Create a browser for a given list of choices.  If a parent Component is
   *  specified, then the default location for the popup display is centered on
   *  the parent.
   *  @param v a Vector containing the String choices available
   *  @param parent the parent Component, if any.
   *  @param test a flag, which, if true, indicates that this is only a test
   */
  public OrderedListBrowser (Vector v, Component parent, boolean test) {
    super("Browse...");
    testInstance = test;
    if (v != null && v.size() > 0) {
      // do one of two things, depending on the type of input
      if (v.elementAt(0) instanceof String)
        configureStringData(v);
      else if (v.elementAt(0) instanceof Code)
        configureCodeData(v);
    }
    else
      choices = new String[0];
    configureUI(parent);
  }

  /**
   *  Create this browser, as above, but assuming this is not a test
   *  @param v a Vector containing the String choices available
   *  @param parent the parent Component, if any.
   */
  public OrderedListBrowser (Vector v, Component parent) {
    this(v, parent, false);
  }

  // Arrange the Strings in alphabetical order (ignoring capitalization) and
  // store them in the array of choices.
  private void configureStringData (Vector v) {
    choices = new String[v.size()];
    Enumeration enu = new OrderedTraversal(v,
      OrderedTraversal.STRING_IGNORE_CASE);
    for (int i = 0; enu.hasMoreElements(); i++)
      choices[i] = (String) enu.nextElement();
  }

  // a VariantMap that allows Code objects to be sorted by their codeValue field
  private static class CodeSorter implements VariantMap {
    public Variant map (Object w) {
      Code c = (Code) w;
      return new VariantText(c.getCodeValue().toUpperCase());
    }
  }
  private static VariantMap codeValueOrder = new CodeSorter();

  // Arrange the Code objects in alphabetical order by codeValue and store them
  // in the array of choices.  The codeStrings are stored in the toolTips array
  private void configureCodeData (Vector v) {
    choices = new String[v.size()];
    toolTips = new String[v.size()];
    Enumeration enu = new OrderedTraversal(v, codeValueOrder);
    for (int i = 0; enu.hasMoreElements(); i++) {
      Code c = (Code) enu.nextElement();
      choices[i] = c.getCodeValue();
      toolTips[i] = c.getCodeString();
    }
  }

  // set up the UI components
  private void configureUI (Component parent) {
    JPanel content = new JPanel();
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(content, BorderLayout.CENTER);

    content.setLayout(new BorderLayout());
    content.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.gray));

    list = new GList(choices);
    content.add(listScroll = new JScrollPane(list), BorderLayout.CENTER);
    list.addListSelectionListener(new ListEar());

    field = new RestrictedJTextField(15);
    field.addKeyListener(new KeyEar());

    if (testInstance)
      okay = new JButton("Okay");
    else
      okay = RuleEditPane.getButtonFactory().getButton("submit");
    okay.addActionListener(new InternalOkayEar());

    if (testInstance)
      cancel = new JButton("Cancel");
    else
      cancel = RuleEditPane.getButtonFactory().getButton("cancel");
    cancel.addActionListener(new InternalCancelEar());

    JPanel top;
    if (testInstance)
      top = new JPanel();
    else
      top = new BasicPanel();
    top.setLayout(new FlowLayout());
    top.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, Color.gray));
    top.add(field);
    top.add(okay);
    top.add(cancel);
    content.add(new JScrollPane(top), BorderLayout.NORTH);

    if (toolTips != null) {
      footLine = new JTextArea();
      footLine.setOpaque(false);
      footLine.setEditable(false);
      footLine.setLineWrap(true);
      footLine.setWrapStyleWord(true);
      content.add(footLine, BorderLayout.SOUTH);
    }

    setSize(400, 400);

    if (parent != null) {
      Rectangle r = parent.getBounds();
      Dimension d = getSize();
      int my_x = r.x + r.width/2 - d.width/2;
      int my_y = r.y + r.height/2 - d.height/2;
      setLocation((my_x < 0 ? 0 : my_x), (my_y < 0 ? 0 : my_y));
    }

    setVisible(true);
  }

  /**
   *  Specify the restrictions to be enforced on the input.
   *  <br><br>
   *  Be advised that these restrictions will apply not only to keyboard input,
   *  but also to the selections themselves.  For example, if input is
   *  restricted to a length of two characters, and the user clicks on a
   *  5-character option, then the selection will be truncated at 2 characters.
   *  Other similar types of transformations are possible.
   *  @param ch the character set that is allowed or forbidden (see below)
   *  @param inv a flag indicating whether ch is the allowable set or the
   *         forbidden set. If inv is false, then ch is the permitted set. If
   *         inv is true, then ch is the forbidden set.
   *  @param ini the set of  allowed or forbidden the initial characters
   *  @param ini_inv if true then ini is the allowed initial set; otherwise ini
   *         is the forbidden initial set
   *  @param max_len the maximum number of characters allowed for this input
   *  @param caps a flag indicating, if true, that letters should be capitalized
   */
  public void setRestrictions (
      String ch, boolean inv, String ini, boolean ini_inv,
      int max_len, boolean caps)
  {
    field.setCharSet(ch);
    field.setInverseSet(inv);
    field.setInitialsSet(ini);
    field.setInverseInitials(ini_inv);
    field.setMaxLength(max_len);
    field.setCapitalize(caps);
  }

  /**
   *  Adopt the same restrictions as another specified RestrictedJTextField
   *  @param otherField the RestrictedJTextField to mimic
   */
  public void mimicRestrictions (RestrictedJTextField otherField) {
    field.mimic(otherField);
  }

  // GList is a subclass of JList that responds responds to selection events
  // only when it has the focus.  So, if the user is typing a partial selection,
  // the automatic completion doesn't override the typed one.  If the mouse is
  // being used to select, then the list is forced to report it.
  private static class GList extends JList {
    /**
     *  Create this GList object with the given options available
     *  @param o the array of choices
     */
    public GList (Object[] o) {
      super(o);
      addMouseListener(new TrivialMouseListener());
    }

    // A MouseListener which forces the GList to report the user's mouse
    // selections
    private class TrivialMouseListener extends MouseAdapter {
      public void mouseClicked(MouseEvent e) {
        fireSelectionValueChanged (getSelectedIndex(), getSelectedIndex(),
          false, true);
      }
    }

    /**
     *  Notify listeners that a selection has been made.  It only does this
     *  when this component has the keyboard focus.
     */
    protected void fireSelectionValueChanged (int n1, int n2, boolean bla) {
      if (hasFocus())
        super.fireSelectionValueChanged(n1, n2, bla);
    }

    /**
     *  Notify listeners that a selection has been made, if the keyboard focus
     *  is on this component.  An optional flag forces the notification
     *  regardless of where the focus is.
     */
    protected void fireSelectionValueChanged (
        int n1, int n2, boolean bla, boolean force)
    {
      if (force || hasFocus())
        super.fireSelectionValueChanged(n1, n2, bla);
    }
  }

  // A class that listens for changes in the list selection.  Newly selected
  // values are displayed in the input field for user approval, and, when
  // possible, the associated comment is shown at the foot of the browser.
  private class ListEar implements ListSelectionListener {
    public void valueChanged (ListSelectionEvent lse) {
      field.setText(list.getSelectedValue().toString());
      if (footLine != null)
        footLine.setText(toolTips[list.getSelectedIndex()]);
    }
  }

  // A listener for user keystrokes in the input field.
  private class KeyEar extends KeyAdapter {
    public void keyReleased (KeyEvent ke) {
      respondToKeystroke();
    }
  }

  // React to user keystrokes.  Whenever possible, the user's input is
  // interpreted as the initial part of a selection.  The first matching
  // selection is scrolled to the top of the display window.
  private void respondToKeystroke () {
    if (choices == null || choices.length == 0)
      return;
    String keyIn = field.getText();
    int i;
    for (i = 0; i < choices.length; i++) {
      if (choices[i].startsWith(keyIn))
        break;
      if (choices[i].compareTo(keyIn) > 0) {
        if (i > 0)
          i--;
        break;
      }
    }
    if (i == choices.length)
      i--;
    listScroll.getViewport().setViewPosition(list.getCellBounds(i, i).getLocation());
    list.setSelectedIndex(i);
  }

  // support a list of elements interested in local events
  private Vector submitListeners = new Vector();

  /**
   *  Register an ActionListener for selections made by this browser.
   *  @param al the ActionListener that wishes to be notified of selections
   */
  public void addSubmitListener (ActionListener al) {
    submitListeners.addElement(al);
  }

  /**
   *  Unregister an ActionListener for selections made by this browser.
   *  @param al an ActionListener that doesn't wish to be notified of selections
   */
  public void removeSubmitListener (ActionListener al) {
    submitListeners.removeElement(al);
  }

  // Notify interested parties of a selection.
  private void fireSubmitListeners () {
    ActionEvent ae = new ActionEvent(this, 0, field.getText());
    Enumeration enu = submitListeners.elements();
    while (enu.hasMoreElements()) {
      ((ActionListener) enu.nextElement()).actionPerformed(ae);
    }
  }

  // a class that, when prompted to do so, notifies listeners of the current
  // list selection
  private class InternalOkayEar implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      fireSubmitListeners();
    }
  }

  // a listener which, when stimulated, closes the OrderedListBrowser window.
  private class InternalCancelEar implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      setVisible(false);
      dispose();
    }
  }

  // - - - - - - - Testing Code Below This Point - - - - - - - - - - - - - - - -

  private static class TestingEar implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      System.out.println("Captured String \"" + ae.getActionCommand() + "\"");
    }
  }

  public static void main (String[] argv) {
    Vector v = new Vector();
    // scenario_one(v);
    scenario_two(v);
    OrderedListBrowser olb = new OrderedListBrowser(v, null, true);
    olb.addSubmitListener(new TestingEar());
  }

  private static void scenario_two (Vector v) {
    v.addElement(new Code("Bla_Table", "Bla_Code", "Bla", "This is basically 'bla'."));
    v.addElement(new Code("Bla_Table", "Bla_Code", "Blab", "This talks too much."));
    v.addElement(new Code("Bla_Table", "Bla_Code", "Blob", "This doesn't have much shape."));
    v.addElement(new Code("Bla_Table", "Bla_Code", "Blubber", "This has shape but isn't shapely.  And, of course, I could go on all day about this, but if I did, the text area would run out of room to display it."));
  }

  private static void scenario_one (Vector v) {
    for (int i = 9; i > 0; i--) {
      v.addElement("Jane" + i);
      v.addElement("Fred" + i);
    }
    v.addElement("Zane");
  }
}
