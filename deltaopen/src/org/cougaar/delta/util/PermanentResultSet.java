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

import java.sql.*;
import java.util.*;
import java.io.*;
import java.math.*;

/**
 * Wrapper for ResultSet
 * @author ALPINE (alpine-software@bbn.com)
 */

public class PermanentResultSet implements ResultSet {

  private int rowPointer = 0;
  private int columns;
  private Vector rows;
  private Vector columnNames;
  private boolean wasNull = false;

  public PermanentResultSet(ResultSet rs) {
    try {
      ResultSetMetaData rsmd = rs.getMetaData();
      columns = rsmd.getColumnCount();
      columnNames = new Vector(columns);
      rows = new Vector();
      for (int i=0; i<columns; i++) {
        columnNames.addElement(rsmd.getColumnName(i+1));
      }
      while (rs.next()) {
        Vector row = new Vector(columns);
        for (int i=0; i<columns; i++)
          row.addElement(rs.getObject(i+1));
        rows.addElement(row);
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean wasNull() {
    return wasNull;
  }

  public boolean next() {
    rowPointer++;
    if (isAfterLast())
      return false;
    return true;
  }

  public boolean previous() {
    rowPointer--;
    if (isBeforeFirst())
      return false;
    return true;
  }

  public boolean absolute(int row) {
    rowPointer = row;
    if (isBeforeFirst()) {
      rowPointer = 0;
      return false;
    }
    else if (isAfterLast()) {
      rowPointer = rows.size()+1;
      return false;
    }
    return true;
  }

  public boolean relative(int rowsToMove) {
    rowPointer += rowsToMove;
    if (isBeforeFirst()) {
      rowPointer = 0;
      return false;
    }
    else if (isAfterLast()) {
      rowPointer = rows.size()+1;
      return false;
    }
    return true;
  }

  public void afterLast() {
    rowPointer = rows.size()+1;
  }

  public void beforeFirst() {
    rowPointer = 0;
  }

  public boolean first() {
    if (rows.size() > 0)
      rowPointer = 1;
    else
      rowPointer = 0;
    return (rowPointer == 1);
  }

  public boolean isAfterLast() {
    return (rowPointer > rows.size());
  }

  public boolean isBeforeFirst() {
    return (rowPointer <= 0);
  }

  public boolean isFirst() {
    return (rowPointer == 1);
  }

  public boolean isLast() {
    return (rowPointer == rows.size());
  }

  public boolean last() {
    rowPointer = rows.size();
    return (rowPointer != 0);
  }

  public int findColumn(String colName) {
    return columnNames.indexOf(colName.toUpperCase());
  }

  public BigDecimal getBigDecimal(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    wasNull = false;
    if (obj == null)
      wasNull = true;
    return (BigDecimal)obj;
  }

  public BigDecimal getBigDecimal(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        BigDecimal bd = getBigDecimal(c+1);
        return bd;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public boolean getBoolean(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    if (obj == null) {
      wasNull = true;
      return false;
    }
    wasNull = false;
    Boolean b = (Boolean)obj;
    return b.booleanValue();
  }

  public boolean getBoolean(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        boolean b = getBoolean(c+1);
        return b;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public java.sql.Date getDate(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    if (obj == null) {
      wasNull = true;
      return (java.sql.Date)obj;
    }
    wasNull = false;
    if (obj instanceof java.sql.Timestamp) {
      java.sql.Timestamp ts = (Timestamp)obj;
      java.sql.Date date = new java.sql.Date(ts.getTime());
      return date;
    }
    return (java.sql.Date)obj;
  }

  public java.sql.Date getDate(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        java.sql.Date date = getDate(c+1);
        return date;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public java.sql.Time getTime(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    wasNull = false;
    if (obj == null)
      wasNull = true;
    return (java.sql.Time)obj;
  }

  public java.sql.Time getTime(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        java.sql.Time time = getTime(c+1);
        return time;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public java.sql.Timestamp getTimestamp(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    wasNull = false;
    if (obj == null)
      wasNull = true;
    return (java.sql.Timestamp)obj;
  }

  public java.sql.Timestamp getTimestamp(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        java.sql.Timestamp ts = getTimestamp(c+1);
        return ts;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public double getDouble(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    if (obj == null) {
      wasNull = true;
      return 0.0;
    }
    wasNull = false;
    if (obj instanceof BigDecimal) {
      BigDecimal bd = (BigDecimal)obj;
      return bd.doubleValue();
    }
    Double d = (Double)obj;
    return d.doubleValue();
  }

  public double getDouble(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        double d = getDouble(c+1);
        return d;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public float getFloat(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    if (obj == null) {
      wasNull = true;
      return 0.0f;
    }
    wasNull = false;
    if (obj instanceof BigDecimal) {
      BigDecimal bd = (BigDecimal)obj;
      return bd.floatValue();
    }
    Float f = (Float)obj;
    return f.floatValue();
  }

  public float getFloat(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        float f = getFloat(c+1);
        return f;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public int getInt(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    if (obj == null) {
      wasNull = true;
      return 0;
    }
    wasNull = false;
    if (obj instanceof BigDecimal) {
      BigDecimal bd = (BigDecimal)obj;
      return bd.intValue();
    }
    Integer i = (Integer)obj;
    return i.intValue();
  }

  public int getInt(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        int i = getInt(c+1);
        return i;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public long getLong(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    if (obj == null) {
      wasNull = true;
      return 0;
    }
    wasNull = false;
    if (obj instanceof BigDecimal) {
      BigDecimal bd = (BigDecimal)obj;
      return bd.longValue();
    }
    Long l = (Long)obj;
    return l.longValue();
  }

  public long getLong(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        long l = getLong(c+1);
        return l;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public short getShort(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    if (obj == null) {
      wasNull = true;
      return 0;
    }
    wasNull = false;
    if (obj instanceof BigDecimal) {
      BigDecimal bd = (BigDecimal)obj;
      return bd.shortValue();
    }
    Short s = (Short)obj;
    return s.shortValue();
  }

  public short getShort(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        short s = getShort(c+1);
        return s;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public Object getObject(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    wasNull = false;
    if (obj == null)
      wasNull = true;
    return obj;
  }

  public Object getObject(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        Object o = getObject(c+1);
        return o;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public String getString(int colIndex) throws SQLException {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    if (obj == null) {
      wasNull = true;
      return (String)obj;
    }
    wasNull = false;
    return obj.toString();
  }

  public String getString(String colName) throws SQLException {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        String s = getString(c+1);
        return s;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }

  public int getRow() {
    return rowPointer;
  }

  public void cancelRowUpdates() throws SQLException {throw new SQLException("Updates not permitted");}
  public void clearWarnings() throws SQLException {}
  public void close() throws SQLException {}
  public void deleteRow() throws SQLException {throw new SQLException("Updates not permitted");}
  public Array getArray(int i) throws SQLException {throw new SQLException("Method not supported");}
  public Array getArray(String colName) throws SQLException {throw new SQLException("Method not supported");}
  public InputStream getAsciiStream(int i) throws SQLException {throw new SQLException("Method not supported");}
  public InputStream getAsciiStream(String colName) throws SQLException {throw new SQLException("Method not supported");}
  public InputStream getBinaryStream(int i) throws SQLException {throw new SQLException("Method not supported");}
  public InputStream getBinaryStream(String colName) throws SQLException {throw new SQLException("Method not supported");}
  public Blob getBlob(int colIndex) throws SQLException
  {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    wasNull = false;
    byte[] ret = null;
    if (obj == null)
      wasNull = true;
    return (Blob)obj;
  }
  public Blob getBlob(String colName) throws SQLException
  {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        Blob b = getBlob(c+1);
        return b;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }
  public byte getByte(int i) throws SQLException {throw new SQLException("Method not supported");}
  public byte getByte(String colName) throws SQLException {throw new SQLException("Method not supported");}
  public byte[] getBytes(int colIndex) throws SQLException
  {
    if (colIndex < 1 || colIndex > columns) throw new SQLException("Invalid Column Index");
    Vector row = (Vector)rows.elementAt(rowPointer-1);
    Object obj = row.elementAt(colIndex-1);
    wasNull = false;
    if (obj == null)
      wasNull = true;
    return (byte[])obj;
  }
  public byte[] getBytes(String colName) throws SQLException
  {
    int c = findColumn(colName);
    if (c >= 0) {
      try {
        byte[] b = getBytes(c+1);
        return b;
      }
      catch (SQLException e) {
        throw e;
      }
    }
    throw new SQLException("Invalid Column Name");
  }
  public Reader getCharacterStream(int i) throws SQLException {throw new SQLException("Method not supported");}
  public Reader getCharacterStream(String colName) throws SQLException {throw new SQLException("Method not supported");}
  public Clob getClob(int i) throws SQLException {throw new SQLException("Method not supported");}
  public Clob getClob(String colName) throws SQLException {throw new SQLException("Method not supported");}
  public int getConcurrency() throws SQLException {return CONCUR_READ_ONLY;}
  public String getCursorName() throws SQLException {throw new SQLException("CSB");}
  public java.sql.Date getDate(int i, Calendar cal) throws SQLException {throw new SQLException("Method not supported");}
  public java.sql.Date getDate(String s, Calendar cal) throws SQLException {throw new SQLException("Method not supported");}
  public java.sql.Time getTime(int i, Calendar cal) throws SQLException {throw new SQLException("Method not supported");}
  public java.sql.Time getTime(String s, Calendar cal) throws SQLException {throw new SQLException("Method not supported");}
  public java.sql.Timestamp getTimestamp(int i, Calendar cal) throws SQLException {throw new SQLException("Method not supported");}
  public java.sql.Timestamp getTimestamp(String s, Calendar cal) throws SQLException {throw new SQLException("Method not supported");}
  public int getFetchDirection() throws SQLException {throw new SQLException("Method not supported");}
  public int getFetchSize() throws SQLException {throw new SQLException("Method not supported");}
  public ResultSetMetaData getMetaData() throws SQLException {throw new SQLException("Method not supported");}
  public Object getObject(int i, Map m) throws SQLException {throw new SQLException("Method not supported");}
  public Object getObject(String s, Map m) throws SQLException {throw new SQLException("Method not supported");}
  public Ref getRef(int i) throws SQLException {throw new SQLException("Method not supported");}
  public Ref getRef(String s) throws SQLException {throw new SQLException("Method not supported");}
  public Statement getStatement() throws SQLException {throw new SQLException("Statement not available");}
  public int getType() throws SQLException {return TYPE_SCROLL_INSENSITIVE;}
  public InputStream getUnicodeStream(int i) throws SQLException {throw new SQLException("Method not supported");}
  public InputStream getUnicodeStream(String colName) throws SQLException {throw new SQLException("Method not supported");}
  public SQLWarning getWarnings() throws SQLException {return null;}
  public BigDecimal getBigDecimal(int i, int sc) throws SQLException {throw new SQLException("Method not supported");}
  public BigDecimal getBigDecimal(String s, int sc) throws SQLException {throw new SQLException("Method not supported");}
  public void insertRow() throws SQLException {throw new SQLException("Updates not permitted");}
  public void moveToCurrentRow() throws SQLException {throw new SQLException("Method not supported");}
  public void moveToInsertRow() throws SQLException {throw new SQLException("Method not supported");}
  public void refreshRow() throws SQLException {throw new SQLException("Method not supported");}
  public boolean rowDeleted() throws SQLException {throw new SQLException("Updates not permitted");}
  public boolean rowInserted() throws SQLException {throw new SQLException("Updates not permitted");}
  public boolean rowUpdated() throws SQLException {throw new SQLException("Updates not permitted");}
  public void setFetchDirection(int i) throws SQLException {throw new SQLException("Method not supported");}
  public void setFetchSize(int i) throws SQLException {throw new SQLException("Method not supported");}
  public void updateAsciiStream(int i, InputStream x, int l) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateAsciiStream(String s, InputStream x, int l) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateBigDecimal(int i, BigDecimal b) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateBigDecimal(String s, BigDecimal b) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateBinaryStream(int i, InputStream x, int l) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateBinaryStream(String s, InputStream x, int l) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateBoolean(int c, boolean x) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateBoolean(String s, boolean x) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateByte(int c, byte x) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateByte(String s, byte x) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateBytes(int c, byte[] x) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateBytes(String s, byte[] x) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateCharacterStream(int i, Reader x, int l) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateCharacterStream(String s, Reader x, int l) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateDate(int i, java.sql.Date d) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateDate(String s, java.sql.Date d) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateDouble(int i, double d) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateDouble(String s, double d) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateFloat(int i, float d) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateFloat(String s, float d) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateInt(int i, int in) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateInt(String s, int in) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateLong(int i, long l) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateLong(String s, long l) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateShort(int i, short s) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateShort(String s, short sh) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateNull(int i) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateNull(String s) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateObject(int i, Object o) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateObject(String s, Object o) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateObject(int i, Object o, int s) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateObject(String s, Object o, int sc) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateRow() throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateString(int i, String str) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateString(String s, String str) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateTime(int i, java.sql.Time t) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateTime(String s, java.sql.Time t) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateTimestamp(int i, java.sql.Timestamp t) throws SQLException {throw new SQLException("Updates not permitted");}
  public void updateTimestamp(String s, java.sql.Timestamp t) throws SQLException {throw new SQLException("Updates not permitted");}















}

