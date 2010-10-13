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

import java.util.Properties;
import java.util.Date;

import javax.mail.internet.MimeMultipart;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.task.AbstractTask;
import com.servingxml.components.task.Task;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.app.Flow;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;

/**
 *
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public final class SendMail extends AbstractTask 
implements Task {

  //  Temp
  private final MailAccount mailAccount;
  private final SubstitutionExpr toResolver;
  private final SubstitutionExpr ccResolver;
  private final String subject;
  private final MailMessage mailMessage;

  public SendMail(MailAccount mailAccount, SubstitutionExpr toResolver,
    SubstitutionExpr ccResolver, String subject, MailMessage mailMessage) {

    this.mailAccount = mailAccount;
    this.toResolver = toResolver;
    this.ccResolver = ccResolver;
    this.subject = subject;
    this.mailMessage = mailMessage;
  }

  public void execute(ServiceContext context, Flow flow) {

    Record parameters = flow.getParameters();
    Record record = flow.getRecord();

    String to = toResolver.evaluateAsString(parameters,record);
    String cc = ccResolver.evaluateAsString(parameters,record);

    if (to.trim().length() == 0) {
      throw new ServingXmlException("Empty \"to\" in email");
    }

    InternetAddress[] toAddresses; 
    try {
      toAddresses = InternetAddress.parse(to);
    } catch (MessagingException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    InternetAddress[] ccAddresses = new InternetAddress[0]; 
    if (cc.length() > 0) {
        try {
          ccAddresses = InternetAddress.parse(cc);
        } catch (MessagingException e) {
          throw new ServingXmlException(e.getMessage(),e);
        }
    }                                         

    String smtpHost = mailAccount.getSMTPHost();
    Properties props = new Properties();
    props.setProperty("mail.smtp.host",smtpHost);
    Session session = Session.getDefaultInstance(props);
    MimeMessage message = new MimeMessage(session);

    InternetAddress fromAddress = mailAccount.getFromAddress();
    try {
      message.setFrom(fromAddress);
      message.setRecipients(Message.RecipientType.TO,toAddresses);
      message.setRecipients(Message.RecipientType.CC,ccAddresses);

      message.setSubject(subject);
      message.setSentDate(new Date());
    } catch (MessagingException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }

    mailMessage.buildMessage(context, flow,message);
    try {
      Transport.send(message);
    } catch (MessagingException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (Exception e) {
      String s = "Error sending message.  " + e.getMessage();
      throw new ServingXmlException(s,e);
    }
  }
}

