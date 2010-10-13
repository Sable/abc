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

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.util.xml.XsltEvaluator;

/**
 * A command for inserting an element mapped from a flat file into an XML stream.
 * 
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                               
public class GroupChoice implements MapXml {

  private static final Attributes noAttributes = new AttributesImpl();

  private final MapXml[] children;
  private final XsltEvaluator xsltEvaluator;

  public GroupChoice(MapXml[] children, XsltEvaluator xsltEvaluator) {

    this.children = children;
    this.xsltEvaluator = xsltEvaluator;
  }

  public void writeRecord(ServiceContext context, Flow flow, 
    Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {

    GroupState gl = new GroupStateImpl(groupListener);
    for (int i = 0; !gl.wasStarted() && i < children.length; ++i) {
      MapXml child = children[i];
      child.writeRecord(context, flow, previousRecord, nextRecord, handler, gl);
    }          
  }                                         

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record valueRecord1) {

    SaxSource saxSource = flow.getDefaultSaxSource();
    Source source = new SAXSource(saxSource.createXmlReader(),new InputSource());
    Record variables = xsltEvaluator.evaluate(source, flow.getParameters());
    for (int i = 0; i < children.length; ++i) {
      MapXml child = children[i];
      child.groupStarted(context, flow, previousRecord,nextRecord, handler, variables);
    }
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    for (int i = 0; i < children.length; ++i) {
      MapXml child = children[i];
      child.groupStopped(context, flow, handler);
    }
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, 
    AttributesImpl attributes) {
    /*
    for (int i = 0; i < children.length; ++i) {
      MapXml child = children[i];
      child.addToAttributes(context, flow, variables, attributes);
    }
    */
  }

  public boolean isGrouping() {
    boolean grouping = false;
    for (int i = 0; !grouping && i < children.length; ++i) {
      MapXml child = children[i];
      if (child.isGrouping()) {
        grouping = true;
      }
    }
    return grouping;
  }
}




