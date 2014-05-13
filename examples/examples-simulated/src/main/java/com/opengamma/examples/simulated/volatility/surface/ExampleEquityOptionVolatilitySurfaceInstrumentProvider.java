/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.volatility.surface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;

/**
 * Generates equity option synthetic ticker codes from ATM strike (set via init()), tenor, double and date).
 */
public class ExampleEquityOptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<LocalDate, Double> {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleEquityOptionVolatilitySurfaceInstrumentProvider.class);
  /** The ticker scheme */
  private static final ExternalScheme SCHEME = ExternalSchemes.OG_SYNTHETIC_TICKER;
  /** The prefix of the underlying spot rate */
  private final String _underlyingPrefix;
  /** The postfix */
  private final String _postfix;
  /** The market data field name */
  private final String _dataFieldName;
  /** The date formatter for expiries */
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yy");
  /** True if puts are to be generated */
  private Boolean _generatePuts;

  /**
   * @param underlyingPrefix The prefix of the underlying spot rate
   * @param postfix The ticker postfix
   * @param dataFieldName The data field name
   */
  public ExampleEquityOptionVolatilitySurfaceInstrumentProvider(final String underlyingPrefix, final String postfix, final String dataFieldName) {
    ArgumentChecker.notNull(underlyingPrefix, "underlying prefix");
    ArgumentChecker.notNull(postfix, "postfix");
    ArgumentChecker.notNull(dataFieldName, "data field name");
    _underlyingPrefix = underlyingPrefix;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
  }

  /**
   * Sets whether or not to generate puts (true) or calls (false).
   * @param generatePuts True if puts are to be generated
   */
  public void init(final boolean generatePuts) {
    _generatePuts = generatePuts;
  }

  /**
   * Gets the underlying spot rate prefix.
   * @return The underlying spot rate prefix
   */
  public String getUnderlyingPrefix() {
    return _underlyingPrefix;
  }

  /**
   * Gets the ticker postfix.
   * @return The postfix
   */
  public String getPostfix() {
    return _postfix;
  }

  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public ExternalId getInstrument(final LocalDate expiry, final Double strike) {
    throw new OpenGammaRuntimeException("Need surface date to calculate expiry");
  }

  @Override
  public ExternalId getInstrument(final LocalDate expiry, final Double strike, final LocalDate surfaceDate) {
    if (_generatePuts == null) {
      s_logger.error("Cannot create option volatility code until init() has been called");
    }
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_underlyingPrefix);
    final String formattedDate = DATE_FORMATTER.format(expiry);
    ticker.append(formattedDate);
    // TODO: check this logic
    if (_generatePuts) {
      ticker.append("P");
    } else {
      ticker.append("C");
    }
    ticker.append(strike);
    ticker.append(_postfix);
    return ExternalId.of(SCHEME, ticker.toString());
  }

  @Override
  public int hashCode() {
    return getUnderlyingPrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ExampleEquityOptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final ExampleEquityOptionVolatilitySurfaceInstrumentProvider other = (ExampleEquityOptionVolatilitySurfaceInstrumentProvider) obj;
    return getUnderlyingPrefix().equals(other.getUnderlyingPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        getDataFieldName().equals(other.getDataFieldName());
  }
}
