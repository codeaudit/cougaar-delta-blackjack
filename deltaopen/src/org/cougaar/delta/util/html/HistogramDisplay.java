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

import mil.darpa.log.alpine.delta.util.*;
import java.io.*;

/**
 *  The HistorgramDisplay class provides, in HTML form, a bar graph
 *  representation of an IntegerHistogram instance.  The structure of the HTML
 *  generated by this class is as follows.  The whole graph is encased in a
 *  table with three rows, each with a single cell containing the title,
 *  some vertical spacing, and the bar graph itself.
 *  <br><br>
 *  For its part, the bar graph is another table with two or more rows.  The
 *  first row contains alternating empty cells and bars.  The second row
 *  contains alternating empty cells and labels for the bars.  Any other rows
 *  which may be present are generated by the generateFootLine method.
 */
public class HistogramDisplay {
  /**
   *  The title of the graph, if any, which will be displayed above the graph
   *  itself.
   */
  protected String title = null;

  /**
   *  The width in pixels of a single bar in the bar graph.
   */
  protected int bar_width = 30;

  /**
   *  The height in pixels of the tallest bar in the bar graph.  The others are
   *  scaled proportionally.
   */
  protected int max_height = 250;

  /**
   *  The background color of the plot, which basically occupies a rectangular
   *  region.  The color is represented by the string that would appear in the
   *  HTML file.
   */
  protected String bgColor = "#ffffff";

  /**
   *  Construct a new HistogramDisplay instance.
   */
  public HistogramDisplay () {
  }

  /**
   *  Specify the width of a bar in the bar graph.
   *  @param w the bar width in pixels.
   */
  public void setBarWidth (int w) {
    bar_width = w;
  }

  /**
   *  Specify the height of the largest bar in the plot.
   *  @param h the bar height in pixels.
   */
  public void setMaxHeight (int h) {
    max_height = h;
  }

  /**
   *  Specify the title of the graph.
   *  @param t the title
   */
  public void setTitle (String t) {
    title = t;
  }

  /**
   *  Specify the background color for the graph as in an HTML file.  For
   *  instance, white would be "#ffffff".
   *  @param the background color.
   */
  public void setBgColor (String c) {
    bgColor = c;
  }

  /**
   *  Construct a message for the user indicating that there are no data points
   *  in the histogram.
   *  @return the "no data" message
   */
  protected String getEmptyMessage() {
    return "No data points available";
  }

  /**
   *  Prepare the given histogram for display, generate the HTML code, and send
   *  it out to the provided PrintWriter.
   *  @param o the output PrintWriter
   *  @param hist the histogram instance being shown
   */
  public void generateHtml (PrintWriter o, IntegerHistogram hist) {
    if (hist.isEmpty()) {
      o.println("<br><p class=mo2>" + getEmptyMessage() + "</p>");
    }
    else {
      hist.scaleHeights(max_height);
      generateBarGraph(o, hist);
    }
    o.flush();
  }

  /**
   *  Generate the HTML for a nonempty histogram.  This is where the work is
   *  actually done.
   *  @param o the output PrintWriter
   *  @param hist the histogram being shown
   */
  protected void generateBarGraph (PrintWriter o, IntegerHistogram hist) {
    // generate the header
    o.println("<p class=mo2><table border=2 bgcolor=\"" + bgColor +
      "\" cellspacing=0 cellpadding=0 rules=none>");
    if (title != null)
      o.println("<tr><td align=center><b>" + title + "</b></td></tr>");
    o.println("<tr><td height=15></td></tr>");
    o.println("<tr><td><table cellspacing=0 cellpadding=0 bgcolor=\"" +
      bgColor + "\">");

    // then generate the bars in the graph
    o.println("  <tr>");
    generateEdgeSpacing(o);
    generateBar(o, hist.getBin(0), generateDetailLink(0));
    for (int i = 1; i < hist.length(); i++) {
      generateInterBarSpacing(o);
      generateBar(o, hist.getBin(i), generateDetailLink(i));
    }
    generateEdgeSpacing(o);
    o.println("  </tr>");

    // next generate the labels
    o.println("  <tr><td></td>");
    generateLabel(o, hist.getBin(0));
    for (int i = 1; i < hist.length(); i++) {
      o.println("    <td></td>");
      generateLabel(o, hist.getBin(i));
    }
    o.println("  </tr>");

    // finally, generate the footers
    generateFootLine(o, hist);
    o.println("</table></td></tr></table></p>");
  }

  /**
   *  Generate the spacing left of the first bar or right of the last bar.
   *  This can be overridden to give different spacing, if desired.
   *  @param o PrintWriter to which output is written
   */
  protected void generateEdgeSpacing (PrintWriter o) {
    o.println("    <td width=10></td>");
  }

  /**
   *  Generate the spacing between adjacent bars in the bar graph.  This can
   *  be overridden to give different spacing, if desired.
   *  @param o PrintWriter to which output is written
   */
  protected void generateInterBarSpacing (PrintWriter o) {
    o.println("    <td width=3></td>");
  }

  /**
   *  Determine the color assigned to a given bar in the bar graph.  The
   *  implementation of this mechanism is in flux, but currently, the current
   *  default behavior is to assign green to the bar whose right-hand endpoint
   *  is zero (on time), blue to those on its left (early), and red to those on
   *  its right (late).
   *  @param bin the Bin in the histogram corresponding to the bar to be colored
   *  @return the HTML-formatted color for the given Bin.
   */
  protected String getColor (IntegerHistogram.Bin bin) {
    int rep = bin.getRightEnd();
    if (rep > 0)
      return "#ff0000";
    else if (rep == 0)
      return "#00ff00";
    else
      return "#0000ff";
  }

  /**
   *  Calculate the to be label applied to the bar in the graph corresponding
   *  to a given histogram Bin.  The default is a range indicating which
   *  integers lie within the given Bin.
   *  @param bin the Bin being labeled
   *  @return the String label applied to the corresponding bar
   */
  protected String getLabel (IntegerHistogram.Bin bin) {
    return bin.getLeftEnd() + "..." + bin.getRightEnd();
  }

  /**
   *  Generate the HTML tags associated with a single bar in the bar graph.
   *  @param o the PrintWriter to which output is sent
   *  @param bin the Bin being rendered as a bar in a bar graph
   *  @param t the hyperlink associated with the rendered bar
   */
  protected void generateBar (PrintWriter o, IntegerHistogram.Bin bin, String t) {
    o.println("<td valign=bottom>" +
      "<table width=\"100%\" cellspacing=0 cellpadding=0 border=0>");
    o.println("  <tr><td align=center>" + bin.getCount() + "</td></tr>");
    o.println("  <tr><td height=" + bin.getDisplayHeight() +
      " bgcolor=\"" + getColor(bin) + "\">" + t + "</td></tr>");
    o.println("</table></td>");
  }

  /**
   *  Generate the HTML to be contained by a bar, including the associated
   *  hyperlink, if any. By default, there is no link, but subclasses can
   *  override this behavior.
   *  @param i the index of the bin
   *  @return the hyperlink HTML code
   */
  protected String generateDetailLink (int i) {
    return "<img src=\"/art/spacer.gif\" border=0 width=100% height=100%>";
  }

  /**
   *  Generate the HTML for the label attatched to a given bin.
   *  @param o the PrintWriter where output is sent
   *  @param bin the bin whose bar is being labeled.
   */
  protected void generateLabel (PrintWriter o, IntegerHistogram.Bin bin) {
    o.println("    <td align=center width=" + bar_width + ">" + getLabel(bin) +
      "</td>");
  }

  /**
   *  Generate the HTML inserted at the foot of the bar graph display.  By
   *  default, no footer is generated.
   *  @param o the PrintWriter to which output is being written
   *  @param hist the histogram represented by the generated bar graph.
   */
  protected void generateFootLine (PrintWriter o, IntegerHistogram hist) {
  }
}
