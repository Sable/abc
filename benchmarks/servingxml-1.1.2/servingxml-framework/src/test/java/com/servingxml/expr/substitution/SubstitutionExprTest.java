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

package com.servingxml.expr.substitution;

import junit.framework.TestCase;

import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.QnameContext;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.util.QualifiedName;

public class SubstitutionExprTest extends TestCase {
  private static final String MY_LITERAL_VALUE = "literal";
  private static final String MY_PARAM_VALUE = "param";
  private static final String MY_PARAM_VALUE1 = "param1";
  private static final String MY_PARAM_VALUE2 = "param2";
  private static final String MY_FIELD_VALUE = "field";
  private static final String MY_FIELD_VALUE1 = "field1";
  private static final String MY_FIELD_VALUE2 = "field2";
  
  private Record parameters;
  private Record mvParameters;
  private Record record;
  private Record mvFields;

  public SubstitutionExprTest(String name) {
    super(name);
  }
    
  protected void setUp() throws Exception {
    MutableNameTable nameTable = new NameTableImpl();
    
    
    RecordBuilder parametersBuilder = new RecordBuilder(new QualifiedName("myParams"));
    RecordBuilder mvParametersBuilder = new RecordBuilder(new QualifiedName("myMvParams"));
    RecordBuilder recordBuilder = new RecordBuilder(new QualifiedName("myFields"));
    RecordBuilder mvFieldsBuilder = new RecordBuilder(new QualifiedName("myMvFields"));
    
    parametersBuilder.setString(new QualifiedName("myParam"),MY_PARAM_VALUE);

    recordBuilder.setString(new QualifiedName("myField"),MY_FIELD_VALUE);
    
    String[] paramValues = new String[]{MY_PARAM_VALUE1,MY_PARAM_VALUE2};
    mvParametersBuilder.setStringArray(new QualifiedName("myMvParam"),paramValues);
    
    String[] fieldValues = new String[]{MY_FIELD_VALUE1,MY_FIELD_VALUE2};
    mvFieldsBuilder.setStringArray(new QualifiedName("myMvField"),fieldValues);
    
    mvParameters = mvParametersBuilder.toRecord();
    parameters = parametersBuilder.toRecord();
    record = recordBuilder.toRecord();
    mvFields = mvFieldsBuilder.toRecord();
  }
  
  public void testLiteral() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,MY_LITERAL_VALUE);
    String value = subExpr.evaluateAsString(parameters,record);
    String expected = MY_LITERAL_VALUE;
    assertTrue(value + " = " + expected,value.equals(expected));
  }
  
  public void testEmptyValue() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"");
    String value = subExpr.evaluateAsString(parameters,record);
    String expected = "";
    assertTrue(value + " = " + expected,value.equals(expected));
  }
  
  public void testParameter() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"{$myParam}");
    String value = subExpr.evaluateAsString(parameters,record);
    String expected = MY_PARAM_VALUE;
    assertTrue(value + " = " + expected,value.equals(expected));
  }
  
  public void testField() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"{myField}");
    String value = subExpr.evaluateAsString(parameters,record);
    String expected = MY_FIELD_VALUE;
    assertTrue(value + " = " + expected,value.equals(expected));
  }
/*  
  public void testUnresolvedField() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"{hisField}");
    boolean threwException = false;
    try {
      String value = subExpr.evaluateAsString(parameters,record);
    } catch (Exception e) {
      threwException = true;
    }
    if (!threwException) {
      assertTrue("No exception",false);
    }
  }
  
  public void testUnresolvedParameter() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"{$hisParameter}");
    boolean threwException = false;
    try {
      String value = subExpr.evaluateAsString(parameters,record);
    } catch (Exception e) {
      threwException = true;
    }
    if (!threwException) {
      assertTrue("No exception",false);
    }
  }
*/  
  public void testLiteralAndParameterAndField() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"literal{$myParam}{myField}");
    String value = subExpr.evaluateAsString(parameters,record);
    String expected = MY_LITERAL_VALUE+MY_PARAM_VALUE+MY_FIELD_VALUE;
    assertTrue(value + " = " + expected,value.equals(expected));
  }
  
  public void testMvParameters() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"{$myMvParam}");
    String[] values = subExpr.evaluateAsStringArray(mvParameters,record);
    assertTrue(""+values.length + " = 2", values.length == 2); 
    
    String[] expected = {MY_PARAM_VALUE1,MY_PARAM_VALUE2};
    assertTrue(values[0] + " = " + expected[0],values[0].equals(expected[0]));
    assertTrue(values[1] + " = " + expected[1],values[1].equals(expected[1]));
  }
  
  public void testMvFields() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"{myMvField}");
    String[] values = subExpr.evaluateAsStringArray(mvParameters,mvFields);
    assertTrue(""+values.length + " = 2", values.length == 2); 
    
    String[] expected = {MY_FIELD_VALUE1,MY_FIELD_VALUE2};
    assertTrue(values[0] + " = " + expected[0],values[0].equals(expected[0]));
    assertTrue(values[1] + " = " + expected[1],values[1].equals(expected[1]));
  }
  
  public void testEmptyValues() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"");
    String[] values = subExpr.evaluateAsStringArray(mvParameters,mvFields);
    assertTrue(""+values.length + " = 1", values.length == 1); 
    assertTrue(""+values[0].length() + " = 0", values[0].length() == 0); 
  }
  
  public void testSingleSpaceValues() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context," ");
    String[] values = subExpr.evaluateAsStringArray(mvParameters,mvFields);
    assertTrue(""+values.length + " = 1", values.length == 1); 
    
    String[] expected = new String[]{" "};
    assertTrue(values[0] + " = " + expected[0],values[0].equals(expected[0]));
  }

  //revisit - unexpected
  public void testUnresolvedParameters() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"{$hisMvParameters}");
    boolean threwException = false;
    try {
      String[] values = subExpr.evaluateAsStringArray(mvParameters,mvFields);
    } catch (Exception e) {
      threwException = true;
    }
    //if (!threwException) {
    //  assertTrue("No exception",false);
    //}
  }
/*  
  public void testUnresolvedFields() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"{hisMvField}");
    boolean threwException = false;
    try {
      String[] values = subExpr.evaluateAsStringArray(mvParameters,mvFields);
    } catch (Exception e) {
      threwException = true;
    }
    if (!threwException) {
      assertTrue("No exception",false);
    }
  }
*/  
  public void testMvParametersAndLiteral() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"{$myMvParam}s");
    String[] values = subExpr.evaluateAsStringArray(mvParameters,record);
    assertTrue(""+values.length + " = 2", values.length == 2); 
    
    String[] expected = {MY_PARAM_VALUE1+"s",MY_PARAM_VALUE2+"s"};
    assertTrue(values[0] + " = " + expected[0],values[0].equals(expected[0]));
    assertTrue(values[1] + " = " + expected[1],values[1].equals(expected[1]));
  }
/*  
  public void testToString() throws Exception {
    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"{$myMvParam}s");
    String[] values = subExpr.evaluateAsStringArray(mvParameters,record);
    assertTrue(""+values.length + " = 2", values.length == 2); 
    
    String[] expected = {MY_PARAM_VALUE1+"s",MY_PARAM_VALUE2+"s"};
    assertTrue(values[0] + " = " + expected[0],values[0].equals(expected[0]));
    assertTrue(values[1] + " = " + expected[1],values[1].equals(expected[1]));
    
    SubstitutionExpr svr = new ToString(subExpr,",","'");
    String[] a = svr.evaluateAsStringArray(mvParameters,record);
    assertTrue("string of length 1",a.length == 1);
    assertTrue(a[0],a[0].equals("'param1s','param2s'"));
  }
*/  
  public void testQuotedParameterValue() throws Exception {
    RecordBuilder parametersBuilder = new RecordBuilder(new QualifiedName("myParams"));
    parametersBuilder.setString(new QualifiedName("paramWithQuote"),"It's");
    Record parameters = parametersBuilder.toRecord();

    RecordBuilder recordBuilder = new RecordBuilder(new QualifiedName("myFields"));
    recordBuilder.setString(new QualifiedName("field1"),"xxx'");
    recordBuilder.setString(new QualifiedName("field2"),"'yyy");
    recordBuilder.setString(new QualifiedName("field3"),"zz'z");
    Record record = recordBuilder.toRecord();

    EscapeSubstitutionVariables escapeVariables = new DoEscapeSubstitutionVariables('\'', "''");

    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"'{$paramWithQuote}'", escapeVariables);
    String value = subExpr.evaluateAsString(parameters,record);
    assertTrue(value, value.equals("'It''s'")); 
  }

  public void testQuotedValues() throws Exception {
    RecordBuilder parametersBuilder = new RecordBuilder(new QualifiedName("myParams"));
    parametersBuilder.setString(new QualifiedName("param1"),"aaa'");
    parametersBuilder.setString(new QualifiedName("param2"),"'bbb");
    parametersBuilder.setString(new QualifiedName("param3"),"cc'c");
    Record parameters = parametersBuilder.toRecord();

    RecordBuilder recordBuilder = new RecordBuilder(new QualifiedName("myFields"));
    recordBuilder.setString(new QualifiedName("field1"),"xxx'");
    recordBuilder.setString(new QualifiedName("field2"),"'yyy");
    recordBuilder.setString(new QualifiedName("field3"),"zz'z");
    Record record = recordBuilder.toRecord();

    EscapeSubstitutionVariables escapeVariables = new DoEscapeSubstitutionVariables('\'', "''");

    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"'{$param1} {$param2} {$param3}' {field1}", escapeVariables);
    String value = subExpr.evaluateAsString(parameters,record);
    assertTrue(value, value.equals("'aaa'' ''bbb cc''c' xxx'")); 
  }

  public void testQuotedValues2() throws Exception {
    RecordBuilder parametersBuilder = new RecordBuilder(new QualifiedName("myParams"));
    parametersBuilder.setString(new QualifiedName("param1"),"aaa'");
    parametersBuilder.setString(new QualifiedName("param2"),"'bbb");
    parametersBuilder.setString(new QualifiedName("param3"),"cc'c");
    Record parameters = parametersBuilder.toRecord();

    RecordBuilder recordBuilder = new RecordBuilder(new QualifiedName("myFields"));
    recordBuilder.setString(new QualifiedName("field1"),"xxx'");
    recordBuilder.setString(new QualifiedName("field2"),"'yyy");
    recordBuilder.setString(new QualifiedName("field3"),"zz'z");
    Record record = recordBuilder.toRecord();

    EscapeSubstitutionVariables escapeVariables = new DoEscapeSubstitutionVariables('\'', "''");

    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"'It''s {$param1} {$param2} {$param3}' {field1}", escapeVariables);
    String value = subExpr.evaluateAsString(parameters,record);
    assertTrue(value, value.equals("'It''s aaa'' ''bbb cc''c' xxx'")); 
  }

  public void testQuotedValues3() throws Exception {
    RecordBuilder parametersBuilder = new RecordBuilder(new QualifiedName("myParams"));
    parametersBuilder.setString(new QualifiedName("param1"),"aaa'");
    parametersBuilder.setString(new QualifiedName("param2"),"'bbb");
    parametersBuilder.setString(new QualifiedName("param3"),"cc'c");
    Record parameters = parametersBuilder.toRecord();

    RecordBuilder recordBuilder = new RecordBuilder(new QualifiedName("myFields"));
    recordBuilder.setString(new QualifiedName("field1"),"xxx'");
    recordBuilder.setString(new QualifiedName("field2"),"'yyy");
    recordBuilder.setString(new QualifiedName("field3"),"zz'z");
    Record record = recordBuilder.toRecord();

    EscapeSubstitutionVariables escapeVariables = new DoEscapeSubstitutionVariables('\'', "&quot;");

    QnameContext context = new SimpleQnameContext();
    SubstitutionExpr subExpr = SubstitutionExpr.parseString(context,"'It&quot;s {$param1} {$param2} {$param3}' {field1}", escapeVariables);
    String value = subExpr.evaluateAsString(parameters,record);
    assertTrue(value, value.equals("'It&quot;s aaa&quot; &quot;bbb cc&quot;c' xxx'")); 
  }
}
