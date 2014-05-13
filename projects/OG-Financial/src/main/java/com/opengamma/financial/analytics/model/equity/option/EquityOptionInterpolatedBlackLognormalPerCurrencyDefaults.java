/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import com.opengamma.core.security.Security;
import com.opengamma.financial.property.DefaultPropertyFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * Populates {@link EquityOptionFunction}, including {@link EquityVanillaBarrierOptionBlackFunction}, with defaults appropriate
 * for pricing using an interpolated Black lognormal volatility surface.
 */
public class EquityOptionInterpolatedBlackLognormalPerCurrencyDefaults extends EquityOptionInterpolatedBlackLognormalDefaults {

  /**
   * @param priority The priority class of {@link DefaultPropertyFunction} instances, allowing them to be ordered relative to each other, not null
   * @param perCurrencyConfig Default values of discounting curve, discounting curve configuration, surface name and interpolation method,
   * forward curve name and forward curve calculation method per currency, not null
   */
  public EquityOptionInterpolatedBlackLognormalPerCurrencyDefaults(final String priority, final String... perCurrencyConfig) {
    super(priority, perCurrencyConfig);
  }

  @Override
  protected String getId(final Security security) {
    return FinancialSecurityUtils.getCurrency(security).getCode().toUpperCase();
  }

}
