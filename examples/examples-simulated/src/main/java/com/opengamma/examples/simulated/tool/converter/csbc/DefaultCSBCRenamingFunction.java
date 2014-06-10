/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.result.Function2;

/**
 *  @author emcleod
 *
 * The default renaming function for {@link CurveSpecificationBuilderConfiguration}. If
 * {@link #_name} is null, the new name is "[original name] [ISO currency string]. 
 * If {@link #_name} is not null, the new name is "[original name] [_name] [ISO currency string]".
 */
public class DefaultCSBCRenamingFunction implements Function2<String, String, String> {
  /** A string containing additional information */
  private final String _name;

  /**
   * Sets the additional string to null
   */
  public DefaultCSBCRenamingFunction() {
    this(null);
  }

  /**
   * @param name The additional string, can be null
   */
  public DefaultCSBCRenamingFunction(final String name) {
    _name = name;
  }

  @Override
  public String apply(final String name, final String currency) {
    return _name == null ? name + " " + currency : name + " " + _name + " " + currency;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DefaultCSBCRenamingFunction)) {
      return false;
    }
    final DefaultCSBCRenamingFunction other = (DefaultCSBCRenamingFunction) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

}
