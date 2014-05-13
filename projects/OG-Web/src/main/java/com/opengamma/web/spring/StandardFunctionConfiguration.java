/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.opengamma.engine.function.config.AbstractFunctionConfigurationBean;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.CurrencyPairsDefaults;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.analytics.model.bond.BondFunction;
import com.opengamma.financial.analytics.model.bond.BondFunctions;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionBlackFunction;
import com.opengamma.financial.analytics.model.bondfutureoption.BondFutureOptionFunctions;
import com.opengamma.financial.analytics.model.credit.CreditFunctions;
import com.opengamma.financial.analytics.model.curve.CurveFunctions;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.curve.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.curve.interestrate.InterestRateFunctions;
import com.opengamma.financial.analytics.model.equity.option.EquityOptionFunction;
import com.opengamma.financial.analytics.model.equity.option.OptionFunctions;
import com.opengamma.financial.analytics.model.equity.portfoliotheory.PortfolioTheoryFunctions;
import com.opengamma.financial.analytics.model.fixedincome.InterestRateInstrumentFunction;
import com.opengamma.financial.analytics.model.future.FutureFunctions;
import com.opengamma.financial.analytics.model.future.FuturesFunction;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunction;
import com.opengamma.financial.analytics.model.futureoption.FutureOptionFunctions;
import com.opengamma.financial.analytics.model.horizon.HorizonFunctions;
import com.opengamma.financial.analytics.model.irfutureoption.IRFutureOptionFunctions;
import com.opengamma.financial.analytics.model.irfutureoption.InterestRateFutureOptionBlackFunction;
import com.opengamma.financial.analytics.model.multicurve.MultiCurvePricingFunctions;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.sensitivities.SensitivitiesFunctions;
import com.opengamma.financial.analytics.model.swaption.black.SwaptionBlackFunction;
import com.opengamma.financial.analytics.model.var.VaRFunctions;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.LocalVolatilitySurfaceDefaults;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;
import com.opengamma.web.spring.defaults.GeneralLocalVolatilitySurfaceDefaults;

/**
 * Constructs a standard function repository.
 * <p>
 * A sub-class should provide installation specific details relating to the data providers used.
 */
@SuppressWarnings("deprecation")
public abstract class StandardFunctionConfiguration extends AbstractFunctionConfigurationBean {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(StandardFunctionConfiguration.class);

  /**
   * Holds one or more values referenced by a hierarchical key.
   */
  public static class Value {
    /** A map from key to value */
    private final Map<String, String> _values = new HashMap<>();

    /**
     * Sets the value for a key.
     * @param key The key
     * @param value The value
     */
    public void set(final String key, final String value) {
      _values.put(key, value);
    }

    // TODO: allow wildcard matches, e.g. */discounting

    /**
     * Gets the value for a key. If the key is not matched, then repeatedly tries to match
     * a value at the next level of the hierarchy, until the null value is reached.
     * @param key The key
     * @return The matched value
     */
    public String get(final String key) {
      final String value = _values.get(key);
      if (value != null) {
        return value;
      }
      final int separator = key.lastIndexOf('/');
      if (separator == -1) {
        return _values.get(null);
      }
      return get(key.substring(0, separator));
    }

  }

  /**
   * Constants for a particular currency.
   */
  public static class CurrencyInfo {
    /** The currency string */
    private final String _currency;
    /** Usually the default value of the {@link ValuePropertyNames#CURVE_CONSTRUCTION_CONFIG} property */
    private final Value _curveConfiguration = new Value();
    /** Usually the default value of the {@link ValuePropertyNames#CURVE} property */
    private final Value _curveName = new Value();
    /** Usually the default value of the {@link ValuePropertyNames#CURVE_CALCULATION_METHOD} property */
    private final Value _curveCalculationMethodName = new Value();
    /** Usually the default value of the {@link ValuePropertyNames#SURFACE} property */
    private final Value _surfaceName = new Value();
    /** Usually the default value of the {@link ValuePropertyNames#CUBE} property */
    private final Value _cubeName = new Value();
    /** The forward curve name */
    private final Value _forwardCurveName = new Value();
    /** The forward curve calculation method */
    private final Value _forwardCurveCalculationMethod = new Value();
    /** The surface calculation method */
    private final Value _surfaceCalculationMethod = new Value();
    /** Usually the default value of the {@link SurfaceAndCubePropertyNames#PROPERTY_CUBE_DEFINITION} property */
    private final Value _cubeDefinitionName = new Value();
    /** Usually the default value of the {@link SurfaceAndCubePropertyNames#PROPERTY_CUBE_SPECIFICATION} property */
    private final Value _cubeSpecificationName = new Value();
    /** Usually the default value of the {@link SurfaceAndCubePropertyNames#PROPERTY_SURFACE_DEFINITION} property */
    private final Value _surfaceDefinitionName = new Value();
    /** Usually the default value of the {@link SurfaceAndCubePropertyNames#PROPERTY_SURFACE_SPECIFICATION} property */
    private final Value _surfaceSpecificationName = new Value();

    /**
     * @param currency The currency string.
     */
    public CurrencyInfo(final String currency) {
      _currency = currency;
    }

    /**
     * Gets the currency string.
     * @return The currency string
     */
    public String getCurrency() {
      return _currency;
    }

    /**
     * Sets the curve configuration name for a key, e.g. a default value for the {@link ValuePropertyNames#CURVE_CONSTRUCTION_CONFIG} property.
     * @param key The key
     * @param curveConfiguration The curve configuration name
     */
    public void setCurveConfiguration(final String key, final String curveConfiguration) {
      _curveConfiguration.set(key, curveConfiguration);
    }

    /**
     * Gets the curve configuration name for a key, e.g. a default value for the {@link ValuePropertyNames#CURVE_CONSTRUCTION_CONFIG} property.
     * @param key The key
     * @return The curve configuration name
     */
    public String getCurveConfiguration(final String key) {
      return _curveConfiguration.get(key);
    }

    /**
     * Sets the curve name for a key, e.g. a default value for the {@link ValuePropertyNames#CURVE} property.
     * @param key The key
     * @param curveName The curve name
     */
    public void setCurveName(final String key, final String curveName) {
      _curveName.set(key, curveName);
    }

    /**
     * Gets the curve name for a key, e.g. a default value for the {@link ValuePropertyNames#CURVE} property.
     * @param key The key
     * @return The curve name
     */
    public String getCurveName(final String key) {
      return _curveName.get(key);
    }

    /**
     * Sets the curve calculation method name for a key, e.g. a default value for the {@link ValuePropertyNames#CURVE_CALCULATION_METHOD} property.
     * @param key The key
     * @param curveCalculationMethodName The curve calculation method name
     */
    public void setCurveCalculationMethodName(final String key, final String curveCalculationMethodName) {
      _curveCalculationMethodName.set(key, curveCalculationMethodName);
    }

    /**
     * Gets the curve calculation method name for a key, e.g. a default value for the {@link ValuePropertyNames#CURVE_CALCULATION_METHOD} property.
     * @param key The key
     * @return The curve calculation method name
     */
    public String getCurveCalculationMethodName(final String key) {
      return _curveCalculationMethodName.get(key);
    }

    /** 
     * Sets the surface name for a key, e.g. a default value for the {@link ValuePropertyNames#SURFACE} property.
     * @param key The key
     * @param surfaceName The surface name
     */
    public void setSurfaceName(final String key, final String surfaceName) {
      _surfaceName.set(key, surfaceName);
    }

    /** 
     * Gets the surface name for a key, e.g. a default value for the {@link ValuePropertyNames#SURFACE} property.
     * @param key The key
     * @return The surface name
     */
    public String getSurfaceName(final String key) {
      return _surfaceName.get(key);
    }

    /**
     * Sets the cube name for a key, e.g. a default value for the {@link ValuePropertyNames#CUBE} property.
     * @param key The key
     * @param cubeName The cube name
     */
    public void setCubeName(final String key, final String cubeName) {
      _cubeName.set(key, cubeName);
    }

    /**
     * Gets the cube name for a key, e.g. a default value for the {@link ValuePropertyNames#CUBE} property.
     * @param key The key
     * @return The cube name
     */
    public String getCubeName(final String key) {
      return _cubeName.get(key);
    }

    /**
     * Sets the forward curve name for a key.
     * @param key The key
     * @param forwardCurveName The forward curve name
     */
    public void setForwardCurveName(final String key, final String forwardCurveName) {
      _forwardCurveName.set(key, forwardCurveName);
    }

    /**
     * Gets the forward curve name for a key.
     * @param key The key
     * @return The forward curve name
     */
    public String getForwardCurveName(final String key) {
      return _forwardCurveName.get(key);
    }

    /**
     * Sets the forward curve calculation method name for a key.
     * @param key The key
     * @param forwardCurveCalculationMethod The forward curve calculation name
     */
    public void setForwardCurveCalculationMethod(final String key, final String forwardCurveCalculationMethod) {
      _forwardCurveCalculationMethod.set(key, forwardCurveCalculationMethod);
    }

    /**
     * Gets the forward curve calculation method name for a key.
     * @param key The key
     * @return The forward curve calculation method name
     */
    public String getForwardCurveCalculationMethod(final String key) {
      return _forwardCurveCalculationMethod.get(key);
    }

    /**
     * Sets the surface calculation method name for a key, e.g. a default value for the {@link ValuePropertyNames#SURFACE_CALCULATION_METHOD}
     * @param key The key
     * @param surfaceCalculationMethod The surface calculation method name
     */
    public void setSurfaceCalculationMethod(final String key, final String surfaceCalculationMethod) {
      _surfaceCalculationMethod.set(key, surfaceCalculationMethod);
    }

    /**
     * Gets the surface calculation method name for a key, e.g. a default value for the {@link ValuePropertyNames#SURFACE_CALCULATION_METHOD}
     * @param key The key
     * @return The surface calculation method name
     */
    public String getSurfaceCalculationMethod(final String key) {
      return _surfaceCalculationMethod.get(key);
    }

    /**
     * Gets the cube definition name for a key, e.g. a default value for the {@link SurfaceAndCubePropertyNames#PROPERTY_CUBE_DEFINITION} property.
     * @param key The key
     * @return The cube definition name
     */
    public String getCubeDefinitionName(final String key) {
      return _cubeDefinitionName.get(key);
    }

    /**
     * Sets a cube definition name for a key, e.g. a default value for the {@link SurfaceAndCubePropertyNames#PROPERTY_CUBE_DEFINITION} property.
     * @param key The key
     * @param cubeDefinitionName The cube definition name
     */
    public void setCubeDefinitionName(final String key, final String cubeDefinitionName) {
      _cubeDefinitionName.set(key, cubeDefinitionName);
    }

    /**
     * Gets the cube specification name for a key, e.g. a default value for the {@link SurfaceAndCubePropertyNames#PROPERTY_CUBE_SPECIFICATION} property.
     * @param key The key
     * @return The cube specification name
     */
    public String getCubeSpecificationName(final String key) {
      return _cubeSpecificationName.get(key);
    }

    /**
     * Sets a cube specification name for a key, e.g. a default value for the {@link SurfaceAndCubePropertyNames#PROPERTY_CUBE_SPECIFICATION} property.
     * @param key The key
     * @param cubeSpecificationName The cube specification name
     */
    public void setCubeSpecificationName(final String key, final String cubeSpecificationName) {
      _cubeSpecificationName.set(key, cubeSpecificationName);
    }

    /**
     * Gets the surface definition name for a key, e.g. a default value for the {@link SurfaceAndCubePropertyNames#PROPERTY_SURFACE_DEFINITION} property.
     * @param key The key
     * @return The surface definition name
     */
    public String getSurfaceDefinitionName(final String key) {
      return _surfaceDefinitionName.get(key);
    }

    /**
     * Sets a surface definition name for a key, e.g. a default value for the {@link SurfaceAndCubePropertyNames#PROPERTY_SURFACE_DEFINITION} property.
     * @param key The key
     * @param surfaceDefinitionName The surface definition name
     */
    public void setSurfaceDefinitionName(final String key, final String surfaceDefinitionName) {
      _surfaceDefinitionName.set(key, surfaceDefinitionName);
    }

    /**
     * Gets the surface specification name for a key, e.g. a default value for the {@link SurfaceAndCubePropertyNames#PROPERTY_SURFACE_SPECIFICATION} property.
     * @param key The key
     * @return The surface specification name
     */
    public String getSurfaceSpecificationName(final String key) {
      return _surfaceSpecificationName.get(key);
    }

    /**
     * Sets a surface specification name for a key, e.g. a default value for the {@link SurfaceAndCubePropertyNames#PROPERTY_SURFACE_SPECIFICATION} property.
     * @param key The key
     * @param surfaceSpecificationName The surface specification name
     */
    public void setSurfaceSpecificationName(final String key, final String surfaceSpecificationName) {
      _surfaceSpecificationName.set(key, surfaceSpecificationName);
    }
  }

  /**
   * Constants for a particular currency pair
   */
  public static class CurrencyPairInfo {
    /** The pair of currencies */
    private final Pair<String, String> _currencies;
    /** Usually the default value for the {@link ValuePropertyNames#CURVE} property */
    private final Value _curveName = new Value();
    /** Usually the default value for the {@link ValuePropertyNames#CURVE_CALCULATION_METHOD} property */
    private final Value _curveCalculationMethod = new Value();
    /** Usually the default value for the {@link ValuePropertyNames#SURFACE} property */
    private final Value _surfaceName = new Value();
    /** The forward curve name */
    private final Value _forwardCurveName = new Value();

    /**
     * @param currencies The currencies
     */
    public CurrencyPairInfo(final Pair<String, String> currencies) {
      _currencies = currencies;
    }

    /**
     * Gets the currency pair for which these defaults apply.
     * @return The currency pair
     */
    public Pair<String, String> getCurrencies() {
      return _currencies;
    }

    /**
     * Sets the curve name for a key, e.g. a default value for the {@link ValuePropertyNames#CURVE} property.
     * @param key The key
     * @param curveName The curve name
     */
    public void setCurveName(final String key, final String curveName) {
      _curveName.set(key, curveName);
    }

    /**
     * Gets the curve name for a key, e.g. a default value for the {@link ValuePropertyNames#CURVE} property.
     * @param key The key
     * @return The curve name
     */
    public String getCurveName(final String key) {
      return _curveName.get(key);
    }

    /**
     * Sets the curve calculation method for a key, e.g. a default value for the {@link ValuePropertyNames#CURVE_CALCULATION_METHOD} property.
     * @param key The key
     * @param curveCalculationMethod The curve calculation method
     */
    public void setCurveCalculationMethod(final String key, final String curveCalculationMethod) {
      _curveCalculationMethod.set(key, curveCalculationMethod);
    }

    /**
     * Gets the curve calculation method for a key, e.g. a default value for the {@link ValuePropertyNames#CURVE_CALCULATION_METHOD} property.
     * @param key The key
     * @return The curve calculation method
     */
    public String getCurveCalculationMethod(final String key) {
      return _curveCalculationMethod.get(key);
    }

    /**
     * Sets the surface name for a key, e.g. a default value for the {@link ValuePropertyNames#SURFACE} property.
     * @param key The key
     * @param surfaceName The surface name
     */
    public void setSurfaceName(final String key, final String surfaceName) {
      _surfaceName.set(key, surfaceName);
    }

    /**
     * Gets the surface name for a key, e.g. a default value for the {@link ValuePropertyNames#SURFACE} property.
     * @param key The key
     * @return The surface name
     */
    public String getSurfaceName(final String key) {
      return _surfaceName.get(key);
    }

    /**
     * Gets the forward curve name for a key. 
     * @param key The key
     * @return The forward curve name
     */
    public String getForwardCurveName(final String key) {
      return _forwardCurveName.get(key);
    }

    /**
     * Sets the forward curve name for a key.
     * @param key The key
     * @param forwardCurveName The forward curve name
     */
    public void setForwardCurveName(final String key, final String forwardCurveName) {
      _forwardCurveName.set(key, forwardCurveName);
    }
  }

  /**
   * Constants for a particular equity ticker.
   */
  public static class EquityInfo {
    /** The equity ticker */
    private final String _equity;
    /** The discounting curve name. Usually the default value of the {@link EquityOptionFunction#PROPERTY_DISCOUNTING_CURVE_NAME} property */
    private final Value _discountingCurve = new Value();
    /** The discounting curve calculation configuration name. Usually the default value of the {@link EquityOptionFunction#PROPERTY_DISCOUNTING_CURVE_CONFIG} property */
    private final Value _discountingCurveConfig = new Value();
    /** The discounting curve currency. Usually the default value of the {@link ValuePropertyNames#CURVE_CURRENCY} property */
    private final Value _discountingCurveCurrency = new Value();
    /** The volatility surface name. Usually the default value of the {@link ValuePropertyNames#SURFACE} property */
    private final Value _volatilitySurface = new Value();
    /** The volatility surface calculation method. Usually the default value of the {@link ValuePropertyNames#SURFACE_CALCULATION_METHOD} property */
    private final Value _surfaceCalculationMethod = new Value();
    /** The volatility surface interpolation method name. Usually the default value of the {@link BlackVolatilitySurfacePropertyNamesAndValues#PROPERTY_SMILE_INTERPOLATOR} property */
    private final Value _surfaceInterpolationMethod = new Value();
    /** The forward curve name. Usually the default value of the {@link ForwardCurveValuePropertyNames#PROPERTY_FORWARD_CURVE_NAME} property */
    private final Value _forwardCurve = new Value();
    /** The forward curve interpolator */
    private final Value _forwardCurveInterpolator = new Value();
    /** The forward curve left extrapolator */
    private final Value _forwardCurveLeftExtrapolator = new Value();
    /** The forward curve right extrapolator */
    private final Value _forwardCurveRightExtrapolator = new Value();
    /** The forward curve calculation method name. Usually the default value of the {@link ForwardCurveValuePropertyNames#PROPERTY_FORWARD_CURVE_CALCULATION_METHOD} property */
    private final Value _forwardCurveCalculationMethod = new Value();
    /** The dividend type. Usually the default value of the {@link ValuePropertyNames#DIVIDEND_TYPE} property */
    private final Value _dividendType = new Value();

    /**
     * @param equity The equity ticker.
     */
    public EquityInfo(final String equity) {
      _equity = equity;
    }

    /**
     * Gets the equity id.
     * @return The equity id
     */
    public String getEquity() {
      return _equity;
    }

    /**
     * Sets the discounting curve name for a key.
     * @param key The key
     * @param discountingCurve The discounting curve name
     */
    public void setDiscountingCurve(final String key, final String discountingCurve) {
      _discountingCurve.set(key, discountingCurve);
    }

    /**
     * Gets the discounting curve name for a key.
     * @param key The key
     * @return The discounting curve name
     */
    public String getDiscountingCurve(final String key) {
      return _discountingCurve.get(key);
    }

    /**
     * Sets the discounting curve configuration name.
     * @param key The key
     * @param discountingCurveConfig The discounting curve configuration name
     */
    public void setDiscountingCurveConfig(final String key, final String discountingCurveConfig) {
      _discountingCurveConfig.set(key, discountingCurveConfig);
    }

    /**
     * Gets the discounting curve configuration name for a key.
     * @param key The key
     * @return The discounting curve configuration name
     */
    public String getDiscountingCurveConfig(final String key) {
      return _discountingCurveConfig.get(key);
    }

    /**
     * Sets the discounting curve currency.
     * @param key The key
     * @param discountingCurveCurrency The discounting curve currency
     */
    public void setDiscountingCurveCurrency(final String key, final String discountingCurveCurrency) {
      _discountingCurveCurrency.set(key, discountingCurveCurrency);
    }

    /**
     * Gets the discounting curve currency for a key.
     * @param key The key
     * @return The discounting curve currency
     */
    public String getDiscountingCurveCurrency(final String key) {
      return _discountingCurveCurrency.get(key);
    }

    /**
     * Sets the volatility surface name for a key.
     * @param key The key
     * @param volatilitySurface The volatility surface name 
     */
    public void setVolatilitySurface(final String key, final String volatilitySurface) {
      _volatilitySurface.set(key, volatilitySurface);
    }

    /**
     * Gets the volatility surface name for a key.
     * @param key The key
     * @return The volatility surface name
     */
    public String getVolatilitySurface(final String key) {
      return _volatilitySurface.get(key);
    }

    /**
     * Sets the volatility surface calculation method for a key.
     * @param key The key
     * @param surfaceCalculationMethod The volatility surface calculation method
     */
    public void setSurfaceCalculationMethod(final String key, final String surfaceCalculationMethod) {
      _surfaceCalculationMethod.set(key, surfaceCalculationMethod);
    }

    /**
     * Gets the volatility surface calculation method for a key.
     * @param key The key
     * @return The volatility surface calculation method
     */
    public String getSurfaceCalculationMethod(final String key) {
      return _surfaceCalculationMethod.get(key);
    }

    /**
     * Sets the volatility surface interpolation method for a key.
     * @param key The key
     * @param surfaceInterpolationMethod The volatility surface interpolation method
     */
    public void setSurfaceInterpolationMethod(final String key, final String surfaceInterpolationMethod) {
      _surfaceInterpolationMethod.set(key, surfaceInterpolationMethod);
    }

    /**
     * Gets the volatility surface interpolation method for a key.
     * @param key The key
     * @return The volatility surface interpolation method
     */
    public String getSurfaceInterpolationMethod(final String key) {
      return _surfaceInterpolationMethod.get(key);
    }

    /**
     * Sets the forward curve name for a key.
     * @param key The key
     * @param forwardCurve The forward curve name
     */
    public void setForwardCurve(final String key, final String forwardCurve) {
      _forwardCurve.set(key, forwardCurve);
    }

    /**
     * Gets the forward curve name for a key.
     * @param key The key
     * @return The forward curve name
     */
    public String getForwardCurve(final String key) {
      return _forwardCurve.get(key);
    }

    /**
     * Sets the forward curve interpolator name for a key.
     * @param key The key
     * @param forwardCurveInterpolator The forward curve interpolator name
     */
    public void setForwardCurveInterpolator(final String key, final String forwardCurveInterpolator) {
      _forwardCurveInterpolator.set(key, forwardCurveInterpolator);
    }

    /**
     * Gets the forward curve interpolator name for a key.
     * @param key The key
     * @return The forward curve interpolator name
     */
    public String getForwardCurveInterpolator(final String key) {
      return _forwardCurveInterpolator.get(key);
    }

    /**
     * Sets the forward curve left extrapolator name for a key.
     * @param key The key
     * @param forwardCurveLeftExtrapolator The forward curve left extrapolator name
     */
    public void setForwardCurveLeftExtrapolator(final String key, final String forwardCurveLeftExtrapolator) {
      _forwardCurveLeftExtrapolator.set(key, forwardCurveLeftExtrapolator);
    }

    /**
     * Gets the forward curve name for a key.
     * @param key The key
     * @return The forward curve name
     */
    public String getForwardCurveLeftExtrapolator(final String key) {
      return _forwardCurveLeftExtrapolator.get(key);
    }

    /**
     * Sets the forward curve right extrapolator name for a key.
     * @param key The key
     * @param forwardCurveRightExtrapolator The forward curve right extrapolator name
     */
    public void setForwardCurveRightExtrapolator(final String key, final String forwardCurveRightExtrapolator) {
      _forwardCurveRightExtrapolator.set(key, forwardCurveRightExtrapolator);
    }

    /**
     * Gets the forward curve right extrapolator name for a key.
     * @param key The key
     * @return The forward curve right extrapolator name
     */
    public String getForwardCurveRightExtrapolator(final String key) {
      return _forwardCurveRightExtrapolator.get(key);
    }

    /**
     * Sets the forward curve calculation method for a key.
     * @param key The key
     * @param forwardCurveCalculationMethod The forward curve calculation method
     */
    public void setForwardCurveCalculationMethod(final String key, final String forwardCurveCalculationMethod) {
      _forwardCurveCalculationMethod.set(key, forwardCurveCalculationMethod);
    }

    /**
     * Gets the forward curve calculation method for a key.
     * @param key The key
     * @return The forward curve calculation method
     */
    public String getForwardCurveCalculationMethod(final String key) {
      return _forwardCurveCalculationMethod.get(key);
    }

    /**
     * Sets the dividend type for a key.
     * @param key The key
     * @param dividendType The dividend type
     */
    public void setDividendType(final String key, final String dividendType) {
      _dividendType.set(key, dividendType);
    }

    /**
     * Gets the dividend type for a key.
     * @param key The key
     * @return The dividend type
     */
    public String getDividendType(final String key) {
      return _dividendType.get(key);
    }
  }

  /** A map of currency strings to per-currency default values */
  private final Map<String, CurrencyInfo> _perCurrencyInfo = new HashMap<>();
  /** A map of currency string pairs to per-currency pair default values */
  private final Map<Pair<String, String>, CurrencyPairInfo> _perCurrencyPairInfo = new HashMap<>();
  /** A map of equity ticker strings to equity ticker default values */
  private final Map<String, EquityInfo> _perEquityInfo = new HashMap<>();
  /** The default value of the mark to market data field */
  private String _mark2MarketField;
  /** The default value of the cost of carry data field */
  private String _costOfCarryField;
  /** The default value of the absolute tolerance used in curve root-finding */
  private double _absoluteTolerance;
  /** The default value of the relative tolerance used in curve root-finding */
  private double _relativeTolerance;
  /** The default value of the maximum iterations used in curve root-finding */
  private int _maxIterations;

  /**
   * Sets the default values for the currencies and currency pairs.
   */
  public StandardFunctionConfiguration() {
    setDefaultCurrencyInfo();
    setDefaultCurrencyPairInfo();
  }

  /**
   * Sets the map of currency strings to per-currency default values.
   * @param perCurrencyInfo A map of currency strings to per-currency default values.
   */
  public void setPerCurrencyInfo(final Map<String, CurrencyInfo> perCurrencyInfo) {
    _perCurrencyInfo.clear();
    _perCurrencyInfo.putAll(perCurrencyInfo);
  }

  /**
   * Gets the map of currency strings to per-currency default values.
   * @return The map of currency strings to per-currency default values
   */
  public Map<String, CurrencyInfo> getPerCurrencyInfo() {
    return _perCurrencyInfo;
  }

  /**
   * Sets per-currency default values for a currency string.
   * @param currency The currency
   * @param info The per-currency default values
   */
  public void setCurrencyInfo(final String currency, final CurrencyInfo info) {
    _perCurrencyInfo.put(currency, info);
  }

  /**
   * Gets the per-currency default values for a currency string. 
   * @param currency The currency
   * @return The per-currency default values
   */
  public CurrencyInfo getCurrencyInfo(final String currency) {
    return _perCurrencyInfo.get(currency);
  }

  /**
   * Gets the currency information for a given filter.
   * @param <T> The type of the object that contains default values for a currency
   * @param filter The filter
   * @return T The object that contains default values for a currency
   */
  protected <T> Map<String, T> getCurrencyInfo(final Function1<CurrencyInfo, T> filter) {
    final Map<String, T> result = new HashMap<>();
    for (final Map.Entry<String, CurrencyInfo> e : getPerCurrencyInfo().entrySet()) {
      final T entry = filter.execute(e.getValue());
      if (entry instanceof InitializingBean) {
        try {
          ((InitializingBean) entry).afterPropertiesSet();
        } catch (final Exception ex) {
          s_logger.debug("Skipping {}", e.getKey());
          s_logger.trace("Caught exception", e);
          continue;
        }
      }
      if (entry != null) {
        result.put(e.getKey(), entry);
      }
    }
    return result;
  }

  /**
   * Sets the map of currency pair strings to per-currency pair default values.
   * @param perCurrencyPairInfo A map of currency pair strings to per-currency pair default values
   */
  public void setPerCurrencyPairInfo(final Map<Pair<String, String>, CurrencyPairInfo> perCurrencyPairInfo) {
    _perCurrencyPairInfo.clear();
    _perCurrencyPairInfo.putAll(perCurrencyPairInfo);
  }

  /**
   * Gets the map of currency pair strings to per-currency pair default values.
   * @return A map of currency pair strings to per-currency pair default values
   */
  public Map<Pair<String, String>, CurrencyPairInfo> getPerCurrencyPairInfo() {
    return _perCurrencyPairInfo;
  }

  /**
   * Sets information for a currency pair.
   * @param currencyPair The currency pair strings
   * @param info The information
   */
  public void setCurrencyPairInfo(final Pair<String, String> currencyPair, final CurrencyPairInfo info) {
    _perCurrencyPairInfo.put(currencyPair, info);
  }

  /**
   * Gets information for a currency pair.
   * @param currencyPair The currency pair strings
   * @return The information
   */
  public CurrencyPairInfo getCurrencyPairInfo(final Pair<String, String> currencyPair) {
    return _perCurrencyPairInfo.get(currencyPair);
  }

  /**
   * Gets the currency pair information for a given filter.
   * @param <T> The type of the object that contains default values for a currency pair 
   * @param filter The filter
   * @return T The object that contains default values for a currency pair
   */
  protected <T> Map<Pair<String, String>, T> getCurrencyPairInfo(final Function1<CurrencyPairInfo, T> filter) {
    final Map<Pair<String, String>, T> result = new HashMap<>();
    for (final Map.Entry<Pair<String, String>, CurrencyPairInfo> e : getPerCurrencyPairInfo().entrySet()) {
      final T entry = filter.execute(e.getValue());
      if (entry instanceof InitializingBean) {
        try {
          ((InitializingBean) entry).afterPropertiesSet();
        } catch (final Exception ex) {
          s_logger.debug("Skipping {}", e.getKey());
          s_logger.trace("Caught exception", e);
          continue;
        }
      }
      if (entry != null) {
        result.put(e.getKey(), entry);
      }
    }
    return result;
  }

  /**
   * Sets the map of equity tickers to per-equity ticker default values.
   * @param perEquityInfo A map of equity tickers to per-equity default values.
   */
  public void setPerEquityInfo(final Map<String, EquityInfo> perEquityInfo) {
    _perEquityInfo.clear();
    _perEquityInfo.putAll(perEquityInfo);
  }

  /**
   * Gets the map of equity tickers to per-equity ticker default values.
   * @return The map of equity tickers to per-equity ticker default values
   */
  public Map<String, EquityInfo> getPerEquityInfo() {
    return _perEquityInfo;
  }

  /**
   * Sets per-equity default values for an equity ticker.
   * @param equity The equity ticker
   * @param info The per-equity ticker default values
   */
  public void setEquityInfo(final String equity, final EquityInfo info) {
    _perEquityInfo.put(equity, info);
  }

  /**
   * Gets the per-equity default values for an equity ticker. 
   * @param equity The equity ticker
   * @return The per-equity default values
   */
  public EquityInfo getEquityInfo(final String equity) {
    return _perEquityInfo.get(equity);
  }

  /**
   * Creates a per-equity default information object for an equity string.
   * @param equity The equity string
   * @return An empty per-equity info object
   */
  protected EquityInfo defaultEquityInfo(final String equity) {
    return new EquityInfo(equity);
  }

  /**
   * Gets the equity ticker information for a given filter.
   * @param <T> The type of the object that contains default values for an equity ticker
   * @param filter The filter
   * @return T The object that contains default values for an equity ticker
   */
  protected <T> Map<String, T> getEquityInfo(final Function1<EquityInfo, T> filter) {
    final Map<String, T> result = new HashMap<>();
    for (final Map.Entry<String, EquityInfo> e : getPerEquityInfo().entrySet()) {
      final T entry = filter.execute(e.getValue());
      if (entry instanceof InitializingBean) {
        try {
          ((InitializingBean) entry).afterPropertiesSet();
        } catch (final Exception ex) {
          s_logger.debug("Skipping {}", e.getKey());
          s_logger.trace("Caught exception", e);
          continue;
        }
      }
      if (entry != null) {
        result.put(e.getKey(), entry);
      }
    }
    return result;
  }

  /**
   * Sets the default value of the mark-to-market data field.
   * @param mark2MarketField The mark-to-market data field.
   */
  public void setMark2MarketField(final String mark2MarketField) {
    _mark2MarketField = mark2MarketField;
  }

  /**
   * Gets the default value of the mark-to-market data field.
   * @return The mark-to-market data field
   */
  public String getMark2MarketField() {
    return _mark2MarketField;
  }

  /**
   * Sets the default value of the cost-of-carry data field.
   * @param costOfCarryField The cost-of-carry data field
   */
  public void setCostOfCarryField(final String costOfCarryField) {
    _costOfCarryField = costOfCarryField;
  }

  /**
   * Gets the default value of the cost-of-carry data field.
   * @return The cost-of-carry field
   */
  public String getCostOfCarryField() {
    return _costOfCarryField;
  }

  /**
   * Sets the absolute tolerance for the curve root-finder.
   * @param absoluteTolerance The absolute tolerance, greater than zero
   */
  public void setAbsoluteTolerance(final double absoluteTolerance) {
    _absoluteTolerance = absoluteTolerance;
  }

  /**
   * Gets the absolute tolerance for the curve root-finder.
   * @return The absolute tolerance
   */
  public double getAbsoluteTolerance() {
    return _absoluteTolerance;
  }

  /**
   * Sets the relative tolerance for the curve root-finder.
   * @param relativeTolerance The relative tolerance, greater than zero
   */
  public void setRelativeTolerance(final double relativeTolerance) {
    _relativeTolerance = relativeTolerance;
  }

  /**
   * Gets the relative tolerance for the curve root-finder.
   * @return The relative tolerance
   */
  public double getRelativeTolerance() {
    return _relativeTolerance;
  }

  /**
   * Sets the maximum number of iterations for the curve root-finder.
   * @param maxIterations The maximum iterations, greater than zero
   */
  public void setMaximumIterations(final int maxIterations) {
    _maxIterations = maxIterations;
  }

  /**
   * Gets the maximum number of iterations for the curve root-finder.
   * @return The maximum iterations
   */
  public int getMaximumIterations() {
    return _maxIterations;
  }

  /**
   * Creates a per-currency default information object for a currency string.
   * @param currency The currency string
   * @return An empty per-currency info object
   */
  protected CurrencyInfo defaultCurrencyInfo(final String currency) {
    return new CurrencyInfo(currency);
  }

  /**
   * Creates a per-currency default information object for ARS.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo arsCurrencyInfo() {
    return defaultCurrencyInfo("ARS");
  }

  /**
   * Creates a per-currency default information object for AUD.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo audCurrencyInfo() {
    return defaultCurrencyInfo("AUD");
  }

  /**
   * Creates a per-currency default information object for BRL.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo brlCurrencyInfo() {
    return defaultCurrencyInfo("BRL");
  }

  /**
   * Creates a per-currency default information object for CAD.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo cadCurrencyInfo() {
    return defaultCurrencyInfo("CAD");
  }

  /**
   * Creates a per-currency default information object for CHF.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo chfCurrencyInfo() {
    return defaultCurrencyInfo("CHF");
  }

  /**
   * Creates a per-currency default information object for CNY.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo cnyCurrencyInfo() {
    return defaultCurrencyInfo("CNY");
  }

  /**
   * Creates a per-currency default information object for CZK.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo czkCurrencyInfo() {
    return defaultCurrencyInfo("CZK");
  }

  /**
   * Creates a per-currency default information object for EGP.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo egpCurrencyInfo() {
    return defaultCurrencyInfo("EGP");
  }

  /**
   * Creates a per-currency default information object for EUR.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo eurCurrencyInfo() {
    return defaultCurrencyInfo("EUR");
  }

  /**
   * Creates a per-currency default information object for GBP.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo gbpCurrencyInfo() {
    return defaultCurrencyInfo("GBP");
  }

  /**
   * Creates a per-currency default information object for HKD.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo hkdCurrencyInfo() {
    return defaultCurrencyInfo("HKD");
  }

  /**
   * Creates a per-currency default information object for HUF.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo hufCurrencyInfo() {
    return defaultCurrencyInfo("HUF");
  }

  /**
   * Creates a per-currency default information object for IDR.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo idrCurrencyInfo() {
    return defaultCurrencyInfo("IDR");
  }

  /**
   * Creates a per-currency default information object for ILS.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo ilsCurrencyInfo() {
    return defaultCurrencyInfo("ILS");
  }

  /**
   * Creates a per-currency default information object for INR.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo inrCurrencyInfo() {
    return defaultCurrencyInfo("INR");
  }

  /**
   * Creates a per-currency default information object for JPY.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo jpyCurrencyInfo() {
    return defaultCurrencyInfo("JPY");
  }

  /**
   * Creates a per-currency default information object for KRW.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo krwCurrencyInfo() {
    return defaultCurrencyInfo("KRW");
  }

  /**
   * Creates a per-currency default information object for MXN.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo mxnCurrencyInfo() {
    return defaultCurrencyInfo("MXN");
  }

  /**
   * Creates a per-currency default information object for MYR.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo myrCurrencyInfo() {
    return defaultCurrencyInfo("MYR");
  }

  /**
   * Creates a per-currency default information object for NOK.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo nokCurrencyInfo() {
    return defaultCurrencyInfo("NOK");
  }

  /**
   * Creates a per-currency default information object for NZD.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo nzdCurrencyInfo() {
    return defaultCurrencyInfo("NZD");
  }

  /**
   * Creates a per-currency default information object for PHP.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo phpCurrencyInfo() {
    return defaultCurrencyInfo("PHP");
  }

  /**
   * Creates a per-currency default information object for PLN.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo plnCurrencyInfo() {
    return defaultCurrencyInfo("PLN");
  }

  /**
   * Creates a per-currency default information object for RUB.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo rubCurrencyInfo() {
    return defaultCurrencyInfo("RUB");
  }

  /**
   * Creates a per-currency default information object for SEK.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo sekCurrencyInfo() {
    return defaultCurrencyInfo("SEK");
  }

  /**
   * Creates a per-currency default information object for SGD.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo sgdCurrencyInfo() {
    return defaultCurrencyInfo("SGD");
  }

  /**
   * Creates a per-currency default information object for TRY.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo tryCurrencyInfo() {
    return defaultCurrencyInfo("TRY");
  }

  /**
   * Creates a per-currency default information object for TWD.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo twdCurrencyInfo() {
    return defaultCurrencyInfo("TWD");
  }

  /**
   * Creates a per-currency default information object for USD.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo usdCurrencyInfo() {
    return defaultCurrencyInfo("USD");
  }

  /**
   * Creates a per-currency default information object for ZAR.
   * @return An empty per-currency info object
   */
  protected CurrencyInfo zarCurrencyInfo() {
    return defaultCurrencyInfo("ZAR");
  }

  /**
   * Creates empty default per-currency information objects.
   */
  protected void setDefaultCurrencyInfo() {
    setCurrencyInfo("ARS", arsCurrencyInfo());
    setCurrencyInfo("AUD", audCurrencyInfo());
    setCurrencyInfo("BRL", brlCurrencyInfo());
    setCurrencyInfo("CAD", cadCurrencyInfo());
    setCurrencyInfo("CHF", chfCurrencyInfo());
    setCurrencyInfo("CNY", cnyCurrencyInfo());
    setCurrencyInfo("CZK", czkCurrencyInfo());
    setCurrencyInfo("EGP", egpCurrencyInfo());
    setCurrencyInfo("EUR", eurCurrencyInfo());
    setCurrencyInfo("GBP", gbpCurrencyInfo());
    setCurrencyInfo("HKD", hkdCurrencyInfo());
    setCurrencyInfo("HUF", hufCurrencyInfo());
    setCurrencyInfo("IDR", idrCurrencyInfo());
    setCurrencyInfo("ILS", ilsCurrencyInfo());
    setCurrencyInfo("INR", inrCurrencyInfo());
    setCurrencyInfo("JPY", jpyCurrencyInfo());
    setCurrencyInfo("KRW", krwCurrencyInfo());
    setCurrencyInfo("MXN", mxnCurrencyInfo());
    setCurrencyInfo("MYR", myrCurrencyInfo());
    setCurrencyInfo("NOK", nokCurrencyInfo());
    setCurrencyInfo("NZD", nzdCurrencyInfo());
    setCurrencyInfo("PHP", phpCurrencyInfo());
    setCurrencyInfo("PLN", plnCurrencyInfo());
    setCurrencyInfo("RUB", rubCurrencyInfo());
    setCurrencyInfo("SEK", sekCurrencyInfo());
    setCurrencyInfo("SGD", sgdCurrencyInfo());
    setCurrencyInfo("TRY", tryCurrencyInfo());
    setCurrencyInfo("TWD", twdCurrencyInfo());
    setCurrencyInfo("USD", usdCurrencyInfo());
    setCurrencyInfo("ZAR", zarCurrencyInfo());
  }

  /**
   * Creates a per-currency pair default information object for a pair of currency strings.
   * @param c1 The first currency string
   * @param c2 The second currency string
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo defaultCurrencyPairInfo(final String c1, final String c2) {
    return new CurrencyPairInfo(Pairs.of(c1, c2));
  }

  /**
   * Creates a per-currency pair default information object for AUD/KRW.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo audKrwCurrencyPairInfo() {
    return defaultCurrencyPairInfo("AUD", "KRW");
  }

  /**
   * Creates a per-currency pair default information object for CHF/JPY.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo chfJpyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("CHF", "JPY");
  }

  /**
   * Creates a per-currency pair default information object for EUR/BRL.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo eurBrlCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "BRL");
  }

  /**
   * Creates a per-currency pair default information object for EUR/CHF.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo eurChfCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "CHF");
  }

  /**
   * Creates a per-currency pair default information object for EUR/GBP.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo eurGbpCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "GBP");
  }

  /**
   * Creates a per-currency pair default information object for EUR/JPY.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo eurJpyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "JPY");
  }

  /**
   * Creates a per-currency pair default information object for EUR/TRY.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo eurTryCurrencyPairInfo() {
    return defaultCurrencyPairInfo("EUR", "TRY");
  }

  /**
   * Creates a per-currency pair default information object for JPY/KRW.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo jpyKrwCurrencyPairInfo() {
    return defaultCurrencyPairInfo("JPY", "KRW");
  }

  /**
   * Creates a per-currency pair default information object for SEK/JPY.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo sekJpyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("SEK", "JPY");
  }

  /**
   * Creates a per-currency pair default information object for USD/AUD.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdAudCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "AUD");
  }

  /**
   * Creates a per-currency pair default information object for USD/BRL.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdBrlCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "BRL");
  }

  /**
   * Creates a per-currency pair default information object for USD/CAD.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdCadCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "CAD");
  }

  /**
   * Creates a per-currency pair default information object for USD/CHF.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdChfCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "CHF");
  }

  /**
   * Creates a per-currency pair default information object for USD/CNY.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdCnyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "CNY");
  }

  /**
   * Creates a per-currency pair default information object for USD/EUR.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdEurCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "EUR");
  }

  /**
   * Creates a per-currency pair default information object for USD/GBP.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdGbpCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "GBP");
  }

  /**
   * Creates a per-currency pair default information object for USD/HKD.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdHkdCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "HKD");
  }

  /**
   * Creates a per-currency pair default information object for USD/HUF.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdHufCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "HUF");
  }

  /**
   * Creates a per-currency pair default information object for USD/INR.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdInrCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "INR");
  }

  /**
   * Creates a per-currency pair default information object for USD/JPY.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdJpyCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "JPY");
  }

  /**
   * Creates a per-currency pair default information object for USD/KRW.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdKrwCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "KRW");
  }

  /**
   * Creates a per-currency pair default information object for USD/MXN.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdMxnCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "MXN");
  }

  /**
   * Creates a per-currency pair default information object for USD/NOK.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdNokCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "NOK");
  }

  /**
   * Creates a per-currency pair default information object for USD/NZD.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdNzdCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "NZD");
  }

  /**
   * Creates a per-currency pair default information object for USD/SGD.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdSgdCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "SGD");
  }

  /**
   * Creates a per-currency pair default information object for USD/ZAR.
   * @return An empty per-currency pair info object
   */
  protected CurrencyPairInfo usdZarCurrencyPairInfo() {
    return defaultCurrencyPairInfo("USD", "ZAR");
  }

  /**
   * Creates empty default per-currency pair information objects.
   */
  protected void setDefaultCurrencyPairInfo() {
    setCurrencyPairInfo(Pairs.of("AUD", "KRW"), audKrwCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("CHF", "JPY"), chfJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("EUR", "BRL"), eurBrlCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("EUR", "CHF"), eurChfCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("EUR", "GBP"), eurGbpCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("EUR", "JPY"), eurJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("EUR", "TRY"), eurTryCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("JPY", "KRW"), jpyKrwCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("SEK", "JPY"), sekJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "AUD"), usdAudCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "BRL"), usdBrlCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "CAD"), usdCadCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "CHF"), usdChfCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "CNY"), usdCnyCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "EUR"), usdEurCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "GBP"), usdGbpCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "HKD"), usdHkdCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "HUF"), usdHufCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "INR"), usdInrCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "JPY"), usdJpyCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "KRW"), usdKrwCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "MXN"), usdMxnCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "NOK"), usdNokCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "NZD"), usdNzdCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "SGD"), usdSgdCurrencyPairInfo());
    setCurrencyPairInfo(Pairs.of("USD", "ZAR"), usdZarCurrencyPairInfo());
  }

  /**
   * Adds the default name of the {@link CurrencyPairs} configuration to the list of functions.
   * @param functionConfigs The list of functions
   */
  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(CurrencyPairsDefaults.class, CurrencyPairs.DEFAULT_CURRENCY_PAIRS));
  }

  /**
   * Adds the default function for local volatility surface defaults to the list of functions.
   * @param functionConfigs The list of functions
   */
  protected void addLocalVolatilitySurfaceDefaults(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(new ParameterizedFunctionConfiguration(LocalVolatilitySurfaceDefaults.class.getName(),
        GeneralLocalVolatilitySurfaceDefaults.getLocalVolatilitySurfaceDefaults()));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    addCurrencyConversionFunctions(functions);
    addLocalVolatilitySurfaceDefaults(functions);
  }

  /**
   * Performs any operations that are required after properties are set e.g. null checks.
   * @param defaults The configuration source populated with defaults
   * @return A populated configuration source
   */
  protected FunctionConfigurationSource getRepository(final SingletonFactoryBean<FunctionConfigurationSource> defaults) {
    try {
      defaults.afterPropertiesSet();
    } catch (final Exception e) {
      s_logger.warn("Caught exception", e);
      return null;
    }
    return defaults.getObject();
  }

  /**
   * Sets the paths for the per-currency default values for functions that extend {@link BondFunction} with the keys<p>
   * <ul>
   * <li> Risk-free curve name = model/bond/riskFree => curveName field
   * <li> Risk-free curve calculation configuration name = model/bond/riskFree => curveConfiguration field
   * <li> Credit curve name = model/bond/credit => curveName field
   * <li> Credit curve calculation configuration name = model/bond/credit => curveConfiguration field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setBondFunctionDefaults(final CurrencyInfo i, final BondFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setRiskFreeCurveName(i.getCurveName("model/bond/riskFree"));
    defaults.setRiskFreeCurveCalculationConfig(i.getCurveConfiguration("model/bond/riskFree"));
    defaults.setCreditCurveName(i.getCurveName("model/bond/credit"));
    defaults.setCreditCurveCalculationConfig(i.getCurveConfiguration("model/bond/credit"));
  }

  /**
   * Sets the per-currency default values for functions that extend {@link BondFunction}.
   * @param defaults The object containing the default values
   */
  protected void setBondFunctionDefaults(final BondFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, BondFunctions.Defaults.CurrencyInfo>() {
      @Override
      public BondFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final BondFunctions.Defaults.CurrencyInfo d = new BondFunctions.Defaults.CurrencyInfo();
        setBondFunctionDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for functions that extend {@link BondFunction} to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource bondFunctions() {
    final BondFunctions.Defaults defaults = new BondFunctions.Defaults();
    setBondFunctionDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets the paths for the per-currency default values for functions that extend {@link BondFutureOptionBlackFunction} with the keys<p>
   * <ul>
   * <li> Curve configuration name = model/bondfutureoption => curveConfiguration field
   * <li> Surface name = model/bondfutureoption => surfaceName field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setBondFutureOptionDefaults(final CurrencyInfo i, final BondFutureOptionFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfig(i.getCurveConfiguration("model/bondfutureoption"));
    defaults.setSurfaceName(i.getSurfaceName("model/bondfutureoption"));
  }

  /**
   * Sets the per-currency default values for functions that extend {@link BondFutureOptionBlackFunction}.
   * @param defaults The object containing the default values
   */
  protected void setBondFutureOptionDefaults(final BondFutureOptionFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, BondFutureOptionFunctions.Defaults.CurrencyInfo>() {
      @Override
      public BondFutureOptionFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final BondFutureOptionFunctions.Defaults.CurrencyInfo d = new BondFutureOptionFunctions.Defaults.CurrencyInfo();
        setBondFutureOptionDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for functions that extend {@link BondFutureOptionBlackFunction} to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource bondFutureOptionFunctions() {
    final BondFutureOptionFunctions.Defaults defaults = new BondFutureOptionFunctions.Defaults();
    setBondFutureOptionDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets the paths for the per-currency default values for CDS and CDX functions with the keys<p>
   * <ul>
   * <li> Yield curve configuration name = model/credit/yield => curveConfiguration field
   * <li> Yield curve name = model/credit/yield => curveName field
   * <li> Yield curve calculation method name = model/credit/yield => curveCalculationMethod field
   * <li> Hazard rate = model/credit/hazardrate => curveCalculationMethod field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setCDSFunctionDefaults(final CurrencyInfo i, final CreditFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveCalculationConfig(i.getCurveConfiguration("model/credit/yield"));
    defaults.setCurveName(i.getCurveName("model/credit/yield"));
    defaults.setCurveCalculationMethod(i.getCurveCalculationMethodName("model/credit/yield"));
    defaults.setCurveCalculationMethod(i.getCurveCalculationMethodName("model/credit/hazardrate"));
  }

  /**
   * Sets the per-currency default values for CDS and CDX functions.
   * @param defaults The object containing the default values
   */
  protected void setCDSFunctionDefaults(final CreditFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, CreditFunctions.Defaults.CurrencyInfo>() {
      @Override
      public CreditFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final CreditFunctions.Defaults.CurrencyInfo d = new CreditFunctions.Defaults.CurrencyInfo();
        setCDSFunctionDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for CDS and CDX functions to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource cdsFunctions() {
    final CreditFunctions.Defaults defaults = new CreditFunctions.Defaults();
    setCDSFunctionDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets the paths for the per-currency default values for cross-currency swaps with the keys<p>
   * <ul>
   * <li> Curve configuration name = model/xccyswap => curveConfiguration field
   * </ul>
   * <p>
   * <b>This default value will only work correctly if the curve configuration has curves in both
   * currencies<b>.
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setXCcySwapFunctionDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveCalculationConfig(i.getCurveConfiguration("model/xccyswap"));
  }

  /**
   * Sets the per-currency default values for cross-currency swaps.
   * @param defaults The object containing the default values
   */
  protected void setXCcySwapFunctionDefaults(final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo();
        setXCcySwapFunctionDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for cross-currency swaps to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource xCcySwapFunctions() {
    final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults defaults =
        new com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults();
    setXCcySwapFunctionDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Adds deprecated functions to the configuration source. This method does not add any functions
   * but is maintained for backwards compatibility.
   * @return The configuration source populated with deprecated functions
   */
  protected FunctionConfigurationSource deprecatedFunctions() {
    return null;
  }

  /**
   * This method does not add any defaults but is maintained for backwards compatibility.
   * @param defaults The object containing default values
   */
  protected void setEquityOptionDefaults(final OptionFunctions.Defaults defaults) {
    return;
  }

  /**
   * Adds deprecated equity option functions to the configuration source. This method adds defaults
   * for equity barrier options but does not add any curve or surface default values to the 
   * configuration source.
   * @return The configuration source populated with deprecated functions
   */
  protected FunctionConfigurationSource equityOptionFunctions() {
    final OptionFunctions.Defaults defaults = new OptionFunctions.Defaults();
    setEquityOptionDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * This method does not add any defaults but is maintained for backwards compatibility.
   * @param calculators The external sensitivities calculators
   */
  protected void setExternalSensitivitesCalculators(final SensitivitiesFunctions.Calculators calculators) {
    return;
  }

  /**
   * Sets the paths for the per-currency default values for external sensitivities with the keys<p>
   * <ul>
   * <li> Curve configuration name = model/sensitivities => curveConfiguration field
   * </ul>
   * <p>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setExternalSensitivitiesDefaults(final CurrencyInfo i, final SensitivitiesFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/sensitivities"));
  }

  /**
   * Sets the per-currency default values for external sensitivities.
   * @param defaults The object containing the default values
   */
  protected void setExternalSensitivitiesDefaults(final SensitivitiesFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, SensitivitiesFunctions.Defaults.CurrencyInfo>() {
      @Override
      public SensitivitiesFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final SensitivitiesFunctions.Defaults.CurrencyInfo d = new SensitivitiesFunctions.Defaults.CurrencyInfo();
        setExternalSensitivitiesDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for external sensitivities to the configuration source. This method also sets the value for the historical time
   * series resolution key as defined in {@link SensitivitiesFunctions}.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource externalSensitivitiesFunctions() {
    final SensitivitiesFunctions.Calculators calculators = new SensitivitiesFunctions.Calculators();
    setExternalSensitivitesCalculators(calculators);
    final SensitivitiesFunctions.Defaults defaults = new SensitivitiesFunctions.Defaults();
    setExternalSensitivitiesDefaults(defaults);
    return CombiningFunctionConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  /**
   * Sets the paths for the per-currency default values for functions that extend {@link InterestRateInstrumentFunction} with the keys<p>
   * <ul>
   * <li> Curve configuration name = model/fixedincome => curveConfiguration field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  @SuppressWarnings("javadoc")
  protected void setFixedIncomeDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveCalculationConfig(i.getCurveConfiguration("model/fixedincome"));
  }

  /**
   * Sets the per-currency default values for functions that extend {@link InterestRateInstrumentFunction}.
   * @param defaults The object containing the default values
   */
  @SuppressWarnings("javadoc")
  protected void setFixedIncomeDefaults(final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults.CurrencyInfo();
        setFixedIncomeDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for functions that extend {@link InterestRateInstrumentFunction} to the configuration source.
   * @return The configuration source populated with default values
   */
  @SuppressWarnings("javadoc")
  protected FunctionConfigurationSource fixedIncomeFunctions() {
    final com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults defaults =
        new com.opengamma.financial.analytics.model.fixedincome.DeprecatedFunctions.Defaults();
    setFixedIncomeDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets the paths for the per-currency default values for functions that price FX instruments with the keys<p>
   * <ul>
   * <li> Curve configuration name = model/forex => curveConfiguration field
   * <li> Discounting curve name = model/forex/discounting => discountingCurve field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setForexDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/forex"));
    defaults.setDiscountingCurve(i.getCurveName("model/forex/discounting"));
  }

  /**
   * Sets the per-currency pair default values for functions that price FX options.
   * <ul>
   * <li> Volatility surface name = model/forex => volatilitySurface field
   * <li> Forward curve name = model/forex/forward => forwardCurveName field
   * </ul>
   * @param i The per-currency pair info
   * @param defaults The object containing the default values
   */
  protected void setForexDefaults(final CurrencyPairInfo i, final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo defaults) {
    defaults.setSurfaceName(i.getSurfaceName("model/forex"));
    defaults.setForwardCurveName(i.getForwardCurveName("model/forex/forward"));
  }

  /**
   * Sets the per-currency and per-currency pair default values for FX instruments.
   * @param defaults The object containing the default values
   */
  protected void setForexDefaults(final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setForexDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function1<CurrencyPairInfo, com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo execute(final CurrencyPairInfo i) {
        final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo d =
            new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo();
        setForexDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for functions that price FX instruments to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource forexFunctions() {
    final com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions defaults =
        new com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions();
    setForexDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets default values (over-hedge and relative strike smoothing) for one-look FX barrier options priced with the Black method.
   * @param defaults The object containing the default values
   */
  protected void setForexOptionDefaults(final com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.Defaults defaults) {
    return;
  }

  /**
   * Adds default values for functions that price one-look FX barrier options with the Black method to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource forexOptionFunctions() {
    final com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.Defaults defaults = new com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions.Defaults();
    setForexOptionDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets default values (call spread) for FX digital options priced using a call spread.
   * @param defaults The object containing the default values
   */
  protected void setForexDigitalDefaults(final com.opengamma.financial.analytics.model.forex.option.callspreadblack.CallSpreadBlackFunctions.Defaults defaults) {
    return;
  }

  /**
   * Adds default values for functions that price FX digital options to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource forexDigitalFunctions() {
    final com.opengamma.financial.analytics.model.forex.option.callspreadblack.CallSpreadBlackFunctions.Defaults defaults =
        new com.opengamma.financial.analytics.model.forex.option.callspreadblack.CallSpreadBlackFunctions.Defaults();
    setForexDigitalDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets the paths for the per-currency default values for FX forward curves<p>
   * <ul>
   * <li> Curve configuration name = model/curve/forward => curveConfiguration field
   * <li> Discounting curve name = model/curve/forward/discounting => discountingCurve field
   * <li> Forward curve name = model/curve/forward => curveName field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setForwardCurveDefaults(final CurrencyInfo i, final ForwardFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/curve/forward"));
    defaults.setDiscountingCurve(i.getCurveName("model/curve/forward/discounting"));
    defaults.setForwardCurve(i.getCurveName("model/curve/forward"));
  }

  /**
   * Sets the paths for the per-currency pairs default values for FX forward curves with the keys<p>
   * <ul>
   * <li> Forward curve name = model/curve/forward => curveName field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setForwardCurveDefaults(final CurrencyPairInfo i, final ForwardFunctions.Defaults.CurrencyPairInfo defaults) {
    defaults.setCurveName(i.getCurveName("model/curve/forward"));
  }

  /**
   * Sets the per-currency and per-currency pair default values for FX forward curves.
   * @param defaults The object containing the default values
   */
  protected void setForwardCurveDefaults(final ForwardFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, ForwardFunctions.Defaults.CurrencyInfo>() {
      @Override
      public ForwardFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final ForwardFunctions.Defaults.CurrencyInfo d = new ForwardFunctions.Defaults.CurrencyInfo();
        setForwardCurveDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function1<CurrencyPairInfo, ForwardFunctions.Defaults.CurrencyPairInfo>() {
      @Override
      public ForwardFunctions.Defaults.CurrencyPairInfo execute(final CurrencyPairInfo i) {
        final ForwardFunctions.Defaults.CurrencyPairInfo d = new ForwardFunctions.Defaults.CurrencyPairInfo();
        setForwardCurveDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for functions for functions that construct FX forward to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource forwardCurveFunctions() {
    final ForwardFunctions.Defaults defaults = new ForwardFunctions.Defaults();
    setForwardCurveDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets the paths for the per-currency pairs default values for functions that extend {@link FuturesFunction}
   * with the keys<p>
   * <ul> 
   * <li> Curve configuration = model/future => curveConfiguration field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setFutureDefaults(final CurrencyInfo i, final FutureFunctions.Deprecated.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/future"));
  }

  /**
   * Sets default values for functions that extend {@link FuturesFunction}.
   * @param defaults The default values
   */
  protected void setFutureDefaults(final FutureFunctions.Deprecated defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, FutureFunctions.Deprecated.CurrencyInfo>() {
      @Override
      public FutureFunctions.Deprecated.CurrencyInfo execute(final CurrencyInfo i) {
        final FutureFunctions.Deprecated.CurrencyInfo d = new FutureFunctions.Deprecated.CurrencyInfo();
        setFutureDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Sets the closing price field for functions that extend {@link FuturesFunction}.
   * @param calculators The calculators
   */
  protected void setFutureFunctionCalculators(final FutureFunctions.Calculators calculators) {
    calculators.setClosingPriceField(getMark2MarketField());
  }

  /**
   * Adds default values for functions that extend {@link FuturesFunction} to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource futureFunctions() {
    final FutureFunctions.Calculators calculators = new FutureFunctions.Calculators();
    setFutureFunctionCalculators(calculators);
    final FutureFunctions.Deprecated defaults = new FutureFunctions.Deprecated();
    setFutureDefaults(defaults);
    return CombiningFunctionConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  /**
   * Sets the per-currency default values for functions that extend {@link FutureOptionFunction} with the keys<p>
   * <ul>
   * <li> Curve name = model/futureoption => curveName field
   * <li> Curve calculation configuration name = model/futureoption => curveConfiguration field
   * <li> Volatility surface name = model/futureoption => volatilitySurface field
   * <li> Forward curve name = model/futureoption => forwardCurveName field
   * </ul>
   * and optionally sets<p>
   * <ul>
   * <li> Forward curve calculation method = model/futureoption => forwardCurveCalculationMethod
   * <li> Surface calculation method = model/futureoption => surfaceCalculationMethod
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setFutureOptionDefaults(final CurrencyInfo i, final FutureOptionFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveName(i.getCurveName("model/futureoption"));
    defaults.setCurveCalculationConfig(i.getCurveConfiguration("model/futureoption"));
    defaults.setSurfaceName(i.getSurfaceName("model/futureoption"));
    defaults.setForwardCurveName(i.getForwardCurveName("model/futureoption"));
    String v = i.getForwardCurveCalculationMethod("model/futureoption");
    if (v != null) {
      defaults.setForwardCurveCalculationMethodName(v);
    }
    v = i.getSurfaceCalculationMethod("model/futureoption");
    if (v != null) {
      defaults.setSurfaceCalculationMethod(v);
    }
  }

  /**
   * Sets default values for functions that extend {@link FutureOptionFunction}.
   * @param defaults The default values
   */
  protected void setFutureOptionDefaults(final FutureOptionFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, FutureOptionFunctions.Defaults.CurrencyInfo>() {
      @Override
      public FutureOptionFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final FutureOptionFunctions.Defaults.CurrencyInfo d = new FutureOptionFunctions.Defaults.CurrencyInfo();
        setFutureOptionDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for functions that extend {@link FutureOptionFunction} to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource futureOptionFunctions() {
    final FutureOptionFunctions.Defaults defaults = new FutureOptionFunctions.Defaults();
    setFutureOptionDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Adds default values for horizon functions to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource horizonFunctions() {
    final HorizonFunctions.Defaults defaults = new HorizonFunctions.Defaults();
    setHorizonDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets the default value for the number of days forward to calculate the horizon.
   * @param defaults The object containing the default values
   */
  protected void setHorizonDefaults(final HorizonFunctions.Defaults defaults) {
    defaults.setDaysForward(1);
  }

  /**
   * Sets default values for root-finding parameters used in curve construction for <b>all</b> currencies that are
   * available. These defaults apply to the older method of constructing curves (i.e. using {@link YieldCurveDefinition},
   * {@link CurveSpecificationBuilderConfiguration} and {@link MultiCurveCalculationConfig}).
   * @param defaults The object containing the default values
   */
  protected void setInterestRateDefaults(final InterestRateFunctions.Defaults defaults) {
    defaults.setApplicableCurrencies(getPerCurrencyInfo().keySet());
  }

  /**
   * Adds default values for root-finding parameters in (old) curve construction to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource interestRateFunctions() {
    final InterestRateFunctions.Defaults defaults = new InterestRateFunctions.Defaults();
    setInterestRateDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Adds default values for root-finding parameters in (new) curve construction to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource curveFunctions() {
    final CurveFunctions.Defaults defaults = new CurveFunctions.Defaults();
    setCurveDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets default values for root-finding parameters used in curve construction. These defaults apply to the newer
   * method of constructing curves (i.e. using {@link CurveDefinition}, {@link CurveNodeIdMapper} and 
   * {@link CurveConstructionConfiguration}.
   * @param defaults The object containing the default values
   */
  protected void setCurveDefaults(final CurveFunctions.Defaults defaults) {
    defaults.setAbsoluteTolerance(_absoluteTolerance);
    defaults.setRelativeTolerance(_relativeTolerance);
    defaults.setMaximumIterations(_maxIterations);
  }

  /**
   * Sets default values for root-finding parameters used in curve construction for pricing and risk functions that
   * use these curves. These defaults apply to the newer method of constructing curves (i.e. using {@link CurveDefinition}, 
   * {@link CurveNodeIdMapper} and  {@link CurveConstructionConfiguration}.
   * @param defaults The object containing the default values
   */
  protected void setMultiCurvePricingDefaults(final MultiCurvePricingFunctions.Defaults defaults) {
    defaults.setAbsoluteTolerance(_absoluteTolerance);
    defaults.setRelativeTolerance(_relativeTolerance);
    defaults.setMaximumIterations(_maxIterations);
  }

  /**
   * Sets the per-currency default values for functions that extend {@link InterestRateFutureOptionBlackFunction} with the keys<p>
   * <ul>
   * <li> Curve name = model/irfutureoption => curveName field
   * <li> Curve calculation configuration name = model/irfutureoption => curveConfiguration field
   * <li> Volatility surface name = model/irfutureoption => volatilitySurface field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  @SuppressWarnings("javadoc")
  protected void setIRFutureOptionDefaults(final CurrencyInfo i, final IRFutureOptionFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/irfutureoption"));
    defaults.setSurfaceName(i.getSurfaceName("model/irfutureoption"));
    defaults.setCurveName(i.getCurveName("model/irfutureoption"));
  }

  /**
   * Sets default values for functions that extend {@link InterestRateFutureOptionBlackFunction}.
   * @param defaults The object containing the default values
   */
  @SuppressWarnings("javadoc")
  protected void setIRFutureOptionDefaults(final IRFutureOptionFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, IRFutureOptionFunctions.Defaults.CurrencyInfo>() {
      @Override
      public IRFutureOptionFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final IRFutureOptionFunctions.Defaults.CurrencyInfo d = new IRFutureOptionFunctions.Defaults.CurrencyInfo();
        setIRFutureOptionDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for functions that extend {@link InterestRateFutureOptionBlackFunction} to the configuration source.
   * @return The configuration source populated with default values
   */
  @SuppressWarnings("javadoc")
  protected FunctionConfigurationSource irFutureOptionFunctions() {
    final IRFutureOptionFunctions.Defaults defaults = new IRFutureOptionFunctions.Defaults();
    setIRFutureOptionDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets the per-currency default values for functions that construct local volatility surface with the keys<p>
   * <ul>
   * <li> Discounting curve name = model/volatility/local/discounting => discountingCurveName field
   * <li> Curve configuration name = model/volatility/local => curveConfiguration field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setLocalVolatilityDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/volatility/local"));
    defaults.setDiscountingCurve(i.getCurveName("model/volatility/local/discounting"));
  }

  /**
   * Sets default values for functions that construct local volatility surfaces.
   * @param defaults The object containing the default values
   */
  protected void setLocalVolatilityDefaults(final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setLocalVolatilityDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for functions that construct local volatility surfaces to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource localVolatilityFunctions() {
    final com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions defaults =
        new com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions();
    setLocalVolatilityDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Adds default values for functions that calculate prices and risk using the new curve configurations to
   * the configuration source
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource multicurvePricingFunctions() {
    final MultiCurvePricingFunctions.Defaults defaults = new MultiCurvePricingFunctions.Defaults();
    setMultiCurvePricingDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Adds default values for the mark-to-market and cost-of-carry fields used in P&L calculations.
   * @param calculators The functions
   */
  protected void setPNLFunctionCalculators(final PNLFunctions.Calculators calculators) {
    calculators.setCostOfCarryField(getCostOfCarryField());
    calculators.setMark2MarketField(getMark2MarketField());
  }

  /**
   * Sets per-currency default values for P&L calculations with the keys<p>
   * <ul>
   * <li> Curve configuration = model/pnl => curveConfiguration field
   * <li> Discounting curve name = model/pnl/discounting => discountingCurveName field
   * </ul>
   * @param i The per-currency defaults
   * @param defaults The object containing the default values
   */
  protected void setPNLFunctionDefaults(final CurrencyInfo i, final PNLFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/pnl"));
    defaults.setDiscountingCurve(i.getCurveName("model/pnl/discounting"));
  }

  /**
   * Sets per-currency pair default values for P&L calculations for FX options with the keys<p>
   * <ul>
   * <li> Volatility surface name = model/pnl => surfaceName field
   * </ul>
   * @param i The per-currency pair defaults
   * @param defaults The object containing the default values
   */
  protected void setPNLFunctionDefaults(final CurrencyPairInfo i, final PNLFunctions.Defaults.CurrencyPairInfo defaults) {
    defaults.setSurfaceName(i.getSurfaceName("model/pnl"));
  }

  /**
   * Sets per-currency and per-currency pair defaults for functions that calculate P&L.
   * @param defaults The object containing the default values
   */
  protected void setPNLFunctionDefaults(final PNLFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, PNLFunctions.Defaults.CurrencyInfo>() {
      @Override
      public PNLFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final PNLFunctions.Defaults.CurrencyInfo d = new PNLFunctions.Defaults.CurrencyInfo();
        setPNLFunctionDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function1<CurrencyPairInfo, PNLFunctions.Defaults.CurrencyPairInfo>() {
      @Override
      public PNLFunctions.Defaults.CurrencyPairInfo execute(final CurrencyPairInfo i) {
        final PNLFunctions.Defaults.CurrencyPairInfo d = new PNLFunctions.Defaults.CurrencyPairInfo();
        setPNLFunctionDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds defaults for P&L calculations to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource pnlFunctions() {
    final PNLFunctions.Calculators calculators = new PNLFunctions.Calculators();
    setPNLFunctionCalculators(calculators);
    final PNLFunctions.Defaults defaults = new PNLFunctions.Defaults();
    setPNLFunctionDefaults(defaults);
    return CombiningFunctionConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  /**
   * Sets the default value for the historical time series resolution key for functions that calculate portfolio-level risk measures.
   * @param calculators The calculators
   */
  protected void setPortfolioTheoryCalculators(final PortfolioTheoryFunctions.Calculators calculators) {
    return;
  }

  /**
   * Sets default values for functions that calculate portfolio-level risk measures. The values are for<p>:
   * <ul>
   * <li> Return calculation e.g. net or gross
   * <li> Sampling period e.g. 2y
   * <li> Sampling function e.g. pad with previous value
   * <li> Standard deviation calculator name e.g. sample or population
   * <li> Covariance calculator name e.g. sample or population
   * <li> Variance calculator name e.g. sample or population
   * <li> Excess return calculation name e.g. mean
   * </ul>
   * @param defaults The object containing the default values
   */
  protected void setPortfolioTheoryDefaults(final PortfolioTheoryFunctions.Defaults defaults) {
    return;
  }

  /**
   * Adds default values for portfolio-level risk measures to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource portfolioTheoryFunctions() {
    final PortfolioTheoryFunctions.Calculators calculators = new PortfolioTheoryFunctions.Calculators();
    setPortfolioTheoryCalculators(calculators);
    final PortfolioTheoryFunctions.Defaults defaults = new PortfolioTheoryFunctions.Defaults();
    setPortfolioTheoryDefaults(defaults);
    return CombiningFunctionConfigurationSource.of(getRepository(calculators), getRepository(defaults));
  }

  /**
   * Sets per-currency default values for functions that use volatility cubes fitted with the SABR model for the keys<p>
   * <ul>
   * <li> Curve construction configuration = model/sabrcube
   * <li> Cube definition name = model/sabrcube
   * <li> Cube specification name = model/sabrcube
   * <li> Surface definition name = model/sabrcube
   * <li> Surface specification name = model/sabrcube
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setSABRCubeDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/sabrcube"));
    defaults.setCubeDefinitionName(i.getCubeDefinitionName("model/sabrcube"));
    defaults.setCubeSpecificationName(i.getCubeSpecificationName("model/sabrcube"));
    defaults.setSurfaceDefinitionName(i.getSurfaceDefinitionName("model/sabrcube"));
    defaults.setSurfaceSpecificationName(i.getSurfaceSpecificationName("model/sabrcube"));
  }

  /**
   * Sets per-currency default values for functions that use volatility cubes fitted with the SABR model.
   * @param defaults The object containing the default values
   */
  protected void setSABRCubeDefaults(final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
        setSABRCubeDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for functions that use volatility cubes fitted with the SABR model to the configuration source.
   * @return The configuration source populated with the default values
   */
  protected FunctionConfigurationSource sabrCubeFunctions() {
    final com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions defaults =
        new com.opengamma.financial.analytics.model.sabrcube.defaultproperties.DefaultPropertiesFunctions();
    setSABRCubeDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets per-currency default values for functions that extend {@link SwaptionBlackFunction} for the keys<p>
   * <ul>
   * <li> Curve configuration name = model/swaption/black => curveConfiguration field
   * <li> Volatility surface name = model/swaption/black => surfaceName field
   * </ul>
   * @param i The per-currency info 
   * @param defaults The object containing the default values
   */
  @SuppressWarnings("javadoc")
  protected void setSwaptionDefaults(final CurrencyInfo i, final com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo defaults) {
    defaults.setCurveConfig(i.getCurveConfiguration("model/swaption/black"));
    defaults.setSurfaceName(i.getSurfaceName("model/swaption/black"));
  }

  /**
   * Sets per-currency default values for functions that extend {@link SwaptionBlackFunction}.
   * @param defaults The object containing the default values
   */
  @SuppressWarnings("javadoc")
  protected void setSwaptionDefaults(final com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo>() {
      @Override
      public com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo execute(final CurrencyInfo i) {
        final com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo d =
            new com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults.CurrencyInfo();
        setSwaptionDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Adds default values for functions that extend {@link SwaptionBlackFunction} to the configuration source.
   * @return The configuration source populated with default values
   */
  @SuppressWarnings("javadoc")
  protected FunctionConfigurationSource swaptionFunctions() {
    final com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults defaults = new com.opengamma.financial.analytics.model.swaption.black.BlackFunctions.Defaults();
    setSwaptionDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets default values for functions that calculate VaR. The values are for<p>:
   * <ul>
   * <li> Sampling period e.g. 2y
   * <li> Schedule e.g. daily or weekly
   * <li> Sampling function e.g. pad with previous value
   * <li> Mean calculator name
   * <li> Standard deviation calculator name e.g. sample or population
   * <li> Confidence level
   * <li> Horizon
   * </ul>
   * @param defaults The object containing the default values
   */
  protected void setVaRDefaults(final VaRFunctions.Defaults defaults) {
    return;
  }

  /**
   * Adds default values for functions that calculate VaR to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource varFunctions() {
    final VaRFunctions.Defaults defaults = new VaRFunctions.Defaults();
    setVaRDefaults(defaults);
    return getRepository(defaults);
  }

  /**
   * Sets default values for functions that fit volatility surfaces using SABR. The values are for<p>:
   * <ul>
   * <li> x interpolator name
   * <li> left x extrapolator name
   * <li> right x extrapolator name
   * <li> y interpolator name
   * <li> left y extrapolator name
   * <li> right y extrapolator name
   * <li> fix alpha
   * <li> fix beta
   * <li> fix rho
   * <li> fix nu
   * <li> alpha start value 
   * <li> beta start value
   * <li> rho start value
   * <li> nu start value
   * <li> error
   * </ul>
   * @param defaults The object containing the default values
   */
  protected void setVolatilitySurfaceDefaults(final com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.Defaults defaults) {
    return;
  }

  /**
   * Sets per-currency default values for functions that create a Black volatility surface for the keys<p>
   * <ul>
   * <li> Forward curve name = model/volatility/surface/black => curveName field
   * <li> Forward curve calculation method name = model/volatility/surface/black => forwardCurveCalculationMethod field
   * <li> Volatility surface name = model/volatility/surface/black => surfaceName field
   * </ul>
   * and optionally sets<p>
   * <ul>
   * <li> Curve calculation method = model/volatility/surface/black => curveCalculationMethod field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setVolatilitySurfaceBlackDefaults(final CurrencyInfo i,
      final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveName(i.getForwardCurveName("model/volatility/surface/black"));
    final String v = i.getForwardCurveCalculationMethod("model/volatility/surface/black");
    if (v != null) {
      defaults.setCurveCalculationMethod(v);
    }
    defaults.setSurfaceName(i.getSurfaceName("model/volatility/surface/black"));
  }

  /**
   * Sets per-currency pair default values for functions that create an FX Black volatility surface for the keys<p>
   * <ul>
   * <li> Forward curve name = model/volatility/surface/black => curveName field
   * <li> Forward curve calculation method name = model/volatility/surface/black => forwardCurveCalculationMethod field
   * <li> Volatility surface name = model/volatility/surface/black => surfaceName field
   * </ul>
   * and optionally sets<p>
   * <ul>
   * <li> Curve calculation method = model/volatility/surface/black => curveCalculationMethod field
   * </ul>
   * @param i The per-currency pair info
   * @param defaults The object containing the default values
   */
  protected void setVolatilitySurfaceBlackDefaults(final CurrencyPairInfo i,
      final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo defaults) {
    defaults.setCurveName(i.getCurveName("model/volatility/surface/black"));
    final String v = i.getCurveCalculationMethod("model/volatility/surface/black");
    if (v != null) {
      defaults.setCurveCalculationMethod(v);
    }
    defaults.setSurfaceName(i.getSurfaceName("model/volatility/surface/black"));
  }

  /**
   * Sets defaults for functions that create Black volatility surfaces.
   * @param defaults The object containing the default values
   */
  protected void setVolatilitySurfaceBlackDefaults(final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions defaults) {
    defaults
        .setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo>() {
          @Override
          public com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
            final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo d =
                new com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo();
            setVolatilitySurfaceBlackDefaults(i, d);
            return d;
          }
        }));
    defaults
        .setPerCurrencyPairInfo(getCurrencyPairInfo(new Function1<CurrencyPairInfo,
        com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo>() {
          @Override
          public com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo execute(final CurrencyPairInfo i) {
            final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo d =
                new com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo();
            setVolatilitySurfaceBlackDefaults(i, d);
            return d;
          }
        }));
  }

  /**
   * Sets default values for functions that create Black volatility surfaces. The values are for<p>:
   * <ul>
   * <li> x interpolator name
   * <li> left x extrapolator name
   * <li> right x extrapolator name
   * <li> y interpolator name
   * <li> left y extrapolator name
   * <li> right y extrapolator name
   * </ul>
   * @param defaults The object containing the default values
   */
  protected void setVolatilitySurfaceDefaults(final com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.Defaults defaults) {
    return;
  }

  /**
   * Adds default values for functions that construct Black volatility surfaces to the configuration source.
   * @return The configuration source populated with default values
   */
  protected FunctionConfigurationSource volatilitySurfaceFunctions() {
    final com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.Defaults d1 = new com.opengamma.financial.analytics.model.volatility.surface.SurfaceFunctions.Defaults();
    setVolatilitySurfaceDefaults(d1);
    final com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions d2 =
        new com.opengamma.financial.analytics.model.volatility.surface.black.defaultproperties.DefaultPropertiesFunctions();
    setVolatilitySurfaceBlackDefaults(d2);
    final com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.Defaults d3 = new com.opengamma.financial.analytics.volatility.surface.SurfaceFunctions.Defaults();
    setVolatilitySurfaceDefaults(d3);
    return CombiningFunctionConfigurationSource.of(getRepository(d1), getRepository(d2), getRepository(d3));
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), bondFunctions(), bondFutureOptionFunctions(), forexDigitalFunctions(), cdsFunctions(),
        deprecatedFunctions(), equityOptionFunctions(), externalSensitivitiesFunctions(), fixedIncomeFunctions(), forexFunctions(), forexOptionFunctions(),
        forwardCurveFunctions(), futureFunctions(), futureOptionFunctions(), horizonFunctions(), interestRateFunctions(), irFutureOptionFunctions(),
        localVolatilityFunctions(), pnlFunctions(), portfolioTheoryFunctions(), sabrCubeFunctions(), swaptionFunctions(), varFunctions(),
        volatilitySurfaceFunctions(), xCcySwapFunctions());
  }

}
