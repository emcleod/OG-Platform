/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Abstract base for classes that help to convert {@link CurveSpecificationBuilderConfiguration} to
 * {@link CurveNodeIdMapper}. These classes copy the data for each strip type into the appropriate
 * methods in the node mapper.
 * <p>
 * This class provides renaming functions for a given method that returns
 * the map from {@link Tenor} to {@link CurveInstrumentProvider} in
 * {@link CurveSpecificationBuilderConfiguration} for a particular {@link StripInstrumentType} e.g. 
 * {@link CurveSpecificationBuilderConfiguration#getCashInstrumentProviders()}. 
 * <p>
 * Once the map has been returned, an empty {@link com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder} 
 * is created if the map is null or empty, or the builder is created with existing maps populated. 
 * <p>
 * This class uses reflection to call the correct method, but implementing classes can use
 * a direct method call if they are intended to be used for a single strip type.
 */
public abstract class InstrumentProviderPopulator {
  /** The getter name to call */
  private final String _instrumentProviderMethodName;
  /** The renaming function */
  private final Function2<String, String, String> _renamingFunction;

  /**
   * Sets the renaming function to {@link DefaultCsbcRenamingFunction}.
   * @param instrumentProviderMethodName The instrument provider getter method name, can be null
   */
  public InstrumentProviderPopulator(final String instrumentProviderMethodName) {
    this(instrumentProviderMethodName, new DefaultCsbcRenamingFunction());
  }

  /**
   * @param instrumentProviderMethodName The instrument provider getter method name, can be null
   * @param renamingFunction The renaming function, not null
   */
  public InstrumentProviderPopulator(final String instrumentProviderMethodName, final Function2<String, String, String> renamingFunction) {
    ArgumentChecker.notNull(renamingFunction, "renamingFunction");
    _instrumentProviderMethodName = instrumentProviderMethodName;
    //TODO test here that the method name exists?
    _renamingFunction = renamingFunction;
  }

  /**
   * Returns a (name, {@link com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder}) 
   * pair with the id mapper populated with the appropriate (Tenor, CurveInstrumentProvider) map, 
   * depending on the value of {@link #_instrumentProviderMethodName}. The id mapper argument is
   * copied, not altered in place.
   * @param idMapper The id mapper of which to create a copy, not null
   * @param identifiers The curve specification builder configuration from which to copy the map, not null
   * @param currency The currency of the curve specification builder configuration, not null
   * @return A (id mapper name, builder) pair.
   */
  public Pair<String, CurveNodeIdMapper.Builder> apply(final CurveNodeIdMapper idMapper, final CurveSpecificationBuilderConfiguration identifiers,
      final String currency) {
    ArgumentChecker.notNull(idMapper, "idMapper");
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(currency, "currency");
    final Map<Tenor, CurveInstrumentProvider> instrumentProviders = getInstrumentProviders(identifiers);
    final String mapperName = rename(idMapper.getName(), currency);
    if (instrumentProviders == null || instrumentProviders.isEmpty()) {
      return Pairs.of(mapperName, copyToBuilder(idMapper, mapperName));
    }
    return Pairs.of(mapperName, createBuilder(idMapper, instrumentProviders, mapperName));
  }

  /**
   * Creates a builder from the id mapper, copying any maps that have already been populated,
   * and populates the appropriate map for the strip instrument type.
   * @param idMapper The id mapper, not null
   * @param instrumentProviders The instrument provider map, not null
   * @param mapperName The new name for the mapper, not null
   * @return A curve node id mapper builder with populated instrument provider maps.
   */
  public abstract CurveNodeIdMapper.Builder createBuilder(CurveNodeIdMapper idMapper, Map<Tenor, CurveInstrumentProvider> instrumentProviders,
      String mapperName);

  /**
   * Gets the map of instrument providers from the curve specification builder configuration. 
   * This method uses reflection to call the correct getter and can be overridden in implementing
   * classes that only handle one strip instrument type to improve performance.
   * @param csbc The curve specification builder configuration, not null
   * @return A map from tenor to curve instrument provider.
   * @throw RuntimeException If the getter cannot be found or there is a problem calling the getter.
   */
  public Map<Tenor, CurveInstrumentProvider> getInstrumentProviders(final CurveSpecificationBuilderConfiguration csbc) {
    ArgumentChecker.notNull(csbc, "csbc");
    try {
      final Method method = csbc.getClass().getMethod(_instrumentProviderMethodName, (Class<?>[]) null);
      return (Map<Tenor, CurveInstrumentProvider>) method.invoke(csbc, (Object[]) null);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Applies a renaming function which generates a name for the curve node id mapper from
   * the name of the curve specification builder configuration and optionally the currency.
   * @param name The name of the curve specification builder configuration, not null
   * @param currency The currency string, not null
   * @return The name of the curve node id mapper
   */
  public String rename(final String name, final String currency) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(currency, "currency");
    return _renamingFunction.apply(name, currency);
  }

  /**
   * Creates a curve node id mapper builder from an original id mapper. The original id mapper
   * is not changed, and maps in the builder are copies of those in the original. 
   * <p>
   * This method copies cash, continuously compounded rate, FRA, periodically compounded rate,
   * rate future and swap node maps. Other curve node types are not copied, as there is no 
   * equivalent strip instrument type.
   * @param mapper The mapper, not null
   * @param mapperName The new name for the mapper, not null
   * @return A builder with any non-null maps from the original mapper populated.
   */
  protected CurveNodeIdMapper.Builder copyToBuilder(final CurveNodeIdMapper mapper, final String mapperName) {
    ArgumentChecker.notNull(mapper, "mapper");
    ArgumentChecker.notNull(mapper, "mapperName");
    final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(mapperName);
    if (mapper.getAllTenors().size() == 0) {
      return builder;
    }
    if (mapper.getCashNodeIds() != null) {
      builder.cashNodeIds(new HashMap<>(mapper.getCashNodeIds()));
    }
    if (mapper.getContinuouslyCompoundedRateNodeIds() != null) {
      builder.continuouslyCompoundedRateNodeIds(new HashMap<>(mapper.getContinuouslyCompoundedRateNodeIds()));
    }
    if (mapper.getFRANodeIds() != null) {
      builder.fraNodeIds(new HashMap<>(mapper.getFRANodeIds()));
    }
    if (mapper.getPeriodicallyCompoundedRateNodeIds() != null) {
      builder.periodicallyCompoundedRateNodeIds(new HashMap<>(mapper.getPeriodicallyCompoundedRateNodeIds()));
    }
    if (mapper.getRateFutureNodeIds() != null) {
      builder.rateFutureNodeIds(new HashMap<>(mapper.getRateFutureNodeIds()));
    }
    if (mapper.getSwapNodeIds() != null) {
      builder.swapNodeIds(new HashMap<>(mapper.getSwapNodeIds()));
    }
    return builder;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_instrumentProviderMethodName == null ? 0 : _instrumentProviderMethodName.hashCode());
    result = prime * result + _renamingFunction.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof InstrumentProviderPopulator)) {
      return false;
    }
    final InstrumentProviderPopulator other = (InstrumentProviderPopulator) obj;
    if (!ObjectUtils.equals(_instrumentProviderMethodName, other._instrumentProviderMethodName)) {
      return false;
    }
    if (!ObjectUtils.equals(_renamingFunction, other._renamingFunction)) {
      return false;
    }
    return true;
  }

}
