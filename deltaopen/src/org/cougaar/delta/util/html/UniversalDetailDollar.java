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
import java.text.*;
import java.io.*;

public class UniversalDetailDollar extends UniversalDetailItem {
  private VariantMap doubleMap = null;
  private static DecimalFormat dollarFormat =
    new DecimalFormat("#,##0.00");

  public UniversalDetailDollar (String n, String f) {
    name = n;
    field = f;
  }
  public UniversalDetailDollar (String n, String e, String f) {
    name = n;
    explanation = e;
    field = f;
  }

  public UniversalDetailDollar (String n, VariantMap vm) {
    name = n;
    doubleMap = vm;
  }
  public UniversalDetailDollar (String n, String e, VariantMap vm) {
    name = n;
    explanation = e;
    doubleMap = vm;
  }

  public UniversalDetailDollar (StringMap n, String f) {
    nameMap = n;
    field = f;
  }

  public UniversalDetailDollar (StringMap n, VariantMap vm) {
    nameMap = n;
    doubleMap = vm;
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
      Variant v = null;
      if (doubleMap != null)
        v = doubleMap.map(data);
      else
        v = data.getProperty(field);
      if (v != null) {
        double d = 0.0;
        if (v instanceof VariantDouble)
          d = ((VariantDouble) v).doubleValue();
        else if (v instanceof VariantFloat)
          d = (double) ((VariantFloat) v).floatValue();
        content = formatDouble(d);
      }
      else {
        content = "NOT FOUND";
      }
      generateRow(o, showName,
        "<table border=0 cellspacing=0 cellpadding=0><tr><td>$</td>" +
        "<td align=right width=125>" + content + "</td></tr></table>");
    }
    catch (Exception oh_no) {
      System.out.println("UniversalDetailItem::generateHtml:  ERROR in " +
      (showName != null ? showName : (name != null ? name : "Unknown field")) +
      "--" + oh_no);
    }
  }

  private String formatDouble (double d) {
    return dollarFormat.format(d);
  }
}
