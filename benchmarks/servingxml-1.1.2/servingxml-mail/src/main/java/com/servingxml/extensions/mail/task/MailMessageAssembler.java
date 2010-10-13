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

import java.util.List;
import java.util.ArrayList;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

import org.w3c.dom.Element;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.DomIterator;

/**
 * Factory for creating a <tt>MailMessageAssembler</tt> instance.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MailMessageAssembler {

  public MailMessage assemble(final ConfigurationContext context) {
    
    Element responseElement = context.getElement();
    
    final List<PartAppender> partAppenderList = new ArrayList<PartAppender>();
    final StringBuilder textBuf = new StringBuilder();

    DomIterator.ChildCommand command = new DomIterator.ChildCommand() {
      public void doText(Element parent, String value) {
        textBuf.append(value);
      }
      public void doElement(Element parent, Element element) {
  
        Object ref = context.getServiceComponent(element);
        if (ref != null) {
          if (ref instanceof PartAppender) {
             partAppenderList.add((PartAppender)ref);
          }
        }
      }
    };
    DomIterator.toEveryChild(responseElement,command);

    try {
        if (textBuf.length() > 0) {
            String s = textBuf.toString();
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setText(s);
            PartAppender partAppender = new TextAppender(mbp);
            partAppenderList.add(0,partAppender);
        }
    } catch (MessagingException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }

    PartAppender[]partAppenders = new PartAppender[partAppenderList.size()];
    partAppenders = partAppenderList.toArray(partAppenders);
    
    MailMessage mailMessage = new MailMessage(partAppenders);

    return mailMessage;
  }
}

