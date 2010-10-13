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

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ContentHandler;

import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;

public interface Value {
  public static final Value EMPTY = new EmptyFieldValue();
  public static final Object[] EMPTY_VALUE_ARRAY = new Object[0];
  public static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();

  String getString(); 

  Object getObject();

  String[] getStringArray();

  /**
  * Returns the value as an array of records 
  * @deprecated since ServingXML 0.8.3: replaced by {@link 
  *             Value#getRecords}
  */

  @Deprecated
  Record[] getSegments();

  /**
  * Returns the value as an array of records 
  */

  Record[] getRecords();

  ValueType getType();

  Object getSqlValue();

  int getSqlType();

  void writeToContentHandler(Name fieldName, PrefixMap  prefixMap, ContentHandler handler) 
  throws SAXException;

  boolean equalsValue(Value value);
}
