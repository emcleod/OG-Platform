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
 * Tests the class that adds swap instrument providers to a {@link CurveNodeIdMapper}
 * from a {@link CurveSpecificationBuilderConfiguration}.
 * 
 * @author elaine
 */
public class SwapInstrumentProviderPopulatorTest {
  /** A map from tenor to curve instrument provider for 3m swaps */
  private static final Map<Tenor, CurveInstrumentProvider> SWAP_3M_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for 6m swaps */
  private static final Map<Tenor, CurveInstrumentProvider> SWAP_6M_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for 12m swaps*/
  private static final Map<Tenor, CurveInstrumentProvider> SWAP_12M_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for 28d swaps */
  private static final Map<Tenor, CurveInstrumentProvider> SWAP_28D_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for OIS */
  private static final Map<Tenor, CurveInstrumentProvider> OIS_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for swaps */
  private static final Map<Tenor, CurveInstrumentProvider> FRA_INSTRUMENTS = new HashMap<>();
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A converting class for 3m swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator SWAP_3M_DEFAULT_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M, "getSwap3MInstrumentProviders");
  /** A converting class for 3m swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator SWAP_3M_RENAMING_PROVIDER;
  /** A converting class for 6m swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator SWAP_6M_DEFAULT_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_6M, "getSwap6MInstrumentProviders");
  /** A converting class for 6m swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator SWAP_6M_RENAMING_PROVIDER;
  /** A converting class for 12m swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator SWAP_12M_DEFAULT_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_12M, "getSwap12MInstrumentProviders");
  /** A converting class for 12m swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator SWAP_12M_RENAMING_PROVIDER;
  /** A converting class for 28d swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator SWAP_28D_DEFAULT_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_28D, "getSwap28DInstrumentProviders");
  /** A converting class for 28d swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator SWAP_28D_RENAMING_PROVIDER;
  /** A converting class for OIS strips that uses the default renaming function */
  private static final InstrumentProviderPopulator OIS_DEFAULT_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.OIS_SWAP, "getOISSwapInstrumentProviders");
  /** A converting class for OIS strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator OIS_RENAMING_PROVIDER;

  static {
    SWAP_3M_INSTRUMENTS.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.SWAP_3M, ExternalSchemes.OG_SYNTHETIC_TICKER));
    SWAP_6M_INSTRUMENTS.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.SWAP_6M, ExternalSchemes.OG_SYNTHETIC_TICKER));
    SWAP_12M_INSTRUMENTS.put(Tenor.ONE_WEEK, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.SWAP_12M, ExternalSchemes.OG_SYNTHETIC_TICKER));
    SWAP_28D_INSTRUMENTS.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.SWAP_28D, ExternalSchemes.OG_SYNTHETIC_TICKER));
    OIS_INSTRUMENTS.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.OIS_SWAP, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EMPTY_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .build();
    final Function2<String, String, String> renamingFunction = new Function2<String, String, String>() {

      @Override
      public String apply(final String name, final String currency) {
        return name + " test";
      }

    };
    SWAP_3M_RENAMING_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M, "getSwap3MInstrumentProviders", renamingFunction);
    SWAP_6M_RENAMING_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_6M, "getSwap6MInstrumentProviders", renamingFunction);
    SWAP_12M_RENAMING_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_12M, "getSwap12MInstrumentProviders", renamingFunction);
    SWAP_28D_RENAMING_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_28D, "getSwap28DInstrumentProviders", renamingFunction);
    OIS_RENAMING_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.OIS_SWAP, "getOISSwapInstrumentProviders", renamingFunction);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the instrument
   * provider name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrumentProviderName1() {
    new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M, null);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the instrument
   * provider name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrumentProviderName2() {
    new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M, null, new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the renaming
   * function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M, "getSwap3MInstrumentProviders", null);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the strip instrument
   * type is not mappable to swap.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType1() {
    new SwapInstrumentProviderPopulator(StripInstrumentType.CASH, "getSwap3MInstrumentProviders");
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the strip instrument
   * type is not mappable to swap.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType2() {
    new SwapInstrumentProviderPopulator(StripInstrumentType.CASH, "getSwap3MInstrumentProviders", new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests the hashCode and equals methods
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(SWAP_3M_DEFAULT_PROVIDER, new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M, "getSwap3MInstrumentProviders"));
    assertEquals(SWAP_3M_DEFAULT_PROVIDER.hashCode(), new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M, "getSwap3MInstrumentProviders").hashCode());
    assertFalse(SWAP_3M_RENAMING_PROVIDER.equals(new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_6M, "getSwap6MInstrumentProviders")));
    assertFalse(SWAP_3M_RENAMING_PROVIDER.hashCode() == new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_6M, "getSwap6MInstrumentProviders").hashCode());
    assertFalse(SWAP_3M_DEFAULT_PROVIDER.equals(new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_12M, "getSwap12MInstrumentProviders")));
    assertFalse(SWAP_3M_DEFAULT_PROVIDER.hashCode() == new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_12M, "getSwap12MInstrumentProviders").hashCode());
    assertFalse(SWAP_3M_DEFAULT_PROVIDER.equals(new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_28D, "getSwap28DInstrumentProviders")));
    assertFalse(SWAP_3M_DEFAULT_PROVIDER.hashCode() == new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_28D, "getSwap28DInstrumentProviders").hashCode());
    assertFalse(SWAP_3M_DEFAULT_PROVIDER.equals(new SwapInstrumentProviderPopulator(StripInstrumentType.OIS_SWAP, "getOISSwapInstrumentProviders")));
    assertFalse(SWAP_3M_DEFAULT_PROVIDER.hashCode() == new SwapInstrumentProviderPopulator(StripInstrumentType.OIS_SWAP, "getOISSwapInstrumentProviders").hashCode());
  }

  /**
   * Tests that the expected getter is called from the {@link CurveSpecificationBuilderConfiguration}.
   */
  @Test
  public void testExpectedInstrumentProvider() {
    final CurveSpecificationBuilderConfiguration emptyCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertNull(SWAP_3M_DEFAULT_PROVIDER.getInstrumentProviders(emptyCsbc));
    assertNull(SWAP_3M_RENAMING_PROVIDER.getInstrumentProviders(emptyCsbc));
    final CurveSpecificationBuilderConfiguration fraCsbc = new CurveSpecificationBuilderConfiguration(null, FRA_INSTRUMENTS, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null);
    assertNull(SWAP_3M_DEFAULT_PROVIDER.getInstrumentProviders(fraCsbc));
    assertNull(SWAP_3M_DEFAULT_PROVIDER.getInstrumentProviders(fraCsbc));
    CurveSpecificationBuilderConfiguration swapTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, null, SWAP_3M_INSTRUMENTS, null,
        null, null, null, null, null, null, null);
    assertEquals(SWAP_3M_INSTRUMENTS, SWAP_3M_DEFAULT_PROVIDER.getInstrumentProviders(swapTypeCsbc));
    assertEquals(SWAP_3M_INSTRUMENTS, SWAP_3M_RENAMING_PROVIDER.getInstrumentProviders(swapTypeCsbc));
    swapTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, SWAP_6M_INSTRUMENTS, null, null,
        null, null, null, null, null, null, null);
    assertEquals(SWAP_6M_INSTRUMENTS, SWAP_6M_DEFAULT_PROVIDER.getInstrumentProviders(swapTypeCsbc));
    assertEquals(SWAP_6M_INSTRUMENTS, SWAP_6M_RENAMING_PROVIDER.getInstrumentProviders(swapTypeCsbc));
    swapTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, SWAP_12M_INSTRUMENTS, null);
    assertEquals(SWAP_12M_INSTRUMENTS, SWAP_12M_DEFAULT_PROVIDER.getInstrumentProviders(swapTypeCsbc));
    assertEquals(SWAP_12M_INSTRUMENTS, SWAP_12M_RENAMING_PROVIDER.getInstrumentProviders(swapTypeCsbc));
    swapTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, SWAP_28D_INSTRUMENTS);
    assertEquals(SWAP_28D_INSTRUMENTS, SWAP_28D_DEFAULT_PROVIDER.getInstrumentProviders(swapTypeCsbc));
    assertEquals(SWAP_28D_INSTRUMENTS, SWAP_28D_RENAMING_PROVIDER.getInstrumentProviders(swapTypeCsbc));
    swapTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, null, null, null,
        null, OIS_INSTRUMENTS, null, null, null, null, null);
    assertEquals(OIS_INSTRUMENTS, OIS_DEFAULT_PROVIDER.getInstrumentProviders(swapTypeCsbc));
    assertEquals(OIS_INSTRUMENTS, OIS_RENAMING_PROVIDER.getInstrumentProviders(swapTypeCsbc));
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider
   * map.
   */
  @Test
  public void testCreateBuilder() {
    CurveNodeIdMapper swapMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(SWAP_3M_INSTRUMENTS)
        .build();
    assertEquals(swapMapper, SWAP_3M_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, SWAP_3M_INSTRUMENTS, NAME).build());
    swapMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .swapNodeIds(SWAP_3M_INSTRUMENTS)
        .build();
    assertEquals(swapMapper, SWAP_3M_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, SWAP_3M_INSTRUMENTS, "Name test").build());
    swapMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(SWAP_6M_INSTRUMENTS)
        .build();
    assertEquals(swapMapper, SWAP_6M_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, SWAP_6M_INSTRUMENTS, NAME).build());
    swapMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .swapNodeIds(SWAP_6M_INSTRUMENTS)
        .build();
    assertEquals(swapMapper, SWAP_3M_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, SWAP_6M_INSTRUMENTS, "Name test").build());
    swapMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(SWAP_12M_INSTRUMENTS)
        .build();
    assertEquals(swapMapper, SWAP_12M_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, SWAP_12M_INSTRUMENTS, NAME).build());
    swapMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .swapNodeIds(SWAP_12M_INSTRUMENTS)
        .build();
    assertEquals(swapMapper, SWAP_12M_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, SWAP_12M_INSTRUMENTS, "Name test").build());
    swapMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(SWAP_28D_INSTRUMENTS)
        .build();
    assertEquals(swapMapper, SWAP_28D_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, SWAP_28D_INSTRUMENTS, NAME).build());
    swapMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .swapNodeIds(SWAP_28D_INSTRUMENTS)
        .build();
    assertEquals(swapMapper, SWAP_28D_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, SWAP_28D_INSTRUMENTS, "Name test").build());
    swapMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(OIS_INSTRUMENTS)
        .build();
    assertEquals(swapMapper, OIS_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, OIS_INSTRUMENTS, NAME).build());
    swapMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .swapNodeIds(OIS_INSTRUMENTS)
        .build();
    assertEquals(swapMapper, OIS_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, OIS_INSTRUMENTS, "Name test").build());
  }

  /**
   * Tests that any swap nodes that are already present in the mapper are re-written
   * and that any existing swap nodes are overwritten.
   * {@link InstrumentProviderPopulator#createBuilder(CurveNodeIdMapper, Map, String)} does
   * not use the renaming function, so it is expected that the result is the same as for
   * a default renaming function.
   */
  @Test
  public void testOverwriteSwapNodes() {
    final CurveNodeIdMapper swap6mMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(FRA_INSTRUMENTS)
        .swapNodeIds(SWAP_6M_INSTRUMENTS)
        .build();
    final CurveNodeIdMapper swap3mMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(FRA_INSTRUMENTS)
        .swapNodeIds(SWAP_3M_INSTRUMENTS)
        .build();
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(FRA_INSTRUMENTS)
        .swapNodeIds(SWAP_3M_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, SWAP_3M_DEFAULT_PROVIDER.createBuilder(swap6mMapper, SWAP_3M_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, SWAP_3M_RENAMING_PROVIDER.createBuilder(swap6mMapper, SWAP_3M_INSTRUMENTS, NAME).build());
    expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(FRA_INSTRUMENTS)
        .swapNodeIds(SWAP_6M_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, SWAP_6M_DEFAULT_PROVIDER.createBuilder(swap3mMapper, SWAP_6M_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, SWAP_6M_RENAMING_PROVIDER.createBuilder(swap3mMapper, SWAP_6M_INSTRUMENTS, NAME).build());
    expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(FRA_INSTRUMENTS)
        .swapNodeIds(SWAP_12M_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, SWAP_12M_DEFAULT_PROVIDER.createBuilder(swap3mMapper, SWAP_12M_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, SWAP_12M_RENAMING_PROVIDER.createBuilder(swap3mMapper, SWAP_12M_INSTRUMENTS, NAME).build());
    expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(FRA_INSTRUMENTS)
        .swapNodeIds(SWAP_28D_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, SWAP_28D_DEFAULT_PROVIDER.createBuilder(swap3mMapper, SWAP_28D_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, SWAP_28D_RENAMING_PROVIDER.createBuilder(swap3mMapper, SWAP_28D_INSTRUMENTS, NAME).build());
    expectedMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(FRA_INSTRUMENTS)
        .swapNodeIds(OIS_INSTRUMENTS)
        .build();
    assertEquals(expectedMapper, OIS_DEFAULT_PROVIDER.createBuilder(swap3mMapper, OIS_INSTRUMENTS, NAME).build());
    assertEquals(expectedMapper, OIS_RENAMING_PROVIDER.createBuilder(swap3mMapper, OIS_INSTRUMENTS, NAME).build());
  }
}
