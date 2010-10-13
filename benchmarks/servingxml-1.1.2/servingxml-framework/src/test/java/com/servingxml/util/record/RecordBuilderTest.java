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

package com.servingxml.util.record;

import java.util.Map;
import java.util.HashMap;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import samples.books.Book;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.QualifiedName;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordBuilderTest extends TestCase {

  private static final String myNamespaceUri = "http://mycompany.com/mynames/";

  public RecordBuilderTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testValueOfScalarField() throws Exception {
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME,0);
    Name bookName = new QualifiedName("book");
    recordBuilder.setString(bookName,"Java");
    Record record = recordBuilder.toRecord();
    String value = record.getString(bookName);
    assertTrue("value not null",value != null);
    assertTrue("Java=" + value,value.equals("Java"));
  }

  public void testScalarFieldGetValues() throws Exception {
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME,0);
    Name bookName = new QualifiedName("book");
    recordBuilder.setString(bookName,"Java");
    Record record = recordBuilder.toRecord();
    String[] values = record.getStringArray(bookName);
    assertTrue("values not null",values != null);
    assertTrue("1=="+values.length,values.length == 1);
    assertTrue("Java=" + values[0],values[0].equals("Java"));
  }

  public void testValueOfArrayField() throws Exception {
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME,0);
    Name bookName = new QualifiedName("book");
    String[] a = new String[]{"Java","C++"};
    recordBuilder.setStringArray(bookName,a);
    Record record = recordBuilder.toRecord();
    String valueOf = record.getString(bookName);
    assertTrue("valueOf not null",valueOf != null);
    assertTrue("Java=" + valueOf,valueOf.equals("Java"));
  }

  public void testArrayFieldGetValues() throws Exception {
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME,0);
    Name bookName = new QualifiedName("book");
    String[] a = new String[]{"Java","C++"};
    recordBuilder.setStringArray(bookName,a);
    Record record = recordBuilder.toRecord();
    String[] values = record.getStringArray(bookName);
    assertTrue("values not null",values != null);
    assertTrue("2=="+values.length,values.length == 2);
    assertTrue("Java=" + values[0],values[0].equals("Java"));
    assertTrue("C++=" + values[1],values[1].equals("C++"));
  }
/*
  public void testAppend() throws Exception {
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME,0);
    Name bookName = new QualifiedName("book");
    Name authorName = new QualifiedName("author");
    recordBuilder.append(bookName,"Java");
    recordBuilder.append(bookName,"C++");
    recordBuilder.append(authorName,"Martin");
    Record record = recordBuilder.toRecord();
    String[] values = record.getStringArray(bookName);
    assertTrue("values not null",values != null);
    assertTrue("2=="+values.length,values.length == 2);
    assertTrue("Java=" + values[0],values[0].equals("Java"));
    assertTrue("C++=" + values[1],values[1].equals("C++"));

    String valueOf = record.getString(bookName);
    assertTrue("valueOf not null",valueOf != null);
    assertTrue("Java=" + valueOf,valueOf.equals("Java"));
  }
*/  

  public void testGetSetObject() throws Exception {
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME,0);
    Map map = new HashMap();
    map.put("key","value");
    Name objectName = new QualifiedName("map");
    recordBuilder.setObject(objectName,map);
    Record record = recordBuilder.toRecord();
    Map map2 = (Map)record.getObject(objectName);
    assertTrue("value not null",map2 != null);
    String value = (String)map2.get("key");
    assertTrue("value=" + value,value.equals("value"));
  }
}                    

