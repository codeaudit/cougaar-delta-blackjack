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

package org.cougaar.delta.util.html;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import javax.servlet.http.*;
import org.cougaar.delta.util.variant.*;

/**
 *  Class UniversalTable represents a table of data of arbitrary type (within
 *  reason) and has the ability to generate an HTML representation of itself.
 *  <br><br>
 *  For this class to be useful, we must have the following:
 *  <ul>
 *    <li>
 *      The class used to represent data rows must adhere to the JavaBeans
 *      naming convention.
 *    </li>
 *    <li>
 *      The names of the relevant fields in this class must be known
 *    </li>
 *    <li>
 *      Formatting information, in the form of a Hashtable array, is provided
 *      by the caller
 *    </li>
 *  </ul>
 */
public class UniversalTable {
  protected DataWrapper[] rows = null;
  protected Hashtable[] columnInfo = null;

  protected int displayStartRow = 0;

  /**
   * The maximum number of rows that will be displayed on a page. Pagination is
   * supported for tables larger than this threshold. Default is 20.
   */
  protected int displayNumRows = 20;

  /**
   * The number of rows this UniversalTable would have if it were completely instantiated.
   */
  protected int totalNumRows = 0;

  /**
   *  The selection model, if any, currently set on this UniversalTable
   */
  protected UniversalTableSelect selector = null;

  /**
   *  Fetch a reference to the selection model (if any) currently in use by
   *  this UniversalTable instance.
   *  @return the {@link UniversalTableSelect} object.
   */
  public UniversalTableSelect getSelector () {
    return selector;
  }

  /**
   *  Respond to selection information that may be contained in an HTTP request
   *  @param request the HTTP request
   */
  public void updateSelection (HttpServletRequest request) {
    if (selector != null)
      selector.importRequest(request);
  }

  /**
   *  Wipe the selection clean, if a selection model is active on this table.
   */
  public void clearSelection () {
    if (selector != null)
      selector.clearSelection();
  }

  /**
   *  Query the selection model for a list of the currently selected rows
   *  @return an Enumeration of the selected rows
   */
  public Enumeration getSelections () {
    if (selector == null)
      return (new Vector()).elements();
    else
      return selector.getSelectedRows();
  }

  /**
   *  Query the selection model for the JavaScript call necessary to save
   *  selection changes into the HTML request.
   */
  public String getParameterSavingJs () {
    if (selector == null)
      return null;
    else
      return selector.getParameterSavingJs();
  }

  /**
   *  The name by which this table may identify itself to the servlet for
   *  purposes of PAGE commands originating in this table.  If null, then
   *  no name parameter is given.
   */
  protected String universalTableId = null;

  /**
   *  A hashtable linking the sorting "keys" to the sorting maps; i.e., the
   *  {@link VariantMap} instances that give order to the rows in the table
   */
  protected Hashtable sortMaps = new Hashtable();

  /**
   *  The name of the field on which comparisons are performed in sorting
   */
  protected String sortOnColumn = null;

  /**
   *  A {@link StringMap} that produces the values of a mapped attribute.  A reference
   *  to the same object is stored here for easy access by the sorting algorithm.
   *  Regardless of the types involved, comparison of {@link StringMap}ped attributes is
   *  done lexicographically.
   */
  protected VariantMap sortOnMap = null;

  /**
   *  The sense of the sorting comparisons.  A value of 1 indicates "ascending"
   *  order and a value of -1 indicates "descending" order.  The meaning of
   *  this variable is relative to the comparison algorithm being applied, and
   *  thus depends on the type of the fields being compared.
   *  @see Variant
   */
  protected int sortDirection = 1;

  /**
   *  A flag to indicate whether or not the table is already sorted.
   */
  protected boolean sorted = false;

  /**
   * A flag to indicate whether or not the total number of rows is known.
   * Note that if the number of rows is not known, the value of
   * totalNumRows and getTotalNumRows() is not meaningful.
   */
   private boolean numberOfRowsIsKnown = true;

  /**
   *  Mathematical signum function.
   *  @param n any integer value
   *  @return 1 for positive n, -1 for negative n, or 0 if n is zero
   */
  public static int signum (int n) {
    if (n > 0) return 1;
    if (n < 0) return -1;
    return 0;
  }

  /**
   *  Specify the name by which this table is known
   *  @param name the table's new name
   */
  public void setUniversalTableId (String name) {
    universalTableId = name;
  }

  /**
   *  Retrieve the name by which this table is known
   *  @return the table's name
   */
  public String getUniversalTableId () {
    return universalTableId;
  }

  /**
   *  Indicate whether the table is sorted using the current sortOnColumn or
   *  sortOnMap in the current sortDirection
   *  @return true if the table is sorted, false otherwise
   */
  public boolean isSorted () {
    return sorted;
  }

  /**
   *  Notify the table that a previous sort may have been invalidated (probably
   *  due to a change in data elements).  Subsequent requests to sort the table
   *  will make no assumptions about the current order of the rows.
   */
  public void invalidateOrder () {
    sorted = false;
  }

  /**
   *  Retrieve the index of the first row to be displayed when generateHTML
   *  is called.
   *  @return the first row to be displayed
   */
  public int getDisplayStartRow () {
    return displayStartRow;
  }

  /**
   *  Specify the index of the first row to be displayed when generateHTML
   *  is called.
   *  @param r the first row to be displayed
   */
  public void setDisplayStartRow (int r) {
    displayStartRow = r;
  }

  /**
   *  Retrieve the name of the field in the data class that is compared
   *  between rows for sorting.
   *  @return data field name
   */
  public String getSortOnColumn () {
    return sortOnColumn;
  }

  /**
   *  Specify the name of the field (or "column") used for comparisons
   *  between rows in the sorting algorithm.
   *  @param col name of a data field name
   */
  public void setSortOnColumn (String col) {
    if (col == null) throw new NullPointerException();
    if (!col.equals(sortOnColumn)) {
      sorted = false;
      sortOnColumn = col;
      setSortOnMap((VariantMap) sortMaps.get(col));
    }
  }

  /**
   * Sets whether the total number of rows for this is known.
   */
  public void setNumberOfRowsIsKnown(boolean isKnown) {
    numberOfRowsIsKnown = isKnown;
  }
  /**
   * Returns true if the total number of rows is known.
   * Note that if the number of rows is not known, the value of
   * totalNumRows and getTotalNumRows() is not meaningful.
   */
  public boolean isTotalNumberOfRowsKnown() {
    return numberOfRowsIsKnown;
  }

  public void setAlreadySorted(String sortKey, int sortDirection)
          throws IllegalArgumentException{
    if(sortDirection != 1 && sortDirection != -1) {
      throw new IllegalArgumentException();
    }
    sorted = true;
    sortOnColumn = sortKey;
    this.sortDirection = sortDirection;
  }

  /**
   *  Retrieve the {@link StringMap} instance currently being used for sorting on
   *  a mapped attribute.
   *  @return the sorting map
   */
  public VariantMap getSortOnMap () {
    return sortOnMap;
  }

  /**
   *  Specify the {@link StringMap} to be used for making comparisons in sorting.
   *  @param m the new {@link StringMap} instance
   */
  public void setSortOnMap (VariantMap m) {
    sortOnMap = m;
  }

  /**
   *  Determine whether the sort algorithm produces ascending or descending
   *  order.
   *  @return 1 for ascending; -1 for descending
   */
  public int getSortDirection () {
    return sortDirection;
  }

  /**
   *  Specify a sort in either ascending or descending order.
   *  @param dir takes 1 for ascending, -1 for descending
   *  @throws IllegalArgumentException if dir is something other than -1 or 1
   */
  public void setSortDirection (int dir) throws IllegalArgumentException {
    if (dir != 1 && dir != -1)
      throw new IllegalArgumentException();
    if (dir != sortDirection) {
      sorted = false;
      sortDirection = dir;
    }
  }

  /**
   *  Retrieve the maximum number of rows that will be displayed on a page.
   *  Pagination is supported for tables larger than this threshold.<br>
   *  The default value is 20.
   *  @return number of rows displayed at a time.
   */
  public int getDisplayNumRows () {
    return displayNumRows;
  }

  /**
   *  Specify the maximum number of rows in this table that can be displayed
   *  on a page.
   *  @param n Number of lines
   */
  public void setDisplayNumRows (int n) {
    displayNumRows = n;
  }

  public void setTotalNumRows(int n) {
    this.totalNumRows = n;
  }
  public int getTotalNumRows() {
    return this.totalNumRows;
  }

  /**
   * Determine whether this UniversalTable is completely instantiated.
   */
  public boolean isCompletelyInstantiated() {
    return (rows.length == getTotalNumRows());
  }

  /**
   *  Obtain a reference to the formatting information for this table.  For each
   *  column in the table, there is a corresponding Hashtable with information
   *  on how to display its header, links, and entries.
   *  @return an array of column formats
   */
  public Hashtable[] getColumnInfo () {
    return columnInfo;
  }

  /**
   *  Specify the format for the columns of this table.  The column formats
   *  include these key-value pairs:
   *  <ul>
   *    <li>"Header" -- text displayed at the head of the column</li>
   *    <li>"Link" -- hypertext link associated with the column header</li>
   *    <li>"Field" -- name of the field containing this column's data or
            the {@link VariantMap} that computes it</li>
   *    <li>"Justify" -- "left", "right", or "center" justification</li>
   *    <li>"BGColor" -- Hexadecimal color for the column's background</li>
   *    <li>"LinkMap" -- {@link StringMap} implementor to calculate hypertext links
   *        for the entries in this column</li>
   *    <li>"HTML_CONTENT" -- any non-null entry for this key indicates
   *        that the column contains HTML tags that should be parsed by the
   *        browser for display.  This suppresses the default behavior, which
   *        is to encode the table entries in an HTML-inert format.</li>
   *  </ul>
   *  @param c array of formats (as Hashtables)
   */
  public void setColumnInfo (Hashtable[] c) {
    columnInfo = new Hashtable[c.length];
    sortMaps.clear();
    for (int i = 0; i < c.length; i++) {
      // cache the VariantMaps (if any) used for sorting on this column
      String sortKey = (String) c[i].get("SortKey");
      VariantMap sortMap = (VariantMap) c[i].get("SortMap");
      if (sortKey != null && sortMap != null){
        sortMaps.put(sortKey, sortMap);
      }
      // check for selection columns
      if (c[i].get("SELECT_COLUMN") == null){
        columnInfo[i] = c[i];
      }
      else {
        String uidField = (String) c[i].get("SelectorField");
        selector = new UniversalTableSelect(this, uidField);
        Hashtable newCol = new Hashtable();
        newCol.put("HTML_CONTENT", "Y");
        newCol.put("Header", c[i].get("Header"));
        newCol.put("BGColor", c[i].get("BGColor"));
        if(c[i].get("Explanation") != null) {
          newCol.put("Explanation", c[i].get("Explanation"));
        }
        newCol.put("Link", "javascript:" + selector.getPrefix() + "invert()");
        newCol.put("Field", selector.getCheckMapper());
        newCol.put("Justify", "center");
        columnInfo[i] = newCol;
      }
    }
  }

  /**
   *  makeColumnFormat is a short-cut way of generating the Hashtable of
   *  formatting information for a single column in the table display.  This
   *  implementation has parameters for the column's header, the header's HTML
   *  hyperlink, the column's background color, the type of justification used
   *  by the cells in the column, the data field displayed in each cell and the
   *  HTML hyperlink belonging to each data entry.
   *  <br><br>
   *  For purposes of sorting, the name of this column is the same as the data
   *  field, if it is a String.  The keys for sorting the rows on this column
   *  are, in that case, the values obtained by that field name from the
   *  {@link DataWrapper}s that form the table's rows.  If the field is a
   *  {@link VariantMap}, the header is used as the name of the column and the
   *  field's values are the sorting keys.
   *
   *  @param header the String used for the column header
   *  @param link the href for the hyperlink associated with the column header
   *  @param bgcolor the background color for this column
   *  @param justify justification for the column "left", "center", or "right"
   *  @param field the name of the data field or {@link StringMap} that populates this
   *         column
   *  @param linkMap a {@link StringMap} for computing the hyperlinks associated with
   *         the entries in this column
   */
  public static Hashtable makeColumnFormat (String header, Object link,
      String bgcolor, String justify, Object field, StringMap linkMap)
  {
    Hashtable t = makeColumnFormat(header, "", link, bgcolor, justify, field, linkMap);
    return t;
  }
  /**
   * Same as the version w/o String headerExplanation, except the
   * parameter headerExplanation is used as a help message string
  */
  public static Hashtable makeColumnFormat (String header, String headerExplanation,
      Object link, String bgcolor, String justify, Object field, StringMap linkMap)
  {
    Hashtable t = new Hashtable();
    if (header != null)
      t.put("Header", header);
    t.put("Explanation", headerExplanation);
    if (link != null) {
      if (link instanceof String || link instanceof StringMap)
        t.put("Link", link);
      else
        throw new IllegalArgumentException(
          "For \"link\":  expecting String or StringMap, but found " +
          link.getClass().getName());
    }
    if (bgcolor != null)
      t.put("BGColor", bgcolor);
    if (justify != null)
      t.put("Justify", justify);

    if (field != null) {
      if (field instanceof String) {
        t.put("Field", field);
        t.put("SortKey", field);
        t.put("SortMap", new PropertyExtractorMap((String) field));
      }
      else if (field instanceof VariantMap) {
        t.put("Field", field);
        t.put("SortMap", field);
        if (header != null)
          t.put("SortKey", header);
      }
      else
        throw new IllegalArgumentException(
          "For \"field\":  expecting String or VariantMap, but found " +
          field.getClass().getName());
    }
    else
      throw new IllegalArgumentException(
        "For \"field\":  expecting String or VariantMap, but found null");

    if (linkMap != null)
      t.put("LinkMap", linkMap);
    return t;
  }

  /**
   *  Create a configuration Hashtable for a column in a UniversalTable.
   *  In this case, an additional field, sortKey, is used instead of the column
   *  header as the name of the column for sorting purposes.  Otherwise, this
   *  method has the same effect as the six-parameter version (q.v.).
   *  @param sortKey the name of the mapped column for sorting.
   */
  public static Hashtable makeColumnFormat ( String header,
      Object link, String bgcolor, String justify,
      Object field, StringMap linkMap, String sortKey)
  {
    Hashtable t = makeColumnFormat(header, "", link, bgcolor, justify, field, linkMap, sortKey);
    return t;
  }
  /**
   * Same as the version w/o String headerExplanation, except
   * param headerExplanation is used as a help message string
  */
  public static Hashtable makeColumnFormat ( String header,
      String headerExplanation, Object link, String bgcolor, String justify,
      Object field, StringMap linkMap, String sortKey)
  {
    if (sortKey == null)
      throw new IllegalArgumentException("provided sorting name is null");
    Hashtable t = makeColumnFormat(
      header, headerExplanation, link, bgcolor, justify, field, linkMap);
    t.put("SortKey", sortKey);
    return t;
  }

  /**
   *  Same as the six-parameter version except that two additional arguments
   *  are accepted.  The first of these is the name associated with the column
   *  for sorting.  The other is a {@link VariantMap}, whose values are used in place
   *  of the column's actual values to determine the order when sorting the
   *  rows.
   *  <br>
   *  If either of these values is null, then both will be silently ignored.
   *  @param sortKey the column's name for purposes of sorting
   *  @param sortMap the map that determines the order when sorting
   */
  public static Hashtable makeColumnFormat ( String header,
      Object link, String bgcolor, String justify,
      Object field, StringMap linkMap,
      String sortKey, VariantMap sortMap)
  {
    Hashtable t = makeColumnFormat(header, "", link, bgcolor, justify, field,
      linkMap, sortKey, sortMap);
    return t;
  }
  /**
   * Same as the version w/o String headerExplanation, except
   * param headerExplanation is used as a help message string
  */
  public static Hashtable makeColumnFormat ( String header,
      String headerExplanation, Object link, String bgcolor, String justify,
      Object field, StringMap linkMap,
      String sortKey, VariantMap sortMap)
  {
    Hashtable t = makeColumnFormat(
      header, headerExplanation, link, bgcolor, justify, field, linkMap);
    if (sortKey != null && sortMap != null) {
      t.put("SortKey", sortKey);
      t.put("SortMap", sortMap);
    }
    return t;
  }

  /**
   *  Construct a format for a column that will be presented to the user as a
   *  column of checkboxes for selecting rows in the table.  When the format
   *  is set on an instance of UniversalTable, this entry will be given special
   *  treatment.  A selection model will be created for that table and a new
   *  Hashtable containing the appropriate configuration will be substituted
   *  in place of this one.
   *  @param header the column header text
   *  @param bgColor the RGB color used as the column background
   *  @param selectorField the name of the field used to identify rows uniquely
   */
  public static Hashtable makeSelectColumn (String header, String bgColor,
      String selectorField)
  {
    Hashtable col = new Hashtable();
    col.put("SELECT_COLUMN", "Y");
    col.put("Header", header);
    col.put("BGColor", bgColor);
    col.put("SelectorField", selectorField);
    return col;
  }

  /**
   *  Construct a format for a column that will be presented to the user as a
   *  column of checkboxes for selecting rows in the table.  When the format
   *  is set on an instance of UniversalTable, this entry will be given special
   *  treatment.  A selection model will be created for that table and a new
   *  Hashtable containing the appropriate configuration will be substituted
   *  in place of this one.
   *  @param header the column header text
   *  @param headerExplanation a help message
   *  @param bgColor the RGB color used as the column background
   *  @param selectorField the name of the field used to identify rows uniquely
   */
  public static Hashtable makeSelectColumn (String header, String headerExplanation, String bgColor,
      String selectorField) {
    Hashtable col=makeSelectColumn(header,bgColor,selectorField);
    col.put("Explanation", headerExplanation);
    return col;
  }

  /**
   * Fetch all of the rows of data contained in this table
   * @return {@link DataWrapper}s containing the wrapped beans
   */
  public DataWrapper [] getRows()
  {
    return rows;
  }

  /**
   *  construct this UniversalTable with data rows and column formats as provided
   *  @param dw array of data rows of this table ensconced in DataWrapper objects
   *  @param c array of column formats for HTML output
   */
  public UniversalTable (DataWrapper[] dw, Hashtable[] c) {
    rows = dw;
    columnInfo = c;
  }

  /**
   *  construct this UniversalTable with data rows as provided
   *  @param dw array of data rows of this table ensconced in DataWrapper objects
   */
  public UniversalTable (DataWrapper[] dw) {
    if (dw == null)
      rows = new DataWrapper[0];
    else
      rows = dw;
  }

  /**
   *  Sort this table on the column given.  Ascending order is presumed unless
   *  the table is already sorted, in which case the order is reversed.
   *  @param col name of the column used for comparisons
   */
  public void sort (String col) throws IllegalArgumentException {
    if (col == null)
      throw new IllegalArgumentException("can't sort on null column");
    if (sorted && col.equals(sortOnColumn)) {
      reverseRows();
      sortDirection = -sortDirection;
    }
    else {
      sort(col, 1);
    }
  }

  /**
   *  Sort this table on the given column in the specified order.
   *  @param col name of the column used for comparisons
   *  @param dir direction of the sort:  1 for ascending or -1 for descending
   *  @throws IllegalArgumentException if col is null or dir is neither 1 nor -1
   */
  public void sort (String col, int dir) throws IllegalArgumentException {
    // check for validity of the arguments
    if (col == null)
      throw new IllegalArgumentException("can't sort on null column");
    if (dir != 1 && dir != -1)
      throw new IllegalArgumentException("illegal sort direction, " + dir);

    // support the sort direction toggle
    sortOnColumn = col;
    sortDirection = dir;
    sorted = true;

    // find the appropriate mapping and do the sort
    VariantMap orderingMap = (VariantMap) sortMaps.get(col);
    if (orderingMap == null)
      orderingMap = new PropertyExtractorMap(col);

    mappedSort(orderingMap);
  }

  // This class of VariantMap does the trivial task of extracting a property
  // using the DataWrapper
  private static class PropertyExtractorMap implements VariantMap {
    private String propertyName = null;

    public PropertyExtractorMap (String s) {
      propertyName = s;
    }

    public Variant map (Object w) {
      return ((DataWrapper) w).getProperty(propertyName);
    }
  }

  /**
   *  For servlets using standard techniques, the SortLinkMap class allows them
   *  to respond to requests to sort any UniversalTable they manage.  The
   *  servlet itself must recognize requests with parameters
   *  <ul>
   *    <li><i>command</i> = SORT,</li>
   *    <li><i>sortKey</i> = the column name, and</li>
   *    <li><i>universalTableId</i> = that table's getUniversalTableId().</li>
   *  </ul>
   *  For those tables supporting a selection model, new user selections are
   *  also handled by this class.
   */
  public static class SortLinkMap implements StringMap {
    private String key;

    /**
     *  Construct a new SortLinkMap configured to sort on the specified
     *  column in a UniversalTable.
     *  @param k the column name
     */
    public SortLinkMap (String k) {
      key = k;
    }

    /**
     *  Construct the hyperlink which a Servlet may interpret as a request to
     *  sort a UniversalTable
     *  @param w an Object (instance of UniversalTable) to be sorted
     */
    public String map (Object w) {
      UniversalTable ut = (UniversalTable) w;
      StringBuffer buf = new StringBuffer();
      buf.append("'?command=SORT&sortKey=");
      buf.append(key);
      buf.append("&universalTableId=");
      buf.append(ut.getUniversalTableId());
      buf.append("'");
      String jsCall = ut.getParameterSavingJs();
      if (jsCall != null)
        buf.append(" + '&' + " + jsCall);
      return "javascript:location.replace(" + buf + ");";
    }
  }

  /**
   *  Sort this table in order according to the values produced by the provided
   *  VariantMap.  The rows are sorted into ascending or decending order
   *  depending on the value of sortDirection.
   *  @param vMap a VariantMap for producing the sort keys
   */
  public void mappedSort (VariantMap vMap) {
    if (rows.length <= 1) {
      sorted = true;
      return;
    }
    Enumeration enu = new OrderedTraversal(rows, vMap, sortDirection);
    for (int i = 0; i < rows.length; i++)
      rows[i] = (DataWrapper) enu.nextElement();

    sorted = true;
  }

  /**
   *  Reverse the order of the rows in this table.
   */
  public void reverseRows () {
    for (int i = 0, j = rows.length - 1; i < j; i++, j--) {
      DataWrapper temp = rows[i];
      rows[i] = rows[j];
      rows[j] = temp;
    }
  }

  /**
   *  Search for and return the first row in the table matching the given
   *  criterion.  The comparison is performed on String representations.
   *  <br><br>
   *  If no match is found, null is returned.
   *  @param field the name of the column to search for matches
   *  @param target the value to match
   *  @return a matching data item or null
   */
  public DataWrapper findRow (String field, String target) {
    try {
      for (int i = 0; i < rows.length; i++) {
        if (rows[i].getProperty(field).toString().equals(target)) {
          return rows[i];
        }
      }
    }
    catch (Exception oh_no) {
      System.out.println("UniversalTable::findRow:  Error--" + oh_no);
    }
    return null;
  }

  /**
   *  Search for and remove all rows in the table matching the given criterion.
   *  The comparisons are performed between String representations.
   *  @param field the name of the column to search for matches
   *  @param target the value to match
   */
  public void removeRows (String field, String target) {
    removeRows(new PropertyExtractorMap(field), target);
  }

  /**
   *  Search for and remove all rows in the table matching the given criterion.
   *  The comparisons are performed between String representations.
   *  @param field a {@link VariantMap} representing the column to search for matches
   *  @param target the value to match
   */
  public void removeRows (VariantMap field, String target) {
    try {
      Vector cache = new Vector();
      int i;
      for (i = 0; i < rows.length; i++) {
        if (!field.map(rows[i]).toString().equals(target)) {
          cache.addElement(rows[i]);
        }
      }
      DataWrapper[] newRows = new DataWrapper[cache.size()];
      Enumeration enu = cache.elements();
      for (i = 0; enu.hasMoreElements(); i++) {
        newRows[i] = (DataWrapper) enu.nextElement();
      }
      rows = newRows;
    }
    catch (Exception oh_no) {
      System.out.println("UniversalTable::removeRows:  Error--" + oh_no);
    }
  }

  /**
   *  Search for a matching row and substitute the new record in place of the
   *  first match found.  Comparisons are performed on String representations.
   *  <br><br>
   *  If no match is found, then no substitution is performed.
   *  @param field the name of the column to search for matches
   *  @param record the new data bean being inserted
   */
  public void updateRow (String field, DataWrapper dw) {
    try {
      Variant v_target = dw.getProperty(field);
      String target = null;
      if (v_target != null)
        target = dw.getProperty(field).toString();
      if (target == null)
        throw new Exception(
          "trying to match a null value in field \"" + field + "\"");
      for (int i = 0; i < rows.length; i++) {
        if (target.equals(rows[i].getProperty(field).toString())) {
          rows[i] = dw;
          sorted = false;
          break;
        }
      }
    }
    catch (Exception oh_no) {
      System.out.println("UniversalTable::updateRow:  Error--" + oh_no);
    }
  }

  /**
   *  Give a printout of the contents of the table.  Features currently
   *  implemented include:
   *  <ul>
   *    <li>column header HTML links</li>
   *    <li>column justification (left, right, center)</li>
   *    <li>column background colors</li>
   *    <li>cell HTML link mapping</li>
   *  </ul>
   *  @param out a PrintWriter to take the HTML output
   */
  private void generateTable (PrintWriter out) {
    if (rows == null)
      System.out.println(
        "UniversalTable::generateHtml:  rows array is null");
    if (rows.length == 0) {
      out.println("<br><p class=mo2>No matching entries.</p>");
      out.flush();
      return;
    }

    // Begin table structure
    out.println("<center><table cellpadding=0 cellspacing=0 border=0 width=\"100%\">");

    // Place headers
    out.println("  <tr>");
    for (int i = 0; i < columnInfo.length; i++) {
      out.println("    <td valign=bottom width=5>" +
        "<img src=\"/art/spacer.gif\" width=5 border=0></td>");
      String header = (String) columnInfo[i].get("Header");
      //this is for the tooltip
      String tooltipText = (String) columnInfo[i].get("Explanation");
      Object link = columnInfo[i].get("Link");
      String sortKey = (String) columnInfo[i].get("SortKey");
      if (sortKey == null) {
        Object o = columnInfo[i].get("Field");
        if (o != null && (o instanceof String))
          sortKey = (String) o;
      }
      String sortArrow = "";
      if (sorted && sortOnColumn.equals(sortKey))
        sortArrow = "&nbsp;&nbsp;<img src=\"/art/" +
          (sortDirection > 0 ? "a" : "de") + "scending.gif\" border=0>";

      out.print("    <td align=center valign=bottom>");
      //this is for the tooltip.
      out.print("<a title=\"" +tooltipText + "\"");
      if (link != null) {
        out.print(" href=\"" +
          (link instanceof StringMap ?
            ((StringMap) link).map(this) :
            (String) link) +
          "\"");
      }
      out.print(">");
      out.print("<p class=mo1><b>" + header + sortArrow + "</b></p>");
      out.print("</a>");
      out.println("</td>");
      out.println("    " +
        "<td valign=bottom width=5><img src=\"/art/spacer.gif\" width=5 border=0></td>");
    }
    out.println("  </tr>");
    out.println("  <tr><td colspan=" + (3 * columnInfo.length) +
      " bgcolor=black><img src=\"/art/spacer.gif\" width=1 height=1 border=0></td></tr>");

    // Now output rows in succession
    int displayStopRow = Math.min(displayStartRow + displayNumRows, rows.length);
    for (int j = displayStartRow; j < displayStopRow; j++) {
      out.println("  <tr>");
      for (int i = 0; i < columnInfo.length; i++) {
        out.println("    " +
          "<td valign=top width=5><img src=\"/art/spacer.gif\" width=5 border=0></td>");
        try {
          Object field = columnInfo[i].get("Field");
          boolean encodeContent = (columnInfo[i].get("HTML_CONTENT") == null);
          String content = null;
          try {
            if (field instanceof VariantMap) {
              content = ((VariantMap) field).map(rows[j]).toString();
            }
            else if (field instanceof String) {
              content = rows[j].getProperty((String) field).toString();
            }
          }
          catch (Exception eep) { }

          if (content == null) {
            content = "";
          }
          if (encodeContent) {
            content = HTML.encode(content);
          }
          String justify = (String) columnInfo[i].get("Justify");
          String bgcolor = (String) columnInfo[i].get("BGColor");
          StringMap linkMap = (StringMap) columnInfo[i].get("LinkMap");
          String link = null;
          if (linkMap != null) {
            try {
              link = (linkMap == null ? null : linkMap.map(rows[j]));
            }
            catch (Exception badLink) {
              System.out.println(
                "UniversalTable::generateTable:  link generation error in \"" +
                columnInfo[i].get("Header") + "\"");
            }
          }
          out.print("    <td" + (justify == null ? "" : " align=" + justify) +
            (bgcolor == null ? "" : " bgcolor=" + bgcolor) +
            ">");
          if (link != null)
            out.print("<a href=\"" + link + "\">");
          if (justify == null || justify.equalsIgnoreCase("center"))
              out.print("<p class=mo1>" + content + "</p>");
          else
              out.print("<p class=mo1>&nbsp;" + content + "&nbsp;</p>");
          if (link != null)
            out.print("</a>");
          out.println("</td>");
          out.println("    " +
            "<td valign=top width=5><img src=\"/art/spacer.gif\" width=5 border=0></td>");
        }
        catch (Exception oh_no) {
          System.out.println(
            "UniversalTable::generateHtml:  Error in rows loop--" + oh_no);
          oh_no.printStackTrace();
        }
      }
      out.println("  </tr>");
      out.println("  <tr><td colspan=" + (3 * columnInfo.length) +
        " bgcolor=black><img src=\"/art/spacer.gif\" width=1 height=1 border=0></td></tr>");
    }

    // Close off table structure
    out.println("</table></center>");
  }

  /**
   *  Print out the number of pages and the number of the page currently being
   *  viewed.  Also, if appropriate, display HTML buttons for paging forward
   *  and back through the table.
   *  @param out a PrintWriter to take the HTML output
   */
  private void generatePageIndicator (PrintWriter out) {
    if(!numberOfRowsIsKnown) {
      generateNoPageIndicator(out);
      return;
    }

    int realLength = Math.max(rows.length, getTotalNumRows());
    if (rows.length == 0) return;
    // Page indicator
    int p = 1 + displayStartRow/displayNumRows;
    int n_pages = 1 + (realLength - 1)/displayNumRows;
    String resultStr = realLength == 1 ? " result" : " results";
    out.println(
      "<br><center><p class=mo9>PAGE " + p + " OF " + n_pages + "<br>"
          +realLength + resultStr + " found</p></center>");

    // display "Next" and "Previous" buttons, if applicable
    if (p > 1 || p < n_pages) {
      out.println("<center><table cellspacing=0 cellpadding=0 border=0 width=\"100%\"><tr>");
      out.println("  <td width=\"25%\"></td>");
      out.print("  <td width=\"20%\" align=center>");
      if (p > 1) {
        // need to show the "Previous" button
        out.print("<input type=button value=\"Previous\" onclick=\"" +
          prevPageActionJs() + "\">");
      }
      out.println("</td>");
      out.println("  <td width=\"10%\"></td>");
      out.print("  <td width=\"20%\" align=center>");
      if (p < n_pages) {
        // we need to show the "Next" button
        out.print("<input type=button value=\"Next\" onclick=\"" +
          nextPageActionJs() + "\">");
      }
      out.println("</td>");

      out.println("  <td width=\"25%\"></td>");
      out.println("</tr></table></center>");
    }
    out.flush();
  }

  /**
   *  Print a message noting that the total number of
   *  results is not calculated.
   *  If appropriate, display HTML buttons for paging forward
   *  and back through the table.
   *  @param out a PrintWriter to take the HTML output
   */
  private void generateNoPageIndicator (PrintWriter out) {
    boolean needNextButton, needPrevButton;
    needPrevButton = displayStartRow >= displayNumRows;
    needNextButton = true;
    if(rows.length < displayStartRow + displayNumRows)
      needNextButton = false;

    String message = "Result Count Not Available";
    out.println(
      "<br><center><p class=mo9>" + message + "</p></center>");

    // display "Next" and "Previous" buttons, if applicable
    if (needPrevButton || needNextButton) {
      out.println("<center><table cellspacing=0 cellpadding=0 border=0 width=\"100%\"><tr>");
      out.println("  <td width=\"25%\"></td>");
      out.print("  <td width=\"20%\" align=center>");
      if (needPrevButton) {
        // need to show the "Previous" button
        out.print("<input type=button value=\"Previous\" onclick=\"" +
          prevPageActionJs() + "\">");
      }
      out.println("</td>");
      out.println("  <td width=\"10%\"></td>");
      out.print("  <td width=\"20%\" align=center>");
      if (needNextButton) {
        // we need to show the "Next" button
        out.print("<input type=button value=\"Next\" onclick=\"" +
          nextPageActionJs() + "\">");
      }
      out.println("</td>");

      out.println("  <td width=\"25%\"></td>");
      out.println("</tr></table></center>");
    }
    out.flush();
  }

  protected String nextPageActionJs () {
    return "location.replace('?command=PAGE&rownum=" +
      (displayStartRow + displayNumRows) +
      (universalTableId == null ? "" :
        "&universalTableId=" + universalTableId) +
      "'" +
      (selector == null ? "" : " + '&' + " + selector.getParameterSavingJs()) +
      ")";
  }

  protected String prevPageActionJs () {
    return "location.replace('?command=PAGE&rownum=" +
      (displayStartRow - displayNumRows) +
      (universalTableId == null ? "" :
        "&universalTableId=" + universalTableId) +
      "'" +
      (selector == null ? "" : " + '&' + " + selector.getParameterSavingJs()) +
      ")";
  }

  /**
   *  Give an HTML printout of the current page of this table, with associated
   *  buttons, page numbering, and pagination controls.
   *  @param out a PrintWriter to write HTML output
   */
  public void generateHtml (PrintWriter out) {
    if (selector != null)
      selector.echoSelectScript(out);
    out.println("<form name=" +
      (universalTableId == null ? "UniversalTable" : universalTableId) +
      " method=GET>");
    out.println("<input name=command type=hidden value=\"\">");
    generateTable(out);
    out.println("</form>");
    generatePageIndicator(out);
  }

  /**
   * Adds additionalRows to rows. It is expected that additionalRows
   * have the same format as rows.
   */
 public void addRows(DataWrapper[] additionalRows) {
    int oldNumRows = rows.length;
    int newNumRows = additionalRows.length;
    int totalNumRows = oldNumRows + newNumRows;
    DataWrapper[] totalRows = new DataWrapper[totalNumRows];
    for(int i=0; i<oldNumRows; i++) {
      totalRows[i] = rows[i];
    }
    for(int j=0; j<newNumRows; j++) {
      totalRows[oldNumRows+j] = additionalRows[j];
    }
    rows = totalRows;
  }
}
