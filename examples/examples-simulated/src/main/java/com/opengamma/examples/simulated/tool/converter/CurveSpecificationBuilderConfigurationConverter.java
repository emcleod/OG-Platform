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
import java.util.Collections;
import java.util.EnumMap;
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
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

public class CurveSpecificationBuilderConfigurationConverter {
  private static final Logger s_logger = LoggerFactory.getLogger(CurveSpecificationBuilderConfigurationConverter.class);
  private static final Map<StripInstrumentType, StripConverter> CONVERTERS = new EnumMap<>(StripInstrumentType.class);

  static {
    CONVERTERS.put(BANKERS_ACCEPTANCE, new NoOpStripConverter(BANKERS_ACCEPTANCE));
    CONVERTERS.put(BASIS_SWAP, new BasisSwapTypeStripConverter(BASIS_SWAP));
    CONVERTERS.put(CASH, new ReflectionStripConverter(CASH, "getCashInstrumentProviders", "cashNodeIds", new DefaultRenamingFunction()));
    CONVERTERS.put(CDOR, new CashTypeStripConverter(CDOR, "getCDORInstrumentProviders", new FixedCurrencyRenamingFunction("CAD", "CDOR")));
    CONVERTERS.put(CIBOR, new CashTypeStripConverter(CIBOR, "getCiborInstrumentProviders", new FixedCurrencyRenamingFunction("DKK", "Cibor")));
    CONVERTERS.put(CONTINUOUS_ZERO_DEPOSIT, new ContinuouslyCompoundedZeroDepositStripConverter());
    CONVERTERS.put(EURIBOR, new CashTypeStripConverter(EURIBOR, "getEuriborInstrumentProviders", new FixedCurrencyRenamingFunction("EUR", "Euribor")));
    CONVERTERS.put(FRA, new NoOpStripConverter(FRA));
    CONVERTERS.put(FRA_3M, new FraTypeStripConverter(FRA_3M, "getFra3MInstrumentProviders", new DefaultRenamingFunction("3m")));
    CONVERTERS.put(FRA_6M, new FraTypeStripConverter(FRA_6M, "getFra6MInstrumentProviders", new DefaultRenamingFunction("6m")));
    CONVERTERS.put(FUTURE, new FutureTypeStripConverter(FUTURE));
    CONVERTERS.put(LIBOR, new CashTypeStripConverter(LIBOR, "getLiborInstrumentProviders", new DefaultRenamingFunction("Libor")));
    CONVERTERS.put(OIS_SWAP, new SwapTypeStripConverter(OIS_SWAP, "getOISSwapInstrumentProviders"));
    CONVERTERS.put(PERIODIC_ZERO_DEPOSIT, new PeriodicZeroDepositStripConverter());
    CONVERTERS.put(SIMPLE_ZERO_DEPOSIT, new NoOpStripConverter(SIMPLE_ZERO_DEPOSIT));
    CONVERTERS.put(SPREAD, new NoOpStripConverter(SPREAD));
    CONVERTERS.put(STIBOR, new CashTypeStripConverter(STIBOR, "getStiborInstrumentProviders", new FixedCurrencyRenamingFunction("SEK", "Stibor")));
    CONVERTERS.put(SWAP, new NoOpStripConverter(SWAP));
    CONVERTERS.put(SWAP_28D, new SwapTypeStripConverter(SWAP_28D, "getSwap28DInstrumentProviders", new DefaultRenamingFunction("28d")));
    CONVERTERS.put(SWAP_3M, new SwapTypeStripConverter(SWAP_3M, "getSwap3MInstrumentProviders", new DefaultRenamingFunction("3m")));
    CONVERTERS.put(SWAP_6M, new SwapTypeStripConverter(SWAP_6M, "getSwap6MInstrumentProviders", new DefaultRenamingFunction("6m")));
    CONVERTERS.put(SWAP_12M, new SwapTypeStripConverter(SWAP_12M, "getSwap12MInstrumentProviders", new DefaultRenamingFunction("12m")));
    CONVERTERS.put(TENOR_SWAP, new BasisSwapTypeStripConverter(TENOR_SWAP));
  }

  public static Collection<CurveNodeIdMapper> convert(final String currency, final Map<String, CurveSpecificationBuilderConfiguration> configMap) {
    return convert(currency, configMap, CONVERTERS);
  }

  public static Collection<CurveNodeIdMapper> convert(final String currency, final Map<String, CurveSpecificationBuilderConfiguration> configMap,
      final Map<StripInstrumentType, StripConverter> converters) {
    final Map<String, CurveNodeIdMapper.Builder> convertedWithNames = new HashMap<>();
    for (final Map.Entry<String, CurveSpecificationBuilderConfiguration> convertedEntry : configMap.entrySet()) {
      final String originalName = convertedEntry.getKey();
      final CurveSpecificationBuilderConfiguration originalConfig = convertedEntry.getValue();
      for (final Map.Entry<StripInstrumentType, StripConverter> entry : converters.entrySet()) {
        final StripConverter converter = entry.getValue();
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

  public static class DefaultRenamingFunction implements Function2<String, String, String> {
    private final String _name;

    public DefaultRenamingFunction() {
      _name = null;
    }

    public DefaultRenamingFunction(final String name) {
      _name = name;
    }

    @Override
    public String apply(final String name, final String currency) {
      return _name == null ? name + " " + currency : name + " " + _name + " " + currency;
    }

  }

  public static class FixedCurrencyRenamingFunction implements Function2<String, String, String> {
    private final String _currency;
    private final String _name;

    public FixedCurrencyRenamingFunction(final String currency) {
      _currency = currency;
      _name = null;
    }

    public FixedCurrencyRenamingFunction(final String currency, final String name) {
      _currency = currency;
      _name = name;
    }

    @Override
    public String apply(final String name, final String currency) {
      return _name == null ? name + " " + _currency : name + " " + _name + " " + _currency;
    }
  }

  public abstract static class StripConverter {
    static final Logger s_logger = LoggerFactory.getLogger(CurveSpecificationBuilderConfigurationConverter.StripConverter.class);
    private final String _instrumentProviderName;
    private final Function2<String, String, String> _renamingFunction;

    public StripConverter(final String instrumentProviderName) {
      this(instrumentProviderName, new DefaultRenamingFunction());
    }

    public StripConverter(final String instrumentProviderName, final Function2<String, String, String> renamingFunction) {
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

    //TODO extract to a class that extends the curve node id mapper builder, if possible
    CurveNodeIdMapper.Builder copyToBuilder(final CurveNodeIdMapper mapper) {
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

  public static class NoOpStripConverter extends StripConverter {
    private final StripInstrumentType _type;

    public NoOpStripConverter(final StripInstrumentType type) {
      super(null);
      _type = type;
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      s_logger.error("Cannot convert strips of type {}", _type);
      return Collections.emptyMap();
    }

  }

  public static class ReflectionStripConverter extends StripConverter {
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
    public Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      try {
        final Method getMethod = CurveNodeIdMapper.class.getMethod(_builderGetterName, (Class<?>[]) null);
        if (getMethod.invoke(idMapper, (Object[]) null) != null) {
          s_logger.warn("Nodes already exist in mapper called {}; overwriting with {}", _type);
        }
        final Builder newBuilder = copyToBuilder(idMapper);
        final Method builderMethod = Builder.class.getMethod(_builderMethodName, Map.class);
        return (Builder) builderMethod.invoke(newBuilder, instrumentProviders);
      } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        throw new RuntimeException(e.getMessage());
      }
    }

  }

  public static class CashTypeStripConverter extends StripConverter {
    private final StripInstrumentType _type;

    public CashTypeStripConverter(final StripInstrumentType type, final String instrumentProviderName) {
      this(type, instrumentProviderName, new DefaultRenamingFunction());
    }

    public CashTypeStripConverter(final StripInstrumentType type, final String instrumentProviderName,
        final Function2<String, String, String> renamingFunction) {
      super(instrumentProviderName, renamingFunction);
      _type = type;
    }

    @Override
    public Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getCashNodeIds() != null) {
        s_logger.warn("Cash nodes already exist in mapper called {}: overwriting with {}", idMapper.getName(), _type);
      }
      return copyToBuilder(idMapper).cashNodeIds(instrumentProviders);
    }

  }

  public static class FutureTypeStripConverter extends StripConverter {
    private final StripInstrumentType _type;

    public FutureTypeStripConverter(final StripInstrumentType type) {
      this(type, new DefaultRenamingFunction());
    }

    public FutureTypeStripConverter(final StripInstrumentType type, final Function2<String, String, String> renamingFunction) {
      super(null, renamingFunction);
      _type = type;
    }

    @Override
    public Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getRateFutureNodeIds() != null) {
        s_logger.warn("Rate future nodes already exist in mapper called {}, overwriting with {} futures", idMapper.getName(), _type);
      }
      return copyToBuilder(idMapper).rateFutureNodeIds(instrumentProviders);
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getFutureInstrumentProviders();
    }

  }

  public static class SwapTypeStripConverter extends StripConverter {
    private final StripInstrumentType _type;

    public SwapTypeStripConverter(final StripInstrumentType type, final String instrumentProviderName) {
      this(type, instrumentProviderName, new DefaultRenamingFunction());
    }

    public SwapTypeStripConverter(final StripInstrumentType type, final String instrumentProviderName,
        final Function2<String, String, String> renamingFunction) {
      super(instrumentProviderName, renamingFunction);
      _type = type;
    }

    @Override
    public Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getSwapNodeIds() != null) {
        s_logger.warn("Swap nodes already exist in mapper called {}: overwriting with {}", idMapper.getName(), _type);
      }
      return copyToBuilder(idMapper).swapNodeIds(instrumentProviders);
    }

  }

  public static class FraTypeStripConverter extends StripConverter {
    private final StripInstrumentType _type;

    public FraTypeStripConverter(final StripInstrumentType type, final String instrumentProviderName) {
      this(type, instrumentProviderName, new DefaultRenamingFunction());
    }

    public FraTypeStripConverter(final StripInstrumentType type, final String instrumentProviderName, final Function2<String, String, String> renamingFunction) {
      super(instrumentProviderName, renamingFunction);
      _type = type;
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getFRANodeIds() != null) {
        s_logger.warn("FRA nodes already exist in mapper called {}: overwriting with {}", idMapper.getName(), _type);
      }
      return copyToBuilder(idMapper).fraNodeIds(instrumentProviders);
    }

  }

  public static class BasisSwapTypeStripConverter extends StripConverter {
    private final StripInstrumentType _type;

    public BasisSwapTypeStripConverter(final StripInstrumentType type) {
      super(null);
      _type = type;
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getSwapNodeIds() != null) {
        s_logger.warn("Swap node ids already exist in mapper called {}: overwriting with {}", idMapper.getName(), _type);
      }
      return copyToBuilder(idMapper).swapNodeIds(instrumentProviders);
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

  }

  public static class PeriodicZeroDepositStripConverter extends StripConverter {

    public PeriodicZeroDepositStripConverter() {
      super(null);
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

  public static class ContinuouslyCompoundedZeroDepositStripConverter extends StripConverter {

    public ContinuouslyCompoundedZeroDepositStripConverter() {
      super(null);
    }

    @Override
    public CurveNodeIdMapper.Builder createBuilder(final CurveNodeIdMapper idMapper, final Map<Tenor, CurveInstrumentProvider> instrumentProviders) {
      if (idMapper.getPeriodicallyCompoundedRateNodeIds() != null) {
        s_logger.warn("Continuously compounded rate nodes already exist in mapper called {}: overwriting", idMapper.getName());
      }
      return copyToBuilder(idMapper).continuouslyCompoundedRateNodeIds(instrumentProviders);
    }

    @Override
    public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration identifiers) {
      return identifiers.getContinuousZeroDepositInstrumentProviders();
    }

  }
}
