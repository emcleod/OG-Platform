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
 * Tests the class that adds FRA instrument providers to a {@link CurveNodeIdMapper}
 * from a {@link CurveSpecificationBuilderConfiguration}.
 * 
 * @author elaine
 */
public class FraInstrumentProviderPopulatorTest {
  /** A map from tenor to curve instrument provider for 3m FRAs */
  private static final Map<Tenor, CurveInstrumentProvider> FRA_3M_INSTRUMENTS = new HashMap<>();
  /** A map from tenor to curve instrument provider for 6m FRAs */
  private static final Map<Tenor, CurveInstrumentProvider> FRA_6M_INSTRUMENTS = new HashMap<>();
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A converting class for 3m FRA strips that uses the default renaming function */
  private static final InstrumentProviderPopulator FRA_3M_DEFAULT_PROVIDER = new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M, "getFra3MInstrumentProviders");
  /** A converting class for 3m FRA strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator FRA_3M_RENAMING_PROVIDER;
  /** A converting class for 6m FRA strips that uses the default renaming function */
  private static final InstrumentProviderPopulator FRA_6M_DEFAULT_PROVIDER = new FraInstrumentProviderPopulator(StripInstrumentType.FRA_6M, "getFra6MInstrumentProviders");
  /** A converting class for 6m FRA strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator FRA_6M_RENAMING_PROVIDER;
  /** A map from tenor to curve instrument provider for basis swaps */
  private static final Map<Tenor, CurveInstrumentProvider> BASIS_SWAP_INSTRUMENTS = new HashMap<>();

  static {
    FRA_3M_INSTRUMENTS.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.FRA_3M, ExternalSchemes.OG_SYNTHETIC_TICKER));
    FRA_6M_INSTRUMENTS.put(Tenor.ONE_YEAR, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.FRA_6M, ExternalSchemes.OG_SYNTHETIC_TICKER));
    EMPTY_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .build();
    final Function2<String, String, String> renamingFunction = new Function2<String, String, String>() {

      @Override
      public String apply(final String name, final String currency) {
        return name + " test";
      }

    };
    FRA_3M_RENAMING_PROVIDER = new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M, "getFra3MInstrumentProviders", renamingFunction);
    FRA_6M_RENAMING_PROVIDER = new FraInstrumentProviderPopulator(StripInstrumentType.FRA_6M, "getFra6MInstrumentProviders", renamingFunction);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the instrument
   * provider name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrumentProviderName1() {
    new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M, null);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the instrument
   * provider name is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrumentProviderName2() {
    new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M, null, new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the renaming
   * function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M, "getFra3MInstrumentProviders", null);
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the strip instrument
   * type is not mappable to FRA.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType1() {
    new FraInstrumentProviderPopulator(StripInstrumentType.CASH, "getFra3MInstrumentProviders");
  }

  /**
   * Tests that the constructor throws an IllegalArgumentException if the strip instrument
   * type is not mappable to FRA.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType2() {
    new FraInstrumentProviderPopulator(StripInstrumentType.CASH, "getFra3MInstrumentProviders", new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests the hashCode and equals methods
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(FRA_3M_DEFAULT_PROVIDER, new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M, "getFra3MInstrumentProviders"));
    assertEquals(FRA_3M_DEFAULT_PROVIDER.hashCode(), new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M, "getFra3MInstrumentProviders").hashCode());
    assertFalse(FRA_3M_RENAMING_PROVIDER.equals(new FraInstrumentProviderPopulator(StripInstrumentType.FRA_6M, "getFra6MInstrumentProviders")));
    assertFalse(FRA_3M_RENAMING_PROVIDER.hashCode() == new FraInstrumentProviderPopulator(StripInstrumentType.FRA_6M, "getFra6MInstrumentProviders").hashCode());
  }

  /**
   * Tests that the expected getter is called from the {@link CurveSpecificationBuilderConfiguration}.
   */
  @Test
  public void testExpectedInstrumentProvider() {
    final CurveSpecificationBuilderConfiguration emptyCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertNull(FRA_3M_DEFAULT_PROVIDER.getInstrumentProviders(emptyCsbc));
    assertNull(FRA_3M_RENAMING_PROVIDER.getInstrumentProviders(emptyCsbc));
    final CurveSpecificationBuilderConfiguration basisSwapCsbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null, null,
        null, null, null, null, null, BASIS_SWAP_INSTRUMENTS, null, null, null, null, null, null);
    assertNull(FRA_3M_DEFAULT_PROVIDER.getInstrumentProviders(basisSwapCsbc));
    assertNull(FRA_3M_DEFAULT_PROVIDER.getInstrumentProviders(basisSwapCsbc));
    CurveSpecificationBuilderConfiguration fraTypeCsbc = new CurveSpecificationBuilderConfiguration(null, FRA_3M_INSTRUMENTS, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertEquals(FRA_3M_INSTRUMENTS, FRA_3M_DEFAULT_PROVIDER.getInstrumentProviders(fraTypeCsbc));
    assertEquals(FRA_3M_INSTRUMENTS, FRA_3M_RENAMING_PROVIDER.getInstrumentProviders(fraTypeCsbc));
    fraTypeCsbc = new CurveSpecificationBuilderConfiguration(null, null, FRA_6M_INSTRUMENTS, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null);
    assertEquals(FRA_6M_INSTRUMENTS, FRA_6M_DEFAULT_PROVIDER.getInstrumentProviders(fraTypeCsbc));
    assertEquals(FRA_6M_INSTRUMENTS, FRA_6M_RENAMING_PROVIDER.getInstrumentProviders(fraTypeCsbc));
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider
   * map.
   */
  @Test
  public void testCreateBuilder() {
    CurveNodeIdMapper fraMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(FRA_3M_INSTRUMENTS)
        .build();
    assertEquals(fraMapper, FRA_3M_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, FRA_3M_INSTRUMENTS, NAME).build());
    fraMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .fraNodeIds(FRA_3M_INSTRUMENTS)
        .build();
    assertEquals(fraMapper, FRA_3M_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, FRA_3M_INSTRUMENTS, "Name test").build());
    fraMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(FRA_6M_INSTRUMENTS)
        .build();
    assertEquals(fraMapper, FRA_6M_DEFAULT_PROVIDER.createBuilder(EMPTY_MAPPER, FRA_6M_INSTRUMENTS, NAME).build());
    fraMapper = CurveNodeIdMapper.builder()
        .name("Name test")
        .fraNodeIds(FRA_6M_INSTRUMENTS)
        .build();
    assertEquals(fraMapper, FRA_3M_RENAMING_PROVIDER.createBuilder(EMPTY_MAPPER, FRA_6M_INSTRUMENTS, "Name test").build());
  }

  /**
   * Tests that any FRA nodes that are already present in the mapper are re-written and
   * that any existing FRA nodes are overwritten. 
   * {@link InstrumentProviderPopulator#createBuilder(CurveNodeIdMapper, Map, String)} does
   * not use the renaming function, so it is expected that the result is the same as for
   * a default renaming function.
   */
  @Test
  public void testOverwriteFRANodes() {
    final CurveNodeIdMapper fra3mMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(BASIS_SWAP_INSTRUMENTS)
        .fraNodeIds(FRA_3M_INSTRUMENTS)
        .build();
    final CurveNodeIdMapper fra6mMapper = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(BASIS_SWAP_INSTRUMENTS)
        .fraNodeIds(FRA_6M_INSTRUMENTS)
        .build();
    assertEquals(fra3mMapper, FRA_3M_DEFAULT_PROVIDER.createBuilder(fra6mMapper, FRA_3M_INSTRUMENTS, NAME).build());
    assertEquals(fra3mMapper, FRA_3M_RENAMING_PROVIDER.createBuilder(fra6mMapper, FRA_3M_INSTRUMENTS, NAME).build());
    assertEquals(fra6mMapper, FRA_6M_DEFAULT_PROVIDER.createBuilder(fra3mMapper, FRA_6M_INSTRUMENTS, NAME).build());
    assertEquals(fra6mMapper, FRA_6M_RENAMING_PROVIDER.createBuilder(fra3mMapper, FRA_6M_INSTRUMENTS, NAME).build());
  }
}
