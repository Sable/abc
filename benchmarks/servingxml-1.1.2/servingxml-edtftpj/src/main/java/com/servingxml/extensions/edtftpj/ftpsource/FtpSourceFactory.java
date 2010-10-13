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

package com.servingxml.extensions.edtftpj.ftpsource; 

import java.io.File;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FTPConnectMode;

import com.servingxml.util.record.Record;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.extensions.edtftpj.connect.FtpClient;
import com.servingxml.app.Flow;

public class FtpSourceFactory implements StreamSourceFactory {
  
  private final FtpClient ftpClient;
  private final FTPTransferType transferType;
  private final FTPConnectMode connectMode;
  private final SubstitutionExpr remoteDirResolver;
  private final SubstitutionExpr remoteFileResolver;
  private final SubstitutionExpr localDirResolver;
  private final SubstitutionExpr localFileResolver;
  private final boolean deleteLocalFile;
  
  public FtpSourceFactory(FtpClient ftpClient,
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
  }

  public StreamSource createStreamSource(ServiceContext context, Flow flow) {
    Record parameters = flow.getParameters();
    Record record = flow.getRecord();
    String remoteDir = remoteDirResolver.evaluateAsString(parameters,record);
    String remoteFile = remoteFileResolver.evaluateAsString(parameters,record);
    String localDir = localDirResolver.evaluateAsString(parameters,record);
    String localFilename = localFileResolver.evaluateAsString(parameters,record);


    if (localFilename.length() == 0) {
      localFilename = "servingxml" + System.currentTimeMillis();
    }

    File localFile = localDir.length() > 0 ? new File(localDir,localFilename) : new File(localFilename);
    return new FtpSource(ftpClient,remoteDir,remoteFile, transferType,connectMode, localFile, deleteLocalFile);
  }
}
