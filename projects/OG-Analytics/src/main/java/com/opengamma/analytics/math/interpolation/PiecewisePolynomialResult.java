/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Result of interpolation by piecewise polynomial containing
 * _knots: Positions of knots
 * _coefMatrix: Coefficient matrix whose i-th row vector is { a_n, a_{n-1}, ...} for the i-th interval, where a_n, a_{n-1},... are coefficients of f(x) = a_n (x-x_i)^n + a_{n-1} (x-x_i)^{n-1} + ....
 * In multidimensional cases, coefficients for the i-th interval of the j-th spline is in (j*(i-1) + i) -th row vector.
 * _nIntervals: Number of intervals, which should be (Number of knots) - 1
 * _order: Number of coefficients in polynomial, which is equal to (polynomial degree) + 1
 * _dim: Number of splines
 */
public class PiecewisePolynomialResult {

  private DoubleMatrix1D _knots;
  private DoubleMatrix2D _coefMatrix;
  private int _nIntervals;
  private int _order;
  private int _dim;

  /**
   * Constructor
   * @param knots 
   * @param coefMatrix 
   * @param order 
   * @param dim 
   */
  public PiecewisePolynomialResult(final DoubleMatrix1D knots, final DoubleMatrix2D coefMatrix, final int order, final int dim) {

    _knots = knots;
    _coefMatrix = coefMatrix;
    _nIntervals = knots.getNumberOfElements() - 1;
    _order = order;
    _dim = dim;

  }

  /**
   * Access _knots
   * @return Knots as DoubleMatrix1D
   */
  public DoubleMatrix1D getKnots() {
    return _knots;
  }

  /**
   * Access _coefMatrix
   * @return Coefficient Matrix
   */
  public DoubleMatrix2D getCoefMatrix() {
    return _coefMatrix;
  }

  /**
   * Access _nIntervals
   * @return Number of Intervals
   */
  public int getNumberOfIntervals() {
    return _nIntervals;
  }

  /**
   * Access _order
   * @return Number of coefficients in polynomial; 2 if _nIntervals=1, 3 if _nIntervals=2, 4 otherwise
   */
  public int getOrder() {
    return _order;
  }

  /**
   * Access _dim
   * @return Dimension of spline 
   */
  public int getDimensions() {
    return _dim;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _coefMatrix.hashCode();
    result = prime * result + _dim;
    result = prime * result + _knots.hashCode();
    result = prime * result + _order;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof PiecewisePolynomialResult)) {
      return false;
    }
    PiecewisePolynomialResult other = (PiecewisePolynomialResult) obj;
    if (!_coefMatrix.equals(other._coefMatrix)) {
      return false;
    }
    if (_dim != other._dim) {
      return false;
    }
    if (!_knots.equals(other._knots)) {
      return false;
    }
    if (_order != other._order) {
      return false;
    }
    return true;
  }

}
