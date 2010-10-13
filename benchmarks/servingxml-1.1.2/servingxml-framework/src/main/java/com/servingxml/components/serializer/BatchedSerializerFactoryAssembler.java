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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.components.streamsink.DefaultStreamSinkFactory;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.expr.saxpath.RestrictedMatchParser;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class BatchedSerializerFactoryAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private String path = null;
  private long batchSize = -1;
  private int maxFiles = Integer.MAX_VALUE;
  private SaxSinkFactory saxSinkFactory = null;
  private StreamSinkFactory sinkFactory = new DefaultStreamSinkFactory();

  public void setPath(String path) {
    this.path = path;
  }

  public void setBatchSize(long batchSize) {
    this.batchSize = batchSize;
  }

  public void setMaxFiles(int maxFiles) {
    this.maxFiles = maxFiles;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(SaxSinkFactory saxSinkFactory) {
    this.saxSinkFactory = saxSinkFactory;
  }

  public SaxSinkFactory assemble(ConfigurationContext context) {

    try {
      if (path == null) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                                   context.getElement().getTagName(),"path");
        throw new ServingXmlException(message);
      }
      if (batchSize == -1) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                                   context.getElement().getTagName(),"batchSize");
        throw new ServingXmlException(message);
      }
      if (saxSinkFactory == null) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                                                                   context.getElement().getTagName(),"sx:saxSink");
        throw new ServingXmlException(message);
      }
      RestrictedMatchParser parser = new RestrictedMatchParser(context.getQnameContext(),path);
      RestrictedMatchPattern expr = parser.parse();

      SaxSinkFactory batchedFactory = new BatchedSerializerFactory(saxSinkFactory, expr, batchSize, maxFiles);
      if (parameterDescriptors.length > 0) {
        batchedFactory = new SerializerFactoryPrefilter(batchedFactory, parameterDescriptors); 
      }
      return batchedFactory;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}
