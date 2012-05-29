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

import org.cougaar.delta.util.*;
import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import java.text.*;
import java.util.*;
import java.io.*;

public class XML {
  // don't be instantiating this class
  private XML () { }

  /**
   *  A token used to represent the space character (' ')
   */
  public static final String spaceToken = "_SPACE_";

  /**
   *  A token used to represent the asterisk character ('*')
   */
  public static final String starToken = "_STAR_";

  /**
   *  A token used to represent the percent character ('%')
   */
  public static final String percentToken = "_PERCENT_";

  /**
   *  A token used to represent the forward slash character ('/', and yes, for
   *  for those of you who were wondering, this really is a forward slash, not
   *  a backslash)
   */
  public static final String slashToken = "_SLASH_";

  /**
   *  A token used to represent an underscore character ('_').  Though this is
   *  not actually needed now, it is included in case at some future time
   *  Strings will be parsed with underscores used as escape characters.
   */
  public static final String underscoreToken = "_UNDERSCORE_";

  /**
   *  A utility method that looks for occurrences of a substring and replaces
   *  them with another string.  The resulting string is returned.
   */
  public static String findAndReplace (String s, String find, String replace) {
    if (s == null) return null;
    int k = 0;
    int n = s.length();
    int f = find.length();
    StringBuffer buf = new StringBuffer();
    while (k < n) {
      int i = s.indexOf(find, k);
      if (i == -1) {
        buf.append(s.substring(k));
        break;
      }
      else {
        buf.append(s.substring(k, i));
        buf.append(replace);
        k = i + f;
      }
    }
    return buf.toString();
  }

  /**
   *  Write an edi_transaction_sequence to an XML file substituting "-" for
   *  "/", since the character '/' is not allowed in enumerated tokens in XML
   */
  public static String edi_trans_to_xml (String target) {
    return findAndReplace(target, "/", "-");
  }

  /**
   *  Convert an encoded edi_transaction_sequence to its proper representation
   *  by substituting the (correct) slashes for the place-holder dashes
   */
  public static String edi_trans_from_xml (String target) {
    return findAndReplace(target, "-", "/");
  }

  /**
   *  Replace characters that XML doesn't like with tokens.  Currently supported
   *  are ' ', '*', '%', '/', and '_', though others could be added later.
   *  @param c the Character to be encoded
   *  @return a String representing the character
   */
  public static String encodeChar (Character c) {
    if (c == null)
      return null;
    switch (c.charValue()) {
      case ' ' :  return spaceToken;
      case '*' :  return starToken;
      case '%' :  return percentToken;
      case '/' :  return slashToken;
      case '_' :  return underscoreToken;
      default  :  return c.toString();
    }
  }

  /**
   *  Decode Character data by replacing character tokens with the corresponding
   *  Character values.
   *  @param s A String containing the character data
   *  @return the decoded Character value
   *  @throws Exception if the argument is a String of length zero
   */
  public static Character decodeChar (String s) throws Exception {
    if (s == null)
      return null;
    if (s.length() == 0)
      throw new Exception("no character data");
    Character c = null;
    if (s.equals(spaceToken))
      c = new Character(' ');
    else if (s.equals(starToken))
      c = new Character('*');
    else if (s.equals(percentToken))
      c = new Character('%');
    else if (s.equals(slashToken))
      c = new Character('/');
    else if (s.equals(underscoreToken))
      c = new Character('_');
    else
      c = new Character(s.charAt(0));
    return c;
  }

  /**
   *  Returns a String containing an attribute node of the form:
   *  <br>
   *  <br>
   *  &lt;a name="<I>name</I>"&gt<I>value</I>&lt/a&gt
   *  <br>
   *  <br>
   *  @param name the attribute name
   *  @param value the attribute value
   *  @return the attribute node string
   */
  static public String putAttributeNode(String name, String value) {
    return "<a name=\"" + name + "\">" + value + "</a>";
  }

  /**
   *  Capitalize on the paralellism between a PropertyBundle and a list of
   *  attribute names and values in an XML node.  The names and types of the
   *  attributes in which we are interested must be supplied by the caller.
   *  @param t the node whose attributes are to be inspected
   *  @param types A Hashtable containing the types of all of the values for this
   *      PropertyBundle referenced by attribute name
   *  @return a PropertyBundle with the given names paired with attribute values
   *//*
  public static PropertyBundle getPropertyBundle (Node t, Hashtable types) {
    PropertyBundle bun = new PropertyBundle();
    return getPropertyBundle(t, types, bun);
  }*/

  /**
   *  Capitalize on the paralellism between a PropertyBundle and a list of
   *  attribute names and values in an XML node.  The names and types of the
   *  attributes in which we are interested must be supplied by the caller.
   *  @param t the node whose attributes are to be inspected
   *  @param types A Hashtable containing the types of all of the types for this
   *      PropertyBundle referenced by attribute name
   *  @param bun An inital PropertyBundle that will be modified with the new values
   *  @return the PropertyBundle object that was passed in with the given names
   *      paired with attribute values
   *//*
  public static PropertyBundle getPropertyBundle (Node t, Hashtable types, PropertyBundle bun) {
    // First, check if this Node has a delete tag as a child.  If so, remove all values from the
    // PropertyBundle
    ChildEnumerator ce = new ChildEnumerator(t);
    Node child = ce.current();
    // If there is a delete node, it should be first.
    if (child != null && child.getNodeName().equals("delete")) {
      bun.clear();
      child = ce.next();
      if (child != null)
        System.err.println("XML.getPropertyBundle: Found child \"" + child.getNodeName() + "\" after delete node; Ignoring.");
      return bun;
    }

    AttributeExtractor ae = new AttributeExtractor(t);
    Enumeration enm = ae.keys();
    // Drag out the many attributes for this tag.
    while (enm.hasMoreElements()) {
      String name = (String) enm.nextElement();
      // If the attribute contained a Delete node rather than a regular value, remove it
      // from the bundle.
      if (ae.getString(name).equals(AttributeExtractor.DELETE_VALUE)) {
        bun.remove(name);
      }
      else {
        try {
          // check for the type of this attribute
          String type = (String) types.get(name);
          if (type == null)
            System.err.println("XML.putPropertyBundle: No type found for node: \"" + name + "\"");
          else if (type.equals("Boolean"))
            bun.put(name, ae.getBoolean(name));
          else if (type.equals("Character"))
            bun.put(name, ae.getChar(name));
          else if (type.equals("CharAsString"))
            bun.put(name, ae.getCharAsString(name));
          else if (type.equals("Integer"))
            bun.put(name, ae.getInteger(name));
          else if (type.equals("Double"))
            bun.put(name, ae.getDouble(name));
          else if (type.equals("BigDecimal"))
            bun.put(name, ae.getBigDecimal(name));
          else if (type.equals("String"))
            bun.put(name, ae.getString(name));
          else if (type.equals("EdiTransaction"))
            bun.put(name, ae.getEdiTrans(name));
          else
            System.out.println("XML::getPropertyBundle:  unsupported type \"" +
              type + "\" for attribute \"" + name + "\"");
        }
        catch (Exception e) {
          System.out.println("Trouble parsing \"" + name + "\":  " +
          e.getMessage());
        }
      }
    }
    return bun;
  }*/

  /**
   *  Capitalize once again on the parallelism between an attribute set and a
   *  PropertyBundle.  This time, produce an XML String to represent the Java
   *  object(s).  The result is a complete XML tag of the specified type, with
   *  attributes dictated by the attribute index and values drawn from the
   *  Property Bundle.
   *  @param bun the PropertyBundle carrying the attribute values
   *  @param index an array containing the names and ostensible types of the attributes
   *  @return a String containing the XML text
   *//*
  public static String putPropertyBundle (
      PropertyBundle bun, String[][] index)
  {
    return putPropertyBundle(
      bun, index, "", new XmlTagSet(new String[0]));
  }*/

  /**
   *  Capitalize once again on the parallelism between an attribute set and a
   *  PropertyBundle object.  This time, procude an XML String to represent the
   *  Java object(s).  Here, we generate the XML element of the given type with
   *  attributes from the index, values from the property bundle and child
   *  elements provided by the caller in the form of an XmlTagSet object.
   *  @param bun the PropertyBundle carrying the attribute values
   *  @param index an array containing the names and ostensible types of the attributes
   *  @param children the set of child elements for this XML element
   *  @return a String containing the XML text
   *//*
  public static String putPropertyBundle (
      PropertyBundle bun, String[][] index,
      String otherAttributes, XmlTagSet children)
  {
    StringBuffer buf = new StringBuffer();
    if (otherAttributes != null)
      buf.append(otherAttributes);
    if (bun != null) {
      for (int i = 0; i < index.length; i++) {
        Object val = bun.get(index[i][0]);
        String str = null;
        if (val != null) {
          if (val instanceof Boolean) {
            str = (((Boolean) val).booleanValue() ? "Y" : "N");
          }
          else if (index[i][1].equals("EdiTransaction")) {
            str = edi_trans_to_xml(val.toString());
          }
          else {
            str = val.toString();
          }

          if (str != null) {
            buf.append(putAttributeNode(index[i][0], str) + "\n");
          }
        }
      }
    }
    if (children != null && !children.isEmpty())
      buf.append(children.generateXml());
    return buf.toString();
  }*/

/*
  public class SpecialResolver implements EntityResolver {
    public InputSource resolveEntity(String publicID, String systemID) {
      InputSource is = new InputSource(systemID);
      return is;
    }
    }
    */

}
