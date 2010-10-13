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
                                      
import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.common.ChildEvaluator;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.util.QnameContext;

public class FieldAttributeMap implements MapXml {
  private final QnameContext nameContext;
  private final NameSubstitutionExpr nameResolver;
  private final ChildEvaluator containerEvaluator;

  public FieldAttributeMap(QnameContext nameContext, NameSubstitutionExpr nameResolver,ChildEvaluator containerEvaluator) {
    this.nameContext = nameContext;
    this.nameResolver = nameResolver;
    this.containerEvaluator = containerEvaluator;
  }

  public Name createName(Record record) {
    return nameResolver.evaluateName(Record.EMPTY,record);
  }

  public boolean hasName(Name name) {

    return nameResolver.hasName(name);
  }

  public String getValue(ServiceContext context, Flow flow, Record variables) {

    String v = containerEvaluator.evaluateString(context, flow, variables);

    return v;
  }

  public void groupStarted(ServiceContext context, Flow flow, 
    Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, Record variables) {
  }

  public void writeRecord(ServiceContext context, Flow flow, 
    Record previousRecord, Record nextRecord, 
    ExtendedContentHandler handler, GroupState groupListener) {
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, AttributesImpl attributes) {
    Name name = createName(flow.getRecord());
    String qname = name.toQname(nameContext);
    try {
      String value = getValue(context, flow, variables);
      //System.out.println(getClass().getName()+".addToAttributes value="+value + "\nvariables="+variables.toXmlString(context));
      attributes.addAttribute(name.getNamespaceUri(),name.getLocalName(),qname,"CDATA",value);
      //System.out.println(getClass()+".addToAttributes value="+ value);
    } catch (ServingXmlException e) {
      if (qname != null) {
        e = e.contextualizeMessage("@"+qname);
      }
      throw e;
    }
  }

  public boolean isGrouping() {
    return false;
  }
}
