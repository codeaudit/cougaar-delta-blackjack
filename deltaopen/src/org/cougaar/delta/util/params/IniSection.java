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



public class IniSection {
  String    name_;
  Hashtable entries_;

  public IniSection()
  {
    this("");
  }

  public IniSection(String name)
  {
    setName(name);
    entries_ = new Hashtable();
  }

  public void setName(String name)
  {
    name_ = name;
  }

  public String getName()
  {
    return name_;
  }

  public void addEntry(IniEntry entry)
  {
    if (!hasEntry(entry.getName()))
      entries_.put(entry.getName(), entry);
    else
      throw new IllegalArgumentException("entry '"+entry.getName()+"' already exists");
  }

  public boolean hasEntry(String entry)
  {
    if (entries_.get(entry) == null)
      return false;
    return true;
  }

  public IniEntry getEntry(String entry)
  {
    return (IniEntry) entries_.get(entry);
  }

  public Enumeration getEntries()
  {
    return entries_.elements();
  }
}
