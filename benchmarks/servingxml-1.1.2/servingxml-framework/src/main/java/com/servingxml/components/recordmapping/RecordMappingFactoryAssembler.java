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

package com.servingxml.components.recordmapping;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.content.Content;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.xsltconfig.XsltConfiguration;

/**
 * The <code>RecordMappingFactoryAssembler</code> implements an assembler for
 * assembling <code>MapXmlFactory</code> objects.
 *
 *                                               
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordMappingFactoryAssembler {
 
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private MapXmlFactory[] childFactories = new MapXmlFactory[0];
  private Content contentFactory = null;
  private XsltConfiguration xsltConfiguration;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }
  
  public void injectComponent(MapXmlFactory[] childFactories) {
    this.childFactories = childFactories;
  }

  public void injectComponent(Content contentFactory) {
    this.contentFactory = contentFactory;
  }
                                                       
  public RecordMappingFactory assemble(ConfigurationContext context) {

    if (childFactories.length == 0 && contentFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_LITERAL_CONTENT_REQUIRED,
                                                                 context.getElement().getTagName());
      throw new ServingXmlException(message);
    }

    try {
      if (xsltConfiguration == null) {
        xsltConfiguration = XsltConfiguration.getDefault();
      }
      if (contentFactory != null) {
        childFactories = new MapXmlFactory[1];
        childFactories[0] = new MapXmlFactoryAdaptor(contentFactory);
      }
      MapXmlFactory rmf;
      if (childFactories.length == 1) {
        rmf = childFactories[0];
      } else {
        rmf = new MultipleMapXmlFactory(context.getQnameContext(), xsltConfiguration, childFactories);
      }

      RecordMappingFactory recordMappingFactory = new RecordMappingFactoryImpl(rmf);
      if (parameterDescriptors.length > 0) {
        recordMappingFactory = new RecordMappingFactoryPrefilter(recordMappingFactory,parameterDescriptors);
      }

      return recordMappingFactory;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}
