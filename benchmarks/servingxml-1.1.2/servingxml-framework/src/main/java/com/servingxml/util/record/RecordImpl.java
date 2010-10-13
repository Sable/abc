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
import com.servingxml.util.PrefixMapImpl;

/**
 * A <code>RecordImpl</code> class represents a set of record.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                                          
public class RecordImpl extends AbstractRecord implements Record {

  private final RecordType recordType;
  private Value[] values;

  /**
   * Creates an empty record object.
   */

  public RecordImpl(RecordType recordType, Value[] values) {
    this.recordType = recordType;
    this.values = values;
  }
  
  /**
   * Creates an empty record object.
   */

  public RecordImpl(Name recordTypeName) {
    this.recordType = new RecordTypeImpl(recordTypeName);
    this.values = new Value[0];
  }
  
  public RecordType getRecordType() {
    return recordType;
  }

  public int fieldCount() {
    return recordType.count();
  }

  public Name getFieldName(int i) {
    return recordType.getFieldType(i).getName();
  }

  public Value getValue(Name name) {
    int index = recordType.getFieldIndex(name);
    return index == -1 ? null : values[index];
  }

  public Value getValue(int index) {
    return values[index];
  }

  public String[] getStringArray(Name name) {
    String[] sa = null;

    int index = recordType.getFieldIndex(name);
    if (index != -1) {
      Value value = values[index];
      sa = value.getStringArray();
    }
    return sa;
  }

  public String getString(Name name) {
    String s = null;
    int index = recordType.getFieldIndex(name);
    if (index != -1) {
      Value value = values[index];
      s = value.getString();
    }
    return s;
  }

  public Object getObject(Name name) {
    Object o = null;
    int index = recordType.getFieldIndex(name);
    if (index != -1) {
      Value value = values[index];
      o = value.getObject();
    }
    return o;
  }

  public void writeToContentHandler(PrefixMap prefixMap, ContentHandler handler) 
  throws SAXException {
    //System.out.println(getClass().getName()+".writeToContentHandler start");
    Name recordTypeName = recordType.getName();
    String qname = recordTypeName.toQname(prefixMap);
    String namespaceUri = recordTypeName.getNamespaceUri();
    if (namespaceUri.length() > 0) {
      String prefix = prefixMap.getPrefix(namespaceUri);
      if (!prefixMap.containsPrefixMapping(prefix,namespaceUri)) {
        PrefixMapImpl newPrefixMap = new PrefixMapImpl(prefixMap);
        newPrefixMap.setPrefixMapping(prefix,namespaceUri);
        prefixMap = newPrefixMap;
        handler.startPrefixMapping(prefix,namespaceUri);
      }
    }

    handler.startElement(recordTypeName.getNamespaceUri(),recordTypeName.getLocalName(),qname, FieldType.EMPTY_ATTRIBUTES);
    for (int i = 0; i < recordType.count(); ++i) {
      FieldType fieldType = recordType.getFieldType(i);
      Value value = values[i];
      //System.out.println(value.getClass().getName());
      value.writeToContentHandler(fieldType.getName(), prefixMap, handler);
    }
    handler.endElement(recordTypeName.getNamespaceUri(),recordTypeName.getLocalName(),qname);
  }
}

                                               

