/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.SyntheticIdentifierCurveInstrumentProvider;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Function2;
import com.opengamma.util.time.Tenor;

/**
 * Tests the class that adds continuously-compounded zero deposit instrument providers to a {@link CurveNodeIdMapper} from 
 * a {@link CurveSpecificationBuilderConfiguration}.
 *
 * @author elaine
 */
public class ContinuouslyCompoundedZeroDepositInstrumentProviderPopulatorTest {
  /** A map from tenor to synthetic USD continuously-compounded zero deposits */
  private static final Map<Tenor, CurveInstrumentProvider> USD_PROVIDER = new HashMap<>();
  /** A map from tenor to synthetic EUR continuously-compounded zero deposits */
  private static final Map<Tenor, CurveInstrumentProvider> EUR_PROVIDER = new HashMap<>();
  /** A map from tenor to cash tickers */
  private static final Map<Tenor, CurveInstrumentProvider> PERIODIC_PROVIDER = new HashMap<>();
  /** An empty CurveSpecificationBuilderConfiguration */
  private static final CurveSpecificationBuilderConfiguration EMPTY_CSBC;
  /** A CurveSpecificationBuilderConfiguration that contains only cash mappings */
  private static final CurveSpecificationBuilderConfiguration CASH_CSBC;
  /** The configuration from which to convert */
  private static final CurveSpecificationBuilderConfiguration CASH_PERIODIC_ZERO_CSBC;
  /** A converting class that uses the default renaming function */
  private static final InstrumentProviderPopulator DEFAULT_PROVIDER = new ContinuouslyCompoundedRateInstrumentProviderPopulator();
  /** A converting class that uses a custom renaming function */
  private static final InstrumentProviderPopulator RENAMING_PROVIDER;
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A mapper containing only cash mappings */
  private static final CurveNodeIdMapper PERIODIC_MAPPER;
  /** A mapper containing only continuously-compounded zero mappings */
  private static final CurveNodeIdMapper CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER;
  /** A mapper containing only continuously-compounded zero mappings that renames the mapper */
  private static final CurveNodeIdMapper CONTINUOUSLY_COMPOUNDED_ZERO_RENAMING_MAPPER;
  /** A mapper containing cash and continuously-compounded zero mappings */
  private static final CurveNodeIdMapper PERIODIC_CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER;
  /** A mapper containing cash and continuously-compounded zero mappings that renames the mapper */
  private static final CurveNodeIdMapper PERIODIC_CONTINUOUSLY_COMPOUNDED_ZERO_RENAMING_MAPPER;
  /** A mapper containing cash and EUR continuously-compounded zero mappings */
  private static final CurveNodeIdMapper EUR_CASH_CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER;

  static {
    USD_PROVIDER.put(Tenor.ONE_DAY, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.PERIODIC_ZERO_DEPOSIT, ExternalSchemes.OG_SYNTHETIC_TICKER));
    USD_PROVIDER.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.PERIODIC_ZERO_DEPOSIT, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EUR_PROVIDER.put(Tenor.ONE_DAY, new SyntheticIdentifierCurveInstrumentProvider(Currency.EUR, StripInstrumentType.PERIODIC_ZERO_DEPOSIT, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EUR_PROVIDER.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.EUR, StripInstrumentType.PERIODIC_ZERO_DEPOSIT, ExternalSchemes.OG_SYNTHETIC_TICKER));
    PERIODIC_PROVIDER.put(Tenor.ONE_DAY, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.CASH, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EMPTY_CSBC = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null);
    CASH_CSBC = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, PERIODIC_PROVIDER, null, null, null);
    CASH_PERIODIC_ZERO_CSBC = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null,
        USD_PROVIDER, null, null, null, null, null, null, PERIODIC_PROVIDER, USD_PROVIDER, null, null);
    final Function2<String, String, String> renamingFunction = new Function2<String, String, String>() {

      @Override
      public String apply(final String name, final String currency) {
        return name + " " + currency + " test";
      }

    };
    RENAMING_PROVIDER = new ContinuouslyCompoundedRateInstrumentProviderPopulator(renamingFunction);
    EMPTY_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .build();
    PERIODIC_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .periodicallyCompoundedRateNodeIds(PERIODIC_PROVIDER)
        .build();
    CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .continuouslyCompoundedRateNodeIds(USD_PROVIDER)
        .build();
    CONTINUOUSLY_COMPOUNDED_ZERO_RENAMING_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME + " test")
        .continuouslyCompoundedRateNodeIds(USD_PROVIDER)
        .build();
    PERIODIC_CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .periodicallyCompoundedRateNodeIds(PERIODIC_PROVIDER)
        .continuouslyCompoundedRateNodeIds(USD_PROVIDER)
        .build();
    PERIODIC_CONTINUOUSLY_COMPOUNDED_ZERO_RENAMING_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME + " test")
        .periodicallyCompoundedRateNodeIds(PERIODIC_PROVIDER)
        .continuouslyCompoundedRateNodeIds(USD_PROVIDER)
        .build();
    EUR_CASH_CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .periodicallyCompoundedRateNodeIds(PERIODIC_PROVIDER)
        .continuouslyCompoundedRateNodeIds(EUR_PROVIDER)
        .build();
  }

  /**
   * Tests that the correct exception is thrown if the renaming function is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new ContinuouslyCompoundedRateInstrumentProviderPopulator(null);
  }

  /**
   * Tests that the {@link CurveSpecificationBuilderConfiguration#getContinuousZeroDepositInstrumentProviders()} method
   * is called.
   */
  @Test
  public void testExpectedInstrumentProvider() {
    assertNull(DEFAULT_PROVIDER.getInstrumentProviders(EMPTY_CSBC));
    assertNull(RENAMING_PROVIDER.getInstrumentProviders(EMPTY_CSBC));
    assertNull(DEFAULT_PROVIDER.getInstrumentProviders(CASH_CSBC));
    assertNull(RENAMING_PROVIDER.getInstrumentProviders(CASH_CSBC));
    assertEquals(USD_PROVIDER, DEFAULT_PROVIDER.getInstrumentProviders(CASH_PERIODIC_ZERO_CSBC));
    assertEquals(USD_PROVIDER, RENAMING_PROVIDER.getInstrumentProviders(CASH_PERIODIC_ZERO_CSBC));
  }

  /**
   * Tests that a curve node id mapper with cash, if originally populated, and continuously-compounded rate nodes
   * is created.
   */
  @Test
  public void testCreateBuilder() {
    assertEquals(CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER, DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, USD_PROVIDER, "Name").build());
    assertEquals(CONTINUOUSLY_COMPOUNDED_ZERO_RENAMING_MAPPER, RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, USD_PROVIDER, "Name test").build());
    assertEquals(PERIODIC_CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER, DEFAULT_PROVIDER.createBuilder(PERIODIC_MAPPER, USD_PROVIDER, "Name").build());
    assertEquals(PERIODIC_CONTINUOUSLY_COMPOUNDED_ZERO_RENAMING_MAPPER, RENAMING_PROVIDER.createBuilder(PERIODIC_MAPPER, USD_PROVIDER, "Name test").build());
  }

  /**
   * Tests that a mapper with previously-populated continuously-compounded rate nodes returns a mapper with other nodes
   * populated but the continuously-compounded rate nodes overwritten.
   */
  @Test
  public void testOverwritePeriodicZeroNodes() {
    assertEquals(PERIODIC_CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER, DEFAULT_PROVIDER.createBuilder(EUR_CASH_CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER, USD_PROVIDER, "Name").build());
    assertEquals(PERIODIC_CONTINUOUSLY_COMPOUNDED_ZERO_RENAMING_MAPPER, RENAMING_PROVIDER.createBuilder(EUR_CASH_CONTINUOUSLY_COMPOUNDED_ZERO_MAPPER, USD_PROVIDER, "Name test").build());
  }

  /**
   * Tests the hashCode and equals methods
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(DEFAULT_PROVIDER, new ContinuouslyCompoundedRateInstrumentProviderPopulator());
    assertEquals(DEFAULT_PROVIDER.hashCode(), new ContinuouslyCompoundedRateInstrumentProviderPopulator().hashCode());
    assertFalse(RENAMING_PROVIDER.equals(new ContinuouslyCompoundedRateInstrumentProviderPopulator()));
    assertFalse(RENAMING_PROVIDER.hashCode() == new ContinuouslyCompoundedRateInstrumentProviderPopulator().hashCode());
  }
}
