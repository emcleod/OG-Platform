/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorCMSSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to CapFloorCMSSpreadDefinition construction.
 */
@Test(groups = TestGroup.UNIT)
public class CapFloorCMSSpreadDefinitionTest {

  //Swaps
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 17);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final boolean FIXED_IS_PAYER = true; // Irrelevant for the underlying
  private static final double RATE = 0.0; // Irrelevant for the underlying
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  // Swap 10Y
  private static final Period ANNUITY_TENOR_1 = Period.ofYears(10);
  private static final IndexSwap CMS_INDEX_1 = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR_1, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_1 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_1, 1.0, RATE, FIXED_IS_PAYER, CALENDAR);
  // Swap 2Y
  private static final Period ANNUITY_TENOR_2 = Period.ofYears(2);
  private static final IndexSwap CMS_INDEX_2 = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR_2, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_2 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_2, 1.0, RATE, FIXED_IS_PAYER, CALENDAR);
  // CMS spread coupon
  private static final double NOTIONAL = 10000000;
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtils.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtils.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtils.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCounts.ACT_360;
  private static final double PAYMENT_ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double STRIKE = 0.0050; // 50 bps
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSSpreadDefinition CMS_SPREAD_DEFINITION = new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
      FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP, CALENDAR, CALENDAR);

  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_1_NAME = "Forward 1";
  private static final String[] CURVES_2_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_1_NAME };
  private static final DayCount ACT_ACT = DayCounts.ACT_ACT_ISDA;
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE.toLocalDate(), LocalTime.MIDNIGHT), ZoneOffset.UTC);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, SWAP_DEFINITION_1.getFixedLeg().getNthPayment(0).getAccrualStartDate());

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CapFloorCMSSpreadDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2,
        CMS_INDEX_2, STRIKE, IS_CAP, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new CapFloorCMSSpreadDefinition(CUR, null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2,
        STRIKE, IS_CAP, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStart() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, null, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE,
        IS_CAP, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEnd() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, null, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE,
        IS_CAP, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, null, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2,
        STRIKE, IS_CAP, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSwap1() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE,
        IS_CAP, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex1() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, null, SWAP_DEFINITION_2, CMS_INDEX_2,
        STRIKE, IS_CAP, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSwap2() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, null, CMS_INDEX_2, STRIKE,
        IS_CAP, CALENDAR, CALENDAR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex2() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, null,
        STRIKE, IS_CAP, CALENDAR, CALENDAR);
  }

  @Test
  public void testGetter() {
    assertEquals(SWAP_DEFINITION_1, CMS_SPREAD_DEFINITION.getUnderlyingSwap1());
    assertEquals(CMS_INDEX_1, CMS_SPREAD_DEFINITION.getCmsIndex1());
    assertEquals(SWAP_DEFINITION_2, CMS_SPREAD_DEFINITION.getUnderlyingSwap2());
    assertEquals(CMS_INDEX_2, CMS_SPREAD_DEFINITION.getCmsIndex2());
    assertEquals(STRIKE, CMS_SPREAD_DEFINITION.getStrike(), 1E-10);
    assertEquals(IS_CAP, CMS_SPREAD_DEFINITION.isCap());
  }

  @Test
  public void testEqualHash() {
    final CapFloorCMSSpreadDefinition newCMSSpread = new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
        SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP, CALENDAR, CALENDAR);
    assertEquals(newCMSSpread.equals(CMS_SPREAD_DEFINITION), true);
    assertEquals(newCMSSpread.hashCode() == CMS_SPREAD_DEFINITION.hashCode(), true);
    final Currency newCur = Currency.USD;
    final CapFloorCMSSpreadDefinition cmsSpreadCur = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
        SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP, CALENDAR, CALENDAR);
    assertEquals(cmsSpreadCur.equals(CMS_SPREAD_DEFINITION), false);
    CapFloorCMSSpreadDefinition cmsSpreadModified;
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_2, CMS_INDEX_1,
        SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP, CALENDAR, CALENDAR);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_2,
        SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP, CALENDAR, CALENDAR);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1,
        SWAP_DEFINITION_1, CMS_INDEX_2, STRIKE, IS_CAP, CALENDAR, CALENDAR);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1,
        SWAP_DEFINITION_2, CMS_INDEX_1, STRIKE, IS_CAP, CALENDAR, CALENDAR);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1,
        SWAP_DEFINITION_2, CMS_INDEX_1, STRIKE + 0.0001, IS_CAP, CALENDAR, CALENDAR);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1,
        SWAP_DEFINITION_2, CMS_INDEX_1, STRIKE, !IS_CAP, CALENDAR, CALENDAR);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testToDerivativeDeprecated() {
    final SwapFixedCoupon<? extends Payment> swap1 = SWAP_DEFINITION_1.toDerivative(REFERENCE_DATE, CURVES_2_NAME);
    final SwapFixedCoupon<? extends Payment> swap2 = SWAP_DEFINITION_2.toDerivative(REFERENCE_DATE, CURVES_2_NAME);
    final CapFloorCMSSpread cmsSpread = (CapFloorCMSSpread) CMS_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_2_NAME);
    assertEquals(swap1, cmsSpread.getUnderlyingSwap1());
    assertEquals(swap2, cmsSpread.getUnderlyingSwap2());
    final CapFloorCMSSpread cmsSpreadExpected = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, swap1, CMS_INDEX_1, swap2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE, IS_CAP, FUNDING_CURVE_NAME);
    assertEquals("CMS Spread to derivatives", cmsSpreadExpected, cmsSpread);
  }

  @Test
  public void testToDerivative() {
    final SwapFixedCoupon<? extends Payment> swap1 = SWAP_DEFINITION_1.toDerivative(REFERENCE_DATE);
    final SwapFixedCoupon<? extends Payment> swap2 = SWAP_DEFINITION_2.toDerivative(REFERENCE_DATE);
    final CapFloorCMSSpread cmsSpread = (CapFloorCMSSpread) CMS_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE);
    assertEquals(swap1, cmsSpread.getUnderlyingSwap1());
    assertEquals(swap2, cmsSpread.getUnderlyingSwap2());
    final CapFloorCMSSpread cmsSpreadExpected = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, swap1, CMS_INDEX_1, swap2, CMS_INDEX_2, SETTLEMENT_TIME,
        STRIKE, IS_CAP);
    assertEquals("CMS Spread to derivatives", cmsSpreadExpected, cmsSpread);
  }
}
