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
import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import com.servingxml.util.ServingXmlException;

public class RecordMetaDataFactory {

  private HashMap descriptorMap = new HashMap();
  private Class[] interfaces = new Class[10];
  private int interfaceCount = 0;
  private int indexCount = 0;

  public static RecordMetaDataFactory createInstance() {
    return new RecordMetaDataFactory();
  }

  public RecordMetaData createRecordMetaData() {
    Class[] a = new Class[interfaceCount];
    for (int i = 0; i < interfaceCount; ++i) {
      a[i] = interfaces[i];
    }
    return new RecordMetaData(descriptorMap,a);
  }

  protected RecordMetaDataFactory() {
  }

  public void addInterface(Class bean) {

    boolean found = false;
    for (int k = 0; k < interfaceCount; ++k) {
      if (interfaces[k].equals(bean)) {
        found = true;
        break;
      }
    }
    if (!found) {
      interfaces[interfaceCount] = bean;
      ++interfaceCount;
      try {
        BeanInfo info = Introspector.getBeanInfo(bean);
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; ++i) {
          PropertyDescriptor descriptor = descriptors[i];
          Method method = descriptor.getReadMethod();
          String name = descriptor.getName();
          Class propertyType = descriptor.getPropertyType();
          if (!descriptorMap.containsKey(method.getName())) {
            FieldDescriptor parameterDescriptor = new FieldDescriptor(method.getName(), 
              indexCount, name, propertyType);
            descriptorMap.put(method.getName(),parameterDescriptor);
            ++indexCount;
          }
        }
      } catch (Exception e) {
        ServingXmlException pe = new ServingXmlException(e.getMessage(),e);
        throw(pe);
      }
    }
  }
}
