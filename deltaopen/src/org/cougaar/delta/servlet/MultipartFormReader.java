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
package org.cougaar.delta.servlet;

import java.io.*;

/**
 *  A MultipartFormReader is a device for parsing a form submitted to a servlet
 *  with the parameter "enctype=multipart/form-data".  The data is broken down
 *  into sections, where each section corresponds to a form element.  The
 *  MultipartFormReader parses the input stream containing the multipart form
 *  and spawns separate input substreams for the sections it encounters.
 *  <br><br>
 *  <b>Warning:</b> The design of this class assumes that the various sections
 *  are to be read sequentially and completely within a single thread.
 *  Simultaneous and/or random-access usage is not supported.
 */
public class MultipartFormReader {
  // the underlying InputStream
  private InputStream input = null;

  // a BufferedReader for convenient parsing
  private BufferedReader bufr = null;

  // the String used by this multipart form to separate the sections
  private String separator = null;

  // the String used by this multipart form to mark the end of its last section
  private String terminator = null;

  // a flag indicating, if true, that there are more sections in the form
  private boolean moreSections = false;

  // an input stream for reading the current section
  private FormSectionStream currentSection = null;

  /**
   *  Create this multipart form parser and configure it to read the form data
   *  from the given InputStream.
   *  @param in the InputStream containing the form data
   *  @throws IOException if there is a problem reading the InputStream
   */
  public MultipartFormReader (InputStream in) throws IOException {
    input = in;
    bufr = new BufferedReader(new InputStreamReader(input));
    separator = bufr.readLine();
    if (separator != null) {
      terminator = separator + "--";
      moreSections = true;
    }
  }

  /**
   *  Report whether or not there are more sections in this multipart form.
   *  @return true if there are more sections to read
   */
  public boolean hasMoreSections () {
    return moreSections;
  }

  /**
   *  Create an InputStream as a substream of the underlying input stream.  The
   *  scope of the new InputStream is a single section found in the multipart
   *  form.
   *  @return an InputStream restricted to a section
   *  @throws IOException if there is a problem reading the form data
   */
  public InputStream getSectionStream () throws IOException {
    if (currentSection != null)
      return currentSection;
    if (hasMoreSections())
      return new FormSectionStream();
    return null;
  }

  // remove the reference to a stale section
  private void clearCurrentSection () {
    currentSection = null;
  }

  // set a flag indicating that there are no more sections
  private void noMoreSections () {
    moreSections = false;
  }

  // An instance inner class representing an InputStream that draws its input
  // from the parent MultipartFormReader's underlying InputStream and
  // terminates at the end of a form section.
  //
  // The input is examined a line at a time.  If the line is not the separator
  // or terminator, then it is cached, to be read one character at a time.
  //
  // Since BufferedReader::readLine() does not include the line terminator
  // sequence (whatever it may be on a given system), a '\n' character is
  // artificially included at the end of each line of input.
  private class FormSectionStream extends InputStream {
    private String bufferLine = null;
    private int bufferIndex = -1;
    private boolean sectionDone = false;

    public FormSectionStream () throws IOException {
      bufferLine = bufr.readLine();
      bufferIndex = 0;
      if (bufferLine == null)
        endOfFinalSection();
    }

    public int read () throws IOException {
      if (sectionDone)
        return -1;

      if (bufferIndex > bufferLine.length()) {
        bufferLine = bufr.readLine();
        bufferIndex = 0;
      }

      if (bufferLine == null || bufferLine.equals(terminator))
        endOfFinalSection();
      else if (bufferIndex == bufferLine.length()) {
        bufferIndex++;
        return (int) '\n';
      }
      else if (bufferLine.equals(separator))
        endOfSection();
      else
        return (int) bufferLine.charAt(bufferIndex++);

      return -1;
    }

    private void endOfSection () {
      sectionDone = true;
      clearCurrentSection();
    }

    private void endOfFinalSection () {
      endOfSection();
      noMoreSections();
    }
  }
}
