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

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

import com.servingxml.util.Name;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.PrefixMap;

/**
 * A <code>Record</code> class represents a set of fields.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface Record {

  static final Name EMPTY_RECORD_TYPE_NAME = new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"default");
  static final Record EMPTY = new RecordImpl(EMPTY_RECORD_TYPE_NAME);
  static final Record[] EMPTY_ARRAY = new Record[0];
  
  RecordType getRecordType();

  int fieldCount();

  Name getFieldName(int i);

  /**
  * Gets the value of a field as a string.
  * @param name The field name.
  * @return The field value as a string.
  */

  String getString(Name name);

  /**
  * Gets the value of a field as a string array.
  * @param name The field name.
  * @return The field value as a string array.
  */

  String[] getStringArray(Name name);

  /**
  * Gets the value of a field with the specified name.
  * @param name The field name.
  * @return The field value
  */
  
  Value getValue(Name name);

  /**
  * Gets the value of a field at the specified index.
  * @param index the index of the field.
  * @return The field value
  */

  Value getValue(int index);

  /**
  * Gets an <code>XMLReader</code> representation of the record.
  * @return An <code>XMLReader</code> representation of the record.
  */
  
  XMLReader createXmlReader(PrefixMap prefixMap);

  /**
  * Gets the value of a field as a Java object.
  * @param name The field name.
  * @return The field value
  */

  Object getObject(Name name);

  String toXmlString(PrefixMap prefixMap);

  void writeToContentHandler(PrefixMap prefixMap, ContentHandler handler) 
  throws SAXException;
}

