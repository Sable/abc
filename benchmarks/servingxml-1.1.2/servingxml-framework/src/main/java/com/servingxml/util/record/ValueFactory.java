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

package com.servingxml.util.record;

import javax.xml.datatype.DatatypeFactory;

import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;

public class ValueFactory {
  public static Value createStringArrayValue(String[] sa) {
    Value value;
    if (sa != null) {
      value = new ArrayValue(sa, ValueTypeFactory.STRING_TYPE);
    } else {
      value = new NullValue(ValueTypeFactory.STRING_TYPE);
    }
    return value;
  }
}
