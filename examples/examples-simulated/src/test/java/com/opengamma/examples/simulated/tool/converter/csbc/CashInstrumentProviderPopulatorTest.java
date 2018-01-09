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
 * Tests the class that adds cash instrument providers to a {@link CurveNodeIdMapper}
 * from a {@link CurveSpecificationBuilderConfiguration}.
 * 
 * @author elaine
 */
public class CashInstrumentProviderPopulatorTest {
  /** A map from tenor to curve instrument provider for deposits */
  private static final Map<Tenor, CurveInstrumentProvider> CASH_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for CDOR */
  private static final Map<Tenor, CurveInstrumentProvider> CDOR_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for CIBOR */
  private static final Map<Tenor, CurveInstrumentProvider> CIBOR_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for EURIBOR */
  private static final Map<Tenor, CurveInstrumentProvider> EURIBOR_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for LIBOR */
  private static final Map<Tenor, CurveInstrumentProvider> LIBOR_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for STIBOR */
  private static final Map<Tenor, CurveInstrumentProvider> STIBOR_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for swaps */
  private static final Map<Tenor, CurveInstrumentProvider> SWAP_INSTRUMENTS = new HashMap<>();
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A converting class for cash strips that uses the default renaming function */
  private static final InstrumentProviderPopulator CASH_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CASH, "getCashInstrumentProviders");
  /** A converting class for cash strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator CASH_RENAMING_PROVIDER;
  /** A converting class for CDOR strips that uses the default renaming function */
  private static final InstrumentProviderPopulator CDOR_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CDOR, "getCDORInstrumentProviders");
  /** A converting class for CDOR strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator CDOR_RENAMING_PROVIDER;
  /** A converting class for CIBOR strips that uses the default renaming function */
  private static final InstrumentProviderPopulator CIBOR_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CIBOR, "getCiborInstrumentProviders");
  /** A converting class for CIBOR strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator CIBOR_RENAMING_PROVIDER;
  /** A converting class for EURIBOR strips that uses the default renaming function */
  private static final InstrumentProviderPopulator EURIBOR_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.EURIBOR, "getEuriborInstrumentProviders");
  /** A converting class for EURIBOR strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator EURIBOR_RENAMING_PROVIDER;
  /** A converting class for LIBOR strips that uses the default renaming function */
  private static final InstrumentProviderPopulator LIBOR_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.LIBOR, "getLiborInstrumentProviders");
  /** A converting class for LIBOR strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator LIBOR_RENAMING_PROVIDER;
  /** A converting class for STIBOR strips that uses the default renaming function */
  private static final InstrumentProviderPopulator STIBOR_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.STIBOR, "getStiborInstrumentProviders");
  /** A converting class for STIBOR strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator STIBOR_RENAMING_PROVIDER;

  static {
    CASH_INSTRUMENTS.put(Tenor.ONE_WEEK, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.CASH, ExternalSchemes.OG_SYNTHETIC_TICKER));
    CDOR_INSTRUMENTS.put(Tenor.ONE_WEEK, new SyntheticIdentifierCurveInstrumentProvider(Currency.CAD, StripInstrumentType.CDOR, ExternalSchemes.OG_SYNTHETIC_TICKER));
    CIBOR_INSTRUMENTS.put(Tenor.ONE_WEEK, new SyntheticIdentifierCurveInstrumentProvider(Currency.DKK, StripInstrumentType.CIBOR, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EURIBOR_INSTRUMENTS.put(Tenor.ONE_WEEK, new SyntheticIdentifierCurveInstrumentProvider(Currency.EUR, StripInstrumentType.EURIBOR, ExternalSchemes.OG_SYNTHETIC_TICKER));
    LIBOR_INSTRUMENTS.put(Tenor.ONE_WEEK, new SyntheticIdentifierCurveInstrumentProvider(Currency.GBP, StripInstrumentType.LIBOR, ExternalSchemes.OG_SYNTHETIC_TICKER));
    STIBOR_INSTRUMENTS.put(Tenor.ONE_WEEK, new SyntheticIdentifierCurveInstrumentProvider(Currency.SEK, StripInstrumentType.STIBOR, ExternalSchemes.OG_SYNTHETIC_TICKER));
    SWAP_INSTRUMENTS.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.SWAP_3M, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EMPTY_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .build();
    final Function2<String, String, String> renamingFunction = new Function2<String, String, String>() {

      @Override
      public String apply(final String name, final String currency) {
        return name + " test";
      }

    };
    CASH_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CASH, "getCashInstrumentProviders", renamingFunction);
    CDOR_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CDOR, "getCDORInstrumentProviders", renamingFunction);
    CIBOR_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CIBOR, "getCiborInstrumentProviders", renamingFunction);
    EURIBOR_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.EURIBOR, "getEuriborInstrumentProviders", renamingFunction);
    LIBOR_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.LIBOR, "getLiborInstrumentProviders", renamingFunction);
    STIBOR_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.STIBOR, "getStiborInstrumentProviders", renamingFunction);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the instrument
   * provider name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrumentProviderName1() {
    new CashInstrumentProviderPopulator(StripInstrumentType.CASH, null);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the instrument
   * provider name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrumentProviderName2() {
    new CashInstrumentProviderPopulator(StripInstrumentType.CASH, null, new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the renaming
   * function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new CashInstrumentProviderPopulator(StripInstrumentType.CASH, "getCashInstrumentProviders", null);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the strip instrument
   * type is not mappable to cash.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType1() {
    new CashInstrumentProviderPopulator(StripInstrumentType.BANKERS_ACCEPTANCE, "getCashInstrumentProviders");
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the strip instrument
   * type is not mappable to cash.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType2() {
    new CashInstrumentProviderPopulator(StripInstrumentType.BANKERS_ACCEPTANCE, "getCashInstrumentProviders", new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests the hashCode and equals methods
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(CASH_DEFAULT_PROVIDER, new CashInstrumentProviderPopulator(StripInstrumentType.CASH, "getCashInstrumentProviders"));
    assertEquals(CASH_DEFAULT_PROVIDER.hashCode(), new CashInstrumentProviderPopulator(StripInstrumentType.CASH, "getCashInstrumentProviders").hashCode());
    assertFalse(CASH_RENAMING_PROVIDER.equals(new CashInstrumentProviderPopulator(StripInstrumentType.CASH, "getCashInstrumentProviders")));
    assertFalse(CASH_RENAMING_PROVIDER.hashCode() == new CashInstrumentProviderPopulator(StripInstrumentType.CASH, "getCashInstrumentProviders").hashCode());
    assertFalse(CASH_DEFAULT_PROVIDER.equals(new CashInstrumentProviderPopulator(StripInstrumentType.CDOR, "getCashInstrumentProviders")));
    assertFalse(CASH_DEFAULT_PROVIDER.hashCode() == new CashInstrumentProviderPopulator(StripInstrumentType.CDOR, "getCashInstrumentProviders").hashCode());
    assertFalse(CASH_DEFAULT_PROVIDER.equals(new CashInstrumentProviderPopulator(StripInstrumentType.CDOR, "getCDORInstrumentProviders")));
    assertFalse(CASH_DEFAULT_PROVIDER.hashCode() == new CashInstrumentProviderPopulator(StripInstrumentType.CDOR, "getCDORInstrumentProviders").hashCode());
  }

  /**
   * Tests that the expected getter is called from the {@link CurveSpecificationBuilderConfiguration}.
   */
  @Test
  public void testExpectedInstrumentProvider() {
    final CurveSpecificationBuilderConfiguration emptyCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertNull(CASH_DEFAULT_PROVIDER.getInstrumentProviders(emptyCsbc));
    assertNull(CASH_RENAMING_PROVIDER.getInstrumentProviders(emptyCsbc));
    final CurveSpecificationBuilderConfiguration swapCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null,
        null, null, null, SWAP_INSTRUMENTS, null, null, null, null, null, null, null, null);
    assertNull(CASH_DEFAULT_PROVIDER.getInstrumentProviders(swapCsbc));
    assertNull(CASH_DEFAULT_PROVIDER.getInstrumentProviders(swapCsbc));
    CurveSpecificationBuilderConfiguration cashTypeCsbc = new CurveSpecificationBuilderConfiguration(CASH_INSTRUMENTS, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertEquals(CASH_INSTRUMENTS, CASH_DEFAULT_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    assertEquals(CASH_INSTRUMENTS, CASH_RENAMING_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    cashTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, CDOR_INSTRUMENTS, null, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertEquals(CDOR_INSTRUMENTS, CDOR_DEFAULT_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    assertEquals(CDOR_INSTRUMENTS, CDOR_RENAMING_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    cashTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, CIBOR_INSTRUMENTS, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertEquals(CIBOR_INSTRUMENTS, CIBOR_DEFAULT_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    assertEquals(CIBOR_INSTRUMENTS, CIBOR_RENAMING_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    cashTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, EURIBOR_INSTRUMENTS, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertEquals(EURIBOR_INSTRUMENTS, EURIBOR_DEFAULT_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    assertEquals(EURIBOR_INSTRUMENTS, EURIBOR_RENAMING_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    cashTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, LIBOR_INSTRUMENTS, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertEquals(LIBOR_INSTRUMENTS, LIBOR_DEFAULT_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    assertEquals(LIBOR_INSTRUMENTS, LIBOR_RENAMING_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    cashTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, STIBOR_INSTRUMENTS, null, null, null, null,
        null, null, null, null, null, null, null);
    assertEquals(STIBOR_INSTRUMENTS, STIBOR_DEFAULT_PROVIDER.getInstrumentProviders(cashTypeCsbc));
    assertEquals(STIBOR_INSTRUMENTS, STIBOR_RENAMING_PROVIDER.getInstrumentProviders(cashTypeCsbc));
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider
   * map.
   */
  @Test
  public void testCreateBuilder() {
    CurveNodeIdMapper cashMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CASH_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, CASH_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, CASH_INSTRUMENTS, NAME).build());
    cashMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .cashNodeIds(CASH_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, CASH_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, CASH_INSTRUMENTS, "Name test").build());
    cashMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CDOR_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, CDOR_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, CDOR_INSTRUMENTS, NAME).build());
    cashMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .cashNodeIds(CDOR_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, CASH_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, CDOR_INSTRUMENTS, "Name test").build());
    cashMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CIBOR_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, CIBOR_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, CIBOR_INSTRUMENTS, NAME).build());
    cashMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .cashNodeIds(CIBOR_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, CIBOR_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, CIBOR_INSTRUMENTS, "Name test").build());
    cashMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(EURIBOR_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, EURIBOR_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, EURIBOR_INSTRUMENTS, NAME).build());
    cashMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .cashNodeIds(EURIBOR_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, EURIBOR_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, EURIBOR_INSTRUMENTS, "Name test").build());
    cashMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(LIBOR_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, LIBOR_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, LIBOR_INSTRUMENTS, NAME).build());
    cashMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .cashNodeIds(LIBOR_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, LIBOR_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, LIBOR_INSTRUMENTS, "Name test").build());
    cashMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(STIBOR_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, STIBOR_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, STIBOR_INSTRUMENTS, NAME).build());
    cashMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .cashNodeIds(STIBOR_INSTRUMENTS)
        .build();
    assertEquals(cashMapper, STIBOR_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, STIBOR_INSTRUMENTS, "Name test").build());
  }

  /**
   * Tests that any cash nodes that are already present in the mapper are re-written. 
   * {@link InstrumentProviderPopulator#createBuilder(CurveNodeIdMapper, Map, String)} does
   * not use the renaming function, so it is expected that the result is the same as for
   * a default renaming function.
   */
  @Test
  public void testOverwriteCashNodes() {
    final CurveNodeIdMapper cdorMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CDOR_INSTRUMENTS)
        .swapNodeIds(SWAP_INSTRUMENTS)
        .build();
    final CurveNodeIdMapper cashMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CASH_INSTRUMENTS)
        .swapNodeIds(SWAP_INSTRUMENTS)
        .build();
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CASH_INSTRUMENTS)
        .swapNodeIds(SWAP_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, CASH_DEFAULT_PROVIDER.createBuilder(cdorMapper, CASH_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, CASH_RENAMING_PROVIDER.createBuilder(cdorMapper, CASH_INSTRUMENTS, NAME).build());
    expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CDOR_INSTRUMENTS)
        .swapNodeIds(SWAP_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, CDOR_DEFAULT_PROVIDER.createBuilder(cashMapper, CDOR_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, CDOR_RENAMING_PROVIDER.createBuilder(cashMapper, CDOR_INSTRUMENTS, NAME).build());
    expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(CIBOR_INSTRUMENTS)
        .swapNodeIds(SWAP_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, CIBOR_DEFAULT_PROVIDER.createBuilder(cashMapper, CIBOR_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, CIBOR_RENAMING_PROVIDER.createBuilder(cashMapper, CIBOR_INSTRUMENTS, NAME).build());
    expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(EURIBOR_INSTRUMENTS)
        .swapNodeIds(SWAP_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, EURIBOR_DEFAULT_PROVIDER.createBuilder(cashMapper, EURIBOR_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, EURIBOR_RENAMING_PROVIDER.createBuilder(cashMapper, EURIBOR_INSTRUMENTS, NAME).build());
    expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(LIBOR_INSTRUMENTS)
        .swapNodeIds(SWAP_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, LIBOR_DEFAULT_PROVIDER.createBuilder(cashMapper, LIBOR_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, LIBOR_RENAMING_PROVIDER.createBuilder(cashMapper, LIBOR_INSTRUMENTS, NAME).build());
    expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(STIBOR_INSTRUMENTS)
        .swapNodeIds(SWAP_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, STIBOR_DEFAULT_PROVIDER.createBuilder(cashMapper, STIBOR_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, STIBOR_RENAMING_PROVIDER.createBuilder(cashMapper, STIBOR_INSTRUMENTS, NAME).build());
  }
}
