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
import org.xml.sax.SAXException;

import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.SystemConstants;

public class ArrayValue implements Value {
  private final Object[] values;
  private final ValueType valueType;

  // Precondition:  values != null
  public ArrayValue(Object[] values, ValueType valueType) {
    if (values == null) {
      throw new AssertionError("values is null");
    }
    this.values = values;
    this.valueType = valueType;
  }

  public Object getObject() {
    return values;
  }

  public String getString() {
    String s = "";
    if (values.length > 0) {
      s = valueType.toString(values[0]);
    }
    return s;
  }

  public String[] getStringArray() {
    String[] sa = SystemConstants.EMPTY_STRING_ARRAY;
    if (values instanceof String[]) {
      sa = (String[])values;
    } else {
      sa = new String[values.length];
      for (int i = 0; i < values.length; ++i) {
        sa[i] = valueType.toString(values[i]);
      }
    }

    return sa;
  }

  /**
  * Returns the value as an array of records 
  * @deprecated since ServingXML 0.8.3: replaced by {@link 
  *             ArrayValue#getRecords}
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
    Object o = null;
    if (values.length == 1) {
      o = valueType.getSqlValue(values[0]);
    }
    return o;
  }

  public int getSqlType() {
    return valueType.getSqlType();
  }

  public void writeToContentHandler(Name fieldName, PrefixMap prefixMap, 
                                    ContentHandler handler) 
  throws SAXException {
    //System.out.println(getClass().getName()+".writeToContentHandler start");
    String[] sa = getStringArray();
    if (sa.length > 0) {
      String qname = fieldName.toQname(prefixMap);
      String namespaceUri = fieldName.getNamespaceUri();
      if (namespaceUri.length() > 0) {
        String prefix = prefixMap.getPrefix(namespaceUri);
        if (!prefixMap.containsPrefixMapping(prefix, namespaceUri)) {
          handler.startPrefixMapping(prefix,namespaceUri);
        }
      }
      for (int j = 0; j < sa.length; ++j) {
        handler.startElement(namespaceUri, fieldName.getLocalName(), qname, 
                             FieldType.EMPTY_ATTRIBUTES);
        String s = sa[j];
        if (s.length() > 0) {
          char[] ca = s.toCharArray();
          handler.characters(ca, 0, ca.length);
        }
        handler.endElement(namespaceUri,fieldName.getLocalName(),qname);
      }
    }
    //System.out.println(getClass().getName()+".writeToContentHandler end");
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
