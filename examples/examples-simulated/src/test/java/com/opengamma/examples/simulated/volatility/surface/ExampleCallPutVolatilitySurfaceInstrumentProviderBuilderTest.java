/**
 * 
 */
package com.opengamma.examples.simulated.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.examples.simulated.FinancialTestBase;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the fudge serialization and deserialization of {@link ExampleCallPutVolatilitySurfaceInstrumentProvider}.
 * @author emcleod
 */
@Test(groups = TestGroup.UNIT)
public class ExampleCallPutVolatilitySurfaceInstrumentProviderBuilderTest extends FinancialTestBase {

  /**
   * Tests a full fudge serialization cycle.
   */
  @Test
  public void test() {
    final ExampleCallPutVolatilitySurfaceInstrumentProvider provider = new ExampleCallPutVolatilitySurfaceInstrumentProvider("AAA", MarketDataRequirementNames.IMPLIED_VOLATILITY, 100.);
    assertEquals(provider, cycleObject(ExampleCallPutVolatilitySurfaceInstrumentProvider.class, provider));
  }
}
