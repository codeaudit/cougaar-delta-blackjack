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
package org.cougaar.delta.plugin;


import org.cougaar.delta.util.BasicConstants;
import org.cougaar.delta.util.params.ParameterFileReader;
import org.cougaar.delta.util.Factory;

import org.cougaar.glm.ldm.Constants;
import org.cougaar.util.UnaryPredicate;
import org.cougaar.core.util.UID;
import org.cougaar.core.util.UniqueObject;
import org.cougaar.core.service.BlackboardService;
import org.cougaar.core.persist.Persistence;

import org.cougaar.planning.ldm.plan.NewTask;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.NewPrepositionalPhrase;
import org.cougaar.planning.ldm.plan.AllocationResult;
import org.cougaar.core.domain.RootFactory;
import org.cougaar.planning.ldm.plan.Task;
import org.cougaar.planning.ldm.plan.Preposition;
import org.cougaar.planning.ldm.plan.PrepositionalPhrase;
import org.cougaar.planning.ldm.plan.Expansion;
import org.cougaar.planning.ldm.asset.Asset;
import org.cougaar.planning.ldm.plan.PlanElement;
import org.cougaar.planning.ldm.plan.Allocation;

import org.cougaar.core.util.UID;
import org.cougaar.core.plugin.util.PluginHelper;
import org.cougaar.core.plugin.SimplePlugin;
import org.cougaar.util.DBConnectionPool;
import org.cougaar.util.UnaryPredicate;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;

import java.io.File;
import java.io.PrintStream;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A base plugin class that builds on cougaar's SimplePlugin.
 */
public abstract class BasicPlugin extends SimplePlugin {

  protected String alp_install_path_ = null;
  private   ParameterFileReader parameter_reader_ = null;
  protected String file_path_ = null;
  private final static String THIS_CLASS_NAME = "BasicPlugin";

  private final static String GLOBAL_PROPERTIES          = "GlobalProperties";
  private final static String FILE_PATH_P                = "file.Path";

  private boolean isPersistenceEnabled = false;
  private long times_executed = 0;

  protected String db_driver;
  protected String db_connection_string;
  protected String db_user;
  protected String db_password;

  protected final void setupSubscriptions() {

    try {
      String plugin_property_file_name = ParameterFileReader.getFGIPlugInPropertyFileName();
      alp_install_path_ = System.getProperty(BasicConstants.INSTALL_PATH_ARG);

      /* now try to open the properties file */
      /*
      ** Load the properties file from disk.
      */
      parameter_reader_ = ParameterFileReader.getInstance( plugin_property_file_name );
      if (!parameter_reader_.isLoaded())
      {
          throw new Exception("Error getting parameters");
      }


      String prop = System.getProperty("org.cougaar.core.persistence.enable");
      Boolean persist = null;
      if (prop != null)
        persist = new Boolean(prop.trim());
      if (persist != null && persist.booleanValue())
        isPersistenceEnabled = true;

      // Get the connection information for use of non-persistence connection
      db_driver = parameter_reader_.getParameter(GLOBAL_PROPERTIES, "reportpersist.oracle.Driver", "oracle.jdbc.driver.OracleDriver");
      Enumeration enm = parameter_reader_.getParameterValues(GLOBAL_PROPERTIES, "reportpersist.oracle.ConnectionString");
      db_connection_string = (enm.hasMoreElements() ? (String)enm.nextElement() : "");
      db_user = (enm.hasMoreElements() ? (String)enm.nextElement() : "");
      db_password = (enm.hasMoreElements() ? (String)enm.nextElement() : "");

      Class.forName(db_driver);

      Factory.setParameters(parameter_reader_);
      //
      //  Set variables from properties file FIRST.
      //  Then do any overwrites from plugin arguments SECOND.
      //
      if (hasProperties())
        handleProperties();
      handleArguments();
      if (file_path_ == null)
        file_path_ = alp_install_path_;

      initializeDELTAPlugin();
    }
    catch (Exception e)
    {
        throw new RuntimeException("ERROR initializing PlugIn " + e.getMessage());
    }
  }

  public abstract void initializeDELTAPlugin() throws Exception;

  /**
   *  This method is called after the properties from a file have been read in.
   *  This method allows the subclass to overide any existing parameters with
   *  the plugin parameters.
   */
  protected void handleArguments()
  {
  }

  /**
   *  This method is called after the properties from a file have been read in.
   *  This method allows the subclass to overide any existing parameters with
   *  the plugin parameters.
   */
  protected void handleProperties()
  {
    String fp = concatenate(getPropertyValues(GLOBAL_PROPERTIES, FILE_PATH_P), File.separator, "");
    if (fp.length() == 0)
      {
        System.out.println("BasicPlugin:handleProperties():  WARNING: file path property was not defined!!!");
        file_path_  = alp_install_path_;
      }
    else
      file_path_  = alp_install_path_ + File.separator + fp + File.separator;
  }



  /**
   *  Subclasses must implement this method so that
   *  this class knows whether it should look for a file
   *  <plugin-name>.properties or not.  Some plugin may
   *  not be interested in property retrieval, so there's
   *  no need to add more overhead.  If this method returns false
   *  then the plugin will have no access to properties for the duration
   *  of its run.
   *
   *  @return true if the plugin has a properties file, false otherwise
   */
  public abstract boolean hasProperties();


  /**
   *  Retrieve an array of values associated with a property name.
   *  A property should be in a form of '<section name>.<property name>' .
   *
   *  @param  section_name -- name of the section containing the property
   *  @param  property_name -- name of the property to retrieve
   *  @return String array of values associated with a property
   */
  public String[] getPropertyValueArray(String section_name, String property_name)
  {
    if (parameter_reader_ != null)
        return parameter_reader_.getParameterValueArray(section_name, property_name);

    return null;
  }

  /**
   *  Retrieve an enumeration of values associated with a property name.
   *  A property should be in a form of '<section name>.<property name>' .
   *
   *  @param  section_name -- name of the section containing the property
   *  @param  property_name -- name of the property to retrieve
   *  @return enumeration of values associated with a property
   */
  public Enumeration getPropertyValues(String section_name, String property_name)
  {
    if (parameter_reader_ != null)
      return parameter_reader_.getParameterValues(section_name, property_name);

    return new org.cougaar.util.EmptyEnumeration();

  }

  /**
   *  Retrieve a string property.
   *  If the property has multiple values, only the first one is retrieved.
   *  (all values can be retrieved with 'getPropertyValues(...)' call.
   *
   *  @param  section_name -- name of the section containing the property
   *  @param  property_name -- name of the property to retrieve
   *  @param  default_value  -- string to be returned if no property is present
   *  @return string property if present, 'default_value' otherwise
   */
  public String getProperty(String section_name, String property_name, String default_value)
  {
    if (parameter_reader_ != null)
      return parameter_reader_.getParameter(section_name, property_name, default_value);

    return default_value;

  }
  /**
   *  Retrieve an integer property.
   *  If the property has multiple values, only the first one is retrieved.
   *  (all values can be retrieved with 'getPropertyValues(...)' call.
   *
   *  @param  section_name -- name of the section containing the property
   *  @param  property_name -- name of the property to retrieve
   *  @param  default_value  -- integer to be returned if no property is present
   *  @return integer property if present, 'default_value' therwise
   */
  public int getProperty(String section_name, String property_name, int default_value)
  {
    if (parameter_reader_ != null)
      return parameter_reader_.getParameter(section_name, property_name, default_value);

    return default_value;

  }




  /**
   *  Retrieve a property of type double.
   *
   *  @param  section_name -- name of the section containing the property
   *  @param  property_name -- name of the property to retrieve
   *  @param  default_value  -- double value to be returned if no property is present
   *  @return double value property if present, 'default_value' otherwise
   */
  public double getProperty(String section_name, String property_name, double default_value)
  {
    if (parameter_reader_ != null)
      return parameter_reader_.getParameter(section_name, property_name, default_value);

    return default_value;

  }

  /**
   *  Retrieve a property of type char.
   *  If the property has multiple values, only the first one is retrieved.
   *  (all values can be retrieved with 'getPropertyValues(...)' call.
   *
   *  @param  section_name -- name of the section containing the property
   *  @param  property_name -- name of the property to retrieve
   *  @param  default_value  -- char value to be returned if no property is present
   *  @return char value property if present, 'default_value' otherwise
   */
  public char getProperty(String section_name, String property_name, char default_value)
  {
    if (parameter_reader_ != null)
      return parameter_reader_.getParameter(section_name, property_name, default_value);

    return default_value;

  }



  /**
   *  Retrieve a property of type boolean.
   *  If the property has multiple values, only the first one is retrieved.
   *  (all values can be retrieved with 'getPropertyValues(...)' call.
   *
   *  @param  section_name -- name of the section containing the property
   *  @param  property_name -- name of the property to retrieve
   *  @param  default_value  -- boolean value to be returned if no property is present
   *  @return boolean value property if present, 'default_value' otherwise
   */
  public boolean getProperty(String section_name, String property_name, boolean default_value)
  {
    if (parameter_reader_ != null)
      return parameter_reader_.getParameter(section_name, property_name, default_value);

    return default_value;

  }



  /**
   *  Utility method to concatinate enumeration of strings.  If the 'separator'
   *  arg is null than strings will be appended with no separator chars. If the
   *  separator is specified than it will be placed after the first item, and the
   *  last item will not have a trailing separator.
   *  The 'default_value' arg will be returned if the enumeration is empty.
   *
   *  @param e -- enumeration of strings
   *  @param separator -- optional separator between strings
   *  @param default_value -- returned if the enumeration is empty
   *  @return result of the concatination of strings optionally separated by 'separator' string
   */
  protected String concatenate(Enumeration e, String separator, String default_value)
  {
    String s = (separator == null ? "" : separator);
    StringBuffer sb = new StringBuffer();
    boolean first_loop = true;

    while (e.hasMoreElements())
      {
        if (!first_loop && s.length() > 0)
          sb.append(s);
        else if (first_loop)
          first_loop = false;

        sb.append( (String) e.nextElement());
      }
    if (sb.length() > 0)
      return sb.toString();

    return default_value;
  }

  public final void execute() {
    try
    {
      long start = 0;

      doExecute();

    }
  catch (Throwable t)
  {
    // Emergency measures to keep thread from dying.
    System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\nUncaught Exception or Error in COUGAAR Plugin:");
    t.printStackTrace();
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        try { Thread.sleep(60000); } catch (InterruptedException ie) {}
  }


  }


  /**
   * This method is the PlugIn "execute" method.
   * Subclasses should override this method
   */
  public abstract void doExecute() throws Exception;
  /**
   * Get the name of the plugin
   * @return the name of the plugin
   */
  public abstract String getPlugInName();


  public Connection getDatabaseConnection()  {
    if (isPersistenceEnabled) {
      Connection c = getBlackboardService().getPersistence().getDatabaseConnection(this);
      return c;
    }
    else {
      try
      {
        return DBConnectionPool.getConnection(db_connection_string, db_user, db_password);
      }
      catch (SQLException e)
      {
        e.printStackTrace();
        return null;
      }
    }
  }

  public void releaseConnection(Connection conn) {
    if (isPersistenceEnabled){
      this.getBlackboardService().getPersistence().releaseDatabaseConnection(this);
    }else {
      try
      {
        conn.close();
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }
  }

    //if you just want to change a NewTask's verb or direct object,
    //you can do it easily. If you want to add an extra prepositional
    //phrase, it is a little more work, so we provide these methods.
    /**
     * Given a NewTask, return it with an added prepositional phrase
     * based on <preposition>
     */
    public NewTask addPrepositionalPhrase( NewTask task, String preposition) {

  return addPrepositionalPhrase( task, preposition, null);
    }

    /**
     * Given a NewTask, return it with an added prepositional phrase
     * based on <preposition>, with indirect object <ido> if <ido> non-null.
     * If there is already a prepositional phrase with the same preposition,
     * it is replaced with the new one.
     */
    public NewTask addPrepositionalPhrase( NewTask task, String preposition, Object ido) {
        return addPrepositionalPhrase( task, preposition, ido, getFactory());
    }

    /**
     * just a static version. Useful to save extra calls to getFactory.
     * Also useful if subclasses want to have static methods.
     */
    public static NewTask addPrepositionalPhrase( NewTask task, String preposition, Object ido, RootFactory rootFactory) {

  //make prepositional phrase
  NewPrepositionalPhrase phrase = rootFactory.newPrepositionalPhrase();
  phrase.setPreposition( preposition );
  if( ido != null) {
      phrase.setIndirectObject(ido);
  }

  //add the new prepositional phrase to the old ones
  Vector v = new Vector();
  for( Enumeration e = task.getPrepositionalPhrases(); e.hasMoreElements(); ) {
            PrepositionalPhrase currentPhrase = (PrepositionalPhrase)e.nextElement();
            if(!currentPhrase.getPreposition().equals(preposition)) {
            v.addElement(currentPhrase);
            }
  }
  v.addElement( phrase );

  //alter change <task>
  task.setPrepositionalPhrases ( v.elements() );

  return task;
    }


    public static UnaryPredicate getUniqueObjectUnaryPredicate(final UID uid)
    {
        return new UnaryPredicate()
       {
          public boolean execute(Object o)
          {
            if (o instanceof UniqueObject && uid.equals(((UniqueObject)o).getUID()))
                return true;
            return false;
          }
       };
    }

    /**
     *  If a UniqueObject with this UID exists in the logplan
     * return it.  Otherwise return null.
     */
     public UniqueObject getUniqueObject(UID uid) {
       return (UniqueObject) query(getUniqueObjectUnaryPredicate(uid)).iterator().next();
     }


//////////////////////////////////////////////////////////////////////
// Static helper methods
//
//////////////////////////////////////////////////////////////////////

    /**
     *  Checks the task <t>'s prepositional phrases, returns true if one
     *  matches <p>, otherwise returns false
     */
     public static boolean containsPreposition(Task t, String p) {
        Enumeration ppEnum = t.getPrepositionalPhrases();
        while (ppEnum.hasMoreElements()) {
          PrepositionalPhrase pp = (PrepositionalPhrase) ppEnum.nextElement();
          if (pp.getPreposition().equals(p)) {
            return true;
          }
        }
        return false;
     }


      /**
       * Look through all the elements matching taskPred in the log plan.
       * If one of them is a task with matching UID, return it. Otherwise
       * return null.
       */
      public Task lookupTask(UID tUID, UnaryPredicate taskPred) {
        Iterator tasks = query(taskPred).iterator();
        while(tasks.hasNext()) {
          Object o = tasks.next();
          if(o instanceof Task) {
            Task currentTask = (Task)o;
            if(currentTask.getUID().equals(tUID)) {
              return currentTask;
            }
          }
        }
        return null;
      }

      /**
       * If the task <t> has the preposition <p>, return the indirect object
       * of the prepositional phrase. Otherwise returns null.
       */
      public static Object getIndirectObject(Task t, String p) {
        Object o = null;
        Enumeration ppEnum = t.getPrepositionalPhrases();
        while (ppEnum.hasMoreElements()) {
          PrepositionalPhrase pp = (PrepositionalPhrase) ppEnum.nextElement();
          if (pp.getPreposition().equals(p)) {
            o = pp.getIndirectObject();
            return o;
          }
        }
        return o;
      }

}
