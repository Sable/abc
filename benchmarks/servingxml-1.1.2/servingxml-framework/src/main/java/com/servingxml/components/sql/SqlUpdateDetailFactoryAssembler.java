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
import com.servingxml.components.quotesymbol.QuoteSymbol;
import com.servingxml.expr.substitution.DoEscapeSubstitutionVariables;
import com.servingxml.expr.substitution.EscapeSubstitutionVariables;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;

/**
 * The <code>SqlUpdateDetailFactoryAssembler</code> implements an 
 * assembler for 
 * assembling system <code>SqlStatement</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlUpdateDetailFactoryAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Name fieldName = Name.EMPTY;
  private SqlUpdateDatabaseFactory[] sqlUpdates = new SqlUpdateDatabaseFactory[0];

  public void setField(Name fieldName) {
    this.fieldName = fieldName;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(SqlUpdateDatabaseFactory[] sqlUpdates) {
    this.sqlUpdates = sqlUpdates;
  }
  
  public SqlUpdateDatabaseFactory assemble(final ConfigurationContext context) {

    if (fieldName.isEmpty()) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"field");
      throw new ServingXmlException(message);
    }

    if (sqlUpdates.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
        context.getElement().getTagName(),"sx:sqlUpdates");
      throw new ServingXmlException(message);
    }

    SqlUpdateDatabaseFactory sqlUpdate;
    if (sqlUpdates.length == 1) {
      sqlUpdate = sqlUpdates[0];
    } else {
      sqlUpdate = new MultipleSqlUpdateDatabaseFactory(sqlUpdates);
    }
    SqlUpdateDatabaseFactory updateDetail = new SqlUpdateDetailFactory(
      fieldName, sqlUpdate);

    if (parameterDescriptors.length > 0) {
      updateDetail = new SqlUpdateDatabaseFactoryPrefilter(updateDetail, parameterDescriptors);
    }
    return updateDetail;
  }
}

