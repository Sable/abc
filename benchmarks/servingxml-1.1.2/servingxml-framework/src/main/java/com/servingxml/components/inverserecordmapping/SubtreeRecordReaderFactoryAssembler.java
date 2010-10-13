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

package com.servingxml.components.inverserecordmapping;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.components.recordio.RecordReaderFactoryPrefilter;
import com.servingxml.components.content.Content;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.content.DefaultDocument;

/**
 * The <code>SubtreeRecordReaderFactoryAssembler</code> implements an assembler for
 * building <code>RecordReaderFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SubtreeRecordReaderFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Content[] xmlComponents = new Content[0];
  private InverseRecordMapping inverseRecordMapping = null;
  private XsltConfiguration xsltConfiguration;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(Content[] xmlComponents) {
    this.xmlComponents = xmlComponents;
  }

  public void injectComponent(InverseRecordMapping inverseRecordMapping) {
    this.inverseRecordMapping = inverseRecordMapping;
  }
  
  public RecordReaderFactory assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }
    
    if (inverseRecordMapping == null) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"sx:inverseRecordMapping");
      throw new ServingXmlException(msg);
    }

    RecordReaderFactory readerFactory = new SubtreeRecordReaderFactory(xmlComponents,
      xsltConfiguration.getOutputPropertyFactories(), inverseRecordMapping);
    if (parameterDescriptors.length > 0) {
      readerFactory = new RecordReaderFactoryPrefilter(readerFactory,parameterDescriptors);
    }
    return readerFactory;
  }
}

