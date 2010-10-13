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


import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.Attributes;

import com.servingxml.util.Name;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.record.Record;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.string.Stringable;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.util.QnameContext;
import com.servingxml.app.Environment;

/**
 * A command for inserting an element mapped from a flat file into an XML stream.
 * 
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ElementMap implements MapXml {

  private static final Attributes noAttributes = new AttributesImpl();

  private final Environment env;
  private final NameSubstitutionExpr nameResolver;
  private final AttributesImpl attributes;
  private final Stringable[] stringFactories;
  private final MapXml children;
  private String namespaceUri = "";
  private String localName = "";
  private String qname = "";
  private Flow groupFlow;

  private boolean started = false;

  public ElementMap(Environment env, NameSubstitutionExpr nameResolver,
    AttributesImpl attributes, 
    Stringable[] stringFactories, MapXml children) {

    this.env = env;
    this.nameResolver = nameResolver;
    this.attributes = attributes;
    this.stringFactories = stringFactories;
    this.children = children;
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
   //System.out.println(getClass().getName()+".writeRecord enter");
    try {
      if (groupFlow == null) {
        groupFlow = env.augmentParametersOf(context, flow);
      }
      Flow newFlow = flow.replaceParameters(context,groupFlow.getParameters());
      children.writeRecord(context, newFlow, previousRecord,nextRecord,handler,groupListener);
    } catch (ServingXmlException e) {
      if (qname != null) {
        e = e.contextualizeMessage(qname);
      }
      throw e;
    } catch (Exception e) {
      ServingXmlException t = new ServingXmlException(e.getMessage(), e);
      if (qname != null) {
        t = t.contextualizeMessage(qname);
      }
      throw t;
    }
   //System.out.println(getClass().getName()+".writeRecord leave");
  }                                         

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record variables) {
   //System.out.println(getClass().getName()+".groupStarted enter");
    if (!started) {
      groupFlow = env.augmentParametersOf(context,flow);
      started = true;
      try {
        Record record = groupFlow.getRecord();
        Record parameters = groupFlow.getParameters();

        Name elementName = nameResolver.evaluateName(parameters, record);
        this.namespaceUri = elementName.getNamespaceUri();
        this.localName = elementName.getLocalName();
        this.qname = env.qnameFor(elementName);

        AttributesImpl atts = new AttributesImpl();
        for (int i = 0; i < attributes.getLength();++i) {
          atts.addAttribute(attributes.getURI(i),attributes.getLocalName(i),attributes.getQName(i),
            attributes.getType(i),attributes.getValue(i));
        }
        //System.out.println(getClass().getName()+".groupStarted putting attributes");
        children.addToAttributes(context, groupFlow, variables, atts);

        //System.out.println("ElementMap.start namespaceUri = " + namespaceUri + ", localName = " + localName + ", qname = " + qname);
        handler.startElement(namespaceUri,localName,qname,atts);

        for (int i = 0; i < stringFactories.length; ++i) {
          Stringable stringFactory = stringFactories[i];
          String value = stringFactory.createString(context, groupFlow);
          if (value.length() > 0) {
            handler.characters(value.toCharArray(),0,value.length());
          }
        }
        children.groupStarted(context, groupFlow, previousRecord,nextRecord, handler, Record.EMPTY);
      } catch (ServingXmlException e) {
        if (qname != null) {
          e = e.contextualizeMessage(qname);
        }
        throw e;
      } catch (Exception e) {
        ServingXmlException t = new ServingXmlException(e.getMessage(), e);
        if (qname != null) {
          t = t.contextualizeMessage(qname);
        }
        throw t;
      }
    }
   //System.out.println(getClass().getName()+".groupStarted leave");
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
   //System.out.println(getClass().getName()+".groupStopped enter");
    try {
      groupFlow = env.augmentParametersOf(context, flow);
      children.groupStopped(context, groupFlow, handler);
      if (started) {
        //System.out.println("ElementMap.end namespaceUri = " + namespaceUri + ", localName = " + localName + ", qname = " + qname);
        handler.endElement(namespaceUri,localName,qname);
        started = false;
      }
    } catch (ServingXmlException e) {
      if (qname != null) {
        e = e.contextualizeMessage(qname);
      }
      throw e;
    } catch (Exception e) {
      ServingXmlException t = new ServingXmlException(e.getMessage(), e);
      if (qname != null) {
        t = t.contextualizeMessage(qname);
      }
      throw t;
    }
   //System.out.println(getClass().getName()+".groupStopped leave");
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, 
    AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return started;
  }
}




