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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.*;
import java.util.*;

import org.cougaar.delta.applet.event.BasicPanelEvent;
import org.cougaar.delta.applet.event.BasicPanelListener;
import org.cougaar.delta.applet.event.BasicPanelEventMulticaster;

/**
 * A Panel that allows for addition of buttons/text with a color gradient background
 */
public class BasicPanel extends JPanel implements MouseListener,
						ActionListener{

  protected Color color1;
  protected Color color2;

  // Keep a vector of editable components that are displayed in this panel
  private Vector editableComponents;

  public static final Color black = new Color(0,0,0);
  public static final Color white = new Color(255,255,255);
  public static final Color red = new Color(255,0,0);
  public static final Color green = new Color(0, 255, 0);
  public static final Color blue = new Color(0,0,255);
  public static final Color isiLightBlue = new Color(202, 218, 220);
  public static final Color isiDarkBlue = new Color(150, 180, 185);
  public static final Color isiBrown = new Color(200,200,165);
  public static final Color gray = Color.gray;
  public static final Font titleFont = new Font("Arial", Font.BOLD, 16);
  public static final Font attributeNameFont = new Font("Arial", Font.BOLD, 14);
  public static final Font largeTitleFont = new Font("Arial", Font.BOLD, 20);
  public static final Font standardFont = new Font("Arial", Font.PLAIN, 13);
  public static final Font largeFont = new Font("Arial", Font.PLAIN, 14);
  public static final Font largeBoldFont = new Font("Arial", Font.BOLD, 14);
  public static final Font smallFont = new Font("Arial", Font.BOLD, 12);

  protected BasicPanelListener fgiPanelListener = null;

  protected GridBagConstraints gbcNames;
  protected GridBagConstraints gbcValues;

  // Setup the Vectors needed to add buttons to Panel
  protected Vector buttonImages;
  protected Vector buttonPositions;
  protected Vector buttonActions;

  // Setup the Vectors needed to add text to Panel
  protected Vector textStrings;
  protected Vector textPositions;
  protected Vector textFonts;

  // Vector for unused components
  protected Vector unusedComponents;


  /**
   * Constructor
   * @param c1 start color of gradient background
   * @param c2 end color of gradient background
   */
  public BasicPanel(Color c1, Color c2) {

    color1 = c1;
    color2 = c2;

    init();
  }

  /**
   * Constructor producing standard FGI background
   */
  public BasicPanel() {

    color1 = isiLightBlue;
    color2 = white;

    init();
  }

  /**
   * method to do some initalization of FgiPanel
   */
  private void init() {
    // Set this panel up as a MouseListener so it can respond
    // to user clicks on buttons.
    addMouseListener(this);

    // Create a dark border around the Panel
    this.setBorder(BorderFactory.createLineBorder(black, 2));

    // Create empty Vectors for buttons and Strings to be displayed
    buttonImages = new Vector();
    buttonPositions = new Vector();
    buttonActions = new Vector();
    textStrings = new Vector();
    textPositions = new Vector();
    textFonts = new Vector();

    gbcNames = new GridBagConstraints();
    gbcNames.anchor = GridBagConstraints.NORTHEAST;
    gbcNames.insets = new Insets(5, 5, 0, 10);
    gbcNames.gridx = 0;
    gbcNames.gridwidth = 1;
    gbcNames.weighty = 1;

    gbcValues = new GridBagConstraints();
    gbcValues.anchor = GridBagConstraints.NORTHWEST;
    gbcValues.insets = new Insets(2, 10, 2, 5);
    gbcValues.gridx = 1;
    gbcValues.gridwidth = 2;
    gbcValues.weighty = 1;

    editableComponents = new Vector();


    unusedComponents = new Vector();

  }

  /**
   * overidden paintComponent method
   * @param g current Graphics context
   */
  public void paintComponent(java.awt.Graphics g)
  {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;
    int width = getWidth();
    int height = getHeight();
    Color oldColor = g.getColor();
    GradientPaint gp = new GradientPaint(0f,0f,color1,0f,(float)height,color2);
    g2.setPaint(gp);
    g2.fillRect(0,0,width,height);
    g.setColor(oldColor);

    // Place buttons
    for (int i=0; i<buttonImages.size(); i++) {
      Image img = (Image)buttonImages.elementAt(i);
      Point p = (Point)buttonPositions.elementAt(i);
      g2.drawImage(img, p.x, p.y, this);
    }

    // Place Strings
    for (int i=0; i<textStrings.size(); i++) {
      Point p = (Point)textPositions.elementAt(i);
      g2.setColor(new Color(0,0,0));
      g2.setFont((Font)textFonts.elementAt(i));
      g2.drawString((String)textStrings.elementAt(i), p.x, p.y);
    }
  }

  /**
   * method to add button to Panel
   * @param bimg image to use as button
   * @param baction string for actionCommand fired
   * @param bpoint position of button on Panel
   */
  public void addButton(Image bimg, String baction, Point bpoint) {
    buttonImages.add(bimg);
    buttonActions.add(baction);
    buttonPositions.add(bpoint);
  }

  /**
   * method to add text to Panel
   * @param s text to place on Panel
   * @param spoint position of text on Panel
   * @param sfont font to use for text display
   */
  public void addString(String s, Point spoint, Font sfont) {
    textStrings.add(s);
    textPositions.add(spoint);
    textFonts.add(sfont);
  }

  /**
   * method to remove all buttons and text from Panel
   */
  public void clearAll() {
    buttonImages.removeAllElements();
    buttonActions.removeAllElements();
    buttonPositions.removeAllElements();
    textStrings.removeAllElements();
    textPositions.removeAllElements();
    textFonts.removeAllElements();
    this.repaint();
  }

  public synchronized void addFgiPanelListener(BasicPanelListener l) {
    fgiPanelListener = BasicPanelEventMulticaster.add(fgiPanelListener, l);
  }

  public synchronized void removeFgiPanelListener(BasicPanelListener l) {
    fgiPanelListener = BasicPanelEventMulticaster.remove(fgiPanelListener, l);
  }

  protected void processFgiPanelEvent(BasicPanelEvent e) {
    if (fgiPanelListener != null) {
      fgiPanelListener.fgiPanelTriggered(e);
    }
  }

  protected void fireFgiPanelEvent(String s) {
    BasicPanelEvent e = new BasicPanelEvent(this);
    e.setActionCommand(s);
    processFgiPanelEvent(e);
  }

  // Response to Mouse clicks by check each button in Panel
  public void mouseClicked(MouseEvent e) {
    for (int i=0; i<buttonImages.size(); i++) {
      Image img = (Image)buttonImages.elementAt(i);
      Rectangle r = new Rectangle((Point)buttonPositions.elementAt(i),
                                    new Dimension(img.getWidth(this), img.getHeight(this)));
      if (r.contains(e.getX(), e.getY())) {
        fireFgiPanelEvent((String)buttonActions.elementAt(i));
      }
    }
  }

  // Required by MouseListener Interface
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
  public void mousePressed(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}

  /*
   * Method required by ActionListener interface
   */
  public void actionPerformed(ActionEvent e) {
    // reactivate event sent by contract editor frame
//    if (e.getActionCommand().equals(ContractEditorFrame.REACTIVATE_COMMAND)) {
//      activateInactiveComponents();
//    }
  }

  // These functions  set up a list of unused components -- meaning
  // the attributes represented by these components aren't used,
  // so the user shouldn't be able to change them.
  // However, circumstances could arise on the importing of contracts
  // where bad values are given for these, and therefore the contracts
  // couldn't be activated
  // In that case, an action event with command of "REACTIVATE" would
  // occur, and the components would be reactivated


  /*
   * Method to add a component to the vector of the unused-attribute
   * component list so that these things can be reactivated if necessary
   * Will also turn off the component, i.e. call setEnabled(false)
   */
  public void addUnusedComponent(Component comp) {
    comp.setEnabled(false);
    unusedComponents.add(comp);
  }

  /*
   * Method to remove a component from the vector of the unused-attribute
   * component list -- probably won't be used
   * This method does not do anything to the enabled state of the
   * component
   */
  protected void removeUnusedComponent(Component comp) {
    unusedComponents.remove(comp);
  }


  /*
   * Method to clear all the components from the unused list
   * in one easy step
   * This method does not do anything to the enabled state of the
   * component
   */
  protected void clearUnusedComponents() {
    unusedComponents.clear();
  }


  /*
   * Method to activate the components on the vector of the used-attribute
   * component list
   */
  protected void activateInactiveComponents() {

    Enumeration enu = unusedComponents.elements();
    while (enu.hasMoreElements()) {
      ((Component) enu.nextElement()).setEnabled(true);
    }

  }

  protected void deactivateInactiveComponents() {

    Enumeration enu = unusedComponents.elements();
    while (enu.hasMoreElements()) {
      ((Component) enu.nextElement()).setEnabled(false);
    }

  }

  /*
   * Method to retrieve the value of the fgiServletProperty.ini
   * property value about disabling unused attribute fields
   * Talks to the servlet to get the value
   * @return boolean value of variables
   */
   /*
  public boolean getDisableUnused() {

    String servletURL = ContractEditorFrame.theApplet.getDocumentBase() + "ContractServlet";

    AppletToServletChannel ch = new AppletToServletChannel(servletURL);
    ch.setCookie(ContractEditorFrame.theApplet.getParameter("sessionCookie"));

    AppletToServletParcel box = new AppletToServletParcel("GETDISABLEUNUSED", null);

    String ret = "true";
    try {
      box = (AppletToServletParcel)(ch.objectRequest("?command=GETDISABLEUNUSED", box)).readObject();
      ret = (String) box.parcel;
    }
    catch (Exception b_s) {
      System.out.println("FgiPanel::getDisableUnused:  ERROR--" + b_s);
    }

    Boolean returnvalue = new Boolean(ret);
    return returnvalue.booleanValue();
  }*/


  /*
   * Method to retrieve the UseCostRecovery parameter from the servlet
   * which gets it from an ini file
   * @return boolean value for useCostRecover
   */
   /*
  public boolean getUseCostRecovery() {


    String servletURL = ContractEditorFrame.theApplet.getDocumentBase() + "ContractServlet";

    AppletToServletChannel ch = new AppletToServletChannel(servletURL);
    ch.setCookie(ContractEditorFrame.theApplet.getParameter("sessionCookie"));

    AppletToServletParcel box = new AppletToServletParcel("GETUSECOSTRECOVERY", null);

    String ret = "true";
    try {
      box = (AppletToServletParcel)(ch.objectRequest("?command=GETUSECOSTRECOVERY", box)).readObject();
      ret = (String) box.parcel;
    }
    catch (Exception b_s) {
      System.out.println("ContractItemPane::getUseCostRecovery:  ERROR--" + b_s);
    }

    Boolean returnvalue = new Boolean(ret);
    return returnvalue.booleanValue();
  }*/


/*
  public boolean validatePanelData() {
    return true;
  }

  protected JComboBox getCodeComboBox(Vector codeVector, String codename, String codeValue) {
    JComboBox[] newComboBox;
    String[] str = new String[1];
    str[0] = codeValue;
    newComboBox = getCodeComboBox(codeVector, codename, str);
    return newComboBox[0];
  }

  protected JComboBox[] getCodeComboBox(Vector codeVector, String codename, String codeValue[]) {

    Vector codeStrings = new Vector();
    int numCodes = codeValue.length;
    JComboBox[] codeComboBox = new JComboBox[numCodes];
    int[] selectedIndex = new int[numCodes];

    for (int i=0; i<numCodes; i++)
      selectedIndex[i] = -1;

    int index = 0;
    if (codeVector != null) {
      for (int j=0; j<codeVector.size(); j++) {
        Code code = (Code)codeVector.elementAt(j);
        if (code.getCodeName().equals(codename)) {
          String codeString = code.getDisplayableString();
          if (codeString.length() > 30)
            codeString = codeString.substring(0, 30);
          codeStrings.addElement(codeString);
          for (int k=0; k<numCodes; k++) {
            if (codeValue[k] != null) {
              if (codeValue[k].equals(code.getCodeValue()))
                selectedIndex[k] = index;
            }
          }
          index++;
        }
      }
    }
    if (codeStrings.size() == 0) {
      codeStrings.addElement("NO CODES AVAILABLE");
      for (int i=0; i<numCodes; i++) {
        codeComboBox[i] = new JComboBox(codeStrings);
        codeComboBox[i].setFont(standardFont);
        codeComboBox[i].setForeground(black);
        codeComboBox[i].setBackground(isiDarkBlue);
      }
      return codeComboBox;
    }

    for (int i=0; i<numCodes; i++) {
      codeComboBox[i] = new JComboBox(codeStrings);
      if (selectedIndex[i] == -1) {
        codeComboBox[i].insertItemAt("UNSPECIFIED", 0);
        selectedIndex[i] = 0;
      }
      codeComboBox[i].setFont(standardFont);
      codeComboBox[i].setForeground(black);
      codeComboBox[i].setBackground(isiDarkBlue);
      if (selectedIndex[i] > -1)
        codeComboBox[i].setSelectedIndex(selectedIndex[i]);
    }
    return codeComboBox;
  }

  protected Vector getCodesFromDatabase(String command) {

    // command should be of the form : "?table=LTA&code= . . ."

    String servletURL = ContractEditorFrame.theApplet.getDocumentBase() + "CodeServlet";

    AppletToServletChannel ch = new AppletToServletChannel(servletURL);
    ch.setCookie(ContractEditorFrame.theApplet.getParameter("sessionCookie"));

    AppletToServletParcel box = new AppletToServletParcel();

    Vector ret = null;
    try {
      box = (AppletToServletParcel)(ch.objectRequest(command, box)).readObject();
      ret = (Vector) box.parcel;
    }
    catch (Exception b_s) {
      System.out.println("GenericItemEditPanel::getCodesFromDatabase:  ERROR--" + b_s);
    }
    return ret;
  }

  protected Vector getCodesFromDatabase(Vector v, String command) {

    // command should be of the form : "?table=LTA&code= . . ."

    String servletURL = ContractEditorFrame.theApplet.getDocumentBase() + "CodeServlet";

    AppletToServletChannel ch = new AppletToServletChannel(servletURL);
    ch.setCookie(ContractEditorFrame.theApplet.getParameter("sessionCookie"));

    AppletToServletParcel box = new AppletToServletParcel();

    Vector ret = null;
    try {
      box = (AppletToServletParcel)(ch.objectRequest(command, box)).readObject();
      ret = (Vector) box.parcel;
    }
    catch (Exception b_s) {
      System.out.println("GenericItemEditPanel::getCodesFromDatabase:  ERROR--" + b_s);
    }
    if (ret != null) {
      for (int i=0; i<ret.size(); i++)
        v.add(ret.elementAt(i));
    }
    return v;
  }*/

  /*
   * Method to Add Components to list of those that must be validated for this panel
   * (used by LTA Activation Code)
   * @param component to add to list of those that require validation
   */
   /*
  public void addValidationComponent(JComponent component) {
    editableComponents.addElement(component);
  }*/

  /*
   * Method to check that all components in this panel that require validation are in
   * fact valid.  Used by LTA activation code.
   * @return boolean indicator of whether ALL components are valid
   */
   /*
  public boolean validateComponents() {
    for (int i=0; i<editableComponents.size(); i++) {
      EditComponent component = (EditComponent)editableComponents.elementAt(i);
      if (!component.isValid())
        return false;
    }
    return true;
  }*/

}
