/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Includes contributed code by Gordon Zhang for string interface support
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


package com.servingxml.components.choose;

import com.servingxml.app.Flow;                                           
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.components.recordio.RecordFilter;
import com.servingxml.components.recordio.RecordFilterPrefilter;
import com.servingxml.components.recordio.RecordFilterAppender;
import com.servingxml.components.recordio.RecordFilterChain;
import com.servingxml.components.recordio.RecordPipeline;
import com.servingxml.components.recordmapping.MapXml;
import com.servingxml.components.recordmapping.RecordMapPrefilter;
import com.servingxml.components.recordmapping.MapXmlFactory;
import com.servingxml.io.cache.Key;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.XmlReaderSaxSource;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Record;    
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.app.ParameterDescriptor;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ChoosePrefilter 
implements Choose {
  private final Choose choose;
  private final ParameterDescriptor[] parameterDescriptors;

  public ChoosePrefilter(Choose choose, ParameterDescriptor[] parameterDescriptors) {
    this.choose = choose;
    this.parameterDescriptors = parameterDescriptors;
  }

  public RecordPipeline createRecordPipeline(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    return choose.createRecordPipeline(context,newFlow);
  }

  //  Fix by Gordon Zhang
  public String createString(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    return choose.createString(context,newFlow);
  }

  public void createString(ServiceContext context, Flow flow, StringBuilder buf) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    choose.createString(context,newFlow,buf);
  }

  public void execute(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    choose.execute(context,newFlow);
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
                               XmlFilterChain pipeline) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    choose.appendToXmlPipeline(context,newFlow,pipeline);
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
                                     RecordFilterChain pipeline) {

    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    RecordFilter prefilter = new RecordFilterPrefilter(parameterDescriptors);
    pipeline.addRecordFilter(prefilter);
    choose.appendToRecordPipeline(context,newFlow,pipeline);
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    return choose.createSaxSource(context,newFlow);
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    Flow newFlow = flow.augmentParameters(context, parameterDescriptors);
    return choose.createXmlPipeline(context,newFlow);
  }

  public MapXml createMapXml(ServiceContext context) {
    //System.out.println(getClass().getName()+".createMapXml");
    //for (int i = 0; i < parameterDescriptors.length; ++i) {
      //System.out.println(" " + parameterDescriptors[i].getName());
    //}
    MapXml recordMap = choose.createMapXml(context);
    return new RecordMapPrefilter(recordMap, parameterDescriptors); 
  }

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory recordTemplatesFactory) {
    choose.addToXsltEvaluator(mode,recordTemplatesFactory);
  }

  public boolean isGroup() {
    return choose.isGroup();
  }

  public boolean isRecord() {
    return choose.isRecord();
  }
}

