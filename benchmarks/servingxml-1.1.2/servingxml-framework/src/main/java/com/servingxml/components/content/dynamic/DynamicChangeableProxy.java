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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import com.servingxml.components.content.Cacheable;
import com.servingxml.util.ServingXmlException;

/**
 * Defines an interface for resource dynamic XML content.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class DynamicChangeableProxy implements DynamicChangeable {

  private final Cacheable cacheable;
  private Method getLastModifiedMethod = null;
  private Class parametersType = null;

  public DynamicChangeableProxy(Cacheable cacheable) {

    this.cacheable = cacheable;

    Class getHandlerClass = cacheable.getClass();
    Method[] methods = getHandlerClass.getMethods();
    for (int i = 0; i < methods.length; ++i) {
      Method method = methods[i];
      if (method.getName().equals("getLastModified")) {
        Class[] parameterTypes = method.getParameterTypes();
        Class parametersType = parameterTypes[0];
        if (!parametersType.isInterface()) {
          throw new ServingXmlException("First parameter of getLastModified is not an interface.");
        }
        Class timestampType = parameterTypes[1];
        if (!long.class.equals(timestampType)) {
          throw new ServingXmlException("Second parameter of getLastModified does not have type long.");
        }
        Class elapsedType = parameterTypes[2];
        if (!long.class.equals(timestampType)) {
          throw new ServingXmlException("Third parameter of getLastModified does not have type long.");
        }
        getLastModifiedMethod = method;
        parametersType = parameterTypes[0];

        break;
      }
    }
    if (getLastModifiedMethod == null) {
      String msg = "Class " + getHandlerClass.getName() + " must have getLastModified method."; 
      throw new ServingXmlException(msg);
    }
  }

  public long getLastModified(Object parameters, long timestamp, long elapsed) {
    long lastModified = -1;
    try {
      Object[] args = new Object[]{parameters, new Long(timestamp),new Long(elapsed)};
      Long l = (Long)getLastModifiedMethod.invoke(cacheable,args);
      lastModified = l.intValue();
    } catch (InvocationTargetException e) {
      ServingXmlException sxe = ServingXmlException.fromInvocationTargetException(e);
      throw sxe;
    } catch (java.lang.IllegalAccessException e) {
      throw new ServingXmlException(e.getMessage(), e);
    } 
    return lastModified;
  }
}
