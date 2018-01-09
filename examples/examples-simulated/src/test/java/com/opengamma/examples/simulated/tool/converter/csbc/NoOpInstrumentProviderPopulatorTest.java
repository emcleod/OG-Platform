/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.SyntheticIdentifierCurveInstrumentProvider;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Tests the no-op instrument provider converter.
 * 
 * @author elaine
 */
public class NoOpInstrumentProviderPopulatorTest {
  /** The no-op instrument provider populator */
  private static final InstrumentProviderPopulator NO_OP = new NoOpInstrumentProviderPopulator(StripInstrumentType.BANKERS_ACCEPTANCE);
  /** A map from tenor to cash tickers */
  private static final Map<Tenor, CurveInstrumentProvider> CASH_PROVIDER = new HashMap<>();
  /** A map from tenor to libor tickers */
  private static final Map<Tenor, CurveInstrumentProvider> FRA_PROVIDER = new HashMap<>();
  /** A mapper containing cash and rate future mappings */
  private static final CurveNodeIdMapper MAPPER;

  static {
    CASH_PROVIDER.put(Tenor.ONE_DAY, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.CASH, ExternalSchemes.OG_SYNTHETIC_TICKER));
    FRA_PROVIDER.put(Tenor.ONE_DAY, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.FRA_3M, ExternalSchemes.OG_SYNTHETIC_TICKER));
    MAPPER = CurveNodeIdMapper.builder()
        .name("Name")
        .cashNodeIds(CASH_PROVIDER)
        .fraNodeIds(FRA_PROVIDER)
        .build();
  }

  /**
   * Tests that the correct exception is thrown for a null strip instrument type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStripInstrumentType() {
    new NoOpInstrumentProviderPopulator(null);
  }

  /**
   * Tests that an empty map is returned.
   */
  @Test
  public void testGetInstrumentProviders() {
    final Map<Tenor, CurveInstrumentProvider> map = NO_OP.getInstrumentProviders(null);
    assertNotNull(map);
    assertTrue(map.isEmpty());
  }

  /**
   * Tests that no providers are added to the curve node id mapper.
   */
  @Test
  public void testCreateBuilder() {
    final Map<Tenor, CurveInstrumentProvider> swaps = new HashMap<>();
    swaps.put(Tenor.ONE_DAY, new SyntheticIdentifierCurveInstrumentProvider(Currency.USD, StripInstrumentType.SWAP_3M, ExternalSchemes.OG_SYNTHETIC_TICKER));
    assertEquals(MAPPER, NO_OP.createBuilder(MAPPER, swaps, "Name").build());
  }

}
