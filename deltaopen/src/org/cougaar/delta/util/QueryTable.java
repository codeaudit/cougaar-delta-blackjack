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

package org.cougaar.delta.util;

import java.util.*;

/**
 *  A QueryTable is a key-value mapping that can be used, by a factory class, for example,
 *  to contain the parameters in a database query.  At the time of construction,
 *  each instance is endowed with a finite set of permissible keys to which
 *  values may be assigned.  Any attempt to set, retrieve, or remove values for
 *  other key values will result in an Exception being generated.  Aside from
 *  this restriction, it behaves, in most important respects, like a Hashtable.
 *  <br><br>
 *  Anyone working with instances of this class should be aware that some of
 *  the methods of interface Map are not implemented, particularly keySet,
 *  values, and entrySet.  Calling one of these methods causes an Error.
 */
public class QueryTable implements Map {
  private Object validityMarker = null;
  private Hashtable table = new Hashtable();
  private int n_entries = 0;

  private static class TableEntry {
    public Object value = null;
  }

  public QueryTable (Object m, Vector keys) {
    if (keys == null)
      throw new NullPointerException("Key set was null");

    validityMarker = m;
    Enumeration keyVals = keys.elements();
    while (keyVals.hasMoreElements())
      table.put(keyVals.nextElement(), new TableEntry());
  }

  public QueryTable (Object m, Object[] keys) {
    if (keys == null)
      throw new NullPointerException("Key set was null");

    validityMarker = m;
    for (int i = 0; i < keys.length; i++)
      table.put(keys[i], new TableEntry());
  }

  public boolean verifyMarker (Object m) {
    return validityMarker == m;
  }

  // - - - - - - - Map Interface Implementation - - - - - - - - - - - - - - - -
  /**
   *  Returns the number of key-value pairs present in the table.
   *  @return the number of key-value pairs
   */
  public int size () {
    return n_entries;
  }

  /**
   *  Returns <tt>true</tt> if this map contains no key-value mappings.
   *  @return <tt>true</tt> if this map contains no key-value mappings.
   */
  public boolean isEmpty () {
    return n_entries == 0;
  }

  /**
   *  Returns <tt>true</tt> if this table contains the given object as one of
   *  its keys.
   *
   *  @param key key whose presence in this map is to be tested.
   *  @return <tt>true</tt> if this table has the given key.
   *
   *  @throws NullPointerException if the key is <tt>null</tt>
   */
  public boolean containsKey (Object key) {
    if (key == null)
      throw new NullPointerException();
    TableEntry entry = (TableEntry) table.get(key);
    return entry != null && entry.value != null;
  }

  /**
   *  Tell whether the given value is one of those in this table.  The equals
   *  method is used for comparisons.
   *
   *  @param value value whose presence in this map is to be tested.
   *  @return <tt>true</tt> if this map maps one or more keys to the specified value.
   */
  public boolean containsValue (Object value) {
    Enumeration enu = table.elements();
    while (enu.hasMoreElements()) {
      TableEntry entry = (TableEntry) enu.nextElement();
      if (value.equals(entry.value))
        return true;
    }
    return false;
  }

  /**
   *  Look up the given key in the table and report the corresponding value.
   *  If that key is not among those supported, then an Exception is generated.
   *
   *  @param k the key to look up
   *  @return the value corresponding to key k
   *  @throws IllegalArgumentException if the key is not one of those supported.
   */
  public Object get (Object k) {
    TableEntry entry = (TableEntry) table.get(k);
    if (entry == null)
      throw new IllegalArgumentException("Key \"" + k.toString() +
        "\" is not supported in this query!");

    return entry.value;
  }

  /**
   *  Insert a value into the table, to be indexed by the key provided.  If
   *  the key previously had a value, that value is lost.  If the key is not
   *  supported by this table, then an Exception is generated.
   *  @param key the indexing key for the given value
   *  @param value the value being added to the table
   *  @return the value being inserted
   *  @throws IllegalArgumentException if the key is not among those supported
   *  @throws NullPointerException if the key or the value is null;
   */
  public Object put (Object key, Object value) {
    if (key == null)
      throw new NullPointerException("Key was null");
    if (value == null)
      throw new NullPointerException("Value was null");
    TableEntry entry = (TableEntry) table.get(key);
    if (entry == null)
      throw new IllegalArgumentException("Key \"" + key.toString() +
        "\" is not supported in this query!");

    if (entry.value == null)
      n_entries ++;

    entry.value = value;
    return value;
  }

  /**
   *  Remove an indexed value from the table, if it exists.  If the specified
   *  key is not one of those supported by this table, then an Exception is
   *  generated.
   *
   *  @param key key whose corresponding value is to be removed.
   *  @return the value that was associated with the key, if any, or else null
   *  @throws IllegalArgumentException if the key is not among those supported
   */
  public Object remove (Object key) {
    TableEntry entry = (TableEntry) table.get(key);
    if (entry == null)
      throw new IllegalArgumentException("Key \"" + key.toString() +
        "\" is not supported in this query!");

    Object val = entry.value;
    if (val != null)
      n_entries --;

    entry.value = null;
    return val;
  }

  /**
   *  Copies all of the key-value pairs from the provided Map into this table.
   */
  public void putAll (Map t) {
    Iterator keys = t.keySet().iterator();
    while (keys.hasNext()) {
      Object key = keys.next();
      Object value = t.get(key);
      put(key, value);
    }
  }

  /**
   *  Clears the contents of this table.
   */
  public void clear () {
    Enumeration enu = table.elements();
    while (enu.hasMoreElements()) {
      TableEntry entry = (TableEntry) enu.nextElement();
      entry.value = null;
    }
    n_entries = 0;
  }

  /**
   *  This method is not supported.  If someone tries to call it,
   *  a runtime Error is generated
   *  @throws Error every time it is called
   */
  public Set keySet () {
    throw new Error("QueryTable::keySet is not supported.");
  }

  /**
   *  This method is not supported.  If someone tries to
   *  call it, a runtime Error is generated.
   *  @throws Error every time it is called
   */
  public Collection values () {
    throw new Error("QueryTable::values is not supported.");
  }

  /**
   *  This method is not supported.  If someone tries to
   *  call it, a runtime Error is generated.
   *  @throws Error every time it is called
   */
  public Set entrySet () {
    throw new Error("QueryTable::entrySet is not supported.");
  }
}
