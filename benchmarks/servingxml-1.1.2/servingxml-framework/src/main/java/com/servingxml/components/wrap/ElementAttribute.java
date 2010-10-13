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

package com.servingxml.components.wrap;

import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.QnameContext;

public class ElementAttribute {
  private final QnameContext nameContext;
  private final NameSubstitutionExpr nameResolver;
  private final ValueEvaluator valueEvaluator;

  public ElementAttribute(QnameContext nameContext, NameSubstitutionExpr nameResolver,ValueEvaluator valueEvaluator) {
    this.nameContext = nameContext;
    this.nameResolver = nameResolver;
    this.valueEvaluator = valueEvaluator;
  }

  public Name createName(Record record) {
    return nameResolver.evaluateName(Record.EMPTY,record);
  }

  public boolean hasName(Name name) {

    return nameResolver.hasName(name);
  }

  public String getValue(ServiceContext context, Flow flow) {

    String v = valueEvaluator.evaluateString(context, flow);

    return v;
  }

  public void addToAttributes(ServiceContext context, Flow flow, AttributesImpl attributes) {
    Name name = createName(flow.getRecord());
    String qname = name.toQname(nameContext);
    //System.out.println(getClass().getName()+".addToAttributes");
    try {
      String value = getValue(context, flow);
      attributes.addAttribute(name.getNamespaceUri(),name.getLocalName(),
        qname,"CDATA",value);
    } catch (ServingXmlException e) {
      if (qname != null) {
        e = e.contextualizeMessage("@"+qname);
      }
      throw e;
    }
  }
}
