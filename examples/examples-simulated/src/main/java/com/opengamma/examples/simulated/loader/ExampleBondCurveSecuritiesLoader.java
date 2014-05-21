/**
 * 
 */
package com.opengamma.examples.simulated.loader;

import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.BondSecurity;

/**
 * @author emcleod
 * Adds the {@link BillSecurity} and {@link BondSecurity} objects to the security master. These
 * securities must be present in the database for the curves to be constructed. The definitions
 * are created in {@link ExampleUSBondCurveConfigurationsPopulator}.
 */
public class ExampleBondCurveSecuritiesLoader extends AbstractPortfolioGeneratorTool {

}
