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

package org.cougaar.delta.plugin;

import org.cougaar.core.blackboard.IncrementalSubscription;

import org.cougaar.core.util.UID;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.glm.ldm.Constants;

import org.cougaar.delta.util.qrule.QRule;
import org.cougaar.delta.util.qrule.QRuleFactory;
import org.cougaar.delta.util.qrule.QualifiableCandidate;
import org.cougaar.delta.util.SampleLoanCandidate;

import org.cougaar.core.agent.service.alarm.Alarm;

import java.io.IOException;

import org.cougaar.util.KeyedSet;
import org.cougaar.util.UnaryPredicate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Iterator;
import java.math.BigDecimal;
import jess.JessException;
import jess.*;

/**
 * Qualify a ProposalSet by checking the proposals against a set of rules.
 * @author ALPINE (alpine-software@bbn.com)
 */

public class SampleQualifierPlugin extends BasicPlugin {
  private final static String PLUGIN_NAME = "SampleQualifierPlugin";
  private Rete rete;
  private QRuleFactory ruleFactory;
  private Vector allRules = new Vector();
  private static boolean isJESSDebugging = false;
  private Alarm testAlarm;
  boolean done =false;

  /**
   *  Indicate if properties are needed.
   *  @return always returns true since we need properties.
   */
  public boolean hasProperties () {
    return true;
  }

  private void setupMySubscriptions() {
    if(testAlarm == null) {
      testAlarm = this.wakeAfter(20000);
    }
  }

  private void getProperties() {
  }
  public void initializeDELTAPlugin() throws Exception {
    try
    {
      setupMySubscriptions();
      ruleFactory = QRuleFactory.getInstance();

      getProperties();

      Enumeration rules = ruleFactory.getAllRules(this.getDatabaseConnection());
      while(rules.hasMoreElements()) {
        allRules.add(rules.nextElement());
      }
      // Start Jess
      startJess();
    }
    catch (Exception e)
    {
        throw new Exception(PLUGIN_NAME + "::load:ERROR " + e.getMessage());
    }
  }


  public String getPlugInName() {
    return PLUGIN_NAME;
  }

  /**
   *  The first time execute is called, three SampleLoanCandidates are
   *  instantiate, qualified, and published.
   */
  public void doExecute() throws Exception {

    try
    {
      if(!done) {
        SampleLoanCandidate slc = new SampleLoanCandidate("Jerry Marsh", 2400.00f, 2);
        slc.setLiquidAssets(10.0f);
        slc.setMonthlyIncome(80.0f);
        slc.setRecentDefault(true);
        slc.setTotalAssets(200.0f);
        slc.setTotalDebt(20.0f);
        qualifyCandidate(slc);
        publishAdd(slc);
        SampleLoanCandidate slc2 = new SampleLoanCandidate("Sue Donnell", 3000.00f, 1);
        slc2.setLiquidAssets(1000.0f);
        slc2.setMonthlyIncome(250.0f);
        slc2.setRecentDefault(false);
        slc2.setTotalAssets(200.0f);
        slc2.setTotalDebt(300.0f);
        qualifyCandidate(slc2);
        publishAdd(slc2);
        SampleLoanCandidate slc3 = new SampleLoanCandidate("Tom Fein", 100.00f, 4);
        slc3.setLiquidAssets(00.0f);
        slc3.setMonthlyIncome(100.0f);
        slc3.setRecentDefault(false);
        slc3.setTotalAssets(200.0f);
        slc3.setTotalDebt(300.0f);
        qualifyCandidate(slc3);
        publishAdd(slc3);
        String qualString = "";
        Vector v =slc.getQualifications();
        for(int i = 0; i< v.size(); i++) {
          qualString = qualString + v.elementAt(i) + " * ";
        }
        System.out.println("candidate "
                     + ",  " + slc.getQualification() + ":  " + "\n"
                     + qualString);
        qualString = "";
        v =slc2.getQualifications();
        for(int i = 0; i< v.size(); i++) {
          qualString = qualString + v.elementAt(i) + " * ";
        }
        System.out.println("candidate "
                     + ",  " + slc2.getQualification() + ":  " + "\n"
                     + qualString);
        qualString = "";
        slc3.getQualifications();
        for(int i = 0; i< v.size(); i++) {
          qualString = qualString + v.elementAt(i) + " * ";
        }
        System.out.println("candidate "
                     + ",  " + slc3.getQualification() + ":  " + "\n"
                     + qualString);

        done = true;
      }
    }
    catch (Exception e) {
      throw new Exception(PLUGIN_NAME + "::doExecute:ERROR "+ e.getMessage());
    }
  }


  /**
   *  Qualify a Candidate
   *  @param candidate the Candidate to qualify
   */

  private void qualifyCandidate(SampleLoanCandidate candidate)
      throws Exception
  {

    Vector rule_names = new Vector();

    Enumeration rules = ruleFactory.getAllRules(this.getDatabaseConnection());

    while (rules.hasMoreElements()) {
      QRule r = (QRule)rules.nextElement();
      Vector cmds = r.toJESS();
      for (int i = 0; i < cmds.size(); i++) {
        String cmd = (String)cmds.elementAt(i);
        rule_names.addElement(getRuleName(cmd));
        rete.executeCommand (cmd);
      }
    }

    // run rules on each proposal

    rete.store("DATE-FORMATTER", new SimpleDateFormat("MM/dd/yyyy"));
    rete.store("PLUGIN", this);
    rete.store("CANDIDATE", candidate);
    rete.executeCommand("(definstance candidate (fetch CANDIDATE) static)");
    rete.run();
    rete.executeCommand("(undefinstance (fetch CANDIDATE))");

    // remove rules from Jess
    rete.clearStorage();

    int i = 0;
    for (i = 0; i < rule_names.size(); i++) {

      String cmd = "(undefrule " + rule_names.elementAt(i) + ")";
      rete.executeCommand (cmd);
    }

    System.out.println("candidate " + candidate);
        String qualString = "";
        Vector v =candidate.getQualifications();
        for(int j = 0; j< v.size(); j++) {
          qualString = qualString + v.elementAt(j) + " * ";
        }
        System.out.println("candidate "
                     + ",  " + candidate.getQualification() + "  " + candidate.getReason() + "\n\n"
                     + "all qualifications " + qualString);
    return;
  }


  /**
   * Extracts the Rule name from a String representation of a JESS rule.
   * @param rule the rule
   * @return the name of the rule
   */
  private static String getRuleName(String rule) {
    int firstSpace = rule.indexOf(' ');
    int secondSpace = rule.indexOf(' ',firstSpace + 1);
    return rule.substring(firstSpace + 1, secondSpace);
  }

  /**
   *  Start the Jess rule engine
   */
  private void startJess () throws JessException, ClassNotFoundException {
    String cmd;
      // Create Rule Engine
      rete = new Rete();
      rete.addUserpackage(new jess.BagFunctions());
      rete.addUserpackage(new jess.MathFunctions());
      rete.addUserpackage(new jess.MiscFunctions());
      rete.addUserpackage(new jess.MultiFunctions());
      rete.addUserpackage(new jess.PredFunctions());
      rete.addUserpackage(new jess.StringFunctions());
      rete.addUserpackage(new jess.ReflectFunctions());
      rete.addUserpackage(new jess.ViewFunctions());

      // Initialize jess
      rete.clear();
      if (isJESSDebugging)
        rete.executeCommand ("(watch all)");

      defineClassForJESS(rete, "BasicPlugin",
        "org.cougaar.delta.plugin.BasicPlugin");

      // Create some Jess functions

      // date_after tests to see if date_2 is after date_1
      cmd = "(deffunction dateAfter (?date_1 ?date_2) "
        + "(bind ?b (call ?date_1 after ?date_2)) "
        + "(return ?b) "
        + ")";
      rete.executeCommand(cmd);

      cmd = "(deffunction dateBefore (?date_1 ?date_2) "
        + "(bind ?b (call ?date_1 before ?date_2)) "
        + "(return ?b) "
        + ")";
      rete.executeCommand(cmd);

      //TODO
      cmd = "(deffunction dateSameDay (?date_1 ?date_2) "
        + "(bind ?b (call ?date_2 after ?date_1)) "
        + "(return ?b) "
        + ")";
      rete.executeCommand(cmd);

      cmd = "(deffunction create-date (?date_string) "
        + "(bind ?date (call (fetch DATE-FORMATTER) parse ?date_string)) "
        + (isJESSDebugging ? "(printout t \"++++++++++ create-date: \" ?date_string \" ++++++++++\" crlf) " : "")
        + (isJESSDebugging ? "(printout t \"++++++++++ create-date: \" ?date \" ++++++++++\" crlf) " : "")
        + "(return ?date) "
        + ")";
      rete.executeCommand(cmd);

      // startsWith
      cmd = "(deffunction startsWith (?string1 ?string2) "
        + "(bind ?b (call ?string1 startsWith ?string2)) "
        + "(return ?b) "
        + ")";
      rete.executeCommand(cmd);

      // endsWith
      cmd = "(deffunction endsWith (?string1 ?string2) "
        + "(bind ?b (call ?string1 endsWith ?string2)) "
        + "(return ?b) "
        + ")";
      rete.executeCommand(cmd);

      // getX2$ - follow accessor chain
      // allows one argument per field inside []
      cmd = "(deffunction getX2$ (?object $?fields) "
        + (isJESSDebugging ? "(printout t crlf \"++++++++++ START of getX2$ ++++++++++\" crlf) " : "")
        + (isJESSDebugging ? "(printout t \"++++++++++ starting object: \" ?object \" ++++++++++\" crlf) " : "")
        + "(foreach ?f $?fields "
        +      (isJESSDebugging ?
                "(printout t \"++++++++++ f: \" ?f \" ++++++++++\" crlf) " : "")
        +     "(bind ?index \"\") "
        +     "(bind ?i (str-index \"[\" ?f)) "
        +     "(bind ?j (str-index \"]\" ?f)) "
        +     "(if ?i then "
        +         "(bind ?nf (sub-string 1 (- ?i 1) ?f)) "
        +         "(bind ?index (sub-string (+ ?i 1)(- ?j 1) ?f)) "
        +         "(bind ?f ?nf) "
        +          (isJESSDebugging ?
                    "(printout t \"++++++++++ index: \" ?index \" ++++++++++\" crlf) " : "")
        +      ") "
        +      "(bind ?name ?f) "
        +        (isJESSDebugging ?
                  "(printout t \"++++++++++ name: \" ?name \", index: \'\" ?index \"\' ++++++++++\" crlf) " :"")
        +      "(if (> (str-length ?index) 0) "
        +          "then (bind ?object (call ?object ?name ?index)) "
        +          "else (bind ?object (call ?object ?name))"
        +      ") "
        +      (isJESSDebugging ?
                "(printout t \"++++++++++ next object: \" ?object \" ++++++++++\" crlf) " : "")
        + ")"
        + "(return (valueOf ?object)) "
        + ")";
      rete.executeCommand(cmd);

      // valueOf - get value of object
      cmd = "(deffunction valueOf (?object) "
        + "(if (not (external-addressp ?object))"
        +     "then (return ?object)) "
        + "(bind ?class (call ?object getClass))"
        + "(bind ?cname (call ?class getName))"
        //                    + "(printout t \"++++++++++ object type: \" ?cname \" ++++++++++\" crlf) "
        + "(if (eq \"java.lang.String\" ?cname)"
        +     "then (return (call ?object toString))) "
        + "(if (eq \"java.lang.Character\" ?cname)"
        +     "then (return (call ?object charValue))) "
        + "(if (eq \"java.lang.Integer\" ?cname)"
        +     "then (return (call ?object intValue))) "
        + "(if (eq \"java.lang.Double\" ?cname)"
        +     "then (return (call ?object doubleValue))) "
        + "(if (eq \"java.math.BigDecimal\" ?cname)"
        +     "then (return (call ?object doubleValue))) "
        + "(if (eq \"java.lang.Boolean\" ?cname)"
        +     "then (return (call ?object booleanValue))) "
        //                    + "(printout t \"++++++++++ did not convert object: \" ?object \" ++++++++++\" crlf) "
        + "(return ?object) "
        + ")";
      rete.executeCommand(cmd);

      // getXa - wrapper for getX2$ that takes a blank separated accessor list string
      cmd = "(deffunction getXa (?object ?accessor) "
        + "(bind $?fields (explode$ ?accessor)) "
        + "(return (getX2$ ?object $?fields)) "
        + ")";
      rete.executeCommand(cmd);

      // Define the classes we will pass to Jess
      defineClassForJESS(rete, "candidate",
        "org.cougaar.delta.util.SampleLoanCandidate");
  }

  private static void defineClassForJESS(Rete rete, String jessName, String javaName) throws JessException, ClassNotFoundException {
    Class.forName(javaName);
    rete.executeCommand("(defclass " + jessName + " " + javaName + ")");
  }


  // - - - - - - - Testing Code - - - - - - - - - - - - - - - - - - - - - - - -
  // A test stub that runs the global rule update mechanism upon demand.  The
  // aspiring tester can use the servlets to modify the rules in the meantime
  // to see if it is working properly.
  public static void main (String[] argv) {

  /*
    java.io.BufferedReader bufr = null;
    try {
      bufr = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
    }
    catch (RuntimeException e) {
      e.printStackTrace();
    }
    Rete rete = makeRete();
    Vector globalRules = new Vector();
    QRuleFactoryProxy ruleFact = QRuleFactory.getProxy(null);
    QRuleFactory.getInstance().setDbConnectionString(
      "jdbc:oracle:thin:@delta.alpine.bbn.com:1521:fgi,gdonovan,raziloY");
    for (boolean done = false; !done; ) {
      try {
        Enumeration enu = rete.listDefrules();
        System.out.println("Current Rules:");
        while (enu.hasMoreElements())
          System.out.println("  -> " + enu.nextElement());
        System.out.print("Ready> ");
        if (bufr.readLine().equalsIgnoreCase("q"))
          done = true;
        else
          globalRules = updateGlobalRules(rete, ruleFact, globalRules);
      }
      catch (Exception bugger) {
        System.out.println("ERROR!");
        bugger.printStackTrace();
        done = true;
      }
    }
    System.out.print("Exiting; press return");
    try {
      bufr.readLine();
    }
    catch (Exception bugger) {
    }*/
  }

  // create the JESS rule engine
  private static Rete makeRete () {
    Rete rete = new Rete();
    rete.addUserpackage(new jess.BagFunctions());
    rete.addUserpackage(new jess.MathFunctions());
    rete.addUserpackage(new jess.MiscFunctions());
    rete.addUserpackage(new jess.MultiFunctions());
    rete.addUserpackage(new jess.PredFunctions());
    rete.addUserpackage(new jess.StringFunctions());
    rete.addUserpackage(new jess.ReflectFunctions());
    rete.addUserpackage(new jess.ViewFunctions());
    try {
      rete.clear();
      defineClassForJESS(rete,
        "candidate","org.cougaar.delta.util.qrule.QualifiableCandidate");
    }
    catch (Exception pos) {
    }
    return rete;
  }


}
