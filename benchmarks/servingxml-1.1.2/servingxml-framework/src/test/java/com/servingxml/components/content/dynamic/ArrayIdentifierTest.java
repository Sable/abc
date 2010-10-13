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

package com.servingxml.components.content.dynamic;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ArrayIdentifierTest extends TestCase {

  public ArrayIdentifierTest(String name) {
    super(name);
  }
  protected void setUp() {
  }

  public void testArrayIdentity() {
    String[] a1 = new String[] {"IBM", "GNM", "XIU"};
    String[] a2 = new String[] {"IBM", "GNM", "XIU"};
    String[] a3 = new String[] {"GNM", "IBM", "XIU"};
    String[] a4 = new String[] {"GNM", "XIU", "IBM"};
    String[] a5 = new String[] {"IBM", "XIU", "ATT"};
    String[] a6 = new String[] {};
    String[] a7 = new String[] {"IBM", "GNM", "XIU", "ATT"};
    String[] a8 = new String[] {"IBM", "GNM"};
    ArrayIdentifier identifier = new ArrayIdentifier();

    boolean isEqual = identifier.equalTo(a1,a2);
    assertTrue("identical arrays", (isEqual));
    isEqual = identifier.equalTo(a1,a3);
    assertTrue("same elements, different order", (isEqual));
    isEqual = identifier.equalTo(a1,a4);
    assertTrue("same elements, different order", (isEqual));
    isEqual = identifier.equalTo(a1,a5);
    assertTrue("different arrays", !(isEqual));
    isEqual = identifier.equalTo(a1,a6);
    assertTrue("compare with empty array", !(isEqual));
    isEqual = identifier.equalTo(a1,a7);
    assertTrue("compare with larger array", !(isEqual));
    isEqual = identifier.equalTo(a1,a8);
    assertTrue("compare with smaller array", !(isEqual));
    isEqual = identifier.equalTo(a6,a6);
    assertTrue("compare two empty arrays", (isEqual));
  }
}                    

