/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.strips.PeriodicallyCompoundedRateNode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Class that populates a curve node id mapper with the curve instrument providers for a 
 * {@link StripInstrumentType#PERIODIC_ZERO_DEPOSIT} strip. If a map for 
 * {@link PeriodicallyCompoundedRateNode} is already present, this class will overwrite
 * that entry.
 * 
 * @author elaine
 */
public class PeriodicZeroDepositInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(PeriodicZeroDepositInstrumentProviderPopulator.class);

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   */
  public PeriodicZeroDepositInstrumentProviderPopulator() {
    this(new DefaultCsbcRenamingFunction());
  }

  /**
   * Sets the method name to null, as the getter name for periodic zero strips is known.
   * @param renamingFunction The renaming function, not null
   */
  public PeriodicZeroDepositInstrumentProviderPopulator(final Function2<String, String, String> renamingFunction) {
    super(null, renamingFunction);
  }

  /**
   * Creates a builder from the id mapper, copying any maps that have already been populated,
   * and populates the periodic zero deposit node id map. This method will overwrite the 
   * periodic zero node ids if they are already present in the curve node id mapper.
   * @param idMapper The id mapper, not null
   * @param instrumentProviders The instrument provider map, not null
   * @param mapperName The new name for the mapper, not null
   * @return A curve node id mapper builder with populated instrument provider maps.
   */
  @Override
  public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders,
      final String mapperName) {
    if (idMapper.getPeriodicallyCompoundedRateNodeIds() != null) {
      s_logger.warn("Periodically compounded rate nodes already exist in mapper called {}: overwriting", idMapper.getName());
    }
    return copyToBuilder(idMapper, mapperName).periodicallyCompoundedRateNodeIds(instrumentProviders);
  }

  /**
   * Gets the map of periodic zero deposit instrument providers from the curve specification 
   * builder configuration using {@link CurveSpecificationBuilderConfiguration#getPeriodicZeroDepositInstrumentProviders()}.
   * @param csbc The curve specification builder configuration, not null
   * @return A map from tenor to curve instrument provider.
   */
  @Override
  public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration csbc) {
    ArgumentChecker.notNull(csbc, "csbc");
    return csbc.getPeriodicZeroDepositInstrumentProviders();
  }

}
