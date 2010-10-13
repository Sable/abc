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

import com.servingxml.util.record.Value;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.ValueType;
import com.servingxml.util.record.Value;
import com.servingxml.util.record.ArrayValue;
import com.servingxml.util.record.ScalarValue;
import com.servingxml.util.record.NullValue;
import com.servingxml.app.Flow;                         
import com.servingxml.app.ParameterDescriptor;                         
import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;

public class LazyParameterBinding implements Value {
  private final ParameterDescriptor parameterDescriptor;
  private final ServiceContext context;
  private final Flow flow;
  private final ValueType valueType;
  private Value fieldValue;

  public LazyParameterBinding(ParameterDescriptor parameterDescriptor, 
    ServiceContext context, Flow flow, ValueType valueType) {

    //System.out.println(getClass().getName()+".cons creating parameter value for " + parameterDescriptor.getName());
    this.parameterDescriptor = parameterDescriptor;
    this.context = context;
    this.flow = flow;
    this.valueType = valueType;
    this.fieldValue = null;          
  }

  private synchronized void evaluate() {
    if (fieldValue == null) {
      String[] sa = parameterDescriptor.getValues(context, flow);
      if (sa == null) {
        fieldValue = new NullValue(valueType);
      } else if (sa.length == 1) {
        Object o = valueType.fromString(sa[0]);
        fieldValue = new ScalarValue(o, valueType);
      } else {
        Object[] a = valueType.fromStringArray(sa);
        fieldValue = new ArrayValue(a, valueType);
      }
    }
  }

  public String getString() {
    evaluate();
    return fieldValue.getString();
  }

  public String[] getStringArray() {
    evaluate();
    return fieldValue.getStringArray();
  }

  public Object getObject() {
    evaluate();
    return fieldValue.getObject();
  }


  /**
  * Returns the value as an array of records 
  * @deprecated since ServingXML 0.8.3: replaced by {@link 
  *             LazyParameterBinding#getRecords}
  */

  @Deprecated
  public Record[] getSegments() {
    return getRecords();
  }

  /**
  * Returns the value as an array of records 
  */

  public Record[] getRecords() {
    return Record.EMPTY_ARRAY;
  }

  public ValueType getType() {
    return valueType;
  }

  public Object getSqlValue() {
    evaluate();
    return fieldValue.getSqlValue();
  }

  public int getSqlType() {
    return valueType.getSqlType();
  }
  public void writeToContentHandler(Name fieldName, 
                                    PrefixMap prefixMap, ContentHandler handler) 
  throws SAXException {
    evaluate();
    fieldValue.writeToContentHandler(fieldName, prefixMap, handler);
  }

  public boolean equalsValue(Value aValue) {
    String s = aValue.getString();
    return s.equals(getString());
  }

  public int hashCode() {
    String s = getString();
    return s.hashCode();
  }
}
