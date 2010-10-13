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

import org.xml.sax.XMLFilter;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.components.saxfilter.ContentXmlFilter;
import com.servingxml.components.saxsource.SaxSourceFactory;
import com.servingxml.components.saxsource.SaxSourceFactoryAdaptor;
import com.servingxml.components.streamsource.DefaultStreamSourceFactory;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.io.saxsource.SaxSource;

/**
 * A <code>Document</code> instance may be used to obtain objects that
 * supply XML content as an input stream.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class Document extends AbstractContent implements Content {     

  private final SaxSourceFactory saxSourceFactory;

  public Document(OutputPropertyFactory[] defaultOutputProperties) {
    super(defaultOutputProperties);

    StreamSourceFactory streanSourceFactory = new DefaultStreamSourceFactory();
    this.saxSourceFactory = new SaxSourceFactoryAdaptor(streanSourceFactory);
  }

  public Document(SaxSourceFactory saxSourceFactory, 
                         OutputPropertyFactory[] defaultOutputProperties) {
    super(defaultOutputProperties);

    this.saxSourceFactory = saxSourceFactory;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    SaxSource source = saxSourceFactory.createSaxSource(context, flow);
    //System.out.println(getClass().getName()+".createSaxSource \n"+source.toString());
    return source;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow, XmlFilterChain pipeline) {
    SaxSource saxSource = saxSourceFactory.createSaxSource(context, flow);
    XMLFilter filter = new ContentXmlFilter(context,flow,saxSource);
    pipeline.addXmlFilter(filter);
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = new XmlPipeline(/*defaultOutputProperties*/);
    SaxSource saxSource = saxSourceFactory.createSaxSource(context, flow);
    pipeline.setSaxSource(saxSource);
    return pipeline;
  }
}

