/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter;

import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.BANKERS_ACCEPTANCE;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.BASIS_SWAP;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CASH;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CDOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CONTINUOUS_ZERO_DEPOSIT;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.EURIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FRA;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FRA_3M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FRA_6M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FUTURE;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.LIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.OIS_SWAP;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.PERIODIC_ZERO_DEPOSIT;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SIMPLE_ZERO_DEPOSIT;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SPREAD;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.STIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_12M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_28D;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_3M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_6M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.TENOR_SWAP;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.util.time.Tenor;

public class YieldCurveDefinitionConverter {
  private static final Map<StripInstrumentType, StripConverter> STRIP_CONVERTERS = new EnumMap<>(StripInstrumentType.class);

  static {
    final StripConverter exceptionConverter = new ExceptionStripConverter();
    STRIP_CONVERTERS.put(LIBOR, new LiborStripConverter());
    STRIP_CONVERTERS.put(CASH, new CashStripConverter());
    STRIP_CONVERTERS.put(FRA, exceptionConverter);
    STRIP_CONVERTERS.put(FUTURE, new FutureStripConverter());
    STRIP_CONVERTERS.put(BANKERS_ACCEPTANCE, new BankersAcceptanceStripConverter());
    STRIP_CONVERTERS.put(SWAP, exceptionConverter);
    STRIP_CONVERTERS.put(TENOR_SWAP, new TenorSwapStripConverter());
    STRIP_CONVERTERS.put(BASIS_SWAP, new BasisSwapStripConverter());
    STRIP_CONVERTERS.put(OIS_SWAP, new OisStripConverter());
    STRIP_CONVERTERS.put(EURIBOR, new EuriborStripConverter());
    STRIP_CONVERTERS.put(FRA_3M, new Fra3mStripConverter());
    STRIP_CONVERTERS.put(FRA_6M, new Fra6mStripConverter());
    STRIP_CONVERTERS.put(SWAP_3M, new Swap3mStripConverter());
    STRIP_CONVERTERS.put(SWAP_6M, new Swap6mStripConverter());
    STRIP_CONVERTERS.put(SWAP_12M, new Swap12mStripConverter());
    STRIP_CONVERTERS.put(CDOR, new CdorStripConverter());
    STRIP_CONVERTERS.put(CIBOR, new CiborStripConverter());
    STRIP_CONVERTERS.put(STIBOR, new StiborStripConverter());
    STRIP_CONVERTERS.put(SIMPLE_ZERO_DEPOSIT, exceptionConverter);
    STRIP_CONVERTERS.put(PERIODIC_ZERO_DEPOSIT, exceptionConverter);
    STRIP_CONVERTERS.put(CONTINUOUS_ZERO_DEPOSIT, exceptionConverter);
    STRIP_CONVERTERS.put(SPREAD, exceptionConverter);
    STRIP_CONVERTERS.put(SWAP_28D, new Swap28dStripConverter());
  }

  public static InterpolatedCurveDefinition convert(final String curveName, final String currency, final YieldCurveDefinition originalDefinition,
      final Map<String, CurveSpecificationBuilderConfiguration> identifierMap) {
    final Set<CurveNode> nodes = new HashSet<>();
    for (final FixedIncomeStrip strip : originalDefinition.getStrips()) {
      final StripConverter stripConverter = STRIP_CONVERTERS.get(strip.getInstrumentType());
      if (stripConverter == null) {
        throw new UnsupportedOperationException();
      }
      final CurveSpecificationBuilderConfiguration identifiers = identifierMap.get(strip.getConventionName());
      if (identifiers == null) {
        throw new UnsupportedOperationException();
      }
      nodes.add(stripConverter.apply(strip, currency, identifiers));
    }
    return new InterpolatedCurveDefinition(curveName, nodes, originalDefinition.getInterpolatorName(),
        originalDefinition.getRightExtrapolatorName(), originalDefinition.getLeftExtrapolatorName());
  }

  public interface StripConverter {

    public CurveNode apply(FixedIncomeStrip strip, String currency, CurveSpecificationBuilderConfiguration identifiers);
  }

  private static class ExceptionStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      throw new UnsupportedOperationException();
    }

  }

  private static class BankersAcceptanceStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      if (!currency.equals("CAD")) {
        throw new IllegalStateException();
      }
      final int futureNumber = strip.getNumberOfFuturesAfterTenor();
      final Tenor startTenor = strip.getCurveNodePointTime();
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      final Tenor futureTenor = Tenor.THREE_MONTHS;
      final Tenor underlyingTenor = Tenor.THREE_MONTHS;
      final ExternalId underlyingIborIdentifier = identifiers.getCDORSecurity(LocalDate.now(), Tenor.THREE_MONTHS);
      return new RateFutureNode(futureNumber, startTenor, futureTenor, underlyingTenor, underlyingIborIdentifier, curveNodeIdMapperName);
    }
  }

  private static class CashStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      String conventionName;
      if (maturityTenor.equals(Tenor.OVERNIGHT) || maturityTenor.equals(Tenor.DAY) || maturityTenor.equals(Tenor.ONE_DAY)) {
        conventionName = currency + " Overnight";
      } else {
        conventionName = currency + " Deposit";
      }
      final ExternalId convention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, conventionName);
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new CashNode(startTenor, maturityTenor, convention, curveNodeIdMapperName);
    }
  }

  private static class CdorStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      if (!currency.equals("CAD")) {
        throw new IllegalStateException();
      }
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final ExternalId cdorIdentifier = identifiers.getCDORSecurity(LocalDate.now(), maturityTenor);
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new CashNode(startTenor, maturityTenor, cdorIdentifier, curveNodeIdMapperName);
    }
  }

  private static class CiborStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      if (!currency.equals("DKK")) {
        throw new IllegalStateException();
      }
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final ExternalId ciborIdentifier = identifiers.getCiborSecurity(LocalDate.now(), maturityTenor);
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new CashNode(startTenor, maturityTenor, ciborIdentifier, curveNodeIdMapperName);
    }
  }

  private static class EuriborStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      if (!currency.equals("EUR")) {
        throw new IllegalStateException();
      }
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final ExternalId euriborIdentifier = identifiers.getCiborSecurity(LocalDate.now(), maturityTenor);
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new CashNode(startTenor, maturityTenor, euriborIdentifier, curveNodeIdMapperName);
    }
  }

  private static class Fra3mStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor fixingEnd = strip.getCurveNodePointTime();
      final Tenor fixingStart = Tenor.of(fixingEnd.getPeriod().minus(Period.ofMonths(3)));
      final ExternalId underlyingIborIdentifier = identifiers.getLiborSecurity(LocalDate.now(), Tenor.THREE_MONTHS);
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new FRANode(fixingStart, fixingEnd, underlyingIborIdentifier, curveNodeIdMapperName);
    }
  }

  private static class Fra6mStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor fixingEnd = strip.getCurveNodePointTime();
      final Tenor fixingStart = Tenor.of(fixingEnd.getPeriod().minus(Period.ofMonths(6)));
      final ExternalId underlyingIborIdentifier = identifiers.getLiborSecurity(LocalDate.now(), Tenor.SIX_MONTHS);
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new FRANode(fixingStart, fixingEnd, underlyingIborIdentifier, curveNodeIdMapperName);
    }
  }

  private static class FutureStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final int futureNumber = strip.getNumberOfFuturesAfterTenor();
      final Tenor startTenor = strip.getCurveNodePointTime();
      final Tenor futureTenor = Tenor.THREE_MONTHS;
      final Tenor underlyingTenor = Tenor.THREE_MONTHS;
      final ExternalId underlyingIborIdentifier;
      if (currency.equals("EUR")) {
        underlyingIborIdentifier = identifiers.getEuriborSecurity(LocalDate.now(), Tenor.THREE_MONTHS);
      } else {
        underlyingIborIdentifier = identifiers.getLiborSecurity(LocalDate.now(), Tenor.THREE_MONTHS);
      }
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new RateFutureNode(futureNumber, startTenor, futureTenor, underlyingTenor, underlyingIborIdentifier, curveNodeIdMapperName);
    }
  }

  private static class LiborStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final String csbcName = strip.getConventionName();
      final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
      csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
      csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
      csbcSearchRequest.setName(csbcName + "_" + currency);
      final ExternalId liborIdentifier = identifiers.getLiborSecurity(LocalDate.now(), maturityTenor);
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new CashNode(startTenor, maturityTenor, liborIdentifier, curveNodeIdMapperName);
    }
  }

  private static class OisStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " OIS Fixed Leg");
      final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " OIS Overnight Leg");
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName);
    }
  }

  private static class StiborStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      if (!currency.equals("SEK")) {
        throw new IllegalStateException();
      }
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final ExternalId stiborIdentifier = identifiers.getEuriborSecurity(LocalDate.now(), maturityTenor);
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new CashNode(startTenor, maturityTenor, stiborIdentifier, curveNodeIdMapperName);
    }
  }

  private static class Swap3mStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " IRS Fixed Leg");
      final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " 3M IRS Ibor Leg");
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName);
    }
  }

  private static class Swap6mStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " IRS Fixed Leg");
      final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " 6M IRS Ibor Leg");
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName);
    }
  }

  private static class Swap12mStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " IRS Fixed Leg");
      final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " 12M IRS Ibor Leg");
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName);
    }
  }

  private static class Swap28dStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " IRS Fixed Leg");
      final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " 28D IRS Ibor Leg");
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName);
    }
  }

  private static class BasisSwapStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final Tenor payTenor = strip.getPayTenor();
      final Tenor receiveTenor = strip.getReceiveTenor();
      final IndexType indexType = strip.getIndexType();
      String indexString;
      switch (indexType) {
        case Euribor:
        case Libor:
        case Tibor:
          indexString = "Ibor";
          break;
        case BBSW:
        case Swap:
        default:
          indexString = indexType.name();
          break;
      }
      final String payLegConventionName = currency + " " + payTenor.getPeriod().toString().substring(1) + " IRS " + indexString + " Leg";
      final String receiveLegConventionName = currency + " " + receiveTenor.getPeriod().toString().substring(1) + " IRS " + indexString + " Leg";
      final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, payLegConventionName);
      final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, receiveLegConventionName);
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName);
    }

  }

  private static class TenorSwapStripConverter implements StripConverter {

    @Override
    public CurveNode apply(final FixedIncomeStrip strip, final String currency, final CurveSpecificationBuilderConfiguration identifiers) {
      final Tenor startTenor = Tenor.of(Period.ZERO);
      final Tenor maturityTenor = strip.getCurveNodePointTime();
      final Tenor payTenor = strip.getPayTenor();
      final Tenor receiveTenor = strip.getReceiveTenor();
      final IndexType indexType = strip.getIndexType();
      String indexString;
      switch (indexType) {
        case Euribor:
        case Libor:
        case Tibor:
          indexString = "Ibor";
          break;
        case BBSW:
        case Swap:
        default:
          indexString = indexType.name();
          break;
      }
      final String payLegConventionName = currency + " " + payTenor.getPeriod().toString().substring(1) + " IRS " + indexString + " Leg";
      final String receiveLegConventionName = currency + " " + receiveTenor.getPeriod().toString().substring(1) + " IRS " + indexString + " Leg";
      final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, payLegConventionName);
      final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, receiveLegConventionName);
      final String curveNodeIdMapperName = strip.getConventionName() + " " + currency;
      return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName);
    }

  }
}
