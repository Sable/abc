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

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;

/**
 * A <code>RecordBuilder</code> class builds instances of {@link
 * com.servingxml.util.record.Record}.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordBuilder extends AbstractRecord implements Record {

  private final Name recordTypeName;
  private FieldType[] fieldTypes;
  private int fieldCount = 0;
  private Value[] values;

  /**
   * Creates a record builder with no initial field types.
   */

  public RecordBuilder(RecordType recordType) {
    this.recordTypeName = recordType.getName();
    this.fieldCount = recordType.count();

    if (fieldCount > 0) {
      this.fieldTypes = recordType.getFieldTypes();
      this.values = new Value[fieldTypes.length];
    } else {
      this.fieldTypes = new FieldType[10];
      this.values = new Value[10];
    }
  }

  /**
   * Creates a record builder with no initial field types.
   */

  public RecordBuilder(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
    this.fieldTypes = new FieldType[10];
    this.values = new Value[10];
  }

  /**
   * Creates a record builder with no initial field types.
   */

  public RecordBuilder(Name recordTypeName, int capacity) {
    this.recordTypeName = recordTypeName;
    this.fieldTypes = new FieldType[capacity];
    this.values = new Value[capacity];
  }

  /**
   * Creates a record builder with the same record type and fields as an existing record.
   */

  public RecordBuilder(Record record) {

    this(record.getRecordType().getName(),record);
  }

  /**
   * Creates a record builder with the same fields as an exisiting record, but a different record type.
   */

  public RecordBuilder(Name recordTypeName, Record record) {

    RecordType recordType = record.getRecordType();

    this.recordTypeName = recordTypeName;
    int inititialCapacity = recordType.count() >= 10 ? recordType.count() : 10;
    this.values = new Value[inititialCapacity];
    this.fieldTypes = new FieldType[inititialCapacity];

    for (int i = 0; i < recordType.count(); ++i) {
      FieldType fieldType = recordType.getFieldType(i);
      Value value = record.getValue(i);
      fieldTypes[i] = fieldType;
      values[i] = value;
    }
    this.fieldCount = recordType.count();
  }

  /**
  * Sets a value for a field at an index.
  * @param index The index
  * @param value The value.
  */

  public void setValue(int index, Value value) {
    if (index+1 >= values.length) {
      int capacity = values.length < 10 ? 10 : values.length*2;
      Value[] newData = new Value[capacity];
      System.arraycopy(values, 0, newData, 0, values.length);
      values = newData;
    }
    values[index] = value;
  }

  /**
  * Sets a value for a field.
  * @param name The field name.
  * @param value The field value.
  * @deprecated since ServingXML 0.6.4: use {@link RecordBuilder#setValue}
  */

  @Deprecated
  public void setField(Name name, Value value) {
    setValue(name,value);
  }

  /**
  * Sets a string value for a field.
  * @param name The field name.
  * @param s The string value.
  * @deprecated since ServingXML 0.6.1: use {@link RecordBuilder#setString}
  */

  @Deprecated
  public void setField(Name name, String s) {
    setString(name, s);
  }

  /**
  * Sets a string array value for a field.
  * @param name The field name.
  * @param sa The string array value.
  * @deprecated since ServingXML 0.6.1: use {@link RecordBuilder#setStringArray}
  */

  @Deprecated
  public void setField(Name name, String[] sa) {
    setStringArray(name, sa);
  }

  /**
  * Sets a Object value for a field.
  * @param name The field name.
  * @param o The object value.
  */

  public void setObject(Name name, Object o) {

    ValueType valueType = new ObjectValueType();
    if (o != null) {
      Value fieldValue = new ScalarValue(o, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a string value for a field.
  * @param name The field name.
  * @param s The string value.
  */

  public void setString(Name name, String s) {

    ValueType valueType = ValueTypeFactory.STRING_TYPE;
    if (s != null) {
      Value fieldValue = new ScalarValue(s, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a string array value for a field.
  * @param name The field name.
  * @param sa The string array value.
  */

  public void setStringArray(Name name, String[] sa) {

    ValueType valueType = ValueTypeFactory.STRING_TYPE;
    Value fieldValue = ValueFactory.createStringArrayValue(sa);
    setValue(name, fieldValue);
  }

  /**
  * Sets a SQL timestamp value for a field.
  * @param name The field name.
  * @param value The timestamp value.
  */

  public void setDateTime(Name name, Timestamp value) {

    ValueType valueType = ValueTypeFactory.DATETIME_TYPE;

    if (value != null) {
      DateTimeData o = DateTimeData.fromSqlTimestamp(value);
      Value fieldValue = new ScalarValue(o, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a SQL timestamp value for a field.
  * @param name The field name.
  * @param value milliseconds since January 1, 1970, 00:00:00 GMT. A negative number is the number of millisecondsbefore January 1, 1970, 00:00:00 GMT.
  */

  public void setDateTime(Name name, long value) {

    ValueType valueType = ValueTypeFactory.DATETIME_TYPE;

    DateTimeData o = DateTimeData.fromSqlTimestamp(new Timestamp(value));
    Value fieldValue = new ScalarValue(o, valueType);
    setValue(name, fieldValue);
  }

  /**
  * Sets a SQL date value for a field.
  * @param name The field name.
  * @param value The date value.
  */

  public void setDate(Name name, Date value) {

    ValueType valueType = ValueTypeFactory.DATE_TYPE;

    if (value != null) {
      DateTimeData o = DateTimeData.fromSqlDate(value);
      Value fieldValue = new ScalarValue(o, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a SQL date value for a field.
  * @param name The field name.
  * @param value  milliseconds since January 1, 1970, 00:00:00 GMT. A negative number is the number of millisecondsbefore January 1, 1970, 00:00:00 GMT.
  */

  public void setDate(Name name, long value) {

    ValueType valueType = ValueTypeFactory.DATE_TYPE;

    DateTimeData o = DateTimeData.fromSqlDate(new Date(value));
    Value fieldValue = new ScalarValue(o, valueType);
    setValue(name, fieldValue);
  }

  /**
  * Sets a SQL time value for a field.
  * @param name The field name.
  * @param value The time value.
  */

  public void setTime(Name name, Time value) {

    ValueType valueType = ValueTypeFactory.TIME_TYPE;

    if (value != null) {
      DateTimeData o = DateTimeData.fromSqlTime(value);
      Value fieldValue = new ScalarValue(o, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a SQL time value for a field.
  * @param name The field name.
  * @param value The time value.
  */

  public void setTime(Name name, long value) {

    ValueType valueType = ValueTypeFactory.TIME_TYPE;

    DateTimeData o = DateTimeData.fromSqlTime(new Time(value));
    Value fieldValue = new ScalarValue(o, valueType);
    setValue(name, fieldValue);
  }

  /**
  * Sets a byte[] value for a field.
  * @param name The field name.
  * @param value The byte[] value.
  */

  public void setHexBinary(Name name, byte[] value) {

    ValueType valueType = ValueTypeFactory.HEX_BINARY_TYPE;
    if (value != null) {
      Value fieldValue = new ScalarValue(value, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a packed decimal value for a field.
  * @param name the field name.
  * @param value the packed decimal value.
  */

  public void setPackedDecimal(Name name, PackedDecimal value) {

    if (value != null) {
      ValueType valueType = new PackedDecimalValueType(value.digitCount(), value.decimalPlaces());
      Value fieldValue = new ScalarValue(value, valueType);
      setValue(name, fieldValue);
    } else {
      ValueType valueType = new PackedDecimalValueType(0, 0);
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a byte value for a field.
  * @param name The field name.
  * @param value The byte value.
  */

  public void setByte(Name name, byte value) {

    ValueType valueType = ValueTypeFactory.BYTE_TYPE;
    Value fieldValue = new ScalarValue(new Byte(value), valueType);
    setValue(name, fieldValue);
  }

  /**
  * Sets a Byte value for a field.
  * @param name The field name.
  * @param value The Byte value.
  */

  public void setByte(Name name, Byte value) {

    ValueType valueType = ValueTypeFactory.BYTE_TYPE;
    if (value != null) {
      Value fieldValue = new ScalarValue(value, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a short value for a field.
  * @param name The field name.
  * @param value The short value.
  */

  public void setShort(Name name, short value) {

    ValueType valueType = ValueTypeFactory.SHORT_TYPE;
    Value fieldValue = new ScalarValue(new Short(value), valueType);
    setValue(name, fieldValue);
  }

  /**
  * Sets a Short value for a field.
  * @param name The field name.
  * @param value The Short value.
  */

  public void setShort(Name name, Short value) {

    ValueType valueType = ValueTypeFactory.SHORT_TYPE;
    if (value != null) {
      Value fieldValue = new ScalarValue(value, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a int value for a field.
  * @param name The field name.
  * @param value The int value.
  */

  public void setInteger(Name name, int value) {

    ValueType valueType = ValueTypeFactory.INTEGER_TYPE;
    Value fieldValue = new ScalarValue(new Integer(value), valueType);
    setValue(name, fieldValue);
  }

  /**
  * Sets a Integer value for a field.
  * @param name The field name.
  * @param value The Integer value.
  */

  public void setInteger(Name name, Integer value) {

    ValueType valueType = ValueTypeFactory.INTEGER_TYPE;
    if (value != null) {
      Value fieldValue = new ScalarValue(value, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a long value for a field.
  * @param name The field name.
  * @param value The long value.
  */

  public void setLong(Name name, long value) {

    ValueType valueType = ValueTypeFactory.LONG_TYPE;
    Value fieldValue = new ScalarValue(new Long(value), valueType);
    setValue(name, fieldValue);
  }

  /**
  * Sets a Long value for a field.
  * @param name The field name.
  * @param value The Long value.
  */

  public void setLong(Name name, Long value) {

    ValueType valueType = ValueTypeFactory.LONG_TYPE;
    if (value != null) {
      Value fieldValue = new ScalarValue(value, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a boolean value for a field.
  * @param name The field name.
  * @param value The boolean value.
  */

  public void setBoolean(Name name, boolean value) {

    ValueType valueType = ValueTypeFactory.BOOLEAN_TYPE;
    Value fieldValue = new ScalarValue(new Boolean(value), valueType);
    setValue(name, fieldValue);
  }

  /**
  * Sets a Boolean value for a field.
  * @param name The field name.
  * @param value The Boolean value.
  */

  public void setBoolean(Name name, Boolean value) {

    ValueType valueType = ValueTypeFactory.BOOLEAN_TYPE;
    if (value != null) {
      Value fieldValue = new ScalarValue(value, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a float value for a field.
  * @param name The field name.
  * @param value The float value.
  */

  public void setFloat(Name name, float value) {

    ValueType valueType = ValueTypeFactory.FLOAT_TYPE;
    Value fieldValue = new ScalarValue(new Float(value), valueType);
    setValue(name, fieldValue);
  }

  /**
  * Sets a Float value for a field.
  * @param name The field name.
  * @param value The Float value.
  */

  public void setFloat(Name name, Float value) {

    ValueType valueType = ValueTypeFactory.FLOAT_TYPE;
    if (value != null) {
      Value fieldValue = new ScalarValue(value, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a double value for a field.
  * @param name The field name.
  * @param value The double value.
  */

  public void setDouble(Name name, double value) {

    ValueType valueType = ValueTypeFactory.DOUBLE_TYPE;
    Value fieldValue = new ScalarValue(new Double(value), valueType);
    setValue(name, fieldValue);
  }

  /**
  * Sets a Double value for a field.
  * @param name The field name.
  * @param value The Double value.
  */

  public void setDouble(Name name, Double value) {

    ValueType valueType = ValueTypeFactory.DOUBLE_TYPE;
    if (value != null) {
      Value fieldValue = new ScalarValue(value, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a BigDecimal value for a field.
  * @param name The field name.
  * @param value The BigDecimal value.
  */

  public void setBigDecimal(Name name, BigDecimal value) {

    ValueType valueType = ValueTypeFactory.DECIMAL_TYPE;

    if (value != null) {
      Value fieldValue = new ScalarValue(value, valueType);
      setValue(name, fieldValue);
    } else {
      Value fieldValue = new NullValue(valueType);
      setValue(name, fieldValue);
    }
  }

  /**
  * Sets a value for a field.
  * @param name The field name.
  * @param value The field value.
  */

  public void setValue(Name name, Value value) {

    if (value != null) {
      int index = getFieldIndex(name);
      if (index == -1) {
        FieldType fieldType = new DefaultFieldType(name);
        index = addFieldType(fieldType);
      }

      setValue(index,value);
    }
  }

  /**
  * Sets a value for a field.
  * @param fieldType The field type.
  * @param value The field value.
  */

  public void setValue(FieldType fieldType, Value value) {

    if (value != null) {
      int index = getFieldIndex(fieldType.getName());
      if (index == -1) {
        index = addFieldType(fieldType);
      } else {
        fieldTypes[index] = fieldType;
      }

      setValue(index,value);
    }
  }

  /**
  * Sets a segment value for a field.
  * @param name The field name.
  * @param segment The segment value.
  * @deprecated since ServingXML 0.8.1: replaced by {@link RecordBuilder#setSegments}
  */

  @Deprecated
  public void setField(Name name, Record segment) {
    setField(name, new Record[]{segment});
  }

  /**
  * Sets a segment array value for a field.
  * @param name The field name.
  * @param segments The segment array value.
  * @deprecated since ServingXML 0.8.1: replaced by {@link RecordBuilder#setSegments}
  */

  @Deprecated
  public void setField(Name name, Record[] segments) {

    if (segments != null) {
      int index = getFieldIndex(name);
      if (index == -1) {
        FieldType fieldType = new DefaultFieldType(name);
        index = addFieldType(fieldType);
      }

      Value value = new SegmentArrayValue(segments);
      setValue(index,value);
    }
  }

  /**
  * Sets a record array value for a field.
  * @param name The field name.
  * @param records The record array value.
  * @deprecated since ServingXML 0.8.3: replaced by {@link 
  *             RecordBuilder#setRecords}
  */

  @Deprecated
  public void setSegments(Name name, Record[] records) {
    setRecords(name, records);
  }

  /**
  * Sets a record array value for a field.
  * @param name The field name.
  * @param records The record array value.
  */

  public void setRecords(Name name, Record[] records) {

    if (records != null) {
      int index = getFieldIndex(name);
      if (index == -1) {
        FieldType fieldType = new DefaultFieldType(name);
        index = addFieldType(fieldType);
      }

      Value value = new SegmentArrayValue(records);
      setValue(index,value);
    }
  }

  public Record toRecord() {

    RecordType recordType = getRecordType();

    Record record = new RecordImpl(recordType,values);

    return record;
  }

  /**
  * Gets the number of fields
  * @return The number of fields
  */

  public int fieldCount() {
    return fieldCount;
  }

  public RecordType getRecordType() {
    FieldType[] types = new FieldType[fieldCount];
    System.arraycopy(fieldTypes, 0, types, 0, fieldCount);
    RecordType recordType = new RecordTypeImpl(recordTypeName,types);
    return recordType;
  }

  public Value getValue(Name name) {
    int index = getFieldIndex(name);
    return index == -1 ? null : values[index];
  }

  public Value getValue(int index) {
    return values[index];
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
    this.values = new Value[fieldCount];
    for (int i = 0; i < fieldCount; ++i) {
      values[i] = Value.EMPTY;
    }
  }

  public void writeToContentHandler(PrefixMap  prefixMap, ContentHandler handler) 
  throws SAXException {

    //System.out.println(getClass().getName()+".writeToContentHandler start");
    String qname = recordTypeName.toQname(prefixMap);
    String namespaceUri = recordTypeName.getNamespaceUri();
    if (namespaceUri.length() > 0) {
      String prefix = prefixMap.getPrefix(namespaceUri);
      if (!prefixMap.containsPrefixMapping(prefix, namespaceUri)) {
        PrefixMapImpl newPrefixMap = new PrefixMapImpl(prefixMap);
        newPrefixMap.setPrefixMapping(prefix,namespaceUri);
        prefixMap = newPrefixMap;
        handler.startPrefixMapping(prefix,namespaceUri);
      }
    }
    handler.startElement(namespaceUri,recordTypeName.getLocalName(),qname, FieldType.EMPTY_ATTRIBUTES);
    for (int i = 0; i < fieldCount; ++i) {
      FieldType fieldType = fieldTypes[i];
      Value value = values[i];
      //System.out.println(value.getClass().getName());
      value.writeToContentHandler(fieldType.getName(), prefixMap, handler);
    }
    handler.endElement(namespaceUri,recordTypeName.getLocalName(),qname);
    //System.out.println(getClass().getName()+".writeToContentHandler end");
  }

  public String[] getStringArray(Name name) {
    String[] sa = null;

    int index = getFieldIndex(name);
    if (index != -1) {
      Value value = values[index];
      sa = value.getStringArray();
    }
    return sa;
  }

  public String getString(Name name) {
    String s = null;
    int index = getFieldIndex(name);
    if (index != -1) {
      Value value = values[index];
      s = value.getString();
    }
    return s;
  }

  public Object getObject(Name name) {
    Object o = null;
    int index = getFieldIndex(name);
    if (index != -1) {
      Value value = values[index];
      o = value.getObject();
    }
    return o;
  }
}



