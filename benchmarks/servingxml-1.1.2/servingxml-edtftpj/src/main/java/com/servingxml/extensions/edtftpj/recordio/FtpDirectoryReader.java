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

package com.servingxml.extensions.edtftpj.recordio;

import java.io.IOException;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPException;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.AbstractRecordReader;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.extensions.edtftpj.connect.FtpClient;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.app.Flow;
import com.servingxml.util.QualifiedName;

/**
 * A <code>FtpDirectoryReader</code> implements a <code>RecordReader</code> interface.
 *
 * 
 * @author  Daniel A. Parker
 */

public class FtpDirectoryReader extends AbstractRecordReader implements RecordReader {

  private static final Name DIR_RECORD_TYPE_NAME = new QualifiedName("directory");
  private static final Name FILE_RECORD_TYPE_NAME = new QualifiedName("file");

  private static final Name FILE_PARENT = new QualifiedName("parentDir");
  private static final Name PARENT_DIRECTORY = new QualifiedName("parentDirectory");
  private static final Name FILE_NAME = new QualifiedName("name");
  private static final Name FILE_LAST_MODIFIED = new QualifiedName("lastModified");
  private static final Name FILE_SIZE = new QualifiedName("size");
  private static final Name FILE_PERMISSIONS = new QualifiedName("permissions");
  private static final Name FILE_OWNER = new QualifiedName("owner");
  private static final Name FILE_IS_LINK = new QualifiedName("isLink");

  private final FtpClient connectionPool;
  private final String remoteDir;
  private final boolean recurse;
  private final long maxItems;
  private long itemCount = 0;       

  public FtpDirectoryReader(FtpClient connectionPool, String remoteDir, 
  boolean recurse, long maxItems) {

    this.connectionPool = connectionPool;
    this.remoteDir = remoteDir;
    this.recurse = recurse;
    this.maxItems = maxItems;
  }

  public void readRecords(ServiceContext context, Flow flow) {
    // create an instance of an FTPClient object
    FTPClient connection = null;
    boolean good = false;

    itemCount = 0;
    try {
      startRecordStream(context,flow);
      connection = connectionPool.getConnection();
      if (remoteDir.length() > 0) {
        connection.chdir(remoteDir);
      }
      readRecords(context, flow, connection);
      endRecordStream(context,flow);
      
      good = true;
    } catch (FTPException e) {
      throw new ServingXmlException(e.getMessage(), e);
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    } finally {
      try {
        close();
      } catch (Exception e) {
        //  Don't care
      }
      try {
        if (connection != null) {
          connection.quit();
        }
      } catch (Exception e) {
        if (good) {
          throw new ServingXmlException(e.getMessage(),e);
        }
      }
    }
  }

  protected void readRecords(ServiceContext context, Flow flow, FTPClient connection) {
    RecordBuilder dirRecordBuilder = new RecordBuilder(DIR_RECORD_TYPE_NAME);
    RecordBuilder fileRecordBuilder = new RecordBuilder(FILE_RECORD_TYPE_NAME);

    try {
      String dir = connection.pwd();
      FTPFile[] files = connection.dirDetails(dir);
      for (int i = 0; i < files.length && itemCount < maxItems; ++i) {
        FTPFile file = files[i];
        RecordBuilder recordBuilder = file.isDir() ? dirRecordBuilder : fileRecordBuilder;
        recordBuilder.setString(FILE_NAME,file.getName());
        recordBuilder.setString(FILE_PARENT,dir);
        recordBuilder.setString(PARENT_DIRECTORY,dir);
        recordBuilder.setDateTime(FILE_LAST_MODIFIED,file.lastModified().getTime());
        recordBuilder.setLong(FILE_SIZE,file.size());
        Record record = recordBuilder.toRecord();
        recordBuilder.setString(FILE_PERMISSIONS,file.getPermissions());
        recordBuilder.setString(FILE_OWNER,file.getOwner());
        String link = file.isLink() ? "yes" : "no";
        recordBuilder.setString(FILE_IS_LINK,link);

        Flow newFlow = flow.replaceRecord(context, record);
        getRecordWriter().writeRecord(context, newFlow);
        ++itemCount;
        if (recurse && file.isDir() && itemCount < maxItems) {
          String subdir = file.getName();
          try {
            connection.chdir(subdir);
            readRecords(context, newFlow, connection);
          } catch (Exception e) {
            String message = "Failed attempting to read directory " + subdir + ".  " + e.getMessage();
            context.warning(message);
          }
          connection.chdir(dir);
        }
        recordBuilder.clear();
      }
    } catch (FTPException e) {
      throw new ServingXmlException(e.getMessage(), e);
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    } catch (java.text.ParseException e) {
      throw new ServingXmlException(e.getMessage(), e);
    } 
  }
}

