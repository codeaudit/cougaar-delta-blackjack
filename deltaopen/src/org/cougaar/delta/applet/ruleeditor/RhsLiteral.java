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
import org.cougaar.delta.util.variant.*;
import org.cougaar.delta.applet.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.text.*;
import java.awt.*;

/**
 *  This class is designed to be a graphical representation of the right-hand
 *  side of a single test in a QRule.  Given the wide variety of different
 *  forms the test RHS can take, this component is polymorphic by nature, the
 *  better to accomodate whatever situation it finds itself in.
 *  <br><br>
 *  The design of the RhsLiteral class presumes that it will be used in
 *  conjunction with an LhsCascade and an OpMenu, whose values dictate the type
 *  of configuration to be assumed by the RhsLiteral.  Needless to say,
 *  instances of this class are not of much use on their own, and not likely
 *  to be used outside the ClausePanel class.
 *  <br><br>
 *  For objects interested in noticing state changes in an RhsLiteral, there is
 *  an interface, RhsListener, of which implementors can register with an
 *  RhsLiteral instance to be notified of events.  By its nature, the
 *  RhsLiteral receives impulses for events flowing from the associated OpMenu
 *  (adjusting itself, if necessary) and passes on the impulses to its own
 *  registered listeners even if no change in itself was observed.
 */
public class RhsLiteral extends JPanel implements ActionListener, ItemListener {
  // the Frame that spawned this element
  private RuleEditPane editor = null;

  // The other components associated with this rule clause right-hand side.
  // The RhsLiteral adjusts itself for compatibility with their values.
  private OpMenu op = null;
  private LhsCascade lhs = null;
  private String previousLhsUiName = "";

  // some of the usual GUI configuration stuff
  private Font delegatedFont = null;
  private Color delegatedColor = null;

  // A configuration state indicator, along with its possible values assigned
  // to heuristic names.  Each state is presented to the user using different
  // graphical elements.  As of the moment, the admissible states are:
  //  - FIELD:  a text field, which may have character set restrictions etc.
  //  - AREA:  a multiline text area; currently this is never used.
  //  - CHOICE:  options for the user to choose among
  //  - DOUBLE_CHOICE:  two selectables.  The first one's value determines the
  //      choices available in the second one
  //  - ACCESSOR:  a version of CHOICE where the options are accessor operands
  private int componentType = 0;
  private static final int FIELD = 1;
  private static final int CHOICE = 2;
  private static final int DOUBLE_CHOICE = 3;
  private static final int ACCESSOR = 4;

  // the layoutManager as a RowLayout
  private RowLayout myRowLayout = null;

  // character sets for restricting input in text fields
  private static final String numericDigit = "0123456789";
  private static final String decimalPoint = ".";
  private static final String capAlphaNumeric = numericDigit +
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String alphaNumeric = capAlphaNumeric +
    "abcdefghijklmnopqrstuvwxyz";
  private static final String tokenSeparator = ", ";

  private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
    "MM/dd/yyyy");

  // A mapping for the DOUBLE_CHOICE configuration.  The keys are the possible
  // values of the primary selection, and the Hashtable maps them to a Vector
  // containing the choices for the secondary selection.  Whenever a primary
  // selection is made by the user, the secondary component's contents are
  // updated accordingly.
  private Hashtable primaryToSecondaryMap = new Hashtable();

  // Some type information relating to this component's value
  private String type = null;
  private String literalType = "";
  private String lhsType = "";
  private int lhsDesiredLength = -1;

  // A flag indicating whether or not this element should use the ACCESSOR
  // configuration.  ACCESSOR mode is allowed only for some operand types
  private boolean isAccessorMode = false;

  // - - - - - - - GUI components - - - - - - - - - - - - - - - - - - - - - - -
  // A JCheckBox that allows the user to toggle between accessor mode and
  // normal mode; It only appears when accessor mode is permitted
  private JCheckBox accessorToggle = new JCheckBox();
  // A JButton for summoning the Contract ID browser box
  private JButton browserButton = RuleEditPane.getButtonFactory().getButton("browse");
  // A JComboBox for presenting the user with a simple choice
  private JComboBox choice = new JComboBox();
  // Another JComboBox for presenting the user with a compound choice
  // (as per the DOUBLE_CHOICE configuration)
  private JComboBox secondaryChoice = new JComboBox();
  // a one-line text field for user input
  private RestrictedJTextField oneLine = new RestrictedJTextField();

  /**
   *  Associate an OpMenu instance with this element.  The RhsLiteral class
   *  operates under the assumption that it is in the context of a rule clause
   *  with an OpMenu and an LhsCascade as the operator and left-hand side,
   *  respectively.
   *  @param m the OpMenu component
   */
  public void setOp (OpMenu m) {
    op = m;
  }

  /**
   *  Get a reference to this clause's Operator selection component
   *  @return the OpMenu
   */
  public OpMenu getOp () {
    return op;
  }

  /**
   *  Set the background color for the "rule elements" in this component
   *  @param c the new rule element background color
   */
  public void setRuleElementColor (Color c) {
    choice.setBackground(c);
    secondaryChoice.setBackground(c);
    oneLine.setBackground(c);
  }

  /**
   *  Set the foreground color to be used for "rule elements" in this component
   *  @param c the new foreground color
   */
  public void setRuleTextColor (Color c) {
    choice.setForeground(c);
    secondaryChoice.setForeground(c);
    oneLine.setForeground(c);
  }

  /**
   *  Set the font to be used for "rule elements" in this component
   *  @param f the new font
   */
  public void setRuleElementFont (Font f) {
    choice.setFont(f);
    secondaryChoice.setFont(f);
    oneLine.setFont(f);
  }

  // Implement the FIELD configuration
  private void TEXT_FIELD (
      String ini, boolean iniInv, String ch, boolean inv, boolean cap, int len)
  {
    oneLine.setInitialsSet(ini);
    oneLine.setInverseInitials(iniInv);
    oneLine.setCharSet(ch);
    oneLine.setInverseSet(inv);
    oneLine.setCapitalize(cap);
    if (len < 1 || len > 85)
      len = 85;
    oneLine.setMaxLength(len);

    componentType = FIELD;
    myRowLayout.removeAll();
    oneLine.setColumns(15);
    oneLine.setText("");
    myRowLayout.addLeft(oneLine);
  }

  private void TEXT_FIELD () {
    TEXT_FIELD(null, false, null, false, false, 85);
  }

  // Implement the CHOICE configuration with "True" and "False" as choices
  private void BOOLEAN_CHOICE () {
    componentType = CHOICE;
    myRowLayout.removeAll();
    choice.removeAllItems();
    choice.addItem("True");
    choice.addItem("False");
    myRowLayout.addLeft(choice);
  }

  // Implement the CHOICE configuration with a range of numbers as choices
  private void NUMBER_CHOICE (int n1, int n2) {
    componentType = CHOICE;
    myRowLayout.removeAll();
    choice.removeAllItems();
    for (int i = n1; i <= n2; i++)
      choice.addItem(String.valueOf(i));
    myRowLayout.addLeft(choice);
  }

  // Implement the DOUBLE_CHOICE configuration with the primary being the
  // Geographies in the system, and the secondary choices being its Regions
  private void REGION_CHOICE () {
    componentType = DOUBLE_CHOICE;
    myRowLayout.removeAll();
    choice.removeAllItems();
    primaryToSecondaryMap.clear();

    Vector geos = (Vector) editor.serverRequest("GEOGRAPHY NAMES", null);
    for (Enumeration enu = geos.elements(); enu.hasMoreElements(); ) {
      String geo = (String) enu.nextElement();
      choice.addItem(geo);
      primaryToSecondaryMap.put(geo, new RegionList(geo));
    }
    refreshSecondaryChoice((String) choice.getSelectedItem());

    myRowLayout.addLeft(choice);
    myRowLayout.addLeft(secondaryChoice);
  }

  // Implement the CHOICE configuration with "Depot" and "Vendor" and "Corporate Contract MRO" as choices
  private void DVD_DEPOT () {
    componentType = CHOICE;
    myRowLayout.removeAll();
    choice.removeAllItems();
    choice.addItem("Depot");
    choice.addItem("Vendor");
    choice.addItem("Corporate Contract MRO");
    myRowLayout.addLeft(choice);
  }

  // Implement the CHOICE configuration with choices obtained by passing the
  // given command String to the Servlet
  private void DB_POPULATED_CHOICE (String command) {
    componentType = CHOICE;
    myRowLayout.removeAll();
    choice.removeAllItems();
    Vector v = (Vector) editor.serverRequest(command, null);
    if (v != null) {
      Enumeration enu = v.elements();
      while (enu.hasMoreElements()) {
        choice.addItem(enu.nextElement());
      }
      myRowLayout.addLeft(choice);
    }
  }

  // Implement the ACCESSOR_CHOICE configuration with the choices being those
  // accessors that match the type of the left-hand side
  private void ACCESSOR_CHOICE (QRuleAccessorOperand lhs) {
    componentType = ACCESSOR;
    myRowLayout.removeAll();
    choice.removeAllItems();
    Enumeration enu = LhsCascade.getLhsOperands();
    while (enu.hasMoreElements()) {
      QRuleAccessorOperand q = (QRuleAccessorOperand) enu.nextElement();
      if (q.getUiType().equals(lhs.getUiType()) &&
          !q.getUiName().equals(lhs.getUiName()))
      {
        choice.addItem(q);
      }
    }
    myRowLayout.addLeft(choice);
  }

  // press the proverbial "reset" button
  // Taking into consideration the LhsCascade and OpMenu, reconfigure for
  // compatibility in the present setting.
  private void reset () {
    QRuleAccessorOperand left = (QRuleAccessorOperand) lhs.getSelectedItem();
    QRuleOperator o = (QRuleOperator) op.getSelectedItem();

    String newLhsType = left.getUiType();
    int newDesiredLength = -1;
    // for the moment, we don't care about string lengths
    if (newLhsType.startsWith("String")) {
      int l = newLhsType.indexOf("(");
      int r = newLhsType.indexOf(")");
      if (l > -1 && r > -1 && r > l) {
        try {
          newDesiredLength = Integer.parseInt(newLhsType.substring(l + 1, r));
        }
        catch (Exception e) { }
      }
      newLhsType = "String";
    }

    String newLiteralType = o.getOperand2Type();
    // if the operator doesn't uniquely specify a type, take a cue from the lhs
    if (newLiteralType.equals("anything") || newLiteralType.equals("number")) {
      newLiteralType = newLhsType;
    }
    // or if the operator demands a list, derive its type from the lhs
    // and remove length restrictions from the field
    else if (newLiteralType.equals("list of anything")) {
      newLiteralType = "List(" + newLhsType + ")";
      newDesiredLength = -1;
    }

    boolean newAccessorMode = accessorToggle.isSelected();
    // if the UI name and type of the operand hasn't changed, leave the value in place
    if (literalType.equals(newLiteralType) &&
        lhsType.equals(newLhsType) &&
        newDesiredLength == lhsDesiredLength &&
        previousLhsUiName.equals(left.getUiName()) &&
        !(newAccessorMode || isAccessorMode))
    {
      return;
    }
    literalType = newLiteralType;
    isAccessorMode = newAccessorMode;
    lhsType = newLhsType;
    lhsDesiredLength = newDesiredLength;
    previousLhsUiName = left.getUiName(); //update previousUiName

    // set the length limitation on the text field
    oneLine.setMaxLength(lhsDesiredLength);

    // these cases are immune to "accessor mode"
    if (literalType.startsWith("List("))
      TEXT_FIELD();
    else if (literalType.equalsIgnoreCase("CustomerGroup"))
      TEXT_FIELD();
    else if (literalType.equalsIgnoreCase("ItemGroupType"))
      TEXT_FIELD();
    else if (literalType.equalsIgnoreCase("Geography:Region"))
      REGION_CHOICE();
    else if (literalType.equalsIgnoreCase("boolean"))
      BOOLEAN_CHOICE();
    else if (left.getUiName().equalsIgnoreCase("priority"))
      NUMBER_CHOICE(1, 15);
    else if (left.getUiName().equalsIgnoreCase("IPG"))
      NUMBER_CHOICE(1, 3);
    else if (left.getUiName().equalsIgnoreCase("Contract ID"))
      TEXT_FIELD(null, false, capAlphaNumeric, false, true, 13);
    else if (left.getUiName().equalsIgnoreCase("NSN"))
      TEXT_FIELD(null, false, numericDigit, false, false, 13);
    else if (left.getUiType().startsWith("String"))
      TEXT_FIELD();
    else if (left.getUiType().equals("AdviceCodeType"))
      DB_POPULATED_CHOICE("ADVICE_CODE_TABLE");
    else if (left.getUiType().equals("DeliveryDaysCode"))
      DB_POPULATED_CHOICE("DELIVERY_DAYS_TABLE");
    else if (left.getUiType().equals("FMSSupportAllowed"))
      DB_POPULATED_CHOICE("FMS_SUPPORT_TABLE");
    else if (left.getUiType().equals("DebarredStatus"))
      DB_POPULATED_CHOICE("VENDOR_DEBARRED_TABLE");
    else if (left.getUiType().equals("DODAACType"))
      TEXT_FIELD(null, false, capAlphaNumeric, false, true, 6);
    else if (left.getUiType().equals("HazMatCodeType"))
      TEXT_FIELD();
    else if (left.getUiType().equals("PrimeVendorType"))
      TEXT_FIELD();
    else if (left.getUiType().equals("ProposalType"))
      DVD_DEPOT();
    else if (left.getUiType().equals("UIType"))
      // Replaced with selection element for UI codes
      // TEXT_FIELD();
      DB_POPULATED_CHOICE("UI_CODE_TABLE");
    else if (left.getUiType().equals("Float"))
      TEXT_FIELD(null, false, numericDigit + decimalPoint, false, false, -1);
      // as of now, meaningful comparisons between Float accessors aren't possible

    // accessor mode
    else if (accessorToggle.isSelected()) {
      ACCESSOR_CHOICE(left);
      // add a checkbox for switching between literal and accessor modes
      myRowLayout.addLeft(accessorToggle);
    }

    // these cases will be reached only if not in "accessor mode", but they
    // are not immune, so the switch is included
    else {
      if (left.getUiType().startsWith("Integer"))
        TEXT_FIELD(null, false, numericDigit, false, false, -1);
      else if (left.getUiType().startsWith("Currency"))
        TEXT_FIELD(null, false, numericDigit + decimalPoint, false, false, -1);
      else
        TEXT_FIELD();
      // add a checkbox for switching between literal and accessor modes
      myRowLayout.addLeft(accessorToggle);
    }

    // Add a browser button for the Contract IDs, etc., maybe.
    maybeAddBrowserButton(left.getUiName());

    revalidate();
  }

  private void maybeAddBrowserButton(String s) {
    browserButton.setActionCommand("GET LIST FOR " + s);
    if (s.equalsIgnoreCase("Contract ID") ||
        s.equalsIgnoreCase("Customer DODAAC") ||
        s.equalsIgnoreCase("Customer") ||
        s.equalsIgnoreCase("NSN") ||
        s.equalsIgnoreCase("Item"))
    {
      myRowLayout.addLeft(browserButton);
    }
  }

  // some ui types have parenthetical modifiers--find the base type
  private String typeBase (String s) {
    int n = s.indexOf("(");
    if (n > -1)
      return s.substring(0, n);
    else
      return s;
  }

  /**
   *  Set the value of this right-hand side.  First, detect the configuration
   *  state, and then try to set the active component to the given value.  If
   *  this is not possible, the request will be ignored or bashed in, as is
   *  appropriate.
   *  <br><br>
   *  In the case of a compound choice, the colon ':' is used as a field
   *  separator within the String representation
   *
   *  @param QRuleOperand q
   */
  public void setValue (QRuleOperand q) {
    if (q instanceof QRuleAccessorOperand) {
      choice.setSelectedItem(q);
    }
    else {
      Object val = ((QRuleLiteralOperand) q).getValue();
      if (val instanceof Boolean) {
        if (((Boolean) val).booleanValue())
          choice.setSelectedItem("True");
        else
          choice.setSelectedItem("False");
      }
      else if (val instanceof Vector) {
        StringBuffer buf = new StringBuffer();
        Enumeration enu = ((Vector) val).elements();
        if (enu.hasMoreElements()) {
          String vectorElement = (String)enu.nextElement();
          //Add double quotes around the element if it contains a comma or a space
          //to tell method parseStringList() that the words are a single element
          if(vectorElement.indexOf(',') != -1 || vectorElement.indexOf(' ') != -1){
            vectorElement = "\"" + vectorElement + "\"";
          }
          buf.append(vectorElement);
          while (enu.hasMoreElements()) {
            vectorElement = (String)enu.nextElement();
            if(vectorElement.indexOf(',') != -1 || vectorElement.indexOf(' ') != -1){
              vectorElement = "\"" + vectorElement + "\"";
            }
            buf.append(" " + vectorElement);
          }
        }
        oneLine.setText(buf.toString());
      }
      else if (val instanceof Date) {
        oneLine.setText(dateFormat.format((Date) val));
      }
      else {
        String s = q.toString();
        if (componentType == FIELD)
          oneLine.setText(s);
        else if (componentType == CHOICE)
          choice.setSelectedItem(s);
        else if (componentType == DOUBLE_CHOICE) {
          int k = s.indexOf(":");
          choice.setSelectedItem(s.substring(0, k));
          secondaryChoice.setSelectedItem(s.substring(1 + k));
        }
      }
    }
  }

  // note:  using colon as a field separator
  public QRuleOperand getOperandValue () {
    if (componentType == ACCESSOR)
      return (QRuleOperand) choice.getSelectedItem();

    // extract the literal data as a String from the relevant input element
    String s = null;
    if (componentType == FIELD){
      s = oneLine.getText();
    }else if (componentType == CHOICE) {
      Object c = choice.getSelectedItem();
      if (c != null)
        s = choice.getSelectedItem().toString();
      else
        s = "<<NONE>>";
    }
    else if (componentType == DOUBLE_CHOICE) {
      Object c = choice.getSelectedItem();
      Object c2 = secondaryChoice.getSelectedItem();
      s = (c != null ? c : "") + ":" + (c2 != null ? c2 : "");
    }

    // convert what we find to the appropriate type
    Object w = null;
    try {
      if (literalType.startsWith("Integer")) {
        w = new Long(s);
      }
      else if (literalType.equals("Float") || literalType.equals("Currency")) {
        w = new Double(s);
      }
      else if (literalType.startsWith("String")) {
        w = s;
      }
      else if (literalType.equals("boolean")) {
        w = new Boolean(s);
      }
      else if (literalType.equals("Date")) {
        w = dateFormat.parse(s);
      }
      else if (literalType.startsWith("List(")) {
        w = parseStringList(s); //turns String s into Vector w
      }
      else {
        w = s;
      }
    }
    catch (Exception oh_no) {
      w = s;
    }
    return new QRuleLiteralOperand(w);
  }

//  private Vector parseStringList(String s) {
//    Vector v = new Vector();
//    StringTokenizer tok = new StringTokenizer(s, " ,;");
//    while (tok.hasMoreTokens()) {
//      v.addElement(tok.nextToken());
//    }
//    return v;
//  }

  /**
   * Parses the text input from an RhsLiteral text box into a list of separate String
   * elements.  Places the Strings as list elements into a Vector for further
   * processing.  Any String enclosed in double quotes is interpreted as a single
   * list item, even if it contains spaces or commas (which are normally interpreted
   * as delimiters between list elements).
   * @param s The input String that represents items in a list
   * @return A Vector of Strings, each element of which is one list item
   */
  private Vector parseStringList(String s) {
    Vector v = new Vector();
    //Construct a StringTokenizer having space, comma, and semicolon as delimiters,
    //and returning the delimiters as tokens, too.  We need the delimiters as tokens
    //so we can include commas as part of the text when it is entered as a list element
    //(e.g., STAKE,WOOD).
    StringTokenizer tok = new StringTokenizer(s, " ,;", true);
    while (tok.hasMoreTokens()) {
      String tokenVal = tok.nextToken();
      //We don't care about the delimiters if they don't appear within double quotes
      if(tokenVal.equals(" ") || tokenVal.equals(",") || tokenVal.equals(";"))
        continue;
      //When we encounter a double quote, assemble everything that appears until
      //the ending double quote in a StringBuffer.
      if(tokenVal.startsWith("\"")){
        StringBuffer listElement = new StringBuffer(tokenVal.substring(1)); //eliminates the leading double quote
        while(!tokenVal.endsWith("\"")){
          if(tok.hasMoreTokens()){
            tokenVal = tok.nextToken();
            listElement.append(tokenVal);
          }else
            break;
        }
        if(listElement.length() > 0 && tokenVal.endsWith("\""))
          listElement.deleteCharAt(listElement.length() - 1); //eliminates the trailing double quote
        tokenVal = listElement.toString();
      }
      v.addElement(tokenVal.toUpperCase());

    }
    return v;
  }

  // This class listens for events on the Accessor Mode toggle
  private class AccessorModeEar implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      toggleAccessorMode();
    }
  }

  // This class responds to changes in the first of two choices, in case of
  // a compound choice
  private class PartialChoiceEar implements ItemListener {
    public void itemStateChanged (ItemEvent e) {
      if (e.getStateChange() == ItemEvent.SELECTED &&
          componentType == DOUBLE_CHOICE)
      {
        refreshSecondaryChoice((String) e.getItem());
      }
    }
  }

  /**
   *  Construct a new RhsLiteral instance.  The new object is initialized with
   *  the "right" operand provided as a right-hand-operand in the context of
   *  the operator and left-hand-operand provided.
   *  @param left the left-hand-operand
   *  @param qrop the operator
   *  @param right the operand represented by this RhsLiteral
   *  @param theFrame the Rule Editor of which this is a part
   */
  public RhsLiteral (
      LhsCascade left, OpMenu qrop, QRuleOperand right, RuleEditPane p)
  {
    super();

    setRuleElementColor(RuleEditPane.ruleElementColor);
    setRuleTextColor(RuleEditPane.ruleTextColor);
    setRuleElementFont(RuleEditPane.elementFont);

    editor = p;
    lhs = left;
    previousLhsUiName = lhs.getSelectedItem().getUiName();
    op = qrop;
    op.addOpListener(getOperatorEar());

    myRowLayout = new RowLayout(this, RowLayout.STRETCH);
    myRowLayout.setSpaceParameters(0, 0, 0, 0, 5, 0);
    accessorToggle.addActionListener(new AccessorModeEar());
    accessorToggle.setOpaque(false);
    browserButton.addActionListener(new BrowserButtonEar());
    oneLine.addKeyListener(getKeyEar());
    choice.addItemListener(this);
    choice.addItemListener(new PartialChoiceEar());
    secondaryChoice.addItemListener(this);

    setOpaque(false);
    if (right != null) {
      if (right instanceof QRuleAccessorOperand)
        accessorToggle.setSelected(true);
      reset();
      setValue(right);
    }
    else {
      reset();
    }
    cancelRhsEvents = false;
  }

  private void toggleAccessorMode () {
    reset();
    fireRhsListeners();
  }

  // In case of a compound choice configuration, update the choices in the
  // secondary component in response to a change in the first component
  private void refreshSecondaryChoice (String s) {
    if (componentType == DOUBLE_CHOICE) {
      secondaryChoice.removeAllItems();
      Object refreshAction = (s == null ? null : primaryToSecondaryMap.get(s));
      Vector secondaries = null;
      if (refreshAction instanceof Vector)
        secondaries = (Vector) refreshAction;
      else if (refreshAction instanceof ListMaker)
        secondaries = ((ListMaker) refreshAction).getList();

      if (secondaries != null && secondaries.size() > 0) {
        Enumeration enu = secondaries.elements();
        while (enu.hasMoreElements())
          secondaryChoice.addItem(enu.nextElement());
      }
      else
        fireRhsListeners();
    }
  }

  // This Ear listens for a keystroke--probably because the user is making a
  // change in one of the input fields
  private class KeyEar extends KeyAdapter {
    public void keyReleased (KeyEvent ke) {
      respondToKeystroke();
    }
  }
  private KeyListener keyEar = new KeyEar();

  // lend an ear for listening to keystrokes
  private KeyListener getKeyEar () {
    return keyEar;
  }

  // when a keystroke indicates that an input field has changed, notify
  // interested parties of the change
  private void respondToKeystroke () {
    fireRhsListeners();
  }

  // the class that listens for events on the corresponding QRuleOperator
  // and/or OpMenu
  private class OperatorEar implements OpListener {
    public void changeInOp (QRuleOperator q) {
      adjustToOperator(q);
    }
  }

  /**
   *  Get a sensor attuned to events on an OpMenu, presumably the one
   *  corresponding to the operator in the same QRuleTest of which this
   *  RhsLiteral represents the right-hand side.
   *  @return the OpListener for this component.
   */
  private OpListener getOperatorEar () {
    return new OperatorEar();
  }

  // Called when the operator changes
  private void adjustToOperator (QRuleOperator op) {
    reset();
    fireRhsListeners();
  }

  // - - - - - - - Contract ID Browser Stuff - - - - - - - - - - - - - - - - - -

  private Vector getBrowserContent (String command) {
    return (Vector) editor.serverRequest(command, null);
  }

  private class BrowserButtonEar implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      showBrowserBox(ae.getActionCommand());
    }
  }

  private class BrowserValueEar implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      synchronized (browserLock) {
        oneLine.setText(ae.getActionCommand());
        fireRhsListeners();
        browserFrame.setVisible(false);
        browserFrame.dispose();
        browserFrame = null;
      }
    }
  }

  private Object browserLock = new Object();
  private JFrame browserFrame = null;

  private void showBrowserBox (String command) {
    synchronized (browserLock) {
      if (browserFrame == null || !browserFrame.isShowing()) {
        if (browserFrame != null)
          browserFrame.dispose();
        OrderedListBrowser olb = new OrderedListBrowser(
          getBrowserContent(command), editor);
        olb.addSubmitListener(new BrowserValueEar());
        olb.mimicRestrictions(oneLine);
        browserFrame = olb;
      }
    }
  }

  // - - - - - - - End of Browser Stuff- - - - - - - - - - - - - - - - - - - - -

  // Support for RhsListeners
  private boolean cancelRhsEvents = true;
  private Vector RhsListeners = new Vector();

  /**
   *  Add a listener to those registered to recieve notice when this element
   *  experiences a change
   *  @param l the interested RhsListener
   */
  public void addRhsListener (RhsListener l) {
    RhsListeners.addElement(l);
  }

  /**
   *  Remove a listener from the list of those who are interested in events
   *  originating from this element.
   *  @param l the disinterested RhsListener
   */
  public void removeRhsListener (RhsListener l) {
    RhsListeners.removeElement(l);
  }

  // Send notification to interested parties that an change has been observed
  private void fireRhsListeners () {
    if (cancelRhsEvents) return;
    Enumeration enu = RhsListeners.elements();
    while (enu.hasMoreElements())
      ((RhsListener) enu.nextElement()).changeInRhs();
  }

  // ActionListener implementation
  public void actionPerformed(ActionEvent ae) {
    fireRhsListeners();
  }

  // ItemListener implementation
  public void itemStateChanged(ItemEvent ie) {
    if (ie.getStateChange() == ItemEvent.SELECTED &&
        (ie.getSource() == secondaryChoice || componentType != DOUBLE_CHOICE))
    {
      fireRhsListeners();
    }
  }

  // - - - - - - - Support for the automatic DB access stuff - - - - - - - - - -

  // common interface for all automatic list accessor classes
  private static interface ListMaker {
    public Vector getList ();
  }

  // this class of list accessors automatically fetches the Region names for
  // a particular Geography.  The Geography name is determined at creation.
  private class RegionList implements ListMaker {
    private String geo = null;

    public RegionList (String name) {
      if (name == null)
        throw new IllegalArgumentException("name can't be null");
      geo = name;
    }

    public Vector getList () {
      return (Vector) editor.serverRequest("GET REGIONS", geo);
    }
  }
}
