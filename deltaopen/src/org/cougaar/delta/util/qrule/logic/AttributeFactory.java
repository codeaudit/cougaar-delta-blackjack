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

package org.cougaar.delta.util.qrule.logic;

import org.cougaar.delta.util.qrule.*;
import java.util.*;

public class AttributeFactory {
  private static AttributeFactory instance = null;

  private AttributeFactory () {
  }

  public static AttributeFactory getInstance () {
    if (instance == null)
      instance = new AttributeFactory();
    return instance;
  }

  public AttributeRange makeRangeFor (
      QRuleAccessorOperand attribute, Vector conds)
  {
    String name = attribute.getInternalName();
    String type = attribute.getUiType();
    /*
      for the nonce, we'll simply separate things out by their types

      currently:
      -- all Integer types are treated alike
      -- all String types are treated alike

      The following types are supported:
      -- boolean
      -- String/String(*)/DODAACType/PrimeVendorType/HazMatCodeType/UIType/AdviceCodeType/DebarredStatus/DeliveryDaysCode
      -- Integer/Integer(*)
      -- Float/Currency
      -- CustomerType/ItemType
      -- PhysicalAddressType
    */

    AttributeRange range = null;

    if (type.startsWith("String"))
    {
      range = makeStringRange();
    }
    else if (type.equals("boolean")) {
      range = makeBooleanRange();
    }
    else if (type.startsWith("Integer")) {
      range = makeIntegerRange();
    }
    else if (type.equals("Float") || type.equals("Currency")) {
      range = makeDecimalRange();
    }
    else if (type.equals("Date")) {
      range = makeDateRange();
    }
    else {
      System.out.println("Unsupported type \"" + type + "\"!");
    }

    if (range != null)
      range.assumeConstraints(attribute, conds);

    return range;
  }

  private static StringRange makeStringRange () {
    StringRange range = new StringRange();
    range.setOperatorNames("eq", "neq", "isMember", "isNotMember");
    return range;
  }

  private static BooleanRange makeBooleanRange () {
    BooleanRange range = new BooleanRange();
    range.setOperatorNames("eq", null);
    return range;
  }

  private static IntegerRange makeIntegerRange () {
    IntegerRange range = new IntegerRange();
    range.setOperatorNames("=", "<>", ">", "<", ">=", "<=");
    return range;
  }

  private static DecimalRange makeDecimalRange () {
    DecimalRange range = new DecimalRange();
    range.setOperatorNames("=", "<>", ">", "<", ">=", "<=");
    return range;
  }

  private static DateRange makeDateRange () {
    DateRange range = new DateRange();
    range.setOperatorNames("dateSameDay", "dateAfter", "dateBefore");
    return range;
  }


}
