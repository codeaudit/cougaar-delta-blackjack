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

import org.cougaar.delta.util.qrule.*;
import org.cougaar.delta.applet.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;

/**
 *  The OpMenu class is designed to give a graphical representation of the
 *  comparison operator (QRuleOperator) in a QRuleTest.  The user interface of
 *  the component is derived from its JComboBox heritage, whereas the choices
 *  actually presented to the user depend on the prevailing situation.
 *  <br><br>
 *  This class is designed under the assumption that it will be used in
 *  conjunction with an LhsCascade and an RhsLiteral to represent its left- and
 *  right-hand operands (though the latter is not actually required).  Indeed,
 *  the choices presented to the user at a given time are determined by the
 *  value of the LhsCascade.
 *  <br><br>
 *  By its nature, the OpMenu instance receives impulses from its associated
 *  LhsCascade, sometimes causing it to adjust its array of choices.  There is
 *  also an interface, OpListener, of which implementors can register to be
 *  notified of events on the OpMenu itself.  However, even when no change
 *  in the component is observed, impulses flowing from the LhsCascade are
 *  passed along to any listeners, indicating a change in the left-hand side.
 *  <br><br>
 *  Incidentally, since the RhsLiteral passes along events in the same fashion,
 *  most listeners will register with the RhsLiteral, and not with the OpMenu
 *  directly.  The notable exception, of course, is the RhsLiteral itself.
 */
public class OpMenu extends JComboBox implements ItemListener {
  // the Frame that spawned this element
  private RuleEditPane editor = null;

  // A place to store all the operators drawn from the database.  Not all of
  // these will be in use at any given time.
  private static Vector operators = null;

  // Retrieve the operators from the database via the Servlet
  private void loadOperators () {
    if (operators == null)
      operators = (Vector) editor.serverRequest("OPERATOR_TABLE", null);
  }

  // A mapping from String choices to the QRuleOperators they represent
  private Hashtable uiOptIndex = new Hashtable();

  // The associated LhsCascade Object and a String representation of its "type"
  private LhsCascade left = null;
  private String leftHandType = "";

  /**
   *  Retrieve the LhsCascade associated with this OpMenu
   *  @return the left-hand operand's GUI representation
   */
  public LhsCascade getLeft () {
    return left;
  }

  /**
   *  Construct this OpMenu as the operator in a test with the specified
   *  left-hand-side.  The initial value of the operator is also supplied,
   *  but may be null, indicating that a default value should be found.
   *  @param left the left operand
   *  @param op the value represented by this OpMenu
   *  @param theFrame the RuleFrame that spawned this OpMenu instance
   */
  public OpMenu (LhsCascade lhs, QRuleOperator op, RuleEditPane p) {
    super();
    editor = p;

    setBackground(RuleEditPane.ruleElementColor);
    setForeground(RuleEditPane.ruleTextColor);
    //setFont(RuleEditPane.elementFont);
    setFont(new Font("Dialog", Font.PLAIN, 12)); //had to make this font different
      //because the original font, Arial, was not properly rendering some of the
      //mathematical operators

    loadOperators();
    addItemListener(this);
    left = lhs;
    left.addLhsListener(lhsEar);
    QRuleAccessorOperand leftOp = (QRuleAccessorOperand) left.getSelectedItem();
    reset();
    if (op != null) {
      setSelectedItem(op);
    }
    cancelOpEvents = false;
  }

  // add an operator's UI-name as an item to the JComboBox ancestry and
  // associate its name with itself in the Hashtable index
  private void addAndIndexItem (QRuleOperator q) {
    addItem(q);
    uiOptIndex.put(q.getUiName(), q);
  }

  // Go through the list of operators and find the ones whose left-hand types
  // are compatible with the current value (and type) of the left-hand-side
  private void COLLECT_MATCHES () {
    loadOperators();

    String matchType = null;
    if (leftHandType.startsWith("Integer") ||
        leftHandType.startsWith("Float") ||
        leftHandType.startsWith("Currency"))
    {
      matchType = "number";
    }
    else if (leftHandType.startsWith("String")) {
      matchType = "String";
    }
    else {
      matchType = leftHandType;
    }

    Enumeration enu = operators.elements();
    while (enu.hasMoreElements()) {
      QRuleOperator qrop = (QRuleOperator) enu.nextElement();
      String lht = qrop.getOperand1Type();

      // determine if the operator is appropriate for the left-hand operand
      boolean matchFound =
      (
        // this is the generic case, where like matches like, and "anything"
        // matches anything besides numbers
        (
          lht.equalsIgnoreCase(matchType)
          ||
          (lht.equals("anything") && !matchType.equals("number"))
        )
        &&
        // exclude some special cases
        // the only operator used on booleans is equality
        !(matchType.equals("boolean") && !qrop.getJessName().equals("eq"))
        &&
        // for Customer, Item, Date, and Address types, only those operators
        // that are specifically designed for them are used
        (qrop.getOperand1Type().equals(matchType) ||
          !(matchType.equals("CustomerType") || matchType.equals("ItemType") ||
            matchType.equals("PhysicalAddressType") ||
            matchType.equals("Date")))
        &&
        // the only operator used on Proposal Type is equality
        !(matchType.equals("ProposalType") && !qrop.getJessName().equals("eq"))
      );

      if (matchFound)
        addAndIndexItem(qrop);
    }
  }

  // Press the proverbial "reset" button
  // Reconfigure this component to accomodate its environment
  private void reset () {
    String newLeftHandType =
      ((QRuleAccessorOperand) left.getSelectedItem()).getUiType();
    if (newLeftHandType.equals(leftHandType)) {
      // Note:  the following invocation of fireOpListeners is found in this
      // conditional block because if the types don't match, an event will be
      // generated in the "itemStateChanged" method
      fireOpListeners((QRuleOperator) getSelectedItem());
      return;
    }
    leftHandType = newLeftHandType;
    removeAllItems();
    uiOptIndex.clear();
    COLLECT_MATCHES();
    invalidate();
  }

  // Support for the OpListeners
  private boolean cancelOpEvents = true;
  private Vector opListeners = new Vector();

  /**
   *  Register a listener interested in knowing about changes in this component
   *  @param cml the OpListener being added
   */
  public void addOpListener (OpListener cml) {
    opListeners.addElement(cml);
  }

  /**
   *  Unregister a listener no longer interested in what happens to this
   *  component
   *  @param cml the OpListener being removed
   */
  public void removeOpListener (OpListener cml) {
    opListeners.removeElement(cml);
  }

  // notify any interested listeners of an event
  private void fireOpListeners (QRuleOperator op) {
    if (cancelOpEvents) return;
    Enumeration enu = opListeners.elements();
    while (enu.hasMoreElements())
      ((OpListener) enu.nextElement()).changeInOp(op);
  }

  // an ear for listening to changes in the left-hand side, so that appropriate
  // adjustments can be made
  private class LhsEar implements LhsListener {
    public void changeInLhs (QRuleAccessorOperand opt) {
      adjustToLhs(opt);
    }
  }
  private LhsListener lhsEar = new LhsEar();

  // Called when the LHS changes
  public void adjustToLhs (QRuleAccessorOperand opt) {
    reset();
  }

  /**
   *  ItemListener implementation for handling ItemEvents originating in the
   *  JComboBox ancestry.
   *  @param ItemEvent the ItemEvent requiring attention
   */
  public void itemStateChanged (ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      fireOpListeners((QRuleOperator) e.getItem());
    }
  }

  /**
   *  Look up the QRuleOperator value of this element in the Hashtable using
   *  its name as the key and set it as the selected item.
   *  @param s the name of an operator
   */
  public void findSelection (String s) {
    setSelectedItem(uiOptIndex.get(s));
  }
}
