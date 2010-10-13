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

package com.servingxml.components.content.dynamic;

import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.io.cache.Key;

/**
 * This class provides an implementation of a key used to uniquely identify XML documents.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public final class DynamicContentKey implements Key {

  private final Name name;
  private final KeyIdentifier identifier;
  private final Object[] attributeValues;
  private final int hashCode;

  public DynamicContentKey(Name name) {
    this(name,Record.EMPTY,new KeyIdentifierImpl());
  }
  
  public DynamicContentKey(Name name, Record parameters) {
    this(name,parameters,new KeyIdentifierImpl(parameters));
  }

  public DynamicContentKey(Name name, Record parameters, KeyIdentifier identifier) {

    this.name = name;
    this.identifier = identifier;

    String[] segmentNames = identifier.getSegmentNames();
    attributeValues = new Object[segmentNames.length];

    int hash = name.hashCode();
    for (int i = 0; i < segmentNames.length; ++i) {
      String segmentName = segmentNames[i];
      Value value = parameters.getValue(new QualifiedName( segmentName));
      if (value != null) {
        Object o = value.getStringArray();
        attributeValues[i] = o;
        hash += 31*o.hashCode();
      } else {
        attributeValues[i] = "";
      }
    }

    hashCode = hash;
  }

  public final Name getName() {
    return name;
  }

  public final Object getValue(String attributeName) {
    return getValue(attributeName,null);
  }

  public final Object getValue(String attributeName,Object defaultValue) {

    Object value = defaultValue;
    String[] segmentNames = identifier.getSegmentNames();
    for (int i = 0; i < segmentNames.length; ++i) {
      if (attributeName.equals(segmentNames[i])) {
        value = attributeValues[i];
        break;
      }
    }
    return value;
  }

  public final Object[] getFieldValues() {
    return attributeValues;
  }
  /**
   * Compares this key to the specified object.
   *
   * @param anObject the object being tested for equality.
   *
   * @return <code>true</code> if the two keys are equal, <code>false</code> otherwise.
   */

  public boolean equals(Object anObject) {
    
    boolean isEqual = true;
    if (anObject != this) {
      isEqual = identifier.equalTo(this,anObject);
    }
    return isEqual;
  }
  /**
   * Returns a hash code value for this key.
   *
   * @return a hash code value for this key.
   */

  public int hashCode() {
    return hashCode;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("name = ");
    buf.append(name.toString());
    String[] segmentNames = identifier.getSegmentNames();
    for (int i = 0; i < segmentNames.length; ++i) {
      buf.append(", ");
      buf.append(segmentNames[i]);
      buf.append("=");
      buf.append(attributeValues[i].toString());
    }

    return buf.toString();
  }
}
