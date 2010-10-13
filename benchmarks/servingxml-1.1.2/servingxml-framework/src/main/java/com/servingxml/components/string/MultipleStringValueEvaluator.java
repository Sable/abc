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

package com.servingxml.components.string;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.util.Name;    
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;    
import com.servingxml.util.record.ArrayValue;
import com.servingxml.util.record.Record;    
import com.servingxml.util.record.Value;
import com.servingxml.util.record.ValueTypeFactory;

/**
 * The <code>MultipleStringValueEvaluator</code> class implements a <code>ValueEvaluator</code>.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MultipleStringValueEvaluator implements ValueEvaluator {

  private final Stringable[] stringFactories;

  public MultipleStringValueEvaluator(Stringable[] stringFactories) {
    this.stringFactories = stringFactories;
  }

  public String evaluateString(ServiceContext context, Flow flow) {
    String s = "";
    if (stringFactories.length > 0) {
      s = stringFactories[0].createString(context, flow);
    }
    return s;
  }

  public String[] evaluateStringArray(ServiceContext context, Flow flow) {
    String[] a = new String[stringFactories.length];
    for (int i = 0; i < stringFactories.length; ++i) {
      a[i] = stringFactories[i].createString(context, flow);
    }
    return a;
  }

  public Value evaluateValue(ServiceContext context, Flow flow) {
    String[] a = evaluateStringArray(context,flow);
    return new ArrayValue(a,ValueTypeFactory.STRING_TYPE);
  }

  public Value bindValue(ServiceContext context, Flow flow) {
    return Value.EMPTY;
  }
}
