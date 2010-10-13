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

import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.components.common.ChildValueEvaluator;
import com.servingxml.components.common.ChildEvaluator;
import com.servingxml.components.common.SubstitutionExprValueEvaluator;
import com.servingxml.components.common.MatchSelectChildEvaluator;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.expr.substitution.FieldSubstitutor;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.string.StringFactoryCompiler;
import com.servingxml.components.string.StringValueEvaluator;
import com.servingxml.components.string.StringFactory;

/**
 * The <code>FieldAttributeMapFactoryAssembler</code> implements an assembler for
 * assembling <code>FieldAttributeMap</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FieldAttributeMapFactoryAssembler {
  
  private Name fieldName = null;
  private String attributeQname = null;
  private String value = null;
  private String match = "/*";
  private String selectExpr = null;

  public void setSelect(String selectExpr) {
    this.selectExpr = selectExpr;
  }
  
  public void setField(Name fieldName) {
    this.fieldName = fieldName;
  }
  
  public void setAttribute(String qname) {
    this.attributeQname = qname;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public MapXmlFactory assemble(ConfigurationContext context) {
    
    if (attributeQname == null || attributeQname.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"attribute");
      throw new ServingXmlException(message);
    }
    
    NameSubstitutionExpr nameResolver = NameSubstitutionExpr.parse(context.getQnameContext(),attributeQname);

    ChildEvaluator containerEvaluator;

    if (value != null) {
      SubstitutionExpr subExpr = SubstitutionExpr.parseString(context.getQnameContext(),value);
      ValueEvaluator valueEvaluator = new SubstitutionExprValueEvaluator(subExpr);
      containerEvaluator = new ChildValueEvaluator(valueEvaluator);        
    } else if (selectExpr != null) {
      containerEvaluator = new MatchSelectChildEvaluator(match, selectExpr);
    } else if (fieldName != null) {
      SubstitutionExpr subExpr = new FieldSubstitutor(fieldName);
      ValueEvaluator valueEvaluator = new SubstitutionExprValueEvaluator(subExpr);
      containerEvaluator = new ChildValueEvaluator(valueEvaluator);        
    } else {
      StringFactory stringFactory = StringFactoryCompiler.fromStringables(context, context.getElement());
      ValueEvaluator valueEvaluator = new StringValueEvaluator(stringFactory);
      containerEvaluator = new ChildValueEvaluator(valueEvaluator);        
    }

    FieldAttributeMapFactory factory = new FieldAttributeMapFactory(context.getQnameContext(),nameResolver , containerEvaluator);
    return factory;
  }
}
