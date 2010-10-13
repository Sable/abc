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

package com.servingxml.components.serializer;

import java.util.Properties;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.InstanceFactory;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.property.Property;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.components.saxsink.SaxSinkFactory;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class CustomSerializerFactoryAssembler {
  private static final Class[] CTOR_ARG_TYPES = new Class[]{Properties.class};

  private Class javaClass = null;
  private Property[] customProperties = new Property[0];

  public void setClass(Class javaClass) {
    this.javaClass = javaClass;
  }

  public void injectComponent(Property[] customProperties) {
    this.customProperties = customProperties;
  }

  public SaxSinkFactory assemble(ConfigurationContext context) {

    if (javaClass == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"class");
      throw new ServingXmlException(message);
    }
    
    InstanceFactory instanceFactory = new InstanceFactory(javaClass, SaxSink.class);
    Properties properties = Property.toProperties(customProperties);

    SaxSinkFactory saxSinkFactory = new CustomSerializerFactory(instanceFactory,properties);

    return saxSinkFactory;
  }
}
