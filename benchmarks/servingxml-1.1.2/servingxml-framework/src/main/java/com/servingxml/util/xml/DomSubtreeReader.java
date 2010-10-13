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

package com.servingxml.util.xml;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.servingxml.util.record.Record;
import com.servingxml.util.system.RuntimeContext;
import com.servingxml.util.PrefixMap;

/**
 * A <code>DomSubtreeReader</code> implement a SAX 2 <code>XMLReader</code> interface for supplying 
 * DocumentFragment content as SAX events.
 *
 * 
 * @author  Daniel A. Parker
 */

public class DomSubtreeReader extends AbstractXmlReader 
implements XMLReader {

  private final Node document;
  private final Record parameters;
  private final TransformerFactory transformerFactory;
  private final UriResolverFactory uriResolverFactory;
  private final ErrorListener errorListener;
  private final PrefixMap prefixMap;
  private final String baseUri;

  public DomSubtreeReader(Node document, 
                          PrefixMap prefixMap,
                          String baseUri,
                          Record parameters,
                          TransformerFactory transformerFactory,
                          UriResolverFactory uriResolverFactory,
                          ErrorListener errorListener) {
    this.document = document;
    this.prefixMap = prefixMap;
    this.baseUri = baseUri;
    this.parameters = parameters;
    this.transformerFactory = transformerFactory;
    this.uriResolverFactory = uriResolverFactory;
    this.errorListener = errorListener; 
  }

  public void parse(String systemId)
  throws IOException, SAXException {

    //System.out.println ("DomSubtreeReader.parse enter");
    try {
      final ContentHandler handler = getContentHandler();
      DOMSource source = new DOMSource(document);
/*
      //System.out.println ("DomSubtreeReader.parse check 15");
        StringWriter writer = new StringWriter();
        Transformer transformer = transformerFactory.newTransformer();
        Result result = new StreamResult(writer);
        //System.out.println ("DomSubtreeReader.parse check 20");
        transformer.transform(source,result);
        //System.out.println ("DomSubtreeReader.parse check 25");
      //System.out.println ("DomSubtreeReader.parse " + writer.toString());
*/
      SAXResult result = new SAXResult(handler);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setErrorListener(errorListener);
      URIResolver uriResolver = uriResolverFactory.createUriResolver(prefixMap, baseUri, parameters, transformer.getURIResolver());
      transformer.setURIResolver(uriResolver);
      transformer.transform(source,result);
    } catch (TransformerException e) {
      throw new SAXException(e.getMessage(),e);
    }
  }
}
