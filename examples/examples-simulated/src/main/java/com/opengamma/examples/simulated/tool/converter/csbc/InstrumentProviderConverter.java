/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * @author emcleod
 *
 */
public abstract class InstrumentProviderConverter {
  static final Logger s_logger = LoggerFactory.getLogger(InstrumentProviderConverter.class);
  private final String _instrumentProviderName;
  private final Function2<String, String, String> _renamingFunction;

  public InstrumentProviderConverter(final String instrumentProviderName) {
    this(instrumentProviderName, new DefaultCSBCRenamingFunction());
  }

  public InstrumentProviderConverter(final String instrumentProviderName, final Function2<String, String, String> renamingFunction) {
    _instrumentProviderName = instrumentProviderName;
    _renamingFunction = renamingFunction;
  }

  public Pair<String, CurveNodeIdMapper.Builder> apply(final CurveNodeIdMapper idMapper, final CurveSpecificationBuilderConfiguration identifiers) {
    final Map<Tenor, CurveInstrumentProvider> instrumentProviders = getInstrumentProviders(identifiers);
    if (instrumentProviders == null || instrumentProviders.isEmpty()) {
      return Pairs.of(idMapper.getName(), copyToBuilder(idMapper));
    }
    return Pairs.of(idMapper.getName(), createBuilder(idMapper, instrumentProviders));
  }

  public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
    throw new UnsupportedOperationException();
  }

  public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
    try {
      final Method method = identifiers.getClass().getMethod(_instrumentProviderName, (Class<?>[]) null);
      return (Map<Tenor, CurveInstrumentProvider>) method.invoke(identifiers, (Object[]) null);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public String rename(final String name, final String currency) {
    return _renamingFunction.apply(name, currency);
  }

  protected CurveNodeIdMapper.Builder copyToBuilder(final CurveNodeIdMapper mapper) {
    final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(mapper.getName());
    if (mapper.getAllTenors().size() == 0) {
      return builder;
    }
    if (mapper.getCashNodeIds() != null) {
      builder.cashNodeIds(mapper.getCashNodeIds());
    }
    if (mapper.getContinuouslyCompoundedRateNodeIds() != null) {
      builder.continuouslyCompoundedRateNodeIds(mapper.getContinuouslyCompoundedRateNodeIds());
    }
    if (mapper.getFRANodeIds() != null) {
      builder.fraNodeIds(mapper.getFRANodeIds());
    }
    if (mapper.getPeriodicallyCompoundedRateNodeIds() != null) {
      builder.periodicallyCompoundedRateNodeIds(mapper.getPeriodicallyCompoundedRateNodeIds());
    }
    if (mapper.getRateFutureNodeIds() != null) {
      builder.rateFutureNodeIds(mapper.getRateFutureNodeIds());
    }
    if (mapper.getSwapNodeIds() != null) {
      builder.swapNodeIds(mapper.getSwapNodeIds());
    }
    return builder;
  }

}
