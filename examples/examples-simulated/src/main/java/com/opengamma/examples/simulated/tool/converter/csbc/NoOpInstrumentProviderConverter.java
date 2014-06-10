/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import java.util.Collections;
import java.util.Map;

import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.time.Tenor;

/**
 * @author emcleod
 *
 */
public class NoOpInstrumentProviderConverter extends InstrumentProviderConverter {
  private final StripInstrumentType _type;

  public NoOpInstrumentProviderConverter(final StripInstrumentType type) {
    super(null);
    _type = type;
  }

  @Override
  public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    s_logger.error("Cannot convert strips of type {}", _type);
    return Collections.emptyMap();
  }

}
