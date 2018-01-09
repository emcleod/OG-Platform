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
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Class that populates a curve node id mapper with the curve instrument providers for a 
 * {@link StripInstrumentType#CONTINUOUS_ZERO_DEPOSIT} strip. If a map for 
 * {@link ContinuouslyCompoundedRateNode} is already present, this class with overwrite
 * that entry.
 * 
 * @author elaine
 */
public class ContinuouslyCompoundedRateInstrumentProviderPopulator extends InstrumentProviderPopulator {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ContinuouslyCompoundedRateInstrumentProviderPopulator.class);

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   */
  public ContinuouslyCompoundedRateInstrumentProviderPopulator() {
    super(null);
  }

  /**
   * Sets the method name to null, as the getter name for continuously-compounded rate strips is known.
   * @param renamingFunction The renaming function, not null
   */
  public ContinuouslyCompoundedRateInstrumentProviderPopulator(final Function2<String, String, String> renamingFunction) {
    super(null, renamingFunction);
  }

  @Override
  public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders,
      final String currency) {
    if (idMapper.getPeriodicallyCompoundedRateNodeIds() != null) {
      s_logger.warn("Continuously compounded rate nodes already exist in mapper called {}: overwriting", idMapper.getName());
    }
    return copyToBuilder(idMapper, currency).continuouslyCompoundedRateNodeIds(instrumentProviders);
  }

  @Override
  public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    return identifiers.getContinuousZeroDepositInstrumentProviders();
  }

}
