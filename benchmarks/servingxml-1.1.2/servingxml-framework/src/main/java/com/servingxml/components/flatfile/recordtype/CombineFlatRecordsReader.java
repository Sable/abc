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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.recordmapping.GroupRecognizer;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Stack;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.record.RecordReceiver;
import java.io.IOException;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class CombineFlatRecordsReader implements FlatRecordReader {
  private Name compositeRecordTypeName;
  private final NameSubstitutionExpr recordTypeNameExpr;
  private final Name repeatingGroupName;
  private final FlatRecordReader defaultRecordReader;
  private final FlatRecordReader dataRecordReader;
  private final GroupRecognizer groupRecognizer;
  private Record previousSegment;
  private Record currentRecord = null;
  private boolean started = false;
  private Flow newFlow;
  private int index;
  private Record headerRecord = Record.EMPTY;
  private final Stack<Record> stack = new Stack<Record>();

  public CombineFlatRecordsReader(NameSubstitutionExpr recordTypeNameExpr, 
                                 Name repeatingGroupName,
                                 GroupRecognizer groupRecognizer,
                                 FlatRecordReader defaultRecordReader,
                                 FlatRecordReader dataRecordReader) {
    this.recordTypeNameExpr = recordTypeNameExpr;
    this.repeatingGroupName = repeatingGroupName;
    this.groupRecognizer = groupRecognizer;
    this.defaultRecordReader = defaultRecordReader;
    this.dataRecordReader = dataRecordReader;
  }

  public void readRecord(final ServiceContext context, 
                         final Flow flow,
                         final RecordInput segmentInput, 
                         final DelimiterExtractor[] recordDelimiters,
                         final int recordDelimiterStart, 
                         int recordDelimiterCount, 
                         int maxRecordWidth,
                         final RecordReceiver receiver) {

    RecordReceiver controlRecordReceiver = new RecordReceiver() {
      public void receiveRecord(Record nextSegment) {
        try {
          if (currentRecord != null) {
            headerRecord = currentRecord;
            newFlow = flow.replaceRecord(context,currentRecord);
          }
          //System.out.println("nextSegment="+nextSegment.toXmlString(context));
        } catch (ServingXmlException e) {
          e.printStackTrace();
          throw e;
        } catch (Exception e) {
          e.printStackTrace();
          throw new ServingXmlException(e.getMessage(),e);
        }
      } 
    };

    RecordReceiver dataRecordReceiver = new RecordReceiver() {
      public void receiveRecord(Record nextSegment) {
        try {
          if (currentRecord != null) {
            if (!started) {
              started = groupRecognizer.startRecognized(context, flow, 
                                                        previousSegment, 
                                                        currentRecord);
            }
            if (started) {
              compositeRecordTypeName = recordTypeNameExpr.evaluateName(flow.getParameters(),
                                                                        currentRecord);
              started = !groupRecognizer.endRecognized(context, flow, 
                                                       currentRecord, 
                                                       nextSegment);
              stack.push(currentRecord);
              if (!started) {
                writeStack(context, receiver);
              }
            } else {
              //String s = currentFlow.getRecord().toXmlString(context);
              //System.out.println("writeRecord " + s);
              //receiver.receiveRecord(currentRecord);
            }
            previousSegment = currentRecord;
          }
          //System.out.println("nextSegment="+nextSegment.toXmlString(context));
          currentRecord = nextSegment;
        } catch (ServingXmlException e) {
          e.printStackTrace();
          throw e;
        } catch (Exception e) {
          e.printStackTrace();
          throw new ServingXmlException(e.getMessage(),e);
        }
      } 
    };

    //System.out.println(getClass().getName()+".resolveFlatRecordReader "+segmentInput.toString());
    index = segmentInput.getPosition();
    if (!started) {
      defaultRecordReader.readRecord(context, flow, segmentInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, maxRecordWidth, controlRecordReceiver);
    }
    try {
      segmentInput.setPosition(index);
      dataRecordReader.readRecord(context, flow, segmentInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, maxRecordWidth, dataRecordReceiver);
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  private void writeStack(ServiceContext context, RecordReceiver receiver) {

    if (!stack.empty()) {
      //System.out.println(getClass().getName()+".writeStack " + compositeRecordTypeName + " " + stack.size());
      //System.out.println("compositeRecordType="+compositeRecordTypeName);
      RecordBuilder recordBuilder = new RecordBuilder(compositeRecordTypeName);
      Record[] segments = new Record[stack.size()];
      for (int i = 0; !stack.empty(); ++i) {
        segments[i] = stack.dequeue();
      }
      recordBuilder.setRecords(repeatingGroupName,segments);
      Record compositeRecord = recordBuilder.toRecord();

      receiver.receiveRecord(compositeRecord);
    }
  }

  public void endReadRecords(final ServiceContext context, 
                             final Flow flow, 
                             DelimiterExtractor[] recordDelimiters,
                             final int recordDelimiterStart, 
                             int recordDelimiterCount, 
                             int maxRecordWidth,
                             final RecordReceiver receiver) {
    //System.out.println(getClass().getName()+".endReadRecords");
    if (started && currentRecord != null) {
      stack.push(currentRecord);
      currentRecord = null;
    }
    writeStack(context,receiver);
  }

  public int calculateFixedRecordLength(Record parameters, Record currentRecord) {

    int length = defaultRecordReader.calculateFixedRecordLength(parameters, currentRecord);
    return length;
  }
}

