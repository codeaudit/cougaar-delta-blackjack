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
package org.cougaar.delta.applet.event;

import java.awt.*;

/**
 * Event generated by a FgiPanel object to signify a button-click/action by the user.
 */
public class BasicPanelEvent extends AWTEvent {
  public static final int FGIPANEL_FIRST = AWTEvent.RESERVED_ID_MAX + 1;
  public static final int FGIPANEL_LAST = FGIPANEL_FIRST;
  public static final int FGIPANEL_TRIGGERED = FGIPANEL_FIRST;

  private String actionCommand = null;

  public BasicPanelEvent(Object source) {
    super(source, FGIPANEL_TRIGGERED);
  }

  public void setActionCommand(String s) {
    actionCommand = s;
  }

  public String getActionCommand() {
    return actionCommand;
  }
}
