/**
 * 
 */
package com.opengamma.examples.simulated.generator;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.master.security.ManageableSecurity;

/**
 * @author emcleod
 *
 */
public class SecuritiesGenerator {

  private final SecurityGenerator<? extends ManageableSecurity> _securityGenerator;
  private final int _numberOfSecurities;

  public SecuritiesGenerator(final SecurityGenerator<? extends ManageableSecurity> securityGenerator, final int numberOfSecurities) {
    _securityGenerator = securityGenerator;
    _numberOfSecurities = numberOfSecurities;
  }

  protected SecurityGenerator<? extends ManageableSecurity> getSecurityGenerator() {
    return _securityGenerator;
  }

  public List<ManageableSecurity> createManageableSecurities() {
    final List<ManageableSecurity> securities = new ArrayList<>();
    for (int i = 0; i < _numberOfSecurities; i++) {
      securities.add(_securityGenerator.createSecurity());
    }
    return securities;
  }
}
