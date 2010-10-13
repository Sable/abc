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

import java.util.ArrayList;

import org.w3c.dom.Element;

import com.servingxml.util.Asserter;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.xml.DomIterator;
import com.servingxml.util.record.Record;       
import com.servingxml.util.record.FieldType;       
import com.servingxml.util.record.RecordType;
import com.servingxml.util.record.Value;       

/**
 * This class provides an implementation of a <code>KeyIdentifier</code>.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class KeyIdentifierImpl implements KeyIdentifier {
  private static final String sourceClass = KeyIdentifierImpl.class.getName();
  
  private static final String SERVINGXML_NS_URI=SystemConstants.SERVINGXML_NS_URI;
  private static final String KEY = "key";

  private String[] segmentNames = SystemConstants.EMPTY_STRING_ARRAY;
  private Identifier arrayIdentity = new ArrayIdentifier();

  public KeyIdentifierImpl() {
  }

  public KeyIdentifierImpl(Record parameters) {
    RecordType recordType = parameters.getRecordType();

    segmentNames = new String[recordType.count()];

    for (int i = 0; i < recordType.count(); ++i) {
      FieldType fieldType = recordType.getFieldType(i);
      segmentNames[i] = fieldType.getName().getLocalName();
    }
  }

  public String[] getSegmentNames() {
    return segmentNames;
  }

  public void initialize(Element keyNode) {
    final String sourceMethod = "initialize";
    
    Asserter.assertTrue(sourceClass,sourceMethod,KEY,
      DomHelper.areEqual(keyNode,SERVINGXML_NS_URI,KEY));

    final ArrayList<String> segmentList = new ArrayList<String>();
    DomIterator.ChildCommand command = new DomIterator.ChildCommand() {
      public void doElement(Element parent, Element segmentNode) {
        String name = segmentNode.getAttribute("name");
        segmentList.add(name);
      }
    };
    DomIterator.toEveryChild(keyNode,command,SERVINGXML_NS_URI,"keyField");

    segmentNames = new String[segmentList.size()];
    segmentNames = segmentList.toArray(segmentNames);
  }
  
  /**
   * Compares the two key arguments for equality.  Returns true or false
   * depending on whether the two key arguments are equal.<p>
   *
   * @param key1 the first key object to be compared.
   * @param key2 the second key object to be compared.
   * @return <code>true</code> or <false> depending on whether the two key
   * arguments are equal.
   */
  public boolean equalTo(Object key1, Object key2) {
    final String sourceMethod = "equalTo";

    boolean isEqual = true;

    DynamicContentKey k1 = (DynamicContentKey)key1;
    DynamicContentKey k2 = (DynamicContentKey)key2;

    isEqual = k1.getName().equals(k2.getName());
    if (isEqual) {
      //  Two documents with same name must have same key identifier
      Object[] values1 = k1.getFieldValues();
      Object[] values2 = k2.getFieldValues();
      Asserter.assertTrue(sourceClass,sourceMethod,"",values1.length == values2.length);
      for (int i = 0; isEqual && i < values1.length; ++i) {
        Object value1 = values1[i];
        Object value2 = values2[i];
        boolean value1IsArray = value1.getClass().isArray();
        boolean value2IsArray = value2.getClass().isArray();
        if (value1IsArray && value2IsArray) {
          isEqual = arrayIdentity.equalTo(value1,value2);
        } else if (!value1IsArray && !value2IsArray) {
          isEqual = value1.equals(value2);
        } else {
          isEqual = false;
        }
      }
    }

    return isEqual;
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();

    for (int i = 0; i < segmentNames.length; ++i) {
      String segmentName = segmentNames[i];
      if (i > 0) {
        buf.append(",");
      }
      buf.append(segmentName);
    }
    return buf.toString();
  }
}
