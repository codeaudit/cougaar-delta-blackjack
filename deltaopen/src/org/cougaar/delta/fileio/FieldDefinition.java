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

package org.cougaar.delta.fileio;

import java.io.Serializable;

/**
 * Defines a field in terms of its name, starting and ending position in the record,
 * and data type.  Methods also allow the programmer to specify how data that is shorter
 * than the field length should be treated in terms of justification and pad characters.
 */
public class FieldDefinition implements Serializable
{

  private String fieldName;
  private int startPosition;
  private int endPosition;
  private String dataType;
  private String justification;
  private String paddingChar;



  public FieldDefinition() {
    fieldName = null;
    dataType = null;
    justification = null;
    paddingChar = null;
  }


  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public void setStartPosition(int startPosition) {
    this.startPosition = startPosition;
  }

  public void setEndPosition(int endPosition) {
    this.endPosition = endPosition;
  }

  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  public void setJustification(String justification) {
    this.justification = justification;
  }

  public void setPaddingChar(String paddingChar) {
    this.paddingChar = paddingChar;
  }


  public String getFieldName() { return fieldName; }

  public int getStartPosition() { return startPosition; }

  public int getEndPosition() { return endPosition; }

  public String getDataType() { return dataType; }

  public String getJustification() { return justification; }

  public String getPaddingChar() { return paddingChar; }

}
