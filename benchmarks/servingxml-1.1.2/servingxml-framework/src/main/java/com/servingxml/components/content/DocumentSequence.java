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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.Name;
import com.servingxml.util.QnameContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.io.saxsink.SaxSink;

/**
 * A <code>DocumentSequence</code> instance may be used to obtain objects that
 * supply XML content as an input stream.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DocumentSequence implements Content {     

  private final QnameContext nameContext;
  private final NameSubstitutionExpr documentElementNameEvaluator;
  private final RecordReaderFactory recordReaderFactory;
  private final Content contentFactory;

  public DocumentSequence(QnameContext nameContext, 
                                 NameSubstitutionExpr documentElementNameEvaluator,
                                 RecordReaderFactory recordReaderFactory, 
                                 Content contentFactory) {

    this.nameContext = nameContext;
    this.documentElementNameEvaluator = documentElementNameEvaluator;
    this.recordReaderFactory = recordReaderFactory;
    this.contentFactory = contentFactory;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    Name documentElementName = documentElementNameEvaluator.evaluateName(flow.getParameters(),flow.getRecord());
    String documentElementQname = documentElementName.toQname(nameContext);
    SaxSource source = new DocumentSequenceSaxSource(context, flow, 
                                                     documentElementName, 
                                                     documentElementQname,
                                                     recordReaderFactory, 
                                                     contentFactory);
    return source;
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = new XmlPipeline(/*defaultOutputProperties*/);
    SaxSource saxSource = createSaxSource(context, flow);
    pipeline.setSaxSource(saxSource);
    return pipeline;
  }

  public String createString(ServiceContext context, Flow flow) {
    return contentFactory.createString(context,flow);
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow, XmlFilterChain pipeline) {
    SaxSource saxSource = createSaxSource(context, flow);
    pipeline.setSaxSource(saxSource);
  }

  public void execute(ServiceContext context, Flow flow) {

    SaxSink saxSink = null;
    //System.out.println(getClass().getName()+".execute enter saxSinkFactory=" + saxSinkFactory.getClass().getName());

    try {
      saxSink = flow.getDefaultSaxSink();
      flow = flow.replaceDefaultSaxSink(context, saxSink);
      XmlPipeline pipeline = createXmlPipeline(context,flow);
      saxSink.setOutputProperties(pipeline.getOutputProperties());
      pipeline.execute(saxSink.getContentHandler());
    } finally {
      if (saxSink != null) {
        saxSink.close();
      }
    }
  }
}

