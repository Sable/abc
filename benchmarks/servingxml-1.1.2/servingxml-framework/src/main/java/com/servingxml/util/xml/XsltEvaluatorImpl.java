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

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;

import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordContentHandler;
import com.servingxml.util.record.RecordReceiver;

/**
 * A <code>XsltEvaluator</code> implement an <code>XsltEvaluator</code> interface
 *
 * 
 * @author  Daniel A. Parker
 */

public class XsltEvaluatorImpl implements XsltEvaluator {
  private final PrefixMap prefixMap;
  private final Name recordTypeName;
  private final Templates templates;
  private final Name[] parameterNames;
  private final String baseUri;
  private UriResolverFactory uriResolverFactory;

  public XsltEvaluatorImpl(PrefixMap prefixMap, Name recordTypeName, Templates templates, Name[] parameterNames) {
    this.prefixMap = prefixMap;
    this.recordTypeName = recordTypeName;
    this.templates = templates;
    this.parameterNames = parameterNames;
    this.uriResolverFactory = UriResolverFactory.getDefault();
    this.baseUri = "";
  }

  public XsltEvaluatorImpl(PrefixMap prefixMap, Name recordTypeName, Templates templates, Name[] parameterNames, UriResolverFactory uriResolverFactory) {
    this.prefixMap = prefixMap;
    this.recordTypeName = recordTypeName;
    this.templates = templates;
    this.parameterNames = parameterNames;
    this.uriResolverFactory = uriResolverFactory;
    this.baseUri = "";
  }

  public void setUriResolverFactory(UriResolverFactory uriResolverFactory) {
    this.uriResolverFactory = uriResolverFactory;
  }

  public Record evaluate(Source source, Record parameters) {
    //System.out.println(getClass().getName()+".evaluate");
    try {
      Transformer transformer = null;

      if (templates != null) {
        transformer = templates.newTransformer();
        URIResolver resolver = uriResolverFactory.createUriResolver(prefixMap,baseUri,parameters,transformer.getURIResolver());
        transformer.setURIResolver(resolver);
        for (int i = 0; i < parameterNames.length; ++i) {
          Name parameterName = parameterNames[i];
          String value = parameters.getString(parameterName);
          if (value != null) {
            transformer.setParameter(parameterName.toString(),value);
          }
        }
      }
      Record variables = Record.EMPTY;
      if (transformer != null) {
        try {
          final Record[] records = new Record[]{null};
          RecordReceiver recordReceiver = new RecordReceiver() {
            public void receiveRecord(Record record) {
              if (records[0] == null) {
                records[0] = record;
              }
            }
          };

          RecordContentHandler receiver = new RecordContentHandler(recordTypeName,recordReceiver);
          SAXResult saxResult = new SAXResult(receiver);
          transformer.transform(source,saxResult);

          if (records[0] != null) {
            variables = records[0];
          }
        } catch (TransformerException te) {
          throw new ServingXmlException(te.getMessage(),te);
        }
      }
      return variables;
    } catch (TransformerException te) {
      throw new ServingXmlException(te.getMessage(),te);
    }
  }

  public boolean isEmpty() {
    return false;
  }
}

