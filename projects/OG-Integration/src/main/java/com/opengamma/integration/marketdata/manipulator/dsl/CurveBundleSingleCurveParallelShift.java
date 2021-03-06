/**
 * 
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CONSTRUCTION_CONFIG;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

@BeanDefinition
public final class CurveBundleSingleCurveParallelShift implements StructureManipulator<ParameterProviderInterface>, ImmutableBean {
  private static final Logger s_logger = LoggerFactory.getLogger(CurveBundleSingleCurveParallelShift.class);

  @PropertyDefinition(validate = "notNull")
  private final YieldCurveParallelShift _yieldCurveManipulator;

  @PropertyDefinition(validate = "notNull")
  private final String _curveName;

  /* package */CurveBundleSingleCurveParallelShift(final ScenarioShiftType shiftType, final double shift, final String curveName) {
    ArgumentChecker.notNull(curveName, "curveName");
    _yieldCurveManipulator = new YieldCurveParallelShift(shiftType, shift);
    _curveName = curveName;
  }

  @Override
  public ParameterProviderInterface execute(final ParameterProviderInterface structure, final ValueSpecification valueSpecification, final FunctionExecutionContext executionContext) {
    s_logger.error("Shifting curve \"{}\" in curve bundle \"{}\" using {}", _curveName, valueSpecification.getProperties().getStrictValue(CURVE_CONSTRUCTION_CONFIG),
        _yieldCurveManipulator);
    if (structure instanceof MulticurveProviderDiscount) {
      final MulticurveProviderDiscount mcpd = ((MulticurveProviderDiscount) structure).copy();
      final YieldAndDiscountCurve curve = mcpd.getCurve(_curveName);
      if (curve instanceof YieldCurve) {
        final YieldCurve yieldCurve = (YieldCurve) curve;
        final YieldCurve shiftedCurve = _yieldCurveManipulator.execute(yieldCurve, valueSpecification, executionContext);
        // find out which map(s) named curve was in
        final Currency currency = mcpd.getCurrencyForName(_curveName);
        final IborIndex iborIndex = mcpd.getIborIndexForName(_curveName);
        final IndexON overnightIndex = mcpd.getOvernightIndexForName(_curveName);
        if (currency != null) {
          mcpd.replaceCurve(currency, shiftedCurve);
        }
        if (iborIndex != null) {
          mcpd.replaceCurve(iborIndex, shiftedCurve);
        }
        if (overnightIndex != null) {
          mcpd.replaceCurve(overnightIndex, shiftedCurve);
        }
        return mcpd;
      }
    }
    throw new IllegalStateException();
  }

  @Override
  public Class<ParameterProviderInterface> getExpectedType() {
    return ParameterProviderInterface.class;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CurveBundleSingleCurveParallelShift}.
   * @return the meta-bean, not null
   */
  public static CurveBundleSingleCurveParallelShift.Meta meta() {
    return CurveBundleSingleCurveParallelShift.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CurveBundleSingleCurveParallelShift.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CurveBundleSingleCurveParallelShift.Builder builder() {
    return new CurveBundleSingleCurveParallelShift.Builder();
  }

  private CurveBundleSingleCurveParallelShift(
      YieldCurveParallelShift yieldCurveManipulator,
      String curveName) {
    JodaBeanUtils.notNull(yieldCurveManipulator, "yieldCurveManipulator");
    JodaBeanUtils.notNull(curveName, "curveName");
    this._yieldCurveManipulator = yieldCurveManipulator;
    this._curveName = curveName;
  }

  @Override
  public CurveBundleSingleCurveParallelShift.Meta metaBean() {
    return CurveBundleSingleCurveParallelShift.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the yieldCurveManipulator.
   * @return the value of the property, not null
   */
  public YieldCurveParallelShift getYieldCurveManipulator() {
    return _yieldCurveManipulator;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the curveName.
   * @return the value of the property, not null
   */
  public String getCurveName() {
    return _curveName;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public CurveBundleSingleCurveParallelShift clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CurveBundleSingleCurveParallelShift other = (CurveBundleSingleCurveParallelShift) obj;
      return JodaBeanUtils.equal(getYieldCurveManipulator(), other.getYieldCurveManipulator()) &&
          JodaBeanUtils.equal(getCurveName(), other.getCurveName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getYieldCurveManipulator());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurveName());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("CurveBundleSingleCurveParallelShift{");
    buf.append("yieldCurveManipulator").append('=').append(getYieldCurveManipulator()).append(',').append(' ');
    buf.append("curveName").append('=').append(JodaBeanUtils.toString(getCurveName()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CurveBundleSingleCurveParallelShift}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code yieldCurveManipulator} property.
     */
    private final MetaProperty<YieldCurveParallelShift> _yieldCurveManipulator = DirectMetaProperty.ofImmutable(
        this, "yieldCurveManipulator", CurveBundleSingleCurveParallelShift.class, YieldCurveParallelShift.class);
    /**
     * The meta-property for the {@code curveName} property.
     */
    private final MetaProperty<String> _curveName = DirectMetaProperty.ofImmutable(
        this, "curveName", CurveBundleSingleCurveParallelShift.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "yieldCurveManipulator",
        "curveName");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -832522004:  // yieldCurveManipulator
          return _yieldCurveManipulator;
        case 771153946:  // curveName
          return _curveName;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CurveBundleSingleCurveParallelShift.Builder builder() {
      return new CurveBundleSingleCurveParallelShift.Builder();
    }

    @Override
    public Class<? extends CurveBundleSingleCurveParallelShift> beanType() {
      return CurveBundleSingleCurveParallelShift.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code yieldCurveManipulator} property.
     * @return the meta-property, not null
     */
    public MetaProperty<YieldCurveParallelShift> yieldCurveManipulator() {
      return _yieldCurveManipulator;
    }

    /**
     * The meta-property for the {@code curveName} property.
     * @return the meta-property, not null
     */
    public MetaProperty<String> curveName() {
      return _curveName;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -832522004:  // yieldCurveManipulator
          return ((CurveBundleSingleCurveParallelShift) bean).getYieldCurveManipulator();
        case 771153946:  // curveName
          return ((CurveBundleSingleCurveParallelShift) bean).getCurveName();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code CurveBundleSingleCurveParallelShift}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<CurveBundleSingleCurveParallelShift> {

    private YieldCurveParallelShift _yieldCurveManipulator;
    private String _curveName;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(CurveBundleSingleCurveParallelShift beanToCopy) {
      this._yieldCurveManipulator = beanToCopy.getYieldCurveManipulator();
      this._curveName = beanToCopy.getCurveName();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -832522004:  // yieldCurveManipulator
          return _yieldCurveManipulator;
        case 771153946:  // curveName
          return _curveName;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -832522004:  // yieldCurveManipulator
          this._yieldCurveManipulator = (YieldCurveParallelShift) newValue;
          break;
        case 771153946:  // curveName
          this._curveName = (String) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public CurveBundleSingleCurveParallelShift build() {
      return new CurveBundleSingleCurveParallelShift(
          _yieldCurveManipulator,
          _curveName);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code yieldCurveManipulator} property in the builder.
     * @param yieldCurveManipulator  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder yieldCurveManipulator(YieldCurveParallelShift yieldCurveManipulator) {
      JodaBeanUtils.notNull(yieldCurveManipulator, "yieldCurveManipulator");
      this._yieldCurveManipulator = yieldCurveManipulator;
      return this;
    }

    /**
     * Sets the {@code curveName} property in the builder.
     * @param curveName  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder curveName(String curveName) {
      JodaBeanUtils.notNull(curveName, "curveName");
      this._curveName = curveName;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("CurveBundleSingleCurveParallelShift.Builder{");
      buf.append("yieldCurveManipulator").append('=').append(JodaBeanUtils.toString(_yieldCurveManipulator)).append(',').append(' ');
      buf.append("curveName").append('=').append(JodaBeanUtils.toString(_curveName));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
