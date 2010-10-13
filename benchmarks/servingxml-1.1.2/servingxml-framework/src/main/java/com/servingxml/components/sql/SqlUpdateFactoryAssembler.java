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
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.components.quotesymbol.QuoteSymbol;
import com.servingxml.expr.substitution.EscapeSubstitutionVariables;
import com.servingxml.expr.substitution.DoEscapeSubstitutionVariables;
import com.servingxml.util.Name;
import com.servingxml.components.recordio.RecordAccepter;
import com.servingxml.components.recordio.RecordAccepterFactory;
import com.servingxml.app.Environment;

/**
 * The <code>SqlUpdateFactoryAssembler</code> implements an assembler for
 * assembling system <code>SqlStatement</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlUpdateFactoryAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private EscapeSubstitutionVariables escapeVariables = new DoEscapeSubstitutionVariables('\'', "''");
  private SqlPreparedStatement preparedStatement = null;
  private SqlUpdateDatabaseFactory[] detailUpdates = new SqlUpdateDatabaseFactory[0];
  private Name recordTypeName = Name.EMPTY;

  public void setRecordType(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
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

  public void injectComponent(SqlPreparedStatement preparedStatement) {

    this.preparedStatement = preparedStatement;
  }

  public void injectComponent(SqlUpdateDatabaseFactory[] detailUpdates) {
    this.detailUpdates = detailUpdates;
  }
  
  public SqlUpdateDatabaseFactory assemble(final ConfigurationContext context) {
    Environment env = new Environment(parameterDescriptors,context.getQnameContext());

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    RecordAccepterFactory accepterFactory = RecordAccepterFactory.newInstance(recordTypeName);
    RecordAccepter accepter = accepterFactory.createRecordAccepter();
    
    SqlUpdateDatabaseFactory sqlUpdate;
    if (preparedStatement == null) {
      SqlStatementFactory factory = new SqlStatementFactory(env,xsltConfiguration.getVersion(), escapeVariables);
      SqlStatement statement = factory.createSqlStatement(context);
      sqlUpdate = new SqlUpdateFactory(accepter, statement, detailUpdates);
    } else {
      sqlUpdate = new SqlPreparedUpdateFactory(accepter, preparedStatement.getStatement(), 
                                               preparedStatement.getArguments(),
                                               detailUpdates);
    }

    if (parameterDescriptors.length > 0) {
      sqlUpdate = new SqlUpdateDatabaseFactoryPrefilter(sqlUpdate, parameterDescriptors);
    }
    return sqlUpdate;
  }
}

