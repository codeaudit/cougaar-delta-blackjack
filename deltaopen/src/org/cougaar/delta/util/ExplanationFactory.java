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
import java.sql.*;
import org.cougaar.delta.util.variant.*;
import org.cougaar.delta.util.html.*;

/**
 * Used to access UI help messages from the database.
 * @author ALPINE (alpine-software@bbn.com)
 */
public class ExplanationFactory extends Factory {

  private static ExplanationFactory _instance = null;

  private Hashtable explanationTable = new Hashtable();

  private void doInit() {
    /*
    try {
      String q_str = "select * from UI_HELP_MESSAGE";
      ResultSet rs = doQuery(q_str, null);
      while(rs.next())
      {
        String label = rs.getString("LABEL");
        String domain = rs.getString("UI_DOMAIN");
        if(label==null || domain==null) {
          System.out.println("error in database UI_HELP__MESSAGE, null LABEL or UI_DOMAIN");
          continue;
        }
        String explanation = rs.getString("MESSAGE");
        if(explanation==null) {
          explanation = "No help message in the database for " + label + " in domain " + domain;
        }
        if(explanationTable.containsKey(domain)) {
            Hashtable domainTable = (Hashtable) explanationTable.get(domain);
//              System.out.println("in domain: "+domain);
//              System.out.println("adding label: "+label);
            domainTable.put(label, explanation);
        }
        else {
            Hashtable domainTable = new Hashtable();
//              System.out.println("adding domain: "+domain);
//              System.out.println("adding label: "+label);
            domainTable.put(label, explanation);
            explanationTable.put(domain, domainTable);
        }
      }
    }
    catch (Exception e)
    {
      System.err.println("Error in ExplanationFactory:doInit()");
      e.printStackTrace();
    }*/

  }
  /**
   * Fetch the singleton CodeFactory instance.
   * @return the shared CodeFactory
   */
  public static ExplanationFactory getInstance() {
    if (_instance == null) {
      _instance = new ExplanationFactory();
    }
    return _instance;
  }

  private ExplanationFactory() {
    doInit();
  }

  public Object[] getParameterList () {
    return new String[] {"label", "domain", "explanation"};
  }

  /**
   * Construct a set of Codes.  This method queries the database
   * to collect a list of matching codes.
   * @param request a hashtable containing keys and values to match
   * @return the codes that match the query
   */
  protected UniversalTable processQuery (Map request) {
  //not used, the factory's data is accessed through lookupExplanation

    DataWrapper[] result = null;
  /*
    String tableName = "UI_HELP_MESSAGE";
    String labelName = (String) request.get("label");
    String domainName = (String) request.get("domain");
    String explanationValue = (String) request.get("explanation");
    String [] separators = {" where", " and"};
    int sep_pos = 0;

    StringBuffer q = new StringBuffer();

    q.append("SELECT label, ui_domain, message from explanationTable");

    if (tableName != null)
    {
      q.append(separators[sep_pos] +
        " label = '" + labelName + "'");
      sep_pos = (sep_pos == separators.length-1 ? sep_pos : sep_pos+1);
    }

    if (domainName != null)
    {
      q.append(separators[sep_pos] +
        " domain = '" + domainName + "'");
      sep_pos = (sep_pos == separators.length-1 ? sep_pos : sep_pos+1);
    }

    if (explanationValue != null)
    {
      q.append(separators[sep_pos] +
        " explanation = '" + explanationValue + "'");
      sep_pos = (sep_pos == separators.length-1 ? sep_pos : sep_pos+1);
    }

    String q_str = q.toString();
    //this.printQuery(q_str, 50);
    try {
      ResultSet rs = doQuery(q_str);
      Vector codes = new Vector();
      while(rs.next())  // need to get this out because the SQL statement will be re-used
      {
        codes.addElement(new String[]{rs.getString(1), rs.getString(2), rs.getString(3)});
      }

      result = new DataWrapper[codes.size()];
      for (int i = 0; i < result.length; i++)
      {
        result[i] = new DataWrapper(codes.elementAt(i));
      }
    } catch (Exception e)
    {
      System.err.println("Error in ExplanationFactory::query()");
      e.printStackTrace();
    }*/
    return new UniversalTable(result);
  }

  public Hashtable lookupDomain(String domain) {
    if (explanationTable.containsKey(domain) ) {
      Hashtable domainTable = (Hashtable) explanationTable.get(domain);
      return domainTable;
    }
    else return (new Hashtable());
  }

  public String lookupExplanation(String domain, String label) {

    if (explanationTable.containsKey(domain) ) {
      Hashtable domainTable = (Hashtable) explanationTable.get(domain);
//      System.out.println("searching domain: "+domain);
      if(domainTable.containsKey(label)) {
        String ret = (String) domainTable.get(label);
        //remove any trailing white space
        ret = ret.trim();
        return ( ret );
      }
    }
    return("Didn't find an explanation for "+label+" in domain: "+domain);

  }

}
