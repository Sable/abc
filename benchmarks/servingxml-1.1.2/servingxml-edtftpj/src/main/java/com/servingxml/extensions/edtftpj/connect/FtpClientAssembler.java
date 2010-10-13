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

import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FTPConnectMode;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;

public class FtpClientAssembler {
  
  private String host;
  private int port = 21;
  private String user = "";
  private String password = "";
  private String remoteSiteCommand = "";
  
  public void setHost(String host) {
    this.host = host;
  }
  
  public void setPort(int port) {
    this.port = port;
  }
  
  public void setUser(String user) {
    this.user = user;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public void setRemoteSiteCommand(String remoteSiteCommand) {
    this.remoteSiteCommand = remoteSiteCommand; 
  }

  public FtpClient assemble(ConfigurationContext context) {
    
    if (host == null || host.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"host");
      throw new ServingXmlException(message);
    }
    
    if (user == null || user.length() == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"user");
      throw new ServingXmlException(message);
    }

    try {
      return new FtpClient(host,port,user,password,remoteSiteCommand);
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}
