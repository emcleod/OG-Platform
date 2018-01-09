/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 * An instrument provider converter that does not perform conversions: to be used
 * in cases where a mapping is not possible or desirable for a particular strip 
 * instrument type, e.g. {@link StripInstrumentType#FRA}, where there is no
 * information about the reset tenor, and so the {@link FRANode} cannot be created.
 * 
 * @author emcleod
 */
public class NoOpInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(NoOpInstrumentProviderPopulator.class);
  /** The strip instrument type */
  private final StripInstrumentType _type;

  /**
   * Sets the getter method name to null.
   * @param type The strip instrument type, not null
   */
  public NoOpInstrumentProviderPopulator(final StripInstrumentType type) {
    super(null);
    ArgumentChecker.notNull(type, "type");
    _type = type;
  }

  /**
   * Returns an empty map.
   * @param csbc The curve specification builder configuration, not used
   * @return An empty map
   */
  @Override
  public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration csbc) {
    s_logger.error("Cannot convert strips of type {}", _type);
    return Collections.emptyMap();
  }

  /**
   * Creates a builder from the id mapper but does not populate any maps.
   * @param idMapper The id mapper, not null
   * @param instrumentProviders The instrument provider map, not null
   * @param mapperName The new name for the mapper, not null
   * @return A curve node id mapper builder with populated instrument provider maps.
   */
  @Override
  public Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders,
      final String mapperName) {
    return copyToBuilder(idMapper, mapperName);
  }

}
