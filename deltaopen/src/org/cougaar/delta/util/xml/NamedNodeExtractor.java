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
package org.cougaar.delta.util.xml;

import org.w3c.dom.*;
import java.math.*;

/**
 *  The NamedNodeExtractor class is a kind of wrapper for a NamedNodeMap.
 *  It is designed to provide easy access to the attributes of a Node,
 *  finding the correct value by name and converting it to an object of the
 *  appropriate type.
 */
public class NamedNodeExtractor {
  // A dummy NamedNodeMap to use whenever one cannot be found
  public static final NamedNodeMap emptyNamedNodeMap = new NamedNodeMap() {
    public Node getNamedItem (String p0) {
      return null;
    }
    public Node getNamedItemNS (String p0, String p1) {
      return null;
    }
    public Node setNamedItem (Node p0) throws DOMException {
      return null;
    }
    public Node setNamedItemNS (Node p0) throws DOMException {
      return null;
    }
    public Node removeNamedItem (String p0) throws DOMException {
      return null;
    }
    public Node removeNamedItemNS (String p0, String p1) throws DOMException {
      return null;
    }
    public Node item (int p0) {
      return null;
    }
    public int getLength () {
      return 0;
    }
  };

  // The NamedNodeMap holding the contents of this extractor
  private NamedNodeMap nnm = null;

  /**
   *  Create this NamedNodeExtractor with data contained in the provided
   *  NamedNodeMap instance.
   *  @param data The NamedNodeMap with the content data.
   */
  public NamedNodeExtractor (NamedNodeMap data) {
    nnm = data;
  }

  /**
   *  Create this NamedNodeExtractor with data being the attributes of the
   *  Node passed in from the caller.  If the Node has no attributes, substitute
   *  the emptyNamedNodeMap.
   *  @param n the Node whose attributes are to be extracted
   */
  public NamedNodeExtractor (Node n) {
    nnm = n.getAttributes();
    if (nnm == null)
      nnm = emptyNamedNodeMap;
  }

  /**
   *  Process a "Boolean" type attribute from an XML file.  We expect to find
   *  either "Y" or "N" as the String value, which is converted to true or
   *  false, respectively.  Any other Strings result in an Exception.  If an
   *  attribute by the name provided is not found, this method returns null.
   *  @param lookup the name of the attribute being accessed
   *  @return the Boolean representation of the attribute
   *  @throws Exception if the attribute's value is anything other than "Y" or "N"
   */
  public Boolean getBoolean (String lookup) throws Exception {
    Node n = nnm.getNamedItem(lookup);
    if (n == null)
      return null;
    String content = n.getNodeValue();
    if (content == null)
      return null;
    else if (content.length() == 0)
      throw new Exception("invalid boolean data in \"" + lookup +
        "\"--no characters found");
    else if (content.equals("Y"))
      return Boolean.TRUE;
    else if (content.equals("N"))
      return Boolean.FALSE;
    else
      throw new Exception("invalid boolean data in \"" + lookup +
        "\"--must be either 'Y' or 'N'");
  }

  /**
   *  Process a "Character" type attribute from an XML file, but return it
   *  as a String.  The expected String values are any Strings of length one or
   *  a token representing a single character.  In longer Strings (other than
   *  the character tokens) all characters beyond the first are ignored.  If no
   *  attribute by the given name can be found, then return null.
   *  @param lookup the name of the attribute being accessed
   *  @return the singleton String representing the character data
   *  @throws Exception if the attribute's value is a String of length zero
   */
  public String getCharAsString (String lookup) throws Exception {
    Character c = getChar(lookup);
    if (c != null)
      return c.toString();
    else
      return null;
  }

  /**
   *  Process a "Character" type attribute from an XML file.  The expected
   *  String values are singletons or character tokens.  In longer Strings
   *  (other than the character tokens) all characters beyond the first are
   *  ignored.  If no attribute by the given name can be found, then return
   *  null.
   *  @param lookup the name of the attribute being accessed
   *  @return the Character representation of the attribute
   *  @throws Exception if the attribute's value is a String of length zero
   */
  public Character getChar (String lookup) throws Exception {
    Node n = nnm.getNamedItem(lookup);
    if (n == null)
      return null;
    else
      return XML.decodeChar(n.getNodeValue());
  }

  /**
   *  Process an "Integer" type attribute.  If the attribute cannot be
   *  interpreted as an integer, then an Exception is thrown, whereas if it
   *  does not exist, then null is returned.
   *  @param lookup the name of the attribute being accessed
   *  @return an Integer representation of the attribute
   *  @throws Exception if the attribute's value is not a valid integer
   */
  public Integer getInteger (String lookup) throws Exception {
    Node n = nnm.getNamedItem(lookup);
    if (n == null)
      return null;
    String content = n.getNodeValue();
    if (content == null)
      return null;
    else if (content.length() == 0)
      throw new Exception("invalid integer data in \"" + lookup +
        "\"--no characters found");
    else
      return new Integer(content);
  }

  /**
   *  Process a "Double" type attribute.  If the attribute cannot be
   *  interpreted as a decimal number or is out of range, then an Exception is
   *  thrown.  If the attribute cannot be found, then return null.
   *  @param lookup the name of the attribute being accessed
   *  @return a Double representation of the attribute
   *  @throw Exception in case of an invalid Double value
   */
  public Double getDouble (String lookup) throws Exception {
    Node n = nnm.getNamedItem(lookup);
    if (n == null)
      return null;
    String content = n.getNodeValue();
    if (content == null)
      return null;
    else if (content.length() == 0)
      throw new Exception("invalid floating point data in \"" + lookup +
        "\"--no characters found");
    else
      return new Double(content);
  }

  /**
   *  Process a "BigDecimal" type attribute.  If there is
   *  any problem creating a BigDecimal from the String value of the attribute,
   *  an Exception is thrown.  If the attribute doesn't exist, then null is
   *  returned.
   *  @param lookup the name of the attribute being accessed
   *  @return a BigDecimal representation of this attribute
   *  @throws Exception if the BigDecimal cannot be created
   */
  public BigDecimal getBigDecimal (String lookup) throws Exception {
    Node n = nnm.getNamedItem(lookup);
    if (n == null)
      return null;
    String content = n.getNodeValue();
    if (content == null)
      return null;
    else if (content.length() == 0)
      throw new Exception("invalid decimal data in \"" + lookup +
        "\"--no characters found");
    else
      return new BigDecimal(content);
  }

  /**
   *  Process a simple String attribute.  Currently, character tokens are not
   *  replaced, but that may change in the future.
   *  @param lookup the name of the attribute being accessed
   *  @return the value of the attribute, or null if not found
   */
  public String getString (String lookup) {
    Node n = nnm.getNamedItem(lookup);
    if (n == null)
      return null;
    String content = n.getNodeValue();
    return content;
  }

  /**
   *  Give special consideration to the edi_transaction_sequence parameter
   *  whose actual values contain slash ('/') characters, which are encoded as
   *  dashes ('-') so that XML won't choke.
   *  @param lookup the name of the attribute, probably "edi_transaction_sequence"
   *  @return the decoded value of the attribute
   */
  public String getEdiTrans (String lookup) {
    Node n = nnm.getNamedItem(lookup);
    if (n == null)
      return null;
    String content = n.getNodeValue();
    return XML.edi_trans_from_xml(content);
  }
}
