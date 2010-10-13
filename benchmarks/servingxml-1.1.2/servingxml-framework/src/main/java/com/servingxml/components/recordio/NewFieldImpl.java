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

package com.servingxml.components.recordio;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.Value;

public class NewFieldImpl implements NewField {
  private final Name name;
  private final ValueEvaluator valueEvaluator;

  public NewFieldImpl(Name name, ValueEvaluator valueEvaluator) {
    this.name = name;
    this.valueEvaluator = valueEvaluator;
  }

  public Name getName() {
    return name;                               
  }

  public void readField(ServiceContext context, 
                        Flow flow, 
                        RecordBuilder recordBuilder) {
    //System.out.println(getClass().getName()+".readField " + name + " name start");
    //System.out.println(flow.getRecord().toXmlString(context));

    String[] myValues = valueEvaluator.evaluateStringArray(context, flow);

    if (myValues != null) {
      recordBuilder.setStringArray(name,myValues);
    }
    //System.out.println(getClass().getName()+".readField " + name + " name end");
  }
}
