/**
 * 
 */
package com.opengamma.examples.simulated.tool;

import java.util.Collections;
import java.util.Map;

import com.opengamma.bbg.loader.IndexLoader;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

public class CurveUnderlyingsLoader {
  private final UnderlyingVisitor _underlyingVisitor = new UnderlyingVisitor();
  private IntegrationToolContext _toolContext;

  public void setToolContext(final IntegrationToolContext toolContext) {
    ArgumentChecker.notNull(toolContext, "toolContext");
    _toolContext = toolContext;
  }

  public IntegrationToolContext getToolContext() {
    return _toolContext;
  }

  public void populateDatabases(final ManageableSecurity security) {
    if (!(security instanceof FinancialSecurity)) {
      return;
    }
    final SecurityMaster securityMaster = _toolContext.getSecurityMaster();
    final Map<String, ManageableSecurity> underlyingId = ((FinancialSecurity) security).accept(_underlyingVisitor);
    return;
    //    SecurityMasterUtils.addOrUpdateSecurity(securityMaster, underlyingId);

  }

  private class UnderlyingVisitor extends FinancialSecurityVisitorSameValueAdapter<Map<String, ManageableSecurity>> {

    public UnderlyingVisitor() {
      super(null);
    }

    @Override
    public Map<String, ManageableSecurity> visitFRASecurity(final FRASecurity security) {
      final ExternalId id = security.getUnderlyingId();
      final IndexLoader indexLoader = new IndexLoader(_toolContext.getBloombergReferenceDataProvider());
      return indexLoader.loadSecurities(Collections.singleton(security.getUnderlyingId().getValue()));
    }
  }
}
