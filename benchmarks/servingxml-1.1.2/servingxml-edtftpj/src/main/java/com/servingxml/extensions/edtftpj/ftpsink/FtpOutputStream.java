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
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FTPConnectMode;

import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.extensions.edtftpj.connect.FtpClient;
import com.servingxml.util.ServingXmlException;

class FtpOutputStream extends OutputStream {
  private final FtpClient ftpClient;
  private final FTPTransferType transferType;
  private final FTPConnectMode connectMode;
  private final String remoteDir;
  private final String remoteFile;
  private final File localFile;
  private final OutputStream os;
  private final boolean deleteLocalFile;

  public FtpOutputStream(FtpClient ftpClient, String remoteDir, String remoteFile,
    FTPTransferType transferType, FTPConnectMode connectMode, 
    File localFile, boolean deleteLocalFile) throws IOException {
    this.ftpClient = ftpClient;
    this.transferType = transferType;
    this.connectMode = connectMode;
    this.localFile = localFile;
    this.remoteDir = remoteDir;
    this.remoteFile = remoteFile;
    OutputStream stream = new FileOutputStream(localFile);
    os = new BufferedOutputStream(stream);
    this.deleteLocalFile = deleteLocalFile;
  }

  public void write(int b) throws IOException {
    os.write(b);
  }

  public void close() {

    ServingXmlException badDispose = null;

    FTPClient connection = null;
    try {
      os.close();
      connection = ftpClient.getConnection();
      // change remote directory to the remoteDir
      if (remoteDir.length() > 0) {
        connection.chdir(remoteDir);
      }
      connection.setType(transferType);
      connection.setConnectMode(connectMode);
      connection.put(localFile.getPath(),remoteFile);
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      if (connection != null) {
        try {
          connection.quit();
        } catch (Exception e) {
          badDispose = new ServingXmlException(e.getMessage(),e);
        }
      }
      if (deleteLocalFile) {
        try {
          localFile.delete();
        } catch (Exception e) {
          badDispose = new ServingXmlException(e.getMessage(),e);
        }
      }
    }
    if (badDispose != null) {
      throw badDispose;
    }
  }

  public void write(byte b[]) throws IOException {
    os.write(b);
  }
  public void write(byte b[], int off, int len) throws IOException {
    os.write(b,off,len);
  }
  public void flush() throws IOException {
    os.flush();
  }
}

