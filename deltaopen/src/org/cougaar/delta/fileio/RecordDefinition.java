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
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Defines formatted records in terms of their name, length, and the fields they
 * contain.  Record names should be placed in the associated file's description
 * file (i.e., ".des file").
 *
 * @see FileFormatter
 */
public class RecordDefinition implements Serializable
{
  private Hashtable fieldDefinitions = new Hashtable();
  private String recordName;
  private RecordDefinition extends_record;
  private int length;

  public RecordDefinition() {
    recordName = null;
  }

  public RecordDefinition(String recordName) {
    this.recordName = recordName;
  }

  public RecordDefinition(Hashtable fieldDefinitions) {
    this.fieldDefinitions = fieldDefinitions;
    recordName = null;
    Enumeration e = fieldDefinitions.elements();
    while (e.hasMoreElements())
      {
        int end = ((FieldDefinition)e.nextElement()).getEndPosition();
        if (end > length)
          length = end;
      }
  }


  public void setExtends(RecordDefinition name) {
    extends_record = name;
    if (name.getLength() > length)
      length = name.getLength();
  }

  public RecordDefinition getExtends() { return extends_record; }

  public void setRecordName(String recordName) {
    this.recordName = recordName;
  }

  public String getRecordName() { return recordName; }


  public void addFieldDefinition(FieldDefinition fd) {
    fieldDefinitions.put(fd.getFieldName(), fd);
    if (fd.getEndPosition() > length)
      length = fd.getEndPosition();
  }

  public Hashtable getFieldDefinitions() {
    return fieldDefinitions;
  }

  public FieldDefinition getFieldDefinition(String fieldName) {
    return (FieldDefinition)fieldDefinitions.get(fieldName);
  }

  public int getLength()
  {
    return length;
  }
}
