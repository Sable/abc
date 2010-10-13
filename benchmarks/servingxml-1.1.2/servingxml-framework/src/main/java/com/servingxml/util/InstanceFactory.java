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

package com.servingxml.util;

import java.lang.reflect.Constructor;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class InstanceFactory {

  public static final Class[] VOID_ARG_TYPES = {};
  public static final Object[] VOID_ARG_VALUES = {};

  private final Class<?> instanceClass;

  public InstanceFactory(String instanceClassName) {

    try {
      this.instanceClass = Thread.currentThread().getContextClassLoader().loadClass(instanceClassName);
    } catch (java.lang.ClassNotFoundException e){
      String s = MessageFormatter.getInstance().getMessage(ServingXmlMessages.CLASS_NOT_FOUND,
                                                           instanceClassName);
      throw new ServingXmlException(s,e);
    }
  }
  
  public boolean hasConstructor(Class[] argTypes) {

    if (instanceClass == null) {
      throw new ServingXmlException("Instance class is null.");
    }
    Constructor[] ctors = instanceClass.getConstructors();
    boolean found = false;
    for (int i = 0; !found && i < ctors.length;  ++i) {
      Constructor ctor = ctors[i];
      Class<?>[] types = ctor.getParameterTypes();
      if (types.length == argTypes.length) {
        boolean match = true;
        for (int j = 0; match && j < types.length; ++j) {
          if (!types[j].isAssignableFrom(argTypes[j])) {
            match = false;
          }
        }
        found = match;
      }
    }
    return found;
  }

  public InstanceFactory(String instanceClassName, Class<?> baseClass) {

    try {
      this.instanceClass = Thread.currentThread().getContextClassLoader().loadClass(instanceClassName);
      if (!makerOf(baseClass)) {
        String msg = "Class " + instanceClassName + " must implement " + baseClass.getName() + " interface.";
        throw new ServingXmlException(msg);
      }
    } catch (java.lang.ClassNotFoundException e){
      String s = MessageFormatter.getInstance().getMessage(ServingXmlMessages.CLASS_NOT_FOUND,
                                                           instanceClassName);
      throw new ServingXmlException(s,e);
    }
  }

  public InstanceFactory(Class<?> instanceClass) {
    this.instanceClass = instanceClass;
  }

  public InstanceFactory(Class<?> instanceClass, Class<?> baseClass) {
    this.instanceClass = instanceClass;
    if (!makerOf(baseClass)) {
      String msg = "Class " + instanceClass.getName() + " must implement " + baseClass.getName() + " interface.";
      throw new ServingXmlException(msg);
    }
  }

  public boolean makerOf(Class<?> cls) {
    return cls.isAssignableFrom(instanceClass);
  }

  public Object createInstance() {
    return createInstance(VOID_ARG_TYPES,VOID_ARG_VALUES);
  }

  public Object createInstance(Class[] argTypes, Object[] args) {

    try {
      if (instanceClass == null) {
        throw new ServingXmlException("Instance class cannot be null.");
      }
      Constructor ctor = instanceClass.getConstructor(argTypes);
      return ctor.newInstance(args);
    } catch (java.lang.NoSuchMethodException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (java.lang.InstantiationException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (java.lang.IllegalAccessException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (java.lang.reflect.InvocationTargetException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}

