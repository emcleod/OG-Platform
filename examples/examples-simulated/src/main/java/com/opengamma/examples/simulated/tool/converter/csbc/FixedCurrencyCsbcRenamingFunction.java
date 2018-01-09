/**
 * 
 */
package com.opengamma.examples.simulated.tool.converter.csbc;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.Function2;

/**
 * A renaming function for {@link CurveSpecificationBuilderConfiguration} where the currency
 * string is fixed upon construction. If {@link #_name} is null, the new name is 
 * "[original name] [ISO currency string]. If {@link #_name} is not null, the new name is 
 * "[original name] [_name] [ISO currency string]".
 *
 * @author emcleod
 */
public class FixedCurrencyCsbcRenamingFunction implements Function2<String, String, String> {
  /** The currency */
  private final String _currency;
  /** A string containing additional information */
  private final String _name;

  /**
   * Sets the additional string to null
   * @param currency The currency string, not null
   */
  public FixedCurrencyCsbcRenamingFunction(final String currency) {
    this(currency, null);
  }

  /**
   * @param currency The currency string, not null
   * @param name The additional string, can be null
   */
  public FixedCurrencyCsbcRenamingFunction(final String currency, final String name) {
    ArgumentChecker.notNull(currency, "currency");
    _currency = currency;
    _name = name;
  }

  @Override
  public String apply(final String name, final String currency) {
    return _name == null ? name + " " + _currency : name + " " + _name + " " + _currency;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FixedCurrencyCsbcRenamingFunction)) {
      return false;
    }
    final FixedCurrencyCsbcRenamingFunction other = (FixedCurrencyCsbcRenamingFunction) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    return true;
  }

}
