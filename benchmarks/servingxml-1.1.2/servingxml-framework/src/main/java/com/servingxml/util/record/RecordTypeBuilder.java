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

import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.math.BigDecimal;

import com.servingxml.util.Name;

/**
 * A <code>RecordTypeBuilder</code> class builds a record.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordTypeBuilder {

  private final Name recordTypeName;
  private FieldType[] fieldTypes;
  private int fieldCount = 0;

  /**
   * Creates a record builder with no initial field types.
   */

  public RecordTypeBuilder(RecordType recordType) {
    this.recordTypeName = recordType.getName();
    this.fieldCount = recordType.count();
    this.fieldTypes = recordType.getFieldTypes();
  }

  /**
   * Creates a record builder with no initial field types.
   */

  public RecordTypeBuilder(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
    this.fieldTypes = new FieldType[10];
    this.fieldCount = 0;
  }

  /**
   * Creates a record builder with no initial field types.
   */

  public RecordTypeBuilder(Name recordTypeName, int capacity) {
    this.recordTypeName = recordTypeName;
    this.fieldTypes = new FieldType[capacity];
    this.fieldCount = 0;
  }

  /**
  * Sets a value for a field.
  * @param name The field name.
  */

  public void setFieldType(Name name) {

    int index = getFieldIndex(name);
    if (index == -1) {
      FieldType fieldType = new DefaultFieldType(name);
      index = addFieldType(fieldType);
    }
  }

  /**
  * Gets the number of fields
  * @return The number of fields
  */

  public int fieldCount() {
    return fieldCount;
  }

  public RecordType toRecordType() {
    FieldType[] types = new FieldType[fieldCount];
    System.arraycopy(fieldTypes, 0, types, 0, fieldCount);
    RecordType recordType = new RecordTypeImpl(recordTypeName,types);
    return recordType;
  }

  public Name getFieldName(int i) {
    return fieldTypes[i].getName();
  }

  protected int getFieldIndex(Name name) {
    int index = -1;
    for (int i = 0; index == -1 && i < fieldCount; ++i) {
      if (fieldTypes[i].getName().equals(name)) {
        index = i;
      }
    }
    return index;
  }

  /**
  * Adds a field type.
  * @param fieldType A field type.
  */

  protected int addFieldType(FieldType fieldType) {
    if (fieldCount+1 >= fieldTypes.length) {
      int capacity = fieldTypes.length < 10 ? 10 : fieldTypes.length*2;
      FieldType[] newFields = new FieldType[capacity];
      System.arraycopy(fieldTypes, 0, newFields, 0, fieldTypes.length);
      fieldTypes = newFields;
    }
    int index = fieldCount;
    fieldTypes[index] = fieldType;
    fieldCount++;

    return index;
  }

  public void clear() {
    fieldCount = 0;
  }
}



