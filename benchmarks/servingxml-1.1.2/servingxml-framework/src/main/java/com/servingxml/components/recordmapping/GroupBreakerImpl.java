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

package com.servingxml.components.recordmapping;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordGroupingXmlReader;
import com.servingxml.util.xml.XPathBooleanExpression;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;
import com.servingxml.components.content.DefaultUriResolverFactory;
import javax.xml.transform.ErrorListener;
import com.servingxml.util.xml.DefaultTransformerErrorListener;
import com.servingxml.util.PrefixMap;

/**
 * The <code>GroupBreaker</code> is a class for breaking groups.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class GroupBreakerImpl implements GroupBreaker {
  private final PrefixMap prefixMap;
  private final XPathBooleanExpressionFactory exprFactory;

  public GroupBreakerImpl(PrefixMap prefixMap, XPathBooleanExpressionFactory exprFactory) {
    this.prefixMap = prefixMap;
    this.exprFactory = exprFactory;
  }

  public boolean breakOn(ServiceContext context, Flow flow, Record previousRecord, Record currentRecord) {

    //String value = currentRecord.getString(new QualifiedName("DataType"));
    XPathBooleanExpression expr = exprFactory.createXPathBooleanExpression();
    expr.setUriResolverFactory(context.getUriResolverFactory());
    expr.setErrorListener(context.getTransformerErrorListener());

    XMLReader reader = new RecordGroupingXmlReader(prefixMap, previousRecord, currentRecord);
    Source source = new SAXSource(reader,new InputSource(""));
    boolean breakGroup = expr.evaluate(source, flow.getParameters());
    //if (value != null) {
    //System.out.println("GroupBreakerImpl.breakOn DataType = " + value + ", break = " + breakGroup);
    //}
    
    return breakGroup;
  }
}

