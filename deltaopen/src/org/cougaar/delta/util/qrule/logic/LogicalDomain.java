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

package org.cougaar.delta.util.qrule.logic;

import org.cougaar.delta.util.qrule.*;
import java.util.*;

/**
 *  A LogicalDomain is a logical view of the conditions under which a rule will
 *  fire, as specified by the tests that make up the rule's logical structure.
 *  At the time of construction, the rule to be examined is subdivided into
 *  its main clause and various exception clauses (represented by instances of
 *  ConditionClause), which, in turn, are analyzed into AttributeRanges
 *  reflecting the constraints set on each referenced attribute.
 *  <br><br>
 *  Upon demand, a LogicalDomain can check its constituency for errors in the
 *  logical structure, which are elucidated by the foregoing analysis.  In the
 *  future, it will also be able to do containment and exclusion comparisons
 *  with other domains.
 */
public class LogicalDomain {
  private QRuleLogicalTest root = null;
  private ConditionClause mainClause = null;
  private Vector exceptions = new Vector();

  /**
   *  Create the LogicalDomain corresponding to a given rule
   */
  public LogicalDomain (QRule r) {
    root = r.getTest();
    parseRule();
  }

  // Assemble the conditions into lists belonging to the main clause and the
  // various exception clauses.
  private void parseRule () {
    Vector mainConds = new Vector();

    for (Enumeration e = root.getOperands(); e.hasMoreElements(); ) {
      QRuleTest t = (QRuleTest) e.nextElement();
      addException(t.getLogicalTest());
    }

    mainClause = new ConditionClause(root);
  }

  // go through the conditions of an exception clause and assemble them into
  // a list.
  private void addException (QRuleLogicalTest qlt) {
    // ignore if it's not a logical test or it's not an exception clause
    if (qlt == null || !qlt.getLogicalOp().equals(QRuleTest.LOGICAL_NAND))
      return;

    exceptions.add(new ConditionClause(qlt.getName(), qlt));
  }

  public Vector analyze () {
    Vector messages = new Vector();

    // first examine the main clause for internal consistency
    Vector clauseAnalysis = mainClause.isConsistent();
    for (Enumeration e = clauseAnalysis.elements(); e.hasMoreElements(); )
      messages.add("In the main clause:  " + e.nextElement());

    // then examine the exception clauses in the context of the main clause
    for (Enumeration e = exceptions.elements(); e.hasMoreElements(); ) {
      ConditionClause ex = (ConditionClause) e.nextElement();
      clauseAnalysis = ex.isConsistent(mainClause);
      for (Enumeration f = clauseAnalysis.elements(); f.hasMoreElements(); )
        messages.add("In " + exAppellation(ex) + ":  " + f.nextElement());
    }

    // finally, compare the exception clauses in case of redundancy
    for (Enumeration e = exceptions.elements(); e.hasMoreElements(); ) {
      ConditionClause ex = (ConditionClause) e.nextElement();
      for (Enumeration f = exceptions.elements(); f.hasMoreElements(); ) {
        ConditionClause fx = (ConditionClause) f.nextElement();
        if (fx != ex && ex.contains(fx))
          messages.add(exAppellation(fx) + " implies " + exAppellation(ex));
      }
    }

    return messages;
  }

  private String exAppellation (ConditionClause cc) {
    String name = cc.getName();
    if (name == null)
      return "unnamed exception";
    else
      return "exception \"" + name + "\"";
  }
}
