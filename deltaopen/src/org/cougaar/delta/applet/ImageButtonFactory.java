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

package org.cougaar.delta.applet;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.io.*;

import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * This class makes JButtons with images instead of just text.
 * It gets its images from the URL passed to the constructor
 */
public class ImageButtonFactory {

  Hashtable images;
  String urlString;
  String archive;
  URL codebase;

  /**
   * Make a button factory that gets images from the web site given.
   * @param codebase the applet code base (directory)
   * @param archive the name of the JAR file containing the images
   * @param artDir the subdirectory (under codebase) containing images (in case
   * they can't be loaded from the JAR)
   */
  public ImageButtonFactory(URL codebase, String archive, String artDir) {
    images = new Hashtable();
    urlString = toCanonicalForm(codebase.toExternalForm() + artDir);
    this.codebase = codebase;
    this.archive = archive;
  }

  /**
   *  Make a button factory that returns default buttons for every request.
   */
  public ImageButtonFactory () {
  }

  /**
   * Remove all ".." sequences from the url_spec string.
   */
  private String toCanonicalForm(String url_spec) {
    if (url_spec.indexOf("..") == -1)
      return url_spec;
    // Strip out .. paths
    StringBuffer ret = new StringBuffer("/");
    URL ret_url = null;
    try {
      URL url = new URL(url_spec);
      String file = url.getFile();
      StringTokenizer strtok = new StringTokenizer(file, "/");
      String [] path_component = new String[strtok.countTokens()+1];
      int count=0;
      while (strtok.hasMoreElements()) {
        path_component[count] = strtok.nextToken();
        count++;
      }
      path_component[path_component.length-1] = "";
      for(int i=0; i<count; i++) {
        if (path_component[i+1].equals("..")) {
          i += 1;
          continue;
        }
        ret.append(path_component[i]);
        ret.append("/");
        ret_url = new URL(url.getProtocol(), url.getHost(), url.getPort(), ret.toString());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return ret_url.toExternalForm();
  }

  /**
   * Create an image button.  Looks on the server for images named <label>.gif and
   * <label>Pressed.gif  If it can't find the image a regular JButton is created using the <label>
   * as the caption.
   * @param label the name of the image to use
   * @return a new button
   */
  public JButton getButton(String label)
  {
    ImageIcon img = null;
    ImageIcon pressedImg = null;

    if (urlString != null) {
      String pressedLabel = label+"Pressed";
      img = (ImageIcon)images.get(label);
      pressedImg = (ImageIcon)images.get(pressedLabel);

      if (img == null)    // try to get it from the JAR file
      {
        img = getImageIconFromJAR("/"+label+".gif");
        pressedImg = getImageIconFromJAR("/"+label+"Pressed.gif");
        if (img != null)
        {
          images.put(label, img);
          images.put(pressedLabel, pressedImg);
        }
      }

      if (img == null) // need to fetch it
      {
        try {
          img = new ImageIcon(new URL(urlString+label + ".gif"));
          pressedImg = new ImageIcon(new URL(urlString+pressedLabel + ".gif"));
        }
        catch (MalformedURLException ex)
        {
          ex.printStackTrace();
        }
        while (img.getImageLoadStatus() == MediaTracker.LOADING);
        if (img.getImageLoadStatus() == MediaTracker.COMPLETE)
          images.put(label, img);
        else
          img = null;

        while (pressedImg.getImageLoadStatus() == MediaTracker.LOADING);
        if (pressedImg.getImageLoadStatus() == MediaTracker.COMPLETE)
          images.put(pressedLabel, pressedImg);
        else
          pressedImg = null;
      }
    }

    if (img == null) // can't find it...
      return new JButton(label);
    else
      return new ImageJButton(img, pressedImg);
  }

  /**
   * A test stub
   */
  public static void main(String[] args) {
    try {
      ImageButtonFactory fact = new ImageButtonFactory(new URL("http://al:8000/art/"), "", "");

      JFrame f = new JFrame("test");
      f.getContentPane().add(fact.getButton("test"));
      f.setSize(100, 100);
      f.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private ImageIcon getImageIconFromJAR(String fileName)
  {
    Image img = getImageFromJAR(fileName);
    if( img == null )
      return null;
    return new ImageIcon(img);
  }

  /**
   * Get an image by filename.  If the file can be loaded as a resource, do so.
   * Otherwise, go to the URL for it.
   * @param fileName the file name of the image eg: "blabbo.gif"
   * @return and image or null if not found
   */
  public Image getImage(String fileName)
  {
    Image ret = getImageFromJAR(fileName);
    if (ret == null)
      try {
        ret = Toolkit.getDefaultToolkit().getImage(new URL(urlString+fileName));
      } catch (MalformedURLException e)
      {
        e.printStackTrace();
      }
    return ret;
  }

  private Image getImageFromJAR(String fileName)
  {
    if( fileName == null )
      return null;
    Image image = null;
    byte[] tn = null;

    Toolkit toolkit = Toolkit.getDefaultToolkit();
    InputStream in = null;
    long bytesToRead = 0;
    try {
      URL url = new URL("jar:"+codebase.toExternalForm()+archive+"!"+fileName);
      JarURLConnection juc = (JarURLConnection)url.openConnection();

      JarFile jf = juc.getJarFile();
      JarEntry imgFile = (JarEntry)jf.getEntry(fileName.substring(1));
      bytesToRead = imgFile.getSize();

      in = juc.getInputStream();
      if (in == null)
        return null;
    } catch (Exception ex) {
      //ex.printStackTrace();
      return null;
    }

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try{
      tn = new byte[1];
      long count = 0;
      while(in.available() > 0 && count < bytesToRead) {
        in.read(tn);
        bos.write(tn);
        count++;
      }

      image = toolkit.createImage(bos.toByteArray());
    } catch(Exception ex)
    {
      System.out.println( ex +" getting resource " +fileName );
      return null;
    }
    return image;
  }

}





/**
 * A borderless button with 2 images: normal and pressed.
 */
class ImageJButton extends JButton
{
  public ImageJButton(Icon img, Icon pressedImg)
  {
    setIcon(img);
    if (pressedImg != null)
      setPressedIcon(pressedImg);
    setBorder(BorderFactory.createLineBorder(Color.white, 0));
    setBorderPainted(false);
    this.setContentAreaFilled(false);
    this.setFocusPainted(false);
    setMargin(new Insets(0, 0, 0, 0));

  }
}
