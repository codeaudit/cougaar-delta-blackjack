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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.sql.*;
import java.lang.ref.*;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import org.cougaar.delta.util.html.*;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.delta.util.variant.DataWrapper;
import org.cougaar.delta.util.params.ParameterFileReader;
import org.cougaar.delta.util.PermanentResultSet;
import org.cougaar.delta.util.UtilString;

import org.cougaar.core.agent.ClusterServesPlugin;

/**
 * Base class for LTADB factory classes.  Holds database connection pool.
 * @author ALPINE (alpine-software@bbn.com)
 * @version $$
 */

public abstract class Factory {
  //   Below is a set of parameters that define how this Factory instance
  // accesses the database.  In the order in which they are declared, these are
  // a reference to the cluster whose Connection is to be used, a flag that
  // indicates whether or not to use a shared connection, and a flag that tells
  // if persistence is enabled.
  //   A shared connection should be used whenever persistence is enabled and
  // the Factory has a non-null clusterRef.
  private ClusterServesPlugin clusterRef = null;
  private boolean usingSharedConnection = false;
  private boolean isPersistenceEnabled = false;
  //this is the default fetch size for batch queries
  //20 matches the number of rows displayed on a servlet summary page
  protected static final int DEFAULT_FETCH_SIZE =20;

  /**
   * Set up to do testing.  Initialize and connect DB connection pool.
   * @param args Args passwd to main().  If there is an arg[0] it is used as the connection string.
   * @param factory the factory under test.
   */
  protected static void initializeTest(String [] args, Factory factory)
  {
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver");
    } catch (Exception e) {
      e.printStackTrace();
    }

    // if the connection parameters have not yet been set...
    if (dbPasswd == null) {
      factory.invokedStandalone = true;
      if (args.length > 0) {
        factory.setDbConnectionString(args[0]);
      } else {
        factory.setDbConnectionString("jdbc:oracle:thin:@delta.alpine.bbn.com:1521:fgi,aleung_aux,aleung");
      }
    }
  }

  /**
   * Set the parameter bundle to be used for the connection pool
   */
  public static void setParameters(ParameterFileReader p)
  {
    String m_JDBCDriver = p.getParameter("ConnectionPool", "jdbc.Driver", "oracle.jdbc.driver.OracleDriver");
    try {
      Class.forName(m_JDBCDriver);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // JDBC Connection URL
    Enumeration enm = p.getParameterValues("ConnectionPool", "jdbc.connect.String");
    String dbConnStr = p.concatenate(enm, ",", "jdbc:oracle:thin:@delta.alpine.bbn.com:1521:fgi,wwright,wwright");
    setDbConnectionString(dbConnStr);
  }


  /**
   * Set the reference (a ClusterServesPlugin) to the cluster in which we are running.  This
   * is required for using the shared persistence database connection.
   *
   * @param c ClusterServesPlugin that provides reference to cluster in which we are running
   */
  protected void setClusterReference(ClusterServesPlugin c) {
    clusterRef = c;
    usingSharedConnection = (isPersistenceEnabled && clusterRef != null);
  }

  /**
   * Returns the reference (a ClusterServesPlugin) to the cluster in which we are running.
   *
   * @return the ClusterServesPlugin reference to the cluster was are running within.
   */
  protected ClusterServesPlugin getClusterReference() {
    return clusterRef;
  }

  /**
   * Initialize connection pool with default values.
   */
  public Factory()
  {
    // since the data returned by this is static, it doesn't matter which Factory
    // object the listener points to.
    if (propertyChangeListeners == null)
      propertyChangeListeners = new PropertyChangeSupport(getClass());

    if (dbPasswd == null)
    {
      setDbConnectionString("jdbc:oracle:thin:@delta.alpine.bbn.com:1521:fgi,aleung_aux,aleung");
      try {
        Class.forName("oracle.jdbc.driver.OracleDriver");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    String prop = System.getProperty("org.cougaar.core.persistence.enable");
    Boolean persist = null;
    if (prop != null)
      persist = new Boolean(prop.trim());
    if (persist != null && persist.booleanValue())
      isPersistenceEnabled = true;
  }

  /**
   * Set the string to be used to connect to the database
   * @param newDbConnectionString the string to be used to connect to the database
   */
  public static synchronized void setDbConnectionString(String newDbConnectionString) {
    String  oldDbConnectionString = dbConnectionString;
    dbConnectionString = newDbConnectionString;
    StringTokenizer st = new StringTokenizer(newDbConnectionString, ",");
    if (st.hasMoreTokens())
      dbURL = st.nextToken();
    else {
      Exception e = new Exception("Missing database URL in string: " + newDbConnectionString);
      e.printStackTrace();
      return;
    }
    if (st.hasMoreTokens())
      dbUser = st.nextToken();
    else {
      Exception e = new Exception("Missing database username in string: " + newDbConnectionString);
      e.printStackTrace();
      return;
    }
    if (st.hasMoreTokens())
      dbPasswd = st.nextToken();
    else {
      Exception e = new Exception("Missing database password in string: " + newDbConnectionString);
      e.printStackTrace();
      return;
    }
    if (propertyChangeListeners != null)
      propertyChangeListeners.firePropertyChange("dbConnectionString", oldDbConnectionString, newDbConnectionString);
  }

  /**
   * Get the string to be used to connect to the database
   * @return the string to be used to connect to the database
   */
  public synchronized String getDbConnectionString() {
    return dbConnectionString;
  }

   /**
   *  Tell whether an entry with the given id exists in the database.  The
   *  implementation given below is a stub, but subclasses interested in
   *  reporting this type of information should define this method, each
   *  according to its own specific nature.
   *  @param id an identifier for objects handled by the Factory instance
   *  @return true if and only if the specified object exists
   */
  public boolean exists (String id) {
    return false;
  }

  /**
   * Modifies bsr so that html for the page of results beginning with the startDisplayRow
   * will be able to be generated
   */
  public final void preparePage(BatchSearchResult bsr, int startDisplayRow)
        throws SQLException {
    UniversalTable ut = bsr.getUniversalTable();
    int numRowsDisplayed = ut.getDisplayNumRows();
    int numInstantiatedRows = bsr.getUniversalTable().getRows().length;

    //we need to add more rows if we are trying to display a row
    //which is not instantiated yet.
    //the max display row is startDisplayRow + numRowsDisplayed
    //is the max display row larger than the numInstantiatedRows?
    //also, is the numInstantiatedRows less than the completeNumRows?
    if( (ut.isTotalNumberOfRowsKnown() &&
          startDisplayRow + numRowsDisplayed > numInstantiatedRows &&
          numInstantiatedRows < ut.getTotalNumRows()) ||
          (!ut.isTotalNumberOfRowsKnown() &&
          startDisplayRow + numRowsDisplayed > numInstantiatedRows)) {
      int newStart = Math.max(startDisplayRow, numInstantiatedRows);
      int newEnd = startDisplayRow+numRowsDisplayed;
      if(bsr.getUniversalTable().isTotalNumberOfRowsKnown())
        newEnd = Math.min(ut.getTotalNumRows(), newEnd);
      Vector newReqs = instantiate(bsr.getResultSet(), newStart+1,
        newEnd + 1);
      int numNewReqs = newReqs.size();
      DataWrapper[] additionalReqs = new DataWrapper[numNewReqs];
      for(int i = 0; i<numNewReqs; i++) {
        additionalReqs[i] = new DataWrapper(newReqs.elementAt(i));
      }
      bsr.addRows(additionalReqs);
    }

    ut.setDisplayStartRow(startDisplayRow);
  }

  /**
   * Returns a vector of objects instantiated from the rs, starting
   * with the startRow and going up to just before the endRow. This
   * is a stub and should be overridden by factories which will be used
   * to generate BatchSearchResults (ie, factories used by servlets
   * that may be doing large searches).
   */
  protected Vector instantiate(ResultSet rs, int startRow, int endRow)
          throws SQLException {
      return new Vector();
  }

  /**
   *  Construct a set of objects.  If a valid query is submitted, this method
   *  queries the database to collect a list of matching objects.  The query
   *  itself is embodied in a QueryTable which should be generated by the
   *  Factory instance to which it will be submitted.  If the table containing
   *  the query does not carry this Factory's marker, then an Exception is
   *  generated.
   *  @param q the query for data items
   *  @return a table containing the matching items
   *  @throws IllegalArgumentException if the query does not belong to this Factory
   */
  public final UniversalTable query (QueryTable q) {
    if (!q.verifyMarker(myQueryMarker))
      throw new IllegalArgumentException(
        "Invalid QueryTable submitted for query to\n  " + getClass().getName() +
        "\n  --use getQueryTable()");
    return processQuery(q);
  }
  /**
   *  Similar to query(QueryTable) except that this returns a BatchSearchResult
   *  instead of a UniversalTable.
   *  If you are a servlet and you are calling this, you should make sure to close
   *  any existing bsr which is in your session.
   *  @param q the query for data items
   *  @param title the title of the servlet requesting the object
   *  @return a BatchSearchResult containing the matching items
   *  @throws IllegalArgumentException if the query does not belong to this Factory
   */
  public final BatchSearchResult batchQuery (QueryTable q, String title) {
    if (!q.verifyMarker(myQueryMarker))
      throw new IllegalArgumentException(
        "Invalid QueryTable submitted for query to\n  " + getClass().getName() +
        "\n  --use getQueryTable()");
    return processBatchQuery(q, title);
  }
  /**
   *  Similar to batchQuery(QueryTable, String) except that the results will be sorted
   *  based on the oldBatchSearchResult and the sortKey
   *  @param q the query for data items
   *  @param title the title of the servlet requesting the object
   *  @param oldBatchSearchResult the last BatchSearchResult, used to determine
   *  whether sort should be in ascending or descending order
   *  @param sortKey the column name to be used for sorting the results, must
   *  correspond with the query used by the factory
   *  @return a BatchSearchResult containing the matching items
   *  @throws IllegalArgumentException if the query does not belong to this Factory
   */
  public final BatchSearchResult sortBatchQuery (QueryTable q, String title, BatchSearchResult
            oldBatchSearchResult, String sortKey) {
    if (!q.verifyMarker(myQueryMarker))
      throw new IllegalArgumentException(
        "Invalid QueryTable submitted for query to\n  " + getClass().getName() +
        "\n  --use getQueryTable()");
    return sortBatchResult(q, title, oldBatchSearchResult, sortKey);
  }

  /**
   *  Having verified that this query is valid, ask the subclass to provide the
   *  requested data items.
   *  @param q the Map containing the query parameters
   *  @return a UniversalTable containing the matching items
   */
// subclasses can override this, but most factories which use BatchSearchResults
//will not need to
  protected UniversalTable processQuery (Map request) {
    BatchSearchResult bsr = processBatchQuery(request, "factory");
    try {
      completeAndClose(bsr);
    }
    catch(SQLException e) {
      e.printStackTrace();
    }
    return bsr.getUniversalTable();
  }

  private void completeAndClose(BatchSearchResult bsr) throws SQLException {
    UniversalTable ut = bsr.getUniversalTable();
    if(!ut.isCompletelyInstantiated()) {
      //starts at 1, end not inclusive
      int start = ut.getRows().length + 1;
      int end = ut.getTotalNumRows() + 1;
      Vector v = instantiate(bsr.getResultSet(), start, end);
      int numNewRows = v.size();
      DataWrapper[] newRows = new DataWrapper[numNewRows];
      for(int i = 0; i<numNewRows; i++) {
        newRows[i] = new DataWrapper(v.elementAt(i));
      }
      bsr.addRows(newRows);
    }
    bsr.close();
  }

  /**
   *  Construct a query table capable of holding only the parameters relevant
   *  to this particular Factory instance.  A new table is generated every time
   *  this method is called.
   *  @return a QueryTable valid for making queries on this Factory
   */
  public QueryTable getQueryTable () {
    return new QueryTable(myQueryMarker, getParameterList());
  }

  // Each Factory instance has a marker assigned to it and referenced by all
  // valid QueryTables originating from that Factory.
  private Object myQueryMarker = new Object();

  /**
   *  Return a list of the keys accepted as query parameters in queries to this
   *  Factory instance.
   *  @return the parameter list
   */
  public abstract Object[] getParameterList ();


  /**
   * Fetch a database connection from the pool.  It must
   * be returned with close_connection when the operation is
   * finished so it can be used by others.
   *
   * @return a java.sql.Connection object.
   * @see #close_connection
   */
  protected Connection getConnection () {
    Connection conn = null;

    try {
      conn = DBConnectionPool.getConnection(dbURL, dbUser, dbPasswd);
    }
    catch (SQLException sqle) {
      sqle.printStackTrace();
    }

    return conn;
  }

  /**
   * Release the database connection so it can be used by others.
   *
   * @param connection to be released
   * @see #getConnection
   */
  protected void close_connection (Connection c) {
    // Release the connection back to the connection pool
    try {
      c.close();
    }
    catch (SQLException sqle) {
      sqle.printStackTrace();
    }
  }

  /**
   * given the sortKey from a servlet request, return
   * the name of the column to SQL sort by. This column name
   * must correspond to the column name used in the selectionString().
   * This is a stub and must be overridden by any factory that
   * makes BatchSearchResult.
   */
  protected String getSortByColumn(String sortKey) {
    return "";
  }
  /**
   * returns a String which can be used to query the DB for the
   * number of records matching the request
   * This is a stub and must be overridden by any factory that
   * makes BatchSearchResult.
   */
  protected String makeCountQuery(Map request) {
    //the String must be of the form "select count(*) as cnt from...."
    //or "select count(column) as cnt from..."
    //the result must be named "cnt"
    return "";
  }
  /**
   * returns a string which can be used to query the DB for the
   * records matching the request
   * This is a stub and must be overridden by any factory that
   * makes BatchSearchResult.
   */
  protected String makeQuery(Map request) {
    return "";
  }

  /**
   * Returns a BatchSearchResult corresponding with
   * the request with its UniversalTable rows sorted by sortKey.
   * Will be sorted in ascending order unless the oldBSR is
   * already sorted by sortKey in ascending order.
   */
  protected final BatchSearchResult sortBatchResult(Map q, String title, BatchSearchResult
        oldBatchSearchResult, String sortKey) {
    oldBatchSearchResult.close();
    String oldSortColumn = oldBatchSearchResult.getUniversalTable().getSortOnColumn();
    int oldSortDirection = oldBatchSearchResult.getUniversalTable().getSortDirection();
    String defaultSortKey = oldBatchSearchResult.getDefaultSortKey();
    return processBatchQuery(q, title, sortKey, oldSortColumn, oldSortDirection, defaultSortKey);
  }

  /**
   * return a BatchSearchResult matching the request and title
   */
  protected final BatchSearchResult processBatchQuery (Map request, String title) {
    return processBatchQuery(request, title, null, null, 0, null);
  }

  /**
   * Returns true.
   * Subclasses that want to prevent the execution of the count query
   * for searches should override this method to return false if the
   * count query is likely to be "too slow"
   */
  protected boolean shouldCalculateCount(Map request) {
    return true;
  }

  /**
   * return a BatchSearchResult matching the request, title, and sortKey given
   * that the old BatchSearchResult has oldSortColumn and oldSortDirection. If
   * sortKey is null, use default sort order.
   */
  private BatchSearchResult processBatchQuery(Map request, String title, String sortKey,
    String oldSortColumn, int oldSortDirection, String defaultSortKey) {

    BatchSearchResult bsr = new BatchSearchResult(title);

    int sortDirection = 1;
    Vector v = new Vector();
    int completeNumRows = 0;
    boolean shouldCount = shouldCalculateCount(request);
    try {
      if(shouldCount) {
        String countStr = makeCountQuery(request);
  //      System.out.println("count str " + countStr);
        completeNumRows = doCountQuery(countStr);
  //      System.out.println("count " + completeNumRows);
        String maxRows = (String) request.get("maxRows");
        if (maxRows != null && !maxRows.equals("")) {
          int setMaxRows = Integer.parseInt(maxRows);
          completeNumRows = Math.min(completeNumRows, setMaxRows);
        }
      }

      String qstr = makeQuery(request);
      if(sortKey!=null && sortKey.length()>0) {
        qstr = qstr + " order by " + getSortByColumn(sortKey);
        if(sortKey.equals(oldSortColumn) && oldSortDirection==1) {
          qstr = qstr + " DESC";
          sortDirection = -1;
        }
      }
      else if(defaultSortKey!=null && defaultSortKey.length()>0) {
        qstr = qstr + " order by " + defaultSortKey;
      }
//      System.out.println("qstr " + qstr);
      doBatchQuery(qstr, bsr);
      ResultSet rs = bsr.getResultSet();
      if(shouldCount)
        v = instantiate(rs, 1, Math.min(DEFAULT_FETCH_SIZE+1, completeNumRows+1));
      else
        v = instantiate(rs, 1, DEFAULT_FETCH_SIZE+1);
    }
    catch( Exception ex ) {
      System.err.println("XXXXXXXXX  Error in Factory::processBatchQuery");
      ex.printStackTrace();
    }

    DataWrapper[] result = new DataWrapper[v.size()];
    for (int i=0; i<v.size(); i++) {
      result[i] = new DataWrapper(v.elementAt(i));
    }

    UniversalTable ut = new UniversalTable(result);
    ut.setTotalNumRows(completeNumRows);
    if(!shouldCount) {
      ut.setNumberOfRowsIsKnown(false);
    }
    if(sortKey!=null) {
      ut.setAlreadySorted(sortKey, sortDirection);
    }

    bsr.setUniversalTable(ut);
    return bsr;
  }

  /**
   * returns the numerical result of the query.
   * Required that query is of the form specified in
   * makeCountQuery.
   */
  protected final int doCountQuery(String query) throws SQLException {
    ResultSet rs = doQuery(query, null);
    rs.next();
    return (rs.getInt("cnt"));
  }

  /**
   * Completes the bsr according to the query.
   */
  protected final void doBatchQuery(String query, BatchSearchResult bsr) throws SQLException {
    ResultSet rs = null;
    Statement st = null;
    Connection conn = null;
    try {
      conn = getConnection();
      conn.setAutoCommit(false);
      st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
          ResultSet.CONCUR_READ_ONLY);
      st.setFetchSize(DEFAULT_FETCH_SIZE);
      rs = st.executeQuery(query);
    }
    catch (SQLException e) {
        // close the Connection and Statement and try again.  The Pool should have
        // been recreated so this should be a brand new Connection and Statement.
        st.close();
        close_connection(conn);
        conn = getConnection();
        conn.setAutoCommit(false);
        st = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY);
        st.setFetchSize(DEFAULT_FETCH_SIZE);
        rs = st.executeQuery(query);
    }
    bsr.setConnection(conn);
    bsr.setStatement(st);
    bsr.setResultSet(rs);
  }

  protected ResultSet doQuery (String query, Connection c) throws SQLException {
    // The JDBC ResultSet object
    ResultSet rs = null;

    Connection conn = c;
    Statement st = null;
    try {
      if (conn == null)
        conn = getConnection();
      st = conn.createStatement();
      rs = st.executeQuery(query);
      String s = query.toUpperCase().trim();
      if (!s.startsWith("UPDATE") && !s.startsWith("INSERT") && !s.startsWith("DELETE"))
        rs = new PermanentResultSet(rs);
    }
    catch (SQLException e) {
      if (c == null) {
        // close the Connection and Statement and try again.  The Pool should have
        // been recreated so this should be a brand new Connection and Statement.
        st.close();
        close_connection(conn);
        conn = getConnection();
        st = conn.prepareStatement(query);
        rs = st.executeQuery(query);
      }
      else
      {
        throw e;
      }
    }
    finally {
      if (st != null)
        st.close();
      if (c == null && conn != null)
        close_connection(conn);
    }
    return rs;
  }

  /**
   * True iff what starts with a Y or a T
   */
  protected boolean isTrue(String what)
  {
    if (what == null)
      return false;
    String cmp = what.toUpperCase();
    return cmp.startsWith("Y") | cmp.startsWith("T");
  }

  /**
   * Takes the first character of a string.  If string is null, it returns null
   */
  protected Character string2char(String s)
  {
    if ((s == null) || (s.length() == 0))
      return null;
    return new Character(s.charAt(0));
  }

  /**
   * Build up a query string by returning either the string (in single-quotes) or
   * null (without quotes) if the string is null
   */
  protected String stringize(Object in)
  {
    return UtilString.stringize(in);

  }

  /**
   * Build up a query string by returning either the string (in single-quotes) or
   * null (without quotes) if the string is null
   */
  protected String stringize(Boolean in)
  {
    if (in == null)
      return "null";
    else
      return ( in.booleanValue() ? "'Y'" : "'N'");
  }

  /**
   * Build up a query string by returning either the string (in single-quotes) or
   * null (without quotes) if the string is null
   */
  protected String stringize(char in)
  {
    if (in == '\0')
      return "null";
    else if (in == '\'')
      return "''''";
    else
      return "'" + in + "'";
  }

  /**
   * Format a date for use in an SQL statement for a "thru" date (ie 11:59:59 PM).
   * returns a string like : "TO_DATE('01/01/2001/86300', 'dd/mm/yyyy/SSSSS')"
   * or "null" if the Date is null.
   */
  protected String endfmt(java.util.Date d)
  {
    if (d == null)
      return "null";
    if (dateFormatter == null)
      dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    return "TO_DATE('"+dateFormatter.format(d)+"/86399', 'dd/mm/yyyy/SSSSS')";
  }

  /**
   * returns true if s does not contain '*', '?', '%', or '_'
   */
   protected boolean hasNoWildcard(String s) {
    if(s.indexOf('*')==-1 && s.indexOf('?')==-1 &&
        s.indexOf('%')==-1 && s.indexOf('_')==-1)
    {
      return true;
    }
    return false;
   }

  /**
   * Format a date for use in an SQL statement.
   * returns a string like : "TO_DATE('01/01/2001', 'dd/mm/yyyy')"
   * or "null" if the Date is null.
   */
  protected String fmt(java.util.Date d)
  {
    if (d == null)
      return "null";
    if (dateFormatter == null)
      dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    return "TO_DATE('"+dateFormatter.format(d)+"', 'dd/mm/yyyy')";
  }
  private SimpleDateFormat dateFormatter = null;


  /**
   * Format a date for use in an SQL statement.
   * returns a string like : "TO_DATE('01/01/2001 23:38:59', 'dd/mm/yyyy hh24:mi:ss')"
   * or "null" if the Date is null.
   */
  protected String hiFiFmt(java.util.Date d)
  {
    if (d == null)
      return "null";
    if (hiFiDateFormatter == null)
      hiFiDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    return "TO_DATE('"+hiFiDateFormatter.format(d)+"', 'dd/mm/yyyy hh24:mi:ss')";
  }
  private SimpleDateFormat hiFiDateFormatter = null;


  /**
   * Converts from user entered search strings to the
   * oracle version.
   * replaces '*' with '%'
   * replaces '?' with '_'
   */
  protected String convertSearchString(String str) {
    String ret = str.replace('*', '%');
    ret = ret.replace('?', '_');
    return ret;
  }

  public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
    propertyChangeListeners.removePropertyChangeListener(l);
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
    propertyChangeListeners.addPropertyChangeListener(l);
  }

  /**
   * Creates an SQL UPDATE statement string.
   * @param table the database table to UPDATE.
   * @param fields the table columns to change
   * @param values the values for the columns in "fields"
   * @param whereClause the last part of the UPDATE string.  Something like
   *              "WHERE LTA_ID = 10"
   */
  protected String makeUpdateStatement(String table, Vector fields, Vector values, String whereClause)
  {
    StringBuffer ret = new StringBuffer("UPDATE ");
    ret.append(table);
    ret.append(" set ");
    int nfields = fields.size();
    for (int i=0; i<nfields; i++)
    {
      ret.append(fields.elementAt(i));
      ret.append(" = ");
      ret.append(values.elementAt(i));
      if (i < nfields-1)
        ret.append(", ");
    }
    ret.append(" ");
    ret.append(whereClause);
    return ret.toString();
  }

  /**
   * Creates an SQL INSERT statement string.
   * @param table the database table to INSERT into.
   * @param valspairs a Hashtable containing the values keyed by the field names
   */
  protected String makeInsertStatement(String table, Hashtable valspairs)
  {
    Vector fields = new Vector();
    Vector values = new Vector();
    extractHashtable(valspairs, fields, values);
    return makeInsertStatement(table, fields, values);
  }

  /**
   * Creates an SQL UPDATE statement string.
   * @param table the database table to UPDATE.
   * @param valspairs a Hashtable containing the values keyed by the field names
   * @whereClause the last part of the UPDATE string.  Something like
   *              "WHERE LTA_ID = 10"
   */
  protected String makeUpdateStatement(String table, Hashtable valspairs, String whereClause)
  {
    Vector fields = new Vector();
    Vector values = new Vector();
    extractHashtable(valspairs, fields, values);
    return makeUpdateStatement(table, fields, values, whereClause);
  }

  private void extractHashtable(Hashtable valspairs, Vector fields, Vector values)
  {
    Enumeration e = valspairs.keys();
    while (e.hasMoreElements())
    {
      Object element = e.nextElement();
      fields.addElement(element);
      values.addElement(valspairs.get(element));
    }

  }
  /**
   * Creates an SQL INSERT statement string.
   * @param table the database table to INSERT into.
   * @param fields the table columns to add
   * @param values the values for the columns in "fields"
   */
  protected String makeInsertStatement(String table, Vector fields, Vector values)
  {
    StringBuffer ret = new StringBuffer("INSERT into ");
    ret.append(table);
    ret.append(" (");
    int nfields = fields.size();
    for (int i=0; i<nfields; i++)
    {
      ret.append(fields.elementAt(i));
      if (i < nfields-1)
        ret.append(", ");
    }
    ret.append(") values (");
    for (int i=0; i<nfields; i++)
    {
      ret.append(values.elementAt(i));
      if (i < nfields-1)
        ret.append(", ");
    }
    ret.append(") ");
    return ret.toString();
  }




  private boolean invokedStandalone = false;
  private static String dbConnectionString = null;
  private static transient PropertyChangeSupport propertyChangeListeners = null;
  private static String dbURL = System.getProperty("org.cougaar.core.persistence.database.url");
  private static String dbUser = System.getProperty("org.cougaar.core.persistence.database.user");
  private static String dbPasswd = System.getProperty("org.cougaar.core.persistence.database.password");



  /**
   * This class is used to give parameters to the ConnectionPool.
   * Parameters are stored in a hashtable rather than read from file.
   */
  private class DBFactoryParameters extends ParameterFileReader {

  public DBFactoryParameters()
  {
    super(null);
  }

  Hashtable hashes = new Hashtable();

  public String getParameter(String classname, String parameterName)
  {
    return (String)hashes.get(parameterName);
  }
  public int getParameter(String classname, String parameterName, int defaultVal)
  {
    int ret = defaultVal;
    String sret =  getParameter(classname, parameterName);
    if (sret != null)
    {
      ret = Integer.parseInt(sret);
    }
    return ret;
  }

  public Enumeration getParameterValues(String classname, String parameterName)
  {
    String text = getParameter(classname, parameterName);
    if (text == null)
      return new Vector().elements();
    StringTokenizer toker = new StringTokenizer(text, ",");
    Vector v = new Vector();
    while(toker.hasMoreElements())
    {
      v.addElement(toker.nextElement());
    }
    return v.elements();

  }
}

  /**
   * Print a query string to stdout.  Inserts line breaks at next whitespace
   * after width so the query can be copy/pasted into SQL*Plus
   */
  protected void printQuery(String q, int width)
  {
    int cpos = 0;
    StringCharacterIterator iter = new StringCharacterIterator(q);

    for(char c = iter.first(); c != iter.DONE; c = iter.next())
    {
      cpos++;
      if ((cpos >= width) && (Character.isWhitespace(c)))
      {
        cpos = 0;
        System.out.println(c);
      }
      else
        System.out.print(c);
    }
    System.out.println();
  }

  /**
   * This class caches LTADB objects by any type of key object.  It is
   * basically a Hashtable that can query the database for changed version
   * numbers and prunes itself of out-of-date objects.  This has now been
   * modified so that the Hashtable maintains soft references. This allows
   * the Java garbage collection process to automatically finalize and gc
   * instances that have not been recently used if heap memory is running low.
   */
  public class Cache {
    Hashtable table = new Hashtable();
    private String query_prefix = null;

    /**
     * Create a new cache to hold LTADBObjects
     */
    public Cache() {
      table = new Hashtable();
    }

    /**
     * Set the first part of the SQL query to be used to
     * retrieve the DB version number. Should be something like:
     * "Select VERSION from sometable where somekey = "
     * @param query_prefx the start of an SQL statement.
     */
    public void setQueryPrefix(String query_prefix) {
      this.query_prefix = query_prefix;
    }

    /**
     * Look for the cached LTADBObject using the key. The key is appended to the
     * queryPrefix to create an SQL statement to retrieve the current version.
     * @param key the object used to look up the LTADB object.
     * @param conn the Database Connection
     * @return the LTADB object or null if not cached or the cached version is out of date.
     */
    public DBObject check(Object key, Connection conn) {
      if (query_prefix == null)
        return null;
      return check(key, query_prefix + stringize(key), conn);
    }

    /**
     * Look for the cached LTADBObject using the key.
     * @param key the object used to look up the LTADB object.
     * @param version_query and SQL statement that returns exactly one row and
     *        one column containing the version of the object.  Something like :
     *        "Select VERSION, database_id from sometable where somekey = 'someval'"
     * @param conn the Database Connection
     * @return the LTADB object or null if not cached or the cached version is out of date.
     */
    public DBObject check(Object key, String version_query, Connection conn) {
      if ((key == null) || (version_query == null))
        return null;

      Reference ref = (Reference) table.get(key);
      if (ref == null)
        return null;

      DBObject ret = (DBObject)ref.get();
      if (ret != null) { // check version
        long current_version = -1;
        long current_id = -1;
        Connection c = null;
        if (conn == null)
          c = getConnection(); // need another connection so as not to step on the
                               // enclosing class result set
        else
          c = conn;
        Statement st = null;
        try {
          st = c.createStatement();
          ResultSet rs = st.executeQuery(version_query);
          if (rs.next()){  // This may be a new contract, so there may be no rows returned
            current_version = rs.getLong(1);
            current_id = rs.getLong(2);
          }
          st.close();
        } catch (SQLException ex) {
          ex.printStackTrace();
        } finally {
          // if we had to create the connection here...
          if (conn == null)
            close_connection(c);
        }
        if (current_version != ret.getVersion() || current_id != ret.getDatabaseId())
        {
          table.remove(key);
          ret = null;
        }
      }
      return ret;
    }

    /**
     * Look for the cached LTADBObject using the key, without checking the database for version consistency
     * @param key the object used to look up the LTADB object.
     * @return the LTADB object or null if not cached
     */
    public DBObject getWithoutCheck(Object key) {
      if (key == null)
        return null;
      else
      {
        Reference ref = (Reference) table.get(key);
        if (ref == null)
            return null;
        return (DBObject) ref.get();
      }
    }


    /**
     * Remove all entries from the cache
     * @see java.util.Hashtable#clear
     */
    public void clear() {
      table.clear();
    }

    /**
     * Get the count of the number of objects in the cache
     * @see java.util.Hashtable#size
     */
    public int size() {
      return table.size();
    }

    /**
     * Get the elements of the cache
     * @see java.util.Hashtable#elements
     */
    public Enumeration elements() {
        Enumeration enm = table.elements();
        Vector vec = new Vector();
        while (enm.hasMoreElements())
        {
            Object obj = ((Reference)enm.nextElement()).get();
            if (obj != null)
            {
                vec.addElement(obj);
            }
        }

      return vec.elements();
    }

    /**
     * Delete an item (by key) from the cache
     * @see java.util.Hashtable#remove(Object)
     */
    public DBObject remove (Object key) {
      Reference ref = (Reference) table.remove(key);
      return (ref == null) ? null : (DBObject) ref.get();
    }

    /**
     * Put an item in the cache.
     * @param key The object to be used to look up the LTADB object.
     * @param val The LTADB object to be looked up
     */
    public void store(Object key, DBObject val) {
      table.put(key, new SoftReference(val));
    }

    /**
     * Copy all of the hashtable entries into the cache.
     * @see java.util.Hashtable#putAll(Map)
     */
    public void putAll(Map other) {
      Iterator i = other.entrySet().iterator();
      while (i.hasNext())
      {
        Map.Entry e = (Map.Entry) i.next();
        Object val = e.getValue();
        if (val instanceof DBObject)
            store(e.getKey(), (DBObject) e.getValue());
      }
    }
  }
}

