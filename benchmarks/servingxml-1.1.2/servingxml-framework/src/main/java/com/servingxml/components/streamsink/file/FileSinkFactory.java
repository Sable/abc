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

package com.servingxml.components.streamsink.file; 

import java.io.File;

import com.servingxml.app.ServiceContext;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsink.file.FileSink;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.app.Flow;
import java.nio.charset.Charset;
import com.servingxml.util.ServingXmlException;

public class FileSinkFactory implements StreamSinkFactory {
  
  private final SubstitutionExpr directoryResolver;
  private final SubstitutionExpr fileResolver;
  private final File parent;
  private final Charset charset;
  
  public FileSinkFactory(SubstitutionExpr directoryResolver, SubstitutionExpr fileResolver, File parent,
                         Charset charset) {
    this.directoryResolver = directoryResolver;
    this.fileResolver = fileResolver;
    this.parent = parent;
    this.charset = charset;
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }

  public StreamSink createStreamSink(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".createStreamSink " + flow.getRecord().toXmlString(context));

    try {
      String dirname = directoryResolver.evaluateAsString(flow.getParameters(),flow.getRecord());
      String filename = fileResolver.evaluateAsString(flow.getParameters(), flow.getRecord());
      File file = dirname.length() > 0 ? new File(dirname,filename) : new File(filename);
      if (!file.isAbsolute()) {
        file = new File(parent,file.getPath());
      }
      return new FileSink(file, charset);
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}
