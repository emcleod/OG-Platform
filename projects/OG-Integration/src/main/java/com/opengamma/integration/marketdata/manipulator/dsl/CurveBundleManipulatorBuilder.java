/**
 * 
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.util.ArgumentChecker;

public class CurveBundleManipulatorBuilder {

  private final CurveBundleSelector _selector;
  private final CurveBundleScenario _scenario;

  protected CurveBundleManipulatorBuilder(final CurveBundleSelector selector, final CurveBundleScenario scenario) {
    ArgumentChecker.notNull(selector, "selector");
    ArgumentChecker.notNull(scenario, "scenario");
    _selector = selector;
    _scenario = scenario;
  }

  public CurveBundleSelector getSelector() {
    return _selector;
  }

  public CurveBundleScenario getScenario() {
    return _scenario;
  }

  public CurveBundleManipulatorBuilder parallelShift(final ScenarioShiftType shiftType, final Number shift, final String curveName) {
    _scenario.add(_selector, new CurveBundleSingleCurveParallelShift(shiftType, shift.doubleValue(), curveName));
    return this;
  }
}
