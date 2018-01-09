/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Class that populates a curve node id mapper with the curve instrument providers for a
 * {@link StripInstrumentType#FUTURE} strip. If a map for {@link RateFutureNode}s is 
 * already present, this class will overwrite that entry.
 *
 * @author emcleod
 */
public class FutureInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FutureInstrumentProviderPopulator.class);

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   */
  public FutureInstrumentProviderPopulator() {
    this(new DefaultCsbcRenamingFunction());
  }

  /**
   * Sets the getter method name to null, as the getter name for future strips is known.
   * @param renamingFunction The renaming function, not null
   */
  public FutureInstrumentProviderPopulator(final Function2<String, String, String> renamingFunction) {
    super(null, renamingFunction);
  }

  /**
   * Creates a builder from the id mapper, copying any maps that have already been populated,
   * and populates the rate future node id map. This method will overwrite the rate future
   * node ids if they are already present in the curve node id mapper.
   * @param idMapper The id mapper, not null
   * @param instrumentProviders The instrument provider map, not null
   * @param mapperName The new name for the mapper, not null
   * @return A curve node id mapper builder with populated instrument provider maps.
   */
  @Override
  public Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders,
      final String mapperName) {
    if (idMapper.getRateFutureNodeIds() != null) {
      s_logger.warn("Rate future nodes already exist in mapper called {}, overwriting", idMapper.getName());
    }
    return copyToBuilder(idMapper, mapperName).rateFutureNodeIds(instrumentProviders);
  }

  /**
   * Gets the map of future instrument providers from the curve specification builder configuration
   * using {@link CurveSpecificationBuilderConfiguration#getFutureInstrumentProviders()}.
   * @param csbc The curve specification builder configuration, not null
   * @return A map from tenor to curve instrument provider.
   */
  @Override
  public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration csbc) {
    ArgumentChecker.notNull(csbc, "csbc");
    return csbc.getFutureInstrumentProviders();
  }

}
