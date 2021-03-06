/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.spring;

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

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.CachedHolidaySource;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.MasterHolidaySource;
import com.opengamma.util.spring.SpringFactoryBean;

/**
 * Spring factory bean to create the holiday source.
 */
@Deprecated
@BeanDefinition
public class HolidaySourceFactoryBean extends SpringFactoryBean<HolidaySource> {

  /**
   * The holiday master.
   */
  @PropertyDefinition
  private HolidayMaster _holidayMaster;

  /**
   * Creates an instance.
   */
  @Deprecated
  public HolidaySourceFactoryBean() {
    super(HolidaySource.class);
  }

  //-------------------------------------------------------------------------
  @Deprecated
  @Override
  protected HolidaySource createObject() {
    HolidaySource source = new CachedHolidaySource(new MasterHolidaySource(getHolidayMaster()));
    return source;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HolidaySourceFactoryBean}.
   * @return the meta-bean, not null
   */
  public static HolidaySourceFactoryBean.Meta meta() {
    return HolidaySourceFactoryBean.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(HolidaySourceFactoryBean.Meta.INSTANCE);
  }

  @Override
  public HolidaySourceFactoryBean.Meta metaBean() {
    return HolidaySourceFactoryBean.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the holiday master.
   * @return the value of the property
   */
  public HolidayMaster getHolidayMaster() {
    return _holidayMaster;
  }

  /**
   * Sets the holiday master.
   * @param holidayMaster  the new value of the property
   */
  public void setHolidayMaster(HolidayMaster holidayMaster) {
    this._holidayMaster = holidayMaster;
  }

  /**
   * Gets the the {@code holidayMaster} property.
   * @return the property, not null
   */
  public final Property<HolidayMaster> holidayMaster() {
    return metaBean().holidayMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public HolidaySourceFactoryBean clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HolidaySourceFactoryBean other = (HolidaySourceFactoryBean) obj;
      return JodaBeanUtils.equal(getHolidayMaster(), other.getHolidayMaster()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidayMaster());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("HolidaySourceFactoryBean{");
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
    buf.append("holidayMaster").append('=').append(JodaBeanUtils.toString(getHolidayMaster())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code HolidaySourceFactoryBean}.
   */
  public static class Meta extends SpringFactoryBean.Meta<HolidaySource> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code holidayMaster} property.
     */
    private final MetaProperty<HolidayMaster> _holidayMaster = DirectMetaProperty.ofReadWrite(
        this, "holidayMaster", HolidaySourceFactoryBean.class, HolidayMaster.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "holidayMaster");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 246258906:  // holidayMaster
          return _holidayMaster;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HolidaySourceFactoryBean> builder() {
      return new DirectBeanBuilder<HolidaySourceFactoryBean>(new HolidaySourceFactoryBean());
    }

    @Override
    public Class<? extends HolidaySourceFactoryBean> beanType() {
      return HolidaySourceFactoryBean.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code holidayMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidayMaster> holidayMaster() {
      return _holidayMaster;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 246258906:  // holidayMaster
          return ((HolidaySourceFactoryBean) bean).getHolidayMaster();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 246258906:  // holidayMaster
          ((HolidaySourceFactoryBean) bean).setHolidayMaster((HolidayMaster) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
