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

import org.cougaar.delta.applet.BasicPanel;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.ObjectInputStream;
import java.net.*;
import java.awt.*;

/**
 * A tooltip for Fgi use
 * @author ALPINE (alpine-software@bbn.com)
 */

public class BasicToolTip {

  private JPopupMenu tooltip;
  private JTextArea textArea;
  private boolean showToolTip;

  private static Hashtable explanationTable = new Hashtable();

  public BasicToolTip() {
    super();
    setAttributes();
  }

  /*
   * This method will set some attributes effecting the general
   * appearance of this tooltip
   */
  private void setAttributes() {

    showToolTip = false;

    tooltip = new JPopupMenu();
    tooltip.setLayout(new BorderLayout());
    tooltip.setBackground(BasicPanel.isiLightBlue);
    tooltip.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
    textArea = new JTextArea("");
    textArea.setForeground(BasicPanel.black);
    textArea.setOpaque(false);
    textArea.setFont(BasicPanel.standardFont);
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setColumns(30);
    textArea.setWrapStyleWord(true);
    textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    tooltip.add(textArea, BorderLayout.NORTH);
  }

  public void setToolTipText(String s) {
    if (s != null && s.length() > 0) {
      textArea.setText(s);
      textArea.setRows(s.length()/textArea.getColumns());
      showToolTip = true;
    }
    else {
      textArea.setText("");
      showToolTip = false;
    }
  }

  /**
   * <label> is the GUI text being given a tool tip
   * <domain> may be: rule, contract, contract-detail, contract-edit,
   *  item, requisition, delivery-order, etc.
   */
  public void setToolTipTextFromDB(String domain, String label) {
    String explanation = lookupExplanation(domain, label);
    setToolTipText(explanation);
  }

  public static String lookupExplanation(String domain, String label) {

    Hashtable domainTable= new Hashtable();
    //if we haven't gotten this domain yet,
    if(! (explanationTable.containsKey(domain))) {
      domainTable= new Hashtable();
      //get the domain hashtable and add it to our explanation table
      String command = "?command=LOOKUPDOMAIN&label="+ URLEncoder.encode(label) +
          "&domain="+domain;
      String servletURL = BasicFrame.fgiDocumentBase + "ExplanationServlet";
      AppletToServletChannel ch = new AppletToServletChannel(servletURL);
      ch.setCookie(BasicFrame.fgiCookieString);

      AppletToServletParcel box = new AppletToServletParcel(command, null);
      try {
        box = (AppletToServletParcel)(ch.objectRequest(command, box)).readObject();
        domainTable = (Hashtable) box.parcel;
        explanationTable.put(domain, domainTable);
      }
      catch (Exception b_s) {
        System.out.println(
          "BasicToolTip::lookupexplanation:  ERROR retrieving \"" + domain +
          ":" + label + "\"--" + b_s);
      }
    }
    else {
      //if we already have this domain, just get it
      domainTable = (Hashtable)explanationTable.get(domain);
    }

    if(domainTable.containsKey(label)) {
      String ret = (String) domainTable.get(label);
      ret = ret.trim();
      return ( ret );
    }
    else {
      return("Didn't find an explanation for "+label+" in domain: "+domain);
    }
  }

  //make it visible
  public void show(MouseEvent e) {
    if (showToolTip)
      tooltip.show(e.getComponent(), e.getX() + 10, e.getY() + 10);
  }

  //stop showing it
  public void hide() {
    if (showToolTip)
      tooltip.setVisible(false);
  }
}
