/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter;

import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.BANKERS_ACCEPTANCE;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.BASIS_SWAP;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CASH;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CDOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CONTINUOUS_ZERO_DEPOSIT;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.EURIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FRA;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FRA_3M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FRA_6M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FUTURE;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.LIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.OIS_SWAP;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.PERIODIC_ZERO_DEPOSIT;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SIMPLE_ZERO_DEPOSIT;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SPREAD;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.STIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_12M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_28D;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_3M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_6M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.TENOR_SWAP;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.examples.simulated.tool.converter.csbc.BasisSwapInstrumentProviderPopulator;
import com.opengamma.examples.simulated.tool.converter.csbc.CashInstrumentProviderPopulator;
import com.opengamma.examples.simulated.tool.converter.csbc.ContinuouslyCompoundedRateInstrumentProviderPopulator;
import com.opengamma.examples.simulated.tool.converter.csbc.DefaultCsbcRenamingFunction;
import com.opengamma.examples.simulated.tool.converter.csbc.FixedCurrencyCsbcRenamingFunction;
import com.opengamma.examples.simulated.tool.converter.csbc.FraInstrumentProviderPopulator;
import com.opengamma.examples.simulated.tool.converter.csbc.FutureInstrumentProviderPopulator;
import com.opengamma.examples.simulated.tool.converter.csbc.InstrumentProviderPopulator;
import com.opengamma.examples.simulated.tool.converter.csbc.NoOpInstrumentProviderPopulator;
import com.opengamma.examples.simulated.tool.converter.csbc.PeriodicZeroDepositInstrumentProviderPopulator;
import com.opengamma.examples.simulated.tool.converter.csbc.SwapInstrumentProviderPopulator;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Converts a {@link CurveSpecificationBuilderConfiguration} into {@link CurveNodeIdMapper}.
 */
public class CurveSpecificationBuilderConfigurationConverter {
  private static final Logger s_logger = LoggerFactory.getLogger(CurveSpecificationBuilderConfigurationConverter.class);
  private static final Map<StripInstrumentType, InstrumentProviderPopulator> CONVERTERS = new EnumMap<>(StripInstrumentType.class);

  static {
    CONVERTERS.put(BANKERS_ACCEPTANCE, new NoOpInstrumentProviderPopulator(BANKERS_ACCEPTANCE));
    CONVERTERS.put(BASIS_SWAP, new BasisSwapInstrumentProviderPopulator(BASIS_SWAP));
    CONVERTERS.put(CASH, new ReflectionStripConverter(CASH, "getCashInstrumentProviders", "cashNodeIds", new DefaultCsbcRenamingFunction()));
    CONVERTERS.put(CDOR, new CashInstrumentProviderPopulator(CDOR, "getCDORInstrumentProviders", new FixedCurrencyCsbcRenamingFunction("CAD", "CDOR")));
    CONVERTERS.put(CIBOR, new CashInstrumentProviderPopulator(CIBOR, "getCiborInstrumentProviders", new FixedCurrencyCsbcRenamingFunction("DKK", "Cibor")));
    CONVERTERS.put(CONTINUOUS_ZERO_DEPOSIT, new ContinuouslyCompoundedRateInstrumentProviderPopulator());
    CONVERTERS.put(EURIBOR, new CashInstrumentProviderPopulator(EURIBOR, "getEuriborInstrumentProviders", new FixedCurrencyCsbcRenamingFunction("EUR", "Euribor")));
    CONVERTERS.put(FRA, new NoOpInstrumentProviderPopulator(FRA));
    CONVERTERS.put(FRA_3M, new FraInstrumentProviderPopulator(FRA_3M, "getFra3MInstrumentProviders", new DefaultCsbcRenamingFunction("3m")));
    CONVERTERS.put(FRA_6M, new FraInstrumentProviderPopulator(FRA_6M, "getFra6MInstrumentProviders", new DefaultCsbcRenamingFunction("6m")));
    CONVERTERS.put(FUTURE, new FutureInstrumentProviderPopulator());
    CONVERTERS.put(LIBOR, new CashInstrumentProviderPopulator(LIBOR, "getLiborInstrumentProviders", new DefaultCsbcRenamingFunction("Libor")));
    CONVERTERS.put(OIS_SWAP, new SwapInstrumentProviderPopulator(OIS_SWAP, "getOISSwapInstrumentProviders"));
    CONVERTERS.put(PERIODIC_ZERO_DEPOSIT, new PeriodicZeroDepositInstrumentProviderPopulator());
    CONVERTERS.put(SIMPLE_ZERO_DEPOSIT, new NoOpInstrumentProviderPopulator(SIMPLE_ZERO_DEPOSIT));
    CONVERTERS.put(SPREAD, new NoOpInstrumentProviderPopulator(SPREAD));
    CONVERTERS.put(STIBOR, new CashInstrumentProviderPopulator(STIBOR, "getStiborInstrumentProviders", new FixedCurrencyCsbcRenamingFunction("SEK", "Stibor")));
    CONVERTERS.put(SWAP, new NoOpInstrumentProviderPopulator(SWAP));
    CONVERTERS.put(SWAP_28D, new SwapInstrumentProviderPopulator(SWAP_28D, "getSwap28DInstrumentProviders", new DefaultCsbcRenamingFunction("28d")));
    CONVERTERS.put(SWAP_3M, new SwapInstrumentProviderPopulator(SWAP_3M, "getSwap3MInstrumentProviders", new DefaultCsbcRenamingFunction("3m")));
    CONVERTERS.put(SWAP_6M, new SwapInstrumentProviderPopulator(SWAP_6M, "getSwap6MInstrumentProviders", new DefaultCsbcRenamingFunction("6m")));
    CONVERTERS.put(SWAP_12M, new SwapInstrumentProviderPopulator(SWAP_12M, "getSwap12MInstrumentProviders", new DefaultCsbcRenamingFunction("12m")));
    CONVERTERS.put(TENOR_SWAP, new BasisSwapInstrumentProviderPopulator(TENOR_SWAP));
  }

  public static Collection<CurveNodeIdMapper> convert(final String currency, final Map<String, CurveSpecificationBuilderConfiguration> configMap) {
    return convert(currency, configMap, CONVERTERS);
  }

  public static Collection<CurveNodeIdMapper> convert(final String currency, final Map<String, CurveSpecificationBuilderConfiguration> configMap,
      final Map<StripInstrumentType, InstrumentProviderPopulator> converters) {
    final Map<String, CurveNodeIdMapper.Builder> convertedWithNames = new HashMap<>();
    for (final Map.Entry<String, CurveSpecificationBuilderConfiguration> convertedEntry : configMap.entrySet()) {
      final String originalName = convertedEntry.getKey();
      final CurveSpecificationBuilderConfiguration originalConfig = convertedEntry.getValue();
      for (final Map.Entry<StripInstrumentType, InstrumentProviderPopulator> entry : converters.entrySet()) {
        final InstrumentProviderPopulator converter = entry.getValue();
        final Builder remappedNameBuilder = convertedWithNames.get(originalName);
        final Pair<String, CurveNodeIdMapper.Builder> pair;
        if (remappedNameBuilder != null) {
          pair = converter.apply(remappedNameBuilder.build(), originalConfig, currency);
        } else {
          pair = converter.apply(CurveNodeIdMapper.builder().name(originalName).build(), originalConfig, currency);
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

  public static class ReflectionStripConverter extends InstrumentProviderPopulator {
    private final StripInstrumentType _type;
    private final String _builderMethodName;
    private final String _builderGetterName;

    public ReflectionStripConverter(final StripInstrumentType type, final String instrumentProviderName, final String builderMethodName,
        final Function2<String, String, String> renamingFunction) {
      super(instrumentProviderName, renamingFunction);
      _type = type;
      _builderMethodName = builderMethodName;
      final StringBuilder sb = new StringBuilder("get");
      sb.append(_builderMethodName.substring(0, 1).toUpperCase());
      sb.append(_builderMethodName.substring(1));
      _builderGetterName = sb.toString();
    }

    public ReflectionStripConverter(final StripInstrumentType type, final String instrumentProviderName, final String builderGetterName,
        final String builderMethodName, final Function2<String, String, String> renamingFunction) {
      super(instrumentProviderName, renamingFunction);
      _type = type;
      _builderGetterName = builderGetterName;
      _builderMethodName = builderMethodName;
    }

    @Override
    public Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders,
        final String currency) {
      try {
        final Method getMethod = CurveNodeIdMapper.class.getMethod(_builderGetterName, (Class<?>[]) null);
        if (getMethod.invoke(idMapper, (Object[]) null) != null) {
          s_logger.warn("Nodes already exist in mapper called {}; overwriting with {}", _type);
        }
        final Builder newBuilder = copyToBuilder(idMapper, currency);
        final Method builderMethod = Builder.class.getMethod(_builderMethodName, Map.class);
        return (Builder) builderMethod.invoke(newBuilder, instrumentProviders);
      } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        throw new RuntimeException(e.getMessage());
      }
    }

  }

}
