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

import org.cougaar.delta.util.variant.*;
import java.io.*;

/**
 *  UniversalDetailItemLink is a subclass of UniversalDetailItem which is
 *  endowed with a hypertext link on the value (right side) in this row.
 */
public class UniversalDetailItemLink extends UniversalDetailItem {
  private StringMap linkMap = null;

  /**
   *  Construct this object on top of the appropriate UniversalDetailItem
   *  with the given StringMap to compute the hyperlink associated with
   *  the value in this row.
   *  @param n the name (left side) of this row
   *  @param f the name of the field containing the value (right side) of this row
   *  @param l a StringMap to calculate the hypertext link
   */
  public UniversalDetailItemLink (String n, String f, StringMap l) {
    super(n, f);
    linkMap = l;
  }
  /**
   * Same as version w/o String e parameter, except <e> is used as a
   * help message for the text of <n>
   * @param e the help message which is to pop up when the mouse is
   * passed over the text of <n>
   */
  public UniversalDetailItemLink (String n, String e, String f, StringMap l) {
    super(n, e, f);
    linkMap = l;
  }

  /**
   *  Construct this object on top of the appropriate UniversalDetailItem
   *  with the given StringMap to compute the hyperlink associated with
   *  the value in this row.
   *  @param n a StringMap to calculate the name (left side) of this row
   *  @param f the name of the field containing the value (right side) of this row
   *  @param l a StringMap to calculate the hypertext link
   */
  public UniversalDetailItemLink (StringMap n, String f, StringMap l) {
    super(n, f);
    linkMap = l;
  }

  /**
   *  Construct this object on top of the appropriate UniversalDetailItem
   *  with the given StringMap to compute the hyperlink associated with
   *  the value in this row.
   *  @param n the name (left side) of this row
   *  @param f a StringMap to calculate the value (right side) of this row
   *  @param l a StringMap to calculate the hypertext link
   */
  public UniversalDetailItemLink (String n, StringMap f, StringMap l) {
    super(n, f);
    linkMap = l;
  }
  /**
   * Same as version w/o String e parameter, except parameter e is used as a
   * help message for the text of parameter n.
   * @param e the help message which is to pop up when the mouse is
   * passed over the text of parameter n
   */
  public UniversalDetailItemLink (String n, String e, StringMap f, StringMap l) {
    super(n, e, f);
    linkMap = l;
  }

  /**
   *  Construct this object on top of the appropriate UniversalDetailItem
   *  with the given StringMap to compute the hyperlink associated with
   *  the value in this row.
   *  @param n a StringMap to calculate the name (left side) of this row
   *  @param f a StringMap to calculate the value (right side) of this row
   *  @param l a StringMap to calculate the hypertext link
   */
  public UniversalDetailItemLink (StringMap n, StringMap f, StringMap l) {
    super(n, f);
    linkMap = l;
  }

  public UniversalDetailItemLink (String n, VariantMap f, StringMap l) {
    super(n, f);
    linkMap = l;
  }
  public UniversalDetailItemLink (String n, String e, VariantMap f, StringMap l) {
    super(n, e, f);
    linkMap = l;
  }

  public UniversalDetailItemLink (StringMap n, VariantMap f, StringMap l) {
    super(n, f);
    linkMap = l;
  }

  /**
   *  Generate the HTML representation for this line in the table.  This
   *  method should be called from the containing {@link UniversalDetailSection}
   *  @param o A PrintWriter to take the HTML output
   *  @param data the source for the content info
   */
  public void generateHtml (PrintWriter o, DataWrapper data) {
    String content = null;
    String showName = null;
    try {
      if (nameMap != null) {
        showName = nameMap.map(data);
      }
      else {
        showName = name;
      }
      if (contentMap != null) {
        content = contentMap.map(data);
      }
      else {
        content = data.getProperty(field).toString();
      }
      if (!usesHtml) {
        content = HTML.encode(content, true);
      }
      String hyperlink = null;
      if (linkMap != null && (hyperlink = linkMap.map(data)) != null) {
        content = "<a href=\"" + hyperlink + "\">" + content + "</a>";
      }
      generateRow(o, showName, content);
    }
    catch (Exception oh_no) {
      System.out.println("UniversalDetailItemLink::generateHtml:  ERROR--" +
        oh_no);
    }
  }
}
