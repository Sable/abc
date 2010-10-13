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

import java.util.Map;
import java.util.HashMap;
import javax.xml.datatype.DatatypeFactory;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.SystemConstants;

public class ValueTypeFactory {
  public static final Name DOUBLE_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"double");
  public static final Name FLOAT_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"float");
  public static final Name STRING_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"string");
  public static final Name LONG_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"long");
  public static final Name INT_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"int");
  public static final Name SHORT_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"short");
  public static final Name BYTE_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"byte");
  public static final Name DATETIME_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"dateTime");
  public static final Name DATE_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"date");
  public static final Name TIME_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"time");
  public static final Name DECIMAL_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"decimal");
  public static final Name BOOLEAN_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"boolean");
  public static final Name HEX_BINARY_TYPE_NAME = new QualifiedName(SystemConstants.SCHEMA_NS_URI,"hexBinary");

  private static DatatypeFactory datatypeFactory;
  static {
    try  {
      datatypeFactory = DatatypeFactory.newInstance();
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public static final ValueType STRING_TYPE = new StringValueType();
  public static final ValueType LONG_TYPE = new LongValueType();
  public static final ValueType INTEGER_TYPE = new IntegerValueType();
  public static final ValueType SHORT_TYPE = new ShortValueType();
  public static final ValueType BYTE_TYPE = new ByteValueType();
  public static final ValueType FLOAT_TYPE = new FloatValueType();
  public static final ValueType DOUBLE_TYPE = new DoubleValueType();
  public static final ValueType DECIMAL_TYPE = new BigDecimalValueType();
  public static final DateTimeValueType DATETIME_TYPE = new DateTimeValueType(datatypeFactory);
  public static final DateValueType DATE_TYPE = new DateValueType(datatypeFactory);
  public static final TimeValueType TIME_TYPE = new TimeValueType(datatypeFactory);
  public static final ValueType BOOLEAN_TYPE = new BooleanValueType();
  public static final ValueType HEX_BINARY_TYPE = new HexBinaryValueType();

  static private final Map<Name,ValueType> valueTypeMap = new HashMap<Name,ValueType>();
  static {
    createValueTypeMap();
  }

  private static void createValueTypeMap() {

    valueTypeMap.put(STRING_TYPE_NAME,STRING_TYPE);
    valueTypeMap.put(INT_TYPE_NAME,INTEGER_TYPE);
    valueTypeMap.put(INT_TYPE_NAME,SHORT_TYPE);
    valueTypeMap.put(INT_TYPE_NAME,BYTE_TYPE);
    valueTypeMap.put(LONG_TYPE_NAME,LONG_TYPE);
    valueTypeMap.put(FLOAT_TYPE_NAME,DATE_TYPE);
    valueTypeMap.put(DOUBLE_TYPE_NAME,DOUBLE_TYPE);
    valueTypeMap.put(DECIMAL_TYPE_NAME,DECIMAL_TYPE);
    valueTypeMap.put(DATETIME_TYPE_NAME,DATETIME_TYPE);
    valueTypeMap.put(DATE_TYPE_NAME,DATE_TYPE);
    valueTypeMap.put(TIME_TYPE_NAME,TIME_TYPE);
    valueTypeMap.put(BOOLEAN_TYPE_NAME,BOOLEAN_TYPE);
    valueTypeMap.put(HEX_BINARY_TYPE_NAME,HEX_BINARY_TYPE);
  }

  public static ValueType lookupValueType(Name typeName) {
    ValueType valueType = valueTypeMap.get(typeName);
    return valueType;
  }
                                         
}
