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
import javax.swing.border.*;
import java.awt.event.*;
import javax.swing.*;
import java.applet.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import java.io.*;

/**
 *  The RuleEditApplet class is intended to be the client-side envoy for the
 *  management of QRules in the system.  Its major functions are providing
 *  access to the database (by way of the RuleEditServlet) and launching the
 *  rule editor upon demand, either to create a new rule or to edit an existing
 *  one.
 *  <br><br>
 *  The GUI for the Applet itself contains a single element, a JButton, whose
 *  appearance and behavior are based on information supplied by the Servlet
 *  in the form of HTML parameters in the applet tags.
 */
public class RuleEditApplet extends JApplet implements RuleServerSupport {
  // a communications channel for interacting with the Servlet
  private AppletToServletChannel channel = null;

  // the cookie values corresponding to the current HTTP session
  private String mySessionCookies = null;

  // This string represents the name of the Servlet that sent this Applet to
  // the client.
  private String spawningServlet = null;

  // parametric information concerning this applet instance's intended behavior
  // Currently, there are two "modes":  creating a new rule from scratch and
  // editing one that already exists.
  private String buttonText = null;
  private String intendedTask = null;

  // A locking mechanism to prevent a new editor from being spawned while
  // another one is still in the process of loading
  private RuleEditorLoader editorLoader = null;

  // A button which is shown to activate the editor
  private JButton editButton = null;

  /**
   *  Retrieve the name of the Servlet that spawned this Applet.  This is useful
   *  to know for communications with the server.
   *  @return the spawning Servlet's name.
   */
  public String getSpawningServlet () {
    return spawningServlet;
  }

  /**
   *  Get a reference to the AppletToServletChannel instance maintained by this
   *  Applet and used for communications with the Servlet.
   *  @return the channel
   */
  public AppletToServletChannel getChannel () {
    return channel;
  }

  /**
   * Overrides the Applet getDocumentBase() method.  Newer JVMs apparently believe
   * that the DocumentBase should include the Servlet name when the Document is created
   * by a Servlet. DELTA (and older JVMs) do not. This method allows DELTA to handle
   * both cases
   */
  public URL getDocumentBase()
  {
    String temp = super.getDocumentBase().toString();
    int ix = temp.lastIndexOf("/") + 1;
    URL retval = null;
    try
    {
        retval = new URL(temp.substring(0, ix));
    }
    catch (MalformedURLException mue)
    {
      throw new RuntimeException("RuleEditApplet::getDocumentBase:Invald DocumentBase: " + temp.substring(0, ix));
    }
    return retval;
  }

  /**
   *  Initialize this Applet.  In particular, read the parameters and store the
   *  relevant information.
   */
  public void init () {
    editorLoader = new RuleEditorLoader(this);

    mySessionCookies = getParameter("sessionCookie");

    if (channel == null) {
      channel = new AppletToServletChannel(getDocumentBase().toString());
      channel.setCookie(mySessionCookies);
    }

    if (spawningServlet == null) {
      spawningServlet = getParameter("spawningServlet");
    }
    if (spawningServlet == null || spawningServlet.equals(""))
      spawningServlet = "OpenRuleEdit";
    buttonText = getParameter("buttonText");
    if (buttonText == null)
      buttonText = "?????";
    intendedTask = getParameter("intendedTask");
    if (intendedTask == null)
      intendedTask = "WHAT_DO_I_DO";

    if (!RuleEditPane.hasButtonFactory())
      RuleEditPane.setButtonFactory(new ImageButtonFactory(
        getCodeBase(), getParameter("ARCHIVE"), "../art/"));
  }

  // this inner class listens for the button being pressed
  private class ButtonEar implements ActionListener {
    public void actionPerformed (ActionEvent ae) {
      respondToButtonPress();
    }
  }

  /**
   *  Start this Applet.  Specifically, set up the GUI and wait for the user
   *  to click the button.
   */
  public void start () {
    editButton = new JButton(buttonText);
    editButton.setFont(new Font("Arial", Font.PLAIN, 14));
    editButton.setBackground(new Color(0xBBBBBB));
    editButton.setBorder(new BevelBorder(BevelBorder.RAISED));
    editButton.addActionListener(new ButtonEar());

    Container jr = getContentPane();
    jr.setLayout(new BorderLayout());
    jr.add(editButton, BorderLayout.CENTER);
  }

  /**
   *  Stop this Applet.
   */
  public void stop () {
  }

  // Bring up the Rule Editor and initialize it with a given QRule instance for
  // editing
  private void showRuleEditor (QRule q) {
    editorLoader.showEditor(q);
  }

  // An inner class whose purpose is to spawn the Rule Editor, never allowing
  // two editors to be loading at the same time (though two can be up at the
  // same time).  This prevents users from accidentally opening two editors by
  // clicking on the button while one is already being created
  private static class RuleEditorLoader implements Runnable {
    private boolean locked_flag = false;
    RuleEditApplet theApplet = null;
    QRule qrule = null;

    public RuleEditorLoader (RuleEditApplet a) {
      theApplet = a;
    }

    public void showEditor (QRule q) {
      synchronized (this) {
        if (locked_flag)
          return;
        else {
          locked_flag = true;
          qrule = q;
        }
      }
      (new Thread(this)).start();
    }

    public void run () {
      RuleFrame frame = new RuleFrame(theApplet);
      frame.loadRule(qrule);
      frame.setVisible(true);
      frame.getContentPane().validate();
      frame.start();
      synchronized (this) {
        locked_flag = false;
      }
    }
  }

  // the user has clicked on the button--bring up the rule editor in whichever
  // "mode" is appropriate under the circumstances
  private void respondToButtonPress () {
    QRule q = null;
    if (intendedTask.equalsIgnoreCase("EDIT")) {
      q = requestRule("GIMME");
    }
    else if (intendedTask.equalsIgnoreCase("CREATE") &&
        serverRequest("KEEP_SESSION", null) != null)
    {
      q = new QRule(null, null);
    }

    if (q != null)
      showRuleEditor(q);
  }

  // Request a rule from the server using the given command word.
  private QRule requestRule (String directive) {
    QRule q = (QRule) serverRequest(directive, null);
    if (q == null)
      q = new QRule(null, null);

    return q;
  }

  /**
   *  Forward a request to the server via the AppletToServletChannel maintained
   *  by this Applet.  In case of a problem connecting to the Servlet, this
   *  method will make five attempts.
   *
   *  @param command a directive informing the Servlet of the nature of the request
   *  @param obj a payload being sent to the Servlet
   */
  public synchronized Serializable serverRequest (String command, Serializable obj) {
    AppletToServletParcel give = new AppletToServletParcel(command, obj);
    Serializable q = null;
    try {
      AppletToServletParcel take = null;
      for (int i = 0; i < 5 && q == null; i++) {
        if (channel == null) System.out.println("channel is null");
        take = (AppletToServletParcel)
          channel.objectRequest(spawningServlet, give).readObject();
        if (!(take.parcel instanceof Exception)) {
          q = take.parcel;
        }
      }
      if (take.parcel instanceof Exception) throw (Exception) take.parcel;
    }
    catch (Exception eek) {
      System.out.println(
        "RuleEditApplet::serverRequest:  Error connecting to server--" + eek);
      eek.printStackTrace();
      displayErrorMessage("Error connecting to server!",
        "Unable to connect to the server.");
      return null;
    }

    return q;
  }

  // display an error message as a pop-up dialog box.
  private void displayErrorMessage (String title, String message) {
    JOptionPane.showMessageDialog(null, message, title,
      JOptionPane.ERROR_MESSAGE);
  }

  // - - - - - - - Testing Code Below This Point - - - - - - - - - - - - - - - -

  public static void main (String[] argv) {
    RuleEditApplet a = new RuleEditApplet();
    a.displayErrorMessage("Fake error...", "Too bad!  This is an ERROR!");
  }
}
