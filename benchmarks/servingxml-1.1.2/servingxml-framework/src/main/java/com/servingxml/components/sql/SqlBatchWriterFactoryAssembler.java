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
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.sql.JdbcConnectionPool;
import com.servingxml.components.recordio.RecordFilterAppenderPrefilter;
import com.servingxml.components.recordio.RecordFilterAppender;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class SqlBatchWriterFactoryAssembler {
  
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private JdbcConnectionPool connectionPool = null;
  private SqlUpdateDatabaseFactory[] sqlUpdates = new SqlUpdateDatabaseFactory[0];
  private int batchSize = 100;

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }
  
  public void injectComponent(JdbcConnectionPool connectionPool) {
    this.connectionPool = connectionPool;
  }
  
  public void injectComponent(SqlUpdateDatabaseFactory[] sqlUpdates) {
    this.sqlUpdates = sqlUpdates;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {

    
    if (connectionPool == null) {                  
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
        context.getElement().getTagName(),"sx:jdbcConnectionPool");
      throw new ServingXmlException(message);
    }
    
    if (sqlUpdates.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
        context.getElement().getTagName(),"sx:sqlUpdate");
      throw new ServingXmlException(message);
    }

    if (sqlUpdates.length != 1) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
        context.getElement().getTagName(),"sx:sqlUpdate");
      throw new ServingXmlException(message);
    }

    SqlUpdateDatabaseFactory sqlUpdate = sqlUpdates[0];
    
    RecordFilterAppender recordWriterFactory = new SqlBatchWriterFactory(
      connectionPool, sqlUpdate, batchSize);
    if (parameterDescriptors.length > 0) {
      recordWriterFactory = new RecordFilterAppenderPrefilter(recordWriterFactory,parameterDescriptors);
    }

    return recordWriterFactory;
  }
}
