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
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.components.streamsink.DefaultStreamSinkFactory;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.saxsink.SaxSinkFactory;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class XsltSerializerFactoryAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private StreamSinkFactory sinkFactory = new DefaultStreamSinkFactory();
  private OutputPropertyFactory[] outputPropertyFactories = new OutputPropertyFactory[0];

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(StreamSinkFactory sinkFactory) {
    this.sinkFactory = sinkFactory;
  }

  public void injectComponent(OutputPropertyFactory[] outputPropertyFactories) {

    this.outputPropertyFactories = outputPropertyFactories;      
  }

  public SaxSinkFactory assemble(ConfigurationContext context) {

    SaxSinkFactory saxSinkFactory = new XsltSerializerFactory(sinkFactory, outputPropertyFactories);
    if (parameterDescriptors.length > 0) {
      saxSinkFactory = new SerializerFactoryPrefilter(saxSinkFactory, parameterDescriptors); 
    }

    return saxSinkFactory;
  }
}
