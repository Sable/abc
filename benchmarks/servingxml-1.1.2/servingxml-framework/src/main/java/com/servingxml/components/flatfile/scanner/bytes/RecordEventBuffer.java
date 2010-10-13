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

package com.servingxml.components.flatfile.scanner.bytes;

import java.nio.charset.Charset;

import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.components.flatfile.options.ByteTrimmer;
import com.servingxml.components.flatfile.FlatContentReceiver;

public class RecordEventBuffer {
  RecordEventBuffer next;
  RecordEventBuffer previous;
  Charset charset;

  RecordEvent[] eventList;
  int length = 0;
  int capacity = 1;
  int level;
  int start = 0;
  private ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();

  public RecordEventBuffer(Charset charset) {
    this.eventList = new RecordEvent[]{new RecordEvent(),null,null,null,null};
    this.charset = charset;
  }

  public boolean isEmptyLine(ByteTrimmer byteTrimmer) {
    boolean isEmpty = true;
    //System.out.println(getClass().getName()+".isEmptyLine event = length = " + length);
    for (int i = 0; isEmpty && i < length; ++i) {
      //System.out.println(getClass().getName()+".isEmptyLine event = " + eventList[i].eventType.getClass().getName());
      isEmpty = eventList[i].isEmptyLine(byteArrayBuilder, byteTrimmer);
    }
    return isEmpty;
  }

  public void clear() {
    length = 0;
    start = 0;
    byteArrayBuilder.clear();
  }

  public ByteArrayBuilder byteArrayBuilder() {
    return byteArrayBuilder;
  }

  public void write(FlatContentReceiver receiver) {
    //System.out.println (getClass().getName()+".write enter");
    for (int i = 0; i < length; ++i) {
      //System.out.println (getClass().getName()+".write start " + i);
      eventList[i].write(byteArrayBuilder, charset, receiver);
      //System.out.println (getClass().getName()+".write end " + i);
    }
    //System.out.println (getClass().getName()+".write leave");
  }

  private final void grow(int n) {

    if (length + n >= eventList.length) {
      int size = (length+n)*2;
      RecordEvent[] temp = new RecordEvent[size];
      System.arraycopy(eventList,0,temp,0,length);
      eventList = temp;  
    }
    if (capacity <= length + n) {
      for (int i = capacity; i < length+n; ++i) {
        eventList[i] = new RecordEvent();
      }
      capacity = length+n;
    }
  }

  public void startLine(int startPos) {
    grow(1);
    eventList[length++].initialize(RecordEventType.START_LINE_EVENT, startPos, 0);
    this.start = startPos;
  }

  public void startRecord(int startDelim, int endDelim) {
    grow(2);
    eventList[length++].initialize(RecordEventType.DATA_EVENT, start, startDelim - start);
    eventList[length++].initialize(RecordEventType.START_RECORD_EVENT, startDelim, endDelim-startDelim);
    this.start = endDelim;
  }

  public void completeLine(int startDelim, int endDelim) {
    eventList[0].setDataType(RecordEventType.LINE_COMPLETION_EVENT_TYPE);
    endRecord(startDelim,endDelim);
    this.start = endDelim;
  }

  public void ignoreLine() {
    for (int i = 0; i < length; ++i) {
      eventList[i].setDataType(RecordEventType.IGNORABLE_WHITESPACE_EVENT);
    }
  }

  public void eol(int end) {
    eventList[0].setDataType(RecordEventType.LINE_COMPLETION_EVENT_TYPE);
    endRecord(end,end);
    this.start = end;
  }

  public void endRecord(int startDelim, int endDelim) {
    grow(2);
    eventList[length++].initialize(RecordEventType.DATA_EVENT, start, startDelim-start);
    eventList[length++].initialize(RecordEventType.END_RECORD_EVENT, startDelim, endDelim-startDelim);
    this.start = endDelim;
  }

  public void startComment(int startComment) {
    if (startComment > start) {
      grow(1);
      eventList[length++].initialize(RecordEventType.DATA_EVENT, start, startComment - start);
    }
    this.start = startComment;
  }

  public void endComment(int endComment) {
    grow(1);
    eventList[length++].initialize(RecordEventType.COMMENT_LINE_EVENT, start, endComment-start);
    this.start = endComment;
  }

  public void lineContinuation(int startPos, int endPos) {
    grow(2);
    eventList[length++].initialize(RecordEventType.DATA_EVENT, start, startPos-start);
    eventList[length++].initialize(RecordEventType.LINE_CONTINUATION_EVENT, startPos, endPos-startPos);
    this.start = endPos;
  }

  public void ignorableWhitespace(int startPos, int endPos) {
    grow(1);
    eventList[length++].initialize(RecordEventType.IGNORABLE_WHITESPACE_EVENT, start, endPos-start);
    this.start = endPos;
  }

  public String toString() {
    String s = "" + length + " ";
    for (int i = 0; i < length; ++i) {
      if (i > 0) {
        s += ",";
      }
      if (eventList[i] != null) {
        s += eventList[i].toString();
      } else {
        s += "event null";
      }
    }
    return s;
  }

  public boolean isEmpty() {
    boolean empty = true;
    for (int i = 0; empty && i < length; ++i) {
      if (eventList[i].length != 0) {
        empty = false;
      }
    }
    return length == 0;
  }
}



