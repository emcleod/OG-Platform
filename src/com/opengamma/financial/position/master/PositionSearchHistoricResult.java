/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanDefinition;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.PropertyReadWrite;
import org.joda.beans.impl.BasicMetaBean;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

import com.opengamma.util.db.Paging;

/**
 * Result from searching for historic positions.
 */
@BeanDefinition
public class PositionSearchHistoricResult extends DirectBean {

  /**
   * The paging information.
   */
  @PropertyDefinition
  private Paging _paging;
  /**
   * The list of matched position documents.
   */
  @PropertyDefinition(readWrite = PropertyReadWrite.READ_ONLY)
  private List<PositionDocument> _documents = new ArrayList<PositionDocument>();

  /**
   * Creates an instance.
   */
  public PositionSearchHistoricResult() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the returned positions from within the documents.
   * @return the positions, not null
   */
  public List<PortfolioTreePosition> getPositions() {
    List<PortfolioTreePosition> result = new ArrayList<PortfolioTreePosition>();
    if (_documents != null) {
      for (PositionDocument doc : _documents) {
        result.add(doc.getPosition());
      }
    }
    return result;
  }

  /**
   * Gets the first document, or null if no documents.
   * @return the first document, null if none
   */
  public PositionDocument getFirstDocument() {
    return getDocuments().size() > 0 ? getDocuments().get(0) : null;
  }

  /**
   * Gets the first position, or null if no documents.
   * @return the first position, null if none
   */
  public PortfolioTreePosition getFirstPosition() {
    return getDocuments().size() > 0 ? getDocuments().get(0).getPosition() : null;
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code PositionSearchHistoricResult}.
   * @return the meta-bean, not null
   */
  public static PositionSearchHistoricResult.Meta meta() {
    return PositionSearchHistoricResult.Meta.INSTANCE;
  }

  @Override
  public PositionSearchHistoricResult.Meta metaBean() {
    return PositionSearchHistoricResult.Meta.INSTANCE;
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

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -995747956:  // paging
        setPaging((Paging) newValue);
        return;
      case 943542968:  // documents
        throw new UnsupportedOperationException("Property cannot be written: documents");
    }
    super.propertySet(propertyName, newValue);
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
   * Gets the list of matched position documents.
   * @return the value of the property
   */
  public List<PositionDocument> getDocuments() {
    return _documents;
  }

  /**
   * Gets the the {@code documents} property.
   * @return the property, not null
   */
  public final Property<List<PositionDocument>> documents() {
    return metaBean().documents().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PositionSearchHistoricResult}.
   */
  public static class Meta extends BasicMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code paging} property.
     */
    private final MetaProperty<Paging> _paging = DirectMetaProperty.ofReadWrite(this, "paging", Paging.class);
    /**
     * The meta-property for the {@code documents} property.
     */
    @SuppressWarnings("unchecked")
    private final MetaProperty<List<PositionDocument>> _documents = DirectMetaProperty.ofReadOnly(this, "documents", (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map;

    @SuppressWarnings("unchecked")
    protected Meta() {
      LinkedHashMap temp = new LinkedHashMap();
      temp.put("paging", _paging);
      temp.put("documents", _documents);
      _map = Collections.unmodifiableMap(temp);
    }

    @Override
    public PositionSearchHistoricResult createBean() {
      return new PositionSearchHistoricResult();
    }

    @Override
    public Class<? extends PositionSearchHistoricResult> beanType() {
      return PositionSearchHistoricResult.class;
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
    public final MetaProperty<List<PositionDocument>> documents() {
      return _documents;
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
