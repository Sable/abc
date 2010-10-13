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

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.activation.DataHandler;
import javax.activation.DataSource;


public class ByteArrayDataSource implements DataSource {

  private final byte[] data; 
  private final String contentType;
  private final String name;

  public ByteArrayDataSource(byte[] data, String contentType) {
    this.data = data;
    this.contentType = contentType;
    this.name = "";
  }

  public ByteArrayDataSource(byte[] data, String contentType, String name) {

    this.data = data;
    this.contentType = contentType;
    this.name = name;

  }

  public String getContentType() {
    return contentType;
  }

  public InputStream getInputStream() {
    return new ByteArrayInputStream(data);
  }

  public OutputStream getOutputStream() throws IOException {
    throw new IOException("Method getOutputStream() not supported.");
  }

  public String getName() {
    return name;
  }
}


