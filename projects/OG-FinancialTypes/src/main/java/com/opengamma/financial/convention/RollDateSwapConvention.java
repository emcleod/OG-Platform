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

import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Convention for IMM swaps (swaps with dates generated by a roll date schedule).
 */
@BeanDefinition
public class RollDateSwapConvention extends FinancialConvention {

  /**
   * Type of the convention.
   */
  public static final ConventionType TYPE = ConventionType.of("RollDateSwap");

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * The roll date convention.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _rollDateConvention;
  /**
   * The pay leg convention.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _payLegConvention;
  /**
   * The receive leg convention.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _receiveLegConvention;

  /**
   * Creates an instance.
   */
  RollDateSwapConvention() {
    super();
  }

  /**
   * Creates an instance.
   * 
   * @param name  the convention name, not null
   * @param externalIdBundle  the external identifiers for this convention, not null
   * @param payLegConvention  the pay leg convention, not null
   * @param receiveLegConvention  the receive leg convention, not null
   * @param rollDateConvention  the roll date convention, not null
   */
  public RollDateSwapConvention(
      final String name, final ExternalIdBundle externalIdBundle, final ExternalId payLegConvention,
      final ExternalId receiveLegConvention, final ExternalId rollDateConvention) {
    super(name, externalIdBundle);
    setRollDateConvention(rollDateConvention);
    setPayLegConvention(payLegConvention);
    setReceiveLegConvention(receiveLegConvention);
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
    return visitor.visitIMMSwapConvention(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RollDateSwapConvention}.
   * @return the meta-bean, not null
   */
  public static RollDateSwapConvention.Meta meta() {
    return RollDateSwapConvention.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RollDateSwapConvention.Meta.INSTANCE);
  }

  @Override
  public RollDateSwapConvention.Meta metaBean() {
    return RollDateSwapConvention.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the roll date convention.
   * @return the value of the property, not null
   */
  public ExternalId getRollDateConvention() {
    return _rollDateConvention;
  }

  /**
   * Sets the roll date convention.
   * @param rollDateConvention  the new value of the property, not null
   */
  public void setRollDateConvention(ExternalId rollDateConvention) {
    JodaBeanUtils.notNull(rollDateConvention, "rollDateConvention");
    this._rollDateConvention = rollDateConvention;
  }

  /**
   * Gets the the {@code rollDateConvention} property.
   * @return the property, not null
   */
  public final Property<ExternalId> rollDateConvention() {
    return metaBean().rollDateConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the pay leg convention.
   * @return the value of the property, not null
   */
  public ExternalId getPayLegConvention() {
    return _payLegConvention;
  }

  /**
   * Sets the pay leg convention.
   * @param payLegConvention  the new value of the property, not null
   */
  public void setPayLegConvention(ExternalId payLegConvention) {
    JodaBeanUtils.notNull(payLegConvention, "payLegConvention");
    this._payLegConvention = payLegConvention;
  }

  /**
   * Gets the the {@code payLegConvention} property.
   * @return the property, not null
   */
  public final Property<ExternalId> payLegConvention() {
    return metaBean().payLegConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the receive leg convention.
   * @return the value of the property, not null
   */
  public ExternalId getReceiveLegConvention() {
    return _receiveLegConvention;
  }

  /**
   * Sets the receive leg convention.
   * @param receiveLegConvention  the new value of the property, not null
   */
  public void setReceiveLegConvention(ExternalId receiveLegConvention) {
    JodaBeanUtils.notNull(receiveLegConvention, "receiveLegConvention");
    this._receiveLegConvention = receiveLegConvention;
  }

  /**
   * Gets the the {@code receiveLegConvention} property.
   * @return the property, not null
   */
  public final Property<ExternalId> receiveLegConvention() {
    return metaBean().receiveLegConvention().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public RollDateSwapConvention clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RollDateSwapConvention other = (RollDateSwapConvention) obj;
      return JodaBeanUtils.equal(getRollDateConvention(), other.getRollDateConvention()) &&
          JodaBeanUtils.equal(getPayLegConvention(), other.getPayLegConvention()) &&
          JodaBeanUtils.equal(getReceiveLegConvention(), other.getReceiveLegConvention()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getRollDateConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPayLegConvention());
    hash += hash * 31 + JodaBeanUtils.hashCode(getReceiveLegConvention());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("RollDateSwapConvention{");
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
    buf.append("rollDateConvention").append('=').append(JodaBeanUtils.toString(getRollDateConvention())).append(',').append(' ');
    buf.append("payLegConvention").append('=').append(JodaBeanUtils.toString(getPayLegConvention())).append(',').append(' ');
    buf.append("receiveLegConvention").append('=').append(JodaBeanUtils.toString(getReceiveLegConvention())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RollDateSwapConvention}.
   */
  public static class Meta extends FinancialConvention.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code rollDateConvention} property.
     */
    private final MetaProperty<ExternalId> _rollDateConvention = DirectMetaProperty.ofReadWrite(
        this, "rollDateConvention", RollDateSwapConvention.class, ExternalId.class);
    /**
     * The meta-property for the {@code payLegConvention} property.
     */
    private final MetaProperty<ExternalId> _payLegConvention = DirectMetaProperty.ofReadWrite(
        this, "payLegConvention", RollDateSwapConvention.class, ExternalId.class);
    /**
     * The meta-property for the {@code receiveLegConvention} property.
     */
    private final MetaProperty<ExternalId> _receiveLegConvention = DirectMetaProperty.ofReadWrite(
        this, "receiveLegConvention", RollDateSwapConvention.class, ExternalId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "rollDateConvention",
        "payLegConvention",
        "receiveLegConvention");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 509875100:  // rollDateConvention
          return _rollDateConvention;
        case 774631511:  // payLegConvention
          return _payLegConvention;
        case -560732676:  // receiveLegConvention
          return _receiveLegConvention;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RollDateSwapConvention> builder() {
      return new DirectBeanBuilder<RollDateSwapConvention>(new RollDateSwapConvention());
    }

    @Override
    public Class<? extends RollDateSwapConvention> beanType() {
      return RollDateSwapConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code rollDateConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> rollDateConvention() {
      return _rollDateConvention;
    }

    /**
     * The meta-property for the {@code payLegConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> payLegConvention() {
      return _payLegConvention;
    }

    /**
     * The meta-property for the {@code receiveLegConvention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> receiveLegConvention() {
      return _receiveLegConvention;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 509875100:  // rollDateConvention
          return ((RollDateSwapConvention) bean).getRollDateConvention();
        case 774631511:  // payLegConvention
          return ((RollDateSwapConvention) bean).getPayLegConvention();
        case -560732676:  // receiveLegConvention
          return ((RollDateSwapConvention) bean).getReceiveLegConvention();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 509875100:  // rollDateConvention
          ((RollDateSwapConvention) bean).setRollDateConvention((ExternalId) newValue);
          return;
        case 774631511:  // payLegConvention
          ((RollDateSwapConvention) bean).setPayLegConvention((ExternalId) newValue);
          return;
        case -560732676:  // receiveLegConvention
          ((RollDateSwapConvention) bean).setReceiveLegConvention((ExternalId) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((RollDateSwapConvention) bean)._rollDateConvention, "rollDateConvention");
      JodaBeanUtils.notNull(((RollDateSwapConvention) bean)._payLegConvention, "payLegConvention");
      JodaBeanUtils.notNull(((RollDateSwapConvention) bean)._receiveLegConvention, "receiveLegConvention");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
