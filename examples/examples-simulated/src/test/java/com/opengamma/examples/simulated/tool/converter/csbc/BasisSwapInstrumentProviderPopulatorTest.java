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
 * from a {@link CurveSpecificationBuilderConfiguration} for basis or tenor swap 
 * nodes.
 * 
 * @author elaine
 */
public class BasisSwapInstrumentProviderPopulatorTest {
  /** A map from tenor to curve instrument provider for basis swaps */
  private static final Map<Tenor, CurveInstrumentProvider> BASIS_SWAP_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for tenor swaps */
  private static final Map<Tenor, CurveInstrumentProvider> TENOR_SWAP_INSTRUMENTS = new HashMap<>();
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A converting class for basis swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator BASIS_SWAP_DEFAULT_PROVIDER = new BasisSwapInstrumentProviderPopulator(StripInstrumentType.BASIS_SWAP);
  /** A converting class for basis swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator BASIS_SWAP_RENAMING_PROVIDER;
  /** A converting class for tenor swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator TENOR_SWAP_DEFAULT_PROVIDER = new BasisSwapInstrumentProviderPopulator(StripInstrumentType.TENOR_SWAP);
  /** A converting class for tenor swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator TENOR_SWAP_RENAMING_PROVIDER;
  /** A map from tenor to curve instrument provider for periodic zero rates */
  private static final Map<Tenor, CurveInstrumentProvider> PERIODIC_ZERO_INSTRUMENTS = new HashMap<>();

  static {
    BASIS_SWAP_INSTRUMENTS.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.BASIS_SWAP, ExternalSchemes.OG_SYNTHETIC_TICKER));
    TENOR_SWAP_INSTRUMENTS.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.TENOR_SWAP, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EMPTY_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .build();
    final Function2<String, String, String> renamingFunction = new Function2<String, String, String>() {

      @Override
      public String apply(final String name, final String currency) {
        return name + " test";
      }

    };
    BASIS_SWAP_RENAMING_PROVIDER = new BasisSwapInstrumentProviderPopulator(StripInstrumentType.BASIS_SWAP, renamingFunction);
    TENOR_SWAP_RENAMING_PROVIDER = new BasisSwapInstrumentProviderPopulator(StripInstrumentType.TENOR_SWAP, renamingFunction);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the renaming
   * function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new BasisSwapInstrumentProviderPopulator(StripInstrumentType.BASIS_SWAP, null);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the strip instrument
   * type is not mappable to FRA.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType1() {
    new BasisSwapInstrumentProviderPopulator(StripInstrumentType.CASH);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the strip instrument
   * type is not mappable to FRA.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType2() {
    new BasisSwapInstrumentProviderPopulator(StripInstrumentType.CASH, new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests the hashCode and equals methods
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(BASIS_SWAP_DEFAULT_PROVIDER, new BasisSwapInstrumentProviderPopulator(StripInstrumentType.BASIS_SWAP));
    assertEquals(BASIS_SWAP_DEFAULT_PROVIDER.hashCode(), new BasisSwapInstrumentProviderPopulator(StripInstrumentType.BASIS_SWAP).hashCode());
    assertFalse(BASIS_SWAP_RENAMING_PROVIDER.equals(new BasisSwapInstrumentProviderPopulator(StripInstrumentType.TENOR_SWAP)));
    assertFalse(BASIS_SWAP_RENAMING_PROVIDER.hashCode() == new BasisSwapInstrumentProviderPopulator(StripInstrumentType.TENOR_SWAP).hashCode());
  }

  /**
   * Tests that the expected getter is called from the {@link CurveSpecificationBuilderConfiguration}.
   */
  @Test
  public void testExpectedInstrumentProvider() {
    final CurveSpecificationBuilderConfiguration emptyCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertNull(BASIS_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(emptyCsbc));
    assertNull(BASIS_SWAP_RENAMING_PROVIDER.getInstrumentProviders(emptyCsbc));
    final CurveSpecificationBuilderConfiguration zeroCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, PERIODIC_ZERO_INSTRUMENTS, null, null, null);
    assertNull(BASIS_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(zeroCsbc));
    assertNull(BASIS_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(zeroCsbc));
    CurveSpecificationBuilderConfiguration basisSwapTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, null, null, BASIS_SWAP_INSTRUMENTS,
        null, null, null, null, null, null, null);
    assertEquals(BASIS_SWAP_INSTRUMENTS, BASIS_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(basisSwapTypeCsbc));
    assertEquals(BASIS_SWAP_INSTRUMENTS, BASIS_SWAP_RENAMING_PROVIDER.getInstrumentProviders(basisSwapTypeCsbc));
    basisSwapTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, null, null, null,
        TENOR_SWAP_INSTRUMENTS, null, null, null, null, null, null);
    assertEquals(TENOR_SWAP_INSTRUMENTS, TENOR_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(basisSwapTypeCsbc));
    assertEquals(TENOR_SWAP_INSTRUMENTS, TENOR_SWAP_RENAMING_PROVIDER.getInstrumentProviders(basisSwapTypeCsbc));
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider
   * map.
   */
  @Test
  public void testCreateBuilder() {
    CurveNodeIdMapper basisSwapMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(BASIS_SWAP_INSTRUMENTS)
        .build();
    assertEquals(basisSwapMapper, BASIS_SWAP_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, BASIS_SWAP_INSTRUMENTS, NAME).build());
    basisSwapMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .swapNodeIds(BASIS_SWAP_INSTRUMENTS)
        .build();
    assertEquals(basisSwapMapper, BASIS_SWAP_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, BASIS_SWAP_INSTRUMENTS, "Name test").build());
    basisSwapMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(TENOR_SWAP_INSTRUMENTS)
        .build();
    assertEquals(basisSwapMapper, TENOR_SWAP_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, TENOR_SWAP_INSTRUMENTS, NAME).build());
    basisSwapMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .swapNodeIds(TENOR_SWAP_INSTRUMENTS)
        .build();
    assertEquals(basisSwapMapper, BASIS_SWAP_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, TENOR_SWAP_INSTRUMENTS, "Name test").build());
  }

  /**
   * Tests that any swap nodes that are already present in the mapper are re-written and
   * that any existing swap nodes are overwritten. 
   * {@link InstrumentProviderPopulator#createBuilder(CurveNodeIdMapper, Map, String)} does
   * not use the renaming function, so it is expected that the result is the same as for
   * a default renaming function.
   */
  @Test
  public void testOverwriteSwapNodes() {
    final CurveNodeIdMapper basisSwapMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(BASIS_SWAP_INSTRUMENTS)
        .periodicallyCompoundedRateNodeIds(PERIODIC_ZERO_INSTRUMENTS)
        .build();
    final CurveNodeIdMapper tenorSwapMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(TENOR_SWAP_INSTRUMENTS)
        .periodicallyCompoundedRateNodeIds(PERIODIC_ZERO_INSTRUMENTS)
        .build();
    assertEquals(basisSwapMapper, BASIS_SWAP_DEFAULT_PROVIDER.createBuilder(tenorSwapMapper, BASIS_SWAP_INSTRUMENTS, NAME).build());
    assertEquals(basisSwapMapper, BASIS_SWAP_RENAMING_PROVIDER.createBuilder(tenorSwapMapper, BASIS_SWAP_INSTRUMENTS, NAME).build());
    assertEquals(tenorSwapMapper, TENOR_SWAP_DEFAULT_PROVIDER.createBuilder(basisSwapMapper, TENOR_SWAP_INSTRUMENTS, NAME).build());
    assertEquals(tenorSwapMapper, TENOR_SWAP_RENAMING_PROVIDER.createBuilder(basisSwapMapper, TENOR_SWAP_INSTRUMENTS, NAME).build());
  }
}
