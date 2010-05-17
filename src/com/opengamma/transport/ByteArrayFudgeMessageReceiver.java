/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.util.ArgumentChecker;

/**
 * A message receiver that receives byte array messages and handles them using a
 * {@link BatchFudgeMessageReceiver}.
 */
public class ByteArrayFudgeMessageReceiver implements ByteArrayMessageReceiver {

  /**
   * The underlying Fudge receiver.
   */
  private final FudgeMessageReceiver _underlying;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates a receiver based on an underlying Fudge receiver.
   * @param underlying  the underlying receiver, not null
   */
  public ByteArrayFudgeMessageReceiver(FudgeMessageReceiver underlying) {
    this(underlying, new FudgeContext());
  }

  /**
   * Creates a receiver based on an underlying Fudge receiver.
   * @param underlying  the underlying receiver, not null
   * @param fudgeContext  the context to use, not null
   */
  public ByteArrayFudgeMessageReceiver(FudgeMessageReceiver underlying, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "Underlying FudgeMessageReceiver");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying Fudge receiver.
   * @return the underlying Fudge receiver, not null
   */
  public FudgeMessageReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the Fudge context.
   * @return the fudge context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Receives the byte array message and processes it using the underlying Fudge receiver.
   * @param message  the byte array message, not null
   */
  @Override
  public void messageReceived(byte[] message) {
    FudgeMsgEnvelope msgEnvelope = getFudgeContext().deserialize(message);
    getUnderlying().messageReceived(getFudgeContext(), msgEnvelope);
  }

}
