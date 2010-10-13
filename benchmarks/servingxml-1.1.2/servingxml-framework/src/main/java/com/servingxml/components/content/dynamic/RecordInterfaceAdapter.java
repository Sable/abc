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

package com.servingxml.components.content.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Adapts calls to getter and setter methods on a Java interface to 
 * reading and writing fields in a record.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class RecordInterfaceAdapter implements InvocationHandler {


  private final Object[] data;
  private final RecordMetaData recordMetaData;

  public static RecordInterfaceAdapter createInstance(Object[] data, 
  RecordMetaData recordMetaData) {
    return new RecordInterfaceAdapter(data,recordMetaData);
  }

  protected RecordInterfaceAdapter(Object[] data, RecordMetaData recordMetaData) {
    this.recordMetaData = recordMetaData;
    this.data = data;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    String methodName = method.getName();
    Object result = null;

    FieldDescriptor descriptor = recordMetaData.getFieldDescriptor(methodName);
    if (descriptor != null) {
      String propertyName = descriptor.getPropertyName();
      result = data[descriptor.getIndex()];
    }

    return result;
  }
}

