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

package com.servingxml;

import java.util.Enumeration;
import java.net.JarURLConnection;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;


/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ServingXmlTests extends TestSuite {

  public ServingXmlTests() {
    super("ServingXML");

    System.setProperty("javax.xml.transform.TransformerFactory","net.sf.saxon.TransformerFactoryImpl");
/*
    addTest(new TestSuite(com.servingxml.io.cache.CacheEntryListTest.class));
    
    addTest(new TestSuite(com.servingxml.util.FaultTest.class));
    addTest(new TestSuite(com.servingxml.components.content.dynamic.DynamicContentTest.class));
    addTest(new TestSuite(com.servingxml.components.content.DocumentTest.class));
    addTest(new TestSuite(com.servingxml.components.DocumentFilterTest.class));
    addTest(new TestSuite(com.servingxml.util.xml.DomIteratorTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.recordtype.FlatRecordTypeTest.class));

    addTest(new TestSuite(com.servingxml.components.inverserecordmapping.InverseRecordMappingTest.class));

    addTest(new TestSuite(com.servingxml.components.regex.PatternMatcherTest.class));
    addTest(new TestSuite(com.servingxml.components.parameter.ParameterTest.class));


    addTest(new TestSuite(com.servingxml.components.cache.CacheTest.class));

    addTest(new TestSuite(com.servingxml.util.xml.DomNameContextTest.class));
    addTest(new TestSuite(com.servingxml.util.record.RecordTest.class));
    addTest(new TestSuite(com.servingxml.util.record.FixedLayoutRecordBuilderTest.class));
    addTest(new TestSuite(com.servingxml.expr.substitution.SubstitutionExprTest.class));
    addTest(new TestSuite(com.servingxml.expr.ScannerTest.class));
    addTest(new TestSuite(com.servingxml.util.NameTableTest.class));
    addTest(new TestSuite(com.servingxml.util.LineFormatterTest.class));
    addTest(new TestSuite(com.servingxml.components.parameter.ParameterInitializerTest.class));
    addTest(new TestSuite(com.servingxml.util.record.RecordBuilderTest.class));
    addTest(new TestSuite(com.servingxml.io.saxsource.JaxpSourceSaxSourceTest.class));
    addTest(new TestSuite(com.servingxml.io.helpers.StreamSourceHelperTest.class));
    addTest(new TestSuite(com.servingxml.components.content.ServingXmlUriResolverTest.class));
    

    addTest(new TestSuite(com.servingxml.components.sql.SqlPrepareTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.scanner.bytes.LineDelimitedByteScannerTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.scanner.bytes.FixedLineLengthByteScannerTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.options.WhitespaceTest.class));
    addTest(new TestSuite(com.servingxml.util.record.PackedDecimalTest.class));
    addTest(new TestSuite(com.servingxml.components.string.ConvertToDateTimeTest.class));
    addTest(new TestSuite(com.servingxml.util.record.PackedDecimalTest.class));
    addTest(new TestSuite(com.servingxml.util.StringHelperTest.class));
    addTest(new TestSuite(com.servingxml.util.record.XmlRecordTransformReaderTest.class));
    addTest(new TestSuite(com.servingxml.util.record.MultivaluedFieldBuilderTest.class));

    addTest(new TestSuite(com.servingxml.util.UrlHelperTest.class));
    addTest(new TestSuite(com.servingxml.util.NameTestTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.scanner.bytes.RecordBlockTest.class));

    addTest(new TestSuite(com.servingxml.components.choose.ChooseRecordMappingTest.class));
    addTest(new TestSuite(com.servingxml.util.record.RecordBuilderTest.class));
    addTest(new TestSuite(com.servingxml.ioc.resources.IocContainerFactoryTest.class));

    addTest(new TestSuite(com.servingxml.util.xml.XPathExpressionTest.class));
    addTest(new TestSuite(com.servingxml.util.xml.XsltChooseReaderTest.class));

    addTest(new TestSuite(com.servingxml.components.flatfile.options.DelimiterTest.class));
     
    addTest(new TestSuite(com.servingxml.components.recordmapping.MultipleGroupingRecordMapContainerTest.class));

    addTest(new TestSuite(com.servingxml.components.saxfilter.RemoveEmptyElementsTest.class));
    addTest(new TestSuite(com.servingxml.components.saxfilter.RemoveEmptyAttributesTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.recordtype.DelimitedRepeatingGroupReaderTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.recordtype.DelimitedFieldReaderTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.recordtype.MultivaluedDelimitedFieldReaderTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.options.FlatFileOptionsTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.scanner.bytes.ByteBufferTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.scanner.characters.CharBufferTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.scanner.bytes.ByteRecordInputTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.scanner.characters.CharRecordInputTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.ByteRecordOutputTest.class));
    addTest(new TestSuite(com.servingxml.util.PrefixMapTest.class));
    addTest(new TestSuite(com.servingxml.components.string.StringTest.class));
    addTest(new TestSuite(com.servingxml.components.string.StringCompilerTest.class));
    addTest(new TestSuite(com.servingxml.components.flatfile.scanner.bytes.BinaryFileTest.class));
    addTest(new TestSuite(com.servingxml.components.saxfilter.MultipleXmlFilterTest.class));
    addTest(new TestSuite(com.servingxml.expr.saxpath.RestrictedMatchParserTest.class));
*/
    addTest(new TestSuite(com.servingxml.components.flatfile.parsing.DelimitedFieldCharTokenizerTest.class));
  }                                                                    
  protected void setUp() {                                        
  }
  protected void tearDown() {
  }

  public void runSuite() {
    TestResult result = new TestResult();
    setUp();
    run(result);
    tearDown();

    System.err.println("Tests run = " + result.runCount());
    System.err.println("Failures = " + result.failureCount());
    Enumeration failures = result.failures();
    while (failures.hasMoreElements()) {
      Object failure = failures.nextElement();
      System.err.println(failure.toString());
    }
    System.err.println("Errors = " + result.errorCount());
    Enumeration errors = result.errors();
    while (errors.hasMoreElements()) {
      TestFailure error = (TestFailure)errors.nextElement();
      Throwable t = error.thrownException();
      t.printStackTrace(System.err);
      System.err.println(error.toString());
    }
  }              
  
  public static void main(String[] args) {
    try {
      //System.out.println ("BEGIN TEST");
      ServingXmlTests suite = new ServingXmlTests();
      //System.out.println ("Created suite");
      suite.runSuite();
      //System.out.println ("END TEST");

    } catch (Exception e) {
      e.printStackTrace(System.err);
    } catch (Error e) {
      e.printStackTrace(System.err);
    }

  }
}                    

