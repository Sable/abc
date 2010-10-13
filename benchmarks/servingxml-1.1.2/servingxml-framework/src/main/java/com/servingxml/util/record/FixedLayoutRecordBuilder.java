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
import com.servingxml.util.ServingXmlException;

/**
 * A <code>FixedLayoutRecordBuilder</code> class builds a record having a fixed number of fields.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FixedLayoutRecordBuilder {

  private static final String[] EMPTY_ROW = new String[0];

  private final Name recordTypeName;
  private final FieldType[] fieldTypes;
  private String[][] data;
  private int[] valueCount;

  /**
   * Creates an empty record object.
   */

  public FixedLayoutRecordBuilder(RecordType recordType) {
    this.recordTypeName = recordType.getName();
    
    this.fieldTypes = recordType.getFieldTypes();
    this.data = new String[fieldTypes.length][];
    this.valueCount = new int[fieldTypes.length];
    clear();
  }

  /**
  * Adds a value for a field at an index.
  * @param index The index
  * @param value The value.
  */

  protected void addValue(int index, String value) {
    String[] row = data[index];
    int size = valueCount[index];
    if (size+1 >= row.length) {
      int capacity = row.length < 2 ? 10 : row.length*2;
      String[] newValues = new String[capacity];
      System.arraycopy(row, 0, newValues, 0, row.length);
      row = newValues;
    }
    row[size] = value;
    valueCount[index] += 1;
    data[index] = row;                    
  }

  public void appendValue(Name name, String value) {

    if (value != null) {
      int index = getFieldIndex(name);
      if (index == -1) {
        throw new ServingXmlException("Field not found.");
      }

      FieldType fieldType = fieldTypes[index];
      addValue(index,value);
    }
  }

  /**
  * Gets the number of record
  * @return The number of record
  */

  public int count() {
    return fieldTypes.length;
  }

  public FieldType[] getFieldTypes() {
    return fieldTypes;
  }
  
  public Record toRecord() {
    RecordType recordType = new RecordTypeImpl(recordTypeName,fieldTypes);

    Value[] values = new Value[fieldTypes.length];

    for (int i = 0; i < fieldTypes.length; ++i) {
      if (data[i].length != valueCount[i]) {
        String[] newValues = new String[valueCount[i]];
        System.arraycopy(data[i], 0, newValues, 0, valueCount[i]);
        values[i] = new ArrayValue(newValues, ValueTypeFactory.STRING_TYPE);
      } else {
        values[i] = new ArrayValue(data[i], ValueTypeFactory.STRING_TYPE);
      }
    }

    Record record = new RecordImpl(recordType,values);
    return record;
  }
  
  protected int getFieldIndex(Name name) {
    int index = -1;
    for (int i = 0; index == -1 && i < fieldTypes.length; ++i) {
      if (fieldTypes[i].getName().equals(name)) {
        index = i;
      }
    }
    return index;
  }

  public void clear() {
    for (int i = 0; i < fieldTypes.length; ++i) {
      data[i] = EMPTY_ROW;
      valueCount[i] = 0;
    }
  }
}



