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
import java.beans.*;
import java.util.Vector;
import java.util.Enumeration;
import org.cougaar.delta.util.DBObject;

/**
 * Represents a Qualification Rule Operator
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: QRuleOperator.java,v 1.1 2002-04-30 17:33:28 cerys Exp $
 */
public class QRuleOperator extends DBObject {
  private boolean invokedStandalone = false;
  private String jessName;
  private String uiName;
  private String operand1Type;
  private String operand2Type;

  public QRuleOperator(String newJessName, String newUIName,
      String newOperand1Type, String newOperand2Type)
  {
    jessName = newJessName;
    uiName = newUIName;
    operand1Type = newOperand1Type;
    operand2Type = newOperand2Type;
  }

  public String getJessName() {
    return jessName;
  }

  public String getUiName() {
    return uiName;
  }

  public void setUiName(String altUIName) {
    uiName = altUIName;
  }

  public String getOperand1Type() {
    return operand1Type;
  }

  public String getOperand2Type() {
    return operand2Type;
  }

  /**
   * Generate a psuedo-readable version of the rule test
   */
  public String ruleToString() {
    return jessName;
  }

  public String toString () {
    return uiName;
  }

  public boolean equals (Object o) {
    if (!(o instanceof QRuleOperator))
      return false;
    return jessName.equals(((QRuleOperator) o).jessName);
  }
}
