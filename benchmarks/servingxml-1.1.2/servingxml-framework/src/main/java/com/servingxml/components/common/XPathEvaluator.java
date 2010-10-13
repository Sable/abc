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

package com.servingxml.components.common;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.DefaultDocument;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;    
import com.servingxml.util.record.Record;    
import com.servingxml.util.record.Value;    
import com.servingxml.util.record.ValueFactory;
import com.servingxml.util.xml.XPathExpression;
import com.servingxml.util.xml.XPathExpressionFactory;

/**
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XPathEvaluator implements ValueEvaluator {
  private final Content content;
  private final XPathExpressionFactory exprFactory;

  public XPathEvaluator(XPathExpressionFactory exprFactory) {
    this.exprFactory = exprFactory;
    this.content = new DefaultDocument();
  }

  public XPathEvaluator(XPathExpressionFactory exprFactory, Content content) {
    this.exprFactory = exprFactory;
    this.content = content;
  }

  public Value bindValue(ServiceContext context, Flow flow) {
    return Value.EMPTY;
  }

  public String evaluateString(ServiceContext context, Flow flow) {
    XPathExpression expr = exprFactory.createXPathExpression();
    expr.setUriResolverFactory(context.getUriResolverFactory());
    SaxSource saxSource = content.createSaxSource(context,flow);
    Source source = new SAXSource(saxSource.createXmlReader(),new InputSource());
    String[] a = expr.evaluate(source, flow.getParameters());

    return a.length == 0 ? "" : a[0];
  }

  public String[] evaluateStringArray(ServiceContext context, Flow flow) {
    XPathExpression expr = exprFactory.createXPathExpression();
    expr.setUriResolverFactory(context.getUriResolverFactory());
    SaxSource saxSource = content.createSaxSource(context,flow);
    Source source = new SAXSource(saxSource.createXmlReader(),new InputSource());
    String[] a = expr.evaluate(source, flow.getParameters());
    return a;
  }

  public Value evaluateValue(ServiceContext context, Flow flow) {
    String[] sa = evaluateStringArray(context,flow);
    Value value = ValueFactory.createStringArrayValue(sa);
    return value;
  }
}

