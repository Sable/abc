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

import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.components.recordio.RecordReaderFactoryPrefilter;
import com.servingxml.components.sql.JdbcConnectionPool;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.expr.substitution.SubstitutionExpr;

/**
 * The <code>SqlGrepFactoryAssembler</code> implements an assembler for
 * assembling system <code>ReaderFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlGrepFactoryAssembler {
  private String database = null;
  private String tablePattern = "%";
  private String columnPattern = "%";
  private String valuePattern = null;
  private String[] tableType = {"TABLE"};
  private int maxRowsPerTable = Integer.MAX_VALUE;
  private int maxLength = Integer.MAX_VALUE;
  
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private JdbcConnectionPool connectionPool = null;
  
  public void setDatabase(String database) {
    this.database = database;
  }
  
  public void setTablePattern(String tablePattern) {
    this.tablePattern = tablePattern;
  }
  
  public void setColumnPattern(String columnPattern) {
    this.columnPattern = columnPattern;
  }
  
  public void setMaxRowCount(int maxRowsPerTable) {
    this.maxRowsPerTable = maxRowsPerTable;
  }
  
  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }
  
  public void setSearchString(String valuePattern) {
    this.valuePattern = valuePattern;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }
  
  public void injectComponent(JdbcConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }

  public RecordReaderFactory assemble(ConfigurationContext context) {
    
    if (connectionPool == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "sx:jdbcConnectionPool");
      throw new ServingXmlException(message);
    }
	if (valuePattern == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "valuePattern");
      throw new ServingXmlException(message);
	}
	
	String[] catalogs = database == null ? new String[0] : new String[]{database};
    
    SubstitutionExpr valueResolver = SubstitutionExpr.parseString(context.getQnameContext(), valuePattern);

    RecordReaderFactory recordPipe = new SqlGrepFactory(catalogs, tablePattern, columnPattern, valueResolver, tableType, 
	maxRowsPerTable, maxLength, connectionPool);
    if (parameterDescriptors.length > 0) {
      recordPipe = new RecordReaderFactoryPrefilter(recordPipe,parameterDescriptors);
    }
    return recordPipe;
  }
}
