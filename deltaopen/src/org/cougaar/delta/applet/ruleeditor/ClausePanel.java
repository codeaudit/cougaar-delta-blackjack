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

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import org.cougaar.delta.util.qrule.*;


public class ClausePanel extends JPanel {
  // "test" is the underlying QRuleTest object of which this ClausePanel is
  // the graphical representation
  private QRuleComparison test = null;

  // Some GUI elements for changing or deleting the clause
  private LhsCascade lhsMenu;
  private OpMenu opMenu;
  private RhsLiteral rhs;
  private JButton deleteButton = RuleEditPane.getButtonFactory().getButton("delete");

  // An Ear for listening to changes in the right-hand side of this test.
  // When such a change is detected, update the QRuleTest accordingly.
  private class RhsEar implements RhsListener {
    public void changeInRhs () {
      updateQRuleTest();
    }
  }

  /**
   *  Retrieve the QRuleTest that this clause is based on.  Once created, the
   *  ClausePanel has a single QRuleTest that it manipulates.
   *  @return the test
   */
  public QRuleComparison getTest () {
    return test;
  }

  /**
   *  Construct a ClausePanel to give a graphical representation of the given
   *  QRuleTest.  The Rule Editor that created this ClausePanel is also
   *  supplied by the caller, and is used in subcomponents to get information
   *  from the server.
   *  @param q the test being represented
   *  @param theFrame the RuleFrame that created this ClausePanel
   */
  public ClausePanel (QRuleComparison q, RuleEditPane p) {
    test = q;

    try {
      jbInit(p);
      rhs.addRhsListener(new RhsEar());
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  // Do this when components of the clause have been modified
  private void updateQRuleTest () {
    if (test == null)
      return;

    configureTest();
    fireClauseListeners();
  }

  // Make a new QRuleTest, initialize it, and install it in this ClausePanel
  public void configureNewTest (QRuleComparison newTest) {
    test = newTest;
    configureTest();
  }

  private void configureTest () {
    test.setOperand1((QRuleOperand) lhsMenu.getSelectedItem());
    test.setOperator((QRuleOperator) opMenu.getSelectedItem());
    test.setOperand2(rhs.getOperandValue());
  }

  // Initialize the graphical stuff
  private void jbInit (RuleEditPane p) throws Exception {
    QRuleAccessorOperand left = null;
    QRuleOperator qrop = null;
    QRuleOperand right = null;
    if (test != null) {
      left = (QRuleAccessorOperand) test.getOperand1();
      qrop = test.getOperator();
      right = test.getOperand2();
    }

    lhsMenu = new LhsCascade(left, p);
    opMenu = new OpMenu(lhsMenu, qrop, p);
    rhs = new RhsLiteral(lhsMenu, opMenu, right, p);

    if (test != null)
      configureTest();

    setOpaque(false);
    setBorder(BorderFactory.createMatteBorder(0,0,1,0, Color.black));
    RowLayout lay = new RowLayout(this);
    lay.setSpaceParameters(2, 2, 2, 2, 5, 20);
    lay.addLeft(lhsMenu);
    lay.addLeft(opMenu);
    lay.addLeft(rhs);
    lay.addRight(deleteButton);
  }

  /**
   *  Delegate responsibility for "delete" events on this ClausePanel to its
   *  "Delete" button.  Here, a request to register a listener for "delete"
   *  events is forwarded to the "Delete" button as a request to register a
   *  generic ActionListener.
   *  @param al the ActionListener interested in "delete" events
   */
  public void addDeleteListener (ActionListener al) {
    deleteButton.addActionListener(al);
  }

  /**
   *  Delegate responsibility for "delete" events on this ClausePanel to its
   *  "Delete" button.  Here, a request to unregister a listener for "delete"
   *  events is forwarded to the "Delete" button as a request to remove a
   *  generic ActionListener.
   *  @param al the ActionListener interested in "delete" events
   */
  public void removeDeleteListener (ActionListener al) {
    deleteButton.removeActionListener(al);
  }

  // Provide support for devices listening for changes in this clause
  private Vector clauseListeners = new Vector();

  /**
   *  Register a listener for changes in the test represented by this
   *  ClausePanel.
   *  @param cl a ClauseListener to be notified in case of a change
   */
  public void addClauseListener (ClauseListener cl) {
    clauseListeners.addElement(cl);
  }

  /**
   *  Unregister a listener for changes in the QRuleTest.  Remove the given
   *  ClauseListener from the list of those to be notified in case of a change.
   *  @param cl the ClauseListener that is no longer interested in changes
   */
  public void removeClauseListener (ClauseListener cl) {
    clauseListeners.removeElement(cl);
  }

  // notify interested parties of an event originating in this clause
  private void fireClauseListeners () {
    Enumeration enu = clauseListeners.elements();
    while (enu.hasMoreElements())
      ((ClauseListener) enu.nextElement()).changeInClause();
  }
}
