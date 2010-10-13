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

import org.w3c.dom.Element;

import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.expr.substitution.EscapeSubstitutionVariables;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.xml.DomIterator;
import com.servingxml.app.Environment;

/**
 * The <code>SqlStatementFactory</code> implements a factory for
 * creating system <code>SqlStatement</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlStatementFactory {

  private final Environment env;
  private final String xsltVersion;
  private final EscapeSubstitutionVariables escapeVariables;

  public SqlStatementFactory(Environment env, 
                             String xsltVersion, EscapeSubstitutionVariables escapeVariables) {
    this.env = env;
    this.xsltVersion = xsltVersion;
    this.escapeVariables = escapeVariables;
  }

  public AbstractSqlStatement createSqlStatement(final ConfigurationContext context) {
    return createSqlStatement(context,null);
  }

  public AbstractSqlStatement createSqlStatement(final ConfigurationContext context,
                                                 AbstractSqlStatement tail) {

    Element sqlQueryElement = context.getElement();

    SqlElementCommand command = new SqlElementCommand(context,tail);
    DomIterator.toEveryChild(sqlQueryElement,command,false);

    AbstractSqlStatement statement = command.getSqlStatement();
    if (statement == null) {
      throw new ServingXmlException("No SQL statement.");
    }
    return statement;
  }

  class SqlElementCommand extends DomIterator.ChildCommand {

    private static final String CHOOSE = "choose";

    private final ConfigurationContext context;
    private AbstractSqlStatement tail;

    SqlElementCommand(ConfigurationContext context, AbstractSqlStatement tail) {
      this.context = context;
      this.tail = tail;
    }

    AbstractSqlStatement getSqlStatement() {
      return tail;
    }

    public void doText(Element parent, String value) {
      SubstitutionExpr subExpr = SubstitutionExpr.parseString(context.getQnameContext(),value,escapeVariables);
      tail = new TextSqlStatement(subExpr,tail);
    }

    public void doElement(Element parent, Element element) {
      if (DomHelper.areEqual(element.getNamespaceURI(),element.getLocalName(),
                             SystemConstants.SERVINGXML_NS_URI,CHOOSE)) {
        ChooseSqlStatementFactory factory = new ChooseSqlStatementFactory(env,xsltVersion,escapeVariables);
        ConfigurationContext childContext = context.createInstance(element);
        tail = factory.createSqlStatement(childContext,tail);
      }
    }
  }
}

