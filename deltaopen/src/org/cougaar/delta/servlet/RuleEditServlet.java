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
package org.cougaar.delta.servlet;

import org.cougaar.delta.util.ExplanationFactory;
import org.cougaar.delta.util.QueryTable;
import org.cougaar.delta.util.Code;
import org.cougaar.delta.util.BatchSearchResult;
import org.cougaar.delta.util.DBObject;
import org.cougaar.delta.util.qrule.*;
import org.cougaar.delta.util.variant.*;
import org.cougaar.delta.util.html.*;
import org.cougaar.delta.applet.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.*;
import java.io.*;

/**
 * This class generates the html for the rule summary/search page and
 * the rule detail page. It is also the spawning servlet for the
 * RuleEditApplet, which is used to create/modify QRules.
 */

public class RuleEditServlet extends BasicServlet {
  private QRuleFactory factory = null;
  private ExplanationFactory explFactory = null;
  private Hashtable[] displayFormat = null;
  private Hashtable[] editHistory = null;
  private UniversalDetail historyFormat = null;
  private UniversalDetail detailFormat = null;
  private UniversalTable historyTable = null;

  // Some String names of things...
  private String self = "RuleServlet";
  private String myTable = self + "_Summary";
  private String myRule = self + "_Viewing";
  private String myHistory = self + "_History";
  private String unconfirmedSave = self + "_Unconfirmed";
  private String myFilter = self + "_Filter";
  private static final String DEFAULT_DESCRIPTION = null;

  private DataWrapper temp_dw;

  // inner class to get modification dates
  private class RuleModifDateMap implements VariantMap {

    public Variant map(Object w) {
      java.text.SimpleDateFormat originalFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
      java.text.SimpleDateFormat newFormat = new java.text.SimpleDateFormat("MM/dd/yyyy h:mm:ss a");

      Hashtable table = (Hashtable) ((DataWrapper) w).unwrap();
      try {
        Date date = originalFormat.parse(table.get("date").toString());
        return new VariantText(newFormat.format(date));
      } catch (Exception e) {
        e.printStackTrace();
        return new VariantText("");
      }
    }
  }

  // inner class to get a description
  private class RuleDescriptionMap implements VariantMap {
    public Variant map(Object w) {
      Hashtable table = (Hashtable) ((DataWrapper) w).unwrap();
      return new VariantText(table.get("description").toString());
    }
  }

  // inner class to compute detail links
  private class DetailLinkMap implements StringMap {
    public String map (Object w) {
      return "?command=VIEW&name=" +
        ((DataWrapper) w).getProperty("name");
    }
  }

  // inner class that gets a boolean property and displays it as "Yes" or "No"
  private static class BooleanDisplayMap implements VariantMap {
    private String field = null;

    public BooleanDisplayMap (String s) {
      field = s;
    }

    public Variant map (Object w) {
      Variant v = ((DataWrapper) w).getProperty(field);
      if (v instanceof VariantBoolean)
        return new VariantText(((VariantBoolean) v).getBoolean() ? "Yes" : "No");
      return null;
    }
  }

  // inner class that gets a boolean property and converts true to -1 and false
  // to 0 (used for sorting, where true comes before false)
  private static class BooleanSortMap implements VariantMap {
    private String field = null;

    public BooleanSortMap (String s) {
      field = s;
    }

    public Variant map (Object w) {
      Variant v = ((DataWrapper) w).getProperty(field);
      if (v instanceof VariantBoolean)
        return new VariantInt(((VariantBoolean) v).getBoolean() ? -1 : 0);
      return null;
    }
  }

  // inner class to give a terse String summary of a QRuleTest
  private class TestMap implements StringMap {
    public String map (Object w) {
      StringBuffer buf = new StringBuffer();
      QRuleTest qt = (QRuleTest) w;
      QRuleComparison q = null;
      QRuleLogicalTest qlt = null;
      if (qt == null)
        buf.append("<<NULL>>");
      else if ((q = qt.getComparison()) != null) {
        buf.append("(" + q.getOperand1() + ")");
        buf.append(" " + getFilteredOperatorName(q.getOperator()) + " ");
        QRuleOperand x = q.getOperand2();
        if (x instanceof QRuleAccessorOperand)
          buf.append("(" + x + ")");
        else
          buf.append("\"" + x + "\"");
      }
      else if ((qlt = qt.getLogicalTest()) != null) {
        buf.append("\"");
        buf.append(qlt.getLogicalOp());
        buf.append("\" clause");
      }
      else
        buf.append("<<ILLEGAL TEST>>");
      return buf.toString();
    }

    /**
     * Intercepts the comparison operators and replaces those that are Unicode
     * characters, which are not displayable in the browser, with the String
     * representation of the operation.
     */
    private String getFilteredOperatorName(QRuleOperator qrop){
      String opName = qrop.getUiName();
      char[] op = opName.toCharArray();
      Character c = new Character(op[0]);
      Character c_ne = new Character('\u2260');  //Unicode value for the "not equal to" sign
      Character c_gte = new Character('\u2265');  //Unicode value for the "greater than or equal to" sign
      Character c_lte = new Character('\u2264');  //Unicode value for the "less than or equal to" sign
      if(c.equals(c_ne))
        opName = "does not equal";
      else if(c.equals(c_gte))
        opName = "is greater than or equal to";
      else if(c.equals(c_lte))
        opName = "is less than or equal to";
      return opName;
    }
  }

  // this class acts as a filter on the types of tests to be displayed
  private class SubtestFilter implements VariantMap {
    private boolean atoms = false;
    private boolean exceptions = false;
    private boolean others = false;

    public SubtestFilter (boolean a, boolean e, boolean o) {
      atoms = a;
      exceptions = e;
      others = o;
    }

    public Variant map (Object w) {
      QRuleLogicalTest root = null;
      if (w instanceof DataWrapper)
        root = (QRuleLogicalTest) ((DataWrapper) w).unwrap();
      else
        root = (QRuleLogicalTest) w;
      Vector v = new Vector();
      for (Enumeration e = root.getOperands(); e.hasMoreElements(); ) {
        QRuleTest t = (QRuleTest) e.nextElement();
        QRuleLogicalTest qlt = t.getLogicalTest();
        if (qlt == null) {
          if (atoms)
            v.addElement(t);
        }
        else if (qlt.getLogicalOp().equals(QRuleTest.LOGICAL_NAND)) {
          if (exceptions)
            v.addElement(t);
        }
        else if (others)
          v.addElement(t);
      }
      return new VariantObject(v);
    }
  }

  // inner class to find the list of base tests in a rule.  Complex tests that
  // are not recognized as exceptions (i.e., are not "NAND" tests) are included
  private class BaseTestFinder implements VariantMap {
    private SubtestFilter filter = new SubtestFilter(true, false, true);

    public Variant map (Object w) {
      QRuleLogicalTest root = ((QRule) ((DataWrapper) w).unwrap()).getTest();
      return filter.map(root);
    }
  }

  // inner class to find the list of exception cases in a rule.
  private class ExceptionFinder implements VariantMap {
    private SubtestFilter filter = new SubtestFilter(false, true, false);
    public Variant map (Object w) {
      QRuleLogicalTest root = ((QRule) ((DataWrapper) w).unwrap()).getTest();
      return filter.map(root);
    }
  }

  // inner class to get the titles of the exception clauses
  private class ExceptionTitleMap implements StringMap {
    public String map (Object w) {
      QRuleLogicalTest qlt = (QRuleLogicalTest) ((DataWrapper) w).unwrap();
      String name = qlt.getName();
      if (name != null && name.length() > 0)
        return "Exception Case \"" + qlt.getName() + "\"";
      return "Exception Case";
    }
  }

  // inner class that calls the toEnglish method of a QRule
  private class EnglishMap implements StringMap {
    public String map (Object w) {
      try {
        return ((QRule) ((DataWrapper) w).unwrap()).toEnglish();
      }
      catch (Exception bugger) {
      }
      return "";
    }
  }

  // a VariantMap that converts Codes to VariantTexts of their values
  private class CodeTextMap implements VariantMap {
    public Variant map (Object w) {
      return new VariantText(((Code) ((DataWrapper) w).unwrap()).getCodeValue());
    }
  }

  public void processInit (ServletConfig sc) throws ServletException {
    try {
      factory = QRuleFactory.getInstance();
      explFactory = ExplanationFactory.getInstance();
      String domain = "rule";

      //
      // configure summary display
      //
      VariantMap v_map;
      displayFormat = new Hashtable[4];
      displayFormat[0] = UniversalTable.makeColumnFormat("Rule Name",
        explFactory.lookupExplanation(domain, "Rule Name"),
        new UniversalTable.SortLinkMap("name"), "ffffff", "left", "name",
        new DetailLinkMap());
      displayFormat[1] = UniversalTable.makeColumnFormat("Action",
        explFactory.lookupExplanation(domain, "Rule Action"),
        new UniversalTable.SortLinkMap("action"), "cbd9dd", "left", "action",
        null);

      VariantMap activatedMap = new BooleanDisplayMap("active");
      displayFormat[2] = UniversalTable.makeColumnFormat("Active",
        explFactory.lookupExplanation(domain, "Active"),
        new UniversalTable.SortLinkMap("Active"), "ffffff", "center",
        activatedMap, null, "Active", new BooleanSortMap("active"));

      VariantMap testingMap = new BooleanDisplayMap("testRule");
      displayFormat[3] = UniversalTable.makeColumnFormat("Test Rule",
        explFactory.lookupExplanation(domain, "Test Rule"),
        new UniversalTable.SortLinkMap("TestRule"), "ffffff", "center",
        testingMap, null, "TestRule", new BooleanSortMap("testRule"));

      //
      // configure detail display
      //
      UniversalDetailSection uds = null;
      UniversalDetailArray udArray = null;
      detailFormat = new UniversalDetail("Rule Detail Display");
      uds = detailFormat.addSection("Rule Description");
      uds.addRow(new UniversalDetailItem("English Statement",
        explFactory.lookupExplanation(domain, "English Statement"), new EnglishMap()));
      uds = detailFormat.addSection("Rule Attributes");
      uds.addRow(new UniversalDetailItem("Name",
        explFactory.lookupExplanation(domain, "Rule Name"), "name"));
      uds.addRow(new UniversalDetailItem("Action",
        explFactory.lookupExplanation(domain, "Rule Action"), "action"));
      uds.addRow(new UniversalDetailItem("Activated",
        explFactory.lookupExplanation(domain, "Active"), activatedMap));
      uds.addRow(new UniversalDetailItem("Test Rule",
        explFactory.lookupExplanation(domain, "Test Rule"), testingMap));

      uds = detailFormat.addSection("Main Condition Set");
      StringMap map = new TestMap();
      uds.addRow(udArray = new UniversalDetailArray(
        new BaseTestFinder(), new String[] {"Conditions"},
        new String[] {explFactory.lookupExplanation(domain,"Conditions")}, map));
      udArray.setEmptyMessage("No conditions specified; this rule applies to all proposals.");

      uds = new UniversalSectionArray(new ExceptionTitleMap(), new ExceptionFinder());
      uds.addRow(udArray = new UniversalDetailArray(
        new SubtestFilter(true, true, true), new String[] {"Conditions"}, map));
      udArray.setEmptyMessage("No conditions specified; all proposals are exempt from this rule.");
      detailFormat.addSection(uds);

      //
      // configure edit history display
      //
      editHistory = new Hashtable[3];
      v_map = new RuleModifDateMap();
      editHistory[0] = UniversalTable.makeColumnFormat("Date", "Date on which this rule was modified",
          new UniversalTable.SortLinkMap("Date"), "ffffff", "center", v_map, null);
      v_map = new RuleDescriptionMap();
      editHistory[2] = UniversalTable.makeColumnFormat("Description", "Description of the changes",
          new UniversalTable.SortLinkMap("Description"), "ffffff", "center", v_map, null);

      // show the JESS code for the rule--testing purposes only
      /*
      uds = detailFormat.addSection("Rule JESS--testing purposes only");
      uds.addRow(new UniversalDetailItem("JESS Code",
        new StringMap() {
          public String map (Object w) {
            QRule q = (QRule) ((DataWrapper) w).unwrap();
            return q.toJESS().toString();
          }
        }
      ));
      */
    }
    catch (Exception b_s) {
      System.out.println("RuleEditServlet::processInit:  ERROR--" + b_s);
    }
  }

  private QueryTable createExactNameQuery (String exactName) {
    QueryTable t = factory.getQueryTable();
    String Y = "Y";
    t.put("showInactive", Y);
    t.put("showTest", Y);
    t.put("exactRuleName", exactName);
    return t;
  }

  public void processGet (
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    try {
      captureCookies(request);
      HttpSession sess = request.getSession(true);
      PrintWriter o = new PrintWriter(response.getOutputStream());
      FormTemplate searchParams = (FormTemplate) sess.getValue(myFilter);
      if (searchParams == null) {
        searchParams = makeSearchForm();
        sess.putValue(myFilter, searchParams);
      }
      String command = request.getParameter("command");
      System.out.println("RULE COMMAND " + command);
      if (command == null || command.equals("")) {
        searchParams.generateHtml(o);
        echoCreateButton(o, sess);
      }
      else if (command.equalsIgnoreCase("SHOW_TABLE")) {
        // display one of the BSR
        String tableId = request.getParameter("universalTableId");
        if (tableId.equals(myTable)) {
          searchParams.generateHtml(o);
          echoCreateButton(o, sess);
          BatchSearchResult bsr = (BatchSearchResult) sess.getValue(BATCH_SEARCH_RESULT_KEY);
          if(bsr != null) {
            if(bsr.getServletTitle().equals(getTitle())) {
              bsr.generateHtml(o);
            }
          }
        } else
          // display edit history data
          if (tableId.equals(myHistory)) {
            DataWrapper dw = (DataWrapper) sess.getValue(myRule);
            historyFormat.generateHtml(o, dw);
            historyTable.generateHtml(o);
            echoGenericButton("Back to Summary", "", o);
          }
      } else
        if (command.equalsIgnoreCase("SHOW_EDIT_HISTORY")) {
          // get the edit history info
          addFgiHistorySite(request.getRequestURI() + "?" + request.getQueryString(), sess);
          DataWrapper dw = (DataWrapper) sess.getValue(myRule);
          QRule rule = null;
          if (dw == null) {
            o.println("<br><p class=mo2>" + "Rule edit history requested -- no Contract is currently selected." + "</p>");
            o.flush();
            echoGenericButton("Back to Summary", "", o);
          } else
            rule = (QRule) dw.unwrap();
          historyTable = universalTabulate(factory.getHistory(rule));
          historyTable.setUniversalTableId(myHistory);
          historyTable.setColumnInfo(editHistory);
          historyFormat = new UniversalDetail("Edit History for Rule: " + rule.getName());
          sess.putValue(myHistory, historyTable);
          requestShowTable(myHistory, o);
      }
      else if (command.equalsIgnoreCase("SHOW_DETAIL")) {
        DataWrapper dw = (DataWrapper) sess.getValue(myRule);
        temp_dw = dw;
        if (dw != null) {
          detailFormat.generateHtml(o, dw);
          echoRuleEditButtons(o, dw.getProperty("name").toString(), sess);
        }
        else
          echoMessageFromSummary("No Rule is currently selected.", o);
      }
      else if (command.equalsIgnoreCase("FIND")) {
        // save form settings
        searchParams.importForm(request);

        // process request
        BatchSearchResult bsr = (BatchSearchResult) sess.getValue(BATCH_SEARCH_RESULT_KEY);
        if(bsr!=null) {
          bsr.close();
        }
        bsr = factory.batchQuery(searchParams.getQuery(), getTitle());
        bsr.setUniversalTableId(myTable);
        bsr.setColumnInfo(displayFormat);
        sess.putValue(BATCH_SEARCH_RESULT_KEY, bsr);
        requestShowTable(myTable, o);
      }
      else if (command.equalsIgnoreCase("VIEW")) {
        String name = request.getParameter("name");
        BatchSearchResult bsr = (BatchSearchResult) sess.getValue(BATCH_SEARCH_RESULT_KEY);
        DataWrapper dw = null;
        if (bsr != null) {
          if(bsr.getServletTitle().equals(getTitle()))
            dw = bsr.getUniversalTable().findRow("name", name);
        }
        if (dw == null) {
          dw = new DataWrapper(createRule(name));
        }
        if (dw == null) {
          throw new Exception("no rule named \"" + name + "\" was found");
        }
        sess.putValue(myRule, dw);
        requestShowDetail(o);
      }
      else if (command.equalsIgnoreCase("DELETE")) {
        DataWrapper dw = (DataWrapper) sess.getValue(myRule);
        String name = dw.getProperty("name").toString();
        factory.delete((QRule) dw.unwrap());
        BatchSearchResult bsr = (BatchSearchResult) sess.getValue(BATCH_SEARCH_RESULT_KEY);
        if(bsr!=null) {
          bsr.close();
        }
        bsr = factory.batchQuery(searchParams.getQuery(), getTitle());
        bsr.setUniversalTableId(myTable);
        bsr.setColumnInfo(displayFormat);
        sess.putValue(BATCH_SEARCH_RESULT_KEY, bsr);
        sess.removeValue(myRule);
        echoMessageFromSummary("The Rule named \"" + name +
          "\" has been removed from the system.", o);
      }
      else if (command.equalsIgnoreCase("XML_DUMP")) {
        exportRule(request, response);
      }
      else if (command.equalsIgnoreCase("PAGE")) {
        // respond to a pagination request from one of the BSR
        BatchSearchResult bsr = (BatchSearchResult)sess.getValue(BATCH_SEARCH_RESULT_KEY);
        String tableId = bsr.getUniversalTable().getUniversalTableId();
        int rn = Integer.parseInt(request.getParameter("rownum"));
        if(bsr.isClosed()) {
          bsr = factory.batchQuery(searchParams.getQuery(), getTitle());
          bsr.setUniversalTableId(tableId);
          bsr.setColumnInfo(displayFormat);
          sess.putValue(BATCH_SEARCH_RESULT_KEY, bsr);
        }
        bsr.updateSelection(request);
        factory.preparePage(bsr,  rn);
        requestShowTable(tableId, o);
      }
      else if (command.equalsIgnoreCase("SORT")) {
        // sort one of the BSR at user request
        String tableId = request.getParameter("universalTableId");
        BatchSearchResult bsr = (BatchSearchResult) sess.getValue(BATCH_SEARCH_RESULT_KEY);
        String sortKey = request.getParameter("sortKey");
        if (tableId == null)
          tableId = bsr.getUniversalTable().getUniversalTableId();
        if (tableId.equals(myTable)) {
          bsr.updateSelection(request);
          bsr = factory.sortBatchQuery(searchParams.getQuery(), getTitle(), bsr, sortKey);
          bsr.setColumnInfo(displayFormat);
          bsr.setUniversalTableId(tableId);
          sess.putValue(BATCH_SEARCH_RESULT_KEY, bsr);
          requestShowTable(tableId, o);
        } else
          if (tableId.equals(myHistory)) {
            UniversalTable ut = (UniversalTable) sess.getValue(tableId);
            ut.sort(sortKey);
            requestShowTable(tableId, o);
          }
       }
      else {
        System.out.println(
          "RuleEditServlet::processGet:  Unrecognized command \"" + command +
          "\" requested");
      }
    }
    catch (Exception oh_no) {
      System.out.println("RuleEditServlet::processGet:  ERROR--" + oh_no);
      oh_no.printStackTrace();
    }
  }

  // Tell the client to replace its previous request with a "SHOW_TABLE"
  // request
  private void requestShowTable (String tableId, PrintWriter o) {
    o.println("<script>location.replace(\"" +
      (tableId == null ?
        "" :
        "?command=SHOW_TABLE&universalTableId=" + tableId) +
      "\");</script>");
    o.flush();
  }

  // Tell the client to replace its previous request with "SHOW_DETAIL"
  private void requestShowDetail (PrintWriter o) {
    o.println("<script>location.replace(\"?command=SHOW_DETAIL\");</script>");
    o.flush();
  }


  private void exportRule (
      HttpServletRequest request, HttpServletResponse response)
  {
    try {
      HttpSession sess = request.getSession(true);
      OutputStream out = response.getOutputStream();
      QRule q = (QRule) ((DataWrapper) sess.getValue(myRule)).unwrap();
      QRuleToXml qtx = new QRuleToXml(factory);
      qtx.export(q, out);
    }
    catch (Exception oh_no) {
      System.out.println("RuleEditServlet::exportRule:  ERROR--" + oh_no);
      oh_no.printStackTrace();
    }
  }

  private void importRule (
      HttpServletRequest request, HttpServletResponse response)
  {
    try {
      HttpSession sess = request.getSession(true);
      PrintWriter o = new PrintWriter(response.getOutputStream());
      generateGetHeader(request, response);
      try {
        QRuleToXml qtx = new QRuleToXml(factory);
        ServletInputStream in = request.getInputStream();

        MultipartFormReader mfr = new MultipartFormReader(in);
        BufferedReader bufr = null;
        String inLine = null;
        QRule[] q = new QRule[0];
        boolean activateImp = false;

        // get the XML document (presumed to be in the first section) and an
        // HTML form element (if there is one) that may follow.  If the HTML
        // element's name is "activateImp" then its value is taken as an
        // indication of whether the user wants to activate the imported rules
        if (mfr.hasMoreSections()) {
          bufr = new BufferedReader(new InputStreamReader(
            mfr.getSectionStream()));
          // consume the section headers and the subsequent blank line
          while ((inLine = bufr.readLine()) != null && inLine.length() > 0);
          q = qtx.parse(bufr);
        }
        if (mfr.hasMoreSections()) {
          bufr = new BufferedReader(new InputStreamReader(
            mfr.getSectionStream()));
          // extract the form element's name
          inLine = bufr.readLine();
          int j = inLine.indexOf("name=\"");
          j += 6;
          int k = inLine.indexOf("\"", j);
          String name = inLine.substring(j, k);

          // skip the rest of the headers and the subsequent blank line
          while ((inLine = bufr.readLine()) != null && inLine.length() > 0);

          inLine = bufr.readLine();
          if (name.equals("activateImp"))
            activateImp = (inLine != null && inLine.equals("Y"));
        }

        System.out.println("RuleEditServlet::importRule:  activateImp is " + activateImp);

        Vector rejects = new Vector();
        Vector bad_references = new Vector();
        int i;
        for (i = 0; i < q.length; i++) {
          if (factory.exists(q[i].getName())) {
            rejects.addElement(q[i].getName());
            q[i] = null;
          }
          else {
            q[i].setActive(activateImp);
            validateReferences(q[i].getTest(), bad_references);
            factory.sync(q[i], "Rule imported from XML file");
          }
        }
        StringBuffer userMessage = new StringBuffer();
        if (q.length == 0)
          userMessage.append("Unable to import--no rules found.");
        else {
          if (bad_references.size() > 0) {
            userMessage.append("<b>Warning:</b>  " +
              "The following referenced entities could not be found " +
              "(the rules were imported anyway):<ul>");
            Enumeration enu = bad_references.elements();
            while (enu.hasMoreElements()) {
              QRuleComparison cond = (QRuleComparison) enu.nextElement();
              userMessage.append("<li>" + getReferenceType(cond) + " \"" +
                cond.getOperand2() + "\"</li>");
            }
            userMessage.append("</ul><br>");
          }
          if (rejects.size() > 0) {
            userMessage.append(
              "Unable to import the following rules due to name conflict:<ul>");
            Enumeration enu = rejects.elements();
            while (enu.hasMoreElements()) {
              userMessage.append("<li>" + enu.nextElement() + "</li>");
            }
            userMessage.append("</ul><br>");
          }
          if (rejects.size() < q.length) {
            userMessage.append("Successfully imported:<ul>");
            for (i = 0; i < q.length; i++)
              if (q[i] != null)
                userMessage.append("<li>" + q[i].getName() + "</li>");
            userMessage.append("</ul></br>");
          }
        }
        echoMessageFromSummary(userMessage.toString(), o);
      }
      catch (Exception didnt_work) {
        echoMessageFromSummary("Error--unable to import rule.", o);
        didnt_work.printStackTrace();
      }
      generateGetFooter(request, response);
    }
    catch (Exception oh_no) {
      System.out.println("RuleEditServlet::importRule:  ERROR--" + oh_no);
      oh_no.printStackTrace();
    }
  }

  // render human-readable the type of reference present in a QRuleComparison
  private String getReferenceType (QRuleComparison c) {
    QRuleAccessorOperand left = (QRuleAccessorOperand) c.getOperand1();
    String name = left.getInternalName();
    String type = left.getUiType();
    return "Unknown Entity";
  }

  /**
   *  Override the default header for GET requests.  In particular, a request
   *  with "command=XML_DUMP" should have no HTML headers, and should have its
   *  content type specified as "text/xml".  A standard HTML header is
   *  generated for all other types of requests.
   *  @param request the HTTP request received from the client
   *  @param response the response to be delivered back to the client
   *  @throws IOException in case there is a problem in I/O
   */
  public void generateGetHeader (
      HttpServletRequest request, HttpServletResponse response)
      throws IOException
  {
    String command = request.getParameter("command");
    if (command == null || !command.equals("XML_DUMP")) {
      BuildHtmlHeader(new PrintWriter (response.getOutputStream()), getTitle());
    }
    else {
      response.setHeader("content-type", "text/xmlqrule");
    }
  }

  /**
   *  Override the default footer for GET requests.  In the special case of
   *  a request with "command=XML_DUMP", no HTML footer is produced.  All other
   *  cases are given the default footer.
   *  @param request the HTTP request received from the client
   *  @param response the response to be delivered back to the client
   *  @throws IOException in case there is a problem in I/O
   */
  public void generateGetFooter(
      HttpServletRequest request, HttpServletResponse response)
      throws java.io.IOException
  {
    String command = request.getParameter("command");
    if (command == null || !command.equals("XML_DUMP"))
      super.generateGetFooter(request, response);
  }

  private QRule createRule(String ruleName)
  {
    QRule ret = null;

    UniversalTable ut = factory.query(createExactNameQuery(ruleName));
    DataWrapper [] dw = ut.getRows();
    if (dw.length > 0)
      ret = (QRule)dw[0].unwrap();

    return ret;
  }

  // extract session ID cookies from this request and store them for future
  // reference as a session variable.
  private void captureCookies (HttpServletRequest req) {
    HttpSession sess = req.getSession(true);
    Cookie[] cookies = req.getCookies();
    StringBuffer buf = new StringBuffer();
    if (cookies != null && cookies.length > 0) {
      buf.append(cookies[0].getName());
      buf.append("=");
      buf.append(cookies[0].getValue());
      for (int i = 1; i < cookies.length; i++) {
        buf.append("; ");
        buf.append(cookies[i].getName());
        buf.append("=");
        buf.append(cookies[i].getValue());
      }
    }
    sess.putValue(sessionCookieData, buf.toString());
    System.out.println("Cookies:  " + buf);
  }

  public void processPost (
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    HttpSession sess = request.getSession(true);

    String command = getParameter(request, "command");
    if (command != null && command.equals("IMPORT")) {
      importRule(request, response);
      return;
    }

    try {
      AppletToServletParcel box = new AppletToServletParcel();
      ObjectInputStream ois = new ObjectInputStream(request.getInputStream());
      box = (AppletToServletParcel) ois.readObject();
      command = box.command;
      Serializable parcel = box.parcel;
      ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());

      if (command.equalsIgnoreCase("ACCESSOR_OPERANDS")) {
        Vector v = new Vector();
        Enumeration ops = factory.getAccessorOperands();
        while (ops.hasMoreElements()) {
          v.addElement(ops.nextElement());
        }
        box.parcel = v;
      }
      else if (command.equalsIgnoreCase("OPERATOR_TABLE")) {
        Vector v = new Vector();
        Enumeration ops = new OrderedTraversal(
          factory.getOperators(), OrderedTraversal.STRING_IGNORE_CASE);
        while (ops.hasMoreElements()) {
          v.addElement(ops.nextElement());
        }
        box.parcel = v;
      }
      else if (command.equalsIgnoreCase("GIMME")) {
        DataWrapper dw = (DataWrapper) sess.getValue(myRule);
        box.parcel = (Serializable) dw.unwrap();
      }
      else if (
          command.equalsIgnoreCase("SAVE") || command.equalsIgnoreCase("COPY"))
      {

        QRule rule = (QRule) parcel;

        // If the user wants to copy this rule, set the databaseId to the
        // NULL_DATABASE_ID so it'll be stored separately.  Save the old one
        // for comparison in case some person forgot to change the name.
        long oldDatabaseId = DBObject.NULL_DATABASE_ID;
        if (command.equalsIgnoreCase("COPY")) {
          oldDatabaseId = rule.getDatabaseId();
          rule.setDatabaseId(DBObject.NULL_DATABASE_ID);
        }

        // Make sure the user isn't trying to make two rules with the same name
        UniversalTable ut = factory.query(createExactNameQuery(rule.getName()));
        boolean okayToSave = false;
        if (ut.getRows().length == 0) {
          // no name collision is found--store the rule
          if (rule.getDatabaseId() == DBObject.NULL_DATABASE_ID) {
            // create the new rule and send its databaseId back to the client
            okayToSave = true;
            box.parcel = "CREATED";
          }
          else {
            // an existing rule is having its name changed
            okayToSave = true;
            box.parcel = "UPDATED";
          }
        }
        else {
          // possible name collision--check to see if it is the same one
          QRule conflict = (QRule) ut.getRows()[0].unwrap();
          if (conflict.getDatabaseId() == oldDatabaseId) {
            // someone's trying to make a copy with the same name
            box.parcel = "SAME_NAME";
          }
          else if (rule.getDatabaseId() == conflict.getDatabaseId()) {
            // this is an update of an existing rule--store the changes
            okayToSave = true;
            box.parcel = "UPDATED";
          }
          else {
            // name collision--don't save
            box.parcel = "FAILED " + ut.getRows()[0].getProperty("name");
          }
        }
        if (okayToSave) {
          Vector badReferences = new Vector();
          validateReferences(rule.getTest(), badReferences);
          if (badReferences.size() == 0) {
            factory.sync(rule, DEFAULT_DESCRIPTION);
            syncSession(sess, rule);
            if (box.parcel.equals("CREATED"))
              box.parcel = box.parcel + " " + rule.getDatabaseId();
          }
          else {
            badReferences.insertElementAt("REFERENCE_NOT_FOUND", 0);
            box.parcel = badReferences;
            sess.putValue(unconfirmedSave, rule);
          }
        }
      }
      else if (command.equals("CONFIRM_SAVE")) {
        QRule rule = (QRule) sess.getValue(unconfirmedSave);
        if (rule != null) {
          factory.sync(rule, DEFAULT_DESCRIPTION);
          syncSession(sess, rule);
          sess.removeValue(unconfirmedSave);
          box.parcel = "SAVED " + rule.getDatabaseId();
        }
        else
          box.parcel = null;
      }
      else if (command.startsWith("VIEW ")) {
        String name = command.substring(5);
        UniversalTable ut = (UniversalTable) sess.getValue(myTable);
        DataWrapper dw = ut.findRow("name", name);
        if (dw == null)
          throw new Exception("ERROR:  \"" + name + "\" not found");
        box.parcel = (Serializable) dw.unwrap();
      }
      else if (command.equals("KEEP_SESSION")) {
        System.out.println("RuleEditServlet::processPost:  PINGed by the rule editor!");
        box.parcel = "OKAY";
      }
      // send the box back to the applet
      oos.writeObject(box);
    }
    catch (Exception oh_no) {
      System.out.println("RuleEditServlet::processPost:  ERROR processing " + command + "--" + oh_no);
      oh_no.printStackTrace();
    }
  }


  private void validateReferences (QRuleLogicalTest test, Vector v) {
    Enumeration enu = test.getOperands();
    while (enu.hasMoreElements()) {
      QRuleTest qt = (QRuleTest) enu.nextElement();
      QRuleLogicalTest qlt = qt.getLogicalTest();
      if (qlt != null)
        validateReferences(qlt, v);
      else {
        QRuleComparison qc = qt.getComparison();
        QRuleAccessorOperand accessor = (QRuleAccessorOperand) qc.getOperand1();
        QRuleOperand value = qc.getOperand2();
        if (!value.isLiteral() ||
            ((QRuleLiteralOperand) value).getValue() instanceof Vector)
        {
          continue;
        }
        String name = accessor.getInternalName();
        String type = accessor.getUiType();
        type = accessor.getInternalName();
        QueryTable qTable = null;

//        Here's where you can add validation check for the different types
//        of literal values
      }
    }
  }

  private void syncSession (HttpSession sess, QRule parcel) {
    DataWrapper dw = new DataWrapper(parcel);
    sess.putValue(myRule, dw);
    UniversalTable ut = (UniversalTable) sess.getValue(myTable);
    if (ut != null)
      ut.updateRow("databaseId", dw);
  }


  /**
   *  Override the default header.  For POST requests, we'll be sending
   *  serialized java Objects, and extraneous HTML tags would interfere.
   */
  public void generatePostHeader (HttpServletRequest q, HttpServletResponse s)
      throws IOException
  {
  }

  /**
   *  Override the default footer.  For POST requests, we'll be sending
   *  serialized java Objects, and extraneous HTML tags would interfere.
   */
  public void generatePostFooter (HttpServletRequest q, HttpServletResponse s)
      throws IOException
  {
  }

  // create the searchForm
  private FormTemplate makeSearchForm () {
    try {
      FormTemplate ft = new FormTemplate(
        template_path_ + "SampleRuleSearchForm.html", factory.getQueryTable());
      ft.addField("ruleName");
//      ft.addField("contract");
//      ft.addField("customer");
//      ft.addField("itemID");
      ft.addField("maxRows");
      ft.put("maxRows", "20");
      ft.addVariableChoices("action",
        new String[] {
          "",
          QRule.NEGATIVE,
          QRule.NEUTRAL,
          QRule.POSITIVE
        }
      );
//      ft.addCheckbox("showGlobal");
//      ft.addCheckbox("showLta");
//      ft.addCheckbox("showItem");
      ft.addCheckbox("showTest");
      ft.addCheckbox("showInactive");
//      ft.put("showGlobal", "checked");
//      ft.put("showLta", "checked");
//      ft.put("showItem", "checked");
      ft.put("showTest", "checked");
      ft.put("showInactive", "checked");

      return ft;
    }
    catch (Exception oh_no) {
      System.out.println(
        "RuleEditServlet::makeSearchForm:  TEMPLATE ERROR--" + oh_no);
      oh_no.printStackTrace();
    }
    return null;
  }

  private void echoCreateButton (PrintWriter o, HttpSession sess) {
    try {
      TemplateProcessor tp;
      tp= new TemplateProcessor(template_path_ + "SampleRuleCreate.html");

      tp.put("HIDE_BUTTONS", null);
      tp.put("APPLET_NAME", "RuleEditor");
      tp.put("CLASS", "org.cougaar.delta.applet.ruleeditor.RuleEditApplet");
      tp.put("CODE_BASE", "/applets/");
      tp.put("HOME_SERVLET", "OpenRuleEdit");
      tp.put("SESSION_COOKIE", sess.getValue(sessionCookieData));
      o.println(tp.process().toString());
      o.flush();
    }
    catch (Exception oh_no) {
      System.out.println("RuleEditServlet::echoCreateButton:  TEMPLATE ERROR--" + oh_no);
    }
  }

  private void echoRuleEditButtons (
      PrintWriter o, String ruleName, HttpSession sess)
  {
    try {
    TemplateProcessor tp;
    tp= new TemplateProcessor(template_path_ + "SampleRuleEdit.html");

      tp.put("HIDE_BUTTONS", null);
      tp.put("APPLET_NAME", "RuleEditor");
      tp.put("CLASS", "org.cougaar.delta.applet.ruleeditor.RuleEditApplet");
      tp.put("CODE_BASE", "/applets/");
      tp.put("HOME_SERVLET", "OpenRuleEdit");
      tp.put("SESSION_COOKIE", sess.getValue(sessionCookieData));
      tp.put("RULE_NAME", ruleName);
      tp.put("BACK_COMMAND", "SHOW_TABLE&universalTableId=" + myTable);
      o.println(tp.process().toString());
      o.flush();
    }
    catch (Exception oh_no) {
      System.out.println("RuleEditServlet::echoRuleEditButtons:  TEMPLATE ERROR--" + oh_no);
    }
  }

  private UniversalTable universalTabulate(Vector stuff) {
    DataWrapper[] wrappers = new DataWrapper[stuff.size()];
    for (int i = 0; i < stuff.size(); i++)
      wrappers[i] = new DataWrapper(stuff.get(i));
    return new UniversalTable(wrappers);
  }

  private void echoGenericButton(String buttonText, String paramString, PrintWriter o) {
    try {
      TemplateProcessor tp = new EncodingTemplate(template_path_ + "SampleButton.html");
      tp.put("COMMAND", paramString);
      tp.put("BUTTON_TEXT", buttonText);
      o.println(tp.process().toString());
      o.flush();
    } catch (Exception oh_no) {
      System.out.println("ContractServlet::echoGenericButton:  Error--" + oh_no);
    }
  }

  private void echoMessageFromSummary (String message, PrintWriter o) {
    o.println(
      "<br><p class=mo2>" + message + "</p>" +
      "<br><br>");
    try {
      TemplateProcessor tp = new EncodingTemplate(
        template_path_ + "SampleButton.html");
      tp.put("COMMAND", "SHOW_TABLE&universalTableId=" + myTable);
      tp.put("BUTTON_TEXT", "Back to Summary");
      o.println(tp.process().toString());
    }
    catch (Exception oh_no) {
      System.out.println(
        "RuleEditServlet::echoMessageFromSummary:  TEMPLATE ERROR-- " + oh_no);
    }
    o.flush();
  }

  public String getTitle () {
    return "Rule Editor";
  }
}
