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

package com.servingxml.components.flatfile.options;

import java.util.zip.CRC32;
import java.nio.charset.Charset;

import junit.framework.TestCase;
import com.servingxml.util.CharsetHelper;

public class WhitespaceTest extends TestCase {

  public WhitespaceTest(String name) {
    super(name);
  }

  public void testSpace() throws Exception {

    WhitespaceByteDelimiterExtractor delimiter = WhitespaceByteDelimiterExtractor.newInstance(Charset.defaultCharset());
    String s1 = "          "; 
    int len1 = delimiter.foundEndDelimiter(s1.getBytes(),0,s1.length());
    assertTrue("10=" +len1, len1 == 10);
    String s2 = " bcdefghij"; 
    int len2 = delimiter.foundEndDelimiter(s2.getBytes(),0,s2.length());
    assertTrue("1=" +len2, len2 == 1);
    String s3 = "  cdefghij"; 
    int len3 = delimiter.foundEndDelimiter(s3.getBytes(),0,s3.length());
    assertTrue("2=" +len3, len3 == 2);
    String s4 = " b defghij"; 
    int len4 = delimiter.foundEndDelimiter(s4.getBytes(),0,s4.length());
    assertTrue("1=" +len4, len4 == 1);
    String s5 = "a cdefghij"; 
    int len5 = delimiter.foundEndDelimiter(s5.getBytes(),0,s5.length());
    assertTrue("0=" +len5, len5 == 0);
  }

  public void testSpaceTab() throws Exception {

    WhitespaceByteDelimiterExtractor delimiter = WhitespaceByteDelimiterExtractor.newInstance(Charset.defaultCharset());
    String s1 = "\t\t\t\t\t\t\t\t\t\t"; 
    int len1 = delimiter.foundEndDelimiter(s1.getBytes(),0,s1.length());
    assertTrue("10=" +len1, len1 == 10);
    String s2 = "\tbcdefghij"; 
    int len2 = delimiter.foundEndDelimiter(s2.getBytes(),0,s2.length());
    assertTrue("1=" +len2, len2 == 1);
    String s3 = "\t cdefghij"; 
    int len3 = delimiter.foundEndDelimiter(s3.getBytes(),0,s3.length());
    assertTrue("2=" +len3, len3 == 2);
    String s4 = "\tb\tdefghij"; 
    int len4 = delimiter.foundEndDelimiter(s4.getBytes(),0,s4.length());
    assertTrue("1=" +len4, len4 == 1);
    String s5 = "a\tcdefghij"; 
    int len5 = delimiter.foundEndDelimiter(s5.getBytes(),0,s5.length());
    assertTrue("0=" +len5, len5 == 0);
    String s6 = " \tcdefghij"; 
    int len6 = delimiter.foundEndDelimiter(s6.getBytes(),0,s6.length());
    assertTrue("2=" +len6, len6 == 2);
  }

  public void testFormFeed() throws Exception {

    WhitespaceByteDelimiterExtractor delimiter = WhitespaceByteDelimiterExtractor.newInstance(Charset.defaultCharset());
    String s1 = "\f\f\f\f\f\f\f\f\f\f"; 
    int len1 = delimiter.foundEndDelimiter(s1.getBytes(),0,s1.length());
    assertTrue("10=" +len1, len1 == 10);
    String s2 = "\fbcdefghij"; 
    int len2 = delimiter.foundEndDelimiter(s2.getBytes(),0,s2.length());
    assertTrue("1=" +len2, len2 == 1);
    String s3 = "\f cdefghij"; 
    int len3 = delimiter.foundEndDelimiter(s3.getBytes(),0,s3.length());
    assertTrue("2=" +len3, len3 == 2);
    String s4 = "\fb\fdefghij"; 
    int len4 = delimiter.foundEndDelimiter(s4.getBytes(),0,s4.length());
    assertTrue("1=" +len4, len4 == 1);
    String s5 = "a\fcdefghij"; 
    int len5 = delimiter.foundEndDelimiter(s5.getBytes(),0,s5.length());
    assertTrue("0=" +len5, len5 == 0);
    String s6 = " \fcdefghij"; 
    int len6 = delimiter.foundEndDelimiter(s6.getBytes(),0,s6.length());
    assertTrue("2=" +len6, len6 == 2);
  }

  public void testCR() throws Exception {

    WhitespaceByteDelimiterExtractor delimiter = WhitespaceByteDelimiterExtractor.newInstance(Charset.defaultCharset());
    String s1 = "\r\r\r\r\r\r\r\r\r\r"; 
    int len1 = delimiter.foundEndDelimiter(s1.getBytes(),0,s1.length());
    assertTrue("10=" +len1, len1 == 10);
    String s2 = "\rbcdefghij"; 
    int len2 = delimiter.foundEndDelimiter(s2.getBytes(),0,s2.length());
    assertTrue("1=" +len2, len2 == 1);
    String s3 = "\r cdefghij"; 
    int len3 = delimiter.foundEndDelimiter(s3.getBytes(),0,s3.length());
    assertTrue("2=" +len3, len3 == 2);
    String s4 = "\rb\rdefghij"; 
    int len4 = delimiter.foundEndDelimiter(s4.getBytes(),0,s4.length());
    assertTrue("1=" +len4, len4 == 1);
    String s5 = "a\rcdefghij"; 
    int len5 = delimiter.foundEndDelimiter(s5.getBytes(),0,s5.length());
    assertTrue("0=" +len5, len5 == 0);
    String s6 = " \rcdefghij"; 
    int len6 = delimiter.foundEndDelimiter(s6.getBytes(),0,s6.length());
    assertTrue("2=" +len6, len6 == 2);
  }

  public void testLineFeed() throws Exception {

    WhitespaceByteDelimiterExtractor delimiter = WhitespaceByteDelimiterExtractor.newInstance(Charset.defaultCharset());
    String s1 = "\n\n\n\n\n\n\n\n\n\n"; 
    int len1 = delimiter.foundEndDelimiter(s1.getBytes(),0,s1.length());
    assertTrue("10=" +len1, len1 == 10);
    String s2 = "\nbcdefghij"; 
    int len2 = delimiter.foundEndDelimiter(s2.getBytes(),0,s2.length());
    assertTrue("1=" +len2, len2 == 1);
    String s3 = "\n cdefghij"; 
    int len3 = delimiter.foundEndDelimiter(s3.getBytes(),0,s3.length());
    assertTrue("2=" +len3, len3 == 2);
    String s4 = "\nb\ndefghij"; 
    int len4 = delimiter.foundEndDelimiter(s4.getBytes(),0,s4.length());
    assertTrue("1=" +len4, len4 == 1);
    String s5 = "a\ncdefghij"; 
    int len5 = delimiter.foundEndDelimiter(s5.getBytes(),0,s5.length());
    assertTrue("0=" +len5, len5 == 0);
    String s6 = " \ncdefghij"; 
    int len6 = delimiter.foundEndDelimiter(s6.getBytes(),0,s6.length());
    assertTrue("2=" +len6, len6 == 2);
  }
}




