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
package org.cougaar.delta.util.qrule;

import java.util.Vector;
import java.io.Serializable;

/**
 * This is that class to extend in order to describe candidate objects.
 * Candidates are the things which are qualified by the rules. Some examples
 * might be things like bids for a construction project, people being considered
 * for a job, or books for selection as "book of the month".
 */

public class QualifiableCandidate implements Serializable{

  private int qualificationLevel =0;
  private String reason;
  private String qualification = QRule.NEUTRAL;
  private Vector qualifications = new Vector();

  public void addQualification(String ruleName) {
    qualifications.add(ruleName);
  }
  public int getQualificationLevel() {
    return qualificationLevel;
  }
  public void setQualificationLevel(int level) {
    qualificationLevel = level;
  }
  public String getQualification() {
    return qualification;
  }
  public void setQualification(String qual) {
    qualification = qual;
  }
  public String getReason() {
    return reason;
  }
  public void setReason(String reason) {
    this.reason = reason;
  }
  public Vector getQualifications(){
    return qualifications;
  }

}
