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
import java.util.List;
import java.util.ArrayList;

import com.servingxml.util.Name;

/**
 * A <code>MultivaluedFieldBuilder</code> class builds a record.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MultivaluedFieldBuilder {

  private FieldEntry[] fieldEntries;
  private int fieldCount = 0;

  /**
   * Creates a record builder with no initial field types.
   */

  public MultivaluedFieldBuilder() {
    this.fieldEntries = new FieldEntry[10];
    this.fieldCount = 0;
  }

  /**
  * Sets a value for a field.
  * @param name The field name.
  * @param value The field value.
  */

  public void addString(Name name, String value) {

    int index = getFieldIndex(name);
    if (index == -1) {
      FieldEntry fieldEntry = new FieldEntry(name);
      index = addFieldEntry(fieldEntry);
      fieldEntry.valueList.add(value);
    } else {
      FieldEntry fieldEntry = fieldEntries[index];
      fieldEntry.valueList.add(value);
    }
  }

  /**
  * Sets a value for a field.
  * @param name The field name.
  * @param record The segment value.
  */

  public void addSegment(Name name, Record record) {
   //System.out.println(getClass().getName()+".addSegment recordType="+record.getRecordType().getName() + ", fieldCount="+record.fieldCount());

    int index = getFieldIndex(name);
    if (index == -1) {
      FieldEntry fieldEntry = new FieldEntry(name);
      index = addFieldEntry(fieldEntry);
      fieldEntry.recordList.add(record);
    } else {
      FieldEntry fieldEntry = fieldEntries[index];
      fieldEntry.recordList.add(record);
    }
  }

  public void updateRecord(RecordBuilder recordBuilder) {
    for (int i = 0; i < fieldCount; ++i) {
      FieldEntry entry = fieldEntries[i];

      Name fieldName = entry.fieldName;
      List<String> valueList = entry.valueList;
      List<Record> recordList = entry.recordList;

      if (recordList.size() == 0) {
        if (valueList.size() == 0) {
          recordBuilder.setString(fieldName,null);
        } else if (valueList.size() == 1) {
          recordBuilder.setString(fieldName,valueList.get(0));
        } else {
          String[] sa = new String[valueList.size()]; 
          sa = (String[])valueList.toArray(sa);
          recordBuilder.setStringArray(fieldName,sa);
        }
      } else {
        Record[] segments = new Record[recordList.size()];
        segments = (Record[])recordList.toArray(segments);
        recordBuilder.setRecords(fieldName,segments);
      }
    }
  }

  protected int getFieldIndex(Name name) {
    int index = -1;
    for (int i = 0; index == -1 && i < fieldCount; ++i) {
      if (fieldEntries[i].fieldName.equals(name)) {
        index = i;
      }
    }
    return index;
  }

  /**
  * Adds a field type.
  * @param fieldEntry A field type.
  */

  protected int addFieldEntry(FieldEntry fieldEntry) {
    if (fieldCount+1 >= fieldEntries.length) {
      int capacity = fieldEntries.length < 10 ? 10 : fieldEntries.length*2;
      FieldEntry[] newFields = new FieldEntry[capacity];
      System.arraycopy(fieldEntries, 0, newFields, 0, fieldEntries.length);
      fieldEntries = newFields;
    }
    int index = fieldCount;
    fieldEntries[index] = fieldEntry;
    fieldCount++;

    return index;
  }

  public void clear() {
    fieldCount = 0;
  }

  static class FieldEntry {
    final Name fieldName;
    final List<String> valueList;
    List<Record> recordList;

    FieldEntry(Name fieldName) {
      this.fieldName = fieldName;
      valueList = new ArrayList<String>();
      recordList = new ArrayList<Record>();
    }
  }
}



