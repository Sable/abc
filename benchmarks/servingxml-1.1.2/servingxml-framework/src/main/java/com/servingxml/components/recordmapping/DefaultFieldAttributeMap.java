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

package com.servingxml.components.recordmapping;

import org.xml.sax.helpers.AttributesImpl;

import org.xml.sax.SAXException;

import com.servingxml.util.Name;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordType;
import com.servingxml.util.record.Value;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.NameTest;
import com.servingxml.util.record.FieldType;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;

/**
 * A command for mapping a field in a flat file to an element or attribute
 * in an XML stream.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class DefaultFieldAttributeMap implements MapXml {
  private final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final PrefixMap prefixMap;
  private final NameTest fields;
  private final NameTest except;

  public DefaultFieldAttributeMap(PrefixMap prefixMap, NameTest fields, NameTest except) {
    this.prefixMap = prefixMap;
    this.fields = fields;
    this.except = except;
  }

  public void groupStarted(final ServiceContext context, final Flow flow, 
                             final Record previousRecord, final Record nextRecord, 
                             final ExtendedContentHandler handler, Record variables) {
  }

  public void writeRecord(ServiceContext context, Flow flow, Record previousRecord, Record nextRecord, 
                          ExtendedContentHandler handler, GroupState groupListener) {
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, 
                              AttributesImpl attributes) {
    final Record record = flow.getRecord();
    RecordType recordType = record.getRecordType();
    for (int i = 0; i < recordType.count(); ++i) {
      FieldType fieldType = recordType.getFieldType(i);
      Name fieldName = fieldType.getName();
      if (fields.matches(fieldName) &&  !except.matches(fieldName)) {
        Value value = record.getValue(i);
        String qname = fieldName.toQname(prefixMap);
        attributes.addAttribute(fieldName.getNamespaceUri(),fieldName.getLocalName(),qname,"CDATA",value.getString());
      }
    }
  }

  public boolean isGrouping() {
    return false;
  }
}
