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

import java.io.UnsupportedEncodingException;

import javax.mail.internet.InternetAddress;
import javax.mail.MessagingException;

import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;

/**
 * Factory for creating a <tt>MailAccount</tt> instances.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SenderAssembler {
  
  private String displayName= "";
  private String emailAddress = null;

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }
  
  public InternetAddress assemble(ConfigurationContext context) {

    if (emailAddress == null) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),"emailAddress");
      throw new ServingXmlException(msg);
    }

    InternetAddress fromAddress;

    try {
      if (displayName.length() == 0) {
        fromAddress = new InternetAddress(emailAddress);
      } else {
        fromAddress = new InternetAddress(emailAddress,displayName);
      }
    } catch (MessagingException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (UnsupportedEncodingException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }

    return fromAddress;
  }
}

