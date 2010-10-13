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

package com.servingxml.extensions.mail.task;

import javax.mail.internet.InternetAddress;
import javax.mail.MessagingException;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;

/**
 * Factory for creating a <tt>MailAccount</tt> instances.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MailAccountAssembler {
  
  private String smtpHost = "";
  private InternetAddress sender = null;

  public void setSmtpHost(String smtpHost) {
    this.smtpHost = smtpHost;
  }

  public void injectComponent(InternetAddress sender) {
    this.sender = sender;
  }
  
  public MailAccount assemble(ConfigurationContext context) {

    if (sender == null) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"jm:sender");
      throw new ServingXmlException(msg);
    }

    MailAccount mailAccount = new MailAccount(smtpHost, sender);

    return mailAccount;
  }
}

