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

package com.servingxml.components.saxfilter;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.XmlValidatorAppender;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.content.DefaultUriResolverFactory;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.DefaultTransformerErrorListener;
import com.servingxml.util.xml.XPathBooleanExpression;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;
import java.io.IOException;
import java.util.List;
import javax.xml.transform.ErrorListener;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import com.servingxml.util.PrefixMap;

class DomXmlFilterAppender extends AbstractXmlFilterAppender implements XmlValidatorAppender {
  private final XPathBooleanExpressionFactory testExprFactory;
  private final StringFactory messageFactory;
  private final PrefixMap prefixMap;
  private final String base;

  public DomXmlFilterAppender(PrefixMap prefixMap, String base, XPathBooleanExpressionFactory testExprFactory, 
                        StringFactory messageFactory) {
    this.prefixMap = prefixMap;
    this.base = base;
    this.testExprFactory = testExprFactory;
    this.messageFactory = messageFactory;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
                                  XmlFilterChain pipeline) {
    XMLFilter filter = createXmlFilter(context, flow);
    pipeline.addXmlFilter(filter);
  }

  public XMLFilter createXmlFilter(ServiceContext context, Flow flow) {
    XPathBooleanExpression testExpr = testExprFactory.createXPathBooleanExpression();
    testExpr.setUriResolverFactory(context.getUriResolverFactory());
    testExpr.setErrorListener(context.getTransformerErrorListener());
    XMLFilter filter = new DomXmlFilter(prefixMap, base, context, flow, testExpr, messageFactory);
    return filter;
  }

  public boolean validate(ServiceContext context, Flow flow, List<String> failures) {
    boolean valid = false;
    try {
      SaxSource saxSource = flow.getDefaultSaxSource();
      XMLReader xmlReader = saxSource.createXmlReader();
      XMLFilter schemaFilter = createXmlFilter(context,flow);
      schemaFilter.setParent(xmlReader);
      schemaFilter.parse("");
      valid = true;
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (Exception e) {
      failures.add(e.getMessage());
    }

    return valid;
  }
}

