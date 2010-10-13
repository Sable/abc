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
import com.servingxml.util.PrefixMap;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class FlatRecordReaderChoice implements FlatRecordReader {
  private final InputSource inputSource = new InputSource();

  private final PrefixMap prefixMap;
  private final FlatRecordReader defaultRecordReader;
  private final FlatRecordReader[] flatRecordReaderOptions;
  private final XsltChooser chooser;
  private FlatRecordReader flatRecordReader;
  private Flow newFlow;

  public FlatRecordReaderChoice(PrefixMap prefixMap, FlatRecordReader defaultRecordReader, 
                                FlatRecordReader[] flatRecordReaderOptions,
                                XsltChooser chooser) {
    this.prefixMap = prefixMap;
    this.defaultRecordReader = defaultRecordReader;
    this.flatRecordReaderOptions = flatRecordReaderOptions;
    this.chooser = chooser;
  }

  public void readRecord(final ServiceContext context, 
                         final Flow flow,
                         final RecordInput recordInput, 
                         final DelimiterExtractor[] recordDelimiters,
                         final int recordDelimiterStart, 
                         final int recordDelimiterCount, 
                         final int maxRecordWidth,
                         final RecordReceiver receiver) {

    try {
      flatRecordReader = FlatRecordReader.NULL;

      RecordReceiver defaultReceiver = new RecordReceiver() {
        public void receiveRecord(Record defaultRecord) {
          Source source = new SAXSource(defaultRecord.createXmlReader(prefixMap), inputSource);
          int index = chooser.choose(source, flow.getParameters());

          flatRecordReader = FlatRecordReader.NULL;
          if (index >= 0 && index < flatRecordReaderOptions.length) {
            //System.out.println(getClass().getName()+".readRecord choose" + index);
            flatRecordReader = flatRecordReaderOptions[index];
            for (int i = 0; i < flatRecordReaderOptions.length; ++i) {
              if (i != index) {
                //System.out.println(getClass().getName()+".readRecord endReadRecords" + i);
                flatRecordReaderOptions[i].endReadRecords(context, flow, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                                                          maxRecordWidth, receiver);
              }
            }
          }
          newFlow = flow.replaceRecord(context,defaultRecord);
          //System.out.println(getClass().getName()+".resolveFlatRecordReader "+defaultRecord.toXmlString(context));
        }
      };

      //System.out.println(getClass().getName()+".resolveFlatRecordReader "+recordInput.toString());
      final int position = recordInput.getPosition();
      defaultRecordReader.readRecord(context, flow, recordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                                     maxRecordWidth, defaultReceiver);

      //System.out.println(getClass().getName()+".resolveFlatRecordReader position="+position+",current="+recordInput.getPosition());
      recordInput.setPosition(position);
      flatRecordReader.readRecord(context,newFlow,recordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount,
                                  maxRecordWidth, receiver);
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public int calculateFixedRecordLength(Record parameters, Record currentRecord) {

    int length = defaultRecordReader.calculateFixedRecordLength(parameters, currentRecord);
    for (int i = 0; i < flatRecordReaderOptions.length && length != -1; ++i) {
      FlatRecordReader flatRecordReader = flatRecordReaderOptions[i];
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
                             int recordDelimiterCount, 
                             int maxRecordWidth,
                             final RecordReceiver receiver) {
    for (int i = 0; i < flatRecordReaderOptions.length; ++i) {
      flatRecordReaderOptions[i].endReadRecords(context, flow, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                                                maxRecordWidth, receiver);
    }
  }
}
