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


import java.io.*;
import java.util.*;
import org.cougaar.delta.util.BasicConstants;




/**
 * Provides a read-only api to access application parameters.
 */

public class ParameterFileReader {

	/**
	 * A hierarchical representation of an .ini parameter file
	 */
    IniArchive parameters_ /* = null */;

    /*
     * A static hashtable of configuration instances indexed by file name.  There is one configuration
     * instance for each configuration file successfully loaded
     */
    private static final Hashtable instances = new Hashtable();

    public static String getFGIPlugInPropertyFileName() {
      String ret = System.getProperty(BasicConstants.PLUGINPROPERTIES_ARG);
      if(ret==null){
        String alpInstallPath = System.getProperty(BasicConstants.INSTALL_PATH_ARG);
        if(alpInstallPath==null) {
          alpInstallPath = BasicConstants.DEFAULT_INSTALL_PATH;
          System.err.println("WARNING: Using default_alp_install_path: "+alpInstallPath);
        }
        String fs = File.separator;
        if(alpInstallPath.endsWith(fs)) {
          ret = alpInstallPath + "deltaopen" + fs + "data" + fs + BasicConstants.PLUGINPROPERTIES_FILENAME;
        }
        else {
          ret =  alpInstallPath + fs + "deltaopen" + fs + "data" + fs + BasicConstants.PLUGINPROPERTIES_FILENAME;
        }
      }
      return ret;
    }

    /**
     * Static method to retrieve a ParameterFileReader
     *
     * @param filename specifies the file name that holds the configuration
     * parameters
     */
    synchronized public static ParameterFileReader getInstance(String filename)
    {
    	ParameterFileReader retval = (ParameterFileReader) instances.get(filename);
    	if (retval == null)
    	{
    		retval = new ParameterFileReader(filename);
    		instances.put(filename, retval);
    	}
    	return retval;
    }

    protected ParameterFileReader(String filename)
    {
	try {
	    /*
	    ** Load the properties file from disk.
	    */
	    IniFactory f = new IniDefaultFactory();
	    parameters_  = new IniArchive(f, false);
      if (filename != null)
  	    parameters_.readFromFile( filename );

	} catch (java.io.FileNotFoundException fnf) {
	    fnf.printStackTrace();
	    parameters_ = null;
	} catch (java.io.IOException e) {
	    e.printStackTrace();
	    parameters_ = null;
	}
    }


    public boolean isLoaded()
    {
	return (parameters_ != null);
    }


    /**
     *  Retrieve an enumeration of values associated with a parameter name.
     *
     *  @param  parameter_name -- name of the parameter to query
     *  @return enumeration of values associated with a parameter
     */
    public Enumeration getParameterValues(String section_name, String parameter_name)
    {
	if (parameters_ != null)
	    {
		IniSection s = parameters_.getSection(section_name);
		if (s != null)
		    {
			IniEntry entry = s.getEntry(parameter_name);
			if (entry != null)
			    return entry.getValues();
		    }
	    }
	return (new Hashtable()).keys();
    }

    public String[] getParameterValueArray(String section_name, String parameter_name)
    {
        if (parameters_ != null)
        {
            IniSection s = parameters_.getSection(section_name);
            if (s != null)
            {
                IniEntry entry = s.getEntry(parameter_name);
                if (entry != null)
                    return entry.getValueArray();
            }
        }
        return null;
    }

    /**
     *  Retrieve a string parameter.
     *  If the parameter has multiple values, only the first one is retrieved.
     *  (all values can be retrieved with 'getParameterValues(...)' call.
     *
     *  @param  section_name -- name of the section containing the parameter
     *  @param  parameter_name -- name of the parameter to retrieve
     *  @param  default_value  -- string to be returned if no parameter is present
     *  @return string parameter if present, 'default_value' otherwise
     */
    public String getParameter(String section_name, String parameter_name, String default_value)
    {
	if (parameters_ != null)
	    {
		Enumeration en = getParameterValues(section_name, parameter_name);
		if (en.hasMoreElements())
		    {
			/* return the 1st element */
			return (String) en.nextElement();
		    }
	    }
	return default_value;
    }

    /**
     *  Retrieve an integer parameter.
     *  If the parameter has multiple values, only the first one is retrieved.
     *  (all values can be retrieved with 'getParameterValues(...)' call.
     *
     *  @param  section_name -- name of the section containing the parameter
     *  @param  parameter_name -- name of the parameter to retrieve
     *  @param  default_value  -- integer to be returned if no parameter is present
     *  @return integer parameter if present, 'default_value' therwise
     */
    public int getParameter(String section_name, String parameter_name, int default_value)
    {
	if (parameters_ != null)
	    {
		Enumeration en = getParameterValues(section_name, parameter_name);
		if (en.hasMoreElements())
		    {
			String val = (String) en.nextElement();
			int ival=0;
			try {
			    ival = Integer.parseInt(val);
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			}
			return ival;
		    }
	    }
	return default_value;
    }

    /**
     *  Retrieve a parameter of type double.
     *
     *  @param  section_name -- name of the section containing the parameter
     *  @param  parameter_name -- name of the parameter to retrieve
     *  @param  default_value  -- double value to be returned if no parameter is present
     *  @return double value parameter if present, 'default_value' otherwise
     */
    public double getParameter(String section_name, String parameter_name, double default_value)
    {
	if (parameters_ != null)
	    {
		Enumeration en = getParameterValues(section_name, parameter_name);
		if (en.hasMoreElements())
		    {
			String val = (String) en.nextElement();
			double dval=0;
			try {
			    dval = Double.valueOf(val).doubleValue();
			} catch (NumberFormatException e) {
			    e.printStackTrace();
			}
			return dval;
		    }
	    }
	return default_value;
    }

    /**
     *  Retrieve a parameter of type char.
     *  If the parameter has multiple values, only the first one is retrieved.
     *  (all values can be retrieved with 'getParameterValues(...)' call.
     *
     *  @param  section_name -- name of the section containing the parameter
     *  @param  parameter_name -- name of the parameter to retrieve
     *  @param  default_value  -- char value to be returned if no parameter is present
     *  @return char value parameter if present, 'default_value' otherwise
     */
    public char getParameter(String section_name, String parameter_name, char default_value)
    {
	if (parameters_ != null)
	    {
		Enumeration en = getParameterValues(section_name, parameter_name);
		if (en.hasMoreElements())
		    {
			String val = (String) en.nextElement();
			try {
			    char c = val.charAt(0);
			    return c;
			} catch (StringIndexOutOfBoundsException e) {
			    e.printStackTrace();
			}
		    }
	    }
	return default_value;
    }



    /**
     *  Retrieve a parameter of type boolean.
     *  If the parameter has multiple values, only the first one is retrieved.
     *  (all values can be retrieved with 'getParameterValues(...)' call.
     *
     *  @param  section_name -- name of the section containing the parameter
     *  @param  parameter_name -- name of the parameter to retrieve
     *  @param  default_value  -- boolean value to be returned if no parameter is present
     *  @return boolean value parameter if present, 'default_value' otherwise
     */
    public boolean getParameter(String section_name, String parameter_name, boolean default_value)
    {
	if (parameters_ != null)
	    {
		Enumeration en = getParameterValues(section_name, parameter_name);
		if (en.hasMoreElements())
		    {
			String val = (String) en.nextElement();
			if (val.equals("true") || val.equals("TRUE"))
			    return true;
			else if (val.equals("false") || val.equals("FALSE"))
			    return false;
		    }
	    }
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
    public static String concatenate(Enumeration e, String separator, String default_value)
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


}
