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
import org.cougaar.delta.util.qrule.*;
import org.cougaar.delta.applet.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;

/**
 *  A RuleCasePane instance gives the user a graphical interface for managing a
 *  collection of conditions in a rule.  Generally speaking, there are two
 *  kinds of cases that are likely to be useful:  the base tests of a rule
 *  (each rule has exactly one such case) and exception cases (a rule may have
 *  zero or more of these).  Clauses can be added to or subtracted from the
 *  case and can be edited in their own fashion.
 */
public class RuleCasePane extends JPanel {
  // The clauses that make up this case are the ones subordinate to the
  // following logical operator QRuleTest
  private QRuleLogicalTest mainCondition = null;

  // the RuleFrame of which this is a part
  private RuleEditPane editor;

  // A listener for changes in the clauses belonging to this case
  private ClauseListener clauseEar = null;

  // boolean flag to indicate whether this case can be deleted
  private boolean delete_flag = false;

  // boolean flag to indicate whether this case's name can be edited by the
  // user
  private boolean namedByUser = false;

  // a JTextField where the user can edit the name, if allowed
  private RestrictedJTextField nameField = null;

  // a button that, if pressed, deletes this case
  private JButton deleteCaseButton = null;

  // the layout manager in charge of the ClausePanels
  private VerticalLayout clauseBox = null;

  // A class of objects for listening to the "Delete" buttons associated with
  // the clause panels
  private class DeleteButtonEar implements ActionListener {
    private ClausePanel myClause = null;

    public DeleteButtonEar (ClausePanel c) {
      if (c == null)
        throw new IllegalArgumentException(
          "A non-null ClausePanel must be supplied");
      myClause = c;
    }

    public void actionPerformed (ActionEvent ae) {
      respondToDelete(myClause);
    }
  }

  // Here's what to do when the user presses one of the "Delete" buttons
  private void respondToDelete (ClausePanel c) {
    clauseBox.remove(c);
    mainCondition.removeOperand(c.getTest());
    clauseEar.changeInClause();
    revalidate();
    repaint();
  }

  // This is a class of "Ears" for listening to changes in the rule's name
  private class RuleNameEar extends KeyAdapter {
    public void keyReleased (KeyEvent ke) {
      respondToNameChange();
    }
  }

  // Do this when the user changes the contents of the "name" text field
  private void respondToNameChange () {
    String text = nameField.getText();
    mainCondition.setName(text);
  }

  /**
   *  Pass on a request to register an ActionListener with the delete button
   *  corresponding to this case.
   *  @param ae the listener for delete requests
   */
  public void addDeleteListener (ActionListener ae) {
    deleteCaseButton.addActionListener(ae);
  }

  /**
   *  Retrieve a reference to the QRuleLogicalTest represented by this case
   *  @return the logical test
   */
  public QRuleLogicalTest getMainCondition () {
    return mainCondition;
  }

  /**
   *  Specify whether the case associated with this RuleCasePane can be deleted
   *  from the containing rule.  This is generally true of exception clauses
   *  but not true of base condition sets.  When this case is under
   *  consideration, the "Delete Case" button will be visible to the user only
   *  if the case can be deleted.
   *  @param d true if this case can be deleted, false otherwise
   */
  public void setRemovable (boolean d) {
    delete_flag = d;
    deleteCaseButton.setEnabled(delete_flag);
    deleteCaseButton.setVisible(delete_flag);
  }

  /**
   *  Find out whether this case is removable or not
   *  @return true if the case can be deleted, false otherwise
   */
  public boolean isRemovable () {
    return delete_flag;
  }

  /**
   *  Set a flag indicating whether the user is allowed to edit the name of
   *  this set of rule conditions.
   *  @param n true if this case is user-nameable; false otherwise
   */
  public void setNamedByUser (boolean n) {
    namedByUser = n;
  }

  /**
   *  Report whether this rule case's name can be edited by the user.
   *  @return true if and only if the name is editable
   */
  public boolean isNamedByUser () {
    return namedByUser;
  }

  /**
   *  Construct a new rule case for the given Rule Editor with a title message
   *  and a root operator as provided by the caller.
   *  @param f the rule editor of which this is part
   *  @param test the logical test containing the tests for this case
   *  @param message the title message for this case
   *  @param d_flag a flag indicating, if true, that this case is removable
   *  @throw NullPointerException if test is null or does not belong to a rule
   */
  public RuleCasePane (
      RuleEditPane p, QRuleLogicalTest test, String message, boolean d_flag,
      boolean rename_flag)
  {
    if (test == null)
      throw new NullPointerException("Constructing RuleCasePane on null case");
    if (test.getRule() == null)
      throw new NullPointerException("Constructing RuleCasePane on case with no rule");
    if (p == null)
      throw new NullPointerException("Constructing RuleCasePane without a RuleEditPane?");

    mainCondition = test;
    editor = p;
    clauseEar = p.getClauseEar();

    deleteCaseButton = RuleEditPane.getButtonFactory().getButton("deleteCase");
    deleteCaseButton.addActionListener(p.getCaseDeleteEar(this));
    setRemovable(d_flag);
    setNamedByUser(rename_flag);

    initGui(message);
    loadClauses();
  }

  // set up the initial configuration of clauses
  private void loadClauses () {
    if (mainCondition.getName() != null)
      nameField.setText(mainCondition.getName());

    for (Enumeration enu = mainCondition.getOperands(); enu.hasMoreElements(); )
    {
      QRuleComparison clause = ((QRuleTest) enu.nextElement()).getComparison();
      if (clause != null)
        addClause(clause);
    }
  }

  // instantiate and configure the main GUI widgets
  private void initGui (String message) {
    setBorder(BorderFactory.createLineBorder(Color.black, 2));
    setBackground(RuleEditPane.ruleBgColor);
    setLayout(new BorderLayout());

    // configure the header panel
    JPanel p = new JPanel();
    p.setOpaque(false);
    add(p, BorderLayout.NORTH);
    RowLayout lay = new RowLayout(p);
    lay.setSpaceParameters(2, 2, 2, 2, 5, 20);

    // include the label and name field in the upper left
    JLabel label = new JLabel(message);
    label.setFont(RuleEditPane.labelFont);
    label.setForeground(RuleEditPane.ruleTextColor);
    label.setOpaque(false);
    lay.addLeft(label);
    nameField = new RestrictedJTextField(15);
    nameField.setMaxLength(20);
    nameField.setFont(RuleEditPane.elementFont);
    nameField.setBackground(RuleEditPane.ruleElementColor);
    nameField.setVisible(isNamedByUser());
    nameField.setEnabled(isNamedByUser());
    nameField.addKeyListener(new RuleNameEar());
    nameField.addFocusListener(new TabNameMonitor());
    lay.addLeft(nameField);

    // include the delete button in the upper right
    lay.addRight(deleteCaseButton);

    // create a panel for the conditions
    p = new BasicPanel();
    p.setBorder(null);
    clauseBox = new VerticalLayout(p);
    add(new JScrollPane(p), BorderLayout.CENTER);
  }

  /**
   *  Add a clause to this case.  If the clause is based on an existing
   *  QRuleComparison, it is supplied in the call.  If not, then a new test is
   *  created, installed in the rule's expression hierarchy, and initialized
   *  with the new ClausePanel's default values.  The new ClausePanel is
   *  returned as the result.
   *  @param q the test to show graphically, if any
   *  @return a newly-created ClausePanel
   */
  public ClausePanel addClause (QRuleComparison q) {
    ClausePanel c = new ClausePanel(q, editor);

    // this check is performed here so that the new test is always initialized
    // with the GUI's default values
    if (q == null)
      c.configureNewTest(new QRuleComparison(
        mainCondition.getRule(), mainCondition, null, null, null));

    c.addDeleteListener(new DeleteButtonEar(c));
    c.addClauseListener(clauseEar);
    clauseBox.add(c);
    clauseEar.changeInClause();
    revalidate();
    return c;
  }

  // set the name of this rule case
  private void setCaseName () {
    String text = nameField.getText();
    mainCondition.setName(text);
  }

  // call upon the parent frame to change the name in the tab for this panel
  private void setMyTabLabel (String text) {
    if (isNamedByUser())
      editor.resetCaseTabLabel(this, text);
  }

  // The purpose of this class is to change the name of an exception case when
  // the focus is moved from the name entry field.
  // Continual updates (on every keystroke) are ill advised because the call to
  // JTabbedPane::indexOfComponent or JTabbedPane::setTitleAt is too slow to
  // keep up with the typist.
  private class TabNameMonitor implements FocusListener {
    public void focusGained(FocusEvent e) { }
    public void focusLost(FocusEvent e) {
      setMyTabLabel(nameField.getText());
    }
  }
}
