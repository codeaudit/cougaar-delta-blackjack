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

import org.cougaar.delta.util.variant.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;

/**
 *  Provides a persistent selection model for {@link UniversalTable}s that employ
 *  checkboxes for user selections.  Every time a new page is requested
 *  (especially requests to sort the table, page through it, or perform an
 *  action on the selected elements), any changes the user has made in the
 *  selections are cached for future reference.  If the table is redisplayed,
 *  selected rows are marked as such in the output.  This class also provides
 *  an iterator for conveniently traversing the set of selected rows when an
 *  operation is to be performed on them.
 */
public class UniversalTableSelect {
  // the UniversalTable in question
  private UniversalTable table = null;

  // the selected elements are cached in this hashtable with the keys being
  // stringized values of the uniqueIdField (q.v.)
  private Hashtable selections = new Hashtable();

  // this is the name of the field used by the beans as a unique identifier
  private String uniqueIdField = null;

  // Some String names of things.  Here we have HTTP parameter names
  // corresponding to adding selections, removing selections, and inverting
  // the entire table.  Also below is the prefix appearing on all JavaScript
  // related to this component.  At the moment, these Strings are essentially
  // constant, but there may be need to allow them to vary in future
  // applications.
  private String myAddCommand = "SELECT";
  private String myRemoveCommand = "DESELECT";
  private String myInvertFlag = "INVERT_CHOICE";
  private String myComponentNamePrefix = "CHOICE_";

  /**
   *  Report on the string used by this selection model to identify HTML and
   *  JavaScript constructs that belong to it.
   *  @return the identifying prefix String
   */
  public String getPrefix () {
    return myComponentNamePrefix;
  }

  /**
   *  Construct this selection model for a {@link UniversalTable}.  Upon creation,
   *  this selector is configured to operate on a particular table and to
   *  use a particular field as caching keys.
   *  <br><br>
   *  Note that instances of this class are created automatically by
   *  {@link UniversalTable}s when appropriate, and it is unlikely that instances
   *  created outside that context will be of any use.
   *
   *  @param ut the {@link UniversalTable} that supports user selections
   *  @param uidField the name of a field that uniquely identifies rows in the table
   */
  public UniversalTableSelect (UniversalTable ut, String uidField) {
    table = ut;
    uniqueIdField = uidField;
    try {
      jsTemplate =  new TemplateProcessor(templateString, true);
    }
    catch (Exception oh_no) {
      System.out.println(
        "Failed to set up selection model on UniversalTable \"" +
        table.getUniversalTableId() + "\"");
      oh_no.printStackTrace();
    }
  }

  /**
   *  Wipe the selection model clean.
   */
  public void clearSelection () {
    selections.clear();
  }

  /**
   *  Invert the set of selected elements with respect to the set of all
   *  rows in the associated {@link UniversalTable}.
   */
  public void invertSelection () {
    DataWrapper[] rows = table.getRows();
    for (int i = 0; i < rows.length; i++) {
      String key = rows[i].getProperty(uniqueIdField).toString();
      if (selections.get(key) != null)
        selections.remove(key);
      else
        selections.put(key, "Y");
    }
  }

  /**
   *  Add the given elements to the set of selected elements.  Elements
   *  previously selected will still be selected after this operation.
   *  @param check an array of String keys to be registered as selected
   */
  public void checkSelections (String[] check) {
    if (check != null) {
      for (int i = 0; i < check.length; i++) {
        selections.put(check[i], "Y");
      }
    }
  }

  /**
   *  Remove the given elements from the set of selected elements.  Elements
   *  that were not selected before this operation will still not be selected
   *  afterward.
   *  @param uncheck array of String keys no longer to be registered as selected
   */
  public void uncheckSelections (String[] uncheck) {
    if (uncheck != null) {
      for (int i = 0; i < uncheck.length; i++) {
        selections.remove(uncheck[i]);
      }
    }
  }

  /**
   *  Derive changes in the user's selections from the HTTP request passed to
   *  the server.  Parameter values corresponding to myAddCommand,
   *  myRemoveCommand, and myInvertFlag are polled, and the selections are
   *  modified accordingly.
   *  @param request the HTTP request containing selection modifications
   */
  public void importRequest (HttpServletRequest request) {
    String[] check = request.getParameterValues(myAddCommand);
    String[] uncheck = request.getParameterValues(myRemoveCommand);
    String invert = request.getParameter(myInvertFlag);
    if (invert != null && invert.length() > 0)
      invertSelection();
    checkSelections(check);
    uncheckSelections(uncheck);
  }

  // This inner class is an instance class that uses the cached selections
  // of the containing UniversalTableSelect instance to determine whether
  // a given row is selected or not.  The output is HTML code for a checkbox
  // with the "checked" flag set accordingly.
  private class SelectedCheckMapper implements VariantMap {
    public Variant map (Object w) {
      DataWrapper dw = (DataWrapper) w;
      String uid = dw.getProperty(uniqueIdField).toString();
      String checked = "";
      if (selections.get(uid) != null)
        checked = " checked";
      return new VariantText(
        "<input type=checkbox name=\"" + myComponentNamePrefix + uid + "\"" +
        checked + ">");
    }
  }

  private SelectedCheckMapper checkMapper = new SelectedCheckMapper();

  /**
   *  Return an instance of SelectedCheckMapper, which renders checkboxes in
   *  HTML and uses this {@link UniversalTable} selection model to determine whether
   *  or not a given checkbox is checked.  This is useful for configuring a
   *  {@link UniversalTable} that uses this as its selection model.
   *  @return a {@link VariantMap} for producing HTML checkboxes
   */
  public VariantMap getCheckMapper () {
    return checkMapper;
  }

  /**
   *  Furnish the name of the JavaScript method that polls the checkboxes in
   *  an HTML page and converts any changes as part of an HTTP parameter string
   *  @return the JavaScript method
   */
  public String getParameterSavingJs () {
    return myComponentNamePrefix + "gather()";
  }

  /**
   *  Read the template file containing the JavaScript needed to support this
   *  selection model and write it out to a given PrintWriter.
   *  @param o the PrintWriter used for output
   */
  public void echoSelectScript (PrintWriter o) {
    try {
      jsTemplate.clear();

      jsTemplate.put("SELECTING", myAddCommand);
      jsTemplate.put("NOT_SELECTING", myRemoveCommand);
      jsTemplate.put("INVERTING", myInvertFlag);
      jsTemplate.put("PREFIX", myComponentNamePrefix);
      jsTemplate.put("UT_ID", table.getUniversalTableId());
      o.println(jsTemplate.process().toString());
      o.flush();
    }
    catch (Exception oh_no) {
      System.out.println("UniversalTableSelect::echoSS:  TEMPLATE ERROR--" + oh_no);
    }
  }

  // This inner class is used to iterate through the currently selected elements
  // in a given UniversalTable.  Instances are created by the getSelectedRows
  // method (q.v.).
  private static class SelectionIterator implements Enumeration {
    private DataWrapper[] r = null;
    private String f = null;
    private Hashtable s = null;
    private boolean hasMore = false;
    private int index = 0;

    public SelectionIterator (
        DataWrapper[] rows, String field, Hashtable selections)
    {
      r = rows;
      f = field;
      s = selections;
      if (r == null || r.length == 0)
        return;
      for (int i = 0; i < r.length; i++) {
        if (s.get(r[i].getProperty(f).toString()) != null) {
          index = i;
          hasMore = true;
          break;
        }
      }
    }

    public boolean hasMoreElements () {
      return hasMore;
    }

    public Object nextElement () {
      if (!hasMore)
        return null;
      Object ret = r[index];
      hasMore = false;
      for (int i = index + 1; i < r.length; i++) {
        if (s.get(r[i].getProperty(f).toString()) != null) {
          index = i;
          hasMore = true;
          break;
        }
      }
      return ret;
    }
  }  //end of inner class SelectionIterator

  /**
   *  Obtain an Enumeration that iterates through only those elements in
   *  the associated {@link UniversalTable} that are currently selected according
   *  to this selection model.
   *  @return an iterator for the current selections
   */
  public Enumeration getSelectedRows () {
    return new SelectionIterator(table.getRows(), uniqueIdField, selections);
  }

  // The string of JavaScript used on the client to support the selection model
  private static String templateString =
  "<script>" +
  "var ##PREFIX##inverted = false;\n" +
  "var ##PREFIX##prefix = \"##PREFIX##\";\n" +

  "function ##PREFIX##gather () {\n" +
  "  var f = document.forms[\"##UT_ID##\"];\n" +
  "  var i;\n" +
  "  var params = \"\";\n" +
  "  var elts = f.elements;\n" +
  "  if (elts != null && elts.length > 0) {\n" +
  "    var elt = elts[0];\n" +
  "    var uid;\n" +
  "    if (elt.type == \"checkbox\" && elt.name.indexOf(##PREFIX##prefix) == 0) {\n" +
  "      uid = elt.name.substring(##PREFIX##prefix.length);\n" +
  "      if (elt.checked)\n" +
  "        params += \"##SELECTING##=\";\n" +
  "      else\n" +
  "        params += \"##NOT_SELECTING##=\";\n" +
  "      params += uid;\n" +
  "    }\n" +
  "    for (i = 1; i < elts.length; i++) {\n" +
  "      elt = elts[i];\n" +
  "      if (elt.type == \"checkbox\" && elt.name.indexOf(##PREFIX##prefix) == 0)\n" +
  "      {\n" +
  "        var uid = elt.name.substring(##PREFIX##prefix.length);\n" +
  "        if (elt.checked)\n" +
  "          params += \"&##SELECTING##=\";\n" +
  "        else\n" +
  "          params += \"&##NOT_SELECTING##=\";\n" +
  "        params += uid;\n" +
  "      }\n" +
  "    }\n" +
  "  }\n" +
  "  if (##PREFIX##inverted)\n" +
  "    return \"##INVERTING##=T\" + (params.length > 0 ? \"&\" : \"\") + params;\n" +
  "  else\n" +
  "    return \"##INVERTING##=\" + (params.length > 0 ? \"&\" : \"\") + params;\n" +
  "}\n" +

  "function ##PREFIX##invert () {\n" +
  "  var f = document.forms[\"##UT_ID##\"];\n" +
  "  var elts = f.elements;\n" +
  "  ##PREFIX##inverted = !##PREFIX##inverted;\n" +
  "  if (elts != null && elts.length > 0) {\n" +
  "    for (i = 0; i < elts.length; i++) {\n" +
  "      elt = elts[i];\n" +
  "      if (elt.type == \"checkbox\" && elt.name.indexOf(##PREFIX##prefix) == 0)\n" +
  "      {\n" +
  "        elt.checked = !elt.checked;\n" +
  "      }\n" +
  "    }\n" +
  "  }\n" +
  "}\n" +
  "</script>";

  // The templateProcessor that interprets the above template when output is
  // produced
  private TemplateProcessor jsTemplate = null;
}
