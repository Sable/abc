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
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.task.Task;
import com.servingxml.app.Flow;

/**
 *
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public final class MessagePartAppender
implements PartAppender {

  private final Task[] tasks;
  private final String type;

  public MessagePartAppender(Task[] tasks, String type) {
    this.tasks = tasks;
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void appendPart(ServiceContext context, Flow flow, MimeMultipart mmp) {

    MimeBodyPart mbp = new MimeBodyPart();
    StreamSink streamSink = new MimeMultipartStreamSink(mbp);

    Flow newFlow = flow.replaceDefaultStreamSink(context, streamSink);
    for (int i = 0; i < tasks.length; ++i) {
      Task action = tasks[i];                                 
      action.execute(context, newFlow);
    }
    try {
      mmp.addBodyPart(mbp);
    } catch (MessagingException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

}

