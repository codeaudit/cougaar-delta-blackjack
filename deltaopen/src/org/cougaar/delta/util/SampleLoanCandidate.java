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

import org.cougaar.delta.util.qrule.QualifiableCandidate;


public class SampleLoanCandidate extends QualifiableCandidate {

  private float totalAssets = 0;
  private float totalDebt = 0;
  private float loanAmount;
  private float liquidAssets = 0;
  private float monthlyIncome = 0;
  private int loanYears;
  private boolean recentDefault = true;
  private String name;

  public SampleLoanCandidate(String name, float amount, int years) {
    loanAmount = amount;
    loanYears = years;
    this.name =name;
  }

  public String getName() {
    return name;
  }
  public void setTotalAssets(float amount) {
    totalAssets = amount;
  }
  public void setTotalDebt(float amount) {
    totalDebt = amount;
  }
  public void setLiquidAssets(float amount) {
    liquidAssets = amount;
  }
  public void setMonthlyIncome(float amount) {
    monthlyIncome = amount;
  }
  public void setRecentDefault(boolean hasDefault) {
    recentDefault = hasDefault;
  }

  public float getTotalAssets() {
    return totalAssets;
  }
  public float getTotalDebt() {
    return totalDebt;
  }
  public float getLiquidAssets() {
    return liquidAssets;
  }
  public float getMonthlyIncome() {
    return monthlyIncome;
  }
  public boolean hasRecentDefault() {
    return recentDefault;
  }
  public float getMonthlyPayment() {
    return (loanAmount/(loanYears*12));
  }
  public float getLoanAmount() {
    return loanAmount;
  }
  public int getLoanYears() {
    return loanYears;
  }

}
