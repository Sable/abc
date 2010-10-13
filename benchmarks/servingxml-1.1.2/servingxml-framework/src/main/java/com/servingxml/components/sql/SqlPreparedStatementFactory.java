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

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.components.quotesymbol.QuoteSymbol;

/**
 * The <code>SqlPreparedStatementFactory</code> implements a factory for
 * creating system <code>SqlStatement</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlPreparedStatementFactory {

  private final QuoteSymbol quoteSymbol;

  public SqlPreparedStatementFactory(QuoteSymbol quoteSymbol) {
    this.quoteSymbol = quoteSymbol;
  }

  public SqlPreparedStatement createSqlStatement(final ConfigurationContext context) {

    Element element = context.getElement();
    String s = DomHelper.getInnerText(element);
    SqlPreparedStatement statement = SqlPreparedStatementParser.parse(context.getQnameContext(),s,quoteSymbol);

    return statement;
  }
}

