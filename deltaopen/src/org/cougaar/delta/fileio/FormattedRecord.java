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

import java.io.File;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Hashtable;

/**
 * Methods to read and write fields.
 */
public class FormattedRecord implements Serializable
{
  private RecordDefinition recordDefinition;
  private StringBuffer record = new StringBuffer();
  static private String lineSeparator = System.getProperty("line.separator");

  public int IS_RECORD_TYPE = 1;
  public int NOT_RECORD_TYPE = 0;
  public int NO_RECORD_AVAIL = -1;

  public FormattedRecord(int recLength) {
    record.setLength(recLength);
    recordDefinition = null;
  }

  public FormattedRecord(RecordDefinition rd, String record) {
    recordDefinition = rd;
    setRecord(record);
  }

  public FormattedRecord(String record) {
    setRecord(record);
  }

  public void setRecordDefinition(RecordDefinition rd) { recordDefinition = rd; }

  public void setRecord(String rec) {
    this.record = new StringBuffer(rec);
  }

  public String getRecord() {
    return record.toString();
  }

  public int isRecordType(String rt) {
    if (recordDefinition != null) {
      if (recordDefinition.getRecordName().equals(rt))
        return IS_RECORD_TYPE;
      else
        return NOT_RECORD_TYPE;
    } else
      return NO_RECORD_AVAIL;
  }

  public RecordDefinition getRecordDefinition() { return recordDefinition; }

  public Object readField(String field) {
    if (recordDefinition == null)
      return null;
    String record_str = record.toString();
    Object o = new Object();
    FieldDefinition fd = recordDefinition.getFieldDefinition(field);

    if (fd != null) {
      int start = fd.getStartPosition() - 1;
      int end = fd.getEndPosition();

      if (fd.getDataType().equals("Integer")) {
        try {
          o = Integer.valueOf(record_str.substring(start, end));
        } catch (NumberFormatException nfe) {
          return null;
        }
        return (Integer)o;
      }
      else if (fd.getDataType().equals("String")) {
        try {
          o = record_str.substring(start, end);
        } catch (StringIndexOutOfBoundsException sie) {
          System.err.println("\nFormattedRecord: ERROR - incorrect record length "+ record_str.length() + " for record: " + record_str);
          return null;
        }
        return (String)o;
      }
      else if (fd.getDataType().equals("Double")) {
        try {
          o = Double.valueOf(record_str.substring(start, end));
        } catch (NumberFormatException nfe) {
          return null;
        }
        return (Double)o;
      }
      else
        return null;
    } else {
      RecordDefinition extends_record = recordDefinition.getExtends();
      if (extends_record != null) {
        FieldDefinition super_fd = extends_record.getFieldDefinition(field);
        int super_start = super_fd.getStartPosition() - 1;
        int super_end = super_fd.getEndPosition();

        if (super_fd.getDataType().equals("Integer")) {
          try {
            o = Integer.valueOf(record_str.substring(super_start, super_end));
          } catch (NumberFormatException nfe) {
            return null;
          }
          return (Integer)o;
        }
        else if (super_fd.getDataType().equals("String")) {
          o = record_str.substring(super_start, super_end);
          return (String)o;
        }
        else if (fd.getDataType().equals("Double")) {
          try {
            o = Double.valueOf(record_str.substring(super_start, super_end));
          } catch (NumberFormatException nfe) {
            return null;
          }
          return (Double)o;
        }
        else
          return null;
      }
    }
    return null;
  }

  /**
   * Writes data to a field in a record.
   * @param field_name the name of the field to be written
   * @param value the String to be written into the field
   * @throws Exception if the field name is not found in the record definition
   */
  public void writeField(String field_name, String value) throws Exception {
    FieldDefinition fd = recordDefinition.getFieldDefinition(field_name);

    if (fd != null) {
      int start_pos = fd.getStartPosition();
      int field_length = fd.getEndPosition() - start_pos + 1;
      String field_type = fd.getDataType();

      if (value == null)
        value = "";
      if (value.length() > field_length) {
        // truncate if too long
        replaceString(start_pos - 1, value.substring(0, field_length));
      }
      else if (value.length() < field_length) {
        int diff = field_length - value.length();
        // pad if too short
        if (field_type.equals("String") || field_type.equals("date") || field_type.equals("time"))
          replaceString(start_pos - 1, value + createString(' ', diff));
        else
          replaceString(start_pos - 1, createString('0', diff) + value);
      }
      else {
        // if the lengths are the same,
        // just write it out "as is"
        replaceString(start_pos - 1, value);
      }
    }
    else {
      RecordDefinition extends_record = recordDefinition.getExtends();
      if (extends_record != null) {
        FieldDefinition super_fd = extends_record.getFieldDefinition(field_name);
        int super_start = super_fd.getStartPosition();
        int super_length = super_fd.getEndPosition() - super_start + 1;
        String field_type = super_fd.getDataType();

        if (value == null)
          value = "";
        if (value.length() > super_length) {
          // truncate if too long
          replaceString(super_start - 1, value.substring(0, super_length));
        }
        else if (value.length() < super_length) {
          int diff = super_length - value.length();
          // pad if too short
          if (field_type.equals("String") || field_type.equals("date") || field_type.equals("time"))
            replaceString(super_start - 1, value + createString(' ', diff));
          else
            replaceString(super_start - 1, createString('0', diff) + value);
        }
        else {
          // if the lengths are the same,
          // just write it out "as is"
          replaceString(super_start - 1, value);
        }
      }
      else {
        throw new Exception("Field: " + field_name + " not found in record definition");
      }
    }
  }


  public void replaceString(int pos, String value) {
    if (record.length() < pos + value.length())
      record.setLength(pos + value.length());

    for (int i = pos; i < pos + value.length(); i++) {
      record.setCharAt(i, value.charAt(i - pos));
    }
  }


  public String createString(char c, int length) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < length; i++) {
      sb = sb.append(c);
    }
    return sb.toString();
  }

  public String toString()
  {
    return record.toString();
  }

  public String finish(RandomAccessFile raf) {
    String s = record.toString();
    String new_str = s.replace('\u0000', ' ');
    try {
      raf.seek(raf.length());
      raf.writeBytes(new_str);
      raf.writeBytes(lineSeparator);
      return new_str;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

}
