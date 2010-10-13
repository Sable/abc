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

package com.servingxml.components.sql;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.components.quotesymbol.QuoteSymbol;
import com.servingxml.expr.substitution.EscapeSubstitutionVariables;
import com.servingxml.expr.substitution.DoEscapeSubstitutionVariables;
import com.servingxml.app.Environment;

/**
 * The <code>SqlQueryAssembler</code> implements an assembler for
 * assembling system <code>SqlQuery</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlQueryAssembler {
  private static final Name DEFAULT_RECORD_TYPE_NAME = new QualifiedName("record");
  
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Name recordTypeName = DEFAULT_RECORD_TYPE_NAME;
  private XsltConfiguration xsltConfiguration;
  private String trimLeading = TrueFalseEnum.FALSE.toString();
  private String trimTrailing = TrueFalseEnum.FALSE.toString();
  private EscapeSubstitutionVariables escapeVariables = new DoEscapeSubstitutionVariables('\'', "''");
  
  public void setRecordType(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void setTrim(String value) {
    this.trimLeading = value;
    this.trimTrailing = value;
  }

  public void setTrimLeading(String trimLeading) {
    this.trimLeading = trimLeading;
  }

  public void setTrimTrailing(String trimTrailing) {
    this.trimTrailing = trimTrailing;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(EscapeSubstitutionVariables escapeVariables) {
    this.escapeVariables = escapeVariables;
  }

  public void injectComponent(QuoteSymbol quoteSymbol) {
    if (quoteSymbol.getEscapeSequence().length() > 0) {
      this.escapeVariables = new DoEscapeSubstitutionVariables(quoteSymbol.getCharacter(),quoteSymbol.getEscapeSequence());
    } else {
      this.escapeVariables = EscapeSubstitutionVariables.DO_NOT_ESCAPE;
    }
  }
  
  public SqlQuery assemble(final ConfigurationContext context) {
    Environment env = new Environment(parameterDescriptors,context.getQnameContext());

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }
    
    if (recordTypeName.isEmpty()) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"recordType");
      throw new ServingXmlException(message);
    }
    TrueFalseEnum trimLeadingIndicator;
    try {
      trimLeadingIndicator = TrueFalseEnum.parse(trimLeading);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
        context.getElement().getTagName(), "trimLeading");
      e = e.supplementMessage(message);
      throw e;
    }

    TrueFalseEnum trimTrailingIndicator;
    try {
      trimTrailingIndicator = TrueFalseEnum.parse(trimTrailing);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
        context.getElement().getTagName(), "trimTrailing");
      e = e.supplementMessage(message);
      throw e;
    }
    
    SqlStatementFactory factory = new SqlStatementFactory(env,xsltConfiguration.getVersion(), escapeVariables);
    SqlStatement statement = factory.createSqlStatement(context);
    SqlQuery query = new SqlQueryImpl(statement, recordTypeName, 
                            trimLeadingIndicator.booleanValue(), trimTrailingIndicator.booleanValue());
    if (parameterDescriptors.length > 0) {
      query = new SqlQueryPrefilter(query, parameterDescriptors);
    }

    return query;
  }
}

