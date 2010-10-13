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

package com.servingxml.expr.saxpath;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import junit.framework.TestCase;

import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.ParameterBuilder;

public class RestrictedMatchParserTest extends TestCase {
  private SaxPath bookStoreMatchContext = null;
  private SaxPath booksMatchContext = null;
  private SaxPath bookMatchContext = null;
  private SaxPath authorMatchContext = null;
  private SaxPath titleMatchContext = null;
  private SaxPath reviewMatchContext = null;

  private MutableNameTable nameTable = new NameTableImpl();

  public RestrictedMatchParserTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    Attributes emptyAttributes = new AttributesImpl();

    AttributesImpl booksAttributes = new AttributesImpl();
    booksAttributes.addAttribute("","category","category","CDATA","F");
    booksMatchContext = new SaxPath(nameTable,"","books","books",booksAttributes);
    AttributesImpl bookAttributes = new AttributesImpl();
    bookAttributes.addAttribute("","price","price","CDATA","9999999");
    bookMatchContext = new SaxPath("","book","book",bookAttributes,booksMatchContext);
    titleMatchContext = new SaxPath("","title","title",emptyAttributes,bookMatchContext);
    reviewMatchContext = new SaxPath("","review","review",emptyAttributes,titleMatchContext);
    authorMatchContext = new SaxPath("","author","author",emptyAttributes,bookMatchContext);
    bookStoreMatchContext = new SaxPath(nameTable,"","bookStore","bookStore",emptyAttributes);
  }

  public void testAbsolutePath() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "/books");
    RestrictedMatchPattern pattern = parser.parse();

    assertTrue("is AbsolutePath", pattern instanceof AbsolutePath);

    Record parameters = Record.EMPTY;
    boolean matched1 = pattern.match(booksMatchContext,parameters);
    assertTrue("matched1",matched1);
    boolean matched2 = pattern.match(bookStoreMatchContext,parameters);
    assertTrue("!matched2",!matched2);

  }

  public void testSingleSlash() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "/");
    RestrictedMatchPattern pattern = parser.parse();

    assertTrue("is AbsolutePath", pattern instanceof AbsolutePath);
    AbsolutePath ap = (AbsolutePath)pattern;
    if (ap.getTail() != null) {
      System.out.println("tail is not null");
    } else {
      System.out.println("tail is null");
    }

    Record parameters = Record.EMPTY;
    boolean matched1 = pattern.match(booksMatchContext,parameters);
    assertTrue("matched1",matched1);
    boolean matched2 = pattern.match(bookStoreMatchContext,parameters);
    assertTrue("matched2",matched2);
  }

  public void testAbsolutePath3() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "/books/book/author");
    RestrictedMatchPattern pattern1 = parser.parse();

    assertTrue("is AbsolutePath", pattern1 instanceof AbsolutePath);

    boolean matched1 = pattern1.match(authorMatchContext,Record.EMPTY);
    assertTrue("matched1",matched1);
  }

  public void testAbsolutePath4() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "//author");
    RestrictedMatchPattern pattern1 = parser.parse();

    assertTrue("is AbsolutePath", pattern1 instanceof AbsolutePath);

    boolean matched1 = pattern1.match(authorMatchContext,Record.EMPTY);
    assertTrue("matched1",matched1);
  }

  public void testRelativePath() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "books");
    RestrictedMatchPattern pattern = parser.parse();

    assertTrue("is RelativePath", pattern instanceof RelativePath);
  }

  public void testRelativePath3() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "books/book/title");
    RestrictedMatchPattern pattern1 = parser.parse();

    assertTrue("is RelativePath", pattern1 instanceof RelativePath);

    boolean matched1 = pattern1.match(titleMatchContext,Record.EMPTY);
    assertTrue("matched1",matched1);
  }

  public void testRelativePath4() throws Exception {                           
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "books/*/title");
    RestrictedMatchPattern pattern1 = parser.parse();

    assertTrue("is RelativePath", pattern1 instanceof RelativePath);

    boolean matched1 = pattern1.match(titleMatchContext,Record.EMPTY);
    assertTrue("matched1",matched1);
  }

  public void testRelativePath5() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "books//title");
    RestrictedMatchPattern pattern1 = parser.parse();

    assertTrue("is RelativePath", pattern1 instanceof RelativePath);

    boolean matched1 = pattern1.match(titleMatchContext,Record.EMPTY);
    assertTrue("matched1",matched1);
  }

  public void testRelativePath6() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser1 = new RestrictedMatchParser(context, "books//review");
    RestrictedMatchPattern pattern1 = parser1.parse();

    assertTrue("is RelativePath", pattern1 instanceof RelativePath);

    boolean matched1 = pattern1.match(reviewMatchContext,Record.EMPTY);
    assertTrue("matched1",matched1);

    boolean matched2 = pattern1.match(titleMatchContext,Record.EMPTY);
    assertTrue("matched2",!matched2);
  }

  public void testPredicate() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "books/book[@price=9999999]");
    RestrictedMatchPattern pattern = parser.parse();
    assertTrue("is RelativePath", pattern instanceof RelativePath);
    boolean matched1 = pattern.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched1",matched1);

    RestrictedMatchParser parser2 = new RestrictedMatchParser(context, "books/book[@price='9999999']");
    RestrictedMatchPattern pattern2 = parser2.parse();
    boolean matched2 = pattern2.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched2",matched2);

    RestrictedMatchParser parser3 = new RestrictedMatchParser(context, "books/book[@price='999999']");
    RestrictedMatchPattern pattern3 = parser3.parse();
    boolean matched3 = pattern3.match(bookMatchContext,Record.EMPTY);
    assertTrue("!matched3",!matched3);

    RestrictedMatchParser parser4 = new RestrictedMatchParser(context, "books/book[9999999=@price]");
    RestrictedMatchPattern pattern4 = parser4.parse();
    assertTrue("is RelativePath", pattern4 instanceof RelativePath);
    boolean matched4 = pattern4.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched4",matched4);

    RestrictedMatchParser parser5 = new RestrictedMatchParser(context, "books/book['9999999'=@price]");
    RestrictedMatchPattern pattern5 = parser5.parse();
    boolean matched5 = pattern5.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched5",matched5);

    RestrictedMatchParser parser6 = new RestrictedMatchParser(context, "books/book['999999'=@price]");
    RestrictedMatchPattern pattern6 = parser6.parse();
    boolean matched6 = pattern6.match(bookMatchContext,Record.EMPTY);
    assertTrue("!matched6",!matched6);

    RestrictedMatchParser parser7 = new RestrictedMatchParser(context, "books/book['999999'!=@price]");
    RestrictedMatchPattern pattern7 = parser7.parse();
    boolean matched7 = pattern7.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched7",matched7);
  }

  public void testLTGT() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser8 = new RestrictedMatchParser(context, "books/book[999999>999999]");
    RestrictedMatchPattern pattern8 = parser8.parse();
    boolean matched8 = pattern8.match(bookMatchContext,Record.EMPTY);
    assertTrue("!matched8",!matched8);

    RestrictedMatchParser parser9 = new RestrictedMatchParser(context, "books/book[999999>999998]");
    RestrictedMatchPattern pattern9 = parser9.parse();
    boolean matched9 = pattern9.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched9",matched9);

    RestrictedMatchParser parser10 = new RestrictedMatchParser(context, "books/book[999999<999999]");
    RestrictedMatchPattern pattern10 = parser10.parse();
    boolean matched10 = pattern10.match(bookMatchContext,Record.EMPTY);
    assertTrue("!matched10",!matched10);

    RestrictedMatchParser parser11 = new RestrictedMatchParser(context, "books/book[999998<999999]");
    RestrictedMatchPattern pattern11 = parser11.parse();
    boolean matched11 = pattern11.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched11",matched11);

    RestrictedMatchParser parser12 = new RestrictedMatchParser(context, "books/book[999999<=999999]");
    RestrictedMatchPattern pattern12 = parser12.parse();
    boolean matched12 = pattern12.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched12",matched12);

    RestrictedMatchParser parser13 = new RestrictedMatchParser(context, "books/book[999999>=999999]");
    RestrictedMatchPattern pattern13 = parser13.parse();
    boolean matched13 = pattern13.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched13",matched13);

    RestrictedMatchParser parser14 = new RestrictedMatchParser(context, "books/book[999998>=999999]");
    RestrictedMatchPattern pattern14 = parser14.parse();
    boolean matched14 = pattern14.match(bookMatchContext,Record.EMPTY);
    assertTrue("!matched14",!matched14);
  }

  public void testNumbers() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser8 = new RestrictedMatchParser(context, "books/book[999999>999999.0]");
    RestrictedMatchPattern pattern8 = parser8.parse();
    boolean matched8 = pattern8.match(bookMatchContext,Record.EMPTY);
    assertTrue("!matched8",!matched8);

    RestrictedMatchParser parser9 = new RestrictedMatchParser(context, "books/book[999999<999999.0]");
    RestrictedMatchPattern pattern9 = parser9.parse();
    boolean matched9 = pattern9.match(bookMatchContext,Record.EMPTY);
    assertTrue("!matched9",!matched9);

    RestrictedMatchParser parser11 = new RestrictedMatchParser(context, "books/book[999999<=999999.0]");
    RestrictedMatchPattern pattern11 = parser11.parse();
    boolean matched11 = pattern11.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched11",matched11);

    RestrictedMatchParser parser12 = new RestrictedMatchParser(context, "books/book[999999.0<=999999]");
    RestrictedMatchPattern pattern12 = parser12.parse();
    boolean matched12 = pattern12.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched12",matched12);

    RestrictedMatchParser parser13 = new RestrictedMatchParser(context, "books/book[999999>=999999.0]");
    RestrictedMatchPattern pattern13 = parser13.parse();
    boolean matched13 = pattern13.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched13",matched13);

    RestrictedMatchParser parser14 = new RestrictedMatchParser(context, "books/book[999999.0>=999999]");
    RestrictedMatchPattern pattern14 = parser14.parse();
    boolean matched14 = pattern14.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched14",matched14);
  }

  public void testPredicateParameter() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    ParameterBuilder paramBuilder = new ParameterBuilder();
    paramBuilder.setString(new QualifiedName("","use"),"yes");
    Record parameters = paramBuilder.toRecord();

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "books/book[$use='yes']");
    RestrictedMatchPattern pattern = parser.parse();

    assertTrue("is RelativePath", pattern instanceof RelativePath);

    boolean matched1 = pattern.match(bookMatchContext,parameters);
    assertTrue("matched1",matched1);
  }

  public void testTwoPredicates() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser1 = new RestrictedMatchParser(context, "books[@category='F']/book[@price=9999999]");
    RestrictedMatchPattern pattern1 = parser1.parse();
    boolean matched1 = pattern1.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched1",matched1);

    RestrictedMatchParser parser2 = new RestrictedMatchParser(context, "books[@category='C']/book[@price=9999999]");
    RestrictedMatchPattern pattern2 = parser2.parse();
    boolean matched2 = pattern2.match(bookMatchContext,Record.EMPTY);
    assertTrue("!matched2",!matched2);

    RestrictedMatchParser parser3 = new RestrictedMatchParser(context, "books[@category='F']/book[@price=999999]");
    RestrictedMatchPattern pattern3 = parser3.parse();
    boolean matched3 = pattern3.match(bookMatchContext,Record.EMPTY);
    assertTrue("!matched3",!matched3);
  }

  public void testOperatorPrecedence() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    ParameterBuilder paramBuilder = new ParameterBuilder();
    paramBuilder.setString(new QualifiedName("","use"),"yes");
    paramBuilder.setString(new QualifiedName("","skip"),"no");
    Record parameters = paramBuilder.toRecord();          

    RestrictedMatchParser parser = new RestrictedMatchParser(context, "books/book[$skip='no' and $use='maybe' or $use='yes']");
    RestrictedMatchPattern pattern = parser.parse();
                                                                                 
    assertTrue("is RelativePath", pattern instanceof RelativePath);

    boolean matched1 = pattern.match(bookMatchContext,parameters);
    assertTrue("matched1",matched1);
  }

  public void testParenthesizedExpressions() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    ParameterBuilder paramBuilder = new ParameterBuilder();
    paramBuilder.setString(new QualifiedName("","selection"),"fiction");
    Record parameters = paramBuilder.toRecord();

    RestrictedMatchParser parser1 = new RestrictedMatchParser(context, "books[(@category='F' or @category='C' or @category='SF') and $selection='crime']/book");
    RestrictedMatchPattern pattern1 = parser1.parse();
    assertTrue("is RelativePath", pattern1 instanceof RelativePath);
    boolean matched1 = pattern1.match(bookMatchContext,parameters);
    assertTrue("!matched1",!matched1);

    RestrictedMatchParser parser2 = new RestrictedMatchParser(context, "books[@category='F' or @category='C' or @category='SF' and $selection='crime']/book");
    RestrictedMatchPattern pattern2 = parser2.parse();
    assertTrue("is RelativePath", pattern2 instanceof RelativePath);
    boolean matched2 = pattern2.match(bookMatchContext,parameters);
    assertTrue("matched2",matched2);
  }

  public void testNumberParameter() throws Exception {
    SimpleQnameContext context = new SimpleQnameContext(nameTable);

    RestrictedMatchParser parser1 = new RestrictedMatchParser(context, "books/book[1]");
    RestrictedMatchPattern pattern1 = parser1.parse();
    boolean matched1 = pattern1.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched1",matched1);

    RestrictedMatchParser parser2 = new RestrictedMatchParser(context, "books/book[2]");
    RestrictedMatchPattern pattern2 = parser2.parse();
    boolean matched2 = pattern2.match(bookMatchContext,Record.EMPTY);
    assertTrue("matched2",matched2);

    RestrictedMatchParser parser3 = new RestrictedMatchParser(context, "books/book[4]");
    RestrictedMatchPattern pattern3 = parser3.parse();
    boolean matched3 = pattern3.match(bookMatchContext,Record.EMPTY);
    assertTrue("!matched3",!matched3);
  }
}
