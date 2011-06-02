/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.BasicBeanBuilder;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.PublicSPI;
import com.opengamma.util.db.Paging;

/**
 * Result from searching historic time-series.
 * 
 * @param <T> java.util.Date/LocalDate
 */
@PublicSPI
@BeanDefinition
public class TimeSeriesSearchHistoricResult<T> extends DirectBean {

  /**
   * The paging information.
   */
  @PropertyDefinition
  private Paging _paging;
  /**
   * The list of matched time-series documents.
   */
  @PropertyDefinition
  private final List<TimeSeriesDocument<T>> _documents = new ArrayList<TimeSeriesDocument<T>>();

  /**
   * Creates an instance.
   */
  public TimeSeriesSearchHistoricResult() {
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TimeSeriesSearchHistoricResult}.
   * @param <R>  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> TimeSeriesSearchHistoricResult.Meta<R> meta() {
    return TimeSeriesSearchHistoricResult.Meta.INSTANCE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public TimeSeriesSearchHistoricResult.Meta<T> metaBean() {
    return TimeSeriesSearchHistoricResult.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -995747956:  // paging
        return getPaging();
      case 943542968:  // documents
        return getDocuments();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -995747956:  // paging
        setPaging((Paging) newValue);
        return;
      case 943542968:  // documents
        setDocuments((List<TimeSeriesDocument<T>>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      TimeSeriesSearchHistoricResult<?> other = (TimeSeriesSearchHistoricResult<?>) obj;
      return JodaBeanUtils.equal(getPaging(), other.getPaging()) &&
          JodaBeanUtils.equal(getDocuments(), other.getDocuments());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getPaging());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDocuments());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the paging information.
   * @return the value of the property
   */
  public Paging getPaging() {
    return _paging;
  }

  /**
   * Sets the paging information.
   * @param paging  the new value of the property
   */
  public void setPaging(Paging paging) {
    this._paging = paging;
  }

  /**
   * Gets the the {@code paging} property.
   * @return the property, not null
   */
  public final Property<Paging> paging() {
    return metaBean().paging().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the list of matched time-series documents.
   * @return the value of the property
   */
  public List<TimeSeriesDocument<T>> getDocuments() {
    return _documents;
  }

  /**
   * Sets the list of matched time-series documents.
   * @param documents  the new value of the property
   */
  public void setDocuments(List<TimeSeriesDocument<T>> documents) {
    this._documents.clear();
    this._documents.addAll(documents);
  }

  /**
   * Gets the the {@code documents} property.
   * @return the property, not null
   */
  public final Property<List<TimeSeriesDocument<T>>> documents() {
    return metaBean().documents().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TimeSeriesSearchHistoricResult}.
   */
  public static class Meta<T> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code paging} property.
     */
    private final MetaProperty<Paging> _paging = DirectMetaProperty.ofReadWrite(
        this, "paging", TimeSeriesSearchHistoricResult.class, Paging.class);
    /**
     * The meta-property for the {@code documents} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<TimeSeriesDocument<T>>> _documents = DirectMetaProperty.ofReadWrite(
        this, "documents", TimeSeriesSearchHistoricResult.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "paging",
        "documents");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -995747956:  // paging
          return _paging;
        case 943542968:  // documents
          return _documents;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends TimeSeriesSearchHistoricResult<T>> builder() {
      return new BasicBeanBuilder<TimeSeriesSearchHistoricResult<T>>(new TimeSeriesSearchHistoricResult<T>());
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends TimeSeriesSearchHistoricResult<T>> beanType() {
      return (Class) TimeSeriesSearchHistoricResult.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code paging} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Paging> paging() {
      return _paging;
    }

    /**
     * The meta-property for the {@code documents} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<TimeSeriesDocument<T>>> documents() {
      return _documents;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
