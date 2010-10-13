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

package com.servingxml.util.record;

import java.io.Writer;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.servingxml.util.QnameContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.PrefixMap;

/**
 * A <code>RecordImpl</code> class represents a set of record.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                                          
public abstract class AbstractRecord implements Record {

  /**
  * Returns a string XML representation of the record, primarily for debugging purposes.
  *
  * @return A string representation of the record.
  */

  public String toString() {

    String s = "";
/*
    try {
      XMLReader compositeReader = createXmlReader();
      Source xmlSource = new SAXSource(compositeReader,new InputSource(""));

      Writer writer = new StringWriter();
      Result result = new StreamResult(writer);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.transform(xmlSource,result);

      s = writer.toString();
    } catch (Exception e) {
      //  Don't care
    }
*/    
    return s;
  }

  public String toXmlString(PrefixMap prefixMap) {
    //System.out.println(getClass().getName()+".toXmlString start");

    String s = "";
    try {
      XMLReader compositeReader = createXmlReader(prefixMap);
      Source xmlSource = new SAXSource(compositeReader,new InputSource(""));

      Writer writer = new StringWriter();
      Result result = new StreamResult(writer);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.transform(xmlSource,result);

      s = writer.toString();
    } catch (TransformerException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
    //System.out.println(getClass().getName()+".toXmlString end");
    return s;
  }
                 
  public XMLReader createXmlReader(PrefixMap prefixMap) {
    //System.out.println(getClass().getName()+".createXmlReader start");
    return new RecordXmlReader(this, prefixMap);
  }
}



