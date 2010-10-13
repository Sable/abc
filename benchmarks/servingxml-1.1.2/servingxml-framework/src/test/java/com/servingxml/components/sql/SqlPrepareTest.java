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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import com.servingxml.util.QnameContext;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.components.quotesymbol.QuoteSymbol;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlPrepareTest extends TestCase {
  private final static QnameContext context = new SimpleQnameContext();
  private QuoteSymbol quoteSymbol = new QuoteSymbol('\'', "''");

  public SqlPrepareTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
  }

  public void testSqlPrepare() throws Exception {
    String s = "select counterparty, quantity from trades where maturity_date >= {$maturityDate} and "
      + "trade_id = {tradeId}";

    String expected = "select counterparty, quantity from trades where maturity_date >= ? and trade_id = ?";

    SqlPreparedStatement statement = SqlPreparedStatementParser.parse(context, s, quoteSymbol);
    assertTrue(expected + "=" + statement.getStatement(),statement.getStatement().equals(expected));
  }

  public void testSqlPrepare_orderBy() throws Exception {
    String s = "select counterparty, quantity from trades where maturity_date >= {$maturityDate} and "
      + "trade_id = {tradeId} order by counterparty";

    String expected = "select counterparty, quantity from trades where maturity_date >= ? and trade_id = ? order by counterparty";

    SqlPreparedStatement statement = SqlPreparedStatementParser.parse(context, s, quoteSymbol);
    assertTrue(expected + "=" + statement.getStatement(),statement.getStatement().equals(expected));
  }
}                    

