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

import javax.swing.text.*;
import javax.swing.*;
import java.awt.*;

/**
 *  RestrictedJTextField is a subclass of JTextField which restricts input to
 *  a specified set of characters.  The allowable characters are treated
 *  normally, and all others are ignored.  The set of allowable characters is
 *  represented as a string which, if null, places no restriction on the input.
 */
public class RestrictedJTextField extends JTextField {
  // the set of allowable characters in this JTextField instance
  protected String charSet = null;
  protected boolean inverseSet = false;
  // the set of allowable initial characters in this JTextField instance
  protected String initialsSet = null;
  protected boolean inverseInitials = false;
  // a restriction on the length of the field, if any
  protected int maxLength = -1;
  protected boolean restrictLength = false;
  // should all input be capitalized
  protected boolean capitalize = false;

  /**
   *  Construct this RestrictedJTextField with the given length and restricted
   *  character set.
   *  @param length the length of the field
   *  @param set a String containing all characters allowed in this field
   */
  public RestrictedJTextField (int length, String set) {
    super(length);
    charSet = set;
  }

  /**
   *  Construct this Restricted JTextField with the given restricted character
   *  set.
   *  @param set a String containing all characters allowed in this field
   */
  public RestrictedJTextField (String set) {
    super();
    charSet = set;
  }

  /**
   *  Construct this RestrictedJTextField with the given length and
   *  unrestricted character set
   *  @param length the length of the field
   */
  public RestrictedJTextField (int length) {
    super(length);
  }

  /**
   *  Construct this RestrictedJTextField with unrestricted character set and
   *  no specified length.
   */
  public RestrictedJTextField () {
    super();
  }

  /**
   *  Configure this RestrictedJTextField instance in ths the same fashion as
   *  another existing instance.
   *  @param field the other field to mimic
   */
  public void mimic (RestrictedJTextField field) {
    setInitialsSet(field.initialsSet);
    setInverseInitials(field.inverseInitials);
    setCharSet(field.charSet);
    setInverseSet(field.inverseSet);
    setCapitalize(field.capitalize);
    setMaxLength(field.maxLength);
  }

  /**
   *  Specify the set of characters allowable as input for this field
   *  @param newSet the new character set
   */
  public void setCharSet (String newSet) {
    charSet = newSet;
  }

  /**
   *  Retrieve the set of characters being accepted as input by this component
   *  @return the character set
   */
  public String getCharSet () {
    return charSet;
  }

  /**
   *  Specify a set of characters to which the initial character in the field
   *  must belong.
   *  @param initSet the new permissible set of initial characters
   */
  public void setInitialsSet (String initSet) {
    initialsSet = initSet;
  }

  /**
   *  Retrieve the set of characters currently being accepted by this field as
   *  the initial character.
   *  @return the set of permitted initials
   */
  public String getInitialsSet () {
    return initialsSet;
  }

  /**
   *  Specify whether the character set membership function is inverted in
   *  comparisons
   *  @param inv the new value of the inversion flag
   */
  public void setInverseSet (boolean inv) {
    inverseSet = inv;
  }

  /**
   *  Discover whether the character set membership function is being inverted
   *  @return true if the set is inverted
   */
  public boolean getInverseSet () {
    return inverseSet;
  }

  /**
   *  Specify whether the initial character set membership function is inverted
   *  in comparisons
   *  @param inv the new value of the initials inversion flag
   */
  public void setInverseInitials (boolean inv) {
    inverseInitials = inv;
  }

  /**
   *  Discover whether the initial character set membership function is being
   *  inverted.
   *  @return true if the set of initials is inverted
   */
  public boolean getInverseInitials () {
    return inverseInitials;
  }

  /**
   *  Specify whether input letters should automatically be converted to upper
   *  case.  If so, the transformation will occur before comparison to any
   *  character sets.
   *  @param c true if the input should be capitalized
   */
  public void setCapitalize (boolean c) {
    capitalize = c;
  }

  /**
   *  Discover whether input is being converted to upper case by this element.
   *  @return true if input is converted to capitals
   */
  public boolean getCapitalize () {
    return capitalize;
  }

  /**
   *  Place a maximum length restriction on this element's text.  If the text
   *  is longer than the new maximum, then its tail is truncated to accomodate
   *  the restriction.
   *  <br><br>
   *  If the argument is negative, then the length restrictions is removed,
   *  which is equivalent to calling setNoMaxLength()
   *  @param n the new maximal length
   */
  public void setMaxLength (int n) {
    if (n < 0) {
      restrictLength = false;
    }
    else {
      maxLength = n;
      restrictLength = true;
      if (getText().length() > maxLength)
        setText(getText().substring(0, maxLength));
    }
  }

  /**
   *  Find out what length restriction has been placed on the text for this
   *  element.
   *  @return the maximum text length
   */
  public int getMaxLength () {
    return maxLength;
  }

  /**
   *  Remove the restriction, if any, that has been placed on the length of this
   *  element's text.
   */
  public void setNoMaxLength () {
    restrictLength = false;
    maxLength = -1;
  }

  /**
   *  Return a Document instance for this JTextField instance.  The document
   *  is a member of the inner class CharRestrictedDocument, q.v.
   *  @return a Document
   */
  protected Document createDefaultModel() {
    return new CharRestrictedDocument();
  }

  /**
   *  This class is a subclass of PlainDocument which filters input, allowing
   *  only members of a specified set to be entered, and ignoring all others.
   *  The allowable set is actually specified by the charSet variable of the
   *  containing class, RestrictedJTextField, through the magic of inner
   *  classes.
   */
  protected class CharRestrictedDocument extends PlainDocument {
    /**
     *  Override the default behavior when inserting a String.  In this case,
     *  <ul>
     *    <li>apply capitalization transform, if called for</li>
     *    <li>examine each character individually for membership in the charSet
     *      String, and attempt to add only those which are members</li>
     *    <li>add the approved characters, subject to length restrictions</li>
     *  </ul>
     *  @param offs the offset within the text that the insertion is to occur
     *  @param str the text that would like to be inserted
     *  @param a Whatever
     */
    public void insertString (int offs, String str, AttributeSet a)
        throws BadLocationException
    {
      if (str == null || str.equals("")) {
        return;
      }
      // capitalize the string, if necessary
      String t_str = (capitalize ? str.toUpperCase() : str);
      // now ferret the characters that don't belong
      StringBuffer buf = new StringBuffer();
      char[] chars = t_str.toCharArray();
      int correction = 0;
      for (int i = 0; i < chars.length; i++) {
        String s = String.valueOf(chars[i]);
        if (offs + i - correction == 0 && initialsSet != null) {
          // offs + i - correction is zero exactly when the character under
          // consideration is going to be inserted at the beginning of the Text
          if (!((initialsSet.indexOf(s) > -1) == inverseInitials))
            buf.append(s);
          else
            correction++;
        }
        else if (charSet == null || !((charSet.indexOf(s) > -1) == inverseSet))
          buf.append(s);
      }
      // At this point, buf contains the characters to be inserted.  Finally,
      // we consider length constraints.  For the nonce, we do this by blindly
      // inserting the text and then truncating the whole to fit, if necessary.
      super.insertString(offs, buf.toString(), a);
      if (restrictLength) {
        int c = getCaretPosition();
        if (c > maxLength)
          c = maxLength;
        setMaxLength(maxLength);
        setCaretPosition(c);
      }
    }

    public void remove (int offs, int num)
        throws BadLocationException
    {
      if (num == 0)
        return;
      if (initialsSet == null)
        super.remove(offs, num);
      else if (offs == 0 && num < getLength()) {
        String nextChar = this.getText(offs + num, 1);
        if (!((initialsSet.indexOf(nextChar) > -1) == inverseInitials))
          super.remove(offs, num);
      }
      else {
        super.remove(offs, num);
      }
    }
  }

  // - - - - - - - Testing Scaffolding - - - - - - - - - - - - - - - - - - - - -

  public static void main (String[] argv) {
    Font ruleElementFont = new Font("Arial", Font.PLAIN, 14);
    JFrame frame = new JFrame();
    frame.setSize(400, 400);
    frame.getContentPane().setLayout(new FlowLayout());
    RestrictedJTextField fred = new RestrictedJTextField(20,
      "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_");
    fred.setInitialsSet("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
    fred.setInverseSet(false);
    // fred.setCapitalize(true);
    fred.setMaxLength(20);
    fred.setFont(ruleElementFont);
    frame.getContentPane().add(fred);
    frame.validate();
    frame.setVisible(true);
  }
}
