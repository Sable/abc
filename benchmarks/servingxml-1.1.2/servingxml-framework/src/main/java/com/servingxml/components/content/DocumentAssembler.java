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

package com.servingxml.components.content;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.saxsource.SaxSourceFactory;
import com.servingxml.components.saxsource.SaxSourceFactoryAdaptor;
import com.servingxml.components.streamsource.DefaultStreamSourceFactory;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.xsltconfig.XsltConfiguration;

/**
 * The <code>DocumentAssembler</code> implements an assembler for
 * assembling system <code>Content</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DocumentAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private SaxSourceFactory saxSourceFactory = null;
  private XsltConfiguration xsltConfiguration;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(StreamSourceFactory streamSourceFactory) {
    this.saxSourceFactory = new SaxSourceFactoryAdaptor(streamSourceFactory);
  }

  public void injectComponent(SaxSourceFactory saxSourceFactory) {
    this.saxSourceFactory = saxSourceFactory;
  }

  public Content assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (saxSourceFactory == null) {
      saxSourceFactory = new SaxSourceFactoryAdaptor(new DefaultStreamSourceFactory());
    }
    
    Content contentFactory = new Document(saxSourceFactory,
      xsltConfiguration.getOutputPropertyFactories());
    if (parameterDescriptors.length > 0) {
      contentFactory = new ContentPrefilter(contentFactory,parameterDescriptors);
    }
    return contentFactory;
  }
}
