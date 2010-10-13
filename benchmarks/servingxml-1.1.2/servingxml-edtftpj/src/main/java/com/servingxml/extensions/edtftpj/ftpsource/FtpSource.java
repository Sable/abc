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

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.nio.charset.Charset;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FTPConnectMode;

import com.servingxml.util.ServingXmlException;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.DefaultKey;
import com.servingxml.extensions.edtftpj.connect.FtpClient;

public class FtpSource implements StreamSource {

  private final FtpClient connectionPool;
  private final String remoteDir;
  private final String remoteFile;
  private final FTPTransferType transferType;
  private final FTPConnectMode connectMode;
  private final File localFile;
  private final boolean deleteLocalFile;
  private final Key key;
  private final Charset charset;

  public FtpSource(FtpClient connectionPool, String remoteDir, String remoteFile, 
    FTPTransferType transferType, FTPConnectMode connectMode, File localFile, boolean deleteLocalFile) {
    this.connectionPool = connectionPool;
    this.remoteDir = remoteDir;
    this.remoteFile = remoteFile;
    this.transferType = transferType;
    this.connectMode = connectMode;
    this.localFile = localFile;
    this.deleteLocalFile = deleteLocalFile;
    this.key = DefaultKey.newInstance();
    this.charset = null;
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }

  public Key getKey() {
    return key;
  }

  public Expirable getExpirable() {
    return Expirable.IMMEDIATE_EXPIRY;
  }

  public String getSystemId() {
    return remoteFile;
  }

  public InputStream openStream() {

    return new FtpInputStream(localFile);
  }

  public void closeStream(InputStream is) throws IOException {
    if (is != null) {
      is.close();
    }
  }

  class FtpInputStream extends InputStream {
    private final File localFile;
    private final InputStream is;

    public FtpInputStream(File localFile) {
      this.localFile = localFile;
      FTPClient connection = connectionPool.getConnection();
      boolean good = false;
      try {
        // change remote directory to the remoteDir
        if (remoteDir.length() > 0) {
          connection.chdir(remoteDir);
        }
        connection.setType(transferType);
        connection.setConnectMode(connectMode);
        good = true;
      } catch (IOException e) {
        throw new ServingXmlException(e.getMessage(), e);
      } catch (FTPException e) {
        throw new ServingXmlException(e.getMessage(), e);
      } finally {
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

      try {
        connection.get(localFile.getPath(),remoteFile);

        InputStream stream = new FileInputStream(localFile);

        this.is = new BufferedInputStream(stream);
      } catch (IOException e) {
        throw new ServingXmlException(e.getMessage(), e);
      } catch (FTPException e) {
        throw new ServingXmlException(e.getMessage(), e);
      }
    }

    public int read() throws IOException {
      return is.read();
    }
    public int read(byte b[], int off, int len) throws IOException {
      return is.read(b,off,len);
    }
    public int read(byte b[]) throws IOException {
      return is.read(b);
    }
    public synchronized void mark(int readlimit) {
      is.mark(readlimit);
    }
    public long skip(long n) throws IOException {
      return is.skip(n);
    }
    public int available() throws IOException {
      return is.available();
    }
    public void close() {
      ServingXmlException badDispose = null;
      if (is != null) {
        try {
          is.close();
        } catch (Exception e) {
          badDispose = new ServingXmlException(e.getMessage(),e);
        }
      }
      if (deleteLocalFile) {
        if (localFile != null) {
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

    public synchronized void reset() throws IOException {
      is.reset();
    }
    public boolean markSupported() {
      return is.markSupported();
    }
  }
}

