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

package com.servingxml.app.xmlpipeline;

import java.util.Properties;
import java.util.Enumeration;

import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;
import org.xml.sax.SAXException;         
import org.xml.sax.ContentHandler;

import com.servingxml.io.cache.Expirable;
import com.servingxml.io.cache.ExpirableFamily;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.xml.NullXmlReader;
import com.servingxml.util.xml.ChainedContentHandler;
import org.xml.sax.Attributes;

public class XmlPipeline implements XmlFilterChain {
  private final ExpirableFamily expirableFamily = new ExpirableFamily();
  private String systemId;
  private XMLReader xmlReader;
  private Properties outputProperties;

  public XmlPipeline(XMLReader xmlReader) {
   //System.out.println(getClass().getName()+".cons");

    this.xmlReader = xmlReader;
    this.systemId = "";
    this.outputProperties = new Properties();
  }

  public XmlPipeline() {
   //System.out.println(getClass().getName()+".cons");

    this.xmlReader = new NullXmlReader();
    this.systemId = "";
    this.outputProperties = new Properties();
  }

  public XmlPipeline(Properties defaultOutputProperties) {
   //System.out.println(getClass().getName()+".cons");

    this.xmlReader = new NullXmlReader();
    this.systemId = "";
    this.outputProperties = new Properties();
    addOutputProperties(defaultOutputProperties);
  }

  public XmlPipeline(XMLReader reader, String systemId, Expirable expirable, Properties defaultOutputProperties) {
    //System.out.println(getClass().getName()+".cons2");

    this.xmlReader = reader;
    this.systemId = systemId;

    this.expirableFamily.addExpirable(expirable);
    this.outputProperties = new Properties(); 
    addOutputProperties(defaultOutputProperties);
  }

  public void setSaxSource(SaxSource saxSource) {
    //System.out.println("XmlPipeline.setSaxSource filter = " + saxSource.getClass().getName());
    this.xmlReader = saxSource.createXmlReader();
    this.systemId = saxSource.getSystemId();
    Expirable expirable = saxSource.getExpirable();
    this.expirableFamily.addExpirable(expirable);
    addOutputProperties(saxSource.getDefaultOutputProperties());
  }

  public XMLReader getXmlReader() {
    return xmlReader;
  }

  public void addXmlFilter(XMLFilter filter) {
    //System.out.println("XmlPipeline.addXmlFilter filter = " + filter.getClass().getName());
    filter.setParent(xmlReader);
    xmlReader = filter;
  }

  public Properties getOutputProperties() {
    return outputProperties;
  }

  public void addOutputProperties(Properties properties) {
   //System.out.println(getClass().getName()+".addOutputProperties");
    Enumeration enumer = properties.propertyNames();
    while (enumer.hasMoreElements()) {
      String name = (String)enumer.nextElement();
      this.outputProperties.setProperty(name,properties.getProperty(name));
    }
  }

  public Expirable getExpirable() {
    return expirableFamily;
  }

  public void addExpirable(Expirable expirable) {
    expirableFamily.addExpirable(expirable);
  }

  public void execute(ContentHandler contentHandler) {
    //System.out.println(getClass().getName()+".execute contentHandler " + contentHandler.getClass().getName());
    if (xmlReader != null) {
      try {
        //System.out.println(getClass().getName()+".execute xmlReader " + xmlReader.getClass().getName());
        xmlReader.setContentHandler(contentHandler);
        xmlReader.parse(systemId);
      } catch (SAXException se) {
        //System.out.println("SAXException " + se.getClass().getName());
        Throwable cause = se;
        if (se.getException() != null && se.getException().getMessage() != null) {
          cause = se.getException();
        }
        if (cause instanceof ServingXmlException) {
          throw (ServingXmlException)cause;
        } else {
          throw new ServingXmlException(cause.getMessage(),cause);
        }
      } catch (Exception e) {
        //System.out.println("Exception " + e.getClass().getName());
        throw new ServingXmlException(e.getMessage(),e);
      }
    }
  }
}

