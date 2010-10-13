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

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import com.servingxml.components.error.CatchError;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.task.Task;
import com.servingxml.components.task.TaskCatchError;
import com.servingxml.components.task.TaskPrefilter;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;

/**
 * Factory for creating a <tt>SendMailAssembler</tt> instance.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SendMailAssembler {

  private String to = "";
  private String cc = "";
  private String subject = "";
  private MailAccount mailAccount = null;
  private MailMessage mailMessage = null;
  private CatchError catchError = null;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }
  
  public void injectComponent(MailAccount mailAccount) {
    this.mailAccount = mailAccount;
  }
  
  public void injectComponent(MailMessage mailMessage) {
    this.mailMessage = mailMessage;
  }
  
  public void injectComponent(CatchError catchError) {
    this.catchError = catchError;
  }
  
  public void setTo(String to) {
    this.to = to;
  }
  
  public void setCc(String cc) {
    this.cc = cc;
  }
  
  public void setSubject(String subject) {
    this.subject = subject;
  }

  public Task assemble(ConfigurationContext context) {
    
    if (mailAccount == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"jm:mailAccount");
      throw new ServingXmlException(message);
    }
    if (mailMessage == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"jm:message");
      throw new ServingXmlException(message);
    }

    SubstitutionExpr toResolver = SubstitutionExpr.parseString(context.getQnameContext(),to);
    SubstitutionExpr ccResolver = SubstitutionExpr.parseString(context.getQnameContext(),cc);
    Task task = new SendMail(mailAccount, toResolver, ccResolver, subject, 
      mailMessage);
    if (parameterDescriptors.length != 0) {
      task = new TaskPrefilter(task,parameterDescriptors);
    }
    if (catchError != null) {
      task = new TaskCatchError(task,catchError);
    }
    
    return task;
  }
}

