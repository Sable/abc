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

package com.servingxml.components.directoryreader;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

import com.servingxml.app.Flow;
import com.servingxml.io.saxsource.RecordSaxSource;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.AbstractRecordReader;
import com.servingxml.components.recordio.RecordWriter;

/**
 * A <code>DirectoryReader</code> implements a <code>RecordReader</code> interface.
 *
 * 
 * @author  Daniel A. Parker
 */

public class DirectoryReader extends AbstractRecordReader implements RecordReader {

  private static final Name DIR_RECORD_TYPE_NAME = new QualifiedName("directory");
  private static final Name FILE_RECORD_TYPE_NAME = new QualifiedName("file");

  private static final Name FILE_PARENT = new QualifiedName("parentDir");
  private static final Name PARENT_DIRECTORY = new QualifiedName("parentDirectory");
  private static final Name FILE_PATHNAME = new QualifiedName("pathname");
  private static final Name FILE_NAME = new QualifiedName("name");
  private static final Name FILE_LAST_MODIFIED = new QualifiedName("lastModified");
  private static final Name FILE_SIZE = new QualifiedName("size");
  
  private final File directory;
  private final boolean recurse;
  private final long maxItems;
  private final FileFilter fileFilter;
  private long itemCount = 0;

  public DirectoryReader(File directory, boolean recurse, long maxItems, FileFilter fileFilter) {

    this.directory = directory;
    this.recurse = recurse;
    this.maxItems = maxItems;
    this.fileFilter = fileFilter;
  }

  public void readRecords(ServiceContext context, Flow flow) {
    try {
      itemCount = 0;
      startRecordStream(context, flow);
      readRecords(context, flow, directory);
      endRecordStream(context, flow);
    } finally {
      try {
        close();
      } catch (Exception e) {
        //  Don't care
      }
    }
  }

  protected void readRecords(ServiceContext context, Flow flow, File directory) {
    RecordBuilder dirRecordBuilder = new RecordBuilder(DIR_RECORD_TYPE_NAME);
    RecordBuilder fileRecordBuilder = new RecordBuilder(FILE_RECORD_TYPE_NAME);
    
    if (directory.isDirectory()) {
      File files[] = directory.listFiles(fileFilter);
      for (int i = 0; i < files.length && itemCount < maxItems; ++i) {
        File file = files[i];
        RecordBuilder recordBuilder = file.isDirectory() ? dirRecordBuilder 
          : fileRecordBuilder;
        recordBuilder.setString(FILE_PATHNAME,file.getPath());
        recordBuilder.setString(FILE_PARENT,file.getParent());
        recordBuilder.setString(PARENT_DIRECTORY,file.getParent());
        recordBuilder.setString(FILE_NAME,file.getName());
        recordBuilder.setDateTime(FILE_LAST_MODIFIED,file.lastModified());
        recordBuilder.setLong(FILE_SIZE,file.length());
        Record record = recordBuilder.toRecord();
        Flow newFlow = flow.replaceRecord(context, record);
        writeRecord(context, newFlow);
        ++itemCount;
        if (recurse && file.isDirectory() && itemCount < maxItems) {

          readRecords(context, newFlow, file);
        }
        recordBuilder.clear();
      }
    }
  }
}

