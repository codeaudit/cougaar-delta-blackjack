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

import java.util.*;
import java.io.*;

public class PrettyPrinter {
  private int depth = 0;
  private String indentString = null;
  private PrintWriter out = null;

  public PrettyPrinter (PrintWriter o) {
    out = o;
    indentString = "  ";
  }

  public PrettyPrinter (PrintWriter o, String i) {
    out = o;
    indentString = i;
  }

  public void indent () {
    depth++;
  }

  public void exdent () {
    if (depth > 0)
      depth--;
  }

  public void print (String s) {
    StringTokenizer tok = new StringTokenizer(s, "\n", true);
    while (tok.hasMoreTokens()) {
      String line = tok.nextToken();
      if (line.equals("\n")) {
        while (tok.hasMoreTokens() && (line = tok.nextToken()).equals("\n"))
          out.println();
      }
      if (!line.equals("\n")) {
        if (line.length() > 0)
          addIndentation();
        out.println(line);
      }
    }
  }

  public void println () {
    out.println();
  }

  private void addIndentation () {
    for (int i = 0; i < depth; i++)
      out.print(indentString);
  }

  public void flush () {
    out.flush();
  }
}

