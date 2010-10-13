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

public class RecordTest extends TestCase {

  private static final String myNamespaceUri = "http://mycompany.com/mynames/";
  
  private final int capacity = 10000;

  public RecordTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testRecord() throws Exception {
    MutableNameTable nameTable = new NameTableImpl();
    RecordBuilder recordBuilder = new RecordBuilder(SystemConstants.PARAMETERS_TYPE_NAME,0);
    
    final Name[] names = new Name[capacity];
    final String[] values = new String[capacity];
    
    for (int i = 0; i < capacity; ++i) {
      names[i] = new QualifiedName("name" + i);
      values[i] = "value" + i;
    }
    
    for (int i = 0; i < capacity; ++i ) {
      recordBuilder.setString(names[i],values[i]);
    }
    Record record = recordBuilder.toRecord();
    RecordType recordType = record.getRecordType();
    for (int i = 0; i < recordType.count(); ++i) {
      FieldType fieldType = recordType.getFieldType(i);
      Value value = record.getValue(i);
      assertTrue("name",fieldType.getName().equals(names[i]));
      String s = value.getString();
      assertTrue("value",s.equals(values[i]));
    }
    
    assertTrue("" + recordType.count() + "==" + capacity,recordType.count() == capacity);
  }
}                    

