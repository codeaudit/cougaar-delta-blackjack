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
import java.util.StringTokenizer;
import org.cougaar.delta.util.DBObject;

/**
 * Represents a Qualification Rule Accessor Operand
 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: QRuleAccessorOperand.java,v 1.1 2002-04-30 17:33:28 cerys Exp $
 */
public class QRuleAccessorOperand extends DBObject implements QRuleOperand {

  private boolean invokedStandalone = false;
  private String internalName;
  private String uiCategory;
  private String uiName;
  private String uiType;
  private String jessType;
  private String jessAccessor;

  /**
   * Create a new QRule Accessor Operand
   * @param newInternalName the unique name to be used for this accessor operand
   * @param newUICategory the top-level category to which this accessor belongs
   * @param newUIName the name of this accessor that is displayed to the user
   * @param newUIType the datatype of the return value of this accessor, used by the user interface
   * @param newJESSType the datatype of the return value of this accessor, used by the JESS rule engine
   * @param newJESSAccessor the chain of accessors used by JESS to obtain the value
   * @return a new QRuleAccessorOperand
   */
  public QRuleAccessorOperand(String newInternalName, String newUICategory,  String newUIName,
      String newUIType, String newJESSType, String newJESSAccessor) {
    internalName = newInternalName;
    uiCategory = newUICategory;
    uiName = newUIName;
    uiType = newUIType;
    jessType = newJESSType;
    jessAccessor = newJESSAccessor;
  }

  /**
   * Accessor operands are always non-Literal
   * @return false
   */
  public boolean isLiteral() {
    return false;
  }

  /**
   * Get the internal name for this accessor
   * @return the internal name
   */
  public String getInternalName() {
    return internalName;
  }

  /**
   * Set the internal name for this accessor
   * @param newInternalName the internal name
   */
  public void setInternalName(String newInternalName) {
    String oldInternalName = internalName;
    internalName = newInternalName;
    propertyChangeListeners.firePropertyChange("internalName", oldInternalName, newInternalName);
  }

  /**
   * Get the user interface category for this accessor. (LTA, ITEM, etc.)
   * @return the user interface category for this accessor.
   */
  public String getUiCategory() {
    return uiCategory;
  }

  /**
   * Set the user interface category for this accessor.
   * @param newUICategory the user interface category for this accessor.
   */
  public void setUiCategory(String newUICategory) {
    String oldUICategory = uiCategory;
    uiCategory = newUICategory;
    propertyChangeListeners.firePropertyChange("uiCategory", oldUICategory, newUICategory);
  }

  /**
   * Get the name presented to the user for this accessor.
   * @return the name presented to the user for this accessor.
   */
  public String getUiName() {
    return uiName;
  }

  /**
   * Set the name to be presented to the user for this accessor.
   * @param newUIName the name to be presented to the user for this accessor.
   */
  public void setUiName(String newUIName) {
    String oldUIName = uiName;
    uiName = newUIName;
    propertyChangeListeners.firePropertyChange("uiName", oldUIName, newUIName);
  }

  /**
   * Get the user interface type for this accessor.
   * @return the user interface type for this accessor.
   */
  public String getUiType() {
    return uiType;
  }

  /**
   * Get the JESS type for this accessor.
   * @return the JESS type for this accessor.
   */
  public String getJessType() {
    return jessType;
  }

  /**
   * Set the JESS type for this accessor.
   * @param newJessType the JESS type for this accessor.
   */
  public void setjessType(String newJessType) {
    String oldJessType = jessType;
    jessType = newJessType;
    propertyChangeListeners.firePropertyChange("jessType", oldJessType, newJessType);
  }

  /**
   * Get the generic notion of type for this accessor.
   * @return the JESS type.
   */
  public String getType()
  {
    return getJessType();
  }


  /**
   * Get the JESS accessor string for this accessor.
   * @return the JESS accessor string for this accessor.
   */
  public String getJessAccessor() {
    return jessAccessor;
  }

  /**
   * Set the JESS accessor string for this accessor.
   * @param newJessAccessor the JESS accessor string for this accessor.
   */
  public void setjessAccessor(String newJessAccessor) {
    String oldJessAccessor = jessAccessor;
    jessAccessor = newJessAccessor;
    propertyChangeListeners.firePropertyChange("jessAccessor", oldJessAccessor, newJessAccessor);
  }

  /**
   * Generates a portion of a JESS defrule form that corresponds to this QRuleOperand.
   * @return the JESS representation of this rule test operand
   */
  public String toJESS() {
    String a = getJessAccessor();
    StringBuffer sb = new StringBuffer("(getXa ");
    StringTokenizer st = new StringTokenizer(a,".");
    String firstAccessor = st.nextToken();
    if (isNonProposalObject(firstAccessor))
      sb.append(nonProposalObject(firstAccessor));
    else
      sb.append("?candidate \"" + firstAccessor + " ");
    while (st.hasMoreTokens()) {
      sb.append(st.nextToken());
      sb.append(" ");
    }
    sb.append ("\") ");
    return sb.toString();
  }

  // This is very simple for now
  private boolean isNonProposalObject(String accessor) {
    return accessor.equals("PLUGIN");
  }

  private String nonProposalObject(String accessor) {
    if (accessor.equals("PLUGIN"))
      return "(fetch PLUGIN) \"";
    else
      return null;
  }

  /**
   * Generate a psuedo-readable version of the rule test
   */
  public String ruleToString() {
    return getInternalName();
  }

  /**
   * Return the UI name as a string.
   * @return the UI name as a string.
   */
  public String toString () {
    return uiName;
  }

  public boolean equals (Object o) {
    if (o instanceof QRuleAccessorOperand) {
      QRuleAccessorOperand q = (QRuleAccessorOperand) o;
      return uiCategory.equals(q.uiCategory) &&
        internalName.equals(q.internalName);
    }
    return false;
  }
}
