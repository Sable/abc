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

package com.servingxml.app;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordType;
import com.servingxml.util.record.Value;
import com.servingxml.util.PrefixMap;

/**
 * A <code>Record</code> class represents a set of fields.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ServingXmlParameters implements Record {
  private final Record tail;

  public ServingXmlParameters(Record tail) {
    this.tail = tail;
  }

  public RecordType getRecordType() {
    return tail.getRecordType();
  }

  public int fieldCount() {
    return tail.fieldCount();
  }

  public Name getFieldName(int i) {
    return tail.getFieldName(i);
  }

  /**
  * Gets the value of a field as a string.
  * @param name The field name.
  * @return The field value as a string.
  */

  public String getString(Name name) {
    return tail.getString(name);
  }

  /**
  * Gets the value of a field as a string array.
  * @param name The field name.
  * @return The field value as a string array.
  */

  public String[] getStringArray(Name name) {
    return tail.getStringArray(name);
  }

  /**
  * Gets the value of a field with the specified name.
  * @param name The field name.
  * @return The field value
  */
  
  public Value getValue(Name name) {
    return tail.getValue(name);
  }

  /**
  * Gets the value of a field at the specified index.
  * @param index the index of the field.
  * @return The field value
  */

  public Value getValue(int index) {
    return tail.getValue(index);
  }

  /**
  * Gets an <code>XMLReader</code> representation of the record.
  * @return An <code>XMLReader</code> representation of the record.
  */
  
  public XMLReader createXmlReader(PrefixMap prefixMap) {
    return tail.createXmlReader(prefixMap);
  }

  /**
  * Gets the value of a field as a Java object.
  * @param name The field name.
  * @return The field value
  */

  public Object getObject(Name name) {
    return tail.getObject(name);
  }

  public String toXmlString(PrefixMap prefixMap) {
    return tail.toXmlString(prefixMap);
  }

  public void writeToContentHandler(PrefixMap prefixMap, ContentHandler handler) 
  throws SAXException {
    tail.writeToContentHandler(prefixMap, handler);
  }
}

