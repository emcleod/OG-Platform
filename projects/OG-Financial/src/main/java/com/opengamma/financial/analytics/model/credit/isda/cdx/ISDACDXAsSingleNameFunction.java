/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit.isda.cdx;

import static com.opengamma.engine.value.ValuePropertyNames.CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.CDX_AS_SINGLE_NAME_ISDA;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE_SHIFT_TYPE;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalyticFactory;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantCreditCurve;
import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantYieldCurve;
import com.opengamma.analytics.math.curve.NodalObjectsCurve;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.conversion.CreditDefaultIndexSwapSecurityToProxyConverter;
import com.opengamma.financial.analytics.model.YieldCurveFunctionUtils;
import com.opengamma.financial.analytics.model.credit.CreditFunctionUtils;
import com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.analytics.model.credit.IMMDateGenerator;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
//TODO rename to make clear that these functions use the ISDA methodology (specifically, for effective dates)
public abstract class ISDACDXAsSingleNameFunction extends AbstractFunction.NonCompiledInvoker {
  private static final BusinessDayConvention FOLLOWING = BusinessDayConventions.FOLLOWING;
  private final String[] _valueRequirements;

  public ISDACDXAsSingleNameFunction(final String... valueRequirements) {
    ArgumentChecker.notNull(valueRequirements, "value requirements");
    _valueRequirements = valueRequirements;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final HolidaySource holidaySource = OpenGammaExecutionContext.getHolidaySource(executionContext);
    final SecuritySource securitySource = OpenGammaExecutionContext.getSecuritySource(executionContext);
    final RegionSource regionSource = OpenGammaExecutionContext.getRegionSource(executionContext);
    final LegalEntitySource legalEntitySource = OpenGammaExecutionContext.getLegalEntitySource(executionContext);
    final ZonedDateTime valuationTime = ZonedDateTime.now(executionContext.getValuationClock());
    final CreditDefaultIndexSwapSecurityToProxyConverter converter = new CreditDefaultIndexSwapSecurityToProxyConverter(holidaySource, regionSource, legalEntitySource, securitySource, valuationTime);
    final CreditDefaultSwapIndexSecurity security = (CreditDefaultSwapIndexSecurity) target.getSecurity();
    final CreditDefaultSwapIndexDefinitionSecurity underlyingDefinition = (CreditDefaultSwapIndexDefinitionSecurity) securitySource.getSingle(ExternalIdBundle.of(security.getReferenceEntity()));
    if (underlyingDefinition == null) {
      throw new OpenGammaRuntimeException("Could not get underlying index definition");
    }
    final double recoveryRate = underlyingDefinition.getRecoveryRate();
    final Calendar calendar = new HolidaySourceCalendarAdapter(holidaySource, FinancialSecurityUtils.getCurrency(security));
    LegacyVanillaCreditDefaultSwapDefinition definition = (LegacyVanillaCreditDefaultSwapDefinition) security.accept(converter);
    //definition = definition.withEffectiveDate(FOLLOWING.adjustDate(calendar, valuationTime.withHour(0).withMinute(0).withSecond(0).withNano(0).plusDays(1)));
    //definition = definition.withRecoveryRate(recoveryRate);
    final Object yieldCurveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (yieldCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve");
    }
    final Object spreadCurveObject = inputs.getValue(ValueRequirementNames.CREDIT_SPREAD_CURVE);
    if (spreadCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get credit spread curve");
    }
    final ISDACompliantYieldCurve yieldCurve = (ISDACompliantYieldCurve) yieldCurveObject;
    final NodalObjectsCurve<?, ?> spreadCurve = (NodalObjectsCurve<?, ?>) spreadCurveObject;
    final Tenor[] tenors = CreditFunctionUtils.getTenors(spreadCurve.getXData());
    final Double[] marketSpreadObjects = CreditFunctionUtils.getSpreads(spreadCurve.getYData());
    ParallelArrayBinarySort.parallelBinarySort(tenors, marketSpreadObjects);
    final int n = tenors.length;
    final ZonedDateTime[] times = new ZonedDateTime[n];
    final double[] marketSpreads = new double[n];
    for (int i = 0; i < n; i++) {
      times[i] = IMMDateGenerator.getNextIMMDate(valuationTime, tenors[i]).withHour(0).withMinute(0).withSecond(0).withNano(0);
      marketSpreads[i] = marketSpreadObjects[i] * 1e-4;
    }
    ISDACompliantCreditCurve hazardCurve = (ISDACompliantCreditCurve) inputs.getValue(ValueRequirementNames.HAZARD_RATE_CURVE);
    final CDSAnalyticFactory analyticFactory = new CDSAnalyticFactory(recoveryRate, definition.getCouponFrequency().getPeriod())
        .with(definition.getBusinessDayAdjustmentConvention())
        .with(definition.getCalendar()).with(definition.getStubType())
        .withAccrualDCC(definition.getDayCountFractionConvention());
    final CDSAnalytic pricingCDS = analyticFactory.makeCDS(definition.getEffectiveDate().toLocalDate(), definition.getStartDate().toLocalDate(), definition.getMaturityDate().toLocalDate());
    final ValueProperties properties = desiredValues.iterator().next().getConstraints().copy()
        .with(ValuePropertyNames.FUNCTION, getUniqueId())
        .get();
    return getComputedValue(definition, yieldCurve, times, marketSpreads, valuationTime, target, properties, inputs, hazardCurve, pricingCDS, tenors);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.CREDIT_DEFAULT_SWAP_INDEX_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = ValueProperties.all();
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirements) {
      results.add(new ValueSpecification(valueRequirement, target.toSpecification(), properties));
    }
    return results;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    //TODO add check for calculation method = ISDA
    final Set<String> yieldCurveNames = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE);
    if (yieldCurveNames == null || yieldCurveNames.size() != 1) {
      return null;
    }
    final Set<String> yieldCurveCalculationConfigNames = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG);
    if (yieldCurveCalculationConfigNames == null || yieldCurveCalculationConfigNames.size() != 1) {
      return null;
    }
    final Set<String> yieldCurveCalculationMethodNames = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD);
    if (yieldCurveCalculationMethodNames == null || yieldCurveCalculationMethodNames.size() != 1) {
      return null;
    }
    final CreditSecurityToIdentifierVisitor identifierVisitor = new CreditSecurityToIdentifierVisitor(OpenGammaCompilationContext.getSecuritySource(context));
    final CreditDefaultSwapIndexSecurity security = (CreditDefaultSwapIndexSecurity) target.getSecurity();
    final String spreadCurveName = "CDS_INDEX_" + security.accept(identifierVisitor).getUniqueId().getValue();
    //    final Set<String> spreadCurveNames = constraints.getValues(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE);
    //    if (spreadCurveNames == null || spreadCurveNames.size() != 1) {
    //      return null;
    //    }
    final ComputationTargetSpecification currencyTarget = ComputationTargetSpecification.of(FinancialSecurityUtils.getCurrency(target.getSecurity()));
    final String yieldCurveName = Iterables.getOnlyElement(yieldCurveNames);
    final String yieldCurveCalculationConfigName = Iterables.getOnlyElement(yieldCurveCalculationConfigNames);
    final String yieldCurveCalculationMethodName = Iterables.getOnlyElement(yieldCurveCalculationMethodNames);
    //    final String spreadCurveName = Iterables.getOnlyElement(spreadCurveNames);
    final ValueRequirement yieldCurveRequirement = YieldCurveFunctionUtils.getCurveRequirement(currencyTarget, yieldCurveName, yieldCurveCalculationConfigName,
        yieldCurveCalculationMethodName);
    final Set<String> creditSpreadCurveShifts = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT);
    final ValueProperties.Builder spreadCurveProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, spreadCurveName);
    if (creditSpreadCurveShifts != null) {
      final Set<String> creditSpreadCurveShiftTypes = constraints.getValues(PROPERTY_SPREAD_CURVE_SHIFT_TYPE);
      if (creditSpreadCurveShiftTypes == null || creditSpreadCurveShiftTypes.size() != 1) {
        return null;
      }
      spreadCurveProperties.with(PROPERTY_SPREAD_CURVE_SHIFT, creditSpreadCurveShifts).with(PROPERTY_SPREAD_CURVE_SHIFT_TYPE, creditSpreadCurveShiftTypes);
    }
    final ValueRequirement creditSpreadCurveRequirement = new ValueRequirement(ValueRequirementNames.CREDIT_SPREAD_CURVE, ComputationTargetSpecification.NULL, spreadCurveProperties.get());
    return Sets.newHashSet(yieldCurveRequirement, creditSpreadCurveRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder propertiesBuilder = getCommonResultProperties();
    propertiesBuilder.with(CALCULATION_METHOD, CDX_AS_SINGLE_NAME_ISDA);
    for (final Map.Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      final ValueSpecification spec = entry.getKey();
      final ValueProperties.Builder inputPropertiesBuilder = spec.getProperties().copy();
      inputPropertiesBuilder.withoutAny(ValuePropertyNames.FUNCTION);
      if (spec.getValueName().equals(ValueRequirementNames.YIELD_CURVE)) {
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE, inputPropertiesBuilder.get().getValues(ValuePropertyNames.CURVE));
        inputPropertiesBuilder.withoutAny(ValuePropertyNames.CURVE);
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_CONFIG, inputPropertiesBuilder.get().getValues(ValuePropertyNames.CURVE_CALCULATION_CONFIG));
        inputPropertiesBuilder.withoutAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_YIELD_CURVE_CALCULATION_METHOD, inputPropertiesBuilder.get().getValues(ValuePropertyNames.CURVE_CALCULATION_METHOD));
        inputPropertiesBuilder.withoutAny(ValuePropertyNames.CURVE_CALCULATION_METHOD);
      } else if (spec.getValueName().equals(ValueRequirementNames.CREDIT_SPREAD_CURVE)) {
        propertiesBuilder.with(CreditInstrumentPropertyNamesAndValues.PROPERTY_SPREAD_CURVE, inputPropertiesBuilder.get().getValues(ValuePropertyNames.CURVE));
        inputPropertiesBuilder.withoutAny(ValuePropertyNames.CURVE);
      }
      if (!inputPropertiesBuilder.get().isEmpty()) {
        for (final String propertyName : inputPropertiesBuilder.get().getProperties()) {
          propertiesBuilder.with(propertyName, inputPropertiesBuilder.get().getValues(propertyName));
        }
      }
    }
    if (labelResultWithCurrency()) {
      propertiesBuilder.with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
    }
    final ValueProperties properties = propertiesBuilder.get();
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Set<ValueSpecification> results = new HashSet<>();
    for (final String valueRequirement : _valueRequirements) {
      results.add(new ValueSpecification(valueRequirement, targetSpec, properties));
    }
    return results;
  }

  protected abstract Set<ComputedValue> getComputedValue(CreditDefaultSwapDefinition definition,
                                                         ISDACompliantYieldCurve yieldCurve,
                                                         ZonedDateTime[] times,
                                                         double[] marketSpreads,
                                                         ZonedDateTime valuationTime,
                                                         ComputationTarget target,
                                                         ValueProperties properties,
                                                         FunctionInputs inputs,
                                                         ISDACompliantCreditCurve hazardCurve,
                                                         CDSAnalytic analytic,
                                                         Tenor[] tenors);

  protected abstract ValueProperties.Builder getCommonResultProperties();

  protected abstract boolean labelResultWithCurrency();

}
