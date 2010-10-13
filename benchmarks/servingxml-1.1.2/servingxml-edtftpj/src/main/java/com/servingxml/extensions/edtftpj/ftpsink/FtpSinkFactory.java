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

package com.servingxml.extensions.edtftpj.ftpsink; 

import java.io.File;
import java.nio.charset.Charset;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FTPConnectMode;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.extensions.edtftpj.connect.FtpClient;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.record.Record;
import com.servingxml.util.ServingXmlException;

public class FtpSinkFactory implements StreamSinkFactory {
  
  private final FtpClient ftpClient;
  private final FTPTransferType transferType;
  private final FTPConnectMode connectMode;
  private final SubstitutionExpr remoteDirResolver;
  private final SubstitutionExpr remoteFileResolver;
  private final SubstitutionExpr localDirResolver;
  private final SubstitutionExpr localFileResolver;
  private final boolean deleteLocalFile;
  private final Charset charset;
  
  public FtpSinkFactory(FtpClient ftpClient,
  SubstitutionExpr remoteDirResolver, SubstitutionExpr remoteFileResolver,
  FTPTransferType transferType, FTPConnectMode connectMode, 
  SubstitutionExpr localDirResolver, SubstitutionExpr localFileResolver, boolean deleteLocalFile) {
    this.ftpClient = ftpClient;
    this.remoteDirResolver = remoteDirResolver;
    this.remoteFileResolver = remoteFileResolver;
    this.transferType = transferType;
    this.connectMode = connectMode;
    this.localDirResolver = localDirResolver;
    this.localFileResolver = localFileResolver;
    this.deleteLocalFile = deleteLocalFile;
    this.charset = null;
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }

  public StreamSink createStreamSink(ServiceContext context, Flow flow) {
    try {
      Record parameters = flow.getParameters();

      String remoteDir = remoteDirResolver.evaluateAsString(parameters,flow.getRecord());

      String remoteFile = remoteFileResolver.evaluateAsString(parameters,flow.getRecord());
      String localDir = localDirResolver.evaluateAsString(parameters,flow.getRecord());
      String localFilename = localFileResolver.evaluateAsString(parameters,flow.getRecord());
      if (localFilename.length() == 0) {
        localFilename = "servingxml" + System.currentTimeMillis();
      }
      File localFile = localDir.length() > 0 ? new File(localDir,localFilename) : new File(localFilename);

      StreamSink streamSink = new FtpStreamSink(ftpClient, remoteDir, remoteFile, 
        transferType, connectMode, localFile, deleteLocalFile, charset);
      return streamSink;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

}
