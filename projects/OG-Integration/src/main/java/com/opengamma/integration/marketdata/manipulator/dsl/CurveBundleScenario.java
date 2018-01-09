/**
 * 
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.opengamma.engine.marketdata.manipulator.DistinctMarketDataSelector;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class CurveBundleScenario extends Scenario {
  /** For parsing valuation time. */
  private static final DateTimeFormatter s_dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /** Manipulators keyed by the selectors for the items they apply to. */
  private final ListMultimap<DistinctMarketDataSelector, StructureManipulator<?>> _manipulations = ArrayListMultimap.create();

  /** This scenario's name. */
  private final String _name;
  /** The simulation to which this scenario belongs, possibly null */
  private final Simulation _simulation;

  /** Calc configs to which this scenario will be applied, null will match any config. */
  private Set<String> _calcConfigNames;
  /** Valuation time of this scenario's calculation cycle. */
  private Instant _valuationTime;
  /** Version correction used by the resolver. */
  private VersionCorrection _resolverVersionCorrection;

  public CurveBundleScenario(final String name) {
    super(name);
    _name = name;
    _simulation = null;
  }

  /* package */CurveBundleScenario(final Simulation simulation, final String name) {
    super(name);
    ArgumentChecker.notNull(simulation, "simulation");
    _simulation = simulation;
    _name = name;
  }

  public CurveBundleSelector.Builder curveBundle() {
    return new CurveBundleSelector.Builder(this);
  }

}
