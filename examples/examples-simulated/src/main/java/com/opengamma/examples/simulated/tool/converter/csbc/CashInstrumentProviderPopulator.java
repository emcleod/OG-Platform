/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Class that populates a curve node id mapper with the curve instrument providers for 
 * cash {@link StripInstrumentType}s. If a map for {@link CashNode} is already present,
 * this class will overwrite that entry.
 * <p>
 * The instrument provider name must be supplied, as there is a many-to-one mapping 
 * from cash strip instrument types to cash nodes. The getter method of the 
 * {@link CurveSpecificationBuilderConfiguration} is called using reflection.
 * <p>
 * The supported types of strip are:
 * <p>
 * <ul>
 * <li> {@link StripInstrumentType#CASH}
 * <li> {@link StripInstrumentType#CDOR}
 * <li> {@link StripInstrumentType#CIBOR}
 * <li> {@link StripInstrumentType#EURIBOR}
 * <li> {@link StripInstrumentType#LIBOR}
 * <li> {@link StripInstrumentType#STIBOR}
 * </ul>
 * 
 * @author elaine
 */
public class CashInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(CashInstrumentProviderPopulator.class);
  /** The supported cash strip instrument types */
  private static final Set<StripInstrumentType> SUPPORTED_TYPES;

  static {
    SUPPORTED_TYPES = EnumSet.of(StripInstrumentType.CASH,
        StripInstrumentType.CDOR,
        StripInstrumentType.CIBOR,
        StripInstrumentType.EURIBOR,
        StripInstrumentType.LIBOR,
        StripInstrumentType.STIBOR);
  }
  /** The strip instrument type */
  private final StripInstrumentType _type;

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   * @param type The strip instrument type, not null
   * @param instrumentProviderName The instrument provider name, not null
   * @throws IllegalArgumentException If the strip instrument type is not one of:
   * <ul>
   * <li> {@link StripInstrumentType#CASH}
   * <li> {@link StripInstrumentType#CDOR}
   * <li> {@link StripInstrumentType#CIBOR}
   * <li> {@link StripInstrumentType#EURIBOR}
   * <li> {@link StripInstrumentType#LIBOR}
   * <li> {@link StripInstrumentType#STIBOR}
   * </ul>
   */
  public CashInstrumentProviderPopulator(final StripInstrumentType type, final String instrumentProviderName) {
    this(type, instrumentProviderName, new DefaultCsbcRenamingFunction());
  }

  /**
   * @param type The strip instrument type, not null
   * @param instrumentProviderName The instrument provider function name, not null
   * @param renamingFunction The renaming function, not null
   * @throws IllegalArgumentException If the strip instrument type is not one of:
   * <ul>
   * <li> {@link StripInstrumentType#CASH}
   * <li> {@link StripInstrumentType#CDOR}
   * <li> {@link StripInstrumentType#CIBOR}
   * <li> {@link StripInstrumentType#EURIBOR}
   * <li> {@link StripInstrumentType#LIBOR}
   * <li> {@link StripInstrumentType#STIBOR}
   * </ul>
   */
  public CashInstrumentProviderPopulator(final StripInstrumentType type, final String instrumentProviderName,
      final Function2<String, String, String> renamingFunction) {
    super(instrumentProviderName, renamingFunction);
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(instrumentProviderName, "instrumentProviderName");
    ArgumentChecker.isTrue(SUPPORTED_TYPES.contains(type), "Strip instrument type {} was not a supported type: allowed {}", type, SUPPORTED_TYPES);
    _type = type;
  }

  /**
   * Creates a builder from the id mapper, copying any maps that have already been populated,
   * and populates the appropriate map for the cash strip instrument type.
   * @param idMapper The id mapper, not null
   * @param instrumentProviders The instrument provider map, not null
   * @param mapperName The new name for the mapper, not null
   * @return A curve node id mapper builder with populated instrument provider maps.
   */
  @Override
  public Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders,
      final String mapperName) {
    if (idMapper.getCashNodeIds() != null) {
      s_logger.warn("Cash nodes already exist in mapper called {}: overwriting with {}", idMapper.getName(), _type);
    }
    return copyToBuilder(idMapper, mapperName).cashNodeIds(instrumentProviders);
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
    if (!(obj instanceof CashInstrumentProviderPopulator)) {
      return false;
    }
    final CashInstrumentProviderPopulator other = (CashInstrumentProviderPopulator) obj;
    return _type == other._type;
  }

}
