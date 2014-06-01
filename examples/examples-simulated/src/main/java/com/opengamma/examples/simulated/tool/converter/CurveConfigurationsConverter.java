/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.SyntheticFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.SyntheticIdentifierCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePropertiesAndDefaults;
import com.opengamma.financial.convention.initializer.PerCurrencyConventionHelper;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

public class CurveConfigurationsConverter extends AbstractTool<ToolContext> {
  private static final Logger s_logger = LoggerFactory.getLogger(CurveConfigurationsConverter.class);
  private static final Map<String, String> YCD_NAME_REMAPPER = new HashMap<>();
  private static final Map<String, String> CSBC_NAME_REMAPPER = new HashMap<>();
  //TODO pull this information from the securities database
  private static final Map<String, ExternalId> OVERNIGHT_CURVE_REFERENCES = new HashMap<>();

  static {
    YCD_NAME_REMAPPER.put("SECONDARY", "Single");
    CSBC_NAME_REMAPPER.put("SECONDARY", "Default");
    OVERNIGHT_CURVE_REFERENCES.put("USD", ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "USDFF"));
    OVERNIGHT_CURVE_REFERENCES.put("EUR", ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "EONIA"));
    OVERNIGHT_CURVE_REFERENCES.put("GBP", ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "SONIO"));
    OVERNIGHT_CURVE_REFERENCES.put("JPY", ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "TONAR"));
    OVERNIGHT_CURVE_REFERENCES.put("CHF", ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, "TOISTOIS"));
  }

  public static void main(final String[] args) {
    new CurveConfigurationsConverter().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    final ConfigSearchRequest<MultiCurveCalculationConfig> mcccSearchRequest = new ConfigSearchRequest<>();
    mcccSearchRequest.setType(MultiCurveCalculationConfig.class);
    mcccSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
    final ConfigSearchRequest<YieldCurveDefinition> ycdSearchRequest = new ConfigSearchRequest<>();
    ycdSearchRequest.setType(YieldCurveDefinition.class);
    ycdSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
    final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
    csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
    csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
    final ConfigSearchRequest<FXForwardCurveDefinition> ffcdSearchRequest = new ConfigSearchRequest<>();
    ffcdSearchRequest.setType(FXForwardCurveDefinition.class);
    ffcdSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
    final Map<Pair<String, String>, CurveSpecificationBuilderConfiguration> csbcMap = new HashMap<>();
    for (final ConfigDocument mcccDocument : ConfigSearchIterator.iterable(configMaster, mcccSearchRequest)) {
      final MultiCurveCalculationConfig mccc = ((ConfigItem<MultiCurveCalculationConfig>) mcccDocument.getConfig()).getValue();
      final CurveConstructionConfiguration ccc = convertMultiCurveCalculationConfig(configMaster, mccc);
      if (ccc != null) {
        final ConfigItem<CurveConstructionConfiguration> cccConfig = ConfigItem.of(ccc);
        cccConfig.setName(ccc.getName());
        ConfigMasterUtils.storeByName(configMaster, cccConfig);
        final ComputationTargetSpecification target = mccc.getTarget();
        final String currency = target.getUniqueId().getValue();
        final String[] curveNames = mccc.getCurveNames();
        final String calculationMethod = mccc.getCalculationMethod();
        if (MultiYieldCurvePropertiesAndDefaults.PAR_RATE_STRING.equals(calculationMethod) || MultiYieldCurvePropertiesAndDefaults.PRESENT_VALUE_STRING.equals(calculationMethod)) {
          final String currencyString = target.getUniqueId().getValue();
          for (final String ycdName : curveNames) {
            ycdSearchRequest.setName(ycdName + "_" + currencyString);
            for (final ConfigDocument ycdDocument : ConfigSearchIterator.iterable(configMaster, ycdSearchRequest)) {
              final YieldCurveDefinition ycd = ((ConfigItem<YieldCurveDefinition>) ycdDocument.getConfig()).getValue();
              final InterpolatedCurveDefinition definition = convertYieldCurveDefinition(configMaster, ycd, currencyString);
              final ConfigItem<InterpolatedCurveDefinition> icdConfig = ConfigItem.of(definition);
              icdConfig.setName(definition.getName());
              ConfigMasterUtils.storeByName(configMaster, icdConfig);
              for (final FixedIncomeStrip strip : ycd.getStrips()) {
                final String csbcName = strip.getConventionName();
                csbcSearchRequest.setName(csbcName + "_" + currencyString);
                for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
                  final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
                  csbcMap.put(Pairs.of(currency, csbcName + "_" + currencyString), csbc);
                }
              }
            }
          }
        } else if (FXImpliedYieldCurveFunction.FX_IMPLIED.equals(calculationMethod)) { //TODO size of curve name array
          final String currencyPairString = target.getUniqueId().getValue();
          for (final String ffcdName : curveNames) {
            ffcdSearchRequest.setName(ffcdName + "_" + currencyPairString);
            for (final ConfigDocument ffcdDocument : ConfigSearchIterator.iterable(configMaster, ffcdSearchRequest)) {
              final FXForwardCurveDefinition ffcd = ((ConfigItem<FXForwardCurveDefinition>) ffcdDocument.getConfig()).getValue();
              s_logger.error("\t\tFX forward curve definition: {}", ffcdDocument.getName());
            }
          }
        }
      }
    }
    final Map<String, CurveNodeIdMapper.Builder> cnimBuilders = convertCurveSpecificationBuilderConfigurations(csbcMap);
    for (final Map.Entry<String, CurveNodeIdMapper.Builder> entry : cnimBuilders.entrySet()) {
      final ConfigItem<CurveNodeIdMapper> config = ConfigItem.of(entry.getValue().build());
      config.setName(entry.getKey());
      ConfigMasterUtils.storeByName(configMaster, config);
    }
  }

  private static CurveConstructionConfiguration convertMultiCurveCalculationConfig(final ConfigMaster configMaster, final MultiCurveCalculationConfig oldConfig) {
    final LocalDate unused = LocalDate.now();
    final String oldName = oldConfig.getCalculationConfigName();
    String name;
    if (oldName.contains("Config")) {
      name = oldName.substring(0, oldName.indexOf("Config"));
    } else {
      name = oldName;
    }
    final Map<String, List<? extends CurveTypeConfiguration>> curveTypeConfigurations = new HashMap<>();
    final String currency = oldConfig.getTarget().getUniqueId().getValue();
    if (oldConfig.getYieldCurveNames().length == 1) {
      final List<CurveTypeConfiguration> list = new ArrayList<>();
      list.add(new DiscountingCurveTypeConfiguration(currency));
      if (OVERNIGHT_CURVE_REFERENCES.containsKey(currency)) {
        list.add(new OvernightCurveTypeConfiguration(OVERNIGHT_CURVE_REFERENCES.get(currency)));
      }
      final String oldCurveName = oldConfig.getYieldCurveNames()[0];
      final Pattern p = Pattern.compile("\\d+");
      final Matcher m = p.matcher(oldCurveName);
      final Tenor iborTenor;
      if (m.find()) {
        iborTenor = Tenor.ofMonths(Integer.parseInt(m.group()));
      } else {
        iborTenor = currency.equals("USD") || currency.equals("CAD") ? Tenor.THREE_MONTHS : Tenor.SIX_MONTHS;
      }
      final CurveTypeConfiguration iborCurveTypeConfiguration = getIborCurveTypeConfiguration(configMaster, oldConfig, unused, oldCurveName, currency, iborTenor);
      if (iborCurveTypeConfiguration != null) {
        list.add(iborCurveTypeConfiguration);
      }
      String curveName = oldCurveName;
      if (YCD_NAME_REMAPPER.containsKey(name)) {
        curveName = YCD_NAME_REMAPPER.get(name);
      }
      curveName += " " + currency;
      curveTypeConfigurations.put(curveName, list);
    } else if (oldConfig.getYieldCurveNames().length == 2) {
      final List<CurveTypeConfiguration> list = new ArrayList<>();
      list.add(new DiscountingCurveTypeConfiguration(currency));
      if (OVERNIGHT_CURVE_REFERENCES.containsKey(currency)) {
        list.add(new OvernightCurveTypeConfiguration(OVERNIGHT_CURVE_REFERENCES.get(currency)));
      }
      String oldCurveName = oldConfig.getYieldCurveNames()[0];
      String curveName = oldCurveName;
      if (YCD_NAME_REMAPPER.containsKey(name)) {
        curveName = YCD_NAME_REMAPPER.get(name);
      }
      curveName += " " + currency;
      curveTypeConfigurations.put(oldCurveName, list);
      oldCurveName = oldConfig.getYieldCurveNames()[1];
      final Pattern p = Pattern.compile("\\d+");
      final Matcher m = p.matcher(oldCurveName);
      final Tenor iborTenor;
      if (m.find()) {
        iborTenor = Tenor.ofMonths(Integer.parseInt(m.group()));
      } else {
        iborTenor = currency.equals("USD") || currency.equals("CAD") ? Tenor.THREE_MONTHS : Tenor.SIX_MONTHS;
      }
      final CurveTypeConfiguration iborCurveTypeConfiguration = getIborCurveTypeConfiguration(configMaster, oldConfig, unused, oldCurveName, currency, iborTenor);
      if (iborCurveTypeConfiguration != null) {
        list.add(iborCurveTypeConfiguration);
      }
      curveName = oldCurveName;
      if (YCD_NAME_REMAPPER.containsKey(name)) {
        curveName = YCD_NAME_REMAPPER.get(name);
      }
      curveName += " " + currency;
      curveTypeConfigurations.put(curveName, list);
    } else {
      s_logger.error("Cannot handle MultiCurveCalculationConfiguration {}: too many curves ({})", oldName, oldConfig.getYieldCurveNames().length);
      return null;
    }
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypeConfigurations);
    final List<String> exogenousCurveNames = oldConfig.getExogenousConfigData() == null ? new ArrayList<String>() : new ArrayList<>(oldConfig.getExogenousConfigData().keySet());
    return new CurveConstructionConfiguration(name, Arrays.asList(group), exogenousCurveNames);
  }

  private static CurveTypeConfiguration getIborCurveTypeConfiguration(final ConfigMaster configMaster, final MultiCurveCalculationConfig oldConfig,
      final LocalDate unused, final String oldCurveName, final String currency, final Tenor iborTenor) {
    final ConfigSearchRequest<YieldCurveDefinition> ycdSearchRequest = new ConfigSearchRequest<>();
    ycdSearchRequest.setType(YieldCurveDefinition.class);
    ycdSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
    ycdSearchRequest.setName(oldCurveName + "_" + currency);
    for (final ConfigDocument ycdDocument : ConfigSearchIterator.iterable(configMaster, ycdSearchRequest)) {
      final YieldCurveDefinition ycd = ((ConfigItem<YieldCurveDefinition>) ycdDocument.getConfig()).getValue();
      for (final FixedIncomeStrip strip : ycd.getStrips()) {
        if (strip.getCurveNodePointTime().equals(iborTenor)) {
          final String csbcName = strip.getConventionName();
          final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
          csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
          csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
          csbcSearchRequest.setName(csbcName + "_" + currency);
          ExternalId underlyingIborIdentifier = null;
          switch (currency) {
            case "CAD":
              for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
                final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
                underlyingIborIdentifier = csbc.getCDORSecurity(unused, iborTenor);
              }
              break;
            case "DKK":
              for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
                final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
                underlyingIborIdentifier = csbc.getCiborSecurity(unused, iborTenor);
              }
              break;
            case "SEK":
              for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
                final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
                underlyingIborIdentifier = csbc.getStiborSecurity(unused, iborTenor);
              }
              break;
            case "EUR":
              for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
                final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
                underlyingIborIdentifier = csbc.getEuriborSecurity(unused, iborTenor);
              }
              break;
            default:
          }
          if (underlyingIborIdentifier != null) {
            return new IborCurveTypeConfiguration(underlyingIborIdentifier, iborTenor);
          }
          s_logger.error("Could not get underlying ibor id for " + strip);
        }
      }
    }
    return null;
  }

  private static InterpolatedCurveDefinition convertYieldCurveDefinition(final ConfigMaster configMaster, final YieldCurveDefinition oldConfig, final String currency) {
    final LocalDate unused = LocalDate.now();
    final Set<FixedIncomeStrip> strips = oldConfig.getStrips();
    String name = oldConfig.getName().split("_")[0];
    if (YCD_NAME_REMAPPER.containsKey(name)) {
      name = YCD_NAME_REMAPPER.get(name);
    }
    name += " " + currency;

    final Set<CurveNode> nodes = new LinkedHashSet<>();
    for (final FixedIncomeStrip strip : strips) {
      String curveNodeIdMapperName;
      if (CSBC_NAME_REMAPPER.containsKey(strip.getConventionName())) {
        curveNodeIdMapperName = CSBC_NAME_REMAPPER.get(strip.getConventionName());
      } else {
        curveNodeIdMapperName = strip.getConventionName();
      }
      switch (strip.getInstrumentType()) {
        case BANKERS_ACCEPTANCE:
          if (currency.equals("CAD")) {
            final int futureNumber = strip.getNumberOfFuturesAfterTenor();
            final Tenor startTenor = strip.getCurveNodePointTime();
            final Tenor futureTenor = Tenor.THREE_MONTHS;
            final Tenor underlyingTenor = Tenor.THREE_MONTHS;
            final String csbcName = strip.getConventionName();
            final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
            csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
            csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
            csbcSearchRequest.setName(csbcName + "_" + currency);
            ExternalId underlyingIborIdentifier = null;
            for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
              final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
              underlyingIborIdentifier = csbc.getCDORSecurity(unused, Tenor.THREE_MONTHS);
            }
            if (underlyingIborIdentifier == null) {
              s_logger.error("Could not get external id for " + strip);
              break;
            }
            nodes.add(new RateFutureNode(futureNumber, startTenor, futureTenor, underlyingTenor, underlyingIborIdentifier, curveNodeIdMapperName));
            break;
          }
          s_logger.error("{} config contains BANKERS_ACCEPTANCE: ignoring");
          break;
        case CASH: {
          final Tenor startTenor = Tenor.of(Period.ZERO);
          final Tenor maturityTenor = strip.getCurveNodePointTime();
          String conventionName;
          if (maturityTenor.equals(Tenor.OVERNIGHT) || maturityTenor.equals(Tenor.DAY) || maturityTenor.equals(Tenor.ONE_DAY)) {
            conventionName = currency + " Overnight";
          } else {
            conventionName = currency + " Deposit";
          }
          final ExternalId convention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, conventionName);
          curveNodeIdMapperName += " " + currency;
          nodes.add(new CashNode(startTenor, maturityTenor, convention, curveNodeIdMapperName));
          break;
        }
        case CDOR:
          if (currency.equals("CAD")) {
            final Tenor startTenor = Tenor.of(Period.ZERO);
            final Tenor maturityTenor = strip.getCurveNodePointTime();
            final String csbcName = strip.getConventionName();
            final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
            csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
            csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
            csbcSearchRequest.setName(csbcName + "_" + currency);
            ExternalId cdorIdentifier = null;
            for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
              final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
              cdorIdentifier = csbc.getCDORSecurity(unused, maturityTenor);
            }
            if (cdorIdentifier == null) {
              s_logger.error("Could not get external id for " + strip);
              break;
            }
            curveNodeIdMapperName += " 3m " + currency;
            nodes.add(new CashNode(startTenor, maturityTenor, cdorIdentifier, curveNodeIdMapperName));
            break;
          }
          s_logger.error("{} config contains CDOR: ignoring");
          break;
        case CIBOR:
          if (currency.equals("DKK")) {
            final Tenor startTenor = Tenor.of(Period.ZERO);
            final Tenor maturityTenor = strip.getCurveNodePointTime();
            final String csbcName = strip.getConventionName();
            final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
            csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
            csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
            csbcSearchRequest.setName(csbcName + "_" + currency);
            ExternalId ciborIdentifier = null;
            for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
              final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
              ciborIdentifier = csbc.getCiborSecurity(unused, maturityTenor);
            }
            if (ciborIdentifier == null) {
              s_logger.error("Could not get external id for " + strip);
              break;
            }
            curveNodeIdMapperName += " 3m " + currency;
            nodes.add(new CashNode(startTenor, maturityTenor, ciborIdentifier, curveNodeIdMapperName));
            break;
          }
          s_logger.error("{} config contains Cibor: ignoring");
          break;
        case EURIBOR:
          if (currency.equals("EUR")) {
            final Tenor startTenor = Tenor.of(Period.ZERO);
            final Tenor maturityTenor = strip.getCurveNodePointTime();
            final String csbcName = strip.getConventionName();
            final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
            csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
            csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
            csbcSearchRequest.setName(csbcName + "_" + currency);
            ExternalId euriborIdentifier = null;
            for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
              final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
              euriborIdentifier = csbc.getEuriborSecurity(unused, maturityTenor);
            }
            if (euriborIdentifier == null) {
              s_logger.error("Could not get external id for " + strip);
              break;
            }
            curveNodeIdMapperName += " 6m " + currency;
            nodes.add(new CashNode(startTenor, maturityTenor, euriborIdentifier, curveNodeIdMapperName));
            break;
          }
          s_logger.error("{} config contains Euribor: ignoring");
          break;
        case FRA_3M: {
          final Tenor fixingEnd = strip.getCurveNodePointTime();
          final Tenor fixingStart = Tenor.of(fixingEnd.getPeriod().minus(Period.ofMonths(3)));
          final String csbcName = strip.getConventionName();
          final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
          csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
          csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
          csbcSearchRequest.setName(csbcName + "_" + currency);
          ExternalId underlyingIborIdentifier = null;
          for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
            final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
            underlyingIborIdentifier = csbc.getLiborSecurity(unused, Tenor.THREE_MONTHS);
          }
          if (underlyingIborIdentifier == null) {
            s_logger.error("Could not get external id for " + strip);
            break;
          }
          curveNodeIdMapperName += " 3m " + currency;
          nodes.add(new FRANode(fixingStart, fixingEnd, underlyingIborIdentifier, curveNodeIdMapperName));
          break;
        }
        case FRA_6M: {
          final Tenor fixingEnd = strip.getCurveNodePointTime();
          final Tenor fixingStart = Tenor.of(fixingEnd.getPeriod().minus(Period.ofMonths(6)));
          final String csbcName = strip.getConventionName();
          final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
          csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
          csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
          csbcSearchRequest.setName(csbcName + "_" + currency);
          ExternalId underlyingIborIdentifier = null;
          for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
            final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
            underlyingIborIdentifier = csbc.getLiborSecurity(unused, Tenor.THREE_MONTHS);
          }
          if (underlyingIborIdentifier == null) {
            s_logger.error("Could not get external id for " + strip);
            break;
          }
          curveNodeIdMapperName += " 6m " + currency;
          nodes.add(new FRANode(fixingStart, fixingEnd, underlyingIborIdentifier, curveNodeIdMapperName));
          break;
        }
        case FUTURE: {
          final int futureNumber = strip.getNumberOfFuturesAfterTenor();
          final Tenor startTenor = strip.getCurveNodePointTime();
          final Tenor futureTenor = Tenor.THREE_MONTHS;
          final Tenor underlyingTenor = Tenor.THREE_MONTHS;
          final String csbcName = strip.getConventionName();
          final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
          csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
          csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
          csbcSearchRequest.setName(csbcName + "_" + currency);
          ExternalId underlyingIborIdentifier = null;
          for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
            final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
            if (currency.equals("EUR")) {
              underlyingIborIdentifier = csbc.getEuriborSecurity(unused, Tenor.THREE_MONTHS);
            } else {
              underlyingIborIdentifier = csbc.getLiborSecurity(unused, Tenor.THREE_MONTHS);
            }
          }
          if (underlyingIborIdentifier == null) {
            s_logger.error("Could not get external id for " + strip);
            break;
          }
          nodes.add(new RateFutureNode(futureNumber, startTenor, futureTenor, underlyingTenor, underlyingIborIdentifier, curveNodeIdMapperName));
          break;
        }
        case LIBOR: {
          final Tenor startTenor = Tenor.of(Period.ZERO);
          final Tenor maturityTenor = strip.getCurveNodePointTime();
          final String csbcName = strip.getConventionName();
          final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
          csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
          csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
          csbcSearchRequest.setName(csbcName + "_" + currency);
          ExternalId liborIdentifier = null;
          for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
            final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
            liborIdentifier = csbc.getLiborSecurity(unused, maturityTenor);
          }
          if (liborIdentifier == null) {
            s_logger.error("Could not get external id for " + strip);
            break;
          }
          if (currency.equals("USD") || currency.equals("CAD")) {
            curveNodeIdMapperName += " 3m " + currency;
          } else {
            curveNodeIdMapperName += " 6m " + currency;
          }
          nodes.add(new CashNode(startTenor, maturityTenor, liborIdentifier, curveNodeIdMapperName));
          break;
        }
        case OIS_SWAP: {
          final Tenor startTenor = Tenor.of(Period.ZERO);
          final Tenor maturityTenor = strip.getCurveNodePointTime();
          final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " OIS Fixed Leg");
          final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " OIS Overnight Leg");
          curveNodeIdMapperName += " " + currency;
          nodes.add(new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName));
          break;
        }
        case STIBOR:
          if (currency.equals("SEK")) {
            final Tenor startTenor = Tenor.of(Period.ZERO);
            final Tenor maturityTenor = strip.getCurveNodePointTime();
            final String csbcName = strip.getConventionName();
            final ConfigSearchRequest<CurveSpecificationBuilderConfiguration> csbcSearchRequest = new ConfigSearchRequest<>();
            csbcSearchRequest.setType(CurveSpecificationBuilderConfiguration.class);
            csbcSearchRequest.setVersionCorrection(VersionCorrection.LATEST);
            csbcSearchRequest.setName(csbcName + "_" + currency);
            ExternalId stiborIdentifier = null;
            for (final ConfigDocument csbcDocument : ConfigSearchIterator.iterable(configMaster, csbcSearchRequest)) {
              final CurveSpecificationBuilderConfiguration csbc = ((ConfigItem<CurveSpecificationBuilderConfiguration>) csbcDocument.getConfig()).getValue();
              stiborIdentifier = csbc.getEuriborSecurity(unused, maturityTenor);
            }
            if (stiborIdentifier == null) {
              s_logger.error("Could not get external id for " + strip);
              break;
            }
            curveNodeIdMapperName += " 6m " + currency;
            nodes.add(new CashNode(startTenor, maturityTenor, stiborIdentifier, curveNodeIdMapperName));
            break;
          }
          s_logger.error("{} config contains Stibor: ignoring");
          break;
        case SWAP_3M: {
          final Tenor startTenor = Tenor.of(Period.ZERO);
          final Tenor maturityTenor = strip.getCurveNodePointTime();
          final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " IRS Fixed Leg");
          final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " 3M IRS Ibor Leg");
          curveNodeIdMapperName += " 3m " + currency;
          nodes.add(new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName));
          break;
        }
        case SWAP_6M: {
          final Tenor startTenor = Tenor.of(Period.ZERO);
          final Tenor maturityTenor = strip.getCurveNodePointTime();
          final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " IRS Fixed Leg");
          final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " 6M IRS Ibor Leg");
          curveNodeIdMapperName += " 6m " + currency;
          nodes.add(new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName));
          break;
        }
        case SWAP_12M: {
          final Tenor startTenor = Tenor.of(Period.ZERO);
          final Tenor maturityTenor = strip.getCurveNodePointTime();
          final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " IRS Fixed Leg");
          final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " 12M IRS Ibor Leg");
          curveNodeIdMapperName += " 12m " + currency;
          nodes.add(new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName));
          break;
        }
        case SWAP_28D: {
          final Tenor startTenor = Tenor.of(Period.ZERO);
          final Tenor maturityTenor = strip.getCurveNodePointTime();
          final ExternalId payLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " IRS Fixed Leg");
          final ExternalId receiveLegConvention = ExternalId.of(PerCurrencyConventionHelper.SCHEME_NAME, currency + " 28D IRS Ibor Leg");
          curveNodeIdMapperName += " 28d " + currency;
          nodes.add(new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName));
          break;
        }
        case BASIS_SWAP:
        case CONTINUOUS_ZERO_DEPOSIT:
        case FRA:
        case PERIODIC_ZERO_DEPOSIT:
        case SIMPLE_ZERO_DEPOSIT:
        case SPREAD:
        case SWAP:
        case TENOR_SWAP:
        default:
          s_logger.error("Cannot handle strip instrument type {}", strip.getInstrumentType());
          break;
      }
    }
    return new InterpolatedCurveDefinition(name, nodes, oldConfig.getInterpolatorName(), oldConfig.getRightExtrapolatorName(), oldConfig.getLeftExtrapolatorName());
  }

  private static Map<String, CurveNodeIdMapper.Builder> convertCurveSpecificationBuilderConfigurations(final Map<Pair<String, String>, CurveSpecificationBuilderConfiguration> oldConfigs) {
    final Map<String, CurveNodeIdMapper.Builder> result = new HashMap<>();
    for (final Map.Entry<Pair<String, String>, CurveSpecificationBuilderConfiguration> entry : oldConfigs.entrySet()) {
      final String currency = entry.getKey().getFirst();
      String name = entry.getKey().getSecond().split("_")[0];
      if (CSBC_NAME_REMAPPER.containsKey(name)) {
        name = CSBC_NAME_REMAPPER.get(name);
      }
      final CurveSpecificationBuilderConfiguration oldConfig = entry.getValue();
      if (oldConfig.getBasisSwapInstrumentProviders() != null) {
        s_logger.error("Cannot handle basis swaps");
      }
      if (oldConfig.getCashInstrumentProviders() != null) {
        final String newName = name + " " + currency;
        if (result.containsKey(newName)) {
          result.get(newName).cashNodeIds(convertInstrumentProviders(oldConfig.getCashInstrumentProviders()));
        } else {
          final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
          builder.cashNodeIds(convertInstrumentProviders(oldConfig.getCashInstrumentProviders()));
          result.put(newName, builder);
        }
      }
      if (oldConfig.getCDORInstrumentProviders() != null) {
        if (currency.equals("CAD")) {
          final String newName = name + " 3m " + currency;
          if (result.containsKey(newName)) {
            result.get(newName).cashNodeIds(convertInstrumentProviders(oldConfig.getCDORInstrumentProviders()));
          } else {
            final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
            builder.cashNodeIds(convertInstrumentProviders(oldConfig.getCDORInstrumentProviders()));
            result.put(newName, builder);
          }
        } else {
          s_logger.error("{} config contains CDOR: ignoring", currency);
        }
      }
      if (oldConfig.getCiborInstrumentProviders() != null) {
        if (currency.equals("DKK")) {
          final String newName = name + " 6m " + currency;
          if (result.containsKey(newName)) {
            result.get(newName).cashNodeIds(convertInstrumentProviders(oldConfig.getCiborInstrumentProviders()));
          } else {
            final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
            builder.cashNodeIds(convertInstrumentProviders(oldConfig.getCiborInstrumentProviders()));
            result.put(newName, builder);
          }
        } else {
          s_logger.error("{} config contains Cibor: ignoring", currency);
        }
      }
      if (oldConfig.getContinuousZeroDepositInstrumentProviders() != null) {
        s_logger.error("Cannot handle continuous zero deposits");
      }
      if (oldConfig.getEuriborInstrumentProviders() != null) {
        if (currency.equals("EUR")) {
          final String newName = name + " 6m " + currency;
          if (result.containsKey(newName)) {
            result.get(newName).cashNodeIds(convertInstrumentProviders(oldConfig.getEuriborInstrumentProviders()));
          } else {
            final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
            builder.cashNodeIds(convertInstrumentProviders(oldConfig.getEuriborInstrumentProviders()));
            result.put(newName, builder);
          }
        } else {
          s_logger.error("{} config contains Euribor: ignoring", currency);
        }
      }
      if (oldConfig.getFra3MInstrumentProviders() != null) {
        final String newName = name + " 3m " + currency;
        if (result.containsKey(newName)) {
          result.get(newName).fraNodeIds(convertInstrumentProviders(oldConfig.getFra3MInstrumentProviders()));
        } else {
          final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
          builder.fraNodeIds(convertInstrumentProviders(oldConfig.getFra3MInstrumentProviders()));
          result.put(newName, builder);
        }
      }
      if (oldConfig.getFra6MInstrumentProviders() != null) {
        final String newName = name + " 6m " + currency;
        if (result.containsKey(newName)) {
          result.get(newName).fraNodeIds(convertInstrumentProviders(oldConfig.getFra6MInstrumentProviders()));
        } else {
          final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
          builder.fraNodeIds(convertInstrumentProviders(oldConfig.getFra6MInstrumentProviders()));
          result.put(newName, builder);
        }
      }
      if (oldConfig.getFutureInstrumentProviders() != null) {
        final String newName = name + " " + currency;
        if (result.containsKey(newName)) {
          result.get(newName).rateFutureNodeIds(convertInstrumentProviders(oldConfig.getFutureInstrumentProviders()));
        } else {
          final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
          builder.rateFutureNodeIds(convertInstrumentProviders(oldConfig.getFutureInstrumentProviders()));
          result.put(newName, builder);
        }
      }
      if (oldConfig.getLiborInstrumentProviders() != null) {
        if (currency.equals("USD") || currency.equals("CAD")) {
          final String newName = name + " 3m " + currency;
          if (result.containsKey(newName)) {
            result.get(newName).cashNodeIds(convertInstrumentProviders(oldConfig.getLiborInstrumentProviders()));
          } else {
            final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
            builder.cashNodeIds(convertInstrumentProviders(oldConfig.getLiborInstrumentProviders()));
            result.put(newName, builder);
          }
        } else {
          final String newName = name + " 6m " + currency;
          if (result.containsKey(newName)) {
            result.get(newName).cashNodeIds(convertInstrumentProviders(oldConfig.getLiborInstrumentProviders()));
          } else {
            final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
            builder.cashNodeIds(convertInstrumentProviders(oldConfig.getLiborInstrumentProviders()));
            result.put(newName, builder);
          }
        }
      }
      if (oldConfig.getOISSwapInstrumentProviders() != null) {
        final String newName = name + " Overnight " + currency;
        if (result.containsKey(newName)) {
          result.get(newName).swapNodeIds(convertInstrumentProviders(oldConfig.getOISSwapInstrumentProviders()));
        } else {
          final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
          builder.swapNodeIds(convertInstrumentProviders(oldConfig.getOISSwapInstrumentProviders()));
          result.put(newName, builder);
        }
      }
      if (oldConfig.getPeriodicZeroDepositInstrumentProviders() != null) {
        s_logger.error("Cannot handle periodic zero deposits");
      }
      if (oldConfig.getSimpleZeroDepositInstrumentProviders() != null) {
        s_logger.error("Cannot handle simple zero deposits");
      }
      if (oldConfig.getStiborInstrumentProviders() != null) {
        if (currency.equals("SEK")) {
          final String newName = name + " 3m " + currency;
          if (result.containsKey(newName)) {
            if (result.get(newName).build().getCashNodeIds() != null) {
              final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
              builder.cashNodeIds(convertInstrumentProviders(oldConfig.getStiborInstrumentProviders()));
              result.put(newName + " (Stibor)", builder);
            } else {
              result.get(newName).cashNodeIds(convertInstrumentProviders(oldConfig.getStiborInstrumentProviders()));
            }
          } else {
            final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
            builder.cashNodeIds(convertInstrumentProviders(oldConfig.getStiborInstrumentProviders()));
            result.put(newName, builder);
          }
        } else {
          s_logger.error("{} config contains Stibor: ignoring", currency);
        }
      }
      if (oldConfig.getSwap12MInstrumentProviders() != null) {
        final String newName = name + " 12m " + currency;
        if (result.containsKey(newName)) {
          result.get(newName).swapNodeIds(convertInstrumentProviders(oldConfig.getSwap12MInstrumentProviders()));
        } else {
          final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
          builder.swapNodeIds(convertInstrumentProviders(oldConfig.getSwap12MInstrumentProviders()));
          result.put(newName, builder);
        }
      }
      if (oldConfig.getSwap28DInstrumentProviders() != null) {
        final String newName = name + " 28d " + currency;
        if (result.containsKey(newName)) {
          result.get(newName).swapNodeIds(convertInstrumentProviders(oldConfig.getSwap28DInstrumentProviders()));
        } else {
          final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
          builder.swapNodeIds(convertInstrumentProviders(oldConfig.getSwap28DInstrumentProviders()));
          result.put(newName, builder);
        }
      }
      if (oldConfig.getSwap3MInstrumentProviders() != null) {
        final String newName = name + " 3m " + currency;
        if (result.containsKey(newName)) {
          result.get(newName).swapNodeIds(convertInstrumentProviders(oldConfig.getSwap3MInstrumentProviders()));
        } else {
          final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
          builder.swapNodeIds(convertInstrumentProviders(oldConfig.getSwap3MInstrumentProviders()));
          result.put(newName, builder);
        }
      }
      if (oldConfig.getSwap6MInstrumentProviders() != null) {
        final String newName = name + " 6m " + currency;
        if (result.containsKey(newName)) {
          result.get(newName).swapNodeIds(convertInstrumentProviders(oldConfig.getSwap6MInstrumentProviders()));
        } else {
          final CurveNodeIdMapper.Builder builder = CurveNodeIdMapper.builder().name(newName);
          builder.swapNodeIds(convertInstrumentProviders(oldConfig.getSwap6MInstrumentProviders()));
          result.put(newName, builder);
        }
      }
      if (oldConfig.getTenorSwapInstrumentProviders() != null) {
        s_logger.error("Cannot handle tenor swaps");
      }
    }
    return result;
  }

  private static Map<Tenor, CurveInstrumentProvider> convertInstrumentProviders(final Map<Tenor, CurveInstrumentProvider> providers) {
    final LocalDate unused = LocalDate.now();
    final Map<Tenor, CurveInstrumentProvider> result = new HashMap<>();
    for (final Map.Entry<Tenor, CurveInstrumentProvider> entry : providers.entrySet()) {
      final CurveInstrumentProvider value = entry.getValue();
      if (value instanceof SyntheticIdentifierCurveInstrumentProvider) {
        final Tenor tenor = entry.getKey();
        final SyntheticIdentifierCurveInstrumentProvider provider = (SyntheticIdentifierCurveInstrumentProvider) value;
        if (provider.getType() == StripInstrumentType.BASIS_SWAP) {
          //          final ExternalId identifier = provider.getInstrument(unused, tenor, payTenor, receiveTenor, payIndexType, receiveIndexType);
          //          final String dataField = provider.getMarketDataField();
          //          final DataFieldType dataType = provider.getDataFieldType();
          //          result.put(tenor, new StaticCurveInstrumentProvider(identifier, dataField, dataType));
          s_logger.error("Cannot convert instrument provider for basis swaps");
          //      @Override
          //      public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final Tenor payTenor, final Tenor receiveTenor, final IndexType payIndexType,
          //          final IndexType receiveIndexType) {
          //        return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + "_" + payIndexType.name() + payTenor.getPeriod().toString() + receiveIndexType.name()
          //            + receiveTenor.getPeriod().toString() + "_" + tenor.getPeriod().toString());
          //      }
        } else if (provider.getType() == StripInstrumentType.PERIODIC_ZERO_DEPOSIT) {
          s_logger.error("Cannot convert instrument provider for periodic zero deposits");
          //      @Override
          //      public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int periodsPerYear, final boolean isPeriodicZeroDeposit) {
          //        return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + Integer.toString(periodsPerYear) + tenor.getPeriod().toString());
          //      }
        } else if (provider.getType() == StripInstrumentType.FUTURE) {
          s_logger.error("Cannot convert instrument provider for futures");
          //      @Override
          //      public ExternalId getInstrument(final LocalDate curveDate, final Tenor startTenor, final Tenor futureTenor, final int numQuarterlyFuturesFromTenor) {
          //        return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + startTenor.getPeriod().toString() + futureTenor.getPeriod().toString());
          //      }
          //      @Override
          //      public ExternalId getInstrument(final LocalDate curveDate, final Tenor tenor, final int numQuarterlyFuturesFromTenor) {
          //        return ExternalId.of(_scheme, _ccy.getCode() + _idType.name() + tenor.getPeriod().toString());
          //      }
        }
        final ExternalId identifier = provider.getInstrument(unused, tenor);
        final String dataField = provider.getMarketDataField();
        final DataFieldType dataType = provider.getDataFieldType();
        result.put(tenor, new StaticCurveInstrumentProvider(identifier, dataField, dataType));
      } else if (value instanceof SyntheticFutureCurveInstrumentProvider) {
        result.put(entry.getKey(), value);
      } else {
        s_logger.error("Cannot handle curve instrument providers of type {}", value.getClass());
      }
    }
    return result;
  }
}
