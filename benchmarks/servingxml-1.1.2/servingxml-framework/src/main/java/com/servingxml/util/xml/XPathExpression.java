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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;

import org.xml.sax.SAXException;

import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordContentHandler;
import com.servingxml.util.record.RecordReceiver;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XPathExpression {
  private static Name FIELD_NAME = new QualifiedName("F");

  private final PrefixMap prefixMap;
  private final Templates templates;
  private final Name recordTypeName;
  private final Name[] parameterNames;
  private final String matchExpr;
  private final String selectExpr;
  private UriResolverFactory uriResolverFactory;
  private ErrorListener errorListener; 

  XPathExpression(PrefixMap prefixMap,
                  Templates templates, 
                  Name[] parameterNames, 
                  Name recordTypeName, 
                  String matchExpr, 
                  String selectExpr) {
    this.prefixMap = prefixMap;
    this.templates = templates;
    this.recordTypeName = recordTypeName;
    this.parameterNames = parameterNames;
    this.matchExpr = matchExpr;
    this.selectExpr = selectExpr;
    this.uriResolverFactory = UriResolverFactory.getDefault();
    this.errorListener = null; 
  }

  public String getMatchExpression() {
    return matchExpr;
  }

  public String getSelectExpression() {
    return selectExpr;
  }

  public void setUriResolverFactory(UriResolverFactory uriResolverFactory) {
    this.uriResolverFactory = uriResolverFactory;
  }

  public void setErrorListener(ErrorListener errorListener) {
    this.errorListener = errorListener;
  }

  public String[] evaluate(Source source, Record parameters) {
    try {

      final Record[] records = new Record[]{null};
      RecordReceiver recordReceiver = new RecordReceiver() {
        public void receiveRecord(Record record) {
          if (records[0] == null) {
            records[0] = record;
          }
        }
      };

      RecordContentHandler receiver = new RecordContentHandler(recordTypeName, recordReceiver);
      Transformer transformer = templates.newTransformer();
      String baseUri = "";
      URIResolver uriResolver = uriResolverFactory.createUriResolver(prefixMap, baseUri, parameters, transformer.getURIResolver());
      transformer.setURIResolver(uriResolver);
      if (errorListener != null) {
        transformer.setErrorListener(errorListener);
      }

      SAXResult saxResult = new SAXResult(receiver);
      for (int i = 0; i < parameterNames.length; ++i) {
        Name parameterName = parameterNames[i];
        String value = parameters.getString(parameterName);
        if (value != null) {
          transformer.setParameter(parameterName.toString(),value);
        }
      }
      transformer.transform(source,saxResult);

      String[] value = SystemConstants.EMPTY_STRING_ARRAY;
      if (records[0] != null) {
        value = records[0].getStringArray(FIELD_NAME);
      }
      return value;
    } catch (TransformerException te) {
      Throwable cause = te;
      if (te.getCause() != null) {
        cause = te.getCause();
        if (cause instanceof SAXException) {
          SAXException se = (SAXException)cause;
          if (se != null && se.getException() != null && se.getException().getMessage() != null) {
            cause = se.getException();
          }
        }
      }
      if (cause instanceof ServingXmlException) {
        throw (ServingXmlException)cause;
      } else {
        throw new ServingXmlException(cause.getMessage(),cause);
      }
    }
  }
}



