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
import java.util.*;

public class BasicPanelEventMulticaster extends AWTEventMulticaster implements BasicPanelListener {

  protected BasicPanelEventMulticaster(EventListener a, EventListener b) {
    super(a, b);
  }

  public static BasicPanelListener add(BasicPanelListener a, BasicPanelListener b) {
    return (BasicPanelListener) addInternal(a, b);
  }

  public static BasicPanelListener remove(BasicPanelListener l, BasicPanelListener oldl) {
    return (BasicPanelListener) removeInternal(l, oldl);
  }

  protected static EventListener addInternal(EventListener a, EventListener b) {
    if (a==null)
      return b;
    if (b==null)
      return a;
    return new BasicPanelEventMulticaster(a, b);
  }

  protected EventListener remove(EventListener oldl) {
    if (oldl == a)
      return b;
    if (oldl == b)
      return a;
    EventListener a2 = removeInternal(a, oldl);
    EventListener b2 = removeInternal(b, oldl);
    if (a2 == a && b2 == b)
      return this;
    return addInternal(a2, b2);
  }

  public void fgiPanelTriggered(BasicPanelEvent e) {
    if (a != null) ((BasicPanelListener) a).fgiPanelTriggered(e);
    if (b != null) ((BasicPanelListener) b).fgiPanelTriggered(e);
  }
}
