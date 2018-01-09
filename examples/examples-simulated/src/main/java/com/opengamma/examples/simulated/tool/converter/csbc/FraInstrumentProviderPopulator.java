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
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Class that populates a curve node id mapper with the curve instrument providers for
 * FRA {@link StripInstrumentType}s. If a map for {@link FRANode} is already present,
 * this class will overwrite that entry.
 * <p>
 * The instrument provider name must be supplied, as there is a many-to-one mapping 
 * from FRA strip instrument types to FRA nodes. The getter method of the 
 * {@link CurveSpecificationBuilderConfiguration} is called using reflection.
 * <p>
 * The supported types of strip are:
 * <p>
 * <ul>
 * <li> {@link StripInstrumentType#FRA_3M}
 * <li> {@link StripInstrumentType#FRA_6M}
 * </ul>
 * 
 * @author elaine
 */
public class FraInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FraInstrumentProviderPopulator.class);
  /** The strip instrument type */
  private final StripInstrumentType _type;

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   * @param type The strip instrument type, not null
   * @param instrumentProviderName The instrument provider name, not null
   * @throws IllegalArgumentException If the strip instrument type is not one of:
   * <p>
   * <ul>
   * <li> {@link StripInstrumentType#FRA_3M}
   * <li> {@link StripInstrumentType#FRA_6M}
   * </ul>
   */
  public FraInstrumentProviderPopulator(final StripInstrumentType type, final String instrumentProviderName) {
    this(type, instrumentProviderName, new DefaultCsbcRenamingFunction());
  }

  /**
   * @param type The strip instrument type, not null
   * @param instrumentProviderName The instrument provider name, not null
   * @param renamingFunction The renaming function, not null
   * @throws IllegalArgumentException If the strip instrument type is not one of:
   * <p>
   * <ul>
   * <li> {@link StripInstrumentType#FRA_3M}
   * <li> {@link StripInstrumentType#FRA_6M}
   * </ul>
   */
  public FraInstrumentProviderPopulator(final StripInstrumentType type, final String instrumentProviderName, final Function2<String, String, String> renamingFunction) {
    super(instrumentProviderName, renamingFunction);
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(instrumentProviderName, "instrumentProviderName");
    ArgumentChecker.isTrue(type == StripInstrumentType.FRA_3M || type == StripInstrumentType.FRA_6M, "Strip instrument type {} was not a supported type: " +
        "allowed {} or {}", type, StripInstrumentType.FRA_3M, StripInstrumentType.FRA_6M);
    _type = type;
  }

  /**
   * Creates a builder from the id mapper, copying any maps that have already been populated,
   * and populates the appropriate map for the FRA strip instrument type.
   * @param idMapper The id mapper, not null
   * @param instrumentProviders The instrument provider map, not null
   * @param mapperName The new name for the mapper, not null
   * @return A curve node id mapper builder with populated instrument provider maps.
   */
  @Override
  public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders,
      final String mapperName) {
    if (idMapper.getFRANodeIds() != null) {
      s_logger.warn("FRA nodes already exist in mapper called {}: overwriting with {}", idMapper.getName(), _type);
    }
    return copyToBuilder(idMapper, mapperName).fraNodeIds(instrumentProviders);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _type.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof FraInstrumentProviderPopulator)) {
      return false;
    }
    final FraInstrumentProviderPopulator other = (FraInstrumentProviderPopulator) obj;
    return _type == other._type;
  }
}
