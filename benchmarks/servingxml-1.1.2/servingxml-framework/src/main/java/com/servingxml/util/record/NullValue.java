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

import org.xml.sax.ContentHandler;

import com.servingxml.util.SystemConstants;
import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;

public class NullValue implements Value {
  private final ValueType valueType;

  public NullValue(ValueType valueType) {
    this.valueType = valueType;
  }

  public String getString() {
    return "";
  }

  public Object getObject() {
    return null;
  }

  public String[] getStringArray() {
    return SystemConstants.EMPTY_STRING_ARRAY;
  }


  /**
  * Returns the value as an array of records 
  * @deprecated since ServingXML 0.8.3: replaced by {@link 
  *             NullValue#getRecords}
  */

  @Deprecated
  public Record[] getSegments() {
    return getRecords();
  }

  /**
  * Returns the value as an array of records 
  */

  public Record[] getRecords() {
    return Record.EMPTY_ARRAY;
  }

  public ValueType getType() {
    return valueType;
  }

  public Object getSqlValue() {
    return null;
  }

  public int getSqlType() {
    return valueType.getSqlType();
  }

  public void writeToContentHandler(Name fieldName, PrefixMap prefixMap, 
                                    ContentHandler handler) {
  }

  public boolean equalsValue(Value aValue) {
    String s = aValue.getString();
    return s.equals(getString());
  }

  public int hashCode() {
    String s = getString();
    return s.hashCode();
  }
}
