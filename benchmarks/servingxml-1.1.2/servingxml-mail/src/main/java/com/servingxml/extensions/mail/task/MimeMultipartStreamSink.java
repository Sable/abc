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

import java.nio.charset.Charset;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetHeaders;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.CharsetHelper;
import com.servingxml.io.streamsink.AbstractStreamSink;

public class MimeMultipartStreamSink extends AbstractStreamSink implements StreamSink {
  public static final String INDETERMINATE_TYPE = "application/octet-stream";

  public static final String INLINE = "inline";
  public static final String ATTACHMENT = "attachment";

  private final ByteArrayOutputStream outputStream;
  private final MimeBodyPart mbp;
  private final String filename;
  private Properties outputProperties = new Properties();
  private final Charset charset;

  public MimeMultipartStreamSink(MimeBodyPart mbp) {
    this.outputStream = new ByteArrayOutputStream();
    this.mbp = mbp;
    this.filename = "";
    this.charset = null;
  }

  public MimeMultipartStreamSink(MimeBodyPart mbp, String filename) {
    this.outputStream = new ByteArrayOutputStream();
    this.mbp = mbp;
    this.filename = filename;
    this.charset = null;
  }
  
  public OutputStream getOutputStream() {
    return outputStream;
  }

  public void close() {
    try {
      String mediaType = outputProperties.getProperty(OutputKeys.MEDIA_TYPE,INDETERMINATE_TYPE);
      DataSource ds = new ByteArrayDataSource(outputStream.toByteArray(),mediaType,filename);
      DataHandler dh = new DataHandler(ds);
      mbp.setDataHandler(dh);
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void setOutputProperties(Properties outputProperties) {
    this.outputProperties = outputProperties;
  }

  public void setOutputProperty(String key, String value) {
    this.outputProperties.setProperty(key,value);
  }

  public Properties getOutputProperties() {
    return outputProperties;
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }
}


