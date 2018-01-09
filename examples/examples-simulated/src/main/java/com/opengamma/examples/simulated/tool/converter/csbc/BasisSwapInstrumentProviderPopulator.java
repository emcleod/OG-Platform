/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Class that populates a curve node id mapper with the curve instrument providers for
 * {@link StripInstrumentType#BASIS_SWAP} and {@link StripInstrumentType#TENOR_SWAP}.
 * If a map for {@link SwapNode} is already present, this class will overwrite that
 * entry.
 *
 * @author elaine
 */
public class BasisSwapInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(BasisSwapInstrumentProviderPopulator.class);
  /** The strip instrument type */
  private final StripInstrumentType _type;

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   * @param type The strip instrument type, not null
   * @throws IllegalArgumentException If the strip instrument type is not one of:
   * <p>
   * <ul>
   * <li> {@link StripInstrumentType#BASIS_SWAP}
   * <li> {@link StripInstrumentType#TENOR_SWAP}
   * </ul>
   */
  public BasisSwapInstrumentProviderPopulator(final StripInstrumentType type) {
    this(type, new DefaultCsbcRenamingFunction());
  }

  /**
   * @param type The strip instrument type, not null
   * @param renamingFunction The renaming function, not null
   * @throws IllegalArgumentException If the strip instrument type is not one of:
   * <p>
   * <ul>
   * <li> {@link StripInstrumentType#BASIS_SWAP}
   * <li> {@link StripInstrumentType#TENOR_SWAP}
   * </ul>
   */
  public BasisSwapInstrumentProviderPopulator(final StripInstrumentType type, final Function2<String, String, String> renamingFunction) {
    super(null, renamingFunction);
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.isTrue(type == StripInstrumentType.BASIS_SWAP || type == StripInstrumentType.TENOR_SWAP, "Strip instrument type {} was not a supported type: " +
        "allowed {} or {}", type, StripInstrumentType.BASIS_SWAP, StripInstrumentType.TENOR_SWAP);
    _type = type;
  }

  @Override
  public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders,
      final String currency) {
    if (idMapper.getSwapNodeIds() != null) {
      s_logger.warn("Swap node ids already exist in mapper called {}: overwriting with {}", idMapper.getName(), _type);
    }
    return copyToBuilder(idMapper, currency).swapNodeIds(instrumentProviders);
  }

  @Override
  public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    switch (_type) {
      case BASIS_SWAP:
        return identifiers.getBasisSwapInstrumentProviders();
      case TENOR_SWAP:
        return identifiers.getTenorSwapInstrumentProviders();
      default:
        s_logger.warn("Could not find instrument provider method for {}", _type);
        return Collections.emptyMap();
    }
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
    if (!(obj instanceof BasisSwapInstrumentProviderPopulator)) {
      return false;
    }
    final BasisSwapInstrumentProviderPopulator other = (BasisSwapInstrumentProviderPopulator) obj;
    return _type == other._type;
  }

}
