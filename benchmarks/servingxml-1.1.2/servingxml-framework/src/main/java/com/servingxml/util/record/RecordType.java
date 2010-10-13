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

import com.servingxml.util.Name;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

/**
 * A representation of the type of a {@link Record record}.
 */

public interface RecordType {
  /**
   * Gets the number of fields in the record.
   * @return the number of fields.
   */
  int count();
  /**
   * Gets the number of fields in the record.
   * @return the number of fields.
   */
  int fieldCount();

  /**
   * Gets the field type at the specified index.
   * @param index the index of the field type.
   * @return the type of the specified field.
   * @deprecated since ServingXML 0.6.2: use {@link RecordType#getFieldType}
   */
  @Deprecated
  FieldType getFieldTypeAt(int index);

  /**
   * Gets the field type at the specified index.
   * @param index the index of the field type.
   * @return the type of the specified field.
   */
  FieldType getFieldType(int index);

  /**
   * Gets the name of the record type.
   * @return the name of the record type.
   */

  Name getName();

  /**
   * Gets the corresponding index in a record for a field by name
   * 
   * @param name the name of the field.
   * @return the index of the field in the record
   */
  int getFieldIndex(Name name);

  FieldType[] getFieldTypes();
}
