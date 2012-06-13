/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility;

/**
 * 
 */
public class VolatilityDataFittingDefaults {
  /** Property representing the volatility model used to fit a cube */
  public static final String PROPERTY_VOLATILITY_MODEL = "VolatilityModel";
  /** SABR */
  public static final String SABR_FITTING = "SABR";
  /** Property representing the method used to fit a cube or surface */
  public static final String PROPERTY_FITTING_METHOD = "FittingMethod";
  /** Non-linear least squares */
  public static final String NON_LINEAR_LEAST_SQUARES = "NonLinearLeastSquares";
}
