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

import java.io.*;

/**
 *  The EncodingTemplate class is a specialization of TemplateProcessor that
 *  is capable of recognizing special tags and treating the corresponding
 *  values in special ways.  Currently, there are two types of special tags,
 *  distinguished by a key inserted in before of the tag's name.  Both are
 *  designed to encode special characters, thereby allowing arbitrary String
 *  values to be inserted into the template file without creating conflicts
 *  with the syntax of the surrounding HTML or JavaScript.  Note that the keys
 *  themselves are not part of the tag names.  The supported keys are:
 *  <ul>
 *    <li>"ENCODE:" to render the output neutral in an HTML context</li>
 *    <li>"JSENCODE:" to render the output neutral in a JavaScript context</li>
 *  </ul>
 */
public class EncodingTemplate extends TemplateProcessor {
  /**
   *  The String tag prefix recognized by this encoding template to indicate
   *  that the values inserted in place of the affected tag should first be
   *  encoded to render inert any embedded HTML special characters.
   */
  protected String encodeKey = "ENCODE:";

  /**
   *  The String tag prefix recognized by this encoding template to indicate
   *  that the values inserted in place of the affected tag should first be
   *  encoded to render inert any JavaScript special characters.
   */
  protected String jsEncodeKey = "JSENCODE:";

  /**
   *  Create this EncodingTemplate instance as a suitable TemplateProcessor
   *  with the named HTML template file.
   *  @param fileName the name of the template file
   *  @throw FileNotFoundException if there is no file at the given location
   *  @throw EOFException if an unexpected end-of-file is encountered
   *  @throw IOException if a problem occurs while reading the template file
   */
  public EncodingTemplate (String fileName)
      throws FileNotFoundException, EOFException, IOException
  {
    super(fileName);
  }

  /**
   *  Create this EncodingTemplate instance as a suitable TemplateProcessor
   *  with the given HTML template file.
   *  @param file the HTML template
   *  @throw FileNotFoundException if the file does not exist
   *  @throw EOFException if an unexpected end-of-file is encountered
   *  @throw IOException if a problem occurs while reading the template file
   */
  public EncodingTemplate (File file)
      throws FileNotFoundException, EOFException, IOException
  {
    super(file);
  }

  /**
   *  Override this method of TemplateProcessor to protect the HTML forms
   *  and fragile JavaScript codes from malicious user input.  In particular,
   *  certain template tags (those with encodeKey or jsEncodeKey prepended) will
   *  be encoded to make them safe for inclusion in HTML or JavaScript
   *  @param tag the tag being examined
   *  @return the String to substitute for tag in the output
   */
  protected String substitute (String tag) {
    String value = "";
    if (tag.startsWith(encodeKey)) {
      value = (String) get(tag.substring(encodeKey.length()));
      if (value != null)
        value = HTML.encode(value);
    }
    else if (tag.startsWith(jsEncodeKey)) {
      value = (String) get(tag.substring(jsEncodeKey.length()));
      value = HTML.jsEncode(value);
    }
    else {
      value = (String) get(tag);
    }
    if (value == null) {
      value = "";
    }
    return value;
  }
}
