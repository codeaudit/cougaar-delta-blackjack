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
package org.cougaar.delta.applet;

import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;

public class CompletingJTextField extends RestrictedJTextField {
  private String[] choices = null;

  public void setChoices (String[] c) {
    choices = c;
  }

  public String[] getChoices () {
    return choices;
  }

  public CompletingJTextField () {
    super();
    setupCompletionKey();
  }

  public CompletingJTextField (int n) {
    super(n);
    setupCompletionKey();
  }

  public CompletingJTextField (String s) {
    super(s);
    setupCompletionKey();
  }

  public CompletingJTextField (int n, String s) {
    super(n, s);
    setupCompletionKey();
  }

  private void setupCompletionKey () {
    Keymap km = getKeymap();
    km.addActionForKeyStroke(KeyStroke.getKeyStroke('\n'), getTabEar());
    setKeymap(km);
  }

  private Action getTabEar () {
    return new AbstractAction () {
      public void actionPerformed (ActionEvent ae) {
        respondToTab();
      }
    };
  }

  private void respondToTab () {
    Document d = getDocument();
    try {
      d.insertString(d.getLength(), executeCompletionSearch(), null);
    }
    catch (Exception b_s) { }
    setCaretPosition(d.getLength());
  }

  private String executeCompletionSearch () {
    System.out.println("Doing completion");
    if (choices == null || choices.length == 0) {
      return "";
    }
    String prefix = getText();
    int n = prefix.length();
    Vector v = new Vector();
    char nextChar = (char) 0;
    for (int i = 0; i < choices.length; i++) {
      if (choices[i].startsWith(prefix)) {
        if (choices[i].length() == n)
          return "";
        if (v.size() == 0)
          nextChar = choices[i].charAt(n);
        else if (choices[i].charAt(n) != nextChar)
          return "";
        v.addElement(choices[i]);
      }
    }
    if (v.size() == 0)
      return "";

    String[] matches = new String[v.size()];
    Enumeration enu = v.elements();
    for (int i = 0; enu.hasMoreElements(); i++) {
      matches[i] = (String) enu.nextElement();
    }

    StringBuffer buf = new StringBuffer();
    buf.append(nextChar);
    for (int i = n + 1; i < matches[0].length(); i++) {
      nextChar = matches[0].charAt(i);
      for (int j = 1; j < matches.length; j++) {
        if (matches[j].length() == i || matches[j].charAt(i) != nextChar)
          return buf.toString();
      }
      buf.append(nextChar);
    }
    return buf.toString();
  }

  // - - - - - - - Testing Scaffolding - - - - - - - - - - - - - - - - - - - - -

  public static void main (String[] argv) {
    Font ruleElementFont = new Font("Arial", Font.PLAIN, 14);
    JFrame frame = new JFrame();
    frame.setSize(400, 400);
    frame.getContentPane().setLayout(new FlowLayout());
    CompletingJTextField fred = new CompletingJTextField(20,
      "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_");
    fred.setInitialsSet("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    fred.setInverseSet(false);
    fred.setMaxLength(20);
    fred.setChoices(new String[] {"bla-hahahahaha", "bla-blabla", "bla-harrumph", "bla", "hahaha"});

    fred.setFont(ruleElementFont);
    frame.getContentPane().add(fred);
    frame.validate();
    frame.setVisible(true);
  }
}
