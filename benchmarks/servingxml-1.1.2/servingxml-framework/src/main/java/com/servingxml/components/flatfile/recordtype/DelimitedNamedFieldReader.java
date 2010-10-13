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

package com.servingxml.components.flatfile.recordtype;

import java.io.IOException;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.Name;
import com.servingxml.util.StringHelper;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Value;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class DelimitedNamedFieldReader implements FlatRecordFieldReader {
  private final Name tagName;
  private final Name variableName;
  private final FlatRecordFieldReader nameReader;
  private final FlatRecordFieldReader valueReader;
  private RecordBuilder namedFieldBuilder;
  private final ByteArrayBuilder byteArrayBuilder;
  private final FlatFileOptions flatFileOptions;

  public DelimitedNamedFieldReader(Name tagName, FlatRecordFieldReader nameReader, 
                                   Name variableName, FlatRecordFieldReader valueReader, FlatFileOptions flatFileOptions) {
    this.tagName = tagName;
    this.variableName = variableName;
    this.nameReader = nameReader;
    this.valueReader = valueReader;
    this.namedFieldBuilder = new RecordBuilder(variableName);
    this.byteArrayBuilder = new ByteArrayBuilder();
    this.flatFileOptions = flatFileOptions;
  }

  public void readField(ServiceContext context, 
                        Flow flow,
                        RecordInput recordInput, 
                        DelimiterExtractor[] recordDelimiters, 
                        int recordDelimiterStart, 
                        int recordDelimiterCount, 
                        int maxRecordWidth,
                        RecordBuilder recordBuilder) {
    try {
      //System.out.println(getClass().getName()+".readField enter");
      //System.out.println(recordInput.toString());
      //System.out.println (getClass().getName()+".readNamedFields enter " + new String(data,start,length));
      //System.out.println (getClass().getName()+".readNamedFields enter index="+position + ", length = " + length + ", repeatDels = " +segmentDelimiters.length);

      int index = recordInput.getPosition();
      int pos1 = index;
      boolean done = false;
      //System.out.println (getClass().getName()+".readNamedFields top index = " + index);
      namedFieldBuilder.clear();
      recordInput.setPosition(index);
      nameReader.readField(context, flow, recordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                           maxRecordWidth, namedFieldBuilder);
      pos1 = recordInput.getPosition();
      if (pos1 == index) {
        done = true;
      }
      if (!done) {
        if (pos1 != index && !recordInput.done()) {
          valueReader.readField(context, flow, recordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                                maxRecordWidth, namedFieldBuilder);
          pos1 = recordInput.getPosition();
          Record nameValue = namedFieldBuilder.toRecord();
          String tag = nameValue.getString(tagName);
          if (tag == null || tag.length() == 0) {
            done = true;
          } else {
            //System.out.println (getClass().getName()+".readField name=" + name);
            String name = StringHelper.constructNameFromValue(tag);
            if (name.length() == 0) {
              done = true;
            } else {
              Value value = nameValue.getValue(variableName);
              if (value != null) {
                //System.out.println(getClass().getName()+".readField " + value.getString());
                Name fieldName = new QualifiedName(name);
                recordBuilder.setValue(fieldName, value);
              }
            }
          }
        }
        index = pos1;
        //System.out.println (getClass().getName()+".readField bottom index = " + index);
      }
      //System.out.println (getClass().getName()+".readNamedFields leave index="+index);

      //recordInput.setPosition(index);
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public int getFixedEndPosition(Record parameters, Record currentRecord, int currentPosition) {
    return -1;
  }
}
