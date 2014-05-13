/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.volatility.surface;

import static com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider.DATA_FIELD_NAME;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider.POSTFIX_FIELD_NAME;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider.PREFIX_FIELD_NAME;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Builder for converting {@link ExampleSwaptionVolatilitySurfaceInstrumentProvider} instances to/from Fudge messages.
 */
@FudgeBuilderFor(ExampleSwaptionVolatilitySurfaceInstrumentProvider.class)
public class ExampleSwaptionVolatilitySurfaceInstrumentProviderFudgeBuilder implements FudgeBuilder<ExampleSwaptionVolatilitySurfaceInstrumentProvider> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ExampleSwaptionVolatilitySurfaceInstrumentProvider object) {
    final MutableFudgeMsg message = serializer.newMessage();
    FudgeSerializer.addClassHeader(message, ExampleSwaptionVolatilitySurfaceInstrumentProvider.class);
    message.add(PREFIX_FIELD_NAME, object.getCountryPrefix());
    message.add("type", object.getTypePrefix());
    message.add(POSTFIX_FIELD_NAME, object.getPostfix());
    message.add("zeroPadFirstTenor", object.isZeroPadSwapMaturityTenor()); //TODO rename the field name
    message.add("zeroPadSecondTenor", object.isZeroPadSwaptionExpiryTenor()); //TODO rename the field name
    message.add(DATA_FIELD_NAME, object.getDataFieldName());
    return message;
  }

  @Override
  public ExampleSwaptionVolatilitySurfaceInstrumentProvider buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final String dataFieldName = getDataFieldName(message);
    if (dataFieldName == null) {
      return new ExampleSwaptionVolatilitySurfaceInstrumentProvider(getCountryPrefix(message), message.getString("type"),
          message.getBoolean("zeroPadFirstTenor"), message.getBoolean("zeroPadSecondTenor"), getPostFix(message));
    }
    return new ExampleSwaptionVolatilitySurfaceInstrumentProvider(getCountryPrefix(message), message.getString("type"),
        message.getBoolean("zeroPadFirstTenor"), message.getBoolean("zeroPadSecondTenor"), getPostFix(message), dataFieldName);
  }

  /**
   * Gets the postfix string from a message, trying first {@link #POSTFIX_FIELD_NAME}, then "postfix".
   * This is done for backwards compatibility.
   * @param message The message
   * @return The postfix
   */
  private static String getPostFix(final FudgeMsg message) {
    String postfix = message.getString(POSTFIX_FIELD_NAME);
    //backward compatibility
    if (postfix == null) {
      postfix = message.getString("postfix");
    }
    return postfix;
  }

  /**
   * Gets the country prefix string from a message, trying first {@link #PREFIX_FIELD_NAME}, then "countryPrefix".
   * This is done for backwards compatibility.
   * @param message The message
   * @return The country prefix
   */
  private static String getCountryPrefix(final FudgeMsg message) {
    String countryPrefix = message.getString(PREFIX_FIELD_NAME);
    //backward compatibility
    if (countryPrefix == null) {
      countryPrefix = message.getString("countryPrefix");
    }
    return countryPrefix;
  }

  /**
   * Gets the data field string from a message, trying first {@link #DATA_FIELD_NAME}, then "dataFieldName".
   * This is done for backwards compatibility.
   * @param message The message
   * @return The data field name
   */
  private static String getDataFieldName(final FudgeMsg message) {
    String dataFieldName = message.getString(DATA_FIELD_NAME);
    //backward compatibility
    if (dataFieldName == null) {
      dataFieldName = message.getString("dataFieldName");
    }
    return dataFieldName;
  }

}
