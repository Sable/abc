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
import org.xml.sax.InputSource;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.util.xml.XsltEvaluator;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SimpleRecordMapContainer implements MapXml {                   
  private final MapXml[] children;
  private final XsltEvaluator xsltEvaluator;

  private Record previous = Record.EMPTY;
  private Flow currentFlow;
  private Record next = Record.EMPTY;
  private Record variables = Record.EMPTY;

  public SimpleRecordMapContainer(MapXml[] children, XsltEvaluator xsltEvaluator) {
    this.children = children;
    this.xsltEvaluator = xsltEvaluator;
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
  ExtendedContentHandler handler, GroupState groupListener) {
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
  ExtendedContentHandler handler, Record variables1) {

    //System.out.println(getClass().getName()+".groupStarted");

    previous = previousRecord;
    currentFlow = flow;
    next = nextRecord;

    if (!xsltEvaluator.isEmpty()) {
      SaxSource saxSource = flow.getDefaultSaxSource();
      Source source = new SAXSource(saxSource.createXmlReader(),new InputSource());
      this.variables = xsltEvaluator.evaluate(source, flow.getParameters());
    }

    for (int i = 0; i < children.length; ++i) {
      MapXml child = children[i];
      child.groupStarted(context, flow, previousRecord, nextRecord, handler, variables);
      child.groupStopped(context, flow, handler);      
    }
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables1, AttributesImpl attributes) {
    //System.out.println(getClass().getName()+".addToAttributes");
    if (!xsltEvaluator.isEmpty()) {
      SaxSource saxSource = flow.getDefaultSaxSource();
      Source source = new SAXSource(saxSource.createXmlReader(),new InputSource());
      this.variables = xsltEvaluator.evaluate(source, flow.getParameters());
    }
    for (int i = 0; i < children.length; ++i) {
      MapXml child = children[i];
      child.addToAttributes(context, flow, variables, attributes);
    }
  }

  public boolean isGrouping() {
    return false;
  }
}

