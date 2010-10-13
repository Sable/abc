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

package com.servingxml.components.saxfilter;

import java.util.Properties;

import org.xml.sax.XMLFilter;

import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.InstanceFactory;
import com.servingxml.app.Flow;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;
import com.servingxml.components.content.Content;

/**
 * Factory for creating a XMLFilter. 
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CustomSaxFilterAppender extends AbstractXmlFilterAppender implements Content {
  private static final Class[] CTOR_ARG_TYPES = new Class[]{Properties.class};
  private final InstanceFactory instanceFactory;
  private final Properties properties;

  public CustomSaxFilterAppender(InstanceFactory instanceFactory, Properties properties) {
    this.instanceFactory = instanceFactory;
    this.properties = properties;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
  XmlFilterChain pipeline) {
    XMLFilter filter = createXmlFilter(context, flow);
    pipeline.addXmlFilter(filter);
  }

  public XMLFilter createXmlFilter(ServiceContext context, Flow flow) {
    XMLFilter filter;
    if (instanceFactory.hasConstructor(CTOR_ARG_TYPES)) {
      Object[] args = new Object[]{properties};
      filter = (XMLFilter)instanceFactory.createInstance(CTOR_ARG_TYPES,args);
    } else {
      filter = (XMLFilter)instanceFactory.createInstance();
    }
    return filter;
  }
}

