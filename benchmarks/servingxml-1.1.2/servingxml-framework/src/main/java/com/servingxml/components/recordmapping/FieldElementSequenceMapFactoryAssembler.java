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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.components.common.ChildValueEvaluator;
import com.servingxml.components.common.ChildEvaluator;
import com.servingxml.components.common.SubstitutionExprValueEvaluator;
import com.servingxml.components.common.MatchSelectChildEvaluator;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.expr.substitution.SubstitutionExpr;  
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.expr.substitution.FieldSubstitutor;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.util.PrefixMap;
import com.servingxml.components.string.StringFactoryCompiler;
import com.servingxml.components.string.StringValueEvaluator;
import com.servingxml.components.string.StringFactory;

/**
 * The <code>FieldElementSequenceMapFactoryAssembler</code> implements an assembler for
 * assembling <code>GenerateElementFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FieldElementSequenceMapFactoryAssembler {

  private MapXmlFactory[] childFactories = new MapXmlFactory[0];
  private Name fieldName = null;
  private String elementQname = null;
  private String value = null;
  private String select = null;
  private String match = null;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;

  public void setField(Name fieldName) {
    this.fieldName = fieldName;
  }

  public void setSelect(String select) {
    this.select = select;
  }

  public void setMatch(String match) {
    this.match = match;
  }

  public void setElement(String qname) {
    this.elementQname = qname;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(MapXmlFactory[] childFactories) {
    this.childFactories = childFactories;
  }

  public MapXmlFactory assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (elementQname == null || elementQname.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "element");
      throw new ServingXmlException(message);
    }

    NameSubstitutionExpr nameResolver = NameSubstitutionExpr.parse(context.getQnameContext(),elementQname);

    ChildEvaluator containerEvaluator = null;
    if (value != null) {
      SubstitutionExpr subExpr = SubstitutionExpr.parseString(context.getQnameContext(),value);
      ValueEvaluator valueEvaluator = new SubstitutionExprValueEvaluator(subExpr);
      containerEvaluator = new ChildValueEvaluator(valueEvaluator);        
    } else if (match != null || select != null) {
      if (match == null) {
        match = "/*";
      }
      containerEvaluator = new MatchSelectChildEvaluator(match, select);
    } else if (fieldName != null) {
      SubstitutionExpr subExpr = new FieldSubstitutor(fieldName);
      ValueEvaluator valueEvaluator = new SubstitutionExprValueEvaluator(subExpr);
      containerEvaluator = new ChildValueEvaluator(valueEvaluator);        
    } else {
      StringFactory stringFactory = StringFactoryCompiler.fromStringables(context, context.getElement());
      ValueEvaluator valueEvaluator = new StringValueEvaluator(stringFactory);
      containerEvaluator = new ChildValueEvaluator(valueEvaluator);        
    }

    MapXmlFactory recordMapFactory;
    //if (containerEvaluator != null) {
      MapXmlFactory rmf = new MultipleMapXmlFactory(context.getQnameContext(), xsltConfiguration, childFactories);
      recordMapFactory = new FieldElementSequenceMapFactory(context.getQnameContext(), nameResolver,containerEvaluator,rmf);
    //} else {
      //MapXmlFactory rmf = new MultipleMapXmlFactory(context.getQnameContext(), xsltConfiguration, childFactories);
      //PrefixMap  localPrefixMap = context.getQnameContext().getPrefixMap();
      //recordMapFactory = new GenerateElementFactory(context.getQnameContext(), nameResolver, stringFactories, rmf);
    //}
    if (parameterDescriptors.length > 0) {
      recordMapFactory = new MapXmlFactoryPrefilter(recordMapFactory,parameterDescriptors);
    }
    return recordMapFactory;
  }
}
