/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import java.util.Map;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * @author emcleod
 *
 */
public class FutureInstrumentProviderConverter extends InstrumentProviderConverter {

  public FutureInstrumentProviderConverter() {
    this(new DefaultCSBCRenamingFunction());
  }

  public FutureInstrumentProviderConverter(final Function2<String, String, String> renamingFunction) {
    super("getFutureInstrumentProviders", renamingFunction);
  }

  @Override
  public Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
    if (idMapper.getRateFutureNodeIds() != null) {
      s_logger.warn("Rate future nodes already exist in mapper called {}, overwriting", idMapper.getName());
    }
    return copyToBuilder(idMapper).rateFutureNodeIds(instrumentProviders);
  }

  @Override
  public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    return identifiers.getFutureInstrumentProviders();
  }

}
