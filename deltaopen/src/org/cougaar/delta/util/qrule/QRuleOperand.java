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
 * Represents a Qualification Rule Operand
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: QRuleOperand.java,v 1.1 2002-04-30 17:33:28 cerys Exp $
 */
public interface QRuleOperand {
  /**
   * Indicates whether this operand is a literal value (true) or a computed attribute lookup.
   */
  boolean isLiteral();

  /**
   * Generates a portion of a JESS defrule form that corresponds to this QRuleOperand.
   * @return the JESS representation of this rule test operand
   */
  public String toJESS();

  /**
   * Generate a psuedo-readable version of the rule operand
   */
  public String ruleToString();

  /**
   * Return the operand's type.  Things like "Integer" or "String" would be appropriate.
   */
  public String getType();
}
