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

import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;    
import com.servingxml.util.Name;    
import com.servingxml.app.Flow;

/**
 * The <code>Concat</code> class implements a <code>StringFactory</code>.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class Concat implements StringFactory {

  private final String quoteSymbol;
  private final String separator;
  private final ValueEvaluator valueEvaluator;

  public Concat(ValueEvaluator valueEvaluator, String separator, 
  String quoteSymbol) {
    this.valueEvaluator = valueEvaluator;
    this.separator = separator;
    this.quoteSymbol = quoteSymbol;
  }

  public String createString(ServiceContext context, Flow flow) {
    StringBuilder buf = new StringBuilder();
    createString(context,flow,buf);
    
    String s = buf.toString();
    return s;
  }

  public void createString(ServiceContext context, Flow flow, StringBuilder buf) {
    String[] values = valueEvaluator.evaluateStringArray(context, flow);
    for (int i = 0; i < values.length; ++i) {
      if (i > 0 && separator.length() > 0) {
        buf.append(separator);
      }
      if (quoteSymbol.length() > 0) {
        buf.append(quoteSymbol);
      }
      buf.append(values[i]);
      if (quoteSymbol.length() > 0) {
        buf.append(quoteSymbol);
      }
    }
  }
}
