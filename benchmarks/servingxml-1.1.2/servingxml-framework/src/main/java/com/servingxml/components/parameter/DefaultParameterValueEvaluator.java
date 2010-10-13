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

package com.servingxml.components.parameter;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.SystemConstants;    
import com.servingxml.components.common.ValueEvaluator;

public class DefaultParameterValueEvaluator implements ValueEvaluator {
  private final Name parameterName;
  private final ValueEvaluator defaultEvaluator;

  public DefaultParameterValueEvaluator(Name parameterName, ValueEvaluator defaultEvaluator) {
    this.parameterName = parameterName;
    this.defaultEvaluator = defaultEvaluator;
  }

  public Value bindValue(ServiceContext context, Flow flow) {
    return Value.EMPTY;
  }

  public String evaluateString(ServiceContext context, Flow flow) {
    String value = flow.getParameters().getString(parameterName);
    if (value == null) {
      String defaultValue = defaultEvaluator.evaluateString(context, flow);
      if (defaultValue != null) {
        value = defaultValue;
      }
    }
    return value;
  }

  public String[] evaluateStringArray(ServiceContext context, Flow flow) {
    String[] value = flow.getParameters().getStringArray(parameterName);
    if (value == null) {
      String[] defaultValues = defaultEvaluator.evaluateStringArray(context, flow);
      if (defaultValues != null && defaultValues.length > 0) {
        value = defaultValues;
      }
    }
    return value;
  }

  public Value evaluateValue(ServiceContext context, Flow flow) {
    Value value = flow.getParameters().getValue(parameterName);
    if (value == null) {
      Value defaultValue = defaultEvaluator.evaluateValue(context, flow);
      if (defaultValue != null) {
        value = defaultValue;
      }
    }
    return value;
  }
}
