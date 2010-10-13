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

package com.servingxml.components.streamsource.file;

import java.io.File;
import java.nio.charset.Charset;

import com.servingxml.app.ServiceContext;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsource.file.FileSource;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.util.record.Record;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.app.Flow;

/**
 * The <code>FileSourceFactory</code> implements a class that
 * creates <code>StreamSource</code objects.
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                          
public class FileSourceFactory 
implements StreamSourceFactory {
                                                       
  private final SubstitutionExpr directoryResolver;
  private final SubstitutionExpr fileResolver;
  private final File parent;
  private final Charset charset;
  
  FileSourceFactory(SubstitutionExpr directoryResolver, SubstitutionExpr fileResolver, 
                    File parent, Charset charset) {
    this.directoryResolver = directoryResolver;
    this.fileResolver = fileResolver;
    this.parent = parent;
    this.charset = charset;
  }

  public StreamSource createStreamSource(ServiceContext context, Flow flow) {
    Record parameters = flow.getParameters();
    Record record = flow.getRecord();

    String dirname = directoryResolver.evaluateAsString(parameters,record);
    String filename = fileResolver.evaluateAsString(parameters,record);
    File file = dirname.length() > 0 ? new File(dirname,filename) : new File(filename);

    if (!file.isAbsolute()) {
      file = new File(parent,file.getPath());
    }

    return new FileSource(file,charset);
  }
}

