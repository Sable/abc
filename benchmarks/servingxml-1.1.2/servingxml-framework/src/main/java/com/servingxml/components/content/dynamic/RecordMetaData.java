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

import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.Proxy;

import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.record.Record;

public class RecordMetaData {

  private final HashMap descriptorMap;
  private final Class[] interfaces;
  private final FieldDescriptor[] descriptors;

  public RecordMetaData(HashMap descriptorMap, Class[] interfaces) {
    this.descriptorMap = descriptorMap;
    this.interfaces = interfaces;
    this.descriptors = new FieldDescriptor[descriptorMap.size()];
    Iterator iter = descriptorMap.values().iterator();
    for (int i = 0; iter.hasNext(); ++i) {
      FieldDescriptor descriptor = (FieldDescriptor)iter.next();
      descriptors[i] = descriptor;
    }
  }

  public FieldDescriptor[] getFieldDescriptors() {
    return descriptors;
  }

  public Object createRecordProxy(Record record) {
    Object[] data = new Object[descriptorMap.size()];

    for (int i = 0; i < descriptors.length; ++i) {
      FieldDescriptor descriptor = descriptors[i];
      Name fieldName = new QualifiedName(descriptor.getPropertyName());
      Object value = descriptor.getValue(fieldName,record);
      data[descriptor.getIndex()] = value;
    }

    RecordInterfaceAdapter proxyFactory = RecordInterfaceAdapter.createInstance(data,this);

    return Proxy.newProxyInstance(getClass().getClassLoader(), interfaces, proxyFactory);
  }

  public FieldDescriptor getFieldDescriptor(String accessorName) {
    FieldDescriptor descriptor = (FieldDescriptor)descriptorMap.get(accessorName);
    return descriptor;
  }
}
