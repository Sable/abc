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

import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.components.common.DefaultingValueEvaluator;
import com.servingxml.app.Flow;
import com.servingxml.util.record.ValueType;
import com.servingxml.util.record.RecordBuilder;

public class ParameterDescriptorImpl implements ParameterDescriptor {
  private final Name name;
  private final ValueEvaluator valueEvaluator;
  private final ValueType valueType;

  public ParameterDescriptorImpl(Name name, ValueEvaluator valueEvaluator, ValueType valueType) {
    this.name = name;
    this.valueEvaluator = valueEvaluator;
    this.valueType = valueType;
  }

  public Name getName() {
    return name;
  }

  public void addParametersTo(ServiceContext context, Flow flow, RecordBuilder parameterBuilder) {
    //Value value = valueEvaluator.bindValue(context, flow);
    //parameterBuilder.setValue(name, value);
    parameterBuilder.setValue(name,new LazyParameterBinding(this, context, flow, valueType));
  }

  public String[] getValues(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".getValues "+"Evaluating " + name);
    String[] myValues = valueEvaluator.evaluateStringArray(context, flow);

    return myValues;
  }
}
