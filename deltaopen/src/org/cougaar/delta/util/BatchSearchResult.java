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

import java.util.Hashtable;
import java.util.Vector;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import javax.servlet.http.HttpServletRequest;

import org.cougaar.delta.util.variant.DataWrapper;
import org.cougaar.delta.util.html.UniversalTable;
import org.cougaar.delta.util.Factory;

/**
 * Description: Replaces the universal table for most servlets.
 * Used by servlets and factories for database
 * queries where the results are used make a universal table
 * which is expanded as needed (ie, the universal table is
 * not necessarily completed immediately, only some of the
 * rows from the result set are looked at). This means that the
 * result set and connection used remain open to allow future
 * completion of the univeral table. Therefore, it is important
 * not to have a lot of BatchSearchResults hanging around at once.
 * Servlets may store their current BatchSearchResult into their
 * session; whenever a new servlet becomes active it will close
 * any BatchSearchResult already in the session.
 */

public class BatchSearchResult {

  private String servletTitle = "";
  private Connection conn = null;
  private Statement stmt = null;
  private ResultSet rs = null;
  private UniversalTable ut = null;
  private int searchLimit = 0;
  private String defaultSortKey = "";
  private boolean isClosed = false;

  protected BatchSearchResult(String servletTitle) {
    this.servletTitle = servletTitle;
  }

  /**
   * Closes the connection of this, if the connection
   * exists and is not already closed.
   */
  public void close() {
    try {
      if(conn != null && !conn.isClosed()) {
        if(stmt != null) {
          stmt.close();
        }
        conn.close();
      }
    }
    catch (SQLException sqle) {
      sqle.printStackTrace();
    }
    isClosed = true;
  }

  /**
   * returns true if this connection of this is
   * already closed
   */
  public boolean isClosed() {
    return isClosed;
  }

  /**
   * returns a string describing what servlet is responsible
   * for creating this
   * called by servlet
   */
  public String getServletTitle() {
    return servletTitle;
  }

  /**
   * return the result set of this
   * called by factory
   */
  protected ResultSet getResultSet() {
    return rs;
  }

  /**
   * return the universal table of this
   * called by factory & servlet
   */
  public UniversalTable getUniversalTable() {
    return ut;
  }

  /**
   * prints the html for the univeral table of
   * this into <out>
   * called by servlet
   */
  public void generateHtml(PrintWriter out) {
    ut.generateHtml(out);
  }

  /**
   * sets the column info for the universal table
   * of this
   * called by servlet
   */
  public void setColumnInfo (Hashtable[] c) {
    ut.setColumnInfo(c);
  }

  /**
   * sets the result set of this
   * called by factory
   */
  protected void setResultSet(ResultSet rs) {
    this.rs = rs;
  }
  /**
   * sets the statement of this
   * called by factory
   */
  protected void setStatement(Statement stmt) {
    this.stmt = stmt;
  }
  /**
   * sets the connection of this
   * called by factory
   */
  protected void setConnection(Connection conn) {
    this.conn = conn;
  }
  /**
   * sets the universal table of this
   * called by factory
   */
  protected void setUniversalTable(UniversalTable ut) {
    this.ut = ut;
  }
  /**
   * add rows to the universal table of this
   * called by factory
   */
  protected void addRows(DataWrapper[] newRows) {
    ut.addRows(newRows);
  }
  /**
   * update the selection of the universal table of this
   * called by servlet
   */
  public void updateSelection(HttpServletRequest request) {
    ut.updateSelection(request);
  }
  /**
   * set the universal table id of this
   * called by servlet
   */
  public void setUniversalTableId(String id) {
    ut.setUniversalTableId(id);
  }
  /**
   * set the default sort key for this. The
   * default sort key will be used to order the
   * result set (and universal table) of this
   * if no overriding sort request is made. The
   * default sort key must correspond exactly to a
   * column as mentioned in the selection query used by the
   * factory to create the result set.
   * called by servlet
   */
  public void setDefaultSortKey(String sortKey) {
    this.defaultSortKey = sortKey;
  }
  /**
   * get the default sort key for this
   * called by factory
   */
  protected String getDefaultSortKey() {
    return defaultSortKey;
  }

}
