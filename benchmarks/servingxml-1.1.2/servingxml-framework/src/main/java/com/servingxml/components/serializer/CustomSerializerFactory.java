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
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.InstanceFactory;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.components.serializer.XsltSerializer;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.streamsink.StreamSink;

/**
 * 
 * @author  Daniel A. Parker
 */

public class CustomSerializerFactory implements SaxSinkFactory {
  private static final Class[] CTOR_ARG_TYPES = new Class[]{Properties.class};
  private final InstanceFactory instanceFactory;
  private final Properties properties;

  public CustomSerializerFactory(InstanceFactory instanceFactory, Properties properties) {
    this.instanceFactory = instanceFactory;
    this.properties = properties;
  }

  public SaxSink createSaxSink(ServiceContext context, Flow flow) {
    SaxSink serializer;
    if (instanceFactory.hasConstructor(CTOR_ARG_TYPES)) {
      Object[] args = new Object[]{properties};
      serializer = (SaxSink)instanceFactory.createInstance(CTOR_ARG_TYPES,args);
    } else {
      serializer = (SaxSink)instanceFactory.createInstance();
    }

    return serializer;
  }
}
