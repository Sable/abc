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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import samples.books.Book;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.Name;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.record.Record;
import com.servingxml.util.QualifiedName;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FixedLayoutRecordBuilderTest extends TestCase {

  private static final String myNamespaceUri = "http://mycompany.com/mynames/";

  public FixedLayoutRecordBuilderTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testVolume() throws Exception {
    MutableNameTable nameTable = new NameTableImpl();

    Name bookName = new QualifiedName("book");
    Name authorName = new QualifiedName("author");
    FieldType[] fieldTypes = new FieldType[]{
      new DefaultFieldType(bookName),new DefaultFieldType(authorName)
    };
    RecordType recordType = new RecordTypeImpl(SystemConstants.PARAMETERS_TYPE_NAME,fieldTypes);

    FixedLayoutRecordBuilder recordBuilder = new FixedLayoutRecordBuilder(recordType);

    for (int i = 0; i < 1000; ++i) {
      recordBuilder.appendValue(bookName,"Java " + i);
    }

    for (int i = 0; i < 1000; ++i) {
      recordBuilder.appendValue(authorName,"Martin " + i);
    }
    Record record = recordBuilder.toRecord();
    String[] values = record.getStringArray(bookName);
    assertTrue("values not null",values != null);
    assertTrue("1000=="+values.length,values.length == 1000);
    assertTrue("Java 0=" + values[0],values[0].equals("Java 0"));

    String valueOf = record.getString(bookName);
    assertTrue("valueOf not null",valueOf != null);
    assertTrue("Java 0=" + valueOf,valueOf.equals("Java 0"));

    String authorValueOf = record.getString(authorName);
    assertTrue("authorValueOf not null",authorValueOf != null);
    assertTrue("Martin 0=" + authorValueOf,authorValueOf.equals("Martin 0"));
  }

  public void testAppend() throws Exception {
    MutableNameTable nameTable = new NameTableImpl();

    Name bookName = new QualifiedName("book");
    Name authorName = new QualifiedName("author");
    FieldType[] fieldTypes = new FieldType[]{
      new DefaultFieldType(bookName),new DefaultFieldType(authorName)
    };
    RecordType recordType = new RecordTypeImpl(SystemConstants.PARAMETERS_TYPE_NAME,fieldTypes);

    FixedLayoutRecordBuilder recordBuilder = new FixedLayoutRecordBuilder(recordType);
    recordBuilder.appendValue(bookName,"Java");
    recordBuilder.appendValue(bookName,"C++");
    recordBuilder.appendValue(authorName,"Martin");
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
  
}                    

