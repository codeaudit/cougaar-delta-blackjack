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

package org.cougaar.delta.util.html;

import java.util.*;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;


/**
 * This class reads a template file and replaces tags found between the delimiters with
 * the corresponding elements from its hashtable
 */
public class TemplateProcessor extends Hashtable {

  private String template; // an in-memory copy of the template file
  private String newline;  // system-dependent EOL sequence

  /**
   * Create a new Template Processor using the named file as the template
   * @param fileName The pathname of the template file
   * @throws FileNotFoundException The template file does not exist
   * @throws EOFException The template file could not be read
   * @throws IOException The template file could not be read
   */
  public TemplateProcessor(String fileName)
      throws FileNotFoundException, EOFException, IOException
  {
    readTemplateFile(new File(fileName));
  }

  /**
   * Create a new Template Processor using the named file as the template
   * @param file A file object representing the template file
   * @throws FileNotFoundException The template file does not exist
   * @throws EOFException The template file could not be read
   * @throws IOException The template file could not be read
   */
  public TemplateProcessor(File file)
      throws FileNotFoundException, EOFException, IOException
  {
    readTemplateFile(file);
  }

  /**
   *  Create a new TemplateProcessor using the given String as either the
   *  template itself or as the name of a file containing the template,
   *  depending on the boolean literalTemplate.  If true, then the String is
   *  taken as a literal value; if false, it is presumed to be a file.
   *  @param templateString the string representing the template
   *  @param literalTemplate a boolean specifying whether the string is a file
   *      name or the template text
   *  @throws FileNotFoundException The template file does not exist
   *  @throws EOFException The template file could not be read
   *  @throws IOException The template file could not be read
   */
  public TemplateProcessor (String templateString, boolean literalTemplate)
      throws FileNotFoundException, EOFException, IOException
  {
    if (literalTemplate) {
      template = templateString;
      newline = System.getProperty("line.separator");
    }
    else {
      readTemplateFile(new File(templateString));
    }
  }

  /**
   *  Initialize this TemplateProcessor with the template found in the given
   *  File object.
   *  @param file A file object representing the template file
   *  @throws FileNotFoundException The template file does not exist
   *  @throws EOFException The template file could not be read
   *  @throws IOException The template file could not be read
   */
  protected void readTemplateFile (File file)
      throws FileNotFoundException, EOFException, IOException
  {
    if (!file.exists())
      throw new FileNotFoundException();
    if (!file.canRead() || !file.isFile())
      throw new EOFException();

    FileReader fr = new FileReader(file);
    long len = file.length();
    char str[] = new char[(int)len];
    fr.read(str);
    fr.close();
    template = new String(str);
    newline = System.getProperty("line.separator");
  }

  /**
   * Returns true if the associated file actually contains template delimiters, false otherwise.
   * @return true if file is template file, false otherwise
   */
  public boolean isTemplateFile() throws IOException {
    BufferedReader in = new BufferedReader(new StringReader(template));
    if (in == null)
      return false;
    String line = null;
    while ((line = in.readLine()) != null)
    {
      if (line.indexOf(delimiter) != -1)
        return true;
    }

    return false;
  }

  /**
   * Run the template processor using the current hashtable state and template file
   * @return The contents of the template file with the substitutions made
   */
  public StringBuffer process()
  {
    StringBuffer ret = new StringBuffer(template.length());
    String line;
    String tag;
    int tokenstart, tokenend;
    String token = delimiter;
    BufferedReader in = new BufferedReader(new StringReader(template));
    if (in == null)
      return ret;
    try {
      while ((line = in.readLine()) != null)
      {
        tokenend = 0;
        while ((tokenstart = line.indexOf(token, tokenend)) >= 0)
        {
          ret.append(line.substring(tokenend, tokenstart));
          //
          // replace the tag if it exists
          //
          // find end of token
          tokenend = line.indexOf(token, tokenstart + token.length());
          if (tokenend < 0) // not found, output the rest of the line
          {
            ret.append(line.substring(tokenstart));
            tokenend = line.length();
            break;
          }
          tag = line.substring(tokenstart + token.length(), tokenend);
          tokenend += token.length();
          ret.append(substitute(tag));
        }
        // append the rest of the line
        ret.append(line.substring(tokenend));
        ret.append(newline);
      }
    }
    catch (Exception e)
    {
      ret.append("Exception In Template Parsing" + e);
    }
    return ret;
  }

  /**
   *  Substitute a suitable String for the tag found in the HTML template file.
   *  This method can be overridden in subclasses to provide more specialized
   *  behavior.
   *  @param tag the tag found in the HTML template
   *  @return the String to substitute for tag in the output
   */
  protected String substitute (String tag) {
    String value = (String) get(tag);
    if (value == null) // replace with nothing if not found
    {
      if (reportLostTags)
        value = "NO MATCH FOUND FOR TAG : " + tag;
      else
        value = "";
    }
    return value;
  }

  /**
   * A debugging stub
   */
  public static void main(String[] args) {
    TemplateProcessor templateProcessor = null;
    try {
      templateProcessor = new TemplateProcessor(args[0]);
    } catch (Exception e)
    {
      System.out.println("Error with file: "+args[0] + e);
    }
    templateProcessor.put("taglet", "blabbo");
    templateProcessor.put("taglet1", "blabbo2");
    templateProcessor.setReportLostTags(true);
    String val = templateProcessor.process().toString();
    System.out.println("Output: " + val);
    System.out.println("Done");

  }

  /**
   * Configures the processor to report a message for tags in the
   * template file that do not exist in the hashtable.  Defaults to
   * false.  If false, unknown tags are replaced by the empty string.
   * @param newReportLostTags The new value for the property
   */
  public void setReportLostTags(boolean newReportLostTags) {
    reportLostTags = newReportLostTags;
  }

  /**
   * @return The state of the reportLostTags property
   */
  public boolean isReportLostTags() {
    return reportLostTags;
  }

  /**
   * Use this property to change the template tag delimiter from the
   * default "##" to any other string.
   * @param newDelimiter The string marking template tags
   */
  public void setDelimiter(String newDelimiter) {
    delimiter = newDelimiter;
  }

  /**
   * @return The delimiter currently in use
   */
  public String getDelimiter() {
    return delimiter;
  }
  private boolean reportLostTags = false;
  private String delimiter = "##";

  /**
   * Override this method to allow null strings
   * @param key the hashtable key
   * @param value the value
   * @return the previous value of the specified key in this hashtable, or null if it did not have one.
   */
  public synchronized Object put(Object key, Object value)
  {
    if (key == null)
      return null;
    if (value == null)
    {
      if(reportLostTags)
        value = "[NULL]";
      else
        value = "";
    }
    if ((key instanceof String) && (value instanceof String))
      return super.put( key,  value);
    else
      return null;
  }

  /**
   * Debug function to dump all of the key-value pairs
   * @param out the stream to write to
   */
  public synchronized void dumpValues(Writer out)
  {
    try {
      for (Enumeration e = keys(); e.hasMoreElements(); )
      {
        String key = (String)e.nextElement();
        String value = (String)get(key);
        out.write("("+key+":"+value+")"+newline);
      }
    } catch (Exception e)
    {
      System.out.println("Error dumping values "+e);
      e.printStackTrace();
    }
  }
}
