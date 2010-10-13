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

package com.servingxml.components.flatfile.layout;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.components.streamsink.DefaultStreamSinkFactory;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.recordio.RecordWriterFactoryPrefilter;
import com.servingxml.components.recordio.RecordWriterFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class FlatFileWriterFactoryAssembler extends FlatFileOptionsFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private FlatFile flatFile = null;
  private StreamSinkFactory sinkFactory = new DefaultStreamSinkFactory();

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(FlatFile flatFile) {
    this.flatFile = flatFile;
  }

  public void injectComponent(StreamSinkFactory sinkFactory) {
    this.sinkFactory = sinkFactory;
  }

  public RecordWriterFactory assemble(ConfigurationContext context) {
    //System.out.println(getClass().getName()+".assemble enter");

    try {
      RecordWriterFactory recordWriterFactory = null;
      FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);
      if (flatFile == null) {
        //System.out.println(getClass().getName()+".assemble create default flat file writer factory");
        recordWriterFactory = new DefaultFlatFileWriterFactory(flatFileOptionsFactory, sinkFactory);
      } else {
        recordWriterFactory = new FlatFileWriterFactory(flatFileOptionsFactory, flatFile, sinkFactory);
      }
      if (parameterDescriptors.length > 0) {
        recordWriterFactory = new RecordWriterFactoryPrefilter(recordWriterFactory,parameterDescriptors);
      }
      return recordWriterFactory;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}
