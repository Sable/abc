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

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Comparator;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.expr.substitution.SubstitutionParser;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.expr.ExpressionException;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.Name;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.app.Environment;

/**
 * The <code>GroupByFactoryAssembler</code> implements an assembler for
 * assembling <code>GroupByFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class GroupByFactoryAssembler {
  private MapXmlFactory[] childFactories = new MapXmlFactory[0];
  private String fieldQnames = "";
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private Name recordTypeName = Name.EMPTY;
  private Sort[] sorts = new Sort[0];

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(Sort[] sorts) {

    this.sorts = sorts;
  }

  public void setRecordType(Name recordType) {
    this.recordTypeName = recordType;
  }

  public void setFields(String fieldQnames) {
    this.fieldQnames = fieldQnames;
  }

  public void injectComponent(MapXmlFactory[] childFactories) {
    this.childFactories = childFactories;
  }
                                                       
  public MapXmlFactory assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    try {

      if (childFactories.length == 0) {
        String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_CHOICE_REQUIRED,
                                                               context.getElement().getTagName(),
                                                               "literal, sx:fieldElementMap, sx:group, sx:onRecord");
        throw new ServingXmlException(msg);
      }

      StringTokenizer fieldNameTokenizer = new StringTokenizer(fieldQnames," ,");
      ArrayList<SubstitutionExpr> fieldNameList = new ArrayList<SubstitutionExpr>();
      while (fieldNameTokenizer.hasMoreTokens()) {
        String fieldQname = fieldNameTokenizer.nextToken();
        SubstitutionParser parser = new SubstitutionParser(context.getQnameContext(),fieldQname.trim());
        SubstitutionExpr expr = parser.parse();
        fieldNameList.add(expr);
      }
      SubstitutionExpr[] fieldNames = new SubstitutionExpr[fieldNameList.size()];
      fieldNames = fieldNameList.toArray(fieldNames);

      MapXmlFactory rmf = new MultipleMapXmlFactory(context.getQnameContext(), xsltConfiguration, childFactories);

      if (sorts.length > 0) {
        Comparator comparator = new SortComparator(sorts);
        rmf = new SortGroupFactory(rmf,comparator);
      }

      Environment env = new Environment(parameterDescriptors,context.getQnameContext());
      MapXmlFactory recordMapFactory = new GroupByFactory(env, recordTypeName, fieldNames, rmf);

      return recordMapFactory;
    } catch (ExpressionException e) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_SUB_EXPR_PARSE_FAILED,
                                                             context.getElement().getTagName());
      throw new ServingXmlException(msg + "  " + e.getMessage());
    }
  }
}
