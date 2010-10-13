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

package com.servingxml.components.saxsource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;

import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsource.StreamSourceSaxSource;
import com.servingxml.io.streamsource.ClosingStreamSource;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.util.ServingXmlException;

/**
 * Factory for creating an XMLFilter. 
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SaxSourceXmlSourceFactory implements XmlSourceFactory {
  private final SaxSourceFactory saxSourceFactory;

  public SaxSourceXmlSourceFactory(SaxSourceFactory saxSourceFactory) {
    this.saxSourceFactory = saxSourceFactory;
  }

  public SaxSource createSaxSource(ServiceContext context, Flow flow) {

    SaxSource saxSource = saxSourceFactory.createSaxSource(context, flow);
    return saxSource;
  }

  public StreamSource createStreamSource(ServiceContext context, Flow flow) {
    InputStream is;
    try {
      TransformerFactory transformerFactory = context.getTransformerFactory();
      SaxSource saxSource = createSaxSource(context,flow);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      SAXSource source = new SAXSource(saxSource.createXmlReader(),new InputSource(""));
      Transformer transformer = transformerFactory.newTransformer();
      Result result = new StreamResult(os);
      transformer.transform(source,result);
      is = new ByteArrayInputStream(os.toByteArray());
      return new ClosingStreamSource(is);
    } catch (TransformerException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}
