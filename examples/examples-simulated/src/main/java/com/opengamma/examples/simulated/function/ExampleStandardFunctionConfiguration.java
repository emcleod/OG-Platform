/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.function;

import static com.opengamma.engine.value.ValuePropertyNames.DIVIDEND_TYPE_NONE;

import java.util.List;

import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.engine.function.config.CombiningFunctionConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.FunctionConfigurationSource;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.model.equity.option.OptionFunctions;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXForwardPropertiesFunctions;
import com.opengamma.financial.analytics.model.forex.defaultproperties.FXOptionPropertiesFunctions;
import com.opengamma.financial.analytics.model.option.AnalyticOptionDefaultCurveFunction;
import com.opengamma.financial.analytics.model.pnl.PNLFunctions;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.currency.CurrencyMatrixConfigPopulator;
import com.opengamma.financial.currency.CurrencyMatrixLookupFunction;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.web.spring.StandardFunctionConfiguration;

/**
 * Constructs a standard function repository.
 */
@SuppressWarnings("deprecation")
public class ExampleStandardFunctionConfiguration extends StandardFunctionConfiguration {

  /**
   * Gets an instance of the example function configuration.
   * 
   * @return Gets the instance
   */
  public static FunctionConfigurationSource instance() {
    return new ExampleStandardFunctionConfiguration().getObjectCreating();
  }

  /**
   * The function configuration.
   */
  public ExampleStandardFunctionConfiguration() {
    setMark2MarketField("CLOSE");
    setCostOfCarryField("COST_OF_CARRY");
    setAbsoluteTolerance(0.0001);
    setRelativeTolerance(0.0001);
    setMaximumIterations(1000);
    setEquityOptionInfo();
  }

  @Override
  protected CurrencyInfo audCurrencyInfo() {
    final CurrencyInfo i = super.audCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultThreeCurveAUDConfig");
    i.setCurveConfiguration("model/fxforward", "AUDFX");
    i.setCurveConfiguration("mode/fxoption/black", "DefaultThreeCurveAUDConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "DEFAULT");
    i.setCurveName("model/fxoption/black", "Discounting");
    return i;
  }

  @Override
  protected CurrencyInfo chfCurrencyInfo() {
    final CurrencyInfo i = super.chfCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveCHFConfig");
    i.setCurveConfiguration("model/fxforward", "CHFFX");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveCHFConfig");
    i.setCurveConfiguration("model/fxoption/black", "DefaultTwoCurveCHFConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "DEFAULT");
    i.setCurveName("model/fxoption/black", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER2");
    return i;
  }

  @Override
  protected CurrencyInfo eurCurrencyInfo() {
    final CurrencyInfo i = super.eurCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveEURConfig");
    i.setCurveConfiguration("mode/future", "DefaultTwoCurveEURConfig");
    i.setCurveConfiguration("model/fxforward", "EURFX");
    i.setCurveConfiguration("model/fxoption/black", "DefaultTwoCurveEURConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveEURConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "DEFAULT");
    i.setCurveName("model/fxoption/black", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER2");
    return i;
  }

  @Override
  protected CurrencyInfo gbpCurrencyInfo() {
    final CurrencyInfo i = super.gbpCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveGBPConfig");
    i.setCurveConfiguration("model/fxforward", "GBPFX");
    i.setCurveConfiguration("model/fxoption/black", "DefaultTwoCurveGBPConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveGBPConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "DEFAULT");
    i.setCurveName("model/fxoption/black", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER1");
    return i;
  }

  @Override
  protected CurrencyInfo jpyCurrencyInfo() {
    final CurrencyInfo i = super.jpyCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveJPYConfig");
    i.setCurveConfiguration("model/fxforward", "JPYFX");
    i.setCurveConfiguration("model/fxforward/black", "DefaultTwoCurveJPYConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveJPYConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "DEFAULT");
    i.setCurveName("model/fxoption/black", "Discounting");
    i.setSurfaceName("model/swaption/black", "PROVIDER3");
    return i;
  }

  @Override
  protected CurrencyInfo usdCurrencyInfo() {
    final CurrencyInfo i = super.usdCurrencyInfo();
    i.setCurveConfiguration(null, "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/fxforward", "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/fxforward/black", "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/swaption/black", "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/bond/riskFree", "DefaultTwoCurveUSDConfig");
    i.setCurveConfiguration("model/bond/credit", "DefaultTwoCurveUSDConfig");
    i.setCurveName(null, "Discounting");
    i.setCurveName("model/fxforward", "Discounting");
    i.setCurveName("model/fxoption/black", "Discounting");
    i.setCurveName("model/bond/riskFree", "Discounting");
    i.setCurveName("model/bond/credit", "Discounting");
    i.setCubeDefinitionName("model/sabrcube", "USD PROVIDER1");
    i.setCubeSpecificationName("model/sabrcube", "USD PROVIDER1");
    i.setSurfaceDefinitionName("model/sabrcube", "US FWD SWAP PROVIDER1");
    i.setSurfaceSpecificationName("model/sabrcube", "US FWD SWAP PROVIDER1");
    i.setForwardCurveName(null, "Forward3M");
    i.setSurfaceName(null, "SECONDARY");
    i.setSurfaceName("model/swaption/black", "PROVIDER1");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdEurCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdEurCurrencyPairInfo();
    i.setCurveName("model/volatility/surface/black", "Discounting");
    i.setSurfaceName("model/volatility/surface/black", "DEFAULT");
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdJpyCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdChfCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdChfCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdAudCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdAudCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo usdGbpCurrencyPairInfo() {
    final CurrencyPairInfo i = super.usdGbpCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo eurGbpCurrencyPairInfo() {
    final CurrencyPairInfo i = super.eurGbpCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  @Override
  protected CurrencyPairInfo chfJpyCurrencyPairInfo() {
    final CurrencyPairInfo i = super.chfJpyCurrencyPairInfo();
    i.setSurfaceName("model/fxoption/black", "DEFAULT");
    i.setForwardCurveName("model/fxforward", "DEFAULT");
    return i;
  }

  /**
   * Sets defaults for AAPL equity options.
   */
  protected void setEquityOptionInfo() {
    setEquityOptionInfo("AAPL", "USD");
  }

  /**
   * Creates empty default per-equity information objects for equity options.
   * @param ticker The equity ticker
   * @param curveCurrency The currency target of the discounting curve (usually, but not necessarily,
   * the currency of the equity).
   */
  protected void setEquityOptionInfo(final String ticker, final String curveCurrency) {
    final EquityInfo i = defaultEquityInfo(ticker);
    final String discountingCurveConfigName = "DefaultTwoCurve" + curveCurrency + "Config";
    i.setDiscountingCurve("model/equityoption", "Discounting");
    i.setDiscountingCurveConfig("model/equityoption", discountingCurveConfigName);
    i.setDiscountingCurveCurrency("model/equityoption", curveCurrency);
    i.setDividendType("model/equityoption", DIVIDEND_TYPE_NONE);
    i.setForwardCurve("model/equityoption", "Discounting");
    i.setForwardCurveCalculationMethod("model/equityoption", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD);
    i.setForwardCurveInterpolator("model/equityoption", Interpolator1DFactory.DOUBLE_QUADRATIC);
    i.setForwardCurveLeftExtrapolator("model/equityoption", Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    i.setForwardCurveRightExtrapolator("model/equityoption", Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
    i.setSurfaceCalculationMethod("model/equityoption", BlackVolatilitySurfacePropertyNamesAndValues.INTERPOLATED_BLACK_LOGNORMAL);
    i.setSurfaceInterpolationMethod("model/equityoption", BlackVolatilitySurfacePropertyNamesAndValues.SPLINE);
    i.setVolatilitySurface("model/equityoption", "DEFAULT");
    setEquityInfo(ticker, i);
  }

  @Override
  protected void addCurrencyConversionFunctions(final List<FunctionConfiguration> functionConfigs) {
    super.addCurrencyConversionFunctions(functionConfigs);
    functionConfigs.add(functionConfiguration(CurrencyMatrixLookupFunction.class, CurrencyMatrixConfigPopulator.SYNTHETIC_LIVE_DATA));
  }

  /**
   * Overridden to allow separate curve default curve names (per currency) to be 
   * set for FX options and FX forwards. The parent class sets the same curve names
   * for all FX instruments.
   * {@inheritDoc}
   */
  @Override
  protected FunctionConfigurationSource forexFunctions() {
    final FXForwardPropertiesFunctions fxForwardDefaults = new FXForwardPropertiesFunctions();
    setForexForwardDefaults(fxForwardDefaults);
    final FunctionConfigurationSource fxForwardRepository = getRepository(fxForwardDefaults);
    final FXOptionPropertiesFunctions fxOptionDefaults = new FXOptionPropertiesFunctions();
    setForexOptionDefaults(fxOptionDefaults);
    final FunctionConfigurationSource fxOptionRepository = getRepository(fxOptionDefaults);
    return CombiningFunctionConfigurationSource.of(fxForwardRepository, fxOptionRepository);
  }

  /**
   * Sets per-currency and per-currency pair default values for FX options.
   * @param defaults The object containing the default values
   */
  protected void setForexOptionDefaults(final FXOptionPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, FXOptionPropertiesFunctions.CurrencyInfo>() {
      @Override
      public FXOptionPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
        final FXOptionPropertiesFunctions.CurrencyInfo d = new FXOptionPropertiesFunctions.CurrencyInfo();
        setForexOptionDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function1<CurrencyPairInfo, FXOptionPropertiesFunctions.CurrencyPairInfo>() {
      @Override
      public FXOptionPropertiesFunctions.CurrencyPairInfo execute(final CurrencyPairInfo i) {
        final FXOptionPropertiesFunctions.CurrencyPairInfo d = new FXOptionPropertiesFunctions.CurrencyPairInfo();
        setForexOptionDefaults(i, d);
        return d;
      }
    }));

  }

  /**
   * Sets per-currency default values for FX forwards.
   * @param defaults The object containing the default values
   */
  protected void setForexForwardDefaults(final FXForwardPropertiesFunctions defaults) {
    defaults.setPerCurrencyInfo(getCurrencyInfo(new Function1<CurrencyInfo, FXForwardPropertiesFunctions.CurrencyInfo>() {
      @Override
      public FXForwardPropertiesFunctions.CurrencyInfo execute(final CurrencyInfo i) {
        final FXForwardPropertiesFunctions.CurrencyInfo d = new FXForwardPropertiesFunctions.CurrencyInfo();
        setForexForwardDefaults(i, d);
        return d;
      }
    }));
    defaults.setPerCurrencyPairInfo(getCurrencyPairInfo(new Function1<CurrencyPairInfo, FXForwardPropertiesFunctions.CurrencyPairInfo>() {
      @Override
      public FXForwardPropertiesFunctions.CurrencyPairInfo execute(final CurrencyPairInfo i) {
        final FXForwardPropertiesFunctions.CurrencyPairInfo d = new FXForwardPropertiesFunctions.CurrencyPairInfo();
        setForexForwardDefaults(i, d);
        return d;
      }
    }));

  }

  /**
   * Sets the paths for the per-currency default values for functions that price FX forwards with the keys<p>
   * <ul>
   * <li> Curve configuration name = model/fxforward => curveConfiguration field
   * <li> Discounting curve name = model/fxforward => discountingCurve field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setForexForwardDefaults(final CurrencyInfo i, final FXForwardPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/fxforward"));
    defaults.setDiscountingCurve(i.getCurveName("model/fxforward"));
  }

  /**
   * Sets the path for the per-currency pair default values for functions that price FX forwards with the keys<p>
   * <ul>
   * <li> Forward curve name = model/fxforward => forwardCurve field
   * </ul>
   * @param i The per-currency pair info
   * @param defaults The object containing the default values
   */
  protected void setForexForwardDefaults(final CurrencyPairInfo i, final FXForwardPropertiesFunctions.CurrencyPairInfo defaults) {
    defaults.setForwardCurveName(i.getForwardCurveName("model/fxforward"));
  }

  /**
   * Sets the paths for the per-currency default values for functions that price FX options using
   * the Black model with the keys<p>
   * <ul>
   * <li> Curve configuration name = model/fxoption/black => curveConfiguration field
   * <li> Discounting curve name = model/fxoption/black => discountingCurve field
   * </ul>
   * @param i The per-currency info
   * @param defaults The object containing the default values
   */
  protected void setForexOptionDefaults(final CurrencyInfo i, final FXOptionPropertiesFunctions.CurrencyInfo defaults) {
    defaults.setCurveConfiguration(i.getCurveConfiguration("model/fxoption/black"));
    defaults.setDiscountingCurve(i.getCurveName("model/fxoption/black"));
  }

  /**
   * Sets the paths for the per-currency pair default values for functions that price FX options using
   * the Black model with the keys<p>
   * <ul>
   * <li> Volatility surface name = model/fxoption/black => surfaceName field
   * </ul>
   * @param i The per-currency pair info
   * @param defaults The object containing the default values
   */
  protected void setForexOptionDefaults(final CurrencyPairInfo i, final FXOptionPropertiesFunctions.CurrencyPairInfo defaults) {
    defaults.setSurfaceName(i.getSurfaceName("model/fxoption/black"));
  }

  /**
   * Overridden to allow separate curve default curve names (per currency) to be 
   * set for FX options and FX forwards. The parent class sets the same curve names
   * for all FX instruments.
   * {@inheritDoc}
   */
  @Override
  protected FunctionConfigurationSource equityOptionFunctions() {
    super.equityOptionFunctions();
    final OptionFunctions.EquityForwardDefaults forwardCurveDefaults = new OptionFunctions.EquityForwardDefaults();
    setEquityOptionForwardCurveDefaults(forwardCurveDefaults);
    final OptionFunctions.EquityOptionDefaults surfaceDefaults = new OptionFunctions.EquityOptionDefaults();
    setEquityOptionSurfaceDefaults(surfaceDefaults);
    final FunctionConfigurationSource forwardCurveRepository = getRepository(forwardCurveDefaults);
    final FunctionConfigurationSource surfaceRepository = getRepository(surfaceDefaults);
    return CombiningFunctionConfigurationSource.of(forwardCurveRepository, surfaceRepository);
  }

  /**
   * Sets the per-equity forward curve defaults for equity option functions.
   * @param defaults The object containing the default values
   */
  protected void setEquityOptionForwardCurveDefaults(final OptionFunctions.EquityForwardDefaults defaults) {
    defaults.setPerEquityInfo(getEquityInfo(new Function1<EquityInfo, OptionFunctions.EquityInfo>() {
      @Override
      public OptionFunctions.EquityInfo execute(final EquityInfo i) {
        final OptionFunctions.EquityInfo d = new OptionFunctions.EquityInfo();
        setEquityOptionForwardCurveDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Sets the paths for the per-equity ticker default values for the forward curve used
   * in pricing with the keys<p>
   * <ul>
   * <li> Forward curve interpolator = model/equityoption
   * <li> Forward curve left extrapolator = model/equityoption
   * <li> Forward curve right extrapolator = model/equityoption
   * <li> Forward curve = model/equityoption
   * <li> Forward curve calculation method = model/equityoption
   * <li> Discounting curve = model/equityoption
   * <li> Discounting curve configuration = model/equityoption
   * <li> Discounting curve currency = model/equityoption
   * <li> Dividend type = model/equityoption
   * </ul>
   * @param i The per-equity info
   * @param defaults The object containing the default values
   */
  protected void setEquityOptionForwardCurveDefaults(final EquityInfo i, final OptionFunctions.EquityInfo defaults) {
    defaults.setForwardCurveInterpolator(i.getForwardCurveInterpolator("model/equityoption"));
    defaults.setForwardCurveLeftExtrapolator(i.getForwardCurveLeftExtrapolator("model/equityoption"));
    defaults.setForwardCurveRightExtrapolator(i.getForwardCurveRightExtrapolator("model/equityoption"));
    defaults.setForwardCurve(i.getForwardCurve("model/equityoption"));
    defaults.setForwardCurveCalculationMethod(i.getForwardCurveCalculationMethod("model/equityoption"));
    defaults.setDiscountingCurve(i.getDiscountingCurve("model/equityoption"));
    defaults.setDiscountingCurveConfig(i.getDiscountingCurveConfig("model/equityoption"));
    defaults.setDiscountingCurveCurrency(i.getDiscountingCurveCurrency("model/equityoption"));
    defaults.setDividendType(i.getDiscountingCurve("model/equityoption"));
  }

  /**
   * Sets the per-equity surface defaults for equity option functions.
   * @param defaults The object containing the default values
   */
  protected void setEquityOptionSurfaceDefaults(final OptionFunctions.EquityOptionDefaults defaults) {
    defaults.setPerEquityInfo(getEquityInfo(new Function1<EquityInfo, OptionFunctions.EquityInfo>() {
      @Override
      public OptionFunctions.EquityInfo execute(final EquityInfo i) {
        final OptionFunctions.EquityInfo d = new OptionFunctions.EquityInfo();
        setEquityOptionSurfaceDefaults(i, d);
        return d;
      }
    }));
  }

  /**
   * Sets the paths for the per-equity ticker default values for the surface used
   * in pricing with the keys<p>
   * <ul>
   * <li> Surface calculation method = model/equityoption
   * <li> Discounting curve name = model/equityoption
   * <li> Discounting curve calculation config = model/equityoption
   * <li> Volatility surface name = model/equityoption
   * <li> Surface interpolation method = model/equityoption
   * <li> Forward curve name = model/equityoption
   * <li> Forward curve calculation method = model/equityoption
   * </ul>
   * @param i The per-equity info
   * @param defaults The object containing the default values
   */
  protected void setEquityOptionSurfaceDefaults(final EquityInfo i, final OptionFunctions.EquityInfo defaults) {
    defaults.setSurfaceCalculationMethod(i.getSurfaceCalculationMethod("model/equityoption"));
    defaults.setDiscountingCurve(i.getDiscountingCurve("model/equityoption"));
    defaults.setDiscountingCurveConfig(i.getDiscountingCurveConfig("model/equityoption"));
    defaults.setVolatilitySurface(i.getVolatilitySurface("model/equityoption"));
    defaults.setSurfaceInterpolationMethod(i.getSurfaceInterpolationMethod("model/equityoption"));
    defaults.setForwardCurve(i.getForwardCurve("model/equityoption"));
    defaults.setForwardCurveCalculationMethod(i.getForwardCurveCalculationMethod("model/equityoption"));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    super.addAllConfigurations(functions);
    functions.add(functionConfiguration(AnalyticOptionDefaultCurveFunction.class, "SECONDARY"));
  }

  @Override
  protected void setPNLFunctionDefaults(final PNLFunctions.Defaults defaults) {
    super.setPNLFunctionDefaults(defaults);
    defaults.setCurveName("SECONDARY");
    defaults.setPayCurveName("SECONDARY");
    defaults.setReceiveCurveName("SECONDARY");
  }

  @Override
  protected FunctionConfigurationSource createObject() {
    return CombiningFunctionConfigurationSource.of(super.createObject(), curveFunctions(), multicurvePricingFunctions());
  }
}
