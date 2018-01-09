/**
 * 
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.FUNCTION;
import static com.opengamma.engine.value.ValueRequirementNames.CURVE_BUNDLE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.math.curve.AddCurveSpreadFunction;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.SpreadDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;

public class CurveBundleSingleCurveParallelShiftTest {
  private static final String OIS_CURVE_NAME = "USD OIS";
  private static final String LIBOR_CURVE_NAME = "USD 3m Libor";
  private static final Currency CURRENCY = Currency.USD;
  private static final IborIndex IBOR_INDEX = new IborIndex(CURRENCY, Period.ofMonths(3), 0, DayCounts.ACT_360, BusinessDayConventions.FOLLOWING, false, "ibor");
  private static final IndexON OVERNIGHT_INDEX = new IndexON("overnight", CURRENCY, DayCounts.ACT_360, 0);
  private static final YieldCurve LIBOR_CURVE = YieldCurve.from(InterpolatedDoublesCurve.from(new double[] {1, 2, 3 }, new double[] {0.02, 0.04, 0.06 }, Interpolator1DFactory.LINEAR_INSTANCE,
      LIBOR_CURVE_NAME));
  private static final YieldCurve OIS_CURVE = YieldCurve.from(InterpolatedDoublesCurve.from(new double[] {1, 2, 3 }, new double[] {0.01, 0.02, 0.03 }, Interpolator1DFactory.LINEAR_INSTANCE,
      OIS_CURVE_NAME));
  private static final FunctionExecutionContext EXECUTION_CONTEXT = new FunctionExecutionContext();
  private static final MulticurveProviderDiscount MCPD;
  private static final ValueSpecification VALUE_SPEC;

  static {
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
    discountingCurves.put(CURRENCY, OIS_CURVE);
    final Map<IborIndex, YieldAndDiscountCurve> iborCurves = new LinkedHashMap<>();
    iborCurves.put(IBOR_INDEX, LIBOR_CURVE);
    final Map<IndexON, YieldAndDiscountCurve> overnightCurves = new LinkedHashMap<>();
    overnightCurves.put(OVERNIGHT_INDEX, OIS_CURVE);
    MCPD = new MulticurveProviderDiscount(discountingCurves, iborCurves, overnightCurves, new FXMatrix());
    final ValueProperties properties = ValueProperties.builder()
        .with(FUNCTION, "MultiCurveDiscountingFunction")
        .with(CURVE_CONSTRUCTION_CONFIG, "USD Rates")
        .with(CURVE, OIS_CURVE_NAME, LIBOR_CURVE_NAME)
        .get();
    VALUE_SPEC = new ValueSpecification(CURVE_BUNDLE, ComputationTargetSpecification.NULL, properties);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShiftType() {
    new CurveBundleSingleCurveParallelShift(null, 0.001, "USD OIS");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurveName() {
    new CurveBundleSingleCurveParallelShift(ScenarioShiftType.ABSOLUTE, 0.001, null);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testWrongBundleType() {
    final IssuerProviderDiscount bundle = new IssuerProviderDiscount(MCPD);
    final StructureManipulator<ParameterProviderInterface> manipulator = new CurveBundleSingleCurveParallelShift(ScenarioShiftType.ABSOLUTE, 0, "USD OIS");
    manipulator.execute(bundle, VALUE_SPEC, EXECUTION_CONTEXT);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testCurveNotFound() {
    final StructureManipulator<ParameterProviderInterface> manipulator = new CurveBundleSingleCurveParallelShift(ScenarioShiftType.ABSOLUTE, 0, "Not in bundle");
    manipulator.execute(MCPD, VALUE_SPEC, EXECUTION_CONTEXT);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testNotYieldCurve() {
    final MulticurveProviderDiscount bundle = MCPD.copy();
    bundle.replaceCurve(CURRENCY, DiscountCurve.from(InterpolatedDoublesCurve.from(new double[] {1, 2, 3 }, new double[] {0.99, 0.98, 0.97 }, Interpolator1DFactory.LINEAR_INSTANCE)));
    final StructureManipulator<ParameterProviderInterface> manipulator = new CurveBundleSingleCurveParallelShift(ScenarioShiftType.ABSOLUTE, 0, "Not in bundle");
    manipulator.execute(bundle, VALUE_SPEC, EXECUTION_CONTEXT);
  }

  @Test
  public void testAdditiveShiftForOISCurve() {
    final double shift = Math.random();
    final StructureManipulator<ParameterProviderInterface> manipulator = new CurveBundleSingleCurveParallelShift(ScenarioShiftType.ABSOLUTE, shift, OIS_CURVE_NAME);
    final ParameterProviderInterface result = manipulator.execute(MCPD, VALUE_SPEC, EXECUTION_CONTEXT);
    assertTrue(result instanceof MulticurveProviderDiscount);
    final MulticurveProviderDiscount shifted = (MulticurveProviderDiscount) result;
    final MulticurveProviderDiscount expected = MCPD.copy();
    final YieldCurve curveWithZeroShift = new YieldCurve(OIS_CURVE_NAME + "_WithParallelShift", SpreadDoublesCurve.from(AddCurveSpreadFunction.getInstance(),
        OIS_CURVE_NAME + "_WithParallelShift", OIS_CURVE.getCurve(), ConstantDoublesCurve.from(shift)));
    expected.replaceCurve(CURRENCY, curveWithZeroShift);
    expected.replaceCurve(OVERNIGHT_INDEX, curveWithZeroShift);
    assertNotSame(MCPD, shifted);
    assertEquals(MCPD.getFxRates(), shifted.getFxRates());
    assertEquals(MCPD.getForwardIborCurves(), shifted.getForwardIborCurves());
    assertFalse(MCPD.getDiscountingCurves().equals(shifted.getDiscountingCurves()));
    assertFalse(MCPD.getForwardONCurves().equals(shifted.getForwardONCurves()));
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = shifted.getDiscountingCurves();
    assertEquals(1, discountingCurves.size());
    assertEquals(OIS_CURVE_NAME + "_WithParallelShift", Iterables.getOnlyElement(discountingCurves.values()).getName());
    final YieldAndDiscountCurve shiftedDiscountingCurve = Iterables.getOnlyElement(discountingCurves.values());
    assertTrue(shiftedDiscountingCurve instanceof YieldCurve);
    final YieldCurve shiftedDiscountingYieldCurve = (YieldCurve) shiftedDiscountingCurve;
    assertTrue(shiftedDiscountingYieldCurve.getCurve() instanceof SpreadDoublesCurve);
    final SpreadDoublesCurve spreadCurve = (SpreadDoublesCurve) shiftedDiscountingYieldCurve.getCurve();
    assertEquals(2, spreadCurve.getUnderlyingCurves().length);
    assertEquals(OIS_CURVE.getCurve(), spreadCurve.getUnderlyingCurves()[0]);
    assertTrue(spreadCurve.getUnderlyingCurves()[1] instanceof ConstantDoublesCurve);
    assertEquals(shift, ((ConstantDoublesCurve) spreadCurve.getUnderlyingCurves()[1]).getYValue(1.), 2e-16);
    final Map<IndexON, YieldAndDiscountCurve> overnightCurves = shifted.getForwardONCurves();
    assertEquals(1, overnightCurves.size());
    final YieldAndDiscountCurve shiftedOvernightCurve = Iterables.getOnlyElement(overnightCurves.values());
    assertEquals(shiftedDiscountingCurve, shiftedOvernightCurve);
  }

  @Test
  public void testMultiplicativeShiftForOISCurve() {
    final double shift = Math.random();
    final StructureManipulator<ParameterProviderInterface> manipulator = new CurveBundleSingleCurveParallelShift(ScenarioShiftType.RELATIVE, shift, OIS_CURVE_NAME);
    final ParameterProviderInterface result = manipulator.execute(MCPD, VALUE_SPEC, EXECUTION_CONTEXT);
    assertTrue(result instanceof MulticurveProviderDiscount);
    final MulticurveProviderDiscount shifted = (MulticurveProviderDiscount) result;
    final MulticurveProviderDiscount expected = MCPD.copy();
    final YieldCurve curveWithZeroShift = new YieldCurve(OIS_CURVE_NAME + "_WithParallelShift", SpreadDoublesCurve.from(AddCurveSpreadFunction.getInstance(),
        OIS_CURVE_NAME + "_WithParallelShift", OIS_CURVE.getCurve(), ConstantDoublesCurve.from(shift)));
    expected.replaceCurve(CURRENCY, curveWithZeroShift);
    expected.replaceCurve(OVERNIGHT_INDEX, curveWithZeroShift);
    assertNotSame(MCPD, shifted);
    assertEquals(MCPD.getFxRates(), shifted.getFxRates());
    assertEquals(MCPD.getForwardIborCurves(), shifted.getForwardIborCurves());
    assertFalse(MCPD.getDiscountingCurves().equals(shifted.getDiscountingCurves()));
    assertFalse(MCPD.getForwardONCurves().equals(shifted.getForwardONCurves()));
    final Map<Currency, YieldAndDiscountCurve> discountingCurves = shifted.getDiscountingCurves();
    assertEquals(1, discountingCurves.size());
    assertEquals(OIS_CURVE_NAME + "_WithParallelShift", Iterables.getOnlyElement(discountingCurves.values()).getName());
    final YieldAndDiscountCurve shiftedDiscountingCurve = Iterables.getOnlyElement(discountingCurves.values());
    assertTrue(shiftedDiscountingCurve instanceof YieldCurve);
    final YieldCurve shiftedDiscountingYieldCurve = (YieldCurve) shiftedDiscountingCurve;
    assertTrue(shiftedDiscountingYieldCurve.getCurve() instanceof SpreadDoublesCurve);
    final SpreadDoublesCurve spreadCurve = (SpreadDoublesCurve) shiftedDiscountingYieldCurve.getCurve();
    assertEquals(2, spreadCurve.getUnderlyingCurves().length);
    assertEquals(OIS_CURVE.getCurve(), spreadCurve.getUnderlyingCurves()[0]);
    assertTrue(spreadCurve.getUnderlyingCurves()[1] instanceof ConstantDoublesCurve);
    assertEquals(1 + shift, ((ConstantDoublesCurve) spreadCurve.getUnderlyingCurves()[1]).getYValue(1.), 2e-16);
    final Map<IndexON, YieldAndDiscountCurve> overnightCurves = shifted.getForwardONCurves();
    assertEquals(1, overnightCurves.size());
    final YieldAndDiscountCurve shiftedOvernightCurve = Iterables.getOnlyElement(overnightCurves.values());
    assertEquals(shiftedDiscountingCurve, shiftedOvernightCurve);
  }

  @Test
  public void testAdditiveShiftForLiborCurve() {
    final double shift = Math.random();
    final StructureManipulator<ParameterProviderInterface> manipulator = new CurveBundleSingleCurveParallelShift(ScenarioShiftType.ABSOLUTE, shift, LIBOR_CURVE_NAME);
    final ParameterProviderInterface result = manipulator.execute(MCPD, VALUE_SPEC, EXECUTION_CONTEXT);
    assertTrue(result instanceof MulticurveProviderDiscount);
    final MulticurveProviderDiscount shifted = (MulticurveProviderDiscount) result;
    final MulticurveProviderDiscount expected = MCPD.copy();
    final YieldCurve curveWithZeroShift = new YieldCurve(LIBOR_CURVE_NAME + "_WithParallelShift", SpreadDoublesCurve.from(AddCurveSpreadFunction.getInstance(),
        LIBOR_CURVE_NAME + "_WithParallelShift", LIBOR_CURVE.getCurve(), ConstantDoublesCurve.from(shift)));
    expected.replaceCurve(IBOR_INDEX, curveWithZeroShift);
    assertNotSame(MCPD, shifted);
    assertEquals(MCPD.getFxRates(), shifted.getFxRates());
    assertEquals(MCPD.getDiscountingCurves(), shifted.getDiscountingCurves());
    assertEquals(MCPD.getForwardONCurves(), shifted.getForwardONCurves());
    assertFalse(MCPD.getForwardIborCurves().equals(shifted.getForwardIborCurves()));
    final Map<IborIndex, YieldAndDiscountCurve> iborCurves = shifted.getForwardIborCurves();
    assertEquals(1, iborCurves.size());
    assertEquals(LIBOR_CURVE_NAME + "_WithParallelShift", Iterables.getOnlyElement(iborCurves.values()).getName());
    final YieldAndDiscountCurve shiftedIborCurve = Iterables.getOnlyElement(iborCurves.values());
    assertTrue(shiftedIborCurve instanceof YieldCurve);
    final YieldCurve shiftedIborYieldCurve = (YieldCurve) shiftedIborCurve;
    assertTrue(shiftedIborYieldCurve.getCurve() instanceof SpreadDoublesCurve);
    final SpreadDoublesCurve spreadCurve = (SpreadDoublesCurve) shiftedIborYieldCurve.getCurve();
    assertEquals(2, spreadCurve.getUnderlyingCurves().length);
    assertEquals(LIBOR_CURVE.getCurve(), spreadCurve.getUnderlyingCurves()[0]);
    assertTrue(spreadCurve.getUnderlyingCurves()[1] instanceof ConstantDoublesCurve);
    assertEquals(shift, ((ConstantDoublesCurve) spreadCurve.getUnderlyingCurves()[1]).getYValue(1.), 2e-16);
  }

  @Test
  public void testMultiplicativeShiftForLiborCurve() {
    final double shift = Math.random();
    final StructureManipulator<ParameterProviderInterface> manipulator = new CurveBundleSingleCurveParallelShift(ScenarioShiftType.RELATIVE, shift, LIBOR_CURVE_NAME);
    final ParameterProviderInterface result = manipulator.execute(MCPD, VALUE_SPEC, EXECUTION_CONTEXT);
    assertTrue(result instanceof MulticurveProviderDiscount);
    final MulticurveProviderDiscount shifted = (MulticurveProviderDiscount) result;
    final MulticurveProviderDiscount expected = MCPD.copy();
    final YieldCurve curveWithZeroShift = new YieldCurve(LIBOR_CURVE_NAME + "_WithParallelShift", SpreadDoublesCurve.from(AddCurveSpreadFunction.getInstance(),
        LIBOR_CURVE_NAME + "_WithParallelShift", LIBOR_CURVE.getCurve(), ConstantDoublesCurve.from(shift)));
    expected.replaceCurve(IBOR_INDEX, curveWithZeroShift);
    assertNotSame(MCPD, shifted);
    assertEquals(MCPD.getFxRates(), shifted.getFxRates());
    assertEquals(MCPD.getDiscountingCurves(), shifted.getDiscountingCurves());
    assertEquals(MCPD.getForwardONCurves(), shifted.getForwardONCurves());
    assertFalse(MCPD.getForwardIborCurves().equals(shifted.getForwardIborCurves()));
    final Map<IborIndex, YieldAndDiscountCurve> iborCurves = shifted.getForwardIborCurves();
    assertEquals(1, iborCurves.size());
    assertEquals(LIBOR_CURVE_NAME + "_WithParallelShift", Iterables.getOnlyElement(iborCurves.values()).getName());
    final YieldAndDiscountCurve shiftedIborCurve = Iterables.getOnlyElement(iborCurves.values());
    assertTrue(shiftedIborCurve instanceof YieldCurve);
    final YieldCurve shiftedIborYieldCurve = (YieldCurve) shiftedIborCurve;
    assertTrue(shiftedIborYieldCurve.getCurve() instanceof SpreadDoublesCurve);
    final SpreadDoublesCurve spreadCurve = (SpreadDoublesCurve) shiftedIborYieldCurve.getCurve();
    assertEquals(2, spreadCurve.getUnderlyingCurves().length);
    assertEquals(LIBOR_CURVE.getCurve(), spreadCurve.getUnderlyingCurves()[0]);
    assertTrue(spreadCurve.getUnderlyingCurves()[1] instanceof ConstantDoublesCurve);
    assertEquals(1 + shift, ((ConstantDoublesCurve) spreadCurve.getUnderlyingCurves()[1]).getYValue(1.), 2e-16);
  }
}
