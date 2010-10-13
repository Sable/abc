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

import com.servingxml.app.ServiceContext;
import com.servingxml.components.sql.JdbcConnectionPool;
import com.servingxml.components.recordio.RecordFilterAppender;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.app.Flow;
import com.servingxml.components.recordio.AbstractRecordReaderFactory;
import com.servingxml.util.record.Record;
import com.servingxml.expr.substitution.SubstitutionExpr;

/**
 * A <code>SqlGrepFactory</code> instance may be used to obtain objects that
 * implement the <code>RecordReaderFactory</code> interface.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlGrepFactory extends AbstractRecordReaderFactory 
implements RecordReaderFactory, RecordFilterAppender {     
  private final String[] catalogs;
  private final String tablePattern;
  private final String columnPattern;
  private final SubstitutionExpr valuePattern;
  private final String[] tableType;
  private final int maxRowsPerTable;
  private final int maxLength;

  private final JdbcConnectionPool connectionPool;

  public SqlGrepFactory(String[] catalogs, String tablePattern, String columnPattern, SubstitutionExpr valuePattern, String[] tableType,
                        int maxRowsPerTable, int maxLength, JdbcConnectionPool connectionPool) {
    this.catalogs = catalogs;
    this.tablePattern = tablePattern;
	this.columnPattern = columnPattern;
	this.valuePattern = valuePattern;
	this.tableType = tableType;
	this.maxRowsPerTable = maxRowsPerTable;
	this.maxLength = maxLength;
    this.connectionPool = connectionPool;
  }

  protected RecordReader createRecordReader(ServiceContext context, Flow flow) {
    RecordReader recordReader = new SqlGrep(catalogs, tablePattern, columnPattern, valuePattern, tableType, 
	                                        maxRowsPerTable, maxLength, connectionPool);
    return recordReader;
  }
}
