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

public class SqlReader extends AbstractRecordReader implements RecordReader {

  private final JdbcConnectionPool connectionPool;
  private final SqlQuery sqlQuery;

  public SqlReader(JdbcConnectionPool connectionPool, SqlQuery sqlQuery) {

    this.connectionPool = connectionPool;
    this.sqlQuery = sqlQuery;
  }

  protected void readRecords(ServiceContext context, Flow flow, ReadRecord readRecord) {

    Connection connection = null;
    ResultSet resultSet = null;
    Statement stat = null;
    boolean lastAutoCommit = false;

    String sql = sqlQuery.buildSql(context, flow);
    //System.out.println(getClass().getName()+".readRecords " + sql);

    Name recordTypeName = sqlQuery.getRecordTypeName();

    try {
      connection = connectionPool.getConnection();
      lastAutoCommit = connection.getAutoCommit();
      connection.setAutoCommit(true);

      stat = connection.createStatement();
      resultSet = stat.executeQuery(sql);

      ResultSetMetaData metaData = resultSet.getMetaData();

      RecordBuilder builder = new RecordBuilder(recordTypeName);
      int columnCount = metaData.getColumnCount();
      for (int i = 1; i <= columnCount; ++i) {
        String columnLabel = metaData.getColumnLabel(i);
        FieldType fieldType = new DefaultFieldType(new QualifiedName(columnLabel), columnLabel);
        builder.setValue(fieldType,Value.EMPTY);
      }
      RecordType recordType = builder.toRecord().getRecordType();
      while (resultSet.next()) {
        RecordBuilder recordBuilder = new RecordBuilder(recordType);

        int fieldCount = recordType.count();
        //System.out.println(getClass().getName()+".readRecords read one");
        for (int i = 0; i < fieldCount; ++i) {
          FieldType fieldType = recordType.getFieldType(i);
          try {
            int columnIndex = resultSet.findColumn(fieldType.getName().getLocalName());
            if (columnIndex != -1) {
              int columnType = metaData.getColumnType(columnIndex);
              switch (columnType) {
                case Types.TIMESTAMP:
                  {
                    Timestamp val = resultSet.getTimestamp(columnIndex);
                    recordBuilder.setDateTime(fieldType.getName(),val);
                  }
                  break;
                case Types.DATE:
                  {
                    Date val = resultSet.getDate(columnIndex);
                    recordBuilder.setDate(fieldType.getName(),val);
                  }
                  break;
                case Types.TIME:
                  {
                    Time val = resultSet.getTime(columnIndex);
                    recordBuilder.setTime(fieldType.getName(),val);
                  }
                  break;
                case Types.DECIMAL:
                case Types.NUMERIC:
                  {
                    BigDecimal val = resultSet.getBigDecimal(columnIndex);
                    recordBuilder.setBigDecimal(fieldType.getName(),val);
                  }
                  break;
                case Types.BIT:
                  {
                    boolean val = resultSet.getBoolean(columnIndex);
                    if (!resultSet.wasNull()) {
                      recordBuilder.setBoolean(fieldType.getName(),val);
                    } else {
                      recordBuilder.setBoolean(fieldType.getName(),null);
                    }
                  }
                  break;
                case Types.TINYINT:
                  {
                    byte val = resultSet.getByte(columnIndex);
                    if (!resultSet.wasNull()) {
                      recordBuilder.setByte(fieldType.getName(),val);
                    } else {
                      recordBuilder.setByte(fieldType.getName(),null);
                    }
                  }
                  break;
                case Types.SMALLINT:
                  {
                    short val = resultSet.getShort(columnIndex);
                    if (!resultSet.wasNull()) {
                      recordBuilder.setShort(fieldType.getName(),val);
                    } else {
                      recordBuilder.setShort(fieldType.getName(),null);
                    }
                  }
                  break;
                case Types.INTEGER:
                  {
                    int val = resultSet.getInt(columnIndex);
                    if (!resultSet.wasNull()) {
                      recordBuilder.setInteger(fieldType.getName(),val);
                    } else {
                      recordBuilder.setInteger(fieldType.getName(),null);
                    }
                  }
                  break;
                case Types.BIGINT:
                  {
                    long val = resultSet.getLong(columnIndex);
                    if (!resultSet.wasNull()) {
                      recordBuilder.setLong(fieldType.getName(),val);
                    } else {
                      recordBuilder.setLong(fieldType.getName(),null);
                    }
                  }
                  break;
                case Types.FLOAT:
                  {
                    float val = resultSet.getFloat(columnIndex);
                    if (!resultSet.wasNull()) {
                      recordBuilder.setFloat(fieldType.getName(),val);
                    } else {
                      recordBuilder.setFloat(fieldType.getName(),null);
                    }
                  }
                  break;
                case Types.DOUBLE:
                  {
                    double val = resultSet.getDouble(columnIndex);
                    if (!resultSet.wasNull()) {
                      recordBuilder.setDouble(fieldType.getName(),val);
                    } else {
                      recordBuilder.setDouble(fieldType.getName(),null);
                    }
                  }
                  break;
                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                  {
                    byte[] val = resultSet.getBytes(columnIndex);
                    if (!resultSet.wasNull()) {
                      recordBuilder.setHexBinary(fieldType.getName(),val);
                    } else {
                      recordBuilder.setHexBinary(fieldType.getName(),null);
                    }
                  }
                  break;
                case Types.LONGVARCHAR:
                case Types.VARCHAR:
                case Types.CHAR:
                  {
                    String value = resultSet.getString(columnIndex);
                    if (value != null) {
                      if (sqlQuery.isTrimLeading() && sqlQuery.isTrimTrailing()) {
                        value = value.trim();
                      } else if (sqlQuery.isTrimLeading()) {
                        value = StringHelper.trimLeading(value);
                      } else if (sqlQuery.isTrimTrailing()) {
                        value = StringHelper.trimTrailing(value);
                      }
                    }
                    recordBuilder.setString(fieldType.getName(),value);
                  }
                  break;
                case Types.CLOB:
                  String value = null;
                  Clob clob = resultSet.getClob(columnIndex);
                  if (clob != null) {
                    StringBuilder sb = new StringBuilder((int)clob.length());
                    Reader reader = clob.getCharacterStream();
                    char[] buf = new char[2048];
                    boolean done = false;
                    while (!done) {
                      int count = reader.read(buf, 0, buf.length);
                      if (count == -1) {
                        done = true;
                      } else if (count > 0) {
                        sb.append(buf, 0, count);
                      }
                    }
                    value = sb.toString();
                  }
                  recordBuilder.setString(fieldType.getName(),value);
                  break;
              }

            }
          } catch (SQLException e) {
            throw new ServingXmlException(e.getMessage(),e);
          }
        }
        Record record = recordBuilder.toRecord();
        readRecord.recordRead(record);
      }

    } catch (SQLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      if (resultSet != null) {
        try {
          resultSet.close();
        } catch (Exception rse) {
          //  Don't care
        }
      }
      if (stat != null) {
        try {
          stat.close();
        } catch (Exception se) {
          //  Don't care
        }
      }
      if (connection != null) {
        try {
          connection.setAutoCommit(lastAutoCommit);
        } catch (SQLException e2) {
          throw new ServingXmlException(e2.getMessage());
        }
        connectionPool.releaseConnection(connection);
      }
    }
  }

  public void readRecords(final ServiceContext context, final Flow flow) {
    try {
      startRecordStream(context, flow);

      ReadRecord readRecord = new ReadRecord() {
        public void recordRead(Record record) {
          Flow newFlow = flow.replaceRecord(context, record);
          //System.out.println(getClass().getName()+".readRecords wrote one");
          getRecordWriter().writeRecord(context, newFlow);
        }
      };
      readRecords(context, flow, readRecord);

      endRecordStream(context, flow);
    } finally {
      try {
        close();
      } catch (Exception t) {
      }
    }
  }
}

interface ReadRecord {
  void recordRead(Record record);
}



