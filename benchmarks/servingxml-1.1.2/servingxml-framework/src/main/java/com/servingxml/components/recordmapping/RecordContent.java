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

import org.xml.sax.XMLReader;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.AbstractContent;
import com.servingxml.components.content.Content;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.components.recordio.RecordFilterAppender;
import com.servingxml.components.recordio.RecordFilterChain;
import com.servingxml.components.recordio.RecordPipeline;
import com.servingxml.components.recordio.RecordPipelineAppender;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;

/**
 * A <code>RecordContent</code> instance may be used to obtain objects that
 * supply XML content as an input stream.
 *
 *                                           
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordContent extends AbstractContent 
implements Content, RecordFilterAppender {     

  private final RecordPipelineAppender[] recordPipelineAppenders;
  private final RecordMappingFactory recordMapFactory;

  public RecordContent(RecordPipelineAppender[] recordPipelineAppenders, 
    RecordMappingFactory recordMapFactory, OutputPropertyFactory[] outputPropertyFactories) {
    super(outputPropertyFactories);

    this.recordPipelineAppenders = recordPipelineAppenders;
    this.recordMapFactory = recordMapFactory;
  }                             

  public RecordPipeline createRecordPipeline(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".createRecordPipeline");

    RecordPipelineAppender content = recordPipelineAppenders[0];
    //System.out.println(getClass().getName()+".createRecordPipeline " + content.getClass().getName());
    RecordPipeline pipeline = content.createRecordPipeline(context, flow);
    for (int i = 1; i < recordPipelineAppenders.length; ++i) {          
      RecordPipelineAppender recordPipelineAppender = recordPipelineAppenders[i];
      recordPipelineAppender.appendToRecordPipeline(context,flow,pipeline);
    }
    return pipeline;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
                                     RecordFilterChain pipeline) {

    for (int i = 0; i < recordPipelineAppenders.length; ++i) {
      RecordPipelineAppender recordPipelineAppender = recordPipelineAppenders[i];
      recordPipelineAppender.appendToRecordPipeline(context,flow,pipeline);
    }
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {

   //System.out.println(getClass().getName()+".createSaxSource enter");

    if (flow.getRecord() != null) {
      //System.out.println("RecordContent.createContent currentRecord = " + flow.getRecord());
    }

    SaxSource content;            
    MapXml recordMap = recordMapFactory.createMapXml(context);

    if (recordPipelineAppenders.length > 0) {
      RecordPipeline recordPipeline = createRecordPipeline(context, flow);
      XMLReader reader = new RecordStreamXmlReader(context, recordPipeline, recordMap);
      content = new RecordSaxSource(reader, recordPipeline.getExpirable(), recordPipeline.getKey(),
        context.getTransformerFactory());
    } else {
      XMLReader reader = new RecordMappingXmlReader(context, flow, recordMap);
      Key key = DefaultKey.newInstance();
      content = new RecordSaxSource(reader, Expirable.IMMEDIATE_EXPIRY, key, context.getTransformerFactory());
    }

   //System.out.println(getClass().getName()+".createSaxSource leave");
    return content;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow, XmlFilterChain pipeline) {
    SaxSource saxSource = createSaxSource(context, flow);
    pipeline.setSaxSource(saxSource);
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".createXmlPipeline");
    XmlPipeline pipeline = new XmlPipeline(createDefaultOutputProperties(context,flow));
    SaxSource saxSource = createSaxSource(context, flow);
    pipeline.setSaxSource(saxSource);
    return pipeline;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("RecordContent:  ");

    return buf.toString();
  }                                       
}


