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

import com.servingxml.util.HexBin;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class PackedDecimalTest extends TestCase {

  public PackedDecimalTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
  }

  public void testPackedDecimal() throws Exception {
    PackedDecimal myPacked = PackedDecimal.parse("0.3542", 10, 2); 
    //System.out.println (" Decimal value is : "  + myPacked.toString()); 
    assertTrue("0.35="+myPacked.toString(),myPacked.toString().equals("0.35"));

    PackedDecimal myPacked1 = PackedDecimal.parse("1543.2545", 3, 3); 
    //System.out.println (" Decimal value is : "  + myPacked1.toString()); 
    //assertTrue("1543.2545",myPacked1.toString().equals("1543.254"));

    PackedDecimal myPacked2 = PackedDecimal.parse("3.2", 10, 2); 
    //System.out.println (" Decimal value is : "  + myPacked2.toString()); 
    //assertTrue("3.2",myPacked2.toString().equals("3.20"));

    PackedDecimal myPacked3 = PackedDecimal.parse("412", 10, 2);
    //System.out.println (" Decimal value is : "  + myPacked3.toString());
    //assertTrue("412",myPacked3.toString().equals("412.00"));

    valueAndExpected("+0", 1, 0, "0C");
    valueAndExpected("+1", 1, 0, "1C");
    valueAndExpected("+12", 2, 0, "012C");
    valueAndExpected("+123", 3, 0, "123C");
    valueAndExpected("+1234", 4, 0, "01234C");
    valueAndExpected("-1", 1, 0, "1D");
    valueAndExpected("-1234", 4, 0, "01234D");
  }

  private void valueAndExpected(String value, int wholeDigits, int decimalDigits, String expected) 
  throws Exception {
    PackedDecimal myPacked = PackedDecimal.parse(value, wholeDigits, decimalDigits);
    byte[] myData = myPacked.getPackedData();
    String s = HexBin.encode(myData);
    assertTrue(s+"="+expected, s.equals(expected));
  }
}                    


