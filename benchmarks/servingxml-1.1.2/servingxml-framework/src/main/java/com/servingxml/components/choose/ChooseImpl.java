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

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import com.servingxml.app.Flow;                                           
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.components.recordio.RecordFilter;
import com.servingxml.components.recordio.RecordFilterAppender;
import com.servingxml.components.recordio.RecordFilterChain;
import com.servingxml.components.recordio.RecordPipeline;
import com.servingxml.components.recordmapping.MapXml;
import com.servingxml.components.recordmapping.MapXmlFactory;
import com.servingxml.io.cache.Key;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.XmlReaderSaxSource;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;    
import com.servingxml.util.record.Record;    
import com.servingxml.util.xml.XsltChooser;
import com.servingxml.util.xml.XsltChooserFactory;
import com.servingxml.util.xml.XsltEvaluatorFactory;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ChooseImpl implements Choose {

  private final Alternative[] alternatives; 
  private final XsltChooserFactory chooserFactory;

  public ChooseImpl(Alternative[] alternatives, XsltChooserFactory chooserFactory) {
    this.alternatives = alternatives;
    this.chooserFactory = chooserFactory;
  }

  public RecordPipeline createRecordPipeline(ServiceContext context, Flow flow) {
    throw new ServingXmlException("Record stream must begin with a reader.");
  }

  //  Fix by Gordon Zhang
  public String createString(ServiceContext context, Flow flow) {
    String s = null;
    Source source = new SAXSource(flow.getDefaultSaxSource().createXmlReader(),new InputSource());
    XsltChooser chooser = chooserFactory.createXsltChooser();
    chooser.setUriResolverFactory(context.getUriResolverFactory());
    int index = chooser.choose(source, flow.getParameters());
    if (index >= 0 && index < alternatives.length) {
      s = alternatives[index].createString(context, flow);
    }
    return s == null ? "" : s;
  }

  public void createString(ServiceContext context, Flow flow, StringBuilder buf) {
    Source source = new SAXSource(flow.getDefaultSaxSource().createXmlReader(),new InputSource());
    XsltChooser chooser = chooserFactory.createXsltChooser();
    chooser.setUriResolverFactory(context.getUriResolverFactory());
    int index = chooser.choose(source, flow.getParameters());
    if (index >= 0 && index < alternatives.length) {
      String s = alternatives[index].createString(context, flow);
      buf.append(s);
    }
  }

  public void execute(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".execute enter");
    Source source = new SAXSource(flow.getDefaultSaxSource().createXmlReader(),new InputSource());
    XsltChooser chooser = chooserFactory.createXsltChooser();
    chooser.setUriResolverFactory(context.getUriResolverFactory());
    int index = chooser.choose(source, flow.getParameters());
    if (index >= 0 && index < alternatives.length) {
      alternatives[index].execute(context, flow);
    }
    //System.out.println(getClass().getName()+".execute leave");
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
                               XmlFilterChain pipeline) {
    //System.out.println(getClass().getName()+".appendToXmlPipeline enter");

    XsltChooser chooser = chooserFactory.createXsltChooser();
    chooser.setUriResolverFactory(context.getUriResolverFactory());
    ChooseXmlFilter filter = new ChooseXmlFilter(context,flow,alternatives,chooser);
    pipeline.addXmlFilter(filter);
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
                                     RecordFilterChain pipeline) {

    XsltChooser chooser = chooserFactory.createXsltChooser();
    chooser.setUriResolverFactory(context.getUriResolverFactory());
    RecordFilter recordFilter = new ChooseRecordFilter(alternatives, chooser);
    pipeline.addRecordFilter(recordFilter);
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {
    SaxSource saxSource = null;
    Source source = new SAXSource(flow.getDefaultSaxSource().createXmlReader(),new InputSource());
    XsltChooser chooser = chooserFactory.createXsltChooser();
    chooser.setUriResolverFactory(context.getUriResolverFactory());
    int index = chooser.choose(source, flow.getParameters());
    if (index >= 0 && index < alternatives.length) {
      saxSource = alternatives[index].createSaxSource(context, flow);
    }
    return saxSource;
  }

  public XmlPipeline createXmlPipeline(ServiceContext context, Flow flow) {
    XmlPipeline pipeline = null;
    Source source = new SAXSource(flow.getDefaultSaxSource().createXmlReader(),new InputSource());
    XsltChooser chooser = chooserFactory.createXsltChooser();
    chooser.setUriResolverFactory(context.getUriResolverFactory());
    int index = chooser.choose(source, flow.getParameters());
    if (index >= 0 && index < alternatives.length) {
      pipeline = alternatives[index].createXmlPipeline(context, flow);
    }
    return pipeline;
  }

  public MapXml createMapXml(ServiceContext context) {
    //System.out.println(getClass().getName()+".createMapXml");

    MapXml[] recordMaps = new MapXml[alternatives.length];
    boolean group = false;

    int index = 0;
    for (int i = 0; i < alternatives.length; ++i) {
      MapXmlFactory recordMapFactory = alternatives[i].getRecordMapFactory();
      if (recordMapFactory != null) {
        //System.out.println("Create record map");
        // Fix to indexing from Gordon Zhang
        recordMaps[i] = recordMapFactory.createMapXml(context);
        if (recordMapFactory.isGroup() || recordMapFactory.isRecord()) {
          group = true;
        }
      } else {
        recordMaps[i] = null;
      }
    }
    //System.out.println(getClass().getName()+".createMapXml group="+group);

    MapXml recordMap;
    XsltChooser chooser = chooserFactory.createXsltChooser();
    chooser.setUriResolverFactory(context.getUriResolverFactory());
    if (group) {
      recordMap = new AlternativeGroupingMapXmlContainer(recordMaps,chooser);
    } else {
      recordMap = new AlternativeSimpleMapXmlContainer(recordMaps,chooser);
    }

    return recordMap; 
  }

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory recordTemplatesFactory) {

    for (int i = 0; i < alternatives.length; ++i) {
      if (alternatives[i].getRecordMapFactory() != null) {
        alternatives[i].getRecordMapFactory().addToXsltEvaluator(mode, recordTemplatesFactory);
      }
    }
  }

  public boolean isGroup() {
    boolean group = false;
    for (int i = 0; !group && i < alternatives.length; ++i) {
      MapXmlFactory recordMapFactory = alternatives[i].getRecordMapFactory();
      if (recordMapFactory != null && (recordMapFactory.isGroup() || recordMapFactory.isRecord())) {
        group = true;
      }
    }
    return group;
  }

  public boolean isRecord() {
    boolean record = false;
    for (int i = 0; !record && i < alternatives.length; ++i) {
      MapXmlFactory recordMapFactory = alternatives[i].getRecordMapFactory();
      if (recordMapFactory != null && recordMapFactory.isRecord()) {
        record = true;
      }
    }
    //return record;
    return false;
  }
}

