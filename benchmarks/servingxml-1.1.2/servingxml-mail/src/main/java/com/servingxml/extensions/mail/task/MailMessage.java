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

import javax.mail.internet.MimeMultipart;
import javax.mail.Transport;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.Flow;

/**
 *
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public final class MailMessage {

  private final PartAppender[] partAppenders;

  public MailMessage(PartAppender[] partAppenders) {

    this.partAppenders = partAppenders;
  }

  public void buildMessage(ServiceContext context, Flow flow,
  MimeMessage message) {

    MimeMultipart mmp = null;

    int attachmentCount = 0;
    int alternativeCount = 0;
    int defaultCount = 0;
    for (int i = 0; i < partAppenders.length; ++i) {
      PartAppender appender = partAppenders[i];
      if (appender == null) {
        throw new ServingXmlException("Cannot find part appender.");
      }
      if (appender.getType().equals(PartAppender.ALTERNATIVE_TYPE)) {
        ++alternativeCount;
      } else if (appender.getType().equals(PartAppender.ALTERNATIVE_TYPE)) {
        ++attachmentCount;
      } else {
        ++defaultCount;
      } 
    }

    try {

      if (alternativeCount > 0) {
        mmp = new MimeMultipart("mixed");
        MimeMultipart mmp2 = new MimeMultipart("alternative");
        for (int i = 0; i < partAppenders.length; ++i) {
          PartAppender appender = partAppenders[i];
          if (appender.getType().equals(PartAppender.ALTERNATIVE_TYPE)) {
            appender.appendPart(context, flow,mmp2);
          }
        }
        MimeBodyPart wrap = new MimeBodyPart();
        wrap.setContent(mmp2);
        mmp.addBodyPart(wrap);
      }

      if (mmp == null) {
        mmp = new MimeMultipart();
      }
  
      for (int i = 0; i < partAppenders.length; ++i) {
        PartAppender appender = partAppenders[i];
        if (!appender.getType().equals(PartAppender.ALTERNATIVE_TYPE)) {
          appender.appendPart(context, flow,mmp);
        }
      }

      message.setContent(mmp);
    } catch (MessagingException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }

  }
}

