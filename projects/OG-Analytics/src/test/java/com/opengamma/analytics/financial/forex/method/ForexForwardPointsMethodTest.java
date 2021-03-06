/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test related to the method for Forex transaction by discounting on each payment.
 * @deprecated This class tests deprecated code
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class ForexForwardPointsMethodTest {

  private static final YieldCurveBundle CURVES = TestsDataSetsForex.createCurvesForex();
  private static final String[] CURVE_NAMES = TestsDataSetsForex.curveNames();

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("CAL");
  private static final ZonedDateTime PAYMENT_DATE = DateUtils.getUTCDate(2013, 6, 26);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 2, 12);
  private static final ZonedDateTime SPOT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, 2, CALENDAR);
  private static final IborIndex USDLIBOR3M = IndexIborMaster.getInstance().getIndex("USDLIBOR3M");

  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE, CURVE_NAMES);

  private static final double[] MARKET_QUOTES_PTS = new double[] {0.000001, 0.000002, 0.0004, 0.0009, 0.0015, 0.0020, 0.0036, 0.0050 };
  private static final int NB_MARKET_QUOTES = MARKET_QUOTES_PTS.length;
  private static final Period[] MARKET_QUOTES_TENOR = new Period[] {Period.ofDays(-2), Period.ofDays(-1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3),
    Period.ofMonths(6), Period.ofMonths(9), Period.ofMonths(12) };
  private static final ZonedDateTime[] MARKET_QUOTES_DATE = new ZonedDateTime[NB_MARKET_QUOTES];
  private static final double[] MARKET_QUOTES_TIME = new double[NB_MARKET_QUOTES];
  static {
    for (int loopt = 0; loopt < NB_MARKET_QUOTES; loopt++) {
      MARKET_QUOTES_DATE[loopt] = ScheduleCalculator.getAdjustedDate(SPOT_DATE, MARKET_QUOTES_TENOR[loopt], USDLIBOR3M, CALENDAR);
      MARKET_QUOTES_TIME[loopt] = TimeCalculator.getTimeBetween(REFERENCE_DATE, MARKET_QUOTES_DATE[loopt]);
    }
  }
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final InterpolatedDoublesCurve FWD_PTS = new InterpolatedDoublesCurve(MARKET_QUOTES_TIME, MARKET_QUOTES_PTS, LINEAR_FLAT, true);

  private static final ForexForwardPointsMethod METHOD_FX_PTS = ForexForwardPointsMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2; // one cent out of 100m
  private static final double TOLERANCE_PV_DELTA = 1.0E-0;

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValue() {
    final double fxRate = CURVES.getFxRates().getFxRate(CUR_1, CUR_2);
    final double payTime = FX.getPaymentTime();
    final double fwdPts = FWD_PTS.getYValue(payTime);
    final double amount1 = NOMINAL_1;
    final double amount2 = -NOMINAL_1 * FX_RATE;
    final double df2 = CURVES.getCurve(CURVE_NAMES[1]).getDiscountFactor(payTime);
    final double pvExpected = df2 * (amount2 + amount1 * (fxRate + fwdPts));
    final MultipleCurrencyAmount pvComputed = METHOD_FX_PTS.presentValue(FX, CURVES, FWD_PTS);
    assertEquals("ForexForwardPointsMethod: presentValue", 1, pvComputed.size());
    assertEquals("ForexForwardPointsMethod: presentValue", pvExpected, pvComputed.getAmount(CUR_2), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the currency exposure computation.
   */
  public void currencyExposure() {
    final double fxRate = CURVES.getFxRates().getFxRate(CUR_1, CUR_2);
    final double payTime = FX.getPaymentTime();
    final double fwdPts = FWD_PTS.getYValue(payTime);
    final double amount1 = NOMINAL_1;
    final double amount2 = -NOMINAL_1 * FX_RATE;
    final double df2 = CURVES.getCurve(CURVE_NAMES[1]).getDiscountFactor(payTime);
    final double ce1 = amount1 * df2 * (1.0d + fwdPts / fxRate);
    final double ce2 = amount2 * df2;
    MultipleCurrencyAmount ceExpected = MultipleCurrencyAmount.of(CUR_1, ce1);
    ceExpected = ceExpected.plus(CUR_2, ce2);
    final MultipleCurrencyAmount ceComputed = METHOD_FX_PTS.currencyExposure(FX, CURVES, FWD_PTS);
    assertEquals("ForexForwardPointsMethod: presentValue", 2, ceComputed.size());
    assertEquals("ForexForwardPointsMethod: presentValue", ceExpected.getAmount(CUR_1), ceComputed.getAmount(CUR_1), TOLERANCE_PV);
    assertEquals("ForexForwardPointsMethod: presentValue", ceExpected.getAmount(CUR_2), ceComputed.getAmount(CUR_2), TOLERANCE_PV);
    final MultipleCurrencyAmount pvComputed = METHOD_FX_PTS.presentValue(FX, CURVES, FWD_PTS);
    assertEquals("ForexForwardPointsMethod: presentValue", CURVES.getFxRates().convert(ceComputed, CUR_2).getAmount(), pvComputed.getAmount(CUR_2), TOLERANCE_PV);
  }

  @Test
  /**
   * Tests the present value sensitivity to forward points.
   */
  public void presentValueForwardPointsSensitivity() {
    final double[] fpsComputed = METHOD_FX_PTS.presentValueForwardPointsSensitivity(FX, CURVES, FWD_PTS);
    final MultipleCurrencyAmount pv = METHOD_FX_PTS.presentValue(FX, CURVES, FWD_PTS);
    final double[] fpsExpected = new double[NB_MARKET_QUOTES];
    final double shift = 1.0E-6;
    for (int loopt = 0; loopt < NB_MARKET_QUOTES; loopt++) {
      final double[] mqpShift = MARKET_QUOTES_PTS.clone();
      mqpShift[loopt] += shift;
      final InterpolatedDoublesCurve fwdPtsShift = new InterpolatedDoublesCurve(MARKET_QUOTES_TIME, mqpShift, LINEAR_FLAT, true);
      final MultipleCurrencyAmount pvShift = METHOD_FX_PTS.presentValue(FX, CURVES, fwdPtsShift);
      fpsExpected[loopt] = (pvShift.getAmount(CUR_2) - pv.getAmount(CUR_2)) / shift;
      assertEquals("ForexForwardPointsMethod: presentValueForwardPointsSensitivity - node " + loopt, fpsExpected[loopt], fpsComputed[loopt], TOLERANCE_PV_DELTA);
    }
  }

  //TODO
  //  @Test
  //  public void presentValueCurveSensitivity() {
  //    final MultipleCurrencyInterestRateCurveSensitivity pvcs = METHOD_FX_PTS.presentValueCurveSensitivity(FX, CURVES, FWD_PTS);
  //    int t = 0;
  //    t++;
  //  }

}
