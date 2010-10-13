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

import java.sql.Types;

import org.xml.sax.ContentHandler;

import com.servingxml.util.SystemConstants;
import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;

public class EmptyFieldValue implements Value {

  public String getString() {
    return "";
  }

  public String[] getStringArray() {
    return SystemConstants.EMPTY_STRING_ARRAY;
  }

  public Object getObject() {
    return null;
  }

  /**
  * Returns the value as an array of records 
  * @deprecated since ServingXML 0.8.3: replaced by {@link 
  *             EmptyFieldValue#getRecords}
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
    return ValueTypeFactory.STRING_TYPE;
  }

  public Object getSqlValue() {
    return null;
  }

  public int getSqlType() {
    return Types.VARCHAR;
  }

  public final void writeToContentHandler(Name fieldName, PrefixMap prefixMap, 
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
