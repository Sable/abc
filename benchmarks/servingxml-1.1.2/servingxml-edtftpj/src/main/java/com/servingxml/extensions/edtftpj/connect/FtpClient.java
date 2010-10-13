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

package com.servingxml.extensions.edtftpj.connect;

import java.io.IOException;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FTPConnectMode;

import com.servingxml.util.ServingXmlException;

public class FtpClient {

  private final String host;
  private final int port;
  private final String user;
  private final String password;
  private final String remoteSiteCommand;

  public FtpClient(String host, int port, String user, String password,
                           String remoteSiteCommand) {
    this.host = host;
    this.port = port;
    this.user = user;
    this.password = password;
    this.remoteSiteCommand = remoteSiteCommand;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getUser() {
    return user;
  }

  public String getRemoteSiteCommand() {
    return remoteSiteCommand;
  }

  public FTPClient getConnection() {
    FTPClient connection = null;

    try {
      connection = new FTPClient(host, port);
    } catch (FTPException e) {
      String msg = "Unable to connect to host " + host + " on port " + port + ": " + e.getMessage();
      throw new ServingXmlException(msg);
    } catch (IOException e) {
      String msg = "Unable to connect to host " + host + " on port " + port + ": " + e.getMessage();
      throw new ServingXmlException(msg);
    }
    // log in

    try {
      connection.login(user, password);
    } catch (FTPException e) {
      String msg="User " + user + "failed attempting to logger on to host " +
                 host + " on port " + port + ": " + e.getMessage();
      throw new ServingXmlException(msg);
    } catch (IOException e ) {
      String msg="User " + user + "failed attempting to logger on to host " +
                 host + " on port " + port + ": " + e.getMessage();
      throw new ServingXmlException(msg);
    }
    try {
      if (remoteSiteCommand != null && remoteSiteCommand.length() > 0) {
        connection.site(remoteSiteCommand);
      }
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    } catch (FTPException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
    return connection;
  }
}

