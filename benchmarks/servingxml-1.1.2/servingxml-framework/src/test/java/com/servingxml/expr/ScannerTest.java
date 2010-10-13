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

package com.servingxml.expr;

import junit.framework.TestCase;

import com.servingxml.util.Name;

public class ScannerTest extends TestCase {
  
  public ScannerTest(String name) {
    super(name);
  }
    
  protected void setUp() throws Exception {
  }

  public void testTokenizerInitialization1() throws Exception {
    Scanner scanner = new Scanner("/b/c");

    scanner.first();
    assertTrue("currentTokenType - Slash",scanner.currentTokenType == TokenType.SLASH);

    scanner.next();

    assertTrue("currentTokenType - Name",scanner.currentTokenType == TokenType.NAME);

    //assertTrue("currentTokenValue = " + scanner.currentTokenValue,scanner.currentTokenValue.equals("/"));
  }

  public void testTokenizerInitializationNS1() throws Exception {
    Scanner scanner = new Scanner("/sx:b/sx:c");
    scanner.first();

    assertTrue("currentTokenType - Slash",scanner.currentTokenType == TokenType.SLASH);

    scanner.next();

    assertTrue("currentTokenType - Name",scanner.currentTokenType == TokenType.NAME);

    //assertTrue("currentTokenValue = " + scanner.currentTokenValue,scanner.currentTokenValue.equals("/"));
  }
  
  public void testTokenizerInitialization2() throws Exception {
    Scanner scanner = new Scanner("/books");
    scanner.first();

    assertTrue("currentTokenType - Slash",scanner.currentTokenType == TokenType.SLASH);

    scanner.next();

    assertTrue("currentTokenType - Name",scanner.currentTokenType == TokenType.NAME);

    //assertTrue("currentTokenValue = " + scanner.currentTokenValue,scanner.currentTokenValue.equals("/"));
  }

  public void testTokenizerInitialization3() throws Exception {
    Scanner scanner = new Scanner("books");
    scanner.first();

    assertTrue("currentTokenType",scanner.currentTokenType == TokenType.NAME);
    assertTrue("currentTokenValue = " + scanner.currentTokenValue,scanner.currentTokenValue.equals("books"));
  }

  public void testTokenizerInitialization4() throws Exception {
    Scanner scanner = new Scanner("b/c");
    scanner.first();

    assertTrue("currentTokenType",scanner.currentTokenType == TokenType.NAME);
    assertTrue("currentTokenValue = " + scanner.currentTokenValue,scanner.currentTokenValue.equals("b"));
  }

  public void testTokenizerInitializationNS4() throws Exception {
    Scanner scanner = new Scanner("sx:b/sx:c");
    scanner.first();

    assertTrue("currentTokenType",scanner.currentTokenType == TokenType.NAME);
    assertTrue("currentTokenValue = " + scanner.currentTokenValue,scanner.currentTokenValue.equals("sx:b"));
  }

  public void testNumber() throws Exception {
    Scanner scanner = new Scanner("100");
    scanner.first();

    assertTrue("currentTokenType",scanner.currentTokenType == TokenType.NUMBER);
    assertTrue("currentTokenValue = " + scanner.currentTokenValue,scanner.currentTokenValue.equals("100"));
  }

  public void testLSB() throws Exception {
    Scanner scanner = new Scanner("[");
    scanner.first();

    assertTrue("currentTokenType",scanner.currentTokenType == TokenType.LEFT_SQUARE);
  }

  public void testRSB() throws Exception {
    Scanner scanner = new Scanner("]");
    scanner.first();

    assertTrue("currentTokenType",scanner.currentTokenType == TokenType.RIGHT_SQUARE);
  }

  public void testIndex() throws Exception {
    Scanner scanner = new Scanner("name[3]");
    scanner.first();

    assertTrue("NAME",scanner.currentTokenType == TokenType.NAME);
    assertTrue("name = " + scanner.currentTokenValue,scanner.currentTokenValue.equals("name"));
    scanner.next();
    assertTrue("[",scanner.currentTokenType == TokenType.LEFT_SQUARE);
    scanner.next();
    assertTrue("NUMBER",scanner.currentTokenType == TokenType.NUMBER);
    assertTrue("3 = " + scanner.currentTokenValue,scanner.currentTokenValue.equals("3"));
    scanner.next();
    assertTrue("]",scanner.currentTokenType == TokenType.RIGHT_SQUARE);
    scanner.next();
    assertTrue("EOF",scanner.currentTokenType == TokenType.EOF);
  }
}
