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

package com.servingxml.components.xslt;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.io.StringWriter;

import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver; 
import javax.xml.transform.Source; 
import javax.xml.transform.sax.SAXSource; 
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import com.servingxml.app.ServiceContext;
import com.servingxml.io.cache.Expirable;
import com.servingxml.io.streamsource.StreamExpirable;
import com.servingxml.io.streamsource.url.UrlSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.DefaultTransformerErrorListener;
import com.servingxml.io.saxsource.SaxSource;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                           
public class XsltExpirable implements Expirable {

  private final SaxSource saxSource;
  private final Expirable[] expirableList;
  private final ServiceContext context;
                                          
  public XsltExpirable(ServiceContext context, SaxSource saxSource) {

    this.saxSource = saxSource;
    ArrayList<String> dependsList = new ArrayList<String>();
    try {
      this.context = context;
      Source source = new SAXSource(saxSource.createXmlReader(),new InputSource(saxSource.getSystemId()));

/*
{
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      //System.out.println ("XsltExpirable.cons check 15");
        StringWriter writer = new StringWriter();
        Transformer transformer = transformerFactory.newTransformer();
        Result result = new StreamResult(writer);
        //System.out.println ("XsltExpirable.cons check 20");
        transformer.transform(source,result);
        //System.out.println ("XsltExpirable.cons check 25");
      //System.out.println ("XsltExpirable.cons " + writer.toString());
}
*/

      DependsUriResolver dependsResolver = new DependsUriResolver(new SimpleUriResolver(),
        dependsList);
      
      TransformerFactory transformerFactory = context.getTransformerFactory();
      transformerFactory.setURIResolver(dependsResolver);
      Templates templates = transformerFactory.newTemplates(source);

      Expirable[] expirableList = new Expirable[dependsList.size()+1];
      expirableList[0] = saxSource.getExpirable();
      for (int i = 1; i < expirableList.length; ++i) {
        String dependsId = dependsList.get(i-1);
        URL url = new URL(dependsId);
        StreamExpirable expirable = UrlSource.createStreamExpirable(url);
        expirableList[i] = expirable;
      }
      this.expirableList = expirableList;
    } catch (javax.xml.transform.TransformerException e) {
      String message = "Failed to compile stylesheet " + saxSource.getSystemId() + ". " + e.getMessage()
      + "  " + e.getLocationAsString();
      if (e.getCause() != null) {
        message = message + e.getCause().getMessage();
      }
      throw new ServingXmlException(message,e);
    } catch (java.net.MalformedURLException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }                                                                       

  public long getLastModified(long timestamp) {
    long lastModified = 0;
    for (int i = 0; i < expirableList.length; ++i) {
      Expirable expirable = expirableList[i];
      long componentLastModified = expirable.getLastModified(timestamp);
      if (componentLastModified == -1) {
        lastModified = -1;
        break;
      }
      if (componentLastModified > lastModified) {
        lastModified = componentLastModified;
      }
    }
    return lastModified;
  }

  public boolean hasExpired(long timestamp) {
    boolean hasExpired = false;
    for (int i = 0; i < expirableList.length; ++i) {
      Expirable expirable = expirableList[i];
      if (expirable.hasExpired(timestamp)) {
        hasExpired = true;
        break;
      }
    }                         
    return hasExpired;
  }

  public boolean immediateExpiry() {
    boolean immediate = false;
    for (int i = 0; i < expirableList.length; ++i) {
      Expirable expirable = expirableList[i];
      if (expirable.immediateExpiry()) {
        immediate = true;
        break;
      }
    }
    return immediate;
  }

  static class DependsUriResolver implements URIResolver {
    private final URIResolver resolver;
    private final List<String> dependsList;

    public DependsUriResolver(URIResolver resolver, List<String> dependsList) {
      this.resolver = resolver;
      this.dependsList = dependsList;
    }

    public Source resolve(String href, String base) throws TransformerException {
      Source source = resolver.resolve(href,base);
      dependsList.add(source.getSystemId());
      return source;
    }
  }
}
