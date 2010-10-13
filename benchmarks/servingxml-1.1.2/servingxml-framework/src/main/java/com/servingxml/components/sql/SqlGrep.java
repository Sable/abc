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

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.sql.JdbcConnectionPool;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.StringHelper;
import com.servingxml.util.record.DefaultFieldType;
import com.servingxml.util.record.FieldType;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.RecordType;
import com.servingxml.util.record.Value;
import com.servingxml.components.recordio.AbstractRecordReader;
import com.servingxml.components.regex.JavaPatternMatcher;
import com.servingxml.components.regex.PatternMatcher;
import com.servingxml.expr.substitution.SubstitutionExpr;

public class SqlGrep extends AbstractRecordReader implements RecordReader {

  private static final Name CATALOG_NAME = new QualifiedName("Database");
  private static final Name SCHEMA_NAME = new QualifiedName("Owner");
  private static final Name TABLE_NAME = new QualifiedName("Table");
  private static final Name PROPERTIES_NAME = new QualifiedName("Properties");
  private static final Name KEY_NAME = new QualifiedName("Key");
  private final JdbcConnectionPool connectionPool;
  private final String tablePattern;
  private final String columnPattern;
  private final SubstitutionExpr valueExpr;
  private final String[] tableType;
  private final int maxRowsPerTable;
  private final int maxLength;
  private final String[] catalogs;

  public SqlGrep(String[] catalogs, String tablePattern, String columnPattern, SubstitutionExpr valueExpr, String[] tableType,
                 int maxRowsPerTable, int maxLength, JdbcConnectionPool connectionPool) {

    this.tablePattern = tablePattern;
	this.columnPattern = columnPattern;
	this.valueExpr = valueExpr;
	this.tableType = tableType;
	this.maxRowsPerTable = maxRowsPerTable;
    this.connectionPool = connectionPool;
	this.maxLength = maxLength;
	this.catalogs = catalogs;
  }

  public void readRecords(final ServiceContext context, final Flow flow) {
	String valuePattern = valueExpr.evaluateAsString(flow.getParameters(),flow.getRecord());
  
	Set<String> columnSet = new HashSet<String>();
  
    Connection connection = null;
    //Connection connection2 = null;
	PatternMatcher patternMatcher = likeToPatternMatcher(valuePattern);
    ResultSet resultSet = null;
    Statement stat = null;
    try {
	  boolean usesLike = valuePattern.contains("_") || valuePattern.contains("%");
	
      startRecordStream(context, flow);
      connection = connectionPool.getConnection();
	  connection.setAutoCommit(true); 
      //connection2 = connectionPool.getConnection();
	  //connection2.setAutoCommit(true);
      stat = connection.createStatement();
	  
      RecordBuilder recordBuilder = new RecordBuilder(new QualifiedName("table"));
	  DatabaseMetaData metaData = connection.getMetaData();
	  
	  String[] theCatalogs = catalogs;
	  if (theCatalogs.length == 0) {
		List<String> catalogList = new ArrayList<String>();
  	    ResultSet rscat = metaData.getCatalogs();
  	    while (rscat.next()) {
		  String catalogName = rscat.getString("TABLE_CAT");
		  catalogList.add(catalogName);
        }	  
		theCatalogs = new String[catalogList.size()];
		theCatalogs = catalogList.toArray(theCatalogs);
	  }
	  
	  //ResultSet rs = metaData.getTables(connection.getCatalog(),"%",tablePattern,tableType);
	  for (int cati = 0; cati < theCatalogs.length; ++cati) {
	  try {
	  ResultSet rs = metaData.getTables(theCatalogs[cati],"%",tablePattern,tableType);
	  boolean isFirstRow = true;
	  while (rs.next()) {
		String catalogName = rs.getString("TABLE_CAT");
		String schemaName = rs.getString("TABLE_SCHEM");
		String tableName = rs.getString("TABLE_NAME");
		
		//String s = generateIdentifyingColumns(connection.getCatalog(),null,tableName,connection);
		//System.out.println("Identifying columns="+s);
		String sql = "";
		ResultSet rsc = metaData.getColumns(catalogName,schemaName,tableName,columnPattern);
		boolean start1 = true;
		while (rsc.next()) {
  		  String columnName = rsc.getString("COLUMN_NAME");
		  if (start1) {
			sql = "SELECT " + columnName;
			start1 = false;
		  } else {
			sql = sql + "," + columnName;
		  }
		}
		if (sql.length() > 0) {
		    sql = sql + " FROM " + tableName;
			
			String where = usesLike ? generateWhereWithLike(catalogName, schemaName, tableName, valuePattern, connection)
			  : generateWhereWithoutLike(catalogName, schemaName, tableName, valuePattern, connection);
			//System.out.println(sql);
			recordBuilder.clear();
			recordBuilder.setString(CATALOG_NAME,catalogName);
			recordBuilder.setString(SCHEMA_NAME,schemaName);
			recordBuilder.setString(TABLE_NAME,tableName);
			if (where.length() > 0) {
			    Set<String> keyColumns = new HashSet<String>();
			    generateIdentifyingColumns(catalogName,schemaName,tableName,connection,keyColumns);
			    sql += where;
				try {
				  resultSet = stat.executeQuery(sql);
				  ResultSetMetaData rsMetaData = resultSet.getMetaData();
				  int columnCount = rsMetaData.getColumnCount();
				  //System.out.println("tableName=" + tableName + ",maxRowsPerTable="+maxRowsPerTable+",columnCount="+columnCount);
				  for (int rowCount = 0; rowCount < maxRowsPerTable && resultSet.next(); ++rowCount) {
					  if (isFirstRow) {
						for (int i = 1; i <= columnCount; ++i) {
						  String columnName = rsMetaData.getColumnName(i).trim();
						  String value = resultSet.getString(i);
						  if (value == null) {
						    value = "";
						  }
						  value = value.trim();
						  if (value.length() > maxLength) {
						    value = value.substring(0, maxLength);
						  }
						  if (patternMatcher.match(value)) {
						    columnSet.add(columnName);
							recordBuilder.setString(new QualifiedName(columnName),value);
						  }
						}
 					    recordBuilder.setString(KEY_NAME,"");
 					    recordBuilder.setString(PROPERTIES_NAME,"");
						isFirstRow = false;
					  }				  
					for (int i = 1; i <= columnCount; ++i){
					    String columnName = rsMetaData.getColumnName(i).trim();
						String value = resultSet.getString(i);
						if (value == null) {
						  value = "";
						}
						value = value.trim();
						if (value.length() > maxLength) {
						  value = value.substring(0, maxLength);
						}
   					    if (columnSet.contains(columnName)) {
							  recordBuilder.setString(new QualifiedName(columnName), value);
						}
					}
					for (int i = 1; i <= columnCount; ++i){
					    String columnName = rsMetaData.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
						  value = "";
						}
						value = value.trim();
						if (value.length() > maxLength) {
						  value = value.substring(0, maxLength);
						}
   					    if (keyColumns.contains(columnName)) {
							  String was = recordBuilder.getString(KEY_NAME);
							  String pair = columnName+"="+value;
							  String pairs = (was == null || was.length() == 0) ? pair : was+","+pair;
							  recordBuilder.setString(KEY_NAME, pairs);
						}
					}
					for (int i = 1; i <= columnCount; ++i){
					    String columnName = rsMetaData.getColumnName(i);
						String value = resultSet.getString(i);
						if (value == null) {
						  value = "";
						}
						value = value.trim();
						if (value.length() > maxLength) {
						  value = value.substring(0, maxLength);
						}
   					    if (!columnSet.contains(columnName) && patternMatcher.match(value)) {
							  String was = recordBuilder.getString(PROPERTIES_NAME);
							  String pair = columnName+"="+value;
							  String pairs = (was == null || was.length() == 0) ? pair : was+","+pair;
							  recordBuilder.setString(PROPERTIES_NAME, pairs);
						}
					}
  		            Record record = recordBuilder.toRecord();
                    Flow newFlow = flow.replaceRecord(context, record);
					getRecordWriter().writeRecord(context, newFlow);
				  }
				} catch (Exception e) {
				  throw e;
				} finally {
				  stat.clearBatch();
				}
			}
		}
	  }

	  } catch (SQLException e) {
	    System.out.println(e.getMessage());
	  }
}
      endRecordStream(context, flow);
}	
	  catch (Exception e) {
		throw new ServingXmlException(e.getMessage(), e);
		}
	finally {
      try {
      if (connection != null) {
        connectionPool.releaseConnection(connection);
      }
        close();
      } catch (Exception t) {
      }
	}
    
  }
  
  private String generateWhereWithLike(String catalogName, String schemaName, String tableName, 
                                       String valuePattern, Connection connection) throws SQLException {
	  DatabaseMetaData metaData = connection.getMetaData();
	  String sql = "";
			boolean start2 = true;
			ResultSet rsc2 = metaData.getColumns(catalogName,schemaName,tableName,columnPattern);
			while (rsc2.next()) {
			  String columnName = rsc2.getString("COLUMN_NAME");
			  int dataType = rsc2.getInt("DATA_TYPE");
			  switch (dataType) {
			    case Types.CHAR:
			    case Types.VARCHAR:
			    case Types.LONGVARCHAR:
				  if (start2) {
					sql = sql + " WHERE " + columnName + " LIKE \'" + valuePattern + "\'";
					start2 = false;
				  } else {
					sql = sql + " OR " + columnName + " LIKE \'" + valuePattern + "\'";
				  }
				  break;
			  }
			}
			return sql;
  }
  
  private void generateIdentifyingColumns(String catalog, 
                                            String schema, 
											String tableName, 
											Connection connection, Set<String> keyColumns) throws SQLException {
	  DatabaseMetaData metaData = connection.getMetaData();
			ResultSet rsc2 = metaData.getPrimaryKeys(catalog, schema, tableName);
			while (rsc2.next()) {
				String columnName = rsc2.getString("COLUMN_NAME");
    			keyColumns.add(columnName);
		  }
  }
  
  private String generateWhereWithoutLike(String catalogName, String schemaName, String tableName, 
                                          String valuePattern, Connection connection) throws SQLException {
	  DatabaseMetaData metaData = connection.getMetaData();
	  String sql = "";
			boolean start2 = true;
			ResultSet rsc2 = metaData.getColumns(catalogName,schemaName,tableName,columnPattern);
			while (rsc2.next()) {
			  String columnName = rsc2.getString("COLUMN_NAME");
			  int dataType = rsc2.getInt("DATA_TYPE");
			  switch (dataType) {
			    case Types.CHAR:
			    case Types.VARCHAR:
			    case Types.LONGVARCHAR:
				  if (start2) {
					sql = sql + " WHERE " + columnName + "=\'" + valuePattern + "\'";
					start2 = false;
				  } else {
					sql = sql + " OR " + columnName + "=\'" + valuePattern + "\'";
				  }
				  break;
			  }
			}
			return sql;
  }
  
  public PatternMatcher likeToPatternMatcher(String pattern) {
    StringBuilder buffer = new StringBuilder();
	char lastChar;
	buffer.append("^");
	for (int i = 0; i < pattern.length(); ++i) {
	  char ch = pattern.charAt(i);
	  if (ch == '%') {
	    buffer.append(".*");
	  } else if (ch == '_') {
	    buffer.append(".");
	  } else {
	    buffer.append(ch);
	  }
	}
	buffer.append("$");
	String s = buffer.toString();
	//System.out.println("REGEX for " + pattern + "=" + s + "!");
	
	PatternMatcher matcher = new JavaPatternMatcher(s,true,true);
	return matcher;
  }
}



