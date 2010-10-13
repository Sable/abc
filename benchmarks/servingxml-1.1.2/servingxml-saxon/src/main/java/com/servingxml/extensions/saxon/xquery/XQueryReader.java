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

package com.servingxml.extensions.saxon.xquery;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.ServingXmlUriResolver;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.xml.AbstractXmlReader;
import com.servingxml.util.xml.UriResolverFactory;
import com.servingxml.util.PrefixMap;

import net.sf.saxon.s9api.SAXDestination;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;

public class XQueryReader extends AbstractXmlReader {
  private final PrefixMap prefixMap;
  private static ServiceContext context;
  private static Flow flow;
  private final String baseUri;
  private final Content contextDocument;
  private final XQueryExecutable exp;

  public XQueryReader(PrefixMap prefixMap, ServiceContext context, Flow flow, String baseUri, 
                      Content contextDocument, XQueryExecutable exp) {
    this.prefixMap = prefixMap;
    this.context = context;
    this.flow = flow;
    this.baseUri = baseUri;
    this.contextDocument = contextDocument;
    this.exp = exp;
  }

  public void parse(String systemId) throws IOException,SAXException {
    try {
      //System.out.println(getClass().getName()+".parse enter");

      SaxSource saxSource = contextDocument.createSaxSource(context,flow);
      XMLReader reader = saxSource.createXmlReader();
      Source source = new SAXSource(reader, new InputSource());

      XQueryEvaluator qe = exp.load();
      qe.setSource(source);

      UriResolverFactory resolverFactory = context.getUriResolverFactory();
      URIResolver uriResolver = resolverFactory.createUriResolver(prefixMap,
                                                                  baseUri,flow.getParameters(),
                                                        qe.getURIResolver());
      //qe.setSource(source);
      qe.setURIResolver(uriResolver);
      qe.run(new SAXDestination(getContentHandler()));
      //System.out.println(getClass().getName()+".parse leave");
    } catch (Exception e) {
      throw new SAXException(e.getMessage(),e);
    }
  }
}
