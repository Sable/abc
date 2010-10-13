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

package com.servingxml.components.flatfile.layout;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.Name;
import com.servingxml.util.record.Value;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class FlatFileSignatureImpl implements FlatFileSignature {
  private final Name recordTypeName;
  private final Name fieldName;
  private final SignatureMethod method;
  private final boolean validatationOn;
  private Value expectedValue = Value.EMPTY;

  public FlatFileSignatureImpl(Name recordTypeName, Name fieldName, 
    SignatureMethod method, boolean validatationOn) {
    this.recordTypeName = recordTypeName;
    this.fieldName = fieldName;
    this.method = method;
    this.validatationOn = validatationOn;
  }

  public void readMetaRecord(ServiceContext context, Record parameters, Record record) {

    if (record.getRecordType().getName().equals(recordTypeName)) {
      expectedValue = record.getValue(fieldName);
      //System.out.println(getClass().getName()+".readMetaRecord fieldName = " + fieldName + ", value = " + expectedValue.getString());
      if (expectedValue == null) {
        throw new ServingXmlException("Header field " + fieldName + " not found");
      }
    }
  }

  public Record updateMetaRecord(ServiceContext context, Record parameters, Record record) {
    if (record.getRecordType().getName().equals(recordTypeName)) {
      RecordBuilder recordBuilder = new RecordBuilder(record);
      Value value = method.getSignature();
      recordBuilder.setValue(fieldName, value);
      record = recordBuilder.toRecord();
    }
    return record;
  }

  public void data(byte[] bytes, int start, int length) {
    method.data(bytes, start, length);
  }

  public void validate(ServiceContext context, Record parameters) {
    if (validatationOn) {
      method.validate(expectedValue);
    }
  }
}
