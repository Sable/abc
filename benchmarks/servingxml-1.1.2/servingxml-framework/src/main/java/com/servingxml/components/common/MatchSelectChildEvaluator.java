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

package com.servingxml.components.common;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;                    
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;    
import com.servingxml.util.record.Record;    
import com.servingxml.util.record.Value;
import com.servingxml.util.record.ValueFactory;
import com.servingxml.util.xml.DefaultMatchableFactory;
import com.servingxml.util.xml.Matchable;
import com.servingxml.util.xml.MatchableFactory;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.util.xml.Selectable;
import com.servingxml.util.xml.SelectableImpl;

/**
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MatchSelectChildEvaluator implements ChildEvaluator {
  private final String matchExpr;
  private final String selectExpr;
  private Name variableName = Name.EMPTY;

  public MatchSelectChildEvaluator(String matchExpr, String selectExpr) {
    this.matchExpr = matchExpr;
    this.selectExpr = selectExpr;
  }

  public String evaluateString(ServiceContext context, Flow flow, Record variables) {
    String s = variables.getString(variableName);
    return s == null ? "" : s;
  }

  public String[] evaluateStringArray(ServiceContext context, Flow flow, Record variables) {
    String[] a = variables.getStringArray(variableName);
    return a == null ? new String[0] : a;
  }

  //Revisit - ValueFactory
  public Value evaluateValue(ServiceContext context, Flow flow, Record variables) {
    String[] sa = evaluateStringArray(context,flow,variables);
    Value value = ValueFactory.createStringArrayValue(sa);
    return value;
  }

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory recordTemplatesFactory) {
    this.variableName = new QualifiedName(mode);

    Selectable[] selectables = new Selectable[1];
    selectables[0] = new SelectableImpl(variableName, selectExpr);
    MatchableFactory matchableFactory = new DefaultMatchableFactory(matchExpr, selectables);
    Matchable matchable = matchableFactory.createMatchable(variableName.getLocalName());
    recordTemplatesFactory.addMatchable(matchable);
  }
}

