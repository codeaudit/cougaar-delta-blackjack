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

import org.cougaar.delta.util.variant.*;
import org.cougaar.delta.util.*;
import org.cougaar.delta.util.html.*;
import org.cougaar.delta.util.xml.*;
import org.w3c.dom.*;
import org.apache.xerces.parsers.DOMParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class QRuleToXml {


  private static final String indent = "  ";
  private static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
  protected QRuleFactory factory = null;
  protected QueryTable qTable = null;

  protected QRuleToXml () {
  }

  /**
   *  Create a QRule-to-XML converter configured to use the given QRuleFactory
   *  instance.
   */
  public QRuleToXml (QRuleFactory f) {
    factory = f;
    qTable = f.getQueryTable();
    qTable.put("showInactive", "Y");
    qTable.put("showTest", "Y");
  }

  /**
   *  Provide command-line support for importing and exporting rules
   */
  public static void main (String[] argv) {
    if (argv.length < 3) {
      printUsage("Insufficient arguments");
    }
    else {
      String dbConnect = argv[0];
      String command = argv[1];
      QRuleToXml instance = null;
      try {
        instance = new QRuleToXml(configureFactory(dbConnect));
      }
      catch (RuntimeException e) {
        System.out.println("Error connecting to database--" + e);
        return;
      }
      if (command.equalsIgnoreCase("import")) {
        FileInputStream file = null;
        boolean activate = false;
        if (argv.length > 3 && argv[3].equals("Y"))
          activate = true;
        try {
          file = new FileInputStream(argv[2]);
          QRule q[] = instance.parse(file);
          q[0].setActive(activate);
          q[0].setCreationDate(new java.util.Date());
          instance.saveRulesToDb(q);
        }
        catch (IOException fileError) {
          System.out.println("Unable to read file " + argv[2]);
        }
        catch (Exception parseError) {
          System.out.println("Unable to parse file " + argv[2]);
        }
      }
      else if (command.equalsIgnoreCase("export") && argv.length == 4) {
        FileOutputStream file = null;
        try {
          file = new FileOutputStream(argv[3]);
          instance.export(instance.getRuleFromDb(argv[2]), file);
        }
        catch (IOException fileError) {
          System.out.println("Error opening file " + argv[3]);
        }
        catch (Exception badRuleName) {
          System.out.println("Unable to export:  " + badRuleName);
        }
      }
      else if (command.equalsIgnoreCase("mass-export") && argv.length > 3) {
        FileOutputStream file = null;
        try {
          file = new FileOutputStream(argv[argv.length - 1]);
          Vector rules = new Vector();
          for (int i = 2; i < argv.length - 1; i++) {
            try {
              rules.addElement(instance.getRuleFromDb(argv[i]));
            }
            catch (Exception badRuleName) {
              System.out.println(
                "Unable to fetch rule \"" + argv[i] + "\"--" + badRuleName);
            }
          }
          if (rules.size() > 0)
            instance.mass_export(rules, file);
        }
        catch (IOException fileError) {
          System.out.println("Error opening file " + argv[3]);
        }
      }
      else {
        printUsage("Unsupported command \"" + command + "\"");
      }
    }
  }

  private static QRuleFactory configureFactory (String db) {
    QRuleFactory qrf = QRuleFactory.getInstance();
    qrf.setDbConnectionString(db);
    return qrf;
  }

  private QRule getRuleFromDb (String ruleName) throws Exception {
    qTable.put("exactRuleName", ruleName);
    DataWrapper[] dw = factory.query(qTable).getRows();
    if (dw.length == 0)
      throw new Exception("Rule \"" + ruleName + "\" does not exist");
    else
      return (QRule) dw[0].unwrap();
  }

  private void saveRulesToDb (QRule[] rules) {
    for (int i = 0; i < rules.length; i++)
      factory.sync(rules[i], "Rule imported from XML file");
  }

  private static void printUsage (String message) {
    System.out.println(message + ".  Usage:");
    System.out.println("  java QRuleToXml <db_connection_string> import <xml-file> [<activate-flag>]");
    System.out.println("  an activate-flag value of Y will cause the rule to be activated, otherwise it will be inactive");
    System.out.println("  java QRuleToXml <db_connection_string> export <rule-id> <xml-file>");
    System.out.println(
      "  java QRuleToXml <db_connection_string> mass-export <rule-id> [<rule-id> ...] <xml-file>");
  }

  public QRule[] parse (InputStream in) throws Exception {
    return parse(new BufferedReader(new InputStreamReader(in)));
  }

  public QRule[] parse (BufferedReader bufr) throws Exception {
    //Parser p = new Parser(".");
    //TXDocument txt = p.readStream(bufr);
    DOMParser p = new DOMParser();
    //No need to do the following since we do normal DTD handling
    //p.setEntityResolver(new SpecialResolver());
    try {
      p.parse(new InputSource(bufr));
      } catch (SAXException e) {
        System.out.println("Error parsing file: " + bufr);
        e.printStackTrace();
    }
    Document txt = p.getDocument();

    ChildEnumerator ce = new ChildEnumerator(txt);
    Node t = ce.current();
    String type = t.getNodeName();
    if (type.equalsIgnoreCase("ruleset")) {
      ce = new ChildEnumerator(t);
      t = ce.current();
    }
    Vector v = new Vector();
    while (t != null) {
      type = t.getNodeName();
      if (type.equalsIgnoreCase("qrule"))
        v.addElement(makeRule(t));
      else
        System.out.println("Ignoring invalid root tag \"" + type + "\".");
      t = ce.next();
    }
    Enumeration enu = v.elements();
    QRule[] q = new QRule[v.size()];
    for (int i = 0; enu.hasMoreElements(); i++)
      q[i] = (QRule) enu.nextElement();
    return q;
  }

  private QRule makeRule (Node t) throws Exception {
    ChildEnumerator ce = new ChildEnumerator(t);
    Node child = ce.current();
    // look for attribute tags
    String name = null;
    String action = null;
    while (child != null && child.getNodeName().equals("a")) {
      Attribute a = readAttribute(child);
      if (a.name != null) {
        if (a.name.equalsIgnoreCase("name"))
          name = a.value;
        else if (a.name.equalsIgnoreCase("action"))
          action = a.value;
      }
      child = ce.next();
    }
    // Now, maybe we have enough information to construct the rule
    if (name == null)
      throw new Exception("Malformed QRule:  no name is specified");
    if (action == null)
      throw new Exception("Malformed QRule:  no action is specified");
    QRule q = new QRule(name, action);

    if (child != null && child.getNodeName().equalsIgnoreCase("test")) {
      makeTest(q, null, child);
      child = ce.next();
    }

    return q;
  }

  private void makeTest (QRule q, QRuleLogicalTest parent, Node t) {
    NamedNodeExtractor nne = new NamedNodeExtractor(t);
    ChildEnumerator ce = new ChildEnumerator(t);

    String logic = nne.getString("logical");
    if (logic == null) {
      QRuleOperand left = readOperand(ce);
      QRuleOperator op = readOperator(ce);
      QRuleOperand right = readOperand(ce);
      if (left != null && op != null && right != null)
        new QRuleComparison(q, parent, left, op, right);
    }
    else {
      QRuleLogicalTest qlt = new QRuleLogicalTest(q, parent, logic);
      Node child = ce.current();
      while (child != null && child.getNodeName().equalsIgnoreCase("a")) {
        Attribute a = readAttribute(child);
        if (a.name.equalsIgnoreCase("name"))
          qlt.setName(a.value);
        child = ce.next();
      }
      while (child != null && child.getNodeName().equalsIgnoreCase("test")) {
        makeTest(q, qlt, child);
        child = ce.next();
      }
    }
  }

  private QRuleOperator readOperator (ChildEnumerator ce) {
    Node child = ce.current();
    String type = null;
    while (child != null && (type = child.getNodeName()) != null &&
        !type.equalsIgnoreCase("op"))
    {
      child = ce.next();
    }
    ce.next();
    if (child == null || type == null)
      return null;
    String op = readTextContent(child);
    if (op == null)
      return null;
    if (factory == null)
      return new QRuleOperator(op, "UIName", "LeftType", "RightType");
    return factory.getOperatorByName(op);
  }

  private QRuleOperand readOperand (ChildEnumerator ce) {
    Node child = ce.current();
    String type = null;
    while (child != null && (type = child.getNodeName()) != null &&
        !type.equalsIgnoreCase("lit") && !type.equalsIgnoreCase("accessor"))
    {
      child = ce.next();
    }
    ce.next();
    if (child == null || type == null)
      return null;
    if (type.equalsIgnoreCase("lit"))
      return readLiteral(child);
    else if (type.equalsIgnoreCase("accessor"))
      return readAccessor(child);
    else return null;
  }

  private QRuleAccessorOperand readAccessor (Node t) {
    NamedNodeExtractor nne = new NamedNodeExtractor(t);
    String name = nne.getString("name");
    if (name == null)
      return null;
    if (factory == null)
      return new QRuleAccessorOperand(name, "Cagegory", "UIName", "UIType",
        "JessType", "JessAccessor");
    return factory.getAccessorOperandByName(name);
  }

  private QRuleLiteralOperand readLiteral (Node t) {
    NamedNodeExtractor nne = new NamedNodeExtractor(t);
    String type = nne.getString("type");
    Object val = null;
    String valString = readTextContent(t);
    try {
      if (valString == null)
        val = "";
      else if (type.equalsIgnoreCase("String"))
        val = valString;
      else if (type.equalsIgnoreCase("Boolean"))
        val = new Boolean(valString);
      else if (type.equalsIgnoreCase("Float"))
        val = new Float(valString);
      else if (type.equalsIgnoreCase("Integer"))
        val = new Integer(valString);
      else if (type.equalsIgnoreCase("Double"))
        val = new Double(valString);
      else if (type.equalsIgnoreCase("Long"))
        val = new Long(valString);
      else if (type.equalsIgnoreCase("Date"))
        val = dateFormatter.parse(valString);
      else if (type.equalsIgnoreCase("Vector")) {
        Vector v = new Vector();
        val = v;
        StringTokenizer tok = new StringTokenizer(valString, ", ");
        while (tok.hasMoreTokens())
          v.addElement(tok.nextToken());
      }
    }
    catch (Exception e) {
    }
    if (val == null)
      return null;

    return new QRuleLiteralOperand(val);
  }

  private Attribute readAttribute (Node t) {
    NamedNodeExtractor nne = new NamedNodeExtractor(t);
    Attribute a = new Attribute();
    a.name = nne.getString("name");
    a.value = readTextContent(t);

    return a;
  }

  private String readTextContent (Node t) {
    Text valueNode = null;
    NodeList nl = t.getChildNodes();
    int length = nl.getLength();
    for (int i = 0; i < length; i++) {
      Node child = nl.item(i);
      if (child != null && child instanceof Text) {
        valueNode = (Text) child;
        break;
      }
    }
    if (valueNode != null)
      return valueNode.getData();
    return null;
  }

  public void export (QRule q, OutputStream out) {
    PrettyPrinter pp = new PrettyPrinter(new PrintWriter(out));
    pp.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    outputRule(q, pp);
    pp.flush();
  }

  public void mass_export (Vector rules, OutputStream out) {
    PrettyPrinter pp = new PrettyPrinter(new PrintWriter(out));
    pp.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    pp.print("<ruleset>");
    pp.indent();
    Enumeration enu = rules.elements();
    while (enu.hasMoreElements())
      outputRule((QRule) enu.nextElement(), pp);
    pp.exdent();
    pp.print("</ruleset>");
    pp.flush();
  }

  private void outputRule (QRule q, PrettyPrinter pp) {
    pp.print("<qrule>");
    pp.indent();
    outputAttribute(pp, "name", q.getName());
    outputAttribute(pp, "action", q.getAction());

    QRuleLogicalTest root = q.getTest();
    if (root != null) {
      outputLogicalTest(pp, root);
    }

    pp.exdent();
    pp.print("</qrule>");
  }

  private void outputAttribute (PrettyPrinter pp, String name, String value) {
    pp.print("<a name=\"" + name + "\">" + xmlEncode(value) + "</a>");
  }

  private void outputLogicalTest (PrettyPrinter pp, QRuleLogicalTest t) {
    pp.print("<test logical=\"" + t.getLogicalOp() + "\">");
    pp.indent();
    if (t.getName() != null)
      outputAttribute(pp, "name", t.getName());
    for (Enumeration enu = t.getOperands(); enu.hasMoreElements(); ) {
      QRuleTest qt = (QRuleTest) enu.nextElement();
      QRuleLogicalTest logic = null;
      QRuleComparison atom = null;
      if ((logic = qt.getLogicalTest()) != null)
        outputLogicalTest(pp, logic);
      else if ((atom = qt.getComparison()) != null)
        outputComparison(pp, atom);
    }
    pp.exdent();
    pp.print("</test>");
  }

  private void outputComparison (PrettyPrinter pp, QRuleComparison t) {
    pp.print("<test>");
    pp.indent();
    outputOperand(pp, t.getOperand1());
    outputOperator(pp, t.getOperator());
    outputOperand(pp, t.getOperand2());
    pp.exdent();
    pp.print("</test>");
  }

  private void outputOperand (PrettyPrinter pp, QRuleOperand op) {
    if (op.isLiteral())
      pp.print("<lit type=\"" + ((QRuleLiteralOperand) op).getType() +
        "\">" + xmlEncode(op) + "</lit>");
    else
      pp.print("<accessor name=\"" +
        ((QRuleAccessorOperand) op).getInternalName() + "\"/>");
  }

  private void outputOperator (PrettyPrinter pp, QRuleOperator op) {
    pp.print("<op>" + xmlEncode(op.getJessName()) + "</op>");
  }

  private static String xmlEncode (Object o) {
    String s = o.toString();
    if (s == null)
      return null;
    StringBuffer buf = new StringBuffer();
    int n = s.length();
    for (int i = 0; i < n; i++) {
      char c = s.charAt(i);
      if (c == '<' || c == '&' || c == '>' || c == '%' || c == '#')
        buf.append("&#" + (int) c + ";");
      else if (c == '&')
        buf.append("&amp;");
      else if (c == '>')
        buf.append("&gt;");
      else
        buf.append(c);
    }
    return buf.toString();
  }

  private class Attribute {
    public String name = null;
    public String value = null;
  }
}
