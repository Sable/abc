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

package com.servingxml.components.recordio;

import java.util.Properties;

import com.servingxml.util.InstanceFactory;
import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.property.Property;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class CustomRecordReaderFactoryAssembler {
  
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Property[] customProperties = new Property[0];
  private Class javaClass = null;

  public void setClass(Class javaClass) {
    this.javaClass = javaClass;
  }

  public void injectComponent(Property[] customProperties) {
    this.customProperties = customProperties;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public RecordReaderFactory assemble(ConfigurationContext context) {

    if (javaClass == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"class");
      throw new ServingXmlException(message);
    }
    
    InstanceFactory instanceFactory = new InstanceFactory(javaClass, RecordReader.class);

    Properties properties = Property.toProperties(customProperties);
    
    RecordReaderFactory recordFilterFactory = new CustomRecordReaderFactory(instanceFactory,properties);
    if (parameterDescriptors.length > 0) {
      recordFilterFactory = new RecordReaderFactoryPrefilter(recordFilterFactory,parameterDescriptors);
    }

    return recordFilterFactory;
  }
}

class CustomRecordReaderFactory extends AbstractRecordReaderFactory 
implements RecordReaderFactory {
  private static final Class[] CTOR_ARG_TYPES = new Class[]{Properties.class};
  private final InstanceFactory instanceFactory;
  private final Properties properties;
  
  public CustomRecordReaderFactory(InstanceFactory instanceFactory, Properties properties) {
    this.instanceFactory = instanceFactory;
    this.properties = properties;
  }

  protected RecordReader createRecordReader(ServiceContext context, Flow flow) {

    RecordReader recordReader; 
    if (instanceFactory.hasConstructor(CTOR_ARG_TYPES)) {
      Object[] args = new Object[]{properties};
      recordReader = (RecordReader)instanceFactory.createInstance(CTOR_ARG_TYPES,args);
    } else {
      recordReader = (RecordReader)instanceFactory.createInstance();
    }
    return recordReader;
  }
}
