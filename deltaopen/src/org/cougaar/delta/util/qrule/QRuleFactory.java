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

package org.cougaar.delta.util.qrule;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import org.cougaar.delta.util.*;
import org.cougaar.delta.util.html.*;
import org.cougaar.delta.util.variant.*;
import org.cougaar.delta.plugin.*;

/**
 * Manager for the rule database. This class is responsible for accessing the
 * database for information about QRules in order to retrieve saved QRules, or
 * save changes, or delete QRules. This class must be in synch with the database
 * schema for representing QRules.
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: QRuleFactory.java,v 1.1 2002-04-30 17:33:28 cerys Exp $
 */
public class QRuleFactory extends Factory {

  private static Hashtable operators = null;
  private static Hashtable operatorsByName = null;
  private static Hashtable operands = null;
  private static Hashtable operandsByName = null;
  private static Cache rules_cache = null;
  private static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

  private static QRuleFactory _instance = null;

  private static final String qbase = "select " +
    "qr.rule_id, qr.version, qr.rule_name, qr.action, qr.creation_date, " +     //  1 -  5
    "qr.is_active, qr.test_rule, upper(qr.rule_name) as upper_rule_name, " +    //  6 -  8
    " qr.modification_date " +                    // 9
    "from temp_qual_rule qr";

  private static final String testQueryBase = "select " +
    "rule_test_id, parent_test_id, logical_operator, operator_id, " +          //  1 -  4
    "operand_1_type, operand_1_literal_type, operand_1_literal_value, " +      //  5 -  7
    "operand_1_accessor_id, operand_2_type, operand_2_literal_type, " +        //  8 - 10
    "operand_2_literal_value, operand_2_accessor_id, logical_test_name " +     // 11 - 13
    "from temp_qual_rule_test";

  /**
   * Fetch the singleton QRuleFactory instance.
   * @return the shared QRuleFactory
   */
  public static QRuleFactory getInstance() {
    if (_instance == null) {
      _instance = new QRuleFactory();
    }
    return _instance;
  }

  /**
   * Create a QRuleFactory
   */
  private QRuleFactory() {
    super();
    if( rules_cache == null ) {
      rules_cache = new Cache();
      rules_cache.setQueryPrefix("SELECT VERSION, RULE_ID from TEMP_QUAL_RULE where RULE_ID = ");
    }
  }


  /**
   * Load up the cached operators and operands if necessary
   */
  private void initialize() {

    if( operators == null ) {
      String opts = "SELECT OPERATOR_ID, JESS_NAME, UI_NAME, OP1_TYPE, OP2_TYPE " +
        "FROM TEMP_QUAL_RULE_OPERATOR";

      operators = new Hashtable();
      operatorsByName = new Hashtable();
      try {
        ResultSet rs = doQuery(opts, null);
        while (rs.next()) {
          Character c;
          long dbId = rs.getLong(1);
          Long key = new Long(dbId);
          String operName = rs.getString(3);
          if(operName.equals("!=")){
            c = new Character('\u2260');  //Unicode value for the "not equal to" sign
            operName = c.toString();
          }else if(operName.equals("<=")){
            c = new Character('\u2264');  //Unicode value for the "less than or equal to" sign
            operName = c.toString();
          }else if(operName.equals(">=")){
            c = new Character('\u2265');  //Unicode value for the "greater than or equal to" sign
            operName = c.toString();
          }
          QRuleOperator qro =  new QRuleOperator(
            rs.getString(2), operName, rs.getString(4), rs.getString(5));
          qro.setDatabaseId(dbId);
          operators.put(key, qro);
          operatorsByName.put(qro.getJessName(), qro);
        }
      }
      catch (Exception e) {
        System.err.println("XXXXXXXXX  Error in QRuleFactory::initialize");
        e.printStackTrace();
      }
    }
    if( operands == null ) {
      String opns = "SELECT ACCESSOR_ID, INTERNAL_NAME, UI_CATEGORY, UI_NAME, " +
        "UI_TYPE, JESS_TYPE, JESS_ACCESSOR FROM TEMP_QRULE_ACCESSOR_OPERAND";

      operands = new Hashtable();
      operandsByName = new Hashtable();
      try {
        ResultSet rs = doQuery(opns, null);
        while (rs.next()) {
          long dbId = rs.getLong(1);
          Long key = new Long(dbId);
          QRuleAccessorOperand qrao = new QRuleAccessorOperand(rs.getString(2),
            rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
            rs.getString(7));
          qrao.setDatabaseId(dbId);
          operands.put(key, qrao);
          operandsByName.put(qrao.getInternalName(), qrao);
        }
      }
      catch (Exception e) {
        System.err.println("XXXXXXXXX  Error in QRuleFactory::initialize");
        e.printStackTrace();
      }
    }

  }

  /**
   *  Get the operator object given the operator ID
   *  @param operatorType The operator ID (BigDecimal)
   *  @return The corresponding operator object.
   */
  public QRuleOperator getOperator (Object operatorType) {
    initialize();
    return((QRuleOperator)operators.get(operatorType));
  }

  /**
   *  Get the operator given the operator's JESS name
   *  @param name the name of the operator
   *  @return The corresponding operator object.
   */
  public QRuleOperator getOperatorByName (String name) {
    initialize();
    return (QRuleOperator) operatorsByName.get(name);
  }

  /**
   *  Get the accessor operand object given the operand ID
   *  @param operandType The operand ID (BigDecimal)
   *  @return The corresponding accessor operand object.
   */
  public QRuleAccessorOperand getAccessorOperand (Object operandType) {
    initialize();
    return((QRuleAccessorOperand)operands.get(operandType));
  }

  /**
   *  Get the accessor operand object given the internal name of the accessor
   *  @param name the internal name of the operand
   *  @return The corresponding accessor operand object.
   */
  public QRuleAccessorOperand getAccessorOperandByName (String name) {
    initialize();
    return (QRuleAccessorOperand) operandsByName.get(name);
  }

  /**
   * Get all of the operator objects
   * @return The operator objects.
   */
  public Enumeration getOperators(){
    initialize();
    return(operators.elements());
  }

  /**
   * Get the accessor operand object given the operand ID
   * @param operandType The operand ID (BigDecimal)
   * @return The corresponding accessor operand object.
   */
  public Enumeration getAccessorOperands() {
    initialize();
    return(operands.elements());
  }


  /**
   * Get a list of rule names that reference the region names in their tests.
   * @param regionNames the names to look for in the format: "GeographyName:RegionName"/
   * @return a (possibly empty) list of rule names that reference any of these regions in their tests.
   */
  public synchronized Vector lookupByRegionName(Vector regionNames, Connection conn) throws SQLException
  {
    Vector ret = new Vector();
    try {
      if (regionNames.size() > 0) {
        StringBuffer qbuf = new StringBuffer("SELECT UNIQUE R.RULE_NAME FROM TEMP_QUAL_RULE R, TEMP_QUAL_RULE_TEST T " +
          "WHERE R.RULE_ID = T.RULE_ID AND  " +
          "T.OPERAND_2_LITERAL_VALUE in (");
        Enumeration e = regionNames.elements();
        qbuf.append(stringize((String)e.nextElement()));
        while (e.hasMoreElements())
          qbuf.append(", " + stringize((String)e.nextElement()));
        qbuf.append(")");
        String q = qbuf.toString();
        ResultSet rs = doQuery(q, conn);
        while (rs.next())
          ret.add(rs.getString(1));
      }
    } catch (SQLException sqle) {
      throw sqle;
    }
    return ret;
  }

  // construct a set of rules based on an SQL query String
  private Vector getRuleInfo (String q, Connection conn) {
    initialize();

    // store the rules in a Vector
    Vector rules = new Vector();
    // keep track of the ones that need to have their tests installed
    Vector incompleteRules = new Vector();
    // holder for rules being constructed
    QRule rule;

    try {
      ResultSet rs = doQuery(q, conn);
      // loop through the result set and grab rule information
      while (rs.next()) {
        long dbId = rs.getLong(1);
        long version = rs.getLong(2);
        Long ruleId = new Long(dbId);
        if ((rule = (QRule) rules_cache.check(ruleId, conn)) != null) {
          rules.addElement(rule);
        } else {
          rule = new QRule(rs.getString(3), rs.getString(4));
          rule.setVersion(version);
          rule.setDatabaseId(dbId);
          rule.setCreationDate(rs.getDate(5));
          String flag = rs.getString(6);
          rule.setActive(flag != null && flag.equalsIgnoreCase("Y"));
          rule.setModificationDate(rs.getDate(9));
          rules.addElement(rule);
          incompleteRules.addElement(rule);
          rules_cache.store(ruleId, rule);
        }
      }
      // now grab the tests for incomplete rules
      for (Enumeration e = incompleteRules.elements(); e.hasMoreElements(); )
        getRuleTests((QRule) e.nextElement(), conn);
    }
    catch (Exception oh_no) {
      System.out.println("QRuleFactory::getRuleInfo:  ERROR--" + oh_no);
      oh_no.printStackTrace();
    }

    return rules;
  }

  // assemble and install the test hierarchy for a given QRule
  private void getRuleTests (QRule q, Connection conn) {
    // keep a table of tests for quick reference
    Hashtable tests = new Hashtable();
    // also for quick reference, keep the root test handy
    QRuleTest root = null;
    try {
      ResultSet rs = doQuery(
        testQueryBase + " where rule_id = " + q.getDatabaseId(), conn);
      while (rs.next()) {
        QRuleTest qt = null;
        QRuleLogicalTest parent = null;
        long dbId = rs.getLong(1);
        long parentId = rs.getLong(2);
        Long key = new Long(dbId);
        Long parentKey = new Long(parentId);
        String type = rs.getString(3);
        if (type == null || type.length() == 0) {
          // not a logical operator--do the normal thing
          QRuleOperator op = getOperator(new Long(rs.getLong(4)));
          QRuleOperand left = processOperand(rs.getString(5), rs.getString(6),
            rs.getObject(7), new Long(rs.getLong(8)));
          QRuleOperand right = processOperand(rs.getString(9), rs.getString(10),
            rs.getObject(11), new Long(rs.getLong(12)));
          qt = new QRuleComparison(left, op, right);
        }
        else {
          // this is a logical operation--check for an existing stand-in
          // (see case III below)
          QRuleLogicalTest qlt = (QRuleLogicalTest) tests.get(key);
          if (qlt == null)
            tests.put(key, qlt = new QRuleLogicalTest(type));
          else
            qlt.setLogicalOp(type);
          qlt.setName(rs.getString(13));
          qt = qlt;
        }
        qt.setRule(q);
        qt.setDatabaseId(dbId);
        // case I:  no parent--this is the root test
        if (parentId == DBObject.NULL_DATABASE_ID)
          root = qt;
        // case II:  parent is already in the table--add this one as a child
        else if ((parent = (QRuleLogicalTest) tests.get(parentKey)) != null)
          qt.setParent(parent);
        // case III:  parent not yet found--create a stand-in
        else {
          parent = new QRuleLogicalTest(null);
          qt.setParent(parent);
          tests.put(parentKey, parent);
        }
      }
      q.setTest(root);
    }
    catch (Exception oh_no) {
      // if a problem occurs, install an empty "AND" operator as the root
      new QRuleLogicalTest(q, null, QRuleTest.LOGICAL_AND);

      System.out.println(
        "QRuleFactory::getRuleTests:  Error generating tests for \"" +
        q.getName() + "\"");
      oh_no.printStackTrace();
    }
  }

  protected String getSortByColumn(String sortKey) {
    String ret = "";
    if(sortKey.equals("name")) {
      ret = "upper_rule_name";
    }
    else if(sortKey.equals("action")) {
      ret = "qr.action";
    }
    else if(sortKey.equals("Active")) {
      ret = "qr.is_active";
    }
    else if(sortKey.equals("LtaSpecific")) {
      ret = "qr.is_lta_specific";
    }
    else if(sortKey.equals("NsnSpecific")) {
      ret = "qr.is_nsn_specific";
    }
    else if(sortKey.equals("Global")) {
      ret = "is_global";
    }
    else if(sortKey.equals("TestRule")) {
      ret = "test_rule";
    }
    else {
      System.out.println("getSortByColumn " + sortKey);
    }
    return ret;
  }

  // get an enumeration of rules from an SQL query
  protected Enumeration getRules (String q, Connection conn) {
    return getRuleInfo(q, conn).elements();
  }

  /**
   * Fetch every rule in the database.
   * @return the whole shebang
   */
  public Enumeration getAllRules (Connection conn) {
    String q = qbase + " where qr.is_active = 'Y'";
    return getRules(q, conn);
  }
  public Enumeration getAllRules () {
    Connection conn = this.getConnection();
    String q = qbase + " where qr.is_active = 'Y'";
    return getRules(q, conn);
  }


  public QRule getNamedRuleFromCache(String name) {
    Enumeration e = rules_cache.elements();
    while (e.hasMoreElements()) {
      QRule rule = (QRule)e.nextElement();
      if (rule.getName().equals(name))
        return rule;
    }
    System.err.println("XXXXXXXXX  Error in QRuleFactory::getNamedRuleFromCache - No rule named " + name);
    return null;
  }

  /**
   * Make an operand out of these database data.
   */
  private QRuleOperand processOperand (
      String opType, String type, Object value, Object opAccID)
      throws Exception
  {
    QRuleOperand op = null;

    if (opType.equals("Literal")) {
      if (value == null) {
        op = new QRuleLiteralOperand("");
      }
      else if (type.equals("String")) {
        op = new QRuleLiteralOperand((String) value);
      }
      else if (type.equals("Boolean")) {
        op = new QRuleLiteralOperand(new Boolean((String) value));
      }
      else if (type.equals("Integer")) {
        op = new QRuleLiteralOperand(new Integer((String) value));
      }
      else if (type.equals("Long")) {
        op = new QRuleLiteralOperand(new Long((String) value));
      }
      else if (type.equals("Float")) {
        op = new QRuleLiteralOperand(new Float((String) value));
      }
      else if (type.equals("Date")) {
        op = new QRuleLiteralOperand(dateFormatter.parse((String) value));
      }
      else if (type.equals("Vector")) {
        String listType = null;
        Vector v = new Vector();
        op = new QRuleLiteralOperand(parseStringList((String) value));
      }
      else {
        op = new QRuleLiteralOperand(value);
      }
    }
    else {
      op = (QRuleOperand) operands.get(opAccID);
    }
    return op;
  }

  // take sequence of double-quote-delimited strings and produce a Vector of
  // String objects.  Any punctuation, white space, or text found between
  // adjacent strings is ignored.  An Exception is thrown if the argument
  // contains an odd number of double-quotes characters.
  public static Vector parseStringList (String list) throws Exception {
    Vector v = new Vector();
    int n = 0;
    while (n < list.length()) {
      int i = list.indexOf("\"", n);
      if (i == -1) {
        // no more strings are in the list
        break;
      }
      else {
        int j = list.indexOf("\"", i + 1);
        if (j == -1) {
          // opening quotes found but closing quotes not found--this is an error
          throw new Exception("DATABASE ERROR--Invalid list format " +
            "(possibly resulting from the list being too long):\n  " +
            list);
        }
        else {
          // snag the quote-delimited string and add it to the Vector
          v.addElement(list.substring(i + 1, j));
          n = j + 1;
        }
      }
    }
    return v;
  }

  // take a space-delimited sequence of integers and produce a Vector of
  // objects of type Long
  public static Vector parseIntegerList (String list) throws Exception {
    Vector v = new Vector();
    StringTokenizer tok = new StringTokenizer(list, ",; \t");
    while (tok.hasMoreTokens()) {
      v.addElement(new Long(tok.nextToken()));
    }
    return v;
  }

  // take a space-delimited sequence of decimal numbers and produce a Vector of
  // objects of type Double
  public static Vector parseFloatList (String list) throws Exception {
    Vector v = new Vector();
    StringTokenizer tok = new StringTokenizer(list, ",; \t");
    while (tok.hasMoreTokens()) {
      v.addElement(new Double(tok.nextToken()));
    }
    return v;
  }

  // take a space-delimited sequence of tokens and produce a Vector of String
  // objects
  private Vector parseTokenList (String list) {
    Vector v = new Vector();
    StringTokenizer tok = new StringTokenizer(list, " ");
    while (tok.hasMoreTokens()) {
      v.addElement(tok.nextToken());
    }
    return v;
  }

  // Convert the argument to a string, enclosing it in quotation marks,
  // if it's already a string
  private String quoteStrings (Object o) {
    if (o instanceof String)
      return "\"" + o + "\"";
    return o.toString();
  }

  /**
   *  Give a string representation of a Vector for purposes of storing it in
   *  the database.  As such, the format of the vector is as a space-delimited
   *  list, with string values being set in double quotes and numeric values
   *  being represented as decimal numerals (without quotes)
   *  <br><br>
   *  If the argument is not a vector, revert to the definition in Factory
   *  @param v The object to be converted to string
   *  @return the string value of the argument
   */
  protected String stringize (Object v) {
    if (v == null)
      return "null";
    else if (v instanceof Vector) {
      StringBuffer buf = new StringBuffer();
      Enumeration enu = ((Vector) v).elements();
      if (enu.hasMoreElements()) {
        buf.append(quoteStrings(enu.nextElement()));
        while (enu.hasMoreElements()) {
          buf.append(" ");
          buf.append(quoteStrings(enu.nextElement()));
        }
      }
      return super.stringize(buf);
    }
    else if (v instanceof java.util.Date) {
      return super.stringize(dateFormatter.format((java.util.Date) v));
    }
    else {
      return super.stringize(v);
    }
  }

  /**
   *  Check the database for the existence of a QRule that refers to a certain
   *  entity.  A reference is detected by comparing the left- and right-hand
   *  sides of a comparison test with the designator (i.e., the accessor) and
   *  a String value identifying the scrutinized entity
   *  <br><br>
   *  The current implementation does not support lists.
   *
   *  @param designator the internal name of the accessor that would make
   *         reference to the type of entity under investigation
   *  @param value a String name or id for the entity under investigation
   *  @return true if and only if a test referring to the entity is found
   */
  public Vector hasRuleReference (String designator, String value) {
    Vector ret = new Vector();
    if (designator == null || value == null)
      return ret;

    String desigStr = stringize(designator.toUpperCase()).toString();

    StringBuffer buf = new StringBuffer();
    buf.append("select unique r.rule_name");
    buf.append(
      " from temp_qual_rule_test t, temp_qrule_accessor_operand a, temp_qual_rule r");
    buf.append(" where t.operand_1_accessor_id = a.accessor_id");
    buf.append(" and t.rule_id = r.rule_id");
    buf.append(" and (upper(a.internal_name) = ");
    buf.append(desigStr);
    buf.append(" or upper(a.ui_type) = ");
    buf.append(desigStr);
    buf.append(") and upper(t.operand_2_literal_value) = ");
    buf.append(stringize(value.toUpperCase()));

    try {
      ResultSet rs = doQuery(buf.toString(), null);
      while (rs.next())
        ret.addElement(rs.getString(1));
    }
    catch (Exception oh_no) {
      System.out.println(
        "QRuleFactory::hasRuleReference:  ERROR IN QUERY--" + oh_no);
    }
    return ret;
  }

  /**
   *  Check the database for the existence of a QRule with the given name.
   *  @param name the name sought
   *  @return true if and only if the named rule can be found in the database
   */
  public boolean exists (String name) {
    boolean ret = false;
    if (name == null)
      return ret;
    String q_str = "select unique rule_name from temp_qual_rule " +
      "where upper(rule_name) = " + stringize(name.toUpperCase());
    try {
      ResultSet rs = doQuery(q_str, null);
      ret = rs.next();
    }
    catch (Exception oh_no) {
      System.out.println(
        "QRuleFactory::exists:  ERROR IN QUERY--" + oh_no);
    }
    return ret;
  }

  /**
   *  Return a list of the query parameters recognized as valid for finding
   *  rules in the database.
   *  @return the list of parameters
   */
  public Object[] getParameterList () {
    return new String[] {"ruleName", "exactRuleName", "contract", "customer",
      "itemID", "action", "maxRows", "showGlobal", "showLta", "showItem",
      "showTest", "showInactive"};
  }

  /**
   *  Construct a set of Rules.  This method queries the database
   *  to collect a list of matching rules.
   *  The keys available for searching currently are:
   *  <ul>
   *    <li>ruleName -- a substring of the rule's name</li>
   *    <li>contract -- a substring of the Contract ID for LTA-specific
   *      rules</li>
   *    <li>customer -- not actually used, but intended to be a substring of
   *      the requisitioning DODAAC, for rules thereunto appertaining.  It is
   *      not clear that this is a useful search criterion</li>
   *    <li>itemId -- a substring of the NSN for NSN-specific rules</li>
   *    <li>action -- the rule's action (e.g., "Exclude", "Must-use", etc.)</li>
   *    <li>showGlobal -- if non-null, matching global rules are included among
   *      those returned by the query</li>
   *    <li>showLta -- if non-null, matching LTA-specific rules are returned</li>
   *    <li>showItem -- if non-null, matching NSN-specific rules are returned</li>
   *  @param request a hashtable containing keys and values to match
   *  @return the rules that match the query
   */
  protected String makeCountQuery(Map request) {
    //need this to happen at the beginning of processBatchQuery
    initialize();

    String ret = "select count(*) as cnt from temp_qual_rule qr" + whereString(request);
//    System.out.println(ret);
    return ret;
  }
  protected String makeQuery(Map request) {
    String ret = qbase + whereString(request);
//    System.out.println(ret);
    return ret;
  }
  private String whereString(Map request) {
    String ruleName = (String) request.get("ruleName");
    String exactRuleName = (String) request.get("exactRuleName");
    String action = (String) request.get("action");
    boolean showTest = (request.get("showTest") != null);
    boolean showInactive = (request.get("showInactive") != null);

    StringBuffer buf = new StringBuffer();

    if (exactRuleName != null && !exactRuleName.equals("")) {
      if (buf.length() > 0) buf.append(" and ");
      buf.append("UPPER(qr.rule_name) = '" + exactRuleName.toUpperCase() + "'");
    }

    if (ruleName != null && !ruleName.equals("")) {
      if (buf.length() > 0) buf.append(" and ");
      buf.append("UPPER(qr.rule_name) like '" + convertSearchString(ruleName.toUpperCase()) + "'");
    }

    if (action != null && !action.equals("")) {
      if (buf.length() > 0) buf.append(" and ");
      buf.append("qr.action = '" + action + "'");
    }

    if (!showInactive) {
      if (buf.length() > 0) buf.append(" and ");
      buf.append("qr.is_active = 'Y'");
    }

    if (!showTest) {
      if (buf.length() > 0) buf.append(" and ");
      buf.append("(qr.test_rule <> 'T' or qr.test_rule is null)");
    }

    String ret = "";
    if(buf.length() > 0) {
      ret = " where " + buf.toString();
    }
    else {
      ret = buf.toString();
    }
    return ret;
  }

  protected Vector instantiate(ResultSet rs, int startRow, int endRow)
          throws SQLException{

    // store the rules in a Vector
    Vector rules = new Vector();
    Vector incompleteRules = new Vector();
    // holder for rules being constructed
    QRule rule;
    Connection conn = getConnection();

    int row_count = startRow;
    while (row_count < endRow)
    {
//      System.out.println("row count " + row_count);
      if(rs.absolute(row_count)) {
        long dbId = rs.getLong("rule_id");
        long version = rs.getLong("version");
        Long ruleId = new Long(dbId);
        if ((rule = (QRule) rules_cache.check(ruleId, conn)) != null) {
          rules.addElement(rule);
        }
        else {
          rule = new QRule(rs.getString("rule_name"), rs.getString("action"));
          rule.setVersion(version);
          rule.setDatabaseId(dbId);
          rule.setCreationDate(rs.getDate("creation_date"));
          String flag = rs.getString("is_active");
          rule.setActive(flag != null && flag.equalsIgnoreCase("Y"));
          rule.setModificationDate(rs.getDate("modification_date"));
          rules.addElement(rule);
          incompleteRules.addElement(rule);
          rules_cache.store(ruleId, rule);
        }
      }
      // now grab the tests for incomplete rules
      for (Enumeration e = incompleteRules.elements(); e.hasMoreElements(); ) {
        getRuleTests((QRule) e.nextElement(), conn);
      }
      row_count++;
    }
    close_connection(conn);
    return rules;
  }

  public Vector getHistory(QRule rule) {
    try {
      return getHistory(rule, null);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private Vector getHistory(QRule rule, Connection c) {
    //
    // Will return a vector with the modification history data for this rule
    //
    Connection conn = c;
    if (conn == null)
      conn = getConnection();
    try {
      String q = "SELECT MODIFICATION_DATE, DESCRIPTION FROM TEMP_QUAL_RULE_HISTORY WHERE RULE_ID = "
        + rule.getDatabaseId()+ " ORDER BY 1 DESC";
      ResultSet rs = doQuery(q, conn);
      Vector data = new Vector();
      Hashtable entry;
      while (rs.next()) {
        entry = new Hashtable(3);
        entry.put("date", rs.getString(1));
        entry.put("user", rs.getString(2));
        Object desc = rs.getObject(3);
        if (desc == null)
          entry.put("description", "");
        else
          entry.put("description", desc.toString());
        data.add(entry);
      }
      return data;

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (c == null)
        close_connection(conn);
    }
  }

  /**
   * Write the modified rule to the database
   * @param r the rule object to be written to the database
   */
  public synchronized void sync(QRule r) {
    sync(r, null);
  }


  /**
   * Write the modified rule to the database
   * @param r the rule object to be written to the database
   * @param user the FgiUser object that is responsible for making this change
   */
  public synchronized void sync(QRule r, String description)
  {
    Connection conn = getConnection();
    Statement st = null;
    String q;
    boolean newRule = false;
    try {
      conn.setAutoCommit(false);
      st = conn.createStatement();
      r.setModificationDate(new java.util.Date());
      if (r.getDatabaseId() == DBObject.NULL_DATABASE_ID) // a new one
      {
        //
        // Get a new DB ID
        //
        ResultSet rs = st.executeQuery("SELECT RULE_ID_SEQ.NextVal from DUAL");
        rs.next();
        r.setDatabaseId(rs.getLong(1));
        r.setVersion(1);
        newRule = true;
      }
      else
      {
        ResultSet rs = st.executeQuery("SELECT VERSION from TEMP_QUAL_RULE where rule_id = "+r.getDatabaseId());
        rs.next();
        r.setVersion(rs.getLong(1) + 1);
        //
        // Clear out the old stuff
        //
        st.executeUpdate("DELETE from TEMP_QUAL_RULE_TEST where rule_id = " + r.getDatabaseId());
        // Let's not delete/recreate QUAL_RULE entries: use UPDATE instead, preserving foreign key constraints
        // st.executeUpdate("DELETE from QUAL_RULE where rule_id = " + r.getDatabaseId());
        rules_cache.remove(new Long(r.getDatabaseId()));
      }

      //
      //  Store the rule
      //
      Vector cols = new Vector();
      Vector vals = new Vector();
      if (newRule) {
        cols.addElement("RULE_ID"); vals.addElement(new Long(r.getDatabaseId()));
      }
      cols.addElement("VERSION"); vals.addElement(new Long(r.getVersion()));
      cols.addElement("RULE_NAME"); vals.addElement(stringize(r.getName()));
      cols.addElement("TEST_RULE"); vals.addElement(r.isTestRule() ? "'Y'" : "'N'");
      cols.addElement("IS_ACTIVE"); vals.addElement(r.isActive() ? "'Y'" : "'N'");
      cols.addElement("ACTION"); vals.addElement(stringize(r.getAction()));
      cols.addElement("CREATION_DATE"); vals.addElement(hiFiFmt(r.getCreationDate()));
      cols.addElement("MODIFICATION_DATE"); vals.addElement(hiFiFmt(r.getModificationDate()));
      if (newRule)
        q = makeInsertStatement("TEMP_QUAL_RULE", cols, vals);
      else
        q = makeUpdateStatement("TEMP_QUAL_RULE", cols, vals, "where rule_id = " + r.getDatabaseId());
      st.executeUpdate(q);

      //
      // Store the HISTORY entry
      //
      cols.clear();
      vals.clear();
      cols.addElement("RULE_ID"); vals.addElement(new Long(r.getDatabaseId()));
      cols.addElement("MODIFICATION_DATE"); vals.addElement(hiFiFmt(r.getModificationDate()));
      if (description != null) {
        cols.addElement("DESCRIPTION"); vals.addElement(stringize(description)); }
      q = makeInsertStatement("TEMP_QUAL_RULE_HISTORY", cols, vals);
      st.executeUpdate(q);

      //
      // Store each test; do a depth-first search of the expression tree
      //
      QRuleTest qt = r.getTest();
      ruleTestDfs(qt, st);

      // looks OK....
      conn.commit();
      st.close();
      rules_cache.store(new Long(r.getDatabaseId()), r);
    } catch (Exception e)
    {
      try {
        conn.rollback();
        st.close();
      } catch (Exception ex)
      {
        System.err.println("Error rolling back Rule " + r.getName());
      }
      System.err.println("Error syncing Rule " + r.getName());
      e.printStackTrace();
    }
    finally {
      close_connection(conn);
    }
  }

  // Do a DFS traversal of the tests subordinate to the one provided and
  // insert them into the database.  This method calls itself recursively to
  // traverse the entire expression subtree
  private void ruleTestDfs (QRuleTest qt, Statement st) throws Exception {
    QRuleLogicalTest logic = qt.getLogicalTest();
    QRuleComparison atom = qt.getComparison();
    Vector cols = new Vector();
    Vector vals = new Vector();

    // associate a databaseId with this test if it is new
    if (qt.getDatabaseId() == DBObject.NULL_DATABASE_ID) {
      ResultSet rs = st.executeQuery("SELECT RULE_TEST_ID_SEQ.NextVal from DUAL");
      rs.next();
      qt.setDatabaseId(rs.getLong(1));
    }
    // populate the rule_id, rule_test_id, and parent_test_id fields
    cols.addElement("RULE_ID");
    vals.addElement(new Long(qt.getRule().getDatabaseId()));
    cols.addElement("RULE_TEST_ID");
    vals.addElement(new Long(qt.getDatabaseId()));
    cols.addElement("parent_test_id");
    QRuleLogicalTest parent = qt.getParent();
    if (parent == null)
      vals.addElement(new Long(DBObject.NULL_DATABASE_ID));
    else
      vals.addElement(new Long(parent.getDatabaseId()));
      // Note:  By the time this call is reached, the parent will already have
      // been processed, so it will have a valid databaseId

    if (logic != null) {
      // populate the logical_operator field
      cols.addElement("logical_operator");
      vals.addElement(stringize(logic.getLogicalOp()));
      cols.addElement("logical_test_name");
      vals.addElement(stringize(logic.getName()));
      st.executeUpdate(makeInsertStatement("TEMP_QUAL_RULE_TEST", cols, vals));
      // now proceed to the operands of this logical operator
      Enumeration operands = logic.getOperands();
      while (operands.hasMoreElements())
        ruleTestDfs((QRuleTest) operands.nextElement(), st);
    }
    else if (atom != null) {
      // Operand 1
      if (atom.getOperand1().isLiteral()) {
        QRuleLiteralOperand lo = (QRuleLiteralOperand) atom.getOperand1();
        cols.addElement("OPERAND_1_TYPE"); vals.addElement(stringize("Literal"));
        cols.addElement("OPERAND_1_LITERAL_TYPE"); vals.addElement(stringize(lo.getType()));
        cols.addElement("OPERAND_1_LITERAL_VALUE"); vals.addElement(stringize(lo.getValue()));
        cols.addElement("OPERAND_1_ACCESSOR_ID"); vals.addElement("NULL");
      }
      else {
        QRuleAccessorOperand ao = (QRuleAccessorOperand) atom.getOperand1();
        cols.addElement("OPERAND_1_TYPE"); vals.addElement(stringize("Accessor"));
        cols.addElement("OPERAND_1_LITERAL_TYPE"); vals.addElement("NULL");
        cols.addElement("OPERAND_1_LITERAL_VALUE"); vals.addElement("NULL");
        cols.addElement("OPERAND_1_ACCESSOR_ID"); vals.addElement(new Long(ao.getDatabaseId()));
      }
      // Operand 2
      if (atom.getOperand2().isLiteral()) {
        QRuleLiteralOperand lo = (QRuleLiteralOperand) atom.getOperand2();
        String type = lo.getType();
        cols.addElement("OPERAND_2_TYPE"); vals.addElement(stringize("Literal"));
        cols.addElement("OPERAND_2_LITERAL_TYPE"); vals.addElement(stringize(lo.getType()));
        cols.addElement("OPERAND_2_LITERAL_VALUE"); vals.addElement(stringize(lo.getValue()));
        cols.addElement("OPERAND_2_ACCESSOR_ID"); vals.addElement("NULL");
      }
      else {
        QRuleAccessorOperand ao = (QRuleAccessorOperand) atom.getOperand2();
        cols.addElement("OPERAND_2_TYPE"); vals.addElement(stringize("Accessor"));
        cols.addElement("OPERAND_2_LITERAL_TYPE"); vals.addElement("NULL");
        cols.addElement("OPERAND_2_LITERAL_VALUE"); vals.addElement("NULL");
        cols.addElement("OPERAND_2_ACCESSOR_ID"); vals.addElement(new Long(ao.getDatabaseId()));
      }
      // Operator
      cols.addElement("OPERATOR_ID"); vals.addElement(new Long(atom.getOperator().getDatabaseId()));
      st.executeUpdate(makeInsertStatement("TEMP_QUAL_RULE_TEST", cols, vals));
    }
  }

  /**
   *  delete a rule from the database
   *  @param q the rule to be deleted
   */
  public void delete (QRule q) {
    // first remove it from the local cache
    rules_cache.remove(new Long(q.getDatabaseId()));

    // next, delete the rule and its tests
    Connection conn = getConnection();
    Statement st = null;
    try {
      st = conn.createStatement();
      // nix the tests
      st.executeUpdate("delete from temp_qual_rule_test where rule_id = " + q.getDatabaseId());
      // nix the history
      st.executeUpdate("delete from temp_qual_rule_history where rule_id = " + q.getDatabaseId());
      // nix the rule itself
      st.executeUpdate("delete from temp_qual_rule where rule_id = " + q.getDatabaseId());
      conn.commit();
      st.close();
    }
    catch (Exception oh_no) {
      System.out.println("QRuleFactory::delete:  SQL ERROR--" + oh_no);
      try {
        conn.rollback();
        st.close();
      }
      catch (Exception disaster) {
        System.out.println("QRuleFactory::delete:  UNABLE TO ROLLBACK!--" + disaster);
      }
    }
    finally {
      close_connection(conn);
    }
  }

  /**
   * A test stub
   */
  public static void main(String[] args) {
    QRuleFactory factory = QRuleFactory.getInstance();

    initializeTest(args, factory);

    try {
      Enumeration e = factory.getAllRules(factory.getConnection());
      while (e.hasMoreElements()) {
        QRule currentRule = (QRule)e.nextElement();
        factory.sync(currentRule, null);
        System.out.println("name: " +currentRule.getName());
        System.out.println("english: " +currentRule.toEnglish());
        System.out.println("cmd: "+currentRule.toJESS() );
        Enumeration eo = currentRule.getTest().getOperands();
        while(eo.hasMoreElements()) {
          QRuleTest op = (QRuleTest)eo.nextElement();
        }
      }
    } catch (Exception e)
    {
      System.out.println("Error in QRuleFactory::main:"+e);
      e.printStackTrace();
    }
  }
}

