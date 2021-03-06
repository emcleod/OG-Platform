/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalTime;

import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Convention for a swap index.
 */
@BeanDefinition
public class SwapIndexConvention extends FinancialConvention {

  /**
   * Type of the convention.
   */
  public static final ConventionType TYPE = ConventionType.of("SwapIndex");

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The fixing time.
   */
  @PropertyDefinition(validate = "notNull")
  private LocalTime _fixingTime;
  /**
   * The swap convention.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _swapConvention;

  /**
   * Creates an instance.
   */
  protected SwapIndexConvention() {
    super();
  }

  /**
   * Creates an instance.
   * 
   * @param name  the convention name, not null
   * @param externalIdBundle  the external identifiers for this convention, not null
   * @param fixingTime  the fixing time, not null
   * @param swapConvention  the swap convention, not null
   */
  public SwapIndexConvention(
      final String name, final ExternalIdBundle externalIdBundle, final LocalTime fixingTime,
      final ExternalId swapConvention) {
    super(name, externalIdBundle);
    setFixingTime(fixingTime);
    setSwapConvention(swapConvention);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type identifying this convention.
   * 
   * @return the {@link #TYPE} constant, not null
   */
  @Override
  public ConventionType getConventionType() {
    return TYPE;
  }

  /**
   * Accepts a visitor to manage traversal of the hierarchy.
   *
   * @param <T>  the result type of the visitor
   * @param visitor  the visitor, not null
   * @return the result
   */
  @Override
  public <T> T accept(final FinancialConventionVisitor<T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapIndexConvention(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SwapIndexConvention}.
   * @return the meta-bean, not null
   */
  public static SwapIndexConvention.Meta meta() {
    return SwapIndexConvention.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SwapIndexConvention.Meta.INSTANCE);
  }

  @Override
  public SwapIndexConvention.Meta metaBean() {
    return SwapIndexConvention.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fixing time.
   * @return the value of the property, not null
   */
  public LocalTime getFixingTime() {
    return _fixingTime;
  }

  /**
   * Sets the fixing time.
   * @param fixingTime  the new value of the property, not null
   */
  public void setFixingTime(LocalTime fixingTime) {
    JodaBeanUtils.notNull(fixingTime, "fixingTime");
    this._fixingTime = fixingTime;
  }

  /**
   * Gets the the {@code fixingTime} property.
   * @return the property, not null
   */
  public final Property<LocalTime> fixingTime() {
    return metaBean().fixingTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the swap convention.
   * @return the value of the property, not null
   */
  public ExternalId getSwapConvention() {
    return _swapConvention;
  }

  /**
   * Sets the swap convention.
   * @param swapConvention  the new value of the property, not null
   */
  public void setSwapConvention(ExternalId swapConvention) {
    JodaBeanUtils.notNull(swapConvention, "swapConvention");
    this._swapConvention = swapConvention;
  }

  /**
   * Gets the the {@code swapConvention} property.
   * @return the property, not null
   */
  public final Property<ExternalId> swapConvention() {
    return metaBean().swapConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SwapIndexConvention clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SwapIndexConvention other = (SwapIndexConvention) obj;
      return JodaBeanUtils.equal(getFixingTime(), other.getFixingTime()) &&
          JodaBeanUtils.equal(getSwapConvention(), other.getSwapConvention()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getFixingTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSwapConvention());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("SwapIndexConvention{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("fixingTime").append('=').append(JodaBeanUtils.toString(getFixingTime())).append(',').append(' ');
    buf.append("swapConvention").append('=').append(JodaBeanUtils.toString(getSwapConvention())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SwapIndexConvention}.
   */
  public static class Meta extends FinancialConvention.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code fixingTime} property.
     */
    private final MetaProperty<LocalTime> _fixingTime = DirectMetaProperty.ofReadWrite(
        this, "fixingTime", SwapIndexConvention.class, LocalTime.class);
    /**
     * The meta-property for the {@code swapConvention} property.
     */
    private final MetaProperty<ExternalId> _swapConvention = DirectMetaProperty.ofReadWrite(
        this, "swapConvention", SwapIndexConvention.class, ExternalId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "fixingTime",
        "swapConvention");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1255686170:  // fixingTime
          return _fixingTime;
        case 1414180196:  // swapConvention
          return _swapConvention;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SwapIndexConvention> builder() {
      return new DirectBeanBuilder<SwapIndexConvention>(new SwapIndexConvention());
    }

    @Override
    public Class<? extends SwapIndexConvention> beanType() {
      return SwapIndexConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code fixingTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalTime> fixingTime() {
      return _fixingTime;
    }

    /**
     * The meta-property for the {@code swapConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> swapConvention() {
      return _swapConvention;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1255686170:  // fixingTime
          return ((SwapIndexConvention) bean).getFixingTime();
        case 1414180196:  // swapConvention
          return ((SwapIndexConvention) bean).getSwapConvention();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1255686170:  // fixingTime
          ((SwapIndexConvention) bean).setFixingTime((LocalTime) newValue);
          return;
        case 1414180196:  // swapConvention
          ((SwapIndexConvention) bean).setSwapConvention((ExternalId) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SwapIndexConvention) bean)._fixingTime, "fixingTime");
      JodaBeanUtils.notNull(((SwapIndexConvention) bean)._swapConvention, "swapConvention");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
