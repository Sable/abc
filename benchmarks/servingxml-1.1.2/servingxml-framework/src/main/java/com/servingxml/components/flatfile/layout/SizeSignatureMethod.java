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

public class SizeSignatureMethod implements SignatureMethod {
  private long size = 0L;

  public void data(byte[] bytes, int start, int length) {
    size += length;
  }

  public void validate(Value expectedValue) {
    long expected;
    try {
      expected = Long.parseLong(expectedValue.getString());
    } catch (Exception t) {
      throw new ServingXmlException("Bad long value " + expectedValue.getString());
    }
    if (expected != size) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.SIZE_INTEGRITY_CHECK_FAILED, 
                                                                 expectedValue.getString(), 
                                                                 Long.toString(size));
      throw new ServingXmlException(message);
    }
  }

  public Value getSignature() {
    return new ScalarValue(new Long(size), ValueTypeFactory.LONG_TYPE);
  }
}


