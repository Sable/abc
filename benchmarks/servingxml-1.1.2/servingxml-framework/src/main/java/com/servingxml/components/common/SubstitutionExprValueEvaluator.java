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

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;    
import com.servingxml.util.record.Record;    
import com.servingxml.util.ServingXmlException;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.app.Flow;
import com.servingxml.util.record.Value;
import com.servingxml.util.SystemConstants;    

/**
 * The <code>SubstitutionExprValueEvaluator</code> implements a <code>ValueEvaluator</code>.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SubstitutionExprValueEvaluator implements ValueEvaluator {
  private final SubstitutionExpr subExpr;

  public SubstitutionExprValueEvaluator(SubstitutionExpr subExpr) {
    this.subExpr = subExpr;
  }

  public Value bindValue(ServiceContext context, Flow flow) {
    return Value.EMPTY;
  }

  public String evaluateString(ServiceContext context, Flow flow) {
   //System.out.println(flow.getRecord().toXmlString(context));
    String s = subExpr.evaluateAsString(flow.getParameters(),flow.getRecord());
    return s;
  }

  public String[] evaluateStringArray(ServiceContext context, Flow flow) {
   //System.out.println(flow.getRecord().toXmlString(context));
    return subExpr.evaluateAsStringArray(flow.getParameters(),flow.getRecord());
  }

  public Value evaluateValue(ServiceContext context, Flow flow) {
    return Value.EMPTY;
  }
}
