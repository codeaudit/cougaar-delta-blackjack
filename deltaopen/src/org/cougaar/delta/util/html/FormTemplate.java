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

package org.cougaar.delta.util.html;

import org.cougaar.delta.util.QueryTable;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;

/**
 *  The FormTemplate class is an attempt to standardize the approach to
 *  using HTML form data in servlets.  Once configured, an instance
 *  of this class knows how to populate itself with data from an
 *  HttpServletRequest as well as how to output an HTML form with the
 *  stored data presented as default entries in the form elements.  The
 *  instance can be stored as a session variable in the servlet, granting
 *  persistence to the form data.
 *  <br><br>
 *  The use of this class depends on the presence of suitable template files.
 *  Currently, there are three types of form elements supported:
 *  <ul>
 *    <li>
 *      Each member of the "fields" vector should be the name of a text form
 *      element.  In order for the form data to persist in repeated displays,
 *      its value attribute must be the name of the element surrounded by double
 *      hash signs (e.g., ...name=firstName type=text value="##firstName##"...).
 *      If the name is preceeded by "ENCODE:", as in "##ENCODE:firstName##",
 *      then HTML.encode is called on the data during output.
 *    </li>
 *    <li>
 *      Regular checkboxes use the "checkboxes" vector in much the same way.
 *      If the checkbox is to exhibit persistence, its HTML input tag must have
 *      an attribute consisting of the name of the element surrounded by double
 *      hashes (e.g., ...name=isAdmin type=checkbox ##isAdmin##...).
 *    </li>
 *    <li>
 *      Each key in the "choices" hashtable must be the name of a "select" or
 *      "radio" form element, and the corresponding value is a vector
 *      containing the names of the options.  For the persistence mechanism to
 *      work on "select" and "radio" elements, each option or radio checkbox
 *      must have an attribute consisting of the input's name, the option's
 *      value, and the string "SELECTED" separated by underscores, "_".  E.g.,
 *      (note:  Angle brackets omitted)
 *      <br>
 *      <br>SELECT name=tree
 *      <br>OPTION value="" ##tree__SELECTED## ...
 *      <br>OPTION value=oak ##tree_oak_SELECTED## ...
 *      <br>OPTION value=gingko ##tree_gingko_SELECTED## ...
 *    </li>
 *    <li>
 *      The "variableChoices" Hashtable is a special case similar to "choices",
 *      where the choices are prone to change, and are not stored in the HTML
 *      template file.  In all likelihood, this will replace the "choices"
 *      table, which remains in support of legacy code and "radio" elements.
 *      The required HTML support for this function includes a named "SELECT"
 *      tag enclosing the template tag of that name followed by "_OPTIONS",
 *      e.g., "##tree_OPTIONS##".
 *    </li>
 *    <li>
 *      The "multiselects" hashtable works much like "variableChoices", except
 *      that the corresponding HTML elements have multiple choices enabled.
 *      Due to a prohibition against using non-string values in
 *      TemplateProcessor, the selected values are stored in a String as a
 *      tab-separated list, rather than more robustly as a Vector or array
 *      (problems will occur if the choices contain any tab characters).
 *    </li>
 *  </ul>
 */
public class FormTemplate extends EncodingTemplate {
  private Vector fields = new Vector();
  private Vector checkboxes = new Vector();
  private Hashtable choices = new Hashtable();
  private Hashtable variableChoices = new Hashtable();
  private Hashtable variableChoiceAliases = new Hashtable();
  private Hashtable multiselects = new Hashtable();
  private Hashtable multiselectAliases = new Hashtable();
  private boolean variablesAssembled = true;
  private Map parameters = null;

  /**
   *  Create this FormTemplate on top of a suitable TemplateProcessor
   *  @param filename the name of the template file containing the HTML form
   */
  public FormTemplate (String filename) throws IOException {
    super(filename);
    parameters = new Hashtable();
  }

  /**
   *  Construct a new FormTemplate atop a suitable TemplateProcessor.  The
   *  provided Map is used to store values obtained from user submissions.
   *  In particular, the Map may be a QueryTable configured for queries on
   *  a certain factory.
   *  @param filename the name of the template file containing the HTML form
   *  @param map the table where search parameters are to be stored
   */
  public FormTemplate (String filename, Map map) throws IOException {
    super(filename);
    if (map == null)
      throw new NullPointerException("Parameter table was null");
    parameters = map;
  }

  /**
   *  Retrieve the table containing the user-submitted parameters.
   *  @return the parameter Map
   */
  public Map getParameters () {
    return parameters;
  }

  /**
   *  Get the parameter table as a QueryTable, if it is one, as it should be
   *  most of the time.
   */
  public QueryTable getQuery () {
    return (QueryTable) parameters;
  }

  /**
   *  Add support for an HTML TextField of the given name.  For this to work
   *  properly, the form in the template file should have an element of this
   *  name.
   *  @param f the name of the form element
   */
  public void addField (String f) {
    if (!fields.contains(f)) {
      fields.addElement(f);
    }
  }

  /**
   *  Retract support for a given HTML TextField.
   *  @param f the name of the form element
   */
  public void removeField (String f) {
    fields.removeElement(f);
  }

  /**
   *  Add support for a single choice of a selection box or suite of radio
   *  buttons with the given name.  The value of the choice supplied by the
   *  caller must agree with the one found in the HTML form template file.
   *  @param param the name of the corresponding form element
   *  @param choice the value associated with one of the element's choices
   */
  public void addChoice (String param, String choice) {
    Vector choiceVec = (Vector) choices.get(param);
    if (choiceVec == null) {
      choiceVec = new Vector();
      choices.put(param, choiceVec);
    }
    if (!choiceVec.contains(choice)) {
      choiceVec.addElement(choice);
    }
  }

  /**
   *  Add support for an array of choices to a given selection box or suite of
   *  radio buttons.  Each member of the array should be the value of one
   *  of the choices found in the HTML form in the template file.
   *  @param param the name of this form element
   *  @param choiceArray the array of values to choose from
   */
  public void addChoices (String param, String[] choiceArray) {
    Vector choiceVec = (Vector) choices.get(param);
    if (choiceVec == null) {
      choiceVec = new Vector();
      choices.put(param, choiceVec);
    }
    for (int i = 0; i < choiceArray.length; i++) {
      if (!choiceVec.contains(choiceArray[i]))
        choiceVec.addElement(choiceArray[i]);
    }
  }

  /**
   *  Remove the Vector of choices supported by this FormTemplate for the named
   *  HTML form element
   *  @param param name of the form element no longer to be supported
   */
  public void removeChoices (String param) {
    choices.remove(param);
  }

  /**
   *  Add support for a single choice in an HTML selection box, where the
   *  choices are variable, as opposed to statically specified in the template
   *  file.
   *  @param param the name of the SELECT element
   *  @param choice the value of the choice
   */
  public void addVariableChoice (String param, String choice) {
    Vector choiceVec = (Vector) variableChoices.get(param);
    if (choiceVec == null) {
      choiceVec = new Vector();
      variableChoices.put(param, choiceVec);
      variableChoiceAliases.put(param, new Hashtable());
    }
    if (!choiceVec.contains(choice)) {
      choiceVec.addElement(choice);
    }
    variablesAssembled = false;
  }

  /**
   *  Add support for a single choice in an HTML selection box, where the
   *  choices are variable, as opposed to statically specified in the template
   *  file.
   *  @param param the name of the SELECT element
   *  @param choice the value of the choice
   *  @param alias the GUI representation of the choice
   */
  public void addVariableChoice (String param, String choice, String alias) {
    Vector choiceVec = (Vector) variableChoices.get(param);
    Hashtable aliasTable = (Hashtable) variableChoiceAliases.get(param);
    if (choiceVec == null) {
      choiceVec = new Vector();
      variableChoices.put(param, choiceVec);
      aliasTable = new Hashtable();
      variableChoiceAliases.put(param, aliasTable);
    }
    if (!choiceVec.contains(choice)) {
      choiceVec.addElement(choice);
      aliasTable.put(choice, alias);
    }
    variablesAssembled = false;
  }

  /**
   *  Add support for an array of choices in an HTML selection box, where the
   *  choices are variable, as opposed to statically specified in the template
   *  file.
   *  @param param the name of the SELECT element
   *  @param choiceArray the values of the choices
   */
  public void addVariableChoices (String param, String[] choiceArray) {
    Vector choiceVec = (Vector) variableChoices.get(param);
    if (choiceVec == null) {
      choiceVec = new Vector();
      variableChoices.put(param, choiceVec);
    }
    for (int i = 0; i < choiceArray.length; i++) {
      if (!choiceVec.contains(choiceArray[i]))
        choiceVec.addElement(choiceArray[i]);
    }
    variablesAssembled = false;
  }

  /**
   *  Add support for an array of choices in an HTML selection box, where the
   *  choices are variable, as opposed to statically specified in the template
   *  file.
   *  @param param the name of the SELECT element
   *  @param choiceArray the values of the choices
   *  @param aliases the GUI representations of the choices
   */
  public void addVariableChoices (
      String param, String[] choiceArray, String[] aliases)
  {
    Vector choiceVec = (Vector) variableChoices.get(param);
    Hashtable aliasTable = (Hashtable) variableChoiceAliases.get(param);
    if (choiceVec == null) {
      choiceVec = new Vector();
      variableChoices.put(param, choiceVec);
      aliasTable = new Hashtable();
      variableChoiceAliases.put(param, aliasTable);
    }
    for (int i = 0; i < choiceArray.length && i < aliases.length; i++) {
      choiceVec.addElement(choiceArray[i]);
      aliasTable.put(choiceArray[i], aliases[i]);
    }
    variablesAssembled = false;
  }

  //Vector "codesDisplayed" contains a list of the form elements that want to have codes
  //  displayed in addition to aliases.
  private Vector codesDisplayed = new Vector();

  /**
   *  Add support for an array of choices in an HTML selection box, where the
   *  choices are variable, as opposed to statically specified in the template
   *  file. This method allows the caller to register the form element as one
   *  that wants to display both the code and the alias by setting a boolean
   *  flag, which allows the aliases to be displayed in the selection box with
   *  the codes.
   *  @param param the name of the SELECT element
   *  @param choiceArray the values of the choices
   *  @param aliases the GUI representations of the choices
   *  @param displayCodes a flag which, when set, allows the aliases to be
   *  displayed in the selection box with the codes
   */
  public void addVariableChoices (
      String param, String[] choiceArray, String[] aliases, boolean displayCodes) {

    codesDisplayed.addElement(param);
    addVariableChoices(param, choiceArray, aliases);
  }

  /**
   *  Remove the Vector of variable choices and its associated table of aliases
   *  supported by this FormTemplate for the named HTML form element
   *  @param param name of the form element no longer to be supported
   */
  public void removeVariableChoices (String param) {
    variableChoices.remove(param);
    variableChoiceAliases.remove(param);
  }

  /**
   *  Initialize this FormTemplate so that the choices added to the
   *  variableChoices and multiselects elements are displayed when generateHtml
   *  is called.  This function is automatically performed by importForm, and,
   *  when necessary, from generateHtml, so it normally does not need to be
   *  called from the outside.
   */
  private void assembleVariableChoices () {
    if (variablesAssembled)
      return;

    // handle the variableChoices
    Enumeration keys = variableChoices.keys(); //keys contains the names of the HTML form elements
    while (keys.hasMoreElements()) {
      String name = (String) keys.nextElement();
      String selection = (String) parameters.get(name);
      StringBuffer buf = new StringBuffer();
      Vector options = (Vector) variableChoices.get(name); //Vector options contains all the codes
      Hashtable aliases = (Hashtable) variableChoiceAliases.get(name); //aliases is hashtable: key is code, value is alias
      Enumeration ops = options.elements();  //ops contains all the codes
      while (ops.hasMoreElements()) {
        String op = (String) ops.nextElement();
        String alias = null;
        String selected = "";
        if (aliases == null || (alias = (String) aliases.get(op)) == null) //if there is no alias for this code
          alias = op; //the alias is set to be same as the code
        if ((selection != null) && selection.equals(op))
          selected = " selected";
        String spaces = "        "; //just for formatting
        if(codesDisplayed.contains(name)) //if we want codes to appear in the selection box w/ the aliases
          buf.append(
            "<option value=\"" + op + "\" " + selected + ">" + op + spaces + alias + "\n");
        else  //no codes, just aliases, displayed in the selection box
          buf.append(
            "<option value=\"" + op + "\" " + selected + ">" + alias + "\n");
      }
      put(name + "_OPTIONS", buf.toString());
    }

    // handle the multiselects
    keys = multiselects.keys();
    while (keys.hasMoreElements()) {
      String name = (String) keys.nextElement();
      StringBuffer buf = new StringBuffer();
      Vector options = (Vector) multiselects.get(name);
      Hashtable aliases = (Hashtable) multiselectAliases.get(name);
      String[] choices = (String[]) parameters.get(name);
      if (choices == null)
        choices = new String[0];
      Enumeration ops = options.elements();
      while (ops.hasMoreElements()) {
        String op = (String) ops.nextElement();
        String alias = null;
        if (aliases == null || (alias = (String) aliases.get(op)) == null)
          alias = op;
        String selected = "";
        for (int i = 0; i < choices.length; i++) {
          if (choices[i].equals(op)) {
            selected = " selected";
            break;
          }
        }
        buf.append("<option value=\"" + op + "\"" + selected);
        buf.append("> " + alias + "\n");
      }
      put(name + "_OPTIONS", buf.toString());
    }
    variablesAssembled = true;
  }

  /**
   *  Extend support for a choice in a selection element with the "multiple"
   *  attribute, indicating that zero, one, or many options may be selected
   *  simultaneously.
   *  @param param the name attribute of the multiple-selection HTML element
   *  @param choice the value attribute of the choice to be supported
   */
  public void addMultiChoice (String param, String choice) {
    Vector choiceVec = (Vector) multiselects.get(param);
    if (choiceVec == null) {
      choiceVec = new Vector();
      multiselects.put(param, choiceVec);
    }
    if (!choiceVec.contains(choice)) {
      choiceVec.addElement(choice);
    }
    variablesAssembled = false;
  }

  /**
   *  Extend support for a choice in a multiple selection HTML element with a
   *  display alias separate from the internal value.
   *  @param param the name of the HTML element
   *  @param choice the internal value of the choice
   *  @param alias the alias displayed to the user
   */
  public void addMultiChoice (String param, String choice, String alias) {
    Vector choiceVec = (Vector) multiselects.get(param);
    Hashtable aliasTable = (Hashtable) multiselectAliases.get(param);
    if (choiceVec == null) {
      choiceVec = new Vector();
      multiselects.put(param, choiceVec);
      aliasTable = new Hashtable();
      multiselectAliases.put(param, aliasTable);
    }
    if (!choiceVec.contains(choice)) {
      choiceVec.addElement(choice);
      aliasTable.put(choice, alias);
    }
    variablesAssembled = false;
  }

  /**
   *  Add support for an array of choices in a multiple selection HTML element.
   *  @param param the name attribute of the multiple-selection HTML element
   *  @param choices the values to be presented by the HTML element
   */
  public void addMultiChoices (String param, String[] choices) {
    for (int i = 0; i < choices.length; i++) {
      addMultiChoice(param, choices[i]);
    }
  }

  /**
   *  Add support for an array of choices in a multiple selection HTML element,
   *  with the provided display aliases.
   *  @param param the name attribute of the multiple-selection HTML element
   *  @param choices the values being selected
   *  @param aliases the displayed names of the choices
   */
  public void addMultiChoices (
      String param, String[] choices, String[] aliases)
  {
    for (int i = 0; i < choices.length; i++) {
      if (i < aliases.length)
        addMultiChoice(param, choices[i], aliases[i]);
      else
        addMultiChoice(param, choices[i]);
    }
  }

  /**
   *  Retract support for a multiple-selection HTML element.  All choices
   *  previously supported are dropped.
   *  @param param the name of the element no longer to be supported
   */
  public void removeMultiChoices (String param) {
    multiselects.remove(param);
  }

  /**
   *  Add support for a checkbox appearing in the HTML form with the given name.
   *  @param c name attribute of the checkbox
   */
  public void addCheckbox (String c) {
    if (!checkboxes.contains(c)) {
      checkboxes.addElement(c);
    }
  }

  /**
   *  Add support for an array of checkboxes appearing in the HTML form with
   *  the given names.
   *  @param c an array of names
   */
  public void addCheckboxes (String[] c) {
    for (int i = 0; i < c.length; i++)
      addCheckbox(c[i]);
  }

  /**
   *  Revoke support for the named checkbox
   *  @param c name attribute of the checkbox to be shunned
   */
  public void removeCheckbox (String c) {
    checkboxes.removeElement(c);
  }

  /**
   *  Read form data from an HttpServletRequest and store the information
   *  in this FormTemplate's Hashtable ancestry.  When this is done, the
   *  FormTemplate is ready to generate an HTML representation of itself
   *  with the newly-read values as the initial entries in its form elements
   *  @param r the HttpServletRequest from which form data is taken
   */
  public void importForm (HttpServletRequest r) {
    // First, get data from the text fields
    Enumeration elts = fields.elements();
    while (elts.hasMoreElements()) {
      String name = (String) elts.nextElement();
      String value = r.getParameter(name);
      if (value != null) {
        parameters.put(name, value);
        put(name, value);
      }
      else {
        parameters.remove(name);
        remove(name);
      }
    }

    // Second, get data from the checkboxes
    elts = checkboxes.elements();
    while (elts.hasMoreElements()) {
      String name = (String) elts.nextElement();
      String value = r.getParameter(name);
      if (value != null) {
        parameters.put(name, value);
        put(name, "checked selected");
      }
      else {
        parameters.remove(name);
        remove(name);
      }
    }

    // Third, get data from the selection boxes and radio buttons
    Enumeration keys = choices.keys();
    while (keys.hasMoreElements()) {
      String name = (String) keys.nextElement();
      String value = r.getParameter(name);
      if(value == null){
        value = "";
      }
      parameters.put(name, value);
      Vector options = (Vector) choices.get(name);
      Enumeration ops = options.elements();
      while (ops.hasMoreElements()) {
        String op = (String) ops.nextElement();
        if (op.equals(value)) {
          put(name + "_" + op + "_SELECTED", "selected checked");
        }
        else {
          remove(name + "_" + op + "_SELECTED");
        }
      }
    }

    // Do something similar for the variable selection boxes
    keys = variableChoices.keys();
    if (keys.hasMoreElements()) {
      variablesAssembled = false;
    }
    while (keys.hasMoreElements()) {
      String name = (String) keys.nextElement();
      String value = r.getParameter(name);
      if (value != null) {
        parameters.put(name, value);
      }
      else {
        parameters.remove(name);
      }
    }

    // Finally, get data from the multi-selection boxes
    keys = multiselects.keys();
    if (keys.hasMoreElements()) {
      variablesAssembled = false;
    }
    while (keys.hasMoreElements()) {
      String name = (String) keys.nextElement();
      Vector options = (Vector) multiselects.get(name);
      String[] paramVals = r.getParameterValues(name);
      if (paramVals != null) {
        parameters.put(name, paramVals);
      }
      else {
        parameters.remove(name);
      }
    }
  }

  /**
   *  Explicitly select one of the entries in a variable choice selection.
   *  Note that assembleVariableChoices() must be called after this and before
   *  generating HTML.  Under normal usage patterns, this is handled
   *  automatically.
   *  @param widgetName the name of the variable choice
   *  @param selectedChoice the value associated with the choice to be selected
   */
  public void setVariableChoiceSelection (
      String widgetName, String selectedChoice)
  {
    if(widgetName == null)
      widgetName = "";
    if(selectedChoice == null)
      selectedChoice = "";
    parameters.put(widgetName, selectedChoice);
    variablesAssembled = false;
  }

  /**
   *  Insert a parameter value into the table.  Note that this value will not
   *  be reflected in the HTML output, even if there is a GUI element for it.
   *  @param name the name of the parameter
   *  @param value the value being inserted
   */
  public void putParameter (String name, Object value) {
    parameters.put(name, value);
  }

  /**
   *  Generate an HTML representation of this form.  Mainly, this involves
   *  calling on the process method of its TemplateProcessor ancestry
   *  @param o a PrintWriter to take the output
   */
  public void generateHtml (PrintWriter o) {
    if (!variablesAssembled)
      assembleVariableChoices();

    o.println(process().toString());
    o.flush();
  }
}
