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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;

import org.xml.sax.InputSource;

import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.util.xml.XsltChooseReceiver;

public class XsltChooser {
  private final PrefixMap prefixMap;
  private final InputSource inputSource = new InputSource();
  private final Templates templates;
  private final Name[] parameterNames;
  private final String baseUri;
  private UriResolverFactory uriResolverFactory;
  private ErrorListener errorListener; 

  public XsltChooser(Templates templates, Name[] parameterNames, PrefixMap prefixMap, String baseUri) {
    this.prefixMap = prefixMap;
    this.templates = templates;
    this.parameterNames = parameterNames;
    this.baseUri = baseUri;
    this.uriResolverFactory = UriResolverFactory.getDefault();
    this.errorListener = null; 
  }

  public void setUriResolverFactory(UriResolverFactory uriResolverFactory) {
    this.uriResolverFactory = uriResolverFactory;
  }
  public void setErrorListener(ErrorListener errorListener) {
    this.errorListener = errorListener;
  }

  public int choose(Source source, Record parameters) {
    try {
      Transformer transformer = templates.newTransformer();
      for (int i = 0; i < parameterNames.length; ++i) {
        Name parameterName = parameterNames[i];
        String value = parameters.getString(parameterName);
        if (value != null) {
          transformer.setParameter(parameterName.toString(),value);
        }
      }
      URIResolver uriResolver = uriResolverFactory.createUriResolver(prefixMap, baseUri, parameters, 
                                                                     transformer.getURIResolver());
      transformer.setURIResolver(uriResolver);
      if (errorListener != null) {
        transformer.setErrorListener(errorListener);
      }

      XsltChooseReceiver receiver = new XsltChooseReceiver();
      Result result = new SAXResult(receiver);
      transformer.transform(source,result);

      int index = receiver.getSelection();
      return index;
    } catch (TransformerException te) {
      throw new ServingXmlException(te.getMessage(),te);
    }
  }
}
