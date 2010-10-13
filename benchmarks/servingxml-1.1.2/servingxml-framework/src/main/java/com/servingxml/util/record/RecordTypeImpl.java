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

import java.io.PrintStream;

import com.servingxml.util.Name;

/**
 * A <code>RecordTypeImpl</code> class implements a RecordType.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordTypeImpl implements RecordType {

  private final Name recordTypeName;
  private final FieldType[] fieldTypes;

  /**
   * Creates an empty recordType object.
   */

  public RecordTypeImpl(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
    this.fieldTypes = new FieldType[0];
  }

  /**
   * Creates a recordType object.
   */

  public RecordTypeImpl(Name recordTypeName, FieldType[] fieldTypes) {
    this.recordTypeName = recordTypeName;
    this.fieldTypes = fieldTypes;
  }
  
  public FieldType[] getFieldTypes() {
    return fieldTypes;
  }
  
  public Name getName() {
    return recordTypeName;
  }

  public int getFieldIndex(Name name) {
    int index = -1;
    for (int i = 0; index == -1 && i < fieldTypes.length; ++i) {
      if (fieldTypes[i].getName().equals(name)) {
        index = i;
      }
    }
    return index;
  }

  public FieldType getFieldType(int index) {
    return fieldTypes[index];
  }

  /**
   * Gets the field type at the specified index.
   * @param index the index of the field type.
   * @return the type of the specified field.
   * @deprecated since ServingXML 0.6.2: use {@link RecordType#getFieldType}
   */

  @Deprecated
  public FieldType getFieldTypeAt(int index) {
    return fieldTypes[index];
  }

  /**
  * Gets the number of recordType
  * @return The number of recordType
  */

  public int count() {
    return fieldTypes.length;
  }

  /**
  * Gets the number of recordType
  * @return The number of recordType
  */

  public int fieldCount() {
    return fieldTypes.length;
  }

  /**
  * Returns a string representation of the recordType, primarily for debugging purposes.
  *
  * @return A string representation of the recordType.
  */

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(recordTypeName + "  ");
    for (int i = 0; i < fieldTypes.length; ++i) {
      FieldType fieldType = fieldTypes[i];
      if (i > 0) {
        buf.append(",");
      }

      buf.append(fieldType.getName());   
    }
    return buf.toString();
  }

  public void printDiagnostics(PrintStream printStream) {
    printStream.println("record");

    for (int i = 0; i < fieldTypes.length; ++i) {
      FieldType fieldType = fieldTypes[i];
      printStream.println("FieldType:  name = " + fieldType.getName()); 
    }
  }

  public boolean equals(Object o) {
    boolean isEqual = true;
    if (o != this) {
      RecordType rhs = (RecordType)o;
      if (!recordTypeName.equals(rhs.getName())) {
        isEqual = false;
      }
    }
    return isEqual;
  }
}



