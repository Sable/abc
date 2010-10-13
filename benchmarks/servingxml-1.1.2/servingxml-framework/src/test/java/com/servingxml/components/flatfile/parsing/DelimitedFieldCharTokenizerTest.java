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

package com.servingxml.components.flatfile.parsing;

import java.nio.charset.Charset;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.layout.FlatRecordReceiverAdaptor;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.RepeatDelimiter;
import com.servingxml.components.flatfile.options.SegmentDelimiter;
import com.servingxml.components.flatfile.options.FlatFileOptionsImpl;
import com.servingxml.components.flatfile.options.FieldDelimiter;

import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DelimitedFieldCharTokenizerTest extends TestCase {

  public DelimitedFieldCharTokenizerTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
  }

  public void testTokenizer() throws Exception {
    final char[] input = "Mewsette,Jaune Tom,".toCharArray();

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);

    Delimiter fieldDelimiter = new FieldDelimiter(",");
    Delimiter[] fieldDelimiters = new Delimiter[]{fieldDelimiter};
    flatFileOptions.setFieldDelimiters(fieldDelimiters);

    CharTokenizer tokenizer = new DelimitedFieldCharTokenizer(flatFileOptions, 10);

    System.out.println("Before tokenize");
    tokenizer.tokenize(input,0,input.length);
    System.out.println("After tokenize");

    int token = tokenizer.getCurrentToken();
    assertTrue("token is string",token == Token.STRING);
    String value = tokenizer.getCurrentTokenValue();
    assertTrue("token value is Mewsette",value.equals("Mewsette"));

    tokenizer.next();
    token = tokenizer.getCurrentToken();
    assertTrue("token is end-of-field",token == Token.END_OF_FIELD);

    tokenizer.next();
    token = tokenizer.getCurrentToken();
    assertTrue("token is string",token == Token.STRING);
    value = tokenizer.getCurrentTokenValue();
    assertTrue("Jaune Tom=" + value,value.equals("Jaune Tom"));
  }
}                    

