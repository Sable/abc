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

package com.servingxml.components.content;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.servingxml.app.Flow;
import com.servingxml.app.FlowImpl;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.Name; 
import com.servingxml.util.PrefixMap;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.UrlHelper;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ServingXmlUriResolver implements URIResolver {

  private final PrefixMap prefixMap;
  private final ServiceContext context;
  private final Record parameters;
  private final String documentBase;
  private final URIResolver parent;

  public ServingXmlUriResolver(PrefixMap prefixMap, ServiceContext context, Record parameters, String documentBase,
                               URIResolver parent) {

    this.prefixMap = prefixMap;
    this.context = context;
    this.parameters = parameters;
    this.documentBase = documentBase;
    this.parent = parent;
  }

  public Source resolve(String href, String base)
  throws TransformerException {

    //System.out.println(getClass().getName()+".resolve href=" + href + ", base="+base);
    try {
      Record newParameters = parameters;
      URI relUri = new URI(href);
      String relativeUri;
      String query = relUri.getQuery();
      if (query != null && query.length() > 0) {
        newParameters = augmentParameters(parameters,query);
        String s = relUri.toString();
        int queryPos = s.indexOf('?');
        relativeUri = s.substring(0,queryPos);
      } else {
        relativeUri = relUri.toString();
      }
      Flow flow = new FlowImpl(newParameters);
      Content contentFactory = (Content)context.getAppContext().getResources().lookupServiceComponent(Content.class, relativeUri);

      Source source = null;
      if (contentFactory != null) {
        SaxSource saxSource = contentFactory.createSaxSource(context,flow);
        XMLReader xmlReader = saxSource.createXmlReader();
        source = new SAXSource(xmlReader,new InputSource(saxSource.getSystemId()));
      }

      if (source == null) {
        URI relative = new URI(relativeUri);
        if (!relative.isAbsolute()) {
          if (documentBase != null && documentBase.length() > 0) {
            URI baseUri = new URI(documentBase);
            relative = baseUri.resolve(relative);
          } else if (base != null && base.length() > 0) {
            URI baseUri = new URI(base);
            relative = baseUri.resolve(relative);
          }
        }
        String systemId = relative.toString();
        if (parent != null) {
          source = parent.resolve(href,documentBase);
        } else {
          URL url;        
          try {
            url = UrlHelper.createUrl(href,documentBase);
            source = new StreamSource(url.toString());
          } catch (ServingXmlException e) {
            source = null;
          }
        }
      }

      return source;
    } catch (ServingXmlException e) {
      Throwable cause = e.getCause();
      if (cause != null && cause instanceof TransformerException) {
        throw (TransformerException)cause;
      } else {
        throw new TransformerException(e.getMessage(),e);
      }
    } catch (URISyntaxException e) {
      throw new TransformerException(e.getMessage(),e);
    }
  }

  private Record augmentParameters(Record parameters, String query) {
    RecordBuilder builder = new RecordBuilder(parameters);

    StringTokenizer tokenizer = new StringTokenizer(query, ";&");
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      int equalsPos = token.indexOf('=');
      if (equalsPos > 0 && equalsPos < (token.length()-1)) {
        String rawName = token.substring(0, equalsPos);
        String value = token.substring(equalsPos+1);
        Name name = Name.createName(rawName,prefixMap);
        builder.setString(name, value);
      }
    }
    return builder.toRecord();
  }
}



