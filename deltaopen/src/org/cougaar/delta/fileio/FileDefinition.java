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

import java.util.Enumeration;
import java.util.Vector;

/**
 * A class representing the definition of a file (i.e., its format), that is primarily
 * made up of a collection of {@link RecordDefinition}s representing the record
 * types found in this file type.
 */
public class FileDefinition
{

  private Vector recordDefinitions = new Vector();
  private String fileName;
  private int maxRecordLength;
  private int minRecordLength = Integer.MAX_VALUE;
  private int recordNameOffset = 0; //indicates how many positions into the record
                                    //to look to find the start of the recordName

  public FileDefinition() {
    fileName = null;
  }

  /**
   * Initializes the name of the files defined by this FileDefinition.
   */
  public FileDefinition(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Attaches a Vector of {@link RecordDefinition}s passed in as the input argument.
   * @param a Vector containing {@link RecordDefinition}s for the records in the
   * file being defined.
   */
  public FileDefinition(Vector recordDefinitions) {
    this.recordDefinitions = recordDefinitions;
    fileName = null;
        Enumeration e = recordDefinitions.elements();
        while (e.hasMoreElements())
        {
            int len = ((RecordDefinition)e.nextElement()).getLength();
            if (len > maxRecordLength)
                maxRecordLength = len;
            if (len < minRecordLength)
                minRecordLength = len;
        }
  }

  /**
   * Gets the {@link RecordDefinition}s associated with this FileDefinition.
   * @return the {@link RecordDefinition}s as an Enumeration
   */
  public Enumeration getRecordDefinitions() {
    return recordDefinitions.elements();
  }

  /**
   * Gets the definition for the specified record name.  The "name" of a record
   * is specified in the .des file in square brackets, and is usually a string that
   * appears at or near the beginning of the record.
   * @param rdname the name of the record for which a definition is desired
   * @return the {@link RecordDefinition} for the named record if it exists, null
   * otherwise
   */
  public RecordDefinition getRecordDefinition(String rdname) {
      int size = recordDefinitions.size();
      for (int i = 0; i < size; i++) {
          if (((RecordDefinition)recordDefinitions.elementAt(i)).getRecordName().equals(rdname))
          return (RecordDefinition)recordDefinitions.elementAt(i);
      }
      return null;
  }

  /**
   * Adds a {@link RecordDefinition} to the Vector of {@link RecordDefinition}s
   * associated with this FileDefinition.
   * @param rd the {@link RecordDefinition} to add
   */
  public void addRecordDefinition(RecordDefinition rd) {
    recordDefinitions.addElement(rd);
      int len = rd.getLength();
      if (len > maxRecordLength)
          maxRecordLength = len;
      if (len < minRecordLength)
          minRecordLength = len;
  }

  public String getFileName() { return fileName; }

  public void setFileName(String fileName) { this.fileName = fileName; }

  /**
   * Get the length of the longest record of this file type (measured in characters).
   */
  public int getMaxRecordLength()
  {
      int retval = 0;
      Enumeration e = recordDefinitions.elements();
      while (e.hasMoreElements())
      {
          int len = ((RecordDefinition)e.nextElement()).getLength();
          if (len > retval)
              retval = len;
      }
      return retval;
  }

  /**
   * Get the length of the shortest record of this file type (measured in characters).
   */
  public int getMinRecordLength()
  {
    int retval = Integer.MAX_VALUE;
    Enumeration e = recordDefinitions.elements();
    if (!e.hasMoreElements())
      return 0;
    while (e.hasMoreElements())
    {
        int len = ((RecordDefinition)e.nextElement()).getLength();
        if (len < retval)
            retval = len;
    }
    return retval;
  }

  /**
   * Gets the record name offset.
   * @return the number of positions into the record to look to find the start of
   * the record name
   */
  public int getRecordNameOffset() {
    return recordNameOffset;
  }

  /**
   * Sets the record name offset.
   * @param offset the number of positions into the record to look to find the
   * start of the record name.
   */
  public void setRecordNameOffset(int offset){
    recordNameOffset = offset;
  }

}
