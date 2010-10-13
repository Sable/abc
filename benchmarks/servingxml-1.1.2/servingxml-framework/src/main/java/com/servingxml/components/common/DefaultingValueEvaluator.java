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

package com.servingxml.components.common;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.record.DefaultingValue;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.SystemConstants;    

public class DefaultingValueEvaluator implements ValueEvaluator {
  private final ValueEvaluator valueEvaluator;
  private final ValueEvaluator defaultEvaluator;

  public DefaultingValueEvaluator(ValueEvaluator valueEvaluator, 
                                  ValueEvaluator defaultEvaluator) {
    this.valueEvaluator = valueEvaluator;
    this.defaultEvaluator = defaultEvaluator;
  }

  public Value bindValue(ServiceContext context, Flow flow) {
    Value value = valueEvaluator.bindValue(context,flow);
    Value defaultValue = defaultEvaluator.bindValue(context,flow);
    return new DefaultingValue(value, defaultValue);
  }

  public String evaluateString(ServiceContext context, Flow flow) {
    Value defaultingValue = bindValue(context,flow);
    return defaultingValue.getString();
  }

  public String[] evaluateStringArray(ServiceContext context, Flow flow) {
    Value defaultingValue = bindValue(context,flow);
    return defaultingValue.getStringArray();
  }

  public Value evaluateValue(ServiceContext context, Flow flow) {
    Value defaultingValue = bindValue(context,flow);
    return defaultingValue;
  }
}

