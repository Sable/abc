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

import java.io.StringWriter;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;

import com.servingxml.util.PrefixMap;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XPathBooleanExpressionImpl implements XPathBooleanExpression {

  private final PrefixMap prefixMap;
  private final String baseUri;
  private final BooleanExpressionTransformerFactory transformerFactory;
  private UriResolverFactory uriResolverFactory;
  private ErrorListener errorListener; 

  public XPathBooleanExpressionImpl(PrefixMap prefixMap,
                                    String baseUri, 
                                    BooleanExpressionTransformerFactory transformerFactory) {

    this.prefixMap = prefixMap;
    this.baseUri = baseUri;
    this.transformerFactory = transformerFactory;
    this.uriResolverFactory = UriResolverFactory.getDefault();
    this.errorListener = null; 
  }

  public void setUriResolverFactory(UriResolverFactory uriResolverFactory) {
    this.uriResolverFactory = uriResolverFactory;
  }
  public void setErrorListener(ErrorListener errorListener) {
    this.errorListener = errorListener;
  }

  public boolean evaluate(Source source, Record parameters) {

    //System.out.println(getClass().getName()+".evaluate start");
    Transformer testStyleTransformer = transformerFactory.newTransformer(parameters);
    boolean success = false;
    try {
      StringWriter writer = new StringWriter();
      Result testStyleResult = new StreamResult(writer);
      String baseUri = "";
      URIResolver uriResolver = uriResolverFactory.createUriResolver(prefixMap, baseUri, parameters,
                                                                     testStyleTransformer.getURIResolver());
      testStyleTransformer.setURIResolver(uriResolver);
      if (errorListener != null) {
        testStyleTransformer.setErrorListener(errorListener);
      }

      testStyleTransformer.transform(source,testStyleResult);
      String s = writer.toString();
      success = s.equals("true");
    } catch (TransformerException te) {
      //System.out.println(getClass().getName()+".evaluate fail " + te.getMessage());
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
    //System.out.println(getClass().getName()+".evaluate end");
    return success;
  }

  public String toString() {
    return transformerFactory.getExpression();
  }
}



