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
package org.cougaar.delta.util.params;


import java.util.*;




public class IniEntry {
  String name_;
  Vector values_;

  public IniEntry()
  {
    this("");
  }

  public IniEntry(String name)
  {
    setName(name);
    values_ = new Vector();
  }

  public String getName()
  {
    return name_;
  }

  public void setName(String name)
  {
    name_ = name;
  }

  public void addValue(String value)
  {
    values_.addElement(value);
  }

  public void removeValue(String value)
  {
    String s;
    int i, length = values_.size();
    for (i=0; i < length; i++)
      {
	s = (String) values_.elementAt(i);
	if (s.equals(value))
	  {
	    values_.removeElementAt(i);
	    break;
	  }
      }
  }

  public void removeAllValues()
  {
    values_.removeAllElements();
  }

  public Enumeration getValues()
  {
    return values_.elements();
  }

  public String[] getValueArray()
  {
    String[] retval = new String[values_.size()];
    return (String[])values_.toArray(retval);
  }
}
