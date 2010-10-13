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

package com.servingxml.components.flatfile.layout;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.zip.CRC32;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import com.servingxml.ioc.resources.ResourceTableImpl;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CrcFileCheckerTest extends TestCase {

  public CrcFileCheckerTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {

  }

  public void testCalcCrc() throws Exception {
/*
    try {

      String fileName = "C:/projects/servingxml-etc/header/rawdata.txt";

      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));

      CRC32 crcValue = new CRC32();

      int byteValue;

      int n = 0;
      while ((byteValue = bis.read())!=-1) {
        crcValue.update((byte)byteValue);
        ++n;
      }

      //System.out.println ("n = " + n);
      //System.out.println ("The value of CRC is "+ crcValue.getValue());

    } catch (Throwable e) {
      //System.out.println ("Error calculating CRC " + e);
    }
*/
  }
}                    

