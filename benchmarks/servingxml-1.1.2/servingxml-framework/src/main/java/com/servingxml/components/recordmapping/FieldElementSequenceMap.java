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

import com.servingxml.util.Name;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.record.Record;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.common.ChildEvaluator;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.util.QnameContext;

/**
 * A command for mapping a field in a flat file to an element or attribute
 * in an XML stream.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class FieldElementSequenceMap implements MapXml {
  private final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final QnameContext nameContext;
  private final NameSubstitutionExpr nameResolver;
  private final ChildEvaluator containerEvaluator;
  private Name elementName = Name.EMPTY;
  private final MapXml children;

  public FieldElementSequenceMap(QnameContext nameContext, NameSubstitutionExpr nameResolver, ChildEvaluator containerEvaluator, 
    MapXml children) {

    this.nameContext = nameContext;
    this.nameResolver = nameResolver;
    this.containerEvaluator = containerEvaluator;
    this.children = children;
  }

  public void groupStarted(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record variables) {

    String qname = null;
    Record record = flow.getRecord();
    Record parameters = flow.getParameters();
    try {
      elementName = nameResolver.evaluateName(parameters,record);
      AttributesImpl atts = new AttributesImpl();
      children.addToAttributes(context, flow, variables, atts);

      String[] values = containerEvaluator.evaluateStringArray(context, flow, variables);
      //System.out.println(getClass().getName()+elementName+" "+values.length);
      if (values.length > 0) {

        qname = elementName.toQname(nameContext);
        for (int i = 0; i < values.length; ++i) {
          String value = values[i];
          //System.out.println(getClass().getName()+value);
          handler.startElement(elementName.getNamespaceUri(),elementName.getLocalName(),qname,atts);
          if (value.length() > 0) {
            handler.characters(value.toCharArray(),0,value.length());
          }
          handler.endElement(elementName.getNamespaceUri(),elementName.getLocalName(),qname);
        }
      }

    } catch (SAXException e) {
      ServingXmlException t = new ServingXmlException(e.getMessage(), e);
      if (qname != null) {
        t = t.contextualizeMessage(qname);
      }
      throw t;
    } catch (ServingXmlException e) {
      if (qname != null) {
        e = e.contextualizeMessage(qname);
      }
      throw e;
    }
  }

  public void writeRecord(ServiceContext context, Flow flow, 
    Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
  }

  public boolean isGrouping() {
    return false;
  }
}
