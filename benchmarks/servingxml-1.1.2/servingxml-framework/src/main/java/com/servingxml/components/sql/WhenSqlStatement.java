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

package com.servingxml.components.sql;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;
import com.servingxml.util.xml.XPathBooleanExpression;
import com.servingxml.components.content.DefaultUriResolverFactory;
import javax.xml.transform.ErrorListener;
import com.servingxml.util.xml.DefaultTransformerErrorListener;
import com.servingxml.app.Environment;

public class WhenSqlStatement implements AlternativeSqlStatement {
  private final Environment env;
  private final XPathBooleanExpressionFactory factory;
  private final AbstractSqlStatement sqlStatement;
  
  public WhenSqlStatement(Environment env, XPathBooleanExpressionFactory factory,
  AbstractSqlStatement sqlStatement) {
    this.env = env;
    this.factory = factory;
    this.sqlStatement = sqlStatement;
  }
  
  public boolean testsTrue(ServiceContext context, Flow flow) {
    
    XPathBooleanExpression expr = factory.createXPathBooleanExpression();
    expr.setUriResolverFactory(context.getUriResolverFactory());
    expr.setErrorListener(context.getTransformerErrorListener());

    Source source = new SAXSource(flow.getRecord().createXmlReader(env.getQnameContext().getPrefixMap()),
                                  new InputSource());
    return expr.evaluate(source, flow.getParameters());
  }
  
  public void buildSql(ServiceContext context, Flow flow,
  StringBuilder buf) {
    
    sqlStatement.buildSql(context, flow, buf);
  }
}
