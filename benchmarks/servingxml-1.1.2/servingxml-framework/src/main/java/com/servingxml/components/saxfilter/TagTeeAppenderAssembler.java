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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.content.AbstractContent;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.ContentPrefilter;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.expr.ExpressionException;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.ioc.components.ConfigurationContext;
import java.util.Properties;

public class TagTeeAppenderAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Content[] xmlComponents = new Content[0];
  private SaxSinkFactory saxSinkFactory = SaxSinkFactory.NULL;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Content[] xmlComponents) {
    this.xmlComponents = xmlComponents;
  }

  public void injectComponent(SaxSinkFactory saxSinkFactory) {
    this.saxSinkFactory = saxSinkFactory;
  }

  public Content assemble(ConfigurationContext context) {

    if (parameterDescriptors.length > 0) {
      Content appender = new XmlFilterAppenderPrefilter(xmlComponents,parameterDescriptors);
      xmlComponents = new Content[]{appender};
    }

    Content filterFactory = new TagTeeAppender(xmlComponents, saxSinkFactory);
    if (parameterDescriptors.length > 0) {
      filterFactory = new ContentPrefilter(filterFactory,parameterDescriptors);
    }
    return filterFactory;
  }
}


