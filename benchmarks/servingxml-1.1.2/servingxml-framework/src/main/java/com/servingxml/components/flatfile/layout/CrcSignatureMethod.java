/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package com.servingxml.components.flatfile.layout;

import java.util.zip.CRC32;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Value;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.record.ScalarValue;
import com.servingxml.util.record.ValueTypeFactory;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class CrcSignatureMethod implements SignatureMethod {
  private final CRC32 crc;

  public CrcSignatureMethod() {
    crc = new java.util.zip.CRC32();
  }

  public void data(byte[] bytes, int start, int length) {
    try {
      crc.update(bytes,start,length);
    } catch (Exception t) {
      throw new ServingXmlException("Error calculating CRC " + t.getMessage(),t);
    }
  }
                                
  public void validate(Value expectedValue) {
    long expected;
    try {
      expected = Long.parseLong(expectedValue.getString());
    } catch (Exception t) {
      throw new ServingXmlException("Bad CRC value " + expectedValue.getString());
    }
    if (expected != crc.getValue()) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.CRC_INTEGRITY_CHECK_FAILED, 
                                                                 expectedValue.getString(), 
                                                                 Long.toString(crc.getValue()));
      throw new ServingXmlException(message);
    }
  }

  public Value getSignature() {
    return new ScalarValue(new Long(crc.getValue()), ValueTypeFactory.LONG_TYPE);
  }
}


