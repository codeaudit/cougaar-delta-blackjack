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

public class HTML {
  /**
   *  Encode String literals for inclusion in HTML documents.  Automatic
   *  handling of strings being passed to and fro requires a type of encoding
   *  that will not break interpretation of HTML elements such as input fields
   *  and table cells.  Characters such as quotes, spaces, angle brackets, etc.
   *  might cause problems otherwise.
   *  <br><br>
   *  Optionally, newline characters are converted to HTML line breaks
   *
   *  @param s the actual value of the string to be encoded
   *  @param br if true, '\n' is converted to "<br>"; otherwise it is simply encoded
   *  @return the encoded string
   */
  public static String encode (String s, boolean br) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      buf.append(charEncode(s.charAt(i), br));
    }
    return buf.toString();
  }

  /**
   *  Do the same encoding as above, with no newline to line break substitution
   *  @param s the string to encode
   *  @return the encoded string
   */
  public static String encode (String s) {
    return encode(s, false);
  }

  // encode characters as HTML literal expressions
  // newline characters are treated as regular characters
  private static String charEncode (char c) {
    return charEncode (c, false);
  }

  // do a character encoding with optional conversion of newline
  // characters to HTML line breaks
  private static String charEncode (char c, boolean br) {
    switch (c) {
      case ' ' :  return "&#32;";
      case '"' :  return "&#34;";
      case '&' :  return "&#" + (int) '&' + ";";
      case ';' :  return "&#" + (int) ';' + ";";
      case '<' :  return "&#" + (int) '<' + ";";
      case '>' :  return "&#" + (int) '>' + ";";
      case '\'' : return "&#" + (int) '\'' + ";";
      case '`' :  return "&#" + (int) '`' + ";";
      case '\n' : return (br ? "<br>" : "&#" + (int) '\n' + ";");
      default  :  return String.valueOf(c);
    }
  }

  /**
   *  Encode a string as a JavaScript String literal for inclusion in a piece
   *  of javascript code.  The typical usage of this is function will probably
   *  be something like
   *  <ul>
   *    <li>o.println("groupName = " + jsEncode(groupName) + ";");</li>
   *  </ul>
   *  to give output directly, or
   *  <ul>
   *    <li>groupName = ##GROUP_NAME##;</li>
   *  </ul>
   *  in a template file.  The quotation marks surrounding the text are
   *  automatically included.
   */
  public static String jsEncode (String s) {
    if (s == null) return "\"\"";
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      buf.append(jsCharEncode(s.charAt(i)));
    }
    String literal = buf.toString();
    String upper = literal.toUpperCase();
    // check for substrings which are case-insensitive matches for "</script>"
    // those are bad...
    String bad = "</SCRIPT>";
    buf = new StringBuffer("\"");
    int j = 0;
    int n = 0;
    while ((j = upper.indexOf(bad, n)) > -1) {
      int m = j + bad.length();
      buf.append(literal.substring(n, j + 4) + "\" + \"" +
        literal.substring(j + 4, m));
      n = m;
    }
    buf.append(literal.substring(n));
    buf.append("\"");
    return buf.toString();
  }

  private static String jsCharEncode (char c) {
    switch (c) {
      case '"' :  return "\\\"";
      case '\\' : return "\\\\";
      case '\n' : return "\\n";
      case '\r' : return "\\r";
      case '\t' : return "\\t";
      default :   return String.valueOf(c);
    }
  }
}
