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

import java.util.ArrayList;

import org.w3c.dom.Element;

import com.servingxml.util.SystemConstants;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.DomIterator;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;
import com.servingxml.util.xml.XPathBooleanExpressionFactoryImpl;
import com.servingxml.expr.substitution.EscapeSubstitutionVariables;
import com.servingxml.app.Environment;

public class ChooseSqlStatementFactory {
  private final Environment env;
  private final String xsltVersion;
  private final EscapeSubstitutionVariables escapeVariables;

  public ChooseSqlStatementFactory(Environment env, 
                                   String xsltVersion, 
                                   EscapeSubstitutionVariables escapeVariables) {
    this.env = env;
    this.xsltVersion = xsltVersion;
    this.escapeVariables = escapeVariables;
  }

  public AbstractSqlStatement createSqlStatement(ConfigurationContext context,
                                                 AbstractSqlStatement tail) {

    Element element = context.getElement();

    ChooseElementCommand command = new ChooseElementCommand(context, escapeVariables, tail);
    DomIterator.toEveryChild(element,command);

    AbstractSqlStatement statement = command.getSqlStatement();
    if (statement == null) {
      throw new ServingXmlException("No when or otherwise statements.");
    }
    return statement;
  }

  class ChooseElementCommand extends DomIterator.ChildCommand {
    private static final String WHEN = "when";
    private static final String TEST = "test";
    private static final String OTHERWISE = "otherwise";

    private ArrayList<AlternativeSqlStatement> alternativeList = new ArrayList<AlternativeSqlStatement>();
    private final ConfigurationContext context;
    private final AbstractSqlStatement tail;
    private final EscapeSubstitutionVariables escapeVariables;

    ChooseElementCommand(ConfigurationContext context, EscapeSubstitutionVariables escapeVariables, AbstractSqlStatement tail) {
      this.context = context;
      this.escapeVariables = escapeVariables;
      this.tail = tail;
    }

    AbstractSqlStatement getSqlStatement() {
      AlternativeSqlStatement[] alternatives = new AlternativeSqlStatement[alternativeList.size()];
      alternatives = alternativeList.toArray(alternatives);
      return new ChooseSqlStatement(alternatives,tail);
    }

    public void doText(Element parent, String value) {
    }
    public void doElement(Element parent, Element element) {
      if (DomHelper.areEqual(element.getNamespaceURI(),element.getLocalName(),
                             SystemConstants.SERVINGXML_NS_URI,WHEN)) {

        String test = element.getAttribute(TEST);
        XPathBooleanExpressionFactory factory = new XPathBooleanExpressionFactoryImpl(
                                                                                     context.getQnameContext(),
                                                                                     test,
                                                                                     xsltVersion,
                                                                                     null,
                                                                                     context.getTransformerFactory());
        SqlStatementFactory statFactory = new SqlStatementFactory(env,xsltVersion, escapeVariables);
        ConfigurationContext childContext = context.createInstance(element);
        AbstractSqlStatement sqlStatement = statFactory.createSqlStatement(childContext,null);
        AlternativeSqlStatement alternative = new WhenSqlStatement(env,
                                                                   factory,sqlStatement);
        alternativeList.add(alternative);
      } else if (DomHelper.areEqual(element.getNamespaceURI(),element.getLocalName(),
                                    SystemConstants.SERVINGXML_NS_URI,OTHERWISE)) {
        SqlStatementFactory statFactory = new SqlStatementFactory(env,xsltVersion, escapeVariables);
        AbstractSqlStatement sqlStatement = statFactory.createSqlStatement(context,null);
        AlternativeSqlStatement alternative = new OtherwiseSqlStatement(sqlStatement);
        alternativeList.add(alternative);
      }
    }
  }
}


