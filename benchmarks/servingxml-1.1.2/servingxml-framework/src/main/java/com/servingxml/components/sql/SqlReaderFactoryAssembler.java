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

/**
 * The <code>SqlReaderFactoryAssembler</code> implements an assembler for
 * assembling system <code>ReaderFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlReaderFactoryAssembler {
  
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private JdbcConnectionPool connectionPool = null;
  private SqlQuery sqlQuery = null;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }
  
  public void injectComponent(JdbcConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }
  
  public void injectComponent(SqlQuery sqlQuery) {
    this.sqlQuery = sqlQuery;
  }

  public RecordReaderFactory assemble(ConfigurationContext context) {
    
    if (sqlQuery == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),
                                                                 "sx:sqlQuery");
      throw new ServingXmlException(message);
    }
    
    if (connectionPool == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "sx:jdbcConnectionPool");
      throw new ServingXmlException(message);
    }
    
    RecordReaderFactory recordPipe = new SqlReaderFactory(connectionPool,sqlQuery);
    if (parameterDescriptors.length > 0) {
      recordPipe = new RecordReaderFactoryPrefilter(recordPipe,parameterDescriptors);
    }
    return recordPipe;
  }
}
