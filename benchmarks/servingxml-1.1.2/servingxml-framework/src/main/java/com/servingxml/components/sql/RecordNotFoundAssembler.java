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

/**
 * The <code>SqlQueryAssembler</code> implements an assembler for
 * assembling system <code>SqlQuery</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordNotFoundAssembler {
  
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private SqlUpdateDatabaseFactory[] sqlUpdates;
  
  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(SqlUpdateDatabaseFactory[] sqlUpdates) {
    this.sqlUpdates = sqlUpdates;
  }
  
  public RecordNotFound assemble(final ConfigurationContext context) {

    SqlUpdateDatabaseFactory sqlUpdate;
    if (sqlUpdates.length == 0) {
      sqlUpdate = SqlUpdateDatabaseFactory.NULL;
    } else if (sqlUpdates.length == 1) {
      sqlUpdate = sqlUpdates[0];
    } else {
      sqlUpdate = new MultipleSqlUpdateDatabaseFactory(sqlUpdates);
    }
    
    RecordNotFound sqlNotFound = new RecordNotFound(sqlUpdate);
    return sqlNotFound;
  }
}

