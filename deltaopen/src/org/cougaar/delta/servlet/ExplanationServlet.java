/*
 * <copyright>
 * LEGEND
 * Use, duplication, or disclosure by the Government is as set forth in the
 * Rights in technical data noncommercial items clause DFAR 252.227-7013 and
 * Rights in noncommercial computer software and noncommercial computer
 * software documentation clause DFAR 252.227-7014.
 *
 * \uFFFD© Copyright 1997,1999 BBNT Solutions LLC
 * </copyright>
 */

package org.cougaar.delta.servlet;

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.net.URLDecoder;
import org.cougaar.delta.applet.*;
import org.cougaar.delta.util.*;
import org.cougaar.delta.util.html.*;
import org.cougaar.delta.util.variant.*;
import org.cougaar.delta.util.variant.Variant;
import org.cougaar.delta.util.variant.VariantText;
import org.cougaar.delta.util.variant.VariantFloat;
import org.cougaar.delta.util.variant.VariantMap;

public class ExplanationServlet extends BasicServlet {
  private ExplanationFactory factory = null;

  /**
   * No title.
   */
  public String getTitle()
  {
    return "";
  }

  /**
   * Initialize the ExplanationFactory
   * @param sc not used
   */
  public void processInit (ServletConfig sc) throws ServletException {
    try {
      factory = ExplanationFactory.getInstance();
    }
    catch (RuntimeException oh_no) {
      System.out.println("ExplanationServlet::processInit:  ERROR--" + oh_no);
      oh_no.printStackTrace();
    }
  }

  /**
   * Perform the call to the factory to get a list of Code objects
   * @param request the servlet request passed to the Get or Post methods
   */
  private Vector getCodes(HttpServletRequest request)
  {
//    QueryTable ht = factory.getQueryTable();
    Vector v = new Vector();
//    String table = getParameter(request, "table");

    /*
     * Require that the table be specified
     */
/*    if (table == null)
      return null;

    ht.put("tableName", table);

    String label = getParameter(request, "label");
    if (label != null)
      ht.put("labelName", label);

    String explanation = getParameter(request, "explanation");
    if (explanation != null)
      ht.put("explanationValue", explanation);

    UniversalTable t = factory.query(ht);

    DataWrapper [] rw = t.getRows();
    for (int i=0; i<rw.length; i++)
      v.addElement((Code)rw[i].unwrap());*/
    return v;
  }


  /**
   * Generate a text list of codes matching the request parameters.  Parameters are
   * "table", "code", and "value".
   * @param request the input request
   * @param response the output response
   */
  public void processGet (HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {/*
    Hashtable ht = new Hashtable();
    Vector codes = getCodes(request);
    try {
      response.setContentType("text/plain");
      PrintWriter o = new PrintWriter(response.getOutputStream());

      if (codes == null)
      {
        o.println("You must specify a table");
      }
      else
      {
        Enumeration e = codes.elements();
        while (e.hasMoreElements())
        {
          o.println((Code)e.nextElement());
        }
      }
      o.flush();

    }
    catch (Exception oh_no) {
      System.out.println("CodeExplanation::processGet:  ERROR--" + oh_no);
      oh_no.printStackTrace();
    }*/
  }

  /**
   *  Override the default header.  For POST requests, we'll be sending
   *  serialized java Objects, and extraneous HTML tags would interfere.
   */
  public void generatePostHeader (HttpServletRequest q, HttpServletResponse s)
      throws IOException
  {
  }

  /**
   *  Override the default footer.  For POST requests, we'll be sending
   *  serialized java Objects, and extraneous HTML tags would interfere.
   */
  public void generatePostFooter (HttpServletRequest q, HttpServletResponse s)
      throws IOException
  {
  }

  /**
   *  Override the default header.  For GET requests, we'll be sending
   *  raw text, and extraneous HTML tags would interfere.
   */
  public void generateGetHeader (HttpServletRequest q, HttpServletResponse s)
      throws IOException
  {
  }

  /**
   *  Override the default footer.  For GET requests, we'll be sending
   *  raw text, and extraneous HTML tags would interfere.
   */
  public void generateGetFooter (HttpServletRequest q, HttpServletResponse s)
      throws IOException
  {
  }

  /**
   * Extract an AppletToServletParcel from the HTTP input stream.
   * @param request the HttpServletRequest passed to doPost().
   * @return The de-serialized AppletToServletParcel or null if error.
   * @exception IOException if there is an error reading the stream.
   */
  private AppletToServletParcel unwrapParcel(HttpServletRequest request) throws IOException
  {
    AppletToServletParcel box = null;
    try {
      ServletInputStream in = request.getInputStream();
      ObjectInputStream ois = new ObjectInputStream(in);
      box = (AppletToServletParcel) ois.readObject();
    } catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    return box;
  }
  /**
   * Generate a serialized vector of codes matching the request parameters.  Parameters are
   * "table", "code", and "value".
   * @param request the input request
   * @param response the output response
   */
  public void processPost (HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {

      String domain = getParameter(request, "domain");
      String label = getParameter(request, "label");
      String command = getParameter(request, "command");

      ObjectInputStream ois = new ObjectInputStream(request.getInputStream());
      try
      {
           AppletToServletParcel box = (AppletToServletParcel) ois.readObject();
      } catch (ClassNotFoundException e)
      {
        // We can actually ignore this here since we don't need and indeed
        // always ignore the incoming post data
      }
      //if we are asked to return a domain's hashtable,
      if(command.equals("LOOKUPDOMAIN")) {
        Hashtable domainTable = factory.lookupDomain(domain);
        try {
          AppletToServletParcel box = new AppletToServletParcel("Success", domainTable);
          ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
          oos.writeObject(box);
        }
        catch (Exception oh_no) {
          System.out.println("ExplanationServlet::processPost:lookup(domain)  ERROR--" + oh_no);
          oh_no.printStackTrace();
          response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return;
      }

      //otherwise, we are being asked to return an explanation string
      //decode label
      try {
        label = URLDecoder.decode(label);
      }
      catch(RuntimeException e) {
        //set label to something harmless
        label = "URLDecoder error";
      }
//    System.out.println("ExplanationServlet process post: "+domain+","+label);
      String explanation = factory.lookupExplanation(domain,label);
      try {
        AppletToServletParcel box = new AppletToServletParcel("Success", explanation);
        ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
        oos.writeObject(box);
      }
      catch (Exception oh_no) {
        System.out.println("ExplanationServlet::processPost:lookup(domain,label)  ERROR--" + oh_no);
        oh_no.printStackTrace();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
  }
}
