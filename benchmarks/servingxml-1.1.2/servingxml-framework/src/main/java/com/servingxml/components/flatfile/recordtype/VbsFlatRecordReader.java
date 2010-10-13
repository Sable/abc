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

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.util.xml.XsltChooser;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class VbsFlatRecordReader implements FlatRecordReader {
  private final FlatRecordReader[] flatRecordReaders;

  public VbsFlatRecordReader(FlatRecordReader[] flatRecordReaders) {
    this.flatRecordReaders = flatRecordReaders;
  }

  public void readRecord(final ServiceContext context, final Flow flow,
                         final RecordInput recordInput, 
                         final DelimiterExtractor[] recordDelimiters,
                         final int recordDelimiterStart, 
                         int recordDelimiterCount, 
                         int maxRecordWidth,
                         final RecordReceiver receiver) {

    try {
      final int index = recordInput.getPosition();
      for (int i = 0; i < flatRecordReaders.length; ++i) {
        recordInput.setPosition(index);
        flatRecordReaders[i].readRecord(context,flow,recordInput,recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                                        maxRecordWidth, receiver);
      }
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public int calculateFixedRecordLength(Record parameters, Record currentRecord) {

    int length = 0;
    for (int i = 0; i < flatRecordReaders.length && length != -1; ++i) {
      FlatRecordReader flatRecordReader = flatRecordReaders[i];
      int n = flatRecordReader.calculateFixedRecordLength(parameters, currentRecord);
      if (n >= 0) {
        length = n > length ? n : length;
      }
    }
    return length;
  }

  public void endReadRecords(final ServiceContext context, 
                             final Flow flow, 
                             final DelimiterExtractor[] recordDelimiters,
                             final int recordDelimiterStart, 
                             final int recordDelimiterCount, 
                             int maxRecordWidth,
                             final RecordReceiver receiver) {
    for (int i = 0; i < flatRecordReaders.length; ++i) {
      flatRecordReaders[i].endReadRecords(context, flow, recordDelimiters, recordDelimiterStart, recordDelimiterCount, maxRecordWidth, receiver);
    }
  }
}
