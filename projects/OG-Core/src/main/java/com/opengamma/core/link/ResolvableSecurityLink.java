/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.link;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Represents a link to a Security object using an ExternalIdBundle
 * that can be resolved on demand.
 *
 * @param <T> type of the security
 */
@BeanDefinition
public final class ResolvableSecurityLink<T extends Security> extends SecurityLink<T> implements ImmutableBean {

  /**
   * The identification data for the object being linked to, not null.
   */
  @PropertyDefinition(validate = "notNull")
  private final LinkIdentifier<ExternalIdBundle, T> _linkIdentifier;

  /**
   * The resolver used to resolve the link on demand, not null.
   */
  // note that the resolver does not form part of the serialized form
  // of the bean
  private final LinkResolver<ExternalIdBundle, T> _resolver;

  /**
   * Creates a resolved link.
   * @param identifier the identifier for the linked object, not null
   * @param type the type of the linked object, not null
   * @param linkResolver the resolver used to resolve the link when requested, not null
   */
  /* package */ ResolvableSecurityLink(ExternalIdBundle identifier, Class<T> type,
                                       LinkResolver<ExternalIdBundle, T> linkResolver) {
    this(LinkIdentifier.of(identifier, type), linkResolver);
  }

  @ImmutableConstructor
  private ResolvableSecurityLink(LinkIdentifier<ExternalIdBundle, T> linkIdentifier) {
    this(linkIdentifier, new ServiceContextSecurityLinkResolver<T>());
  }

  private ResolvableSecurityLink(LinkIdentifier<ExternalIdBundle, T> linkIdentifier,
                                 LinkResolver<ExternalIdBundle, T> linkResolver) {
    _linkIdentifier = linkIdentifier;
    _resolver = ArgumentChecker.notNull(linkResolver, "linkResolver");
  }

  @Override
  public T resolve() {
    return _resolver.resolve(_linkIdentifier);
  }

  @Override
  public Class<T> getTargetType() {
    return _linkIdentifier.getType();
  }

  @Override
  public ExternalIdBundle getIdentifier() {
    return _linkIdentifier.getIdentifier();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ResolvableSecurityLink}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static ResolvableSecurityLink.Meta meta() {
    return ResolvableSecurityLink.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code ResolvableSecurityLink}.
   * @param <R>  the bean's generic type
   * @param cls  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R extends Security> ResolvableSecurityLink.Meta<R> metaResolvableSecurityLink(Class<R> cls) {
    return ResolvableSecurityLink.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ResolvableSecurityLink.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @param <T>  the type
   * @return the builder, not null
   */
  public static <T extends Security> ResolvableSecurityLink.Builder<T> builder() {
    return new ResolvableSecurityLink.Builder<T>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public ResolvableSecurityLink.Meta<T> metaBean() {
    return ResolvableSecurityLink.Meta.INSTANCE;
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
   * Gets the identification data for the object being linked to, not null.
   * @return the value of the property, not null
   */
  public LinkIdentifier<ExternalIdBundle, T> getLinkIdentifier() {
    return _linkIdentifier;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder<T> toBuilder() {
    return new Builder<T>(this);
  }

  @Override
  public ResolvableSecurityLink<T> clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ResolvableSecurityLink<?> other = (ResolvableSecurityLink<?>) obj;
      return JodaBeanUtils.equal(getLinkIdentifier(), other.getLinkIdentifier());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getLinkIdentifier());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("ResolvableSecurityLink{");
    buf.append("linkIdentifier").append('=').append(JodaBeanUtils.toString(getLinkIdentifier()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ResolvableSecurityLink}.
   */
  public static final class Meta<T extends Security> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code linkIdentifier} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<LinkIdentifier<ExternalIdBundle, T>> _linkIdentifier = DirectMetaProperty.ofImmutable(
        this, "linkIdentifier", ResolvableSecurityLink.class, (Class) LinkIdentifier.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "linkIdentifier");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1114306493:  // linkIdentifier
          return _linkIdentifier;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ResolvableSecurityLink.Builder<T> builder() {
      return new ResolvableSecurityLink.Builder<T>();
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends ResolvableSecurityLink<T>> beanType() {
      return (Class) ResolvableSecurityLink.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code linkIdentifier} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LinkIdentifier<ExternalIdBundle, T>> linkIdentifier() {
      return _linkIdentifier;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1114306493:  // linkIdentifier
          return ((ResolvableSecurityLink<?>) bean).getLinkIdentifier();
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
   * The bean-builder for {@code ResolvableSecurityLink}.
   */
  public static final class Builder<T extends Security> extends DirectFieldsBeanBuilder<ResolvableSecurityLink<T>> {

    private LinkIdentifier<ExternalIdBundle, T> _linkIdentifier;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(ResolvableSecurityLink<T> beanToCopy) {
      this._linkIdentifier = beanToCopy.getLinkIdentifier();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1114306493:  // linkIdentifier
          return _linkIdentifier;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder<T> set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1114306493:  // linkIdentifier
          this._linkIdentifier = (LinkIdentifier<ExternalIdBundle, T>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder<T> set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder<T> setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder<T> setString(MetaProperty<?> property, String value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder<T> setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public ResolvableSecurityLink<T> build() {
      return new ResolvableSecurityLink<T>(
          _linkIdentifier);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the {@code linkIdentifier} property in the builder.
     * @param linkIdentifier  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder<T> linkIdentifier(LinkIdentifier<ExternalIdBundle, T> linkIdentifier) {
      JodaBeanUtils.notNull(linkIdentifier, "linkIdentifier");
      this._linkIdentifier = linkIdentifier;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("ResolvableSecurityLink.Builder{");
      buf.append("linkIdentifier").append('=').append(JodaBeanUtils.toString(_linkIdentifier));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
