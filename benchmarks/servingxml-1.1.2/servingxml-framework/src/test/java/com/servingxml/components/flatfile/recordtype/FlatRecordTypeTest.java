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

package com.servingxml.components.flatfile.recordtype;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatRecordTypeTest extends TestCase {

  private static final String MYAPP_NS_URI = "http://mycompany.com/mynames/";
  private SimpleIocContainer resources;

  public FlatRecordTypeTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    //resources = new SimpleIocContainer();
  }

  public void testLookupRecordTypes() throws Exception {
/*
    Name resourceTypeName = new QualifiedName(SystemConstants.SERVINGXML_NS_URI,
      "flatFile");
    Name instanceName = new QualifiedName(MYAPP_NS_URI,"tradeFileLayout");
    FlatFile layout = (FlatFile)resources.lookupServiceComponent(resourceTypeName,instanceName);
    assertTrue("layout not null",layout != null);
    
    resourceTypeName = new QualifiedName(SystemConstants.SERVINGXML_NS_URI,
      "flatRecordType");
    instanceName = new QualifiedName(MYAPP_NS_URI,"trade");
    FlatFileRecordTypeFactory tradeRecordType = (FlatFileRecordTypeFactory)resources.lookupServiceComponent(resourceTypeName,instanceName);
    assertTrue("trade record type not null",tradeRecordType != null);
    
    resourceTypeName = new QualifiedName(SystemConstants.SERVINGXML_NS_URI,
      "flatRecordType");
    instanceName = new QualifiedName(MYAPP_NS_URI,"transaction");
    FlatFileRecordTypeFactory transactionRecordType = (FlatFileRecordTypeFactory)resources.lookupServiceComponent(resourceTypeName,instanceName);
    assertTrue("transaction record type not null",transactionRecordType != null);
*/    
  }
}                    

