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

package org.cougaar.delta.util.variant;

import java.lang.reflect.*;
import java.util.*;

/**
 *  DataWrapper is a class designed to encapsulate java beans, providing
 *  access to their properties regardless of the underlying data class.
 *  @author ALPINE (alpine-software@bbn.com)
 */
public class DataWrapper {
  private Object dataBean = null;
  private Class dataClass = null;

  /**
   *  Wrap the given java bean within this DataWrapper
   *  @param data the content bean
   */
  public DataWrapper (Object data) {
    dataBean = data;
    dataClass = dataBean.getClass();
  }

  /**
   *  Retrieve the underlying bean wrapped by this DataWrapper
   *  @return the content bean
   */
  public Object unwrap () {
    return dataBean;
  }

  /**
   *  Access the properties of the content bean by name.  The result, being
   *  whatever class it is, is wrapped with a Variant implementor for purposes
   *  of general comparison algorithms and string conversions.
   *  @param p the name of the property to be inspected
   *  @return the value of the property p as a Variant
   */
  public Variant getProperty (String p) {
    try {
      if (p == null || p.length() == 0)
        throw new IllegalArgumentException(
          "DataWrapper::getProperty:  invalid property name");

      String suffix = p.substring(0,1).toUpperCase() + p.substring(1);
      Method getter = null;
      // see if "get" method exists
      try {
        getter = dataClass.getMethod("get" + suffix, new Class[0]);
      }
      catch (Exception e) {
        // if no "get" method can be found, maybe it's because the property is
        // boolean.  Look for an "is" method . . .
        try {
          getter = dataClass.getMethod("is" + suffix, new Class[0]);
        }
        catch (Exception e2) {
          throw new Exception("neither get" + suffix + " nor is" + suffix +
            " found in " + dataClass.getName());
        }
      }
      Class propertyClass = getter.getReturnType();
      Object value = getter.invoke(dataBean, new Object[0]);
      return makeVariant(value, propertyClass);
    }
    catch (InvocationTargetException ite) {
      System.out.println("DataWrapper::getProperty:  Target Error--" + ite.getTargetException());
      return null;
    }
    catch (Exception oh_no) {
      System.out.println("DataWrapper::getProperty:  Error--" + oh_no);
      return null;
    }
  }

  /**
   *  Set a named property of the data Object wrapped by this DataWrapper to
   *  a value specified by the caller.  This is accomplished by attempting to
   *  find a "setter" method on the Object with a parameter type matching the
   *  supplied value.  If this fails, then <i>any</i> one-arg "setter" is
   *  sought, and an attempt is made to convert the supplied value
   *  to something compatible with the "setter" method found.  In the latter
   *  case, if multiple one-arg setter methods for the given property are
   *  present, then this algorithm selects the one it discovers first, which
   *  may (non-deterministically) be the wrong one.
   *  @param p the String name of the property being set
   *  @param v the Variant containing the value to be stored as a property
   *    of the wrapped Object
   */
  public void setProperty (String p, Variant v) {
    try {
      String setSuffix =
        "set" + p.substring(0,1).toUpperCase() + p.substring(1);
      Class propertyClass = v.getValue().getClass();
      Method setter = null;
      try {
        setter = dataClass.getDeclaredMethod(setSuffix, new Class[] {propertyClass});
      }
      catch (NoSuchMethodException snme) {
        Method[] methods = dataClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
          if (methods[i].getName().equals(setSuffix) &&
              methods[i].getParameterTypes().length == 1)
          {
            setter = methods[i];
            break;
          }
        }
        if (setter == null)
          throw new Exception(
            "method " + setSuffix + " not found in " + dataClass.getName());
        propertyClass = setter.getParameterTypes()[0];
      }
      Object[] setterParam = {unmakeVariant(v, propertyClass)};
      setter.invoke(dataBean, setterParam);
    }
    catch (Exception oh_no) {
      System.out.println("DataWrapper::setProperty:  Error--" + oh_no);
      oh_no.printStackTrace();
    }
  }

  // Convert what we're handed into a compatible type, if possible
  private static Object unmakeVariant (Variant v, Class c) throws Exception
  {
    Object val = v.getValue();
    String cName = c.getName();
    if ((val instanceof Boolean) && (cName.equals("boolean") || cName.equals("java.lang.Boolean")))
      return val;

    if ((val instanceof String) && (cName.equals("boolean") || cName.equals("java.lang.Boolean")))
      return new Boolean((String) val);

    if ((val instanceof String) && (cName.equals("char") || cName.equals("java.lang.Character")))
    {
      if (val == null || ((String) val).length() == 0)
        return new Character((char) 0);
      else
        return new Character(((String) val).charAt(0));
    }

    if ((val instanceof String) && cName.equals("java.lang.String"))
      return val;

    if ((val instanceof String) && (cName.equals("int") || cName.equals("java.lang.Integer")))
      return new Integer((String) val);

    if ((val instanceof String) && (cName.equals("long") || cName.equals("java.lang.Long")))
      return new Long((String) val);

    if ((val instanceof String) && (cName.equals("float") || cName.equals("java.lang.Float")))
      return new Float((String) val);

    if ((val instanceof String) && (cName.equals("double") || cName.equals("java.lang.Double")))
      return new Double((String) val);

    if ((val instanceof String) && cName.equals("java.util.GregorianCalendar"))
      throw new Exception("GregorianCalendar not supported in DataWrapper::setProperty");

    return val;
  }

  // Convert int, float, String, etc. to an appropriate Variant type
  private static Variant makeVariant (Object v, Class c)
  {
    if (c == null || v == null)
      return null;

    String className = c.getName();
    if (className.equals("java.lang.String")) {
      // v ought to be of type String
      return new VariantText((String) v);
    }
    else if (className.equals("int")) {
      // v ought to be of type Integer
      return new VariantInt(((Integer) v).intValue());
    }
    else if (className.equals("long")) {
      // v ought to be of type Long
      return new VariantLong(((Long) v).longValue());
    }
    else if (className.equals("float")) {
      // v ought to be of type Float
      return new VariantFloat(((Float) v).floatValue());
    }
    else if (className.equals("double")) {
      // v ought to be of type Double
      return new VariantDouble(((Double) v).doubleValue());
    }
    else if (className.equals("boolean")) {
      // v ought to be of type Boolean
      return new VariantBoolean(((Boolean) v).booleanValue());
    }
    else if (className.equals("char")) {
      // v ought to be of type Character
      return new VariantChar(((Character) v).charValue());
    }
    else if (className.equals("java.lang.Character")) {
      // v ought to be of type Character
      return new VariantChar(((Character) v).charValue());
    }
    else if (className.equals("java.util.Date")) {
      return new VariantDate((Date) v);
    }
    else if (className.equals("java.util.GregorianCalendar")) {
      // v ought to be of type GregorianCalendar
      GregorianCalendar d = (GregorianCalendar) v;
      return new VariantCalendar(d.YEAR, d.MONTH, d.DAY_OF_MONTH);
    }
    else {
      return new VariantObject(v);
    }
  }
}
