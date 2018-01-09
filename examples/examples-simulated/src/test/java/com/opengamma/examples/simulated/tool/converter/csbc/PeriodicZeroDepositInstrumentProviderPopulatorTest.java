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
 * Tests the class that adds periodic zero deposit instrument providers to a {@link CurveNodeIdMapper} from 
 * a {@link CurveSpecificationBuilderConfiguration}.
 *
 * @author elaine
 */
public class PeriodicZeroDepositInstrumentProviderPopulatorTest {
  /** A map from tenor to synthetic USD periodic zero deposits */
  private static final Map<Tenor, CurveInstrumentProvider> USD_PROVIDER = new HashMap<>();
  /** A map from tenor to synthetic EUR periodic zero deposits */
  private static final Map<Tenor, CurveInstrumentProvider> EUR_PROVIDER = new HashMap<>();
  /** A map from tenor to cash tickers */
  private static final Map<Tenor, CurveInstrumentProvider> CASH_PROVIDER = new HashMap<>();
  /** An empty CurveSpecificationBuilderConfiguration */
  private static final CurveSpecificationBuilderConfiguration EMPTY_CSBC;
  /** A CurveSpecificationBuilderConfiguration that contains only cash mappings */
  private static final CurveSpecificationBuilderConfiguration CASH_CSBC;
  /** The configuration from which to convert */
  private static final CurveSpecificationBuilderConfiguration CASH_PERIODIC_ZERO_CSBC;
  /** A converting class that uses the default renaming function */
  private static final InstrumentProviderPopulator DEFAULT_PROVIDER = new PeriodicZeroDepositInstrumentProviderPopulator();
  /** A converting class that uses a custom renaming function */
  private static final InstrumentProviderPopulator RENAMING_PROVIDER;
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A mapper containing only cash mappings */
  private static final CurveNodeIdMapper CASH_MAPPER;
  /** A mapper containing only periodic zero mappings */
  private static final CurveNodeIdMapper PERIODIC_ZERO_MAPPER;
  /** A mapper containing only periodic zero mappings that renames the mapper */
  private static final CurveNodeIdMapper PERIODIC_ZERO_RENAMING_MAPPER;
  /** A mapper containing cash and periodic zero mappings */
  private static final CurveNodeIdMapper CASH_PERIODIC_ZERO_MAPPER;
  /** A mapper containing cash and periodic zero mappings that renames the mapper */
  private static final CurveNodeIdMapper CASH_PERIODIC_ZERO_RENAMING_MAPPER;
  /** A mapper containing cash and EUR periodic zero mappings */
  private static final CurveNodeIdMapper EUR_CASH_PERIODIC_ZERO_MAPPER;

  static {
    USD_PROVIDER.put(Tenor.ONE_DAY, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.PERIODIC_ZERO_DEPOSIT, ExternalSchemes.OG_SYNTHETIC_TICKER));
    USD_PROVIDER.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.PERIODIC_ZERO_DEPOSIT, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EUR_PROVIDER.put(Tenor.ONE_DAY, new SyntheticIdentifierCurveInstrumentProvider(Currency.EUR, StripInstrumentType.PERIODIC_ZERO_DEPOSIT, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EUR_PROVIDER.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.EUR, StripInstrumentType.PERIODIC_ZERO_DEPOSIT, ExternalSchemes.OG_SYNTHETIC_TICKER));
    CASH_PROVIDER.put(Tenor.ONE_DAY, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.CASH, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EMPTY_CSBC = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null);
    CASH_CSBC = new CurveSpecificationBuilderConfiguration(CASH_PROVIDER, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null);
    CASH_PERIODIC_ZERO_CSBC = new CurveSpecificationBuilderConfiguration(CASH_PROVIDER, null, null, null, null, null, null, null,
        USD_PROVIDER, null, null, null, null, null, null, USD_PROVIDER, null, null, null);
    final Function2<String, String, String> renamingFunction = new Function2<String, String, String>() {

      @Override
      public String apply(final String name, final String currency) {
        return name + " " + currency + " test";
      }

    };
    RENAMING_PROVIDER = new PeriodicZeroDepositInstrumentProviderPopulator(renamingFunction);
    EMPTY_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .build();
    CASH_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CASH_PROVIDER)
        .build();
    PERIODIC_ZERO_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .periodicallyCompoundedRateNodeIds(USD_PROVIDER)
        .build();
    PERIODIC_ZERO_RENAMING_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME + " test")
        .periodicallyCompoundedRateNodeIds(USD_PROVIDER)
        .build();
    CASH_PERIODIC_ZERO_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CASH_PROVIDER)
        .periodicallyCompoundedRateNodeIds(USD_PROVIDER)
        .build();
    CASH_PERIODIC_ZERO_RENAMING_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME + " test")
        .cashNodeIds(CASH_PROVIDER)
        .periodicallyCompoundedRateNodeIds(USD_PROVIDER)
        .build();
    EUR_CASH_PERIODIC_ZERO_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CASH_PROVIDER)
        .periodicallyCompoundedRateNodeIds(EUR_PROVIDER)
        .build();
  }

  /**
   * Tests that the correct exception is thrown if the renaming function is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new PeriodicZeroDepositInstrumentProviderPopulator(null);
  }

  /**
   * Tests that the {@link CurveSpecificationBuilderConfiguration#getPeriodicZeroDepositInstrumentProviders()} method
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
   * Tests that a curve node id mapper with cash, if originally populated, and periodic zero deposit nodes
   * is created.
   */
  @Test
  public void testCreateBuilder() {
    assertEquals(PERIODIC_ZERO_MAPPER, DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, USD_PROVIDER, "Name").build());
    assertEquals(PERIODIC_ZERO_RENAMING_MAPPER, RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, USD_PROVIDER, "Name test").build());
    assertEquals(CASH_PERIODIC_ZERO_MAPPER, DEFAULT_PROVIDER.createBuilder(CASH_MAPPER, USD_PROVIDER, "Name").build());
    assertEquals(CASH_PERIODIC_ZERO_RENAMING_MAPPER, RENAMING_PROVIDER.createBuilder(CASH_MAPPER, USD_PROVIDER, "Name test").build());
  }

  /**
   * Tests that a mapper with previously-populated periodic zero deposit nodes returns a mapper with other nodes
   * populated but the periodic zero deposit nodes overwritten.
   */
  @Test
  public void testOverwritePeriodicZeroNodes() {
    assertEquals(CASH_PERIODIC_ZERO_MAPPER, DEFAULT_PROVIDER.createBuilder(EUR_CASH_PERIODIC_ZERO_MAPPER, USD_PROVIDER, "Name").build());
    assertEquals(CASH_PERIODIC_ZERO_RENAMING_MAPPER, RENAMING_PROVIDER.createBuilder(EUR_CASH_PERIODIC_ZERO_MAPPER, USD_PROVIDER, "Name test").build());
  }

  /**
   * Tests the hashCode and equals methods
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(DEFAULT_PROVIDER, new PeriodicZeroDepositInstrumentProviderPopulator());
    assertEquals(DEFAULT_PROVIDER.hashCode(), new PeriodicZeroDepositInstrumentProviderPopulator().hashCode());
    assertFalse(RENAMING_PROVIDER.equals(new PeriodicZeroDepositInstrumentProviderPopulator()));
    assertFalse(RENAMING_PROVIDER.hashCode() == new PeriodicZeroDepositInstrumentProviderPopulator().hashCode());
  }
}
