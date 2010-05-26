/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive.robust;

import java.util.Arrays;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class TrimmedMeanCalculator extends Function1D<Double[], Double> {
  private final double _gamma;
  private final Function1D<Double[], Double> _meanCalculator = new MeanCalculator();

  public TrimmedMeanCalculator(final double gamma) {
    if (gamma <= 0 || gamma >= 1)
      throw new IllegalArgumentException("Gamma must be between 0 and 1");
    _gamma = gamma > 0.5 ? 1 - gamma : gamma;
  }

  @Override
  public Double evaluate(final Double[] x) {
    ArgumentChecker.notNull(x, "x");
    if (x.length == 0)
      throw new IllegalArgumentException("Array was empty");
    final int length = x.length;
    final int value = (int) Math.round(length * _gamma);
    final Double[] copy = Arrays.copyOf(x, length);
    Arrays.sort(copy);
    final Double[] trimmed = new Double[length - 2 * value];
    for (int i = 0; i < trimmed.length; i++) {
      trimmed[i] = x[i + value];
    }
    return _meanCalculator.evaluate(trimmed);
  }
}
