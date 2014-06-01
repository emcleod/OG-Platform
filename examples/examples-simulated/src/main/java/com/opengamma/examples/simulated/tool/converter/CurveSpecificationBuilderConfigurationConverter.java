/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

public class CurveSpecificationBuilderConfigurationConverter {
  private static final Logger s_logger = LoggerFactory.getLogger(CurveSpecificationBuilderConfigurationConverter.class);
  private static final Set<StripConverter> CONVERTERS = new HashSet<>();

  static {
    CONVERTERS.add(new BankersAcceptanceStripConverter());
    CONVERTERS.add(new BasisSwapStripConverter());
    CONVERTERS.add(new CashStripConverter());
    CONVERTERS.add(new CdorStripConverter());
    CONVERTERS.add(new CiborStripConverter());
    CONVERTERS.add(new ContinuousZeroDepositStripConverter());
    CONVERTERS.add(new EuriborStripConverter());
    CONVERTERS.add(new FraStripConverter());
    CONVERTERS.add(new Fra3mStripConverter());
    CONVERTERS.add(new Fra6mStripConverter());
    CONVERTERS.add(new FutureStripConverter());
    CONVERTERS.add(new LiborStripConverter());
    CONVERTERS.add(new OisStripConverter());
    CONVERTERS.add(new PeriodicZeroDepositStripConverter());
    CONVERTERS.add(new SimpleZeroDepositStripConverter());
    CONVERTERS.add(new SpreadStripConverter());
    CONVERTERS.add(new StiborStripConverter());
    CONVERTERS.add(new SwapStripConverter());
    CONVERTERS.add(new Swap28dStripConverter());
    CONVERTERS.add(new Swap3mStripConverter());
    CONVERTERS.add(new Swap6mStripConverter());
    CONVERTERS.add(new Swap12mStripConverter());
  }

  public static Collection<CurveNodeIdMapper> convert(final String currency, final Map<String, CurveSpecificationBuilderConfiguration> configMap) {
    final Map<String, CurveNodeIdMapper.Builder> convertedWithNames = new HashMap<>();
    for (final Map.Entry<String, CurveSpecificationBuilderConfiguration> entry : configMap.entrySet()) {
      final String originalName = entry.getKey();
      final CurveSpecificationBuilderConfiguration originalConfig = entry.getValue();
      for (final StripConverter converter : CONVERTERS) {
        final String newName = converter.rename(originalName, currency);
        final Builder remappedNameBuilder = convertedWithNames.get(newName);
        final Pair<String, CurveNodeIdMapper.Builder> pair;
        if (remappedNameBuilder != null) {
          pair = converter.apply(remappedNameBuilder.build(), originalConfig);
        } else {
          pair = converter.apply(CurveNodeIdMapper.builder().name(newName).build(), originalConfig);
        }
        convertedWithNames.put(pair.getFirst(), pair.getSecond());
      }
    }
    final Set<CurveNodeIdMapper> converted = new HashSet<>();
    for (final Map.Entry<String, CurveNodeIdMapper.Builder> entry : convertedWithNames.entrySet()) {
      final CurveNodeIdMapper idMapper = entry.getValue().build();
      if (idMapper.getAllTenors().size() != 0) {
        converted.add(idMapper);
      }
    }
    return converted;
  }

  public abstract static class StripConverter {
    static final Logger s_logger = LoggerFactory.getLogger(CurveSpecificationBuilderConfigurationConverter.StripConverter.class);

    public Pair<String, CurveNodeIdMapper.Builder> apply(final CurveNodeIdMapper idMapper, final CurveSpecificationBuilderConfiguration identifiers) {
      final Map<Tenor, CurveInstrumentProvider> instrumentProviders = getInstrumentProviders(identifiers);
      if (instrumentProviders == null || instrumentProviders.isEmpty()) {
        return Pairs.of(idMapper.getName(), copyToBuilder(idMapper));
      }
      return Pairs.of(idMapper.getName(), createBuilder(idMapper, instrumentProviders));
    }

    public abstract CurveNodeIdMapper.Builder createBuilder(CurveNodeIdMapper idMapper, Map<Tenor, CurveInstrumentProvider> instrumentProviders);

    public abstract Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(CurveSpecificationBuilderConfiguration identifiers);

    public String rename(final String name, final String currency) {
      return name + " " + currency;
    }

    CurveNodeIdMapper.Builder copyToBuilder(final CurveNodeIdMapper mapper) {
      final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(mapper.getName());
      if (mapper.getAllTenors().size() == 0) {
        return builder;
      }
      if (mapper.getBillNodeIds() != null) {
        builder.billNodeIds(mapper.getBillNodeIds());
      }
      if (mapper.getBondNodeIds() != null) {
        builder.bondNodeIds(mapper.getBondNodeIds());
      }
      if (mapper.getCalendarSwapNodeIds() != null) {
        builder.calendarSwapNodeIds(mapper.getCalendarSwapNodeIds());
      }
      if (mapper.getCashNodeIds() != null) {
        builder.cashNodeIds(mapper.getCashNodeIds());
      }
      if (mapper.getContinuouslyCompoundedRateNodeIds() != null) {
        builder.continuouslyCompoundedRateNodeIds(mapper.getContinuouslyCompoundedRateNodeIds());
      }
      if (mapper.getCreditSpreadNodeIds() != null) {
        builder.creditSpreadNodeIds(mapper.getCreditSpreadNodeIds());
      }
      if (mapper.getDeliverableSwapFutureNodeIds() != null) {
        builder.deliverableSwapFutureNodeIds(mapper.getDeliverableSwapFutureNodeIds());
      }
      if (mapper.getDiscountFactorNodeIds() != null) {
        builder.discountFactorNodeIds(mapper.getDiscountFactorNodeIds());
      }
      if (mapper.getFRANodeIds() != null) {
        builder.fraNodeIds(mapper.getFRANodeIds());
      }
      if (mapper.getFXForwardNodeIds() != null) {
        builder.fxForwardNodeIds(mapper.getFXForwardNodeIds());
      }
      if (mapper.getIMMFRANodeIds() != null) {
        builder.immFRANodeIds(mapper.getIMMFRANodeIds());
      }
      if (mapper.getIMMSwapNodeIds() != null) {
        builder.immSwapNodeIds(mapper.getIMMSwapNodeIds());
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
      if (mapper.getThreeLegBasisSwapNodeIds() != null) {
        builder.threeLegBasisSwapNodeIds(mapper.getThreeLegBasisSwapNodeIds());
      }
      if (mapper.getZeroCouponInflationNodeIds() != null) {
        builder.zeroCouponInflationNodeIds(mapper.getZeroCouponInflationNodeIds());
      }
      return builder;
    }
  }

  private static class BankersAcceptanceStripConverter extends StripConverter {

    BankersAcceptanceStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getSwapNodeIds() != null) {
        s_logger.warn("Rate future nodes already exist in mapper called {}: overwriting with bankers acceptance futures", idMapper.getName());
      }
      return copyToBuilder(idMapper).swapNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " BA " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getFutureInstrumentProviders();
    }

  }

  private static class BasisSwapStripConverter extends StripConverter {

    BasisSwapStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getSwapNodeIds() != null) {
        s_logger.warn("Swap nodes already exist in mapper called {}: overwriting with basis swaps", idMapper.getName());
      }
      return copyToBuilder(idMapper).swapNodeIds(instrumentProviders);
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getBasisSwapInstrumentProviders();
    }

  }

  private static class CashStripConverter extends StripConverter {

    CashStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getCashNodeIds() != null) {
        s_logger.warn("Cash nodes already exist in mapper called {}: overwriting with deposits", idMapper.getName());
      }
      return copyToBuilder(idMapper).cashNodeIds(instrumentProviders);
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getCashInstrumentProviders();
    }

  }

  private static class CdorStripConverter extends StripConverter {

    CdorStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getCashNodeIds() != null) {
        s_logger.warn("Cash nodes already exist in mapper called {}: overwriting with CDOR", idMapper.getName());
      }
      return copyToBuilder(idMapper).cashNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " CDOR " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getCDORInstrumentProviders();
    }
  }

  private static class CiborStripConverter extends StripConverter {

    CiborStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getCashNodeIds() != null) {
        s_logger.warn("Cash nodes already exist in mapper called {}: overwriting with CIBOR", idMapper.getName());
      }
      return copyToBuilder(idMapper).cashNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " CIBOR " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getCiborInstrumentProviders();
    }
  }

  private static class ContinuousZeroDepositStripConverter extends StripConverter {

    ContinuousZeroDepositStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return Collections.emptyMap();
    }
  }

  private static class EuriborStripConverter extends StripConverter {

    EuriborStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getCashNodeIds() != null) {
        s_logger.warn("Cash nodes already exist in mapper called {}: overwriting with EURIBOR", idMapper.getName());
      }
      return copyToBuilder(idMapper).cashNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " Euribor " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getEuriborInstrumentProviders();
    }

  }

  private static class FraStripConverter extends StripConverter {

    FraStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return Collections.emptyMap();
    }

  }

  private static class Fra3mStripConverter extends StripConverter {

    Fra3mStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getFRANodeIds() != null) {
        s_logger.warn("FRA nodes already exist in mapper called {}: overwriting with 3m FRAs", idMapper.getName());
      }
      return copyToBuilder(idMapper).fraNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " 3m " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getFra3MInstrumentProviders();
    }

  }

  private static class Fra6mStripConverter extends StripConverter {

    Fra6mStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getFRANodeIds() != null) {
        s_logger.warn("FRA nodes already exist in mapper called {}: overwriting with 6m FRAs", idMapper.getName());
      }
      return copyToBuilder(idMapper).fraNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " 6m " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getFra6MInstrumentProviders();
    }

  }

  private static class FutureStripConverter extends StripConverter {

    FutureStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getRateFutureNodeIds() != null) {
        s_logger.warn("Rate future nodes already exist in mapper called {}: overwriting", idMapper.getName());
      }
      return copyToBuilder(idMapper).cashNodeIds(instrumentProviders);
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getFutureInstrumentProviders();
    }

  }

  private static class LiborStripConverter extends StripConverter {

    LiborStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getCashNodeIds() != null) {
        s_logger.warn("Cash nodes already exist in mapper called {}: overwriting with LIBOR", idMapper.getName());
      }
      return copyToBuilder(idMapper).cashNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " Libor " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getLiborInstrumentProviders();
    }

  }

  private static class OisStripConverter extends StripConverter {

    OisStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getSwapNodeIds() != null) {
        s_logger.warn("Swap nodes already exist in id mapper called {}: overwriting with OIS", idMapper.getName());
      }
      return copyToBuilder(idMapper).swapNodeIds(instrumentProviders);
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getOISSwapInstrumentProviders();
    }
  }

  private static class PeriodicZeroDepositStripConverter extends StripConverter {

    PeriodicZeroDepositStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getPeriodicallyCompoundedRateNodeIds() != null) {
        s_logger.warn("Periodically compounded rate nodes already exist in mapper called {}: overwriting", idMapper.getName());
      }
      return copyToBuilder(idMapper).periodicallyCompoundedRateNodeIds(instrumentProviders);
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getPeriodicZeroDepositInstrumentProviders();
    }

  }

  private static class SimpleZeroDepositStripConverter extends StripConverter {

    SimpleZeroDepositStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return Collections.emptyMap();
    }
  }

  private static class SpreadStripConverter extends StripConverter {

    SpreadStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return Collections.emptyMap();
    }
  }

  private static class StiborStripConverter extends StripConverter {

    StiborStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getCashNodeIds() != null) {
        s_logger.warn("Cash nodes already exist in mapper called {}: overwriting with STIBOR", idMapper.getName());
      }
      return copyToBuilder(idMapper).cashNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " Stibor " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getStiborInstrumentProviders();
    }

  }

  private static class SwapStripConverter extends StripConverter {

    SwapStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return Collections.emptyMap();
    }

  }

  private static class Swap28dStripConverter extends StripConverter {

    Swap28dStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getSwapNodeIds() != null) {
        s_logger.warn("Swap nodes already exist in mapper called {}: overwriting with 28d swap", idMapper.getName());
      }
      return copyToBuilder(idMapper).swapNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " 28d " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getSwap28DInstrumentProviders();
    }

  }

  private static class Swap3mStripConverter extends StripConverter {

    Swap3mStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getSwapNodeIds() != null) {
        s_logger.warn("Swap nodes already exist in mapper called {}: overwriting with 3m swap", idMapper.getName());
      }
      return copyToBuilder(idMapper).swapNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " 3m " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getSwap3MInstrumentProviders();
    }

  }

  private static class Swap6mStripConverter extends StripConverter {

    Swap6mStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getSwapNodeIds() != null) {
        s_logger.warn("Swap nodes already exist in mapper called {}: overwriting with 6m swap", idMapper.getName());
      }
      return copyToBuilder(idMapper).swapNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " 6m " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getSwap6MInstrumentProviders();
    }

  }

  private static class Swap12mStripConverter extends StripConverter {

    Swap12mStripConverter() {
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getSwapNodeIds() != null) {
        s_logger.warn("Swap nodes already exist in mapper called {}: overwriting with 12m swap", idMapper.getName());
      }
      return copyToBuilder(idMapper).swapNodeIds(instrumentProviders);
    }

    @Override
    public String rename(final String name, final String currency) {
      return name + " 12m " + currency;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getSwap12MInstrumentProviders();
    }

  }
}
