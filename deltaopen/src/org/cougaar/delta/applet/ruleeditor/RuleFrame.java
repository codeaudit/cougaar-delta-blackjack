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
package org.cougaar.delta.applet.ruleeditor;

import org.cougaar.delta.util.qrule.*;
import org.cougaar.delta.applet.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.net.*;
import java.io.*;

/**
 *  The RuleFrame class provides the major functionality for the client-side
 *  Rule Editor.  In effect, a RuleFrame instance provides a graphical
 *  representation of a QRule object that can be viewed and/or manipulated by
 *  the user.
 *  <br><br>
 *  It appears as a separate JFrame and can be iconified, moved, or resized at
 *  will by the user.
 */
public class RuleFrame extends BasicFrame {
  // The Rule Editor
  private RuleEditPane editor = null;

  /**
   *  Construct a new RuleFrame instance.  In its current incarnation, a
   *  RuleFrame depends on the Applet that spawned it for its ability to
   *  communicate with the server.
   *  @param theApplet the RuleEditApplet associated with this RuleFrame
   */
  public RuleFrame (RuleServerSupport rss) {

    RuleEditApplet rea = (RuleEditApplet) rss;
    fgiDocumentBase = rea.getDocumentBase().toString();
    fgiCookieString = rea.getParameter("sessionCookie");
    System.out.println("Rule Frame cookies string:" + fgiCookieString);

    editor = new RuleEditPane(rss);
    editor.addCancelListener(new CancelListener());

    // initialize the GUI
    try  {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  // Component initialization
  private void jbInit() throws Exception  {
    // set up the JFrame
    setSize(1000, 500);
    setTitle("Rule Editor");
    addWindowListener(new WindowClosingEar());

    Container content = getContentPane();
    VerticalLayout lay = new VerticalLayout(content);

    // install the rule editor
    lay.add(editor, 0, -1, VerticalLayout.STRETCH);
  }

  // An inner class that collects requests to close the editor
  private class CancelListener implements ActionListener {
    public void actionPerformed (ActionEvent e) {
      stop();
    }
  }

  // An ear that listens for when the window is being closed so that some
  // clean-up can be performed before it's too late
  private class WindowClosingEar extends WindowAdapter {
    public void windowClosing(WindowEvent we) {
      stop();
    }
  }

  /**
   *  Load a rule into this RuleFrame for editing.
   *  @param q the QRule being loaded
   */
  public void loadRule (QRule q) {
    if (editor != null)
      editor.loadRule(q);
  }

  /**
   *  Start the rule editor
   */
  public void start () {
    if (editor != null)
      editor.start();
  }

  /**
   *  Stop the rule editor
   */
  public void stop () {
    if (editor != null)
      editor.stop();
    setVisible(false);
    dispose();
  }

// - - - - - - - - - TEST SCAFFOLDING BELOW THIS POINT - - - - - - - - - - - - -

  public static void main (String[] argv) {
    makeTestRules();
    RuleFrame frame = new RuleFrame(new DummyApplet());
    frame.validate();
    frame.setVisible(true);
    frame.loadRule(rule1);
  }

  private static void makeTestRules () {
  }

  private static QRule rule1;
  private static QRule rule2;
}

/**
 *  This class pretends to be an applet for debugging the applet frame as an
 *  application
 */
class DummyApplet extends RuleEditApplet {
  public URL getDocumentBase() {
    try {
      return new URL("http://al:8000/servlet/");
    } catch (java.net.MalformedURLException e) {}
    return null;
  }

  public String getParameter(String parm1) {
    return null;
  }
}
