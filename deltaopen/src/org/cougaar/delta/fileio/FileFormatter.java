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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Does most of the work with {@link FormattedFile}s and {@link FormattedRecord}s.
 * Formatted files to be read or written must have an associated description
 * file in the <I>org.cougaar.install.path</I> + <I>org.cougaar.delta.data.path</I> +
 * \des directory.  The name of this description file
 * should be the same as the file type, with an extension of ".des".  For example,
 * a file of type "foo" should be described in "foo.des".  Within this file, each
 * record should be listed by name (in square brackets []), zero, one or two optional
 * directives (".extends" or ".offset"), and a list of fields within that record.
 * Fields are defined by a field name, the starting and ending character position
 * at which they appear in the record, and the record type.
 * <p>
 * The <b>.extends</b> directive means that the definition of the record includes the
 * definition of the record whose name follows the .extend directive.  For example,
 * if the record named RECTYPE2 includes (and possibly adds to) the record named
 * RECTYPE1, then the following directive should appear in the definition of RECTYPE2:
 * <p>
 * [RECTYPE2]
 * .extends RECTYPE1
 * <p>
 * If RECTYPE2 adds additional fields to those contained in RECTYPE1, then the
 * definition of RECTYPE2 would go on to list those fields.  If the formats of
 * RECTYPE1 and RECTYPE2 are identical (except for the name), then the above
 * RECTYPE2 definition would be sufficient.
 * <p>
 * The <b>.offset</b> directive, when used, appears at the top of the description file,
 * before any record names.  It is used to indicate that the name of the record,
 * which must appear in the data in order for the record type of an input data
 * record to be recognized, does not appear as the first field, but rather is offset
 * by some number of characters.  For example, if a record being processed has a
 * record name of RECTYPE1 but the input data record appears as follows:
 * <p>
 * 001RECTYPE1459.9FOO99
 * <p>
 * then the offset directive would appear in the description file as follows:
 * <p>
 * .offset 3
 * [RECTYPE1]
 * (record description follows)
 * <p>
 * A field definition includes the field name, starting and ending position in the
 * record, and data type.  For example, a field named "foo" consisting of a three
 * character String in record positions 5 through 7 would be defined as follows:
 * <p>
 * foo 5-7 String
 * <p><p>
 * Putting this all together produces a sample description file named "foo.des":
 * <p><p>
 * .offset3<br>
 * [RECTYPE1]<br>
 * seq_num      1-3     Integer<br>
 * record_type  4-11    String<br>
 * impt_num     12-16   Double<br>
 * next_field   17-19   String<br>
 * more_nums    20-21   Integer<br>
 * <p>
 * [RECTYPE2]<br>
 * .extends RECTYPE1<br>
 * alt_field    17-19   String<br>
 * more_stuff   22-25   String<br>
 * <p>
 * [RECTYPE3]<br>
 * .extends RECTYPE2<br>
 * (assumes RECTYPE2's format and RECTYPE3's formats are identical)<br>
 * <p>
 * Both input files (to be read) and output files (to be written) require a description
 * file.  Reading and writing of fields is done by {@link FormattedRecord}.
 */
public class FileFormatter {

  private static Vector fileDefinitions = new Vector();

  private static String dataDirectory = null;

  /** 
   * Returns the data directory from which files will be read and written.
   * Note: this requires the existence of system properties <I>org.cougaar.install.path</I>
   * and <I>org.cougaar.delta.data.path</I>.
   * @return the data directory
   */
  public static String getDataDirectory() {
    if (dataDirectory == null)
      dataDirectory = System.getProperty("org.cougaar.install.path") + File.separator + 
        System.getProperty("org.cougaar.delta.data.path", "delta" + File.separator + "data");
    return dataDirectory;
  }

  /**
   * Overrides the default data directory with the specified value
   * @param newDataDirectory the new data directory that will override the old value
   */
  public static void setDataDirectory(String newDataDirectory) {
    dataDirectory = newDataDirectory;
  }


  /**
   * Checks for the existence of the file "filename.txt" in the specified directory
   * and loads all occurrences as String file names into a Vector, then returns the
   * Vector.  Note: this requires the existence of system properties <I>org.cougaar.install.path</I>
   * and <I>org.cougaar.delta.data.path</I>.
   */
  public static Vector newInputFileList(String fileType, String filename) {
    String dirName = getDataDirectory() + File.separator;
    File datadir = new File(dirName);
    final String testname = filename;
    Vector files = new Vector();
    String[] fileList = datadir.list(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          if (name.startsWith(testname) && name.endsWith(".txt"))
            return true;
          return false;
        }});

    if (fileList != null && fileList.length > 0) {
      for (int i=0; i<fileList.length; i++) {
        String file = dirName + fileList[i];
        files.addElement(file);
      }
    }
    return files;
  }

  /**
   * Gets a list of files in the data directory having filenames starting with
   * filePrefix and ending with ".txt".  Note that the data directory is defined
   * as the concatenation of the values of the system properties <I>org.cougaar.install.path</I>
   * and <I>org.cougaar.delta.data.path</I>).
   * @param fileType the type of file (user defined). Though required, this parameter
   * is not actually used in this method, so its value could be anything, including null.
   * @param filePrefix a String with which the names of files of interest begin
   * @return a Vector of Strings representing the filenames of files whose names
   * begin with filePrefix and end with ".txt" that were found in the data directory
   */
  public static Vector getFileTypeList(String fileType, String filePrefix) {
    String dirName = getDataDirectory() + File.separator;
    File datadir = new File(dirName);
    final String testname = filePrefix;
    Vector files = new Vector();

    // Make sure we have a prefix
    if (testname != null && !testname.equals(""))
      {
        String[] fileList = datadir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
              if (name.startsWith(testname) && name.endsWith(".txt"))
                return true;
              return false;
            }});

        for (int i=0; i<fileList.length; i++) {
          try {
            File file = new File(dirName + fileList[i]);
            files.addElement(file);
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    return files;
  }

  /**
   * Returns the next sequential input file available. This method assumes that filenames
   * contain sequence numbers; i.e., <i>filename_XXX.txt</i>, where <i>XXX</i> is
   * a three digit integer. The sequence, then, is tied to this sequence number.
   * When the user asks for an input file, this mechanism seeks the lowest-valued
   * filename for data input.
   * @param fileType a user-defined file type
   * @param filename a String with which the names of files of interest begin
   * @return a FormattedFile or null if no files are available
   */
  public static FormattedFile newInputFile(String fileType, String filename) {
    boolean loaded = false;  // Assume this particular fileType has not been loaded
    FormattedFile fgif = null;
    String dirName = getDataDirectory() + File.separator;
    File datadir = new File(dirName);
    final String testname = filename;
    String[] fileList = datadir.list(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          if (name.startsWith(testname) && name.endsWith(".txt"))
            return true;
          return false;
        }});

    if (fileList == null)
      return null;
    switch (fileList.length) {
    case 0:
      return null;
    case 1:
      filename = fileList[0];
      break;
    default:
      filename = fileList[0];
      for (int i = 1; i < fileList.length; i++) {
        if (filename.compareTo(fileList[i]) > 1)
          filename = fileList[i];
      }
      break;
    }
    try {
      fgif = loadFileType(fileType, datadir+File.separator+filename);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return fgif;
  }


  /**
   * Opens a new file called filename and checks to see if the fileType
   * has been loaded into the fileDefinitions yet.  If it hasn't, it
   * calls loadFileType to load it.
   * The second argument of this method should be the "base" of the filename
   * e.g. "foo" rather than "foo.txt", as filename incrementalization has
   * been implemented, automatically adding a number + the extension to the
   * end of the filename.
   */
  public static FormattedFile newOutputFile(String fileType, String filename) {

    boolean loaded = false;
    FormattedFile fgif = null;
    // Need a number to add to filename for incremental filenames.
    // First, check to see if we've previously serialized the number in a previous
    // run; if so, use it; else set to 0 (assume first run).
    // N.B. Only allow for four digit integers in file name, i.e. foo_XXXX.txt.  Therefore,
    // we have an odometer effect when we get to 9999 -> 0000.
    int filenum = 0;
    File intFile = new File(getDataDirectory() + File.separator + filename + "intfile.out");
    if (intFile.exists()) {
      try {
        FileInputStream istream = new FileInputStream(intFile);
        ObjectInputStream oistream = new ObjectInputStream(istream);
        filenum = oistream.readInt();
        istream.close();
      } catch(IOException ioe) {
        ioe.printStackTrace();
      }
    }
    // Now, add this number to the filename.
    String newFilename = getDataDirectory() + File.separator + filename + "_" + createPadding(filenum) + ".txt";
    try {
      fgif = loadFileType(fileType, newFilename);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (fgif != null) {
      try {
        File f = new File(newFilename);
        RandomAccessFile raf = new RandomAccessFile(newFilename, "rw");
        fgif.setFile(raf);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // Bump up the file number if we've opened a file successfully.
    if (fgif != null) {
      if (filenum++ > 9999)
        filenum = 0;
      try {
        FileOutputStream ostream = new FileOutputStream(intFile);
        ObjectOutputStream oostream = new ObjectOutputStream(ostream);
        oostream.writeInt(filenum);
        oostream.flush();
        ostream.close();
      } catch(IOException ioe) {
        ioe.printStackTrace();
      }
    }
    return fgif;
  }

  /**
   * Gets the definition (record format) for a file type.  File definitions are
   * stored in the (data path)\des directory.  The data path is defined
   * as the concatenation of the values of the system properties <I>org.cougaar.install.path</I>
   * and <I>org.cougaar.delta.data.path</I>).
   * @param fileType the type of file for which the definition is desired
   * @return the definition of the file type passed as an argument to this method.
   * @throws FileNotFoundException if the .des file does not exist
   */
  public static FileDefinition getFileDefinition(String fileType) throws Exception {
    String newFileType = getDataDirectory() + File.separator + "des" + File.separator + fileType;

    int size = fileDefinitions.size();
    for (int i = 0; i < size; i++) {
      /*
       * Iterate through fileDefinitions already loaded, checking to see if this one
       * needs to be loaded
       */
      FileDefinition fd = (FileDefinition) fileDefinitions.elementAt(i);
      if (fd.getFileName().equals(fileType)) {
        return fd;
      }
    }

    RecordDefinition rd = null;

    File file = new File(newFileType + ".des");
    try {
      BufferedReader in = new BufferedReader(new FileReader(file));
      try {
        FileDefinition fd = new FileDefinition(fileType);
        while (in.ready()) {
          String newString = in.readLine();
          if (newString.equals(""))
            continue;
          if (newString.charAt(0) == '[') {
            rd = new RecordDefinition(newString.substring(1, newString.indexOf("]")));
            fd.addRecordDefinition(rd);
            continue;
          }
          if (newString.charAt(0) ==  ' ') {
            continue;
          }
          if (newString.charAt(0) == '.') { // A directive
            StringTokenizer st = new StringTokenizer(newString);
            String directive = st.nextToken().substring(1);
            String value = st.nextToken();
            if (directive.equals("extends")) {
              // Set the super-RecordDefinition
              RecordDefinition tmp_rd = fd.getRecordDefinition(value);
              rd.setExtends(tmp_rd);
            }
            else if (directive.equals("offset")) {
              fd.setRecordNameOffset(Integer.parseInt(value));
            }
          } else {
            StringTokenizer st = new StringTokenizer(newString);
            FieldDefinition field = new FieldDefinition();

            field.setFieldName(st.nextToken());
            String positions = st.nextToken();
            int startpos = Integer.parseInt(positions.substring(positions.indexOf("(") + 1, positions.indexOf("-")));
            int endpos = Integer.parseInt(positions.substring(positions.indexOf("-") + 1, positions.indexOf(")")));
            field.setStartPosition(startpos);
            field.setEndPosition(endpos);
            field.setDataType(st.nextToken());

            if (st.hasMoreTokens()) {
              field.setJustification(st.nextToken());
              field.setPaddingChar(st.nextToken());
            }
            rd.addFieldDefinition(field);
          }
        }
        fileDefinitions.addElement(fd);
        return fd;
      }
      catch (IOException ioe) {
        ioe.printStackTrace();
      }
      finally {
        in.close();
      }
    }
    catch (FileNotFoundException fnfe) {
      throw new FileNotFoundException("!!!! File does NOT exist: " + file);
    }
    return null;
  }

  /**
   * Creates a {@link FormattedFile} of the type and with the filename specified in the calling
   * argument.  The {@link FormattedFile} contains the {@link FileDefinition} (i.e., record format).
   * @param fileType the type of file to be created.  Note that the file type specified
   * for this parameter determines the {@link FileDefinition} to be attached to the {@link FormattedFile}.
   * @param filename the name of the {@link FormattedFile} to be returned
   * @return a {@link FormattedFile} containing the filename specified in the calling argument
   * and the {@link FileDefinition} associated with the file type specified in the calling
   * argument.
   * @throws Exception if there is no file definition for the specified fileType
   */
  public static FormattedFile loadFileType(String fileType, String filename) throws Exception {
    FormattedFile fgif = new FormattedFile();
    FileDefinition fd = getFileDefinition(fileType);
    if (fd == null) {
      throw new Exception("Unknown file type: " + fileType);
    }
    fgif.setFileDefinition(fd);
    fgif.setFileName(filename);
    return fgif;
  }

  private static String createPadding(int num) {
    String pad = "";
    if (num < 10)
      pad = "000";
    else if (num < 100)
      pad = "00";
    else if (num < 1000)
      pad = "0";
    return pad + num;
  }

}
