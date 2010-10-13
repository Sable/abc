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

import java.util.Properties;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.app.ServiceContext;        
import com.servingxml.app.xmlpipeline.XmlPipeline;
import com.servingxml.app.Flow;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.XmlReaderSaxSource;
import com.servingxml.util.record.Record;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.components.content.Content;
import com.servingxml.app.Environment;

/**
 * A command for inserting an element mapped from a flat file into an XML stream.
 * 
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordSubtreeMap implements MapXml {

  private final Environment env;
  private final Content[] xmlComponents;

  private boolean started = false;

  public RecordSubtreeMap(Environment env, Content[] xmlComponents) {

    this.env = env;
    this.xmlComponents = xmlComponents;
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
  }                                         

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record variables) {
    //long start = Runtime.getRuntime().totalMemory();
    //System.out.println(getClass().getName() + ".groupStarted enter mem=" + start);

    if (!started) {
      //long start1 = Runtime.getRuntime().totalMemory();
      Record record = flow.getRecord();
      XMLReader reader = record.createXmlReader(env.getQnameContext().getPrefixMap());
      SaxSource saxSource = new XmlReaderSaxSource(reader, context.getTransformerFactory());

      Properties defaultOutputProperties = saxSource.getDefaultOutputProperties();
      XmlPipeline pipeline = new XmlPipeline(saxSource.createXmlReader(), 
        saxSource.getSystemId(), saxSource.getExpirable(), defaultOutputProperties);
      for (int i = 0; i < xmlComponents.length; ++i) {
        //start = Runtime.getRuntime().totalMemory();
        //System.out.println(getClass().getName() + ".groupStarted " + xmlComponents[i].getClass().getName() + " before append mem=" + start);
        xmlComponents[i].appendToXmlPipeline(context, flow, pipeline);
        //start = Runtime.getRuntime().totalMemory();
        //System.out.println(getClass().getName() + ".groupStarted after append mem=" + start);
      }
      //start = Runtime.getRuntime().totalMemory();
      //System.out.println(getClass().getName() + ".groupStarted before exec mem=" + start);
      pipeline.execute(handler);
      //start = Runtime.getRuntime().totalMemory();
      //System.out.println(getClass().getName() + ".groupStarted after exec mem=" + start);
      //long endMem = Runtime.getRuntime().totalMemory();
    }
    //long end = Runtime.getRuntime().totalMemory();
    //System.out.println(getClass().getName() + ".groupStarted leave mem=" + end);
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    if (started) {
      started = false;
    }
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return started;
  }
}




