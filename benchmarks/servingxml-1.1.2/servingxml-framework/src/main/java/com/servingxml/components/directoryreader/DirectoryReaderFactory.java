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

import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.app.Flow;
import com.servingxml.components.recordio.AbstractRecordReaderFactory;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.components.recordio.RecordFilterAppender;

/**
 * A <code>DirectoryReaderFactory</code> instance may be used to obtain objects that
 * implement the <code>RecordReader</code> interface.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DirectoryReaderFactory extends AbstractRecordReaderFactory 
implements RecordReaderFactory, RecordFilterAppender {     

  private final SubstitutionExpr dirResolver;
  private final File parent;
  private final boolean recurse;
  private final long maxItems;
  private final FileFilter fileFilter;
  
  public DirectoryReaderFactory(SubstitutionExpr dirResolver, File parent,
  boolean recurse, long maxItems, FileFilter fileFilter) {
    this.dirResolver = dirResolver;
    this.parent = parent;
    this.recurse = recurse;
    this.maxItems = maxItems;
    this.fileFilter = fileFilter;
  }

  protected RecordReader createRecordReader(ServiceContext context, Flow flow) {

    String dir = dirResolver.evaluateAsString(flow.getParameters(), flow.getRecord());
    File file = new File(dir);
    if (!file.isAbsolute()) {
      file = new File(parent,file.getPath());
    }
    if (!file.isDirectory()) {
      String message = file.getPath() + " is not a directory.";
      throw new ServingXmlException(message);
    }

    RecordReader recordReader = new DirectoryReader(file,recurse,maxItems,fileFilter);
    return recordReader;
  }
}
