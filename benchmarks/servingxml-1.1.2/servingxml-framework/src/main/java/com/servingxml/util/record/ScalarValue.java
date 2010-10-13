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
import org.xml.sax.ContentHandler;

import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;

public class ScalarValue implements Value {
  private final Object value;
  private final ValueType valueType;

  // Precondition:  values != null
  public ScalarValue(Object value, ValueType valueType) {
    if (value == null) {
      throw new AssertionError("value is null");
    }
    this.value = value;
    this.valueType = valueType;
  }

  public Object getObject() {
    return value;
  }

  public String getString() {
    String s = valueType.toString(value);
    return s;
  }

  public String[] getStringArray() {
    String[] sa = new String[]{valueType.toString(value)};
    return sa;
  }


  /**
  * Returns the value as an array of records 
  * @deprecated since ServingXML 0.8.3: replaced by {@link 
  *             ScalarValue#getRecords}
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
    Object o = valueType.getSqlValue(value);
    return o;
  }

  public int getSqlType() {
    return valueType.getSqlType();
  }

  public void writeToContentHandler(Name fieldName, PrefixMap  prefixMap, ContentHandler handler) 
  throws SAXException {
    String qname = fieldName.toQname(prefixMap);
    String namespaceUri = fieldName.getNamespaceUri();

    if (namespaceUri.length() > 0) {
      String prefix = prefixMap.getPrefix(namespaceUri);
      if (!prefixMap.containsPrefixMapping(prefix, namespaceUri)) {
        handler.startPrefixMapping(prefix,namespaceUri);
      }
    }
    handler.startElement(namespaceUri,fieldName.getLocalName(),qname, FieldType.EMPTY_ATTRIBUTES);
    String s = getString();
    if (s.length() > 0) {
      char[] ca = s.toCharArray();
      handler.characters(ca, 0, ca.length);
    }
    handler.endElement(namespaceUri,fieldName.getLocalName(),qname);
  }

  public String toString() {
    return getString();
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
