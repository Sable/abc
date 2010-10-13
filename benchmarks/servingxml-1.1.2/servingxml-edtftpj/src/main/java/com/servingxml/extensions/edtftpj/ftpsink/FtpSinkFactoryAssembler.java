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

import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FTPConnectMode;

import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.extensions.edtftpj.connect.FtpClient;
import com.servingxml.components.common.TrueFalseEnum;

public class FtpSinkFactoryAssembler {
  
  private FtpClient ftpClient = null;
  private FTPTransferType transferType = FTPTransferType.ASCII;
  private FTPConnectMode connectMode = FTPConnectMode.PASV;
  private String remoteDir = "";
  private String remoteFile = null;
  private String localDir = "";
  private String localFile = "";
  private String deleteLocalFile = TrueFalseEnum.FALSE.toString();
  
  public void setRemoteFile(String remoteFile) {
    this.remoteFile = remoteFile;
  }

  public void setDeleteLocalFile(String deleteLocalFile) {
    this.deleteLocalFile = deleteLocalFile;
  }
  
  public void setRemoteDir(String remoteDir) {
    this.remoteDir = remoteDir;
  }

  public void setRemoteDirectory(String remoteDir) {
    this.remoteDir = remoteDir;
  }

  public void setLocalFile(String localFile) {
    this.localFile = localFile;
  }

  public void setLocalDir(String localDir) {
    this.localDir = localDir;
  }
  
  public void setTransferType(String type) {
    this.transferType = type.equals("binary") ? FTPTransferType.BINARY 
      : FTPTransferType.ASCII;
  }
  
  public void injectComponent(FtpClient ftpClient) {
    this.ftpClient = ftpClient;
  }

  public StreamSinkFactory assemble(ConfigurationContext context) {
    
    if (ftpClient == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"edt:ftpClient");
      throw new ServingXmlException(message);
    }
    
    if (remoteFile == null || remoteFile.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"remoteFile");
      throw new ServingXmlException(message);
    }

    TrueFalseEnum deleteLocalIndicator;
    try {
      deleteLocalIndicator = TrueFalseEnum.parse(deleteLocalFile);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
        context.getElement().getTagName(), "deleteLocalFile");
      e = e.supplementMessage(message);
      throw e;
    }

    try {
      SubstitutionExpr remoteDirResolver = SubstitutionExpr.parseString(context.getQnameContext(),remoteDir);
      SubstitutionExpr remoteFileResolver = SubstitutionExpr.parseString(context.getQnameContext(),remoteFile);

      SubstitutionExpr localDirResolver = SubstitutionExpr.parseString(context.getQnameContext(),localDir);
      SubstitutionExpr localFileResolver = SubstitutionExpr.parseString(context.getQnameContext(),localFile);

      return new FtpSinkFactory(ftpClient,remoteDirResolver,remoteFileResolver,
        transferType,connectMode,
        localDirResolver,localFileResolver,deleteLocalIndicator.booleanValue());
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}
