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
import org.cougaar.delta.util.qrule.logic.*;
import org.cougaar.delta.util.qrule.*;
import org.cougaar.delta.applet.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.io.*;

public class RuleEditPane extends JPanel {
// - - - - - - - Rule To Edit  - - - - - - - - - - - - - - - - - - - - - - - - -
  // "rule" is the underlying QRule for which this RuleFrame is the graphical
  // representation
  public QRule rule = null;

// - - - - - - - Static Constants  - - - - - - - - - - - - - - - - - - - - - - -
  // Define some colors and fonts for the GUI components
  public static Color ruleBgColor = BasicPanel.isiLightBlue;
  public static Color ruleElementColor = BasicPanel.isiLightBlue;
  public static Color ruleTextColor = Color.black;
  public static Color ruleErrorColor = Color.red;
  public static Color ruleEnglishColor = BasicPanel.isiBrown;

  // some fonts
  public static String fontFam = "Arial";
  public static Font labelFont = new Font(fontFam, Font.BOLD, 12);
  public static Font elementFont = new Font(fontFam, Font.PLAIN, 12);
  public static Font englishFont = new Font(fontFam, Font.ITALIC, 14);

  // Allowable character sets for the name field
  private static String nameChars =
    "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";
  private static String nameInitialChars =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

// - - - - - - - Back End Support  - - - - - - - - - - - - - - - - - - - - - - -
  // The applet that spawned this Rule Editor and the frame that contains it
  private RuleServerSupport backEnd = null;

  // Create a subclass of HeartbeatDaemon that calls pingServer at the given
  // intervals
  private class MyHeart extends HeartbeatDaemon {
    public MyHeart (long i) {
      super(i);
    }

    public boolean pulse () {
      return pingServer();
    }
  }

  // do this every time the HeartbeatDaemon calls its "pulse" method.  The
  // effect is to keep the HTTP session alive
  private boolean pingServer () {
    return backEnd.serverRequest("KEEP_SESSION", null) != null;
  }

  // pulsing heart to keep the HTTP session alive
  private HeartbeatDaemon heart = null;

  // a factory instance for constructing the buttons used in the GUI
  private static ImageButtonFactory buttonFactory = null;

  /**
   *  Retrieve a reference to the ImageButtonFactory instance, creating a
   *  default instance, if one had not already been provided.
   *  @return the ImageButtonFactory instance
   */
  public static ImageButtonFactory getButtonFactory () {
    if (buttonFactory == null)
      buttonFactory = new ImageButtonFactory();
    return buttonFactory;
  }

  /**
   *  Report whether or not a button factory already has been created for use
   *  by the rule editor GUI.
   *  @return true if a button factory is found; false otherwise
   */
  public static boolean hasButtonFactory () {
    return buttonFactory != null;
  }

  /**
   *  Set the factory to be used by the rule editor GUI for generating buttons.
   *  Whatever button factory was previously in use is lost.
   *  @param f the new button factory
   */
  public static void setButtonFactory (ImageButtonFactory f) {
    buttonFactory = f;
  }

// - - - - - - - GUI Elements  - - - - - - - - - - - - - - - - - - - - - - - - -
  // Text elements for informational display
  private JTextArea englishArea = null;
  private JTextField statusField = new JTextField();

  // a tabbed pane for the various cases
  private JTabbedPane mainPanel = null;

  // components for the rule's name and a selector for its action
  private RestrictedJTextField nameField = null;
  private JComboBox actionMenu = null;

  // checkboxes for the boolean states, "active" and "testing"
  private JCheckBox activeCheck = null;
  private JCheckBox testingCheck = null;

  // Some buttons for the footer:  "Add A Condition", "Add An Exception",
  // "Save", "Copy", and "Cancel"
  private JButton addClauseButton;
  private JButton addExceptionButton;
  private JButton saveButton;
  private JButton copyButton;
  private JButton cancelButton;

// - - - - - - - Listen For User Edits To The Rule Name  - - - - - - - - - - - -
  // This is a class of "Ears" linking the rule's name to the text GUI element
  private class RuleNameEar extends KeyAdapter {
    public void keyReleased (KeyEvent ke) {
      rule.setName(nameField.getText());
    }
  }

// - - - - - - - Listen For Changes On The Activation Checkbox - - - - - - - - -
  // a listener linking the "Active" checkbox to the rule's "active" flag
  private class ActivationChangeEar implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      rule.setActive(((JCheckBox) ae.getSource()).isSelected());
    }
  }

// - - - - - - - Listen For Changes In The Action Selector - - - - - - - - - - -
  // An inner class connecting the Rule Action selection box to the rule's
  // action property
  private class RuleActionListener implements ItemListener {
    public void itemStateChanged (ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        if (rule != null)
          rule.setAction((String) e.getItem());
      }
      recomputeEnglish();
    }
  }

// - - - - - - - Listen For Rule Conditions Being Added  - - - - - - - - - - - -
  // wire up the "Add A Condition" button to the appropriate method
  private class AddConditionListener implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      ((RuleCasePane) mainPanel.getSelectedComponent()).addClause(null);
      recomputeEnglish();
    }
  }

// - - - - - - - Listen For Rule Exceptions Being Added  - - - - - - - - - - - -
  // connect the "Add an Exception" button to the appropriate method
  private class AddExceptionListener implements ActionListener {
    public void actionPerformed (ActionEvent e) {
      addAnException(null);
    }
  }

  // Do the work of adding an exception clause corresponding to the given
  // logical test.  If no test is provided, a new one is created.
  private void addAnException (QRuleLogicalTest test) {
    String tabLabel = null;
    if (test != null)
      tabLabel = test.getName();
    else
      test = new QRuleLogicalTest(rule, rule.getTest(), QRuleTest.LOGICAL_NAND);

    if (tabLabel == null || tabLabel.length() == 0)
      tabLabel = "Exception";

    RuleCasePane p = new RuleCasePane(this, test, "Exception Case", true, true);

    mainPanel.addTab(tabLabel, null, p,
      BasicToolTip.lookupExplanation("rule", tabLabel));
    mainPanel.setSelectedComponent(p);
    recomputeEnglish();
  }

// - - - - - - - Listen For The User Closing The Editor  - - - - - - - - - - - -
  // Connect the cancel button to the respondToCancel method
  private class CancelListener implements ActionListener {
    public void actionPerformed (ActionEvent e) {
      respondToCancel();
    }
  }

  /**
   *  Do whatever is required by when the user presses the "Cancel" button.
   *  By default, nothing needs to be done here, since the parent RuleFrame
   *  handles the request by closing down and calling the editor's stop method.
   */
  protected void respondToCancel () {
  }

// - - - - - - - Listen For Requests To Delete An Exception  - - - - - - - - - -
  // the case delete listener listens for a request to delete an exception case
  private class CaseDeleteListener implements ActionListener {
    private RuleCasePane myCase;

    public CaseDeleteListener (RuleCasePane rcp) {
      myCase = rcp;
    }

    public void actionPerformed (ActionEvent e) {
      removeCase(myCase);
    }
  }

  /**
   *  Get from this frame an ear to listen for when a case is being deleted
   *  @param p the case to be deleted when the ear hears something
   *  @return a listener object
   */
  public ActionListener getCaseDeleteEar (RuleCasePane p) {
    return new CaseDeleteListener(p);
  }

  // do the work of deleting an exception case from the rule and GUI
  private void removeCase (RuleCasePane p) {
    mainPanel.remove(p);
    rule.getTest().removeOperand(p.getMainCondition());
    recomputeEnglish();
  }

// - - - - - - - Listen For Requests To Save The Rule  - - - - - - - - - - - - -
  // This inner class provides access to the respondToSave method, causing the
  // rule to be sent to the servlet, possibly to be saved.  Both the "Save" and
  // the "Save As" buttons are monitored by SaveListeners.  The "Save" button
  // should use the command "SAVE" and the "Save As" button should use the
  // command "COPY".
  private class SaveListener implements ActionListener {
    private String command = null;

    public SaveListener (String s) {
      command = s;
    }

    public void actionPerformed (ActionEvent e) {
      respondToSave(command);
    }
  }

  // Send the rule to the server to be saved.  Some error messages may be
  // returned as a consequence.
  private void respondToSave (String command) {
    setStatus("Saving Rule . . .");
    if (rule.getName().equals("")) {
      setErrorStatus("Could not save--the rule has no name.");
      return;
    }

    LogicalDomain logic = new LogicalDomain(rule);
    Vector logicErrors = logic.analyze();
    if (logicErrors.size() > 0) {
      if (!confirmLogic(logicErrors)) {
        setStatus("Save cancelled.");
        return;
      }
    }

    try {
      Object reply = backEnd.serverRequest(command, rule);
      String response = null;
      if (reply instanceof String) {
        response = (String) reply;
        if (response.startsWith("CREATED ")) {
          rule.setDatabaseId(Long.parseLong(response.substring(8)));
          System.out.println("Setting data base ID to " + response.substring(8));
          setStatus("New rule created.");
        }
        else if (response.equals("SAME_NAME")) {
          setErrorStatus("The new copy of this Rule must have a different name.");
        }
        else if (response.startsWith("FAILED ")) {
          setErrorStatus("A rule named \"" + response.substring(7) +
            "\" already exists--unable to overwrite.");
        }
        else if (response.startsWith("UPDATED")) {
          setStatus("Rule updated.");
        }
      }
      else if (reply instanceof Vector) {
        Vector replyVec = (Vector) reply;
        if (replyVec.size() > 0) {
          Enumeration enu = replyVec.elements();
          response = enu.nextElement().toString();
          if (response.equals("REFERENCE_NOT_FOUND")) {
            if (!confirmEntities(enu)) {
              setStatus("Save cancelled.");
              // backEnd.serverRequest("CANCEL_SAVE", null);
              return;
            }
            reply = backEnd.serverRequest("CONFIRM_SAVE", null);
            if (reply != null && reply instanceof String &&
                ((String) reply).startsWith("SAVED "))
            {
              response = (String) reply;
              rule.setDatabaseId(Long.parseLong(response.substring(6)));
              setStatus("Rule saved.");
            }
            else
              setErrorStatus("Server error--unable to save");
          }
        }
      }
      else {
        setErrorStatus("Unrecognized server response--try again.");
      }
    }
    catch (Exception e) {
      System.out.println("RuleFrame::respondToSave:  ERROR--" + e);
      e.printStackTrace();
    }
  }

  private boolean pollUser (String title, String message) {
    return
      JOptionPane.showConfirmDialog(
        this, message, title, JOptionPane.YES_NO_OPTION) ==
      JOptionPane.YES_OPTION;
  }

  private boolean confirmLogic (Vector v) {
    StringBuffer buf = new StringBuffer();
    buf.append("The rule appears to contain the following logical errors:\n");
    for (Enumeration e = v.elements(); e.hasMoreElements(); ) {
      buf.append("    ");
      buf.append(e.nextElement());
      buf.append('\n');
    }
    buf.append("Continue saving this rule as is?");

    return pollUser("Logical inconsistencies?", buf.toString());
  }

  private boolean confirmEntities (Enumeration entities) {
    StringBuffer buf = new StringBuffer();
    buf.append("The following referenced entities could not be found:\n");
    while (entities.hasMoreElements()) {
      QRuleComparison c = (QRuleComparison) entities.nextElement();
      String type = getReferenceType(c);
      String value = c.getOperand2().toString();
      buf.append("    " + type + " \"" + value + "\"\n");
    }
    buf.append("Continue saving this rule as is?");

    return pollUser("Nonexistent entities?", buf.toString());
  }

  // Convert the accessor name or type into a human-recognizable description
  private String getReferenceType (QRuleComparison c) {
    QRuleAccessorOperand left = (QRuleAccessorOperand) c.getOperand1();
    String name = left.getInternalName();
    String type = left.getUiType();
    if (name.equals("ContractID"))
      return "Contract";
    if (name.equals("NSN"))
      return "Item (NSN)";
    if (name.equals("MfgCAGEPN"))
      return "Item (CAGE/PN)";
    if (name.equals("CustomerDODAAC"))
      return "Customer";
    if (type.equals("PhysicalAddressType"))
      return "Geography:Region";
    if (name.equals("Item"))
      return "Item Group";
    if (name.equals("Customer"))
      return "Customer Group";
    return "Unknown Entity";
  }

// - - - - - - - English Sentence Generation - - - - - - - - - - - - - - - - - -
  // This type of "Ear" listens for changes in the clauses and updates the
  // English statement accordingly
  private class ClauseEar implements ClauseListener {
    public void changeInClause () {
      recomputeEnglish();
    }
  }
  private ClauseEar clauseEar = new ClauseEar();

  /**
   *  Give objects that want this rule editor to be aware of their clauses
   *  an ear that listens for clause changes.
   *  @return the clause-listening ear
   */
  public ClauseListener getClauseEar () {
    return clauseEar;
  }

  // Do this when the rule has changed; i.e., reconstruct the English
  // translation of this rule
  private void recomputeEnglish () {
    System.out.println("RuleFrame::recomputeEnglish");
    if (rule != null) {
      englishArea.setText(rule.toEnglish());
      testingCheck.setSelected(rule.isTestRule());
    }
    revalidate();
  }

// - - - - - - - Constructors And Initialization - - - - - - - - - - - - - - - -
  /**
   *  Construct a new RuleEditPane instance.  In its current incarnation, a
   *  rule editor depends on the Applet that spawned it for its ability to
   *  communicate with the server.
   *  @param theApplet the RuleEditApplet associated with this rule editor
   */
  public RuleEditPane (RuleServerSupport rss) {
    backEnd = rss;

    // initialize the GUI
    try  {
      initGui();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  // Component initialization
  private void initGui () throws Exception  {
    setBackground(ruleBgColor);
    VerticalLayout lay = new VerticalLayout(this);

    // header components
    lay.add(configureEnglish(), 100, -1, VerticalLayout.RELAX);
    lay.add(configureNameActiveRow());
    lay.add(configureActionTestRow());

    // clause editor
    lay.add(configureMainPanel(), 100, -1, VerticalLayout.STRETCH);

    // footers
    lay.add(configureButtonPanel());
    lay.add(configureStatusBar());
  }

  private JComponent configureEnglish () {
    englishArea = new JTextArea();
    englishArea.setBorder(
      BorderFactory.createMatteBorder(10, 20, 20, 20, ruleBgColor));
    englishArea.setEditable(false);
    englishArea.setFont(englishFont);
    englishArea.setBackground(ruleEnglishColor);
    englishArea.setLineWrap(true);
    englishArea.setWrapStyleWord(true);
    return englishArea;
  }

  private JComponent configureNameActiveRow () {
    JLabel nameLabel = new JLabel("Rule Name:  ");
    nameLabel.setFont(labelFont);
    nameLabel.setToolTipText(BasicToolTip.lookupExplanation("rule","Rule Name"));
    nameLabel.setForeground(RuleEditPane.ruleTextColor);

    nameField = new RestrictedJTextField();
    nameField.setColumns(30);
    nameField.setCharSet(nameChars);
    nameField.setInitialsSet(nameInitialChars);
    nameField.setFont(elementFont);
    nameField.addKeyListener(new RuleNameEar());
    nameField.setBackground(ruleElementColor);

    activeCheck = new JCheckBox("Active");
    activeCheck.setToolTipText(BasicToolTip.lookupExplanation("rule","Active"));
    activeCheck.setOpaque(false);
    activeCheck.addActionListener(new ActivationChangeEar());

    JPanel p = new JPanel();
    p.setOpaque(false);
    RowLayout lay = new RowLayout(p);
    lay.setSpaceParameters(2, 2, 5, 5, 4, 20);
    lay.addLeft(nameLabel);
    lay.addLeft(nameField);
    lay.addRight(activeCheck);
    return p;
  }

  private JComponent configureActionTestRow () {
    JLabel actionLabel = new JLabel("Action:  ");
    actionLabel.setToolTipText(BasicToolTip.lookupExplanation("rule","Rule Action"));
    actionLabel.setFont(labelFont);
    actionLabel.setForeground(RuleEditPane.ruleTextColor);

    actionMenu = new JComboBox();
    actionMenu.setFont(elementFont);
    actionMenu.addItemListener(new RuleActionListener());
    actionMenu.setBackground(ruleElementColor);
    // populate the action menu with the appropriate choices
    actionMenu.addItem(QRule.NEGATIVE);
    actionMenu.addItem(QRule.POSITIVE);

    testingCheck = new JCheckBox("Testing");
    testingCheck.setToolTipText(BasicToolTip.lookupExplanation("rule", "Test Rule"));
    testingCheck.setOpaque(false);
    testingCheck.setEnabled(false);

    JPanel p = new JPanel();
    p.setOpaque(false);
    RowLayout lay = new RowLayout(p);
    lay.setSpaceParameters(2, 2, 5, 5, 4, 20);
    lay.addLeft(actionLabel);
    lay.addLeft(actionMenu);
    lay.addRight(testingCheck);
    return p;
  }

  private JComponent configureButtonPanel () {
    ImageButtonFactory f = getButtonFactory();

    addClauseButton = f.getButton("addACondition");
    addClauseButton.addActionListener(new AddConditionListener());

    addExceptionButton = f.getButton("addAnException");
    addExceptionButton.addActionListener(new AddExceptionListener());

    saveButton = f.getButton("save");
    saveButton.addActionListener(new SaveListener("SAVE"));

    copyButton = f.getButton("saveAsNewRule");
    copyButton.addActionListener(new SaveListener("COPY"));

    cancelButton = f.getButton("cancel");
    cancelButton.addActionListener(new CancelListener());

    BasicPanel p = new BasicPanel();
    RowLayout lay = new RowLayout(p);
    lay.addCenter(addClauseButton);
    lay.addCenter(addExceptionButton);
    lay.addCenter(saveButton);
    lay.addCenter(copyButton);
    lay.addCenter(cancelButton);
    return p;
  }

  private JComponent configureStatusBar () {
    statusField = new JTextField();
    statusField.setFont(labelFont);
    statusField.setEditable(false);
    statusField.setBackground(ruleElementColor);
    return statusField;
  }

  private JComponent configureMainPanel () {
    mainPanel = new JTabbedPane();
    mainPanel.setTabPlacement(JTabbedPane.LEFT);
    mainPanel.setOpaque(false);
    mainPanel.setBorder(
      BorderFactory.createMatteBorder(1, 0, 0, 0, Color.black));
    return mainPanel;
  }

// - - - - - - - Public Methods  - - - - - - - - - - - - - - - - - - - - - - - -
  /**
   *  Pass on requests for info from the server to the RuleEditApplet, which
   *  maintains the connection to the Servlet.
   *  @param command the String command to be interpreted by the Servlet
   *  @param obj the object to send, if any, to the Servlet
   *  @return the Servlet's response to the forwarded request
   */
  public Serializable serverRequest (String command, Serializable obj) {
    return backEnd.serverRequest(command, obj);
  }

  /**
   *  Begin operations.  Currently, all this does is set up the HeartbeatDaemon
   *  to keep the session going while the editor is active.
   */
  public void start () {
    if (heart != null)
      heart.heartAttack();
    heart = new MyHeart(300000);
  }

  /**
   *  Desist operations.  This method should be called by the parent when it is
   *  being shut down for whatever reason.
   */
  public void stop () {
    if (heart != null) {
      heart.heartAttack();
      heart = null;
    }
  }

  /**
   *  Set the text displayed in the status bar of the editor.  It will be shown
   *  as normal message text.
   *  @param s the message to be displayed
   */
  public void setStatus (String s) {
    statusField.setForeground(ruleTextColor);
    statusField.setText("  " + s);
    try {
      statusField.paint(statusField.getGraphics());
    }
    catch (Exception bull) { }
  }

  /**
   *  Set text to be displayed in the status bar.  It will be shown as an error
   *  message (currently in red).
   *  @param s the message to be displayed
   */
  public void setErrorStatus (String s) {
    statusField.setForeground(ruleErrorColor);
    statusField.setText("  " + s);
    try {
      statusField.paint(statusField.getGraphics());
    }
    catch (Exception bull) { }
  }

  /**
   *  Add a listener to the "Cancel" button so that interested parties can
   *  react when the user requests the editor to close.
   *  @param ear the ActionListener for the "Cancel" button
   */
  public void addCancelListener (ActionListener ear) {
    cancelButton.addActionListener(ear);
  }

  /**
   *  Change the text displayed in the tab corresponding to the given rule case.
   *  Note:  due to the slowness of method JTabbedPane::setTitleAt(), this
   *  method is slow to return.  Consequently, it should not be called very
   *  often.
   *  @param pane the RuleCasePane whose title is being changed
   *  @param text the new title for the rule case
   */
  public void resetCaseTabLabel (RuleCasePane pane, String text) {
    int index = mainPanel.indexOfComponent(pane);
    if (text == null || text.length() == 0)
      text = "Exception";
    mainPanel.setTitleAt(index, text);
  }

  /**
   *  Load a rule into this RuleFrame for editing.
   *  @param q the QRule being loaded
   */
  public void loadRule (QRule q) {
    setStatus("Loading Rule . . . ");
    rule = q;

    // set up the gizmos in the header:
    // name, action, activation flag, and testing flag
    if (q.getName() == null)
      rule.setName("");
    else
      nameField.setText(q.getName());

    if (q.getAction() != null)
      actionMenu.setSelectedItem(q.getAction());
    else
      q.setAction(actionMenu.getSelectedItem().toString());

    activeCheck.setSelected(q.isActive());
    testingCheck.setSelected(q.isTestRule());

    // make sure the rule satisfies the current formal constraints; i.e., its
    // root test is an "AND" operator, for starters.
    QRuleTest oldRoot = q.getTest();
    QRuleLogicalTest logic = null;
    if (oldRoot == null)
      new QRuleLogicalTest(q, null, QRuleTest.LOGICAL_AND);
    else if ((logic = oldRoot.getLogicalTest()) == null ||
        !logic.getLogicalOp().equals(QRuleTest.LOGICAL_AND))
    {
      QRuleLogicalTest t = new QRuleLogicalTest(q, null, QRuleTest.LOGICAL_AND);
      oldRoot.setParent(t);
    }

    // install the conditions for the base case
    mainPanel.addTab("Rule Conditions", null,
      new RuleCasePane(this, rule.getTest(), "Conditions", false, false),
      BasicToolTip.lookupExplanation("rule", "Rule Conditions"));

    // construct the exception ("NAND") clauses and their GUI elements
    for (Enumeration e = rule.getTest().getOperands(); e.hasMoreElements(); ) {
      QRuleLogicalTest qlt = ((QRuleTest) e.nextElement()).getLogicalTest();
      if (qlt != null && qlt.getLogicalOp().equals(QRuleTest.LOGICAL_NAND))
        addAnException(qlt);
    }

    mainPanel.setSelectedIndex(0);
    recomputeEnglish();
    setStatus("Rule Loaded.");
  }
}
