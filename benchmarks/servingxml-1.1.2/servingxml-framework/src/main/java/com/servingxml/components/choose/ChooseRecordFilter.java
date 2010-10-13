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

package com.servingxml.components.choose;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.AbstractRecordFilter;
import com.servingxml.components.recordio.RecordPipeline;
import com.servingxml.components.recordio.RecordFilter;
import com.servingxml.components.recordio.MultipleRecordFilter;
import com.servingxml.components.recordio.RecordPipelineAppender;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.SplitRecordReader;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.recordio.RecordWriterFilterAdaptor;
import com.servingxml.components.recordio.JoinRecordWriter;
import com.servingxml.app.Flow;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.XsltChooser;

/**
 *
 * 
 * @author  Daniel A. Parker
 */


class ChooseRecordFilter extends AbstractRecordFilter {
  private final Alternative[] alternatives;
  private final XsltChooser chooser;
  private RecordFilter[] recordFilters;

  public ChooseRecordFilter(Alternative[] alternatives, XsltChooser chooser) {
    this.alternatives = alternatives;
    this.chooser = chooser;
    this.recordFilters = new RecordFilter[alternatives.length];
  }

  public void writeRecord(ServiceContext context, Flow flow) {

    //System.out.println(getClass().getName()+".writeRecord enter");
    Source source = new SAXSource(flow.getDefaultSaxSource().createXmlReader(),new InputSource());
    int index = chooser.choose(source, flow.getParameters());
    if (index >= 0 && index < alternatives.length) {
      RecordFilter recordFilter = recordFilters[index];
      if (recordFilter != null) {
        //System.out.println(getClass().getName()+".writeRecord before execute " + recordPipeline.getClass().getName());
        recordFilter.writeRecord(context, flow);
      }
    } else {
      super.writeRecord(context, flow);
    }
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    for (int i = 0; i < alternatives.length; ++i) {
      RecordPipelineAppender[] recordPipelineAppenders = alternatives[i].getRecordPipelineAppenders();
      if (recordPipelineAppenders.length > 0) {
        //RecordReader recordReader = new SplitRecordReader();
        //RecordPipeline subPipeline = new RecordPipeline(recordReader);
        MultipleRecordFilter multipleRecordFilter = new MultipleRecordFilter();
        for (int j = 0; j < recordPipelineAppenders.length; ++j) {
          RecordPipelineAppender recordPipelineAppender = recordPipelineAppenders[j];
          recordPipelineAppender.appendToRecordPipeline(context, flow, multipleRecordFilter);
        }
        //subPipeline.addRecordFilter(new RecordWriterFilterAdaptor(new JoinRecordWriter(this)));
        multipleRecordFilter.addRecordFilter(new RecordWriterFilterAdaptor(new JoinRecordWriter(getRecordWriter())));
        recordFilters[i] = multipleRecordFilter;
        multipleRecordFilter.startRecordStream(context, flow);
      } else {
        recordFilters[i] = null;
      }
    }
    super.startRecordStream(context, flow);
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    for (int i = 0; i < recordFilters.length; ++i) {
      if (recordFilters[i] != null) {
        recordFilters[i].endRecordStream(context, flow);
      }
    }
    super.endRecordStream(context, flow);
  }

  public void close() {
    ServingXmlException badDispose = null;
    try {
      super.close();
    } catch (ServingXmlException e) {
      badDispose = e;
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(),e);
    }
    for (int i = 0; i < recordFilters.length; ++i) {
      try {
        if (recordFilters[i] != null) {
          recordFilters[i].close();
        }
      } catch (ServingXmlException e) {
        badDispose = e;
      } catch (Exception e) {
        badDispose = new ServingXmlException(e.getMessage(),e);
      }
    }
    if (badDispose != null) {
      throw badDispose;
    }
  }
}

