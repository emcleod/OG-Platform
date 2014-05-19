/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.loader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityRegion;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Creates a curve construction configuration, interpolated curve definition and curve node id mapper
 * for US bonds. The ISINs used for the bill nodes follow the form "USB000000XXX" where "XXX" is equal
 * to the number of months until bill maturity. Bond nodes follow the form "UST000000XXX" where "XXX" 
 * is equal to the number of months until bond maturity. 
 * <p>
 * The bond curve contains bill nodes from 6 months to 18 months in six month increments and bond nodes 
 * from 2 years to 30 years in one year increments, and uses the yield quote to construct the curve.
 */
public class ExampleUSBondCurveConfigurationsPopulator {
  /** The curve construction configuration name */
  private static final String CURVE_CONSTRUCTION_CONFIG_NAME = "US Government Bond Configuration";
  /** The curve name */
  private static final String CURVE_NAME = "US Government Bond";
  /** The curve node id mapper name */
  private static final String CURVE_NODE_ID_MAPPER_NAME = "US Government Bond ISIN";

  /**
   * Populates a config master with curve configurations, curve definitions
   * and curve node id mappers.
   * @param configMaster The config master, not null
   */
  public static void populateConfigAndConventionMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveConstructionConfiguration()));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveNodeIdMapper()));
    ConfigMasterUtils.storeByName(configMaster, makeConfig(makeCurveDefinition()));
  }

  /**
   * Creates a curve construction configuration consisting of a single government bond curve
   * which matches against USD.
   * @return The configuration
   */
  private static CurveConstructionConfiguration makeCurveConstructionConfiguration() {
    final Set<Object> keys = Sets.<Object>newHashSet(Currency.USD);
    final LegalEntityRegion regionFilter = new LegalEntityRegion(false, false, Collections.<Country>emptySet(), true, Collections.singleton(Currency.USD));
    final Set<LegalEntityFilter<LegalEntity>> filters = new HashSet<>();
    filters.add(regionFilter);
    final IssuerCurveTypeConfiguration issuerCurveType = new IssuerCurveTypeConfiguration(keys, filters);
    final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
    curveTypes.put(CURVE_NAME, Arrays.asList(issuerCurveType));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypes);
    final List<CurveGroupConfiguration> groups = Arrays.asList(group);
    final List<String> exogenousConfigs = Collections.singletonList("Default USD Curves");
    return new CurveConstructionConfiguration(CURVE_CONSTRUCTION_CONFIG_NAME, groups, exogenousConfigs);
  }

  /**
   * Creates an interpolated curve definition containing 3 bills with tenors from 6 months to 18 months
   * in six month intervals and 29 bonds with tenors from 2 years to 30 years in one year intervals. 
   * The interpolator is double quadratic with linear extrapolation on both sides.
   * @return The curve definition
   */
  private static CurveDefinition makeCurveDefinition() {
    final Set<CurveNode> curveNodes = new LinkedHashSet<>();
    for (int i = 6; i <= 18; i += 6) {
      curveNodes.add(new BillNode(Tenor.ofMonths(i), CURVE_NODE_ID_MAPPER_NAME));
    }
    for (int i = 2; i <= 30; i++) {
      curveNodes.add(new BondNode(Tenor.ofYears(i), CURVE_NODE_ID_MAPPER_NAME));
    }
    final CurveDefinition curveDefinition = new InterpolatedCurveDefinition(CURVE_NAME, curveNodes,
        Interpolator1DFactory.DOUBLE_QUADRATIC, Interpolator1DFactory.LINEAR_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    return curveDefinition;
  }

  /**
   * Creates a curve node id mapper containing ISINs for 3 bills with tenors from 6 months to 18 months
   * in six month intervals and 29 bonds with tenors from 2 years to 30 years in one year intervals.
   * @return The curve node id mapper
   */
  private static CurveNodeIdMapper makeCurveNodeIdMapper() {
    final Map<Tenor, CurveInstrumentProvider> billNodes = new HashMap<>();
    final Map<Tenor, CurveInstrumentProvider> bondNodes = new HashMap<>();
    for (int i = 6; i < 24; i += 6) {
      final Tenor tenor = Tenor.ofMonths(i);
      String suffix;
      if (i < 10) {
        suffix = "00" + Integer.toString(i);
      } else {
        suffix = "0" + Integer.toString(i);
      }
      final ExternalId isin = ExternalSchemes.syntheticSecurityId("USB000000" + suffix);
      final CurveInstrumentProvider instrumentProvider = new StaticCurveInstrumentProvider(isin, MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT);
      billNodes.put(tenor, instrumentProvider);
    }
    for (int i = 0; i < 29; i++) {
      final int years = i + 2;
      final int months = years * 12;
      final Tenor tenor = Tenor.ofYears(years);
      String suffix;
      if (months < 10) {
        suffix = "00" + Integer.toString(months);
      } else if (years < 100) {
        suffix = "0" + Integer.toString(months);
      } else {
        suffix = Integer.toString(months);
      }
      final ExternalId isin = ExternalSchemes.syntheticSecurityId("UST000000" + suffix);
      final CurveInstrumentProvider instrumentProvider = new StaticCurveInstrumentProvider(isin, MarketDataRequirementNames.MARKET_VALUE, DataFieldType.OUTRIGHT);
      bondNodes.put(tenor, instrumentProvider);
    }
    final CurveNodeIdMapper curveNodeIdMapper = CurveNodeIdMapper.builder()
        .name(CURVE_NODE_ID_MAPPER_NAME)
        .billNodeIds(billNodes)
        .bondNodeIds(bondNodes)
        .build();
    return curveNodeIdMapper;
  }

  /**
   * Creates a config item from a curve construction configuration object.
   * @param curveConfig The curve construction configuration
   * @return The config item
   */
  private static ConfigItem<CurveConstructionConfiguration> makeConfig(final CurveConstructionConfiguration curveConfig) {
    final ConfigItem<CurveConstructionConfiguration> config = ConfigItem.of(curveConfig);
    config.setName(curveConfig.getName());
    return config;
  }

  /**
   * Creates a config item from a curve node id mapper object.
   * @param curveNodeIdMapper The curve node id mapper
   * @return The config item
   */
  private static ConfigItem<CurveNodeIdMapper> makeConfig(final CurveNodeIdMapper curveNodeIdMapper) {
    final ConfigItem<CurveNodeIdMapper> config = ConfigItem.of(curveNodeIdMapper);
    config.setName(curveNodeIdMapper.getName());
    return config;
  }

  /**
   * Creates a config item from a curve definition object.
   * @param curveDefinition The curve definition
   * @return The config item
   */
  private static ConfigItem<CurveDefinition> makeConfig(final CurveDefinition curveDefinition) {
    final ConfigItem<CurveDefinition> config = ConfigItem.of(curveDefinition);
    config.setName(curveDefinition.getName());
    return config;
  }
}
