/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;

/**
 * 
 */
public class PositionGreekTest {
  private static final PositionGreek GREEK = new PositionGreek(Greek.VANNA);

  @Test(expected = NullPointerException.class)
  public void testNullGreek() {
    new PositionGreek(null);
  }

  @Test
  public void test() {
    final PositionGreek greek1 = new PositionGreek(Greek.VANNA);
    final PositionGreek greek2 = new PositionGreek(Greek.VARIANCE_ULTIMA);
    assertEquals(GREEK.getUnderlyingGreek(), Greek.VANNA);
    assertEquals(GREEK, greek1);
    assertFalse(GREEK.equals(greek2));
    assertEquals(GREEK.hashCode(), greek1.hashCode());
    assertTrue(GREEK.equals(GREEK));
    assertFalse(GREEK.equals(null));
    assertFalse(GREEK.equals(Greek.VANNA));
  }
}
