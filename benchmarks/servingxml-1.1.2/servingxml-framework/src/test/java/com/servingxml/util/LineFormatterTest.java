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


// JAXP

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import com.servingxml.util.SystemConstants;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class LineFormatterTest extends TestCase implements SystemConstants {

  LineFormatter formatter;

  public LineFormatterTest(String name) {
    super(name);
  }
  protected void setUp() {
  }                                            

  public void testLeftJustify() throws Exception {
    String value = "Hello";
    String line = "";

    formatter = new LineFormatter(1,Alignment.LEFT,' ');
    line = formatter.format(value);
    assertTrue("",line.equals("H"));
    
    formatter = new LineFormatter(4,Alignment.LEFT,' ');
    line = formatter.format(value);
    assertTrue("",line.equals("Hell"));              
    
    formatter = new LineFormatter(5,Alignment.LEFT,' ');
    line = formatter.format(value);
    assertTrue("",line.equals("Hello"));
    
    formatter = new LineFormatter(6,Alignment.LEFT,' ');
    line = formatter.format(value);
    assertTrue("",line.equals("Hello "));
    
    formatter = new LineFormatter(10,Alignment.LEFT,' ');
    line = formatter.format(value);
    assertTrue("",line.equals("Hello     "));
  }

  public void testCenterJustify() throws Exception {
    String value = "Hello";
    String line = "";
    
    formatter = new LineFormatter(1,Alignment.CENTER,' ');
    line = formatter.format(value);
    assertTrue("width = 1",line.equals("H"));
    
    formatter = new LineFormatter(4,Alignment.CENTER,' ');
    line = formatter.format(value);
    assertTrue("width = 4",line.equals("Hell"));
    
    formatter = new LineFormatter(5,Alignment.CENTER,' ');
    line = formatter.format(value);
    assertTrue("width = 5",line.equals("Hello"));
    
    formatter = new LineFormatter(6,Alignment.CENTER,' ');
    line = formatter.format(value);
    assertTrue("width = 6",line.equals("Hello "));
    
    formatter = new LineFormatter(7,Alignment.CENTER,' ');
    line = formatter.format(value);
    assertTrue("width = 7",line.equals(" Hello "));
    
    formatter = new LineFormatter(9,Alignment.CENTER,' ');
    line = formatter.format(value);
    assertTrue("width = 9",line.equals("  Hello  "));
  }

  public void testRightJustify() throws Exception {
    String value = "Hello";
    String line = "";
    
    formatter = new LineFormatter(1,Alignment.RIGHT,' ');
    line = formatter.format(value);
    assertTrue("",line.equals("H"));
    
    formatter = new LineFormatter(4,Alignment.RIGHT,' ');
    line = formatter.format(value);
    assertTrue("",line.equals("Hell"));
    
    formatter = new LineFormatter(5,Alignment.RIGHT,' ');
    line = formatter.format(value);
    assertTrue("",line.equals("Hello"));
    
    formatter = new LineFormatter(6,Alignment.RIGHT,' ');
    line = formatter.format(value);
    assertTrue("",line.equals(" Hello"));
    
    formatter = new LineFormatter(10,Alignment.RIGHT,' ');
    line = formatter.format(value);
    assertTrue("",line.equals("     Hello"));
  }

  public void testLeftJustifyZeroFill() throws Exception {
    String value = "Hello";
    String line = "";
    
    formatter = new LineFormatter(1,Alignment.LEFT,'0');
    line = formatter.format(value);
    assertTrue("",line.equals("H"));
    
    formatter = new LineFormatter(4,Alignment.LEFT,'0');
    line = formatter.format(value);
    assertTrue("",line.equals("Hell"));
    
    formatter = new LineFormatter(5,Alignment.LEFT,'0');
    line = formatter.format(value);
    assertTrue("",line.equals("Hello"));
    
    formatter = new LineFormatter(6,Alignment.LEFT,'0');
    line = formatter.format(value);
    assertTrue("",line.equals("Hello0"));
    
    formatter = new LineFormatter(10,Alignment.LEFT,'0');
    line = formatter.format(value);
    assertTrue("",line.equals("Hello00000"));
  }

  public void testCenterJustifyZeroFill() throws Exception {
    String value = "Hello";
    String line = "";
    
    formatter = new LineFormatter(1,Alignment.CENTER,'0');
    line = formatter.format(value);
    assertTrue("width = 1",line.equals("H"));
    
    formatter = new LineFormatter(4,Alignment.CENTER,'0');
    line = formatter.format(value);
    assertTrue("width = 4",line.equals("Hell"));
    
    formatter = new LineFormatter(5,Alignment.CENTER,'0');
    line = formatter.format(value);
    assertTrue("width = 5",line.equals("Hello"));
    
    formatter = new LineFormatter(6,Alignment.CENTER,'0');
    line = formatter.format(value);
    assertTrue("width = 6",line.equals("Hello0"));
    
    formatter = new LineFormatter(7,Alignment.CENTER,'0');
    line = formatter.format(value);
    assertTrue("width = 7",line.equals("0Hello0"));
    
    formatter = new LineFormatter(9,Alignment.CENTER,'0');
    line = formatter.format(value);
    assertTrue("width = 9",line.equals("00Hello00"));
  }

  public void testRightJustifyZeroFill() throws Exception {
    String value = "Hello";
    String line = "";
    
    formatter = new LineFormatter(1,Alignment.RIGHT,'0');
    line = formatter.format(value);
    assertTrue("",line.equals("H"));
    
    formatter = new LineFormatter(4,Alignment.RIGHT,'0');
    line = formatter.format(value);
    assertTrue("",line.equals("Hell"));
    
    formatter = new LineFormatter(5,Alignment.RIGHT,'0');
    line = formatter.format(value);
    assertTrue("",line.equals("Hello"));
    
    formatter = new LineFormatter(6,Alignment.RIGHT,'0');
    line = formatter.format(value);
    assertTrue("",line.equals("0Hello"));
    
    formatter = new LineFormatter(10,Alignment.RIGHT,'0');
    line = formatter.format(value);
    assertTrue("",line.equals("00000Hello"));
  }
}                    

