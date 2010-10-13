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

package com.servingxml.components.sql;

import java.util.Properties;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.InstanceFactory;
import com.servingxml.components.property.Property;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;

/**
 * The <code>JdbcConnectionPoolAssembler</code> implements an assembler for
 * assembling system <code>JdbcConnectionPool</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CustomJdbcConnectionPoolAssembler {
  private static final Class[] CTOR_ARG_TYPES = new Class[]{Properties.class};

  private Class javaClass = null;
  private Property[] customProperties = new Property[0];

  public void setClassName(Class javaClass) {
    this.javaClass = javaClass;
  }

  public void setClass(Class javaClass) {
    this.javaClass = javaClass;
  }

  public void injectComponent(Property[] customProperties) {
    this.customProperties = customProperties;
  }

  public JdbcConnectionPool assemble(ConfigurationContext context) {

    if (javaClass == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"class");
      throw new ServingXmlException(message);
    }

    try {
      InstanceFactory instanceFactory = new InstanceFactory(javaClass, JdbcConnectionPool.class);

      JdbcConnectionPool connectionPool;
      if (instanceFactory.hasConstructor(CTOR_ARG_TYPES)) {
        Properties properties = Property.toProperties(customProperties);
        Object[] args = new Object[]{properties};
        connectionPool = (JdbcConnectionPool)instanceFactory.createInstance(CTOR_ARG_TYPES,args);
      } else {
        connectionPool = (JdbcConnectionPool)instanceFactory.createInstance();
      }

      return connectionPool;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}
