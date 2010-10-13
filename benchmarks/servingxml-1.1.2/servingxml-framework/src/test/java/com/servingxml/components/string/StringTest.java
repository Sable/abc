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

package com.servingxml.components.string;

import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.util.QnameContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.record.ParameterBuilder;
import com.servingxml.util.record.Record;

import junit.framework.TestCase;

public class StringTest extends TestCase {
  
  public StringTest(String name) {
    super(name);
  }
    
  protected void setUp() throws Exception {
  }

  public void testString1() {
    QnameContext context = new SimpleQnameContext();
    String input = " ";
    SubstitutionExpr expr = SubstitutionExpr.parseString(context,input);

    String expected = input;
    String s = expr.evaluateAsString(Record.EMPTY,Record.EMPTY);
    assertTrue(s+"="+expected, s.equals(expected));
  }

  public void testString2() {
    QnameContext context = new SimpleQnameContext();
    String input = " {$city} ";
    SubstitutionExpr expr = SubstitutionExpr.parseString(context,input);

    String expected = " Toronto ";
    ParameterBuilder parameterBuilder = new ParameterBuilder();
    parameterBuilder.setString(new QualifiedName("city"),"Toronto");
    Record parameters = parameterBuilder.toRecord();

    String s = expr.evaluateAsString(parameters,Record.EMPTY);
    assertTrue(s+"="+expected, s.equals(expected));
  }

  public void testString3() {
    QnameContext context = new SimpleQnameContext();
    String input = "{$city}";
    SubstitutionExpr expr = SubstitutionExpr.parseString(context,input);

    String expected = " Toronto ";
    ParameterBuilder parameterBuilder = new ParameterBuilder();
    parameterBuilder.setString(new QualifiedName("city")," Toronto ");
    Record parameters = parameterBuilder.toRecord();

    String s = expr.evaluateAsString(parameters,Record.EMPTY);
    assertTrue(s+"="+expected, s.equals(expected));
  }
}
