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

package com.servingxml.util;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class NameTableTest extends TestCase {

  private static String MYAPP_NS_URI="http://mycompany.com/mynames/";

  private MutableNameTable baseNameTable;

  public NameTableTest(String name) {
    super(name);
  }

  protected void setUp() {
    baseNameTable = new NameTableImpl(1,1);
    int requestId = baseNameTable.getSymbol(SystemConstants.SERVINGXML_NS_URI,"request");
  }

  public void testNameTable() throws Exception {

    int requestId = baseNameTable.lookupSymbol(SystemConstants.SERVINGXML_NS_URI,"request");

    //System.out.println("requestId1 = " + requestId);

    Name requestName = baseNameTable.lookupName(requestId);
    assertTrue("requestName",requestName.equals(new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"request")));
    
    int faultId = baseNameTable.getSymbol(SystemConstants.SERVINGXML_NS_URI,"error");
    Name faultName = baseNameTable.lookupName(faultId);
    assertTrue("faultName",faultName.equals(new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"error")));
    
    int booksId = baseNameTable.getSymbol(MYAPP_NS_URI,"books");
    Name booksName = baseNameTable.lookupName(booksId);
    assertTrue("booksName",booksName.equals(new QualifiedName(MYAPP_NS_URI,"books")));
    
    int categoryId = baseNameTable.getSymbol(MYAPP_NS_URI,"category");
    Name categoryName = baseNameTable.lookupName(categoryId);
    assertTrue("categoryName",categoryName.equals(new QualifiedName(MYAPP_NS_URI,"category")));

    assertTrue("requestName",requestName.equals(new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"request")));
    assertTrue("faultName",faultName.equals(new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"error")));
    assertTrue("booksName",booksName.equals(new QualifiedName(MYAPP_NS_URI,"books")));
    assertTrue("categoryName",categoryName.equals(new QualifiedName(MYAPP_NS_URI,"category")));

    requestId = baseNameTable.getSymbol(SystemConstants.SERVINGXML_NS_URI,"request");
    requestName = baseNameTable.lookupName(requestId);
    assertTrue("requestName",requestName.equals(new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"request")));
    
    faultId = baseNameTable.getSymbol(SystemConstants.SERVINGXML_NS_URI,"error");
    faultName = baseNameTable.lookupName(faultId);
    assertTrue("faultName",faultName.equals(new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"error")));
    
    booksId = baseNameTable.getSymbol(MYAPP_NS_URI,"books");
    booksName = baseNameTable.lookupName(booksId);
    assertTrue("booksName",booksName.equals(new QualifiedName(MYAPP_NS_URI,"books")));
    
    categoryId = baseNameTable.getSymbol(MYAPP_NS_URI,"category");
    categoryName = baseNameTable.lookupName(categoryId);
    assertTrue("categoryName",categoryName.equals(new QualifiedName(MYAPP_NS_URI,"category")));

    //baseNameTable.printDiagnostics(System.out);
  }

  public void testNameTable2() throws Exception {
    MutableNameTable nameTable = new NameTableImpl(baseNameTable);
    
    int requestId1 = baseNameTable.lookupSymbol(SystemConstants.SERVINGXML_NS_URI,"request");

    int requestId2 = nameTable.getSymbol(SystemConstants.SERVINGXML_NS_URI,"request");

    //System.out.println("requestId1 = " + requestId1 + ", requestId2 = " + requestId2);

    assertTrue("requestId1 == requestId2", requestId1 == requestId2);
  }

  public void testSynchronizedNameTable() throws Exception {
    MutableNameTable nameTable = new SynchronizedNameTable(baseNameTable);
    
    int requestId1 = baseNameTable.lookupSymbol(SystemConstants.SERVINGXML_NS_URI,"request");

    int requestId3 = nameTable.getSymbol(SystemConstants.SERVINGXML_NS_URI,"request");

    assertTrue("requestId1 == requestId3", requestId1 == requestId3);
  }

}                    

