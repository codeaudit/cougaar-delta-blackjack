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

package org.cougaar.delta.util;

/**
 * Represents a code and its text explanation.
 * Codes are stored and accessed by their database table (eg. LTA or LTA_LINE_ITEM)
 * and their field name (eg. PRICE_COMPETITION_CODE).  The possible legal values are stored in
 * the property "codeValue" and the text explanation of that value is

 * @author ALPINE (alpine-software@bbn.com)
 * @version $Id: Code.java,v 1.1 2002-04-30 17:33:27 cerys Exp $
 */

public class Code extends DBObject {

  /**
   * Create an unintialized code object
   */
  public Code() {
  }

  /**
   * Create an intialized code object.
   * @param tableName The database table that contains the code.
   * @param codeName the name of the code field.
   * @param codeValue the possible value of the code.
   * @param codeString the text explanation of that code.
   */
  public Code(String tableName, String codeName, String codeValue, String codeString)
  {
    this.tableName = tableName;
    this.codeName = codeName;
    this.codeValue = codeValue;
    this.codeString = codeString;
  }


  private String tableName;
  private String codeName;
  private String codeValue;
  private String codeString;

  /**
   * Get the name of the table that this code is in.
   * @return the name of the table that this code is in.
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Set the name of the table that this code is in.
   * @param newTableName the name of the table that this code is in.
   */
  public void setTableName(String newTableName) {
    tableName = newTableName;
  }

  /**
   * Set the name of the code
   * @param newCodeName the name of the code
   */
  public void setCodeName(String newCodeName) {
    codeName = newCodeName;
  }

  /**
   * Get the name of the code
   * @return the name of the code
   */
  public String getCodeName() {
    return codeName;
  }

  /**
   * Set one of the legal values for this code.
   * @param newCodeValue one of the legal values for this code.
   */
  public void setCodeValue(String newCodeValue) {
    codeValue = newCodeValue;
  }

  /**
   * Get one of the legal values for this code.
   * @return one of the legal values for this code.
   */
  public String getCodeValue() {
    return codeValue;
  }

  /**
   * Set the descriptive text for this code value.
   * @param newCodeString the descriptive text for this code value.
   */
  public void setCodeString(String newCodeString) {
    codeString = newCodeString;
  }

  /**
   * Get the descriptive text for this code value.
   * @return the descriptive text for this code value.
   */
  public String getCodeString() {
    return codeString;
  }

  /**
   * Get a displayable string for this code value.
   * This is intended for uses like GUI menus.
   * DO NOT change this format without changing the GUI code to properly parse the results!
   * @return displayable string for this code value.
   */
  public String getDisplayableString() {
    return codeValue + " (" + codeString + ")";
  }

  /**
   * Return a string representation of this code value.
   * @return "tableName:codeName:codeValue:codeString"
   */
  public String toString()
  {
    return tableName + ":" +
      codeName + ":" +
      getDisplayableString();
  }
}
