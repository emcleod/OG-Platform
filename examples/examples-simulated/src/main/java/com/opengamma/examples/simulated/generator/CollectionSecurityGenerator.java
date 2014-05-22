/**
 * 
 */
package com.opengamma.examples.simulated.generator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * @author emcleod
 *
 * Security generator that wraps a collection of securities. The securities are generated
 * with repeated calls to {{@link #createSecurity()} and an exception is thrown if more
 * securities are requested than were provided in the original collection. The order in
 * which the securities are returned depends on the original collection.
 */
public class CollectionSecurityGenerator extends SecurityGenerator<ManageableSecurity> {
  /** The iterator */
  private final Iterator<ManageableSecurity> _iterator;

  /**
   * @param securities The securities, not null
   */
  public CollectionSecurityGenerator(final Collection<ManageableSecurity> securities) {
    ArgumentChecker.notNull(securities, "securities");
    _iterator = securities.iterator();
  }

  /**
   * @param securities The securities, not null
   */
  public CollectionSecurityGenerator(final ManageableSecurity[] securities) {
    ArgumentChecker.notNull(securities, "securities");
    _iterator = Arrays.asList(securities).iterator();
  }

  @Override
  public ManageableSecurity createSecurity() {
    if (_iterator.hasNext()) {
      return _iterator.next();
    }
    throw new IllegalStateException("Asked for more securities than were provided to the generator");
  }
}
