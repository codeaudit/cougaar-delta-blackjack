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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Defines a formatted file.  Most of the work with formatted file objects is done
 * by {@link FileFormatter}.
 */
public class FormattedFile
{

  private FileDefinition fileDefinition;
  private String filename;
  private RandomAccessFile file;
  private BufferedReader in;
  private char[] recordBuf;
  String fs = File.separator;
  final String removeString = "."; //make this null if value is to be configurable
  String dataDir = FileFormatter.getDataDirectory();
  private int offset;
  /* Note: the following commented-out variables are used when the value of the
     removeString is to be configurable, with the value set in a .ini file under
     a section heading of [FileIO]: */
  //String SEC_NAME = "FileIO";
  //String propfilename = null;
  //final String removeString = null;

  public FormattedFile() {
    fileDefinition = null;
    filename = null;
//    // First, open and read properties file...
//    propfilename = ParameterFileReader.getFGIPlugInPropertyFileName();
//    File props_file = new File(propfilename);
//    if (props_file.exists()) {
//      ParameterFileReader pfr = ParameterFileReader.getInstance(propfilename);
//      removeString = pfr.getParameter(SEC_NAME, "fileio.remove.String", ".");
//    } else {
//      System.err.println("Properties file " + propfilename + " not found.");
//    }
  }



  public void setFileDefinition(FileDefinition fileDefinition) {
    this.fileDefinition = fileDefinition;
  }


  public FileDefinition getFileDefinition() {
    return fileDefinition;
  }


  public void setFileName(String filename) {
    this.filename = filename;
  }


  public String getFileName() {
    return filename;
  }


  public void setFile(RandomAccessFile file) {
    this.file = file;
  }


  public RandomAccessFile getFile() {
    return file;
  }

  public void copyTo(String dir) {
    try {
      int index = filename.lastIndexOf(fs);

      // Make directory if it does not exist
      File fdir = new File(dataDir + fs + dir + fs);
      fdir.mkdirs();

      // Now we can create the file
      String newFilename = dataDir + fs + dir + fs + filename.substring(index+1);
      BufferedWriter writer = new BufferedWriter(new FileWriter(newFilename));
      if (in == null)
        openFile();
      String line = in.readLine();
      while (line != null) {
        writer.write(line);
        writer.newLine();
        line = in.readLine();
      }
      writer.close();
      closeFile();
    }
    catch (Exception e) {
      System.out.println("FormattedFile : Unable to copy file");
      e.printStackTrace();
    }
  }

  public Vector getRecords(Enumeration records) {
    Vector records_vect = new Vector();
    while (records.hasMoreElements()) {
      Vector tmp_records = getRecords((String)records.nextElement());
      int size = tmp_records.size();
      for (int i = 0; i < size; i++)
        records_vect.addElement(tmp_records.elementAt(i));
    }
    return records_vect;
  }

  /**
   * Gets from the data file all the records having a record name matching the name
   * specified in the input parameter.
   * @param record The record name of all the records desired from the data file
   * @return A Vector of FormattedRecords whose record names match the input specification
   */
  public Vector getRecords(String record) {
    Vector records = new Vector();
    Enumeration recordDefs = fileDefinition.getRecordDefinitions();
    offset = fileDefinition.getRecordNameOffset();
    RecordDefinition rd = null;
    while (recordDefs.hasMoreElements()) {
      RecordDefinition tmp_rd = (RecordDefinition)recordDefs.nextElement();
      if (tmp_rd.getRecordName().equals(record)) {
        rd = tmp_rd;
        break;
      }
    }

    if (rd != null) {
      File file = new File(filename);
      if (file.exists()) {
        try {
          BufferedReader in = new BufferedReader(new FileReader(file));

          while (in.ready()) {
            String newString = in.readLine();
            if (newString.substring(0 + offset).startsWith(record)) {
              FormattedRecord fr = new FormattedRecord(rd, newString);
              records.addElement(fr);
            }
          }
          in.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }

    return records;
  }


  /**
   * Used in conjunction with getRecord(); Must be called before getRecord()
   * to open file, etc.; N.B. must call closeFile() when finished with getRecord().
   * See getRecord() for details.
   */
  public BufferedReader openFile() {
    in = null;
    File file = new File(filename);
    if (file.exists()) {
      try {
        in = new BufferedReader(new FileReader(file));
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
    return in;
  }


  /**
   * Used in conjunction with getRecord(); Must be called after getRecord() usage
   * to close file, etc.;
   * See getRecord() for details.
   */
  public void closeFile() {
    if (in == null)
      return;
    try {
      in.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    in = null;
  }


  /**
   * Returns the next FormattedRecord (of any type); good for iterating through
   * different FormattedRecord types in the same file.
   * N.B. must call openFile() before using getRecord() and must call closeFile()
   * after using getRecord().
   *
   * Example:
   * <PRE>
   *    FormattedFile f = FileFormatter.newInputFile("MILSTRIP", "samplea0.txt");;
   *    f.openFile();
   *    FormattedRecord fgir = null;
   *    while ((fgir = f.getRecord()) != null) {
   *       if (fgir.isRecordType("A0") == FormattedRecord.IS_RECORD_TYPE)
   *          System.out.println("Found an A0 record");
   *       else if (fgir.isRecordType("A0") == FormattedRecord.NO_RECORD_AVAIL)
   *          System.out.println("Unknown record type");
   *    }
   *    f.closeFile();
   * </PRE>
   */
  public FormattedRecord getRecord() {
    FormattedRecord fr = null;
    if (recordBuf == null)
      recordBuf = new char[fileDefinition.getMaxRecordLength()];
    int init = fileDefinition.getMinRecordLength();
    int start = 0;
    try {
      int numread = in.read(recordBuf, start, init);
      if (numread == -1)
        return null;
      else if (numread != init)
      {
        System.err.println("FormattedFile::getRecord:ERROR:  less than minimum record length (" + init + " characters)!");
        return null;
      }
      fr = new FormattedRecord(new String(recordBuf, 0, init));
      fr.setRecordDefinition(getRecordDef(fr));

      // If this record is longer than the minimum for this file,
      // make sure we have the whole record
      int more = fr.getRecordDefinition().getLength() - init;
      if (more > 0)
      {
        if (in.read(recordBuf, init, more) != more)
        {
          System.err.println("FormattedFile::getRecord:ERROR:  less than required record length (" + (init+more) + " characters)!");
          return null;
        }
        fr.setRecord(new String(recordBuf, 0, init + more));
      }

      // Ignore whitespace between records
      in.mark(2);
      char ch = (char) in.read();
      while (Character.isWhitespace(ch))
      {
        in.mark(2);
        ch = (char) in.read();
      }
      in.reset();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    return fr;
  }


  /**
   * Returns an FormattedRecord with a user provided string.
   * Assumes use of an established FormattedFile.
   */
  public FormattedRecord getRecord(String s) {
    FormattedRecord fr = null;
    fr = new FormattedRecord(s);
    fr.setRecordDefinition(getRecordDef(fr));
    return fr;
  }


  /**
   * Read a line of text out of the file and treat it as a record.
   */
  // (Used in cases such as the MilstripFileReaderPlugIn, where we need to
  //  know where the line breaks are so that we can reject a requisition for
  //  having more than or less than the required record size.)
  public FormattedRecord getRecordLine() {
    FormattedRecord fr = null;

    try {
      String wholeLine = in.readLine();
      if (wholeLine != null) {
        fr = new FormattedRecord(wholeLine);
        fr.setRecordDefinition(getRecordDef(fr));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return fr;
  }


  /**
   * Return the record definition for a record
   */
  RecordDefinition getRecordDef(FormattedRecord fr) {
    Enumeration recordDefs = fileDefinition.getRecordDefinitions();
    offset = fileDefinition.getRecordNameOffset();
    RecordDefinition rd = null;
    String raw_record = fr.getRecord();
    while (recordDefs.hasMoreElements()) {
      RecordDefinition tmp_rd = (RecordDefinition)recordDefs.nextElement();
      String rname = tmp_rd.getRecordName();
      if (rd == null || (raw_record.length() >= rname.length() &&
        rname.equalsIgnoreCase(raw_record.substring(0 + offset, rname.length() + offset))))
            rd = tmp_rd;
    }
    return rd;
  }


  /**
   * Creates an FormattedRecord with FieldDefinitions for the recType
   */
  public FormattedRecord newRecord(String recName, int recLength) {
    FormattedRecord fgir = new FormattedRecord(recLength);
    FileDefinition fileDef = getFileDefinition();
    Enumeration enumOfRecs = fileDef.getRecordDefinitions();
    // Loop through the RecordDefinitions until you find the
    // one which matches the recName parameter
    while (enumOfRecs.hasMoreElements()) {
      RecordDefinition rd = (RecordDefinition) enumOfRecs.nextElement();
      if (rd.getRecordName().equals(recName)) {
    fgir.setRecordDefinition(rd);
      }
    }
    return fgir;
  }


  /**
   * Closes the file, use with newOutputFile, and writeRecord
   */
  public void finish() {
    try {
      file.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * "Removes" the file; This can mean moving the file to a temporary
   * directory (for debugging purposes), simply renaming the file, or outright
   * deletion.  Currently configured to rename file "foo.txt" to ".foo.txt"
   */
  public boolean remove(String file_name) {
    //
    try {
      boolean b;
      File file = new File(file_name);
      String newName = file.getParent() + fs + removeString + file.getName();
      File newFile = new File (newName);
      //delete any previously existing file by the same name:
      b = newFile.delete();
      b = file.renameTo(newFile);
      return b;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Same as method remove(String filename), but for use with FormattedFile objects.
   */
  public boolean remove() {
    closeFile();
    boolean b = remove(filename);
    return b;
  }

}
