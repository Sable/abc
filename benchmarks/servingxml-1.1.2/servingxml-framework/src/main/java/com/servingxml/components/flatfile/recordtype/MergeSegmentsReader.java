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
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.recordmapping.GroupRecognizer;
import com.servingxml.expr.substitution.IntegerSubstitutionExpr;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class MergeSegmentsReader implements FlatRecordReader {
  private final FlatRecordReader sdwRecordReader;
  private final boolean suppressRDW;
  private final FlatRecordReader dataRecordReader;
  private final GroupRecognizer groupRecognizer;
  private Record previousSegment;
  private Record currentSegment = null;
  private boolean started = false;
  private final IntegerSubstitutionExpr segmentLengthExpr;
  private Flow newFlow;
  private int index;
  private RecordInput currentSegmentInput;
  private RecordInput compositeRecordInput;
  private boolean suppressSDW;
  private int beginIndex = 0;

  public MergeSegmentsReader(GroupRecognizer groupRecognizer,
                                    FlatRecordReader sdwRecordReader,
                                    boolean suppressRDW,
                                    FlatRecordReader dataRecordReader, 
                                    IntegerSubstitutionExpr segmentLengthExpr) {
    this.groupRecognizer = groupRecognizer;
    this.sdwRecordReader = sdwRecordReader;
    this.suppressRDW = suppressRDW;
    this.dataRecordReader = dataRecordReader;
    this.segmentLengthExpr = segmentLengthExpr;
    this.suppressSDW = suppressRDW;
  }

  public void readRecord(final ServiceContext context, 
                         final Flow flow,
                         final RecordInput segmentInput, 
                         final DelimiterExtractor[] recordDelimiters,
                         final int recordDelimiterStart,
                         final int recordDelimiterCount,
                         final int maxRecordWidth,
                         final RecordReceiver receiver) {

    RecordReceiver controlRecordReceiver = new RecordReceiver() {
      public void receiveRecord(Record nextSegment) {
        try {
          if (currentSegment != null) {
            if (!started) {
              started = groupRecognizer.startRecognized(context, flow, 
                                                        previousSegment, 
                                                        currentSegment);
              if (started) {
                newFlow = flow.replaceRecord(context,currentSegment);
                suppressSDW = suppressRDW;
              }
            }
            if (started) {
              if (compositeRecordInput == null) {
                compositeRecordInput = currentSegmentInput;
              } else {
                //System.out.println("beginIndex="+beginIndex);
                //System.out.println("compositeRecordInput:  start="+compositeRecordInput.start()+", length="+compositeRecordInput.length());
                //System.out.println("currentSegmentInput:  start="+currentSegmentInput.start()+", length="+currentSegmentInput.length());
                //System.out.println(compositeRecordInput.toString());
                //System.out.println("currentSegmentInput");
                //System.out.println(currentSegmentInput.toString());
                compositeRecordInput = compositeRecordInput.concatenate(currentSegmentInput, beginIndex);
                //System.out.println("compositeRecordInput:  start="+compositeRecordInput.start()+", length="+compositeRecordInput.length());
                //System.out.println("after concat:");
                //System.out.println(compositeRecordInput.toString());
              }
              started = !groupRecognizer.endRecognized(context, flow, 
                                                       currentSegment, 
                                                       nextSegment);
              if (!started) {
                if (compositeRecordInput != null) {
                  dataRecordReader.readRecord(context, newFlow, compositeRecordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                                              maxRecordWidth, receiver);
                  compositeRecordInput = null;
                }
              } 
              previousSegment = currentSegment;
            }
          }
          //System.out.println("nextSegment="+nextSegment.toXmlString(context));
          int segmentLength = segmentLengthExpr.evaluateAsInt(flow.getParameters(), nextSegment);
          beginIndex = segmentInput.getPosition() - index;
          if (suppressSDW) {
            int offset = segmentInput.getPosition() - index;
            segmentLength -= offset;
            suppressSDW = true;
          } else {
            segmentInput.setPosition(index);
          }
          //System.out.println("segmentLength="+segmentLength + ", offset="+offset);
          currentSegmentInput = segmentInput.readSegment(segmentLength);
          currentSegment = nextSegment;
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
    sdwRecordReader.readRecord(context, flow, segmentInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, maxRecordWidth, controlRecordReceiver);
  }

  public int calculateFixedRecordLength(Record parameters, Record currentSegment) {

    int length = dataRecordReader.calculateFixedRecordLength(parameters, currentSegment);
    return length;
  }

  public void endReadRecords(final ServiceContext context, 
                             final Flow flow, 
                             DelimiterExtractor[] recordDelimiters,
                             final int recordDelimiterStart, 
                             int recordDelimiterCount, 
                             int maxRecordWidth,
                             final RecordReceiver receiver) {
    if (currentSegmentInput != null) {
      if (compositeRecordInput == null) {
        compositeRecordInput = currentSegmentInput;
      } else {
        //System.out.println("beginIndex="+beginIndex);
        //System.out.println("compositeRecordInput:  start="+compositeRecordInput.start()+", length="+compositeRecordInput.length());
        //System.out.println("currentSegmentInput:  start="+currentSegmentInput.start()+", length="+currentSegmentInput.length());
        //System.out.println(compositeRecordInput.toString());
        //System.out.println("currentSegmentInput");
        //System.out.println(currentSegmentInput.toString());
        compositeRecordInput = compositeRecordInput.concatenate(currentSegmentInput, beginIndex);
        //System.out.println("compositeRecordInput:  start="+compositeRecordInput.start()+", length="+compositeRecordInput.length());
        //System.out.println("after concat:");
        //System.out.println(compositeRecordInput.toString());
      }
      dataRecordReader.readRecord(context, newFlow, compositeRecordInput, recordDelimiters, recordDelimiterStart, recordDelimiterCount, 
                                  maxRecordWidth, receiver);
      compositeRecordInput = null;
    }
  }
}

