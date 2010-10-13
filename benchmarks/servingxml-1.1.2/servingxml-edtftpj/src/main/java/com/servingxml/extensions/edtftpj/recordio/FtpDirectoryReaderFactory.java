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

import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.components.recordio.RecordFilterAppender;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.extensions.edtftpj.connect.FtpClient;
import com.servingxml.app.Flow;
import com.servingxml.components.recordio.AbstractRecordReaderFactory;

/**                                    
 * A <code>FtpDirectoryReaderFactory</code> instance may be used to obtain objects that
 * implement the <code>RecordReader</code> interface.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FtpDirectoryReaderFactory extends AbstractRecordReaderFactory 
implements RecordReaderFactory, RecordFilterAppender {     

  private final FtpClient ftpClient;
  private final SubstitutionExpr dirResolver;
  private final boolean recurse;
  private final long maxItems;
  
  public FtpDirectoryReaderFactory(FtpClient ftpClient, SubstitutionExpr dirResolver,
  boolean recurse, long maxItems) {
    this.ftpClient = ftpClient;
    this.dirResolver = dirResolver;
    this.recurse = recurse;
    this.maxItems = maxItems;
  }                                  
 
  protected RecordReader createRecordReader(ServiceContext context, Flow flow) {
    String dir = dirResolver.evaluateAsString(flow.getParameters(), flow.getRecord());

    RecordReader recordReader = new FtpDirectoryReader(ftpClient, dir, recurse, maxItems);
    return recordReader;
  }
}
