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

import org.cougaar.delta.util.variant.OrderedTraversal;
import org.cougaar.delta.util.qrule.*;
import org.cougaar.delta.applet.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;

/**
 *  The LhsCascade class is a GUI component designed to represent graphically
 *  the left-hand side of a single test in a QRule.  The user can make changes
 *  in this component through a cascading menu that pops up when the mouse is
 *  clicked on it.
 *  <br><br>
 *  The options themselves are retrieved from the database by way of the
 *  RuleEditServlet the first time this class is instantiated, and they are
 *  stored statically for use in future instantiations.
 *  <br><br>
 *  Changes in an LhsCascade component are reported to registered listeners
 *  by means of the LhsListener interface.
 */
public class LhsCascade extends CascadingChoice implements ItemListener {
  // the Rule Editor that spawned this component
  private RuleEditPane editor = null;

  // a place to store the menu's choices so that the work of fetching them does
  // not have to be done repeatedly
  public static Vector choices = null;
  private static Vector menuOptions = null;
  private static Hashtable menuIndex = new Hashtable();
  private static MenuNode lhsConfigTree = null;

  /**
   *  Give a list of the accessor operands currently in use.
   *  @return an enumeration of QRuleAccessorOperand objects
   */
  public static Enumeration getLhsOperands () {
    return menuOptions.elements();
  }

  // add support for LhsListeners
  private boolean cancelLhsEvents = true;
  private Vector lhsListeners = new Vector();

  /**
   *  Register a listener for changes in this component.  All those registered
   *  are notified when a new selection is made.
   *  @param ll the new listener
   */
  public void addLhsListener (LhsListener ll) {
    lhsListeners.addElement(ll);
  }

  /**
   *  Remove a listener and no longer notify it when this component is changed.
   *  @param ll the disinterested party
   */
  public void removeLhsListener (LhsListener ll) {
    lhsListeners.removeElement(ll);
  }

  // notify interested parties of a new selection on this element
  private void fireLhsListeners (QRuleAccessorOperand opt) {
    if (cancelLhsEvents) return;
    Enumeration enu = lhsListeners.elements();
    while (enu.hasMoreElements())
      ((LhsListener) enu.nextElement()).changeInLhs(opt);
  }

  // ItemListener implementation
  private Object lastSelected = null;
  public void itemStateChanged (ItemEvent e) {
    if (e.getStateChange() == ItemEvent.DESELECTED) {
      lastSelected = e.getItem();
    }
    else if (e.getStateChange() == ItemEvent.SELECTED) {
      Object obj = e.getItem();
      if (!obj.toString().equals(lastSelected.toString())) {
        QRuleAccessorOperand opt = (QRuleAccessorOperand) menuIndex.get(obj);
        fireLhsListeners(opt);
      }
    }
  }

  /**
   *  Create a new LhsCascade object initialized with the given operand value.
   *  If null is supplied, then resort to a default setting.
   *  @param q the initial QRuleAccessorOperand value of this element
   *  @param theFrame the RuleFrame that spawned this component
   */
  public LhsCascade (QRuleAccessorOperand q, RuleEditPane p) {
    super();
    editor = p;

    setForeground(RuleEditPane.ruleTextColor);
    setBackground(RuleEditPane.ruleElementColor);
    setFont(RuleEditPane.elementFont);
    setMenuFontAndColor(RuleEditPane.elementFont,
      RuleEditPane.ruleElementColor, RuleEditPane.ruleTextColor);

    if (menuOptions == null) {
      loadMenuOptions();
      configureMenu();
    }
    setCascade(lhsConfigTree);
    if (q == null) {
      if (choices.size() > 0)
        setText((String) choices.elementAt(0));
      else
        setText("<<NONE AVAILABLE>>");
    }
    else {
      setText(q.toString());
    }
    addItemListener(this);
    cancelLhsEvents = false;
  }

  /**
   *  Create a new LhsMenu with the default value as its initial setting
   *  @param theFrame the RuleFrame that spawned this component
   */
  public LhsCascade (RuleEditPane p) {
    this(null, p);
  }

  private void configureMenu () {
    if (choices == null) {
      Hashtable options = new Hashtable();
      Vector headNames = new Vector();
      Enumeration enu = menuOptions.elements();
      while (enu.hasMoreElements()) {
        QRuleAccessorOperand q = (QRuleAccessorOperand) enu.nextElement();
        menuIndex.put(q.getUiName(), q);
        String category = q.getUiCategory();
        if (!headNames.contains(category)) {
          options.put(category, new Vector());
          headNames.addElement(category);
        }
        ((Vector) options.get(category)).addElement(q);
      }

      choices = new Vector();
      lhsConfigTree = new MenuNode();
      Enumeration k = new OrderedTraversal(headNames,
        OrderedTraversal.STRING_IGNORE_CASE);
      while (k.hasMoreElements()) {
        String category = (String) k.nextElement();
        MenuNode child = lhsConfigTree.addChild(category);
        Enumeration opts = new OrderedTraversal((Vector) options.get(category),
          OrderedTraversal.STRING_IGNORE_CASE);
        while (opts.hasMoreElements()) {
          String name = ((QRuleAccessorOperand) opts.nextElement()).getUiName();
          child.addChild(name);
          choices.addElement(name);
        }
      }
    }
  }

  // Get the menu options from the database via the servlet
  private void loadMenuOptions () {
    for (int i = 0; i < 10 && menuOptions == null; i++) {
      menuOptions = (Vector) editor.serverRequest("ACCESSOR_OPERANDS", null);
    }
    if (menuOptions == null)
      System.out.println("LhsCascade::loadMenuOptions:  Failed to load menu options");
  }

  /**
   *  Retrieve the QRuleAccessorOperand currently selected in this component.
   *  In effect, this is the "value" of the corresponding left-hand operand in
   *  a QRuleTest.
   *  @return the selected accessor operand
   */
  public QRuleAccessorOperand getSelectedItem () {
    return (QRuleAccessorOperand) menuIndex.get(getText());
  }
}
