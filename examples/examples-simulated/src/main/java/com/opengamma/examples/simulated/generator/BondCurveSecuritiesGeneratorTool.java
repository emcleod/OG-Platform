/**
 * 
 */
package com.opengamma.examples.simulated.generator;

import java.util.ArrayList;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.legalentity.impl.InMemoryLegalEntityMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * @author emcleod
 *
 * Generates bill and bond securities that are required for construction of curves
 * containing {@link BillNode} and {@link BondNode}. These securities must be 
 * present in the security database for successful curve construction.
 */
public class BondCurveSecuritiesGeneratorTool extends AbstractSecuritiesGeneratorTool {
  /** The bills and bonds */
  private static final List<ManageableSecurity> SECURITIES = new ArrayList<>();

  static {
    final ExternalId region = ExternalSchemes.countryRegionId(Country.US);
    DayCount dayCount = DayCounts.ACT_360;
    final Frequency frequency = PeriodFrequency.SEMI_ANNUAL;
    final ExternalId legalEntityId = ExternalId.of(InMemoryLegalEntityMaster.DEFAULT_OID_SCHEME, "US Government");
    final ZonedDateTime referenceDate = LocalDate.now().atStartOfDay(ZoneOffset.UTC);
    for (int i = 6; i <= 18; i += 6) {
      final Tenor tenor = Tenor.ofMonths(i);
      final BillSecurity bill = new BillSecurity(Currency.USD, new Expiry(referenceDate.plus(tenor.getPeriod())), referenceDate,
          100, 2, region, SimpleYieldConvention.INTERESTATMTY, dayCount, legalEntityId);
      String suffix;
      if (i < 10) {
        suffix = "00" + Integer.toString(i);
      } else {
        suffix = "0" + Integer.toString(i);
      }
      final String isin = "USB000000" + suffix;
      bill.setName(isin);
      bill.setExternalIdBundle(ExternalSchemes.syntheticSecurityId(isin).toBundle());
      SECURITIES.add(bill);
    }
    dayCount = DayCounts.ACT_ACT_ICMA;
    for (int i = 0; i < 29; i++) {
      final int years = i + 2;
      final int months = years * 12;
      final double coupon = 100 - i / 20.;
      final Tenor tenor = Tenor.ofYears(years);
      final BondSecurity bond = new GovernmentBondSecurity("US TREASURY N/B", "Sovereign", "US", "US GOVERNMENT", Currency.USD, SimpleYieldConvention.US_STREET,
          new Expiry(referenceDate.plus(tenor.getPeriod())), "FIXED", coupon, frequency, dayCount, referenceDate, referenceDate, referenceDate.plusMonths(6),
          100., 1000000, 100, 100, 100, 100);
      String suffix;
      if (years < 10) {
        suffix = "00" + Integer.toString(years);
      } else if (years < 100) {
        suffix = "0" + Integer.toString(years);
      } else {
        suffix = Integer.toString(years);
      }
      final String isin = "UST000000" + suffix;
      bond.setName(isin);
      bond.setExternalIdBundle(ExternalSchemes.syntheticSecurityId(isin).toBundle());
      SECURITIES.add(bond);
    }
  }

  @Override
  public SecuritiesGenerator createSecuritiesGenerator() {
    final SecurityGenerator<ManageableSecurity> securityGenerator = new CollectionSecurityGenerator<>(SECURITIES);
    configure(securityGenerator);
    return new SecuritiesGenerator(securityGenerator, SECURITIES.size());
  }
}
