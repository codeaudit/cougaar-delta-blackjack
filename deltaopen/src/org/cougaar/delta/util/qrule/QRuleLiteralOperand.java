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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.Enumeration;
import org.cougaar.delta.util.DBObject;

/**
 * Represents a Qualification Rule Literal Operand
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: QRuleLiteralOperand.java,v 1.1 2002-04-30 17:33:28 cerys Exp $
 */
public class QRuleLiteralOperand extends DBObject implements QRuleOperand {

  private boolean invokedStandalone = false;
  private Object value;  // Should be either a String or a number
  private static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

  public QRuleLiteralOperand(Object newValue) {
    value = newValue;
  }

  public boolean isLiteral() {
    return true;
  }

  /**
   * Generates a portion of a JESS defrule form that corresponds to this QRuleOperand.
   * @return the JESS representation of this rule test operand
   */
  public String toJESS() {
    StringBuffer sb = new StringBuffer();
    // Probably want to use the Variant interface here.
    if (value instanceof String)
      sb.append("\"" + value + "\"");
    else if (value instanceof Boolean)
      sb.append(((Boolean) value).booleanValue() ? "TRUE" : "FALSE");
    else if (value instanceof Date)
      sb.append("(create-date \"" + dateFormatter.format(value) + "\")");
    else if (value instanceof Vector) {
      sb.append ("(create$ ");
      Enumeration e = ((Vector)value).elements();
      while (e.hasMoreElements()) {
        Object o = e.nextElement();
        if (o instanceof String)
          sb.append("\"" + o + "\"" + " ");
        else
          sb.append(o + " ");
      }
      sb.append (")");
    } else
      sb.append(value);
    return sb.toString();
  }

  /**
   * Generate a psuedo-readable version of the rule test
   */
  public String ruleToString() {
    String str;
    if (value instanceof String) {
      str = "\"" + value + "\"";
    }
    else if (value instanceof Vector) {
      StringBuffer buf = new StringBuffer();
      Enumeration enu = ((Vector) value).elements();
      if (enu.hasMoreElements()) {
        Object elt = enu.nextElement();
        if (elt instanceof String)
          buf.append("\"" + elt + "\"");
        else
          buf.append(elt.toString());
        while (enu.hasMoreElements()) {
          elt = enu.nextElement();
          buf.append(" ");
          if (elt instanceof String)
            buf.append("\"" + elt + "\"");
          else
            buf.append(elt.toString());
        }
      }
      str = buf.toString();
    }
    else
      str = value.toString();
    return str;
  }

  /**
   * return the literal value.
   * @return the literal value.
   */
  public Object getValue()
  {
    return value;
  }

  // Generate a string representation without quotation marks
  public String toString () {
    if (value == null)
      return "null";
    if (value instanceof Vector) {
      StringBuffer buf = new StringBuffer();
      Enumeration enu = ((Vector) value).elements();
      if (enu.hasMoreElements()) {
        Object elt = enu.nextElement();
        buf.append(elt.toString());
        while (enu.hasMoreElements()) {
          buf.append(", ");
          elt = enu.nextElement();
          buf.append(elt.toString());
        }
      }
      return buf.toString();
    }
    else if (value instanceof Date)
      return dateFormatter.format(value);
    else
      return value.toString();
  }

  /**
   * Fetch the base type of the literal.  It is calculated from the fully-qualified class name
   * of the literal.  So if it is a java.lang.String, this method returns "String"
   * @return the literal's type.
   */
  public String getType()
  {
    String ret = null;
    if (value != null)
    {
      ret = value.getClass().getName();
      int idx = ret.lastIndexOf(".");
      if (idx > 0)
        ret = ret.substring(idx+1);
    }
    return ret;
  }
}
