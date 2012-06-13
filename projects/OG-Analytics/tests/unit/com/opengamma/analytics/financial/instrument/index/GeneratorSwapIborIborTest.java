/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

public class GeneratorSwapIborIborTest {

  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final IndexIborTestsMaster IBOR_MASTER = IndexIborTestsMaster.getInstance();
  private static final IborIndex USDLIBOR3M = IBOR_MASTER.getIndex("USDLIBOR3M", NYC);
  private static final IborIndex USDLIBOR6M = IBOR_MASTER.getIndex("USDLIBOR6M", NYC);
  private static final GeneratorSwapIborIbor USDLIBOR3MLIBOR6M = new GeneratorSwapIborIbor("USDLIBOR3MLIBOR6M", USDLIBOR3M, USDLIBOR6M);

  @Test
  /**
   * Tests the getter for the swap generator.
   */
  public void getter() {
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR3M, USDLIBOR3MLIBOR6M.getIborIndex1());
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR6M, USDLIBOR3MLIBOR6M.getIborIndex2());
    assertTrue("GeneratorSwapIborIbor: getter", USDLIBOR3MLIBOR6M.getName().equals("USDLIBOR3MLIBOR6M"));
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR6M.getBusinessDayConvention(), USDLIBOR3MLIBOR6M.getBusinessDayConvention());
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR6M.getSpotLag(), USDLIBOR3MLIBOR6M.getSpotLag());
    assertTrue("GeneratorSwapIborIbor: getter", USDLIBOR6M.isEndOfMonth() == USDLIBOR3MLIBOR6M.isEndOfMonth());
  }

  @Test
  /**
   * Tests the constructor with business day convention and end-of-month.
   */
  public void constructor() {
    GeneratorSwapIborIbor generator2 = new GeneratorSwapIborIbor("Generator 2", USDLIBOR3M, USDLIBOR6M, BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), false, 1);
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR3M, generator2.getIborIndex1());
    assertEquals("GeneratorSwapIborIbor: getter", USDLIBOR6M, generator2.getIborIndex2());
    assertTrue("GeneratorSwapIborIbor: getter", generator2.getName().equals("Generator 2"));
    assertEquals("GeneratorSwapIborIbor: getter", BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"), generator2.getBusinessDayConvention());
    assertTrue("GeneratorSwapIborIbor: getter", generator2.isEndOfMonth() == false);
    assertEquals("GeneratorSwapIborIbor: getter", generator2.getSpotLag(), 1);
  }

}
