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
import java.io.FileNotFoundException;
import java.io.LineNumberReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;



/**
 *  @author Mike Walczak (mwalczak@bbn.com)
 *
 *  This class reads/writes a windows style .ini files.
 *  An example of a .ini files follows.  Windows .ini file
 *  has sections names delimited by '[' and ']'.  each section
 *  has entries that may or may not have assigned values.
 *  An entry is the leftmost token on a line, and on the left hand
 *  side of the '=' sign.  If there is value(s) assigned, than it
 *  appears on the right of the '=' sign.  If there are multiple values
 *  for an entry, they are separated by a comma.
 *  Section names must be unique. Entry names in a section can be listed
 *  multiple times (this results in values of each being appended to the
 *  first entry of that name).
 *
 *
 *  A line is considered a comment if the first char on the line is either
 *  a '#' or a ';'.
 *  Current implementation disregards comments on input and hence does not
 *  write them to the output.  Also the order in the output is not preserved.
 *
 *                                 <br>
 *  [386Enh]                       <br>
 *  woafont=dossapp.fon            <br>
 *  EGA80WOA.FON=EGA80WOA.FON      <br>
 *  FileSysChange=off              <br>
 *  test_entry1 = val1, val2, val3 <br>
 *  test_entry2 = val1             <br>
 *  test_entry3                    <br>
 *                                 <br>
 *  # this is a comment            <br>
 *  ; this is also a comment       <br>
 *                                 <br>
 *  [drivers]                      <br>
 *  waveSysChange=mmdrv.dll   ;this is a comment     <br>
 *  timerysChange=timer.drv #this is a comment       <br>
 *                                 <br>
 *  [mci]       # comment                   <br>
 *                                 <br>
 *  [FOO]  # this is a comment     <br>
 *  foo1 = val1                    <br>
 *  foo1 = val2 # this is a comment <br>
 *  foo1 = val3                    <br>
 *  foo2 = val4                    <br>
 *                                 <br>
 *
 */

public class IniArchive {

    /** used for lookup */
    protected Hashtable sections_;
    /** used to keep an alphabetically sorted order */
    protected Vector    sorted_sections_;
    private boolean sort_sections_alphabetically_;
    IniFactory factory_;

    private String COMMENT_DELIMITERS = "#;";
    private final static String WHITESPACE = " \t\r\n";
    private final static String DELIMITERS = "[],=\'\"";

    public final static String NULL_SECTION_NAME = "null_section_name";

    public IniArchive(IniFactory factory, boolean sort_sections_alphabetically)
    {
        sections_ = new Hashtable();

        sort_sections_alphabetically_ = sort_sections_alphabetically;
        if (sort_sections_alphabetically_)
            sorted_sections_ = new Vector();

        factory_ = factory;
        if (factory_ == null)
            throw new IllegalArgumentException("factory is null in the constructor");
    }

    /**
     * Define the set of characters that will be used a comment
     * characters for this IniArchive.  The initial default is "#;"
     * Set it to "#" for .properties files
     * @param newDelimiters The string of characters that can start comments
     * @return The old delimiter string
     */
    public String setCommentDelimiters(String newDelimiters)
    {
      String ret = COMMENT_DELIMITERS;
      COMMENT_DELIMITERS = newDelimiters;
      return ret;
    }
    /**
     * Read an .ini file.
     */
    public void readFromFile(String filename) throws FileNotFoundException
    {
        File file = new File(filename);
        if (!file.exists())
            throw new FileNotFoundException("file '"+filename+"' not found");

        readFile(file);
    }


    /**
     *  Save the data to a file in the .ini format.
     */
    public void writeToFile(String filename)
    {
        File file = new File(filename);
        try {
            FileOutputStream fout = new FileOutputStream(file);
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            PrintWriter pout = new PrintWriter(bout);

            print(pout);

        }
        catch (FileNotFoundException fe) {
            //System.err.println(fe.toString());
            fe.printStackTrace();
        }
        catch (IOException ie) {
            //System.err.println(ie.toString());
            ie.printStackTrace();
        }
        catch (Exception ex) {
            //System.err.println(ex.toString());
            ex.printStackTrace();
        }
    }

    public boolean hasSection(IniSection s)
    {
        if (s != null)
            return (sections_.get(s.getName()) != null);
        return false;
    }

    public boolean hasSection(String section)
    {
        if (section == null)
            return false;
        return (sections_.get(section) != null);
    }

    /**
     *  Retuens the enumeration of sections.
     */
    public Enumeration getSections()
    {
        if (sorted_sections_ != null)
            return sorted_sections_.elements();

        return sections_.elements();
    }

    /**
     * Given a section name, returns the section object if present,
     * and null otherwise.
     */
    public IniSection getSection(String section)
    {
        if (section != null)
            return (IniSection) sections_.get(section);
        return null;
    }


    public void print(PrintStream out)
    {
        print(new PrintWriter(out));
    }

    public void print(PrintWriter out)
    {
        /* print contents */
        for (Enumeration s = getSections(); s.hasMoreElements(); )
        {
            IniSection section = (IniSection) s.nextElement();
            String name = section.getName();
            if (!name.equalsIgnoreCase(NULL_SECTION_NAME))
              out.println("["+section.getName()+"]");
            /* print entries */
            for (Enumeration e = section.getEntries(); e.hasMoreElements(); )
            {
                IniEntry entry = (IniEntry) e.nextElement();
                out.print(entry.getName());
                /* print entry values */
                Enumeration v = entry.getValues();
                if (v.hasMoreElements())
                    out.print("=");

                boolean first = true;
                while (v.hasMoreElements())
                {
                    if (first)
                        first = false;
                    else
                        out.print(",");
                    out.print((String) v.nextElement());
                }
                out.println(""); // go to the next line
            }
        }
        out.flush();
        out.close();
    }


    public void read(LineNumberReader lnr)
    {
        try {
            String  line=null;
            IniSection s=null;
            IniEntry   e=null;
            while ((line = lnr.readLine()) != null)
            {
                if (line.length() == 0 || isComment(line))
                    continue;

                if (isSection(line))
                {
                    s = readSection(line);
                    if (s != null)
                    {
                        if (!addSection(s))
                            throw new Exception("section '"+s.getName()+"' already exists");
                    }
                }
                else
                {
                    if (s == null)  // handle null sections in properties files
                      addSection(s = readSection("["+NULL_SECTION_NAME+"]"));

                    if ((e = readEntry(line)) != null)
                    {
                        IniEntry e2 = s.getEntry(e.getName());
                        /* if this section does not yet have this entry, than add it */
                        if (e2 == null)
                            s.addEntry(e);
                        else                         /* otherwise, if it does have this entry, than add the new value
                         * 		       ** to the existing entry and discard the newly created entry
                         * 		       */
                        {
                            for (Enumeration enm = e.getValues(); enm.hasMoreElements(); )
                                e2.addValue((String) enm.nextElement());
                        }


                    }
                }
            }
            lnr.close();
        }
        catch (IOException ie) {
            //System.err.println(ie.toString());
            ie.printStackTrace();
        }
        catch (Exception ex) {
            //System.err.println(ex.toString());
            ex.printStackTrace();
        }
    }



    private void readFile(File file)
    {
        if (file == null)
            return;
        try {
            LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(file)));
            read(lnr);

        }
        catch (FileNotFoundException fe) {
            fe.printStackTrace();
        }
        catch (IOException ie) {
            ie.printStackTrace();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public boolean addSection(IniSection s)
    {
        if (hasSection(s))
            return false;

        int i, length;
        String name = s.getName();
        sections_.put(name, s);

        if (sort_sections_alphabetically_)
        {
            /* insert in a sorted order */
            if ((length = sorted_sections_.size()) == 0)
                sorted_sections_.addElement(s);            /* first element */
            else
            {
                String str;
                IniSection sec;
                boolean inserted = false;
                for (i=0; i < length; i++)
                {
                    sec = (IniSection) sorted_sections_.elementAt(i);
                    str = sec.getName();
                    /* lexicographically smaller: insert at i and break out of loop*/
                    if (name.compareTo(str) <= 0)
                    {
                        sorted_sections_.insertElementAt(s, i);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted)
                    sorted_sections_.addElement(s);                /* last element */
            }
        }
        return true;
    }


    public boolean removeSection(IniSection s)
    {
        if (hasSection(s))
            return false;

        sections_.remove(s.getName());
        if (sort_sections_alphabetically_)
            sorted_sections_.removeElement(s);
        return true;
    }


    /**
     * Check whether this line is a comment
     * (if the 1st non-blank char is in COMMENT_DELIMITERS string)
     */
    private boolean isComment(String line)
    {
        int i, length = line.length();
        char c;
        for (i=0; i < length; i++)
        {
            c = line.charAt(i);
            if (WHITESPACE.indexOf(c) != -1)
                continue;
            if (COMMENT_DELIMITERS.indexOf(c) != -1)
                return true;
            else
                break;
        }
        return false;
    }

    private boolean isSection(String line)
    {
        return (line.indexOf('[') != -1);
    }

    private IniSection readSection(String line)
    {
        int i, length, end_i, start_i;

        try {
            length  = line.length();
            start_i = line.indexOf('[');
            end_i   = line.indexOf(']');
            if (start_i == -1 || end_i == -1)
                throw new Exception("invalid section declaration: "+line);

            /* remove preceeding blanks */
            start_i = getStartIndex(line, start_i, length);
            /* remove trailing blanks */
            end_i   = getEndIndex(line, end_i, length);

            String name = line.substring(start_i, end_i);
            return factory_.newIniSection(name);
        }
        catch (Exception e) {
            //System.err.println(e.toString());
            e.printStackTrace();
        }
        return null;
    }


    private IniEntry readEntry(String line)  throws Exception
    {
        int indx, state, i, length, end_i, start_i;
        char c = ' ';
        length  = line.length();
        IniEntry e = null;
        String name, token;

        if (length == 0)
            return null;

        /* remove preceeding blanks */
        start_i = getStartIndex(line, 0, length);
        indx = line.indexOf('=');
        end_i = getEndIndex(line, (indx == -1 ? length-1 : indx), length);
        if (end_i < start_i)
            return null;
        name = line.substring(start_i, end_i);

        if (indx == -1)
            return factory_.newIniEntry(name);        /* no values */

        /* there is '=' char so there may be values */
        e = factory_.newIniEntry(name);
        /*
         ** read values:
         ** states 0 (start), 1 (in token)
         */
        state=0;
        token = null;
        for (start_i=end_i=i=indx; i < length; i++)
        {
            c = line.charAt(i);
            switch (state) {
            case 0:                /* start state: skip leading spaces and commas */
                if (COMMENT_DELIMITERS.indexOf(c) != -1)
                {
                    state = 4;
                    break;
                }
                else if (c == '\'' || c == '\"')                /* token delimited by quotes */
                {
                    state = (c == '\'' ? 3 : 2);
                    start_i = i+1;
                    break;
                }
                else if (WHITESPACE.indexOf(c) != -1 || DELIMITERS.indexOf(c) != -1)
                    continue;
                start_i = i;
                state = 1;
                break;
            case 1:
                if (c == ',' || COMMENT_DELIMITERS.indexOf(c) != -1)
                    /* end of token (NOTE: '\n' is not accounted for since 'readLine' does not put them into the String)*/
                {
                    addValue(e, line, start_i, i, length);
                    state = (c == ',' ? 0 : 4);
                }
                break;
            case 2:                /* tokens delimited by double quotes */
                if (c == '\"')
                {
                    addValue(e, line, start_i, i);
                    state = 0;                    /* reset state */
                }
                break;
            case 3:                /* tokens delimited by single quotes */
                if (c == '\'')
                {
                    addValue(e, line, start_i, i);
                    state = 0;                    /* reset state */
                }
            case 4:                /* comment on the same line => skip the rest of the line */
                {
                    if (i == length-1)                    /* if this is the last iteration */
                        c = '\'';                    /* Hack so that the comment will not be added in as a
                     * 			** value after falling out of the loop.
                     * 			*/
                    break;
                }
            };
        }
        if (i == length && e != null && length > 0 && c != '\'' && c != '\"')
            addValue(e, line, start_i, i-1, length);
        return e;
    }


    /**
     *  Given a start and end points this method will remove any trailing delimiter/whitespace
     *  characters.
     */
    private void addValue(IniEntry e, String line, int start_i, int i, int length) throws Exception
    {
        int end_i = getEndIndex(line,i,length);
        if (start_i > end_i)
            throw new  Exception("error while reading entry; line: "+line);
        e.addValue(line.substring(start_i, end_i));
    }


    /**
     *  Given a start and end point, this method will extract a token as-is
     *  and will NOT perform any checks for trailing delimiter/whitespace
     *  chars.
     */
    private void addValue(IniEntry e, String line, int start_i, int end_i) throws Exception
    {
        if (start_i >= end_i)
            throw new  Exception("error while reading entry; line = "+line);
        e.addValue(line.substring(start_i, end_i));
    }


    /* remove preceeding blanks */
    private int getStartIndex(String line, int start_i, int length)
    {
        char c;
        for (int i=0; i < length && (start_i < length); i++)
        {
            c = line.charAt(start_i);
            if (WHITESPACE.indexOf(c) != -1 || DELIMITERS.indexOf(c) != -1)
                //c == '[' || c == ']' || c == ',')
                start_i++;
            else break;
        }
        return start_i;
    }


    /* remove trailing blanks */
    private int getEndIndex(String line, int end_i, int length)
    {
        char c;
        boolean modified= false;
        if (end_i >= length || end_i < 0)
            throw new IllegalArgumentException("end index ("+end_i+") must be < length ("+length+") and >= 0");

        while (end_i >= 0)
        {
            c = line.charAt(end_i);
            if (WHITESPACE.indexOf(c) != -1 || DELIMITERS.indexOf(c) != -1 || COMMENT_DELIMITERS.indexOf(c) != -1)
                //c == '[' || c == ']' || c == ',' || c == '=' || c == '\"' || c == ')
            {
                modified = true;
                end_i--;
            }
            else break;
        }
        if (modified || (end_i == length-1))
            end_i++;

        return end_i;
    }




    public final static void main (String args[])
    {
        if (args.length < 2)
        {
            System.err.println("usage:  java IniArchive <input file> <output file>");
            return;
        }


        String input_file  = args[0];
        String output_file = args[1];
        System.out.println("Input file: "+input_file+"\nOutput file: "+output_file);

        IniFactory f = new IniDefaultFactory();
        IniArchive arch = new IniArchive(f, false);

        try {
            arch.readFromFile(input_file);
            arch.print(System.out);
            arch.writeToFile(output_file);
        }
        catch (FileNotFoundException fe) {
            //System.err.println(fe.toString());
            fe.printStackTrace();
        }
    }
}




