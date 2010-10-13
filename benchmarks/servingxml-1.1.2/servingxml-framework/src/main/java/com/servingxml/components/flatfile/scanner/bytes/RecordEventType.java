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

import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.ByteTrimmer;
import com.servingxml.components.flatfile.FlatContentReceiver;

abstract class RecordEventType {
  static RecordEventType IGNORABLE_WHITESPACE_EVENT = new IgnorableWhitespaceEventType();
  static RecordEventType COMMENT_LINE_EVENT = new CommentLineEventType();
  static RecordEventType START_LINE_EVENT = new StartLineEventType();
  static RecordEventType DATA_EVENT = new DataEventType();
  static RecordEventType LINE_CONTINUATION_EVENT = new LineContinuationEventType();
  static RecordEventType START_RECORD_EVENT = new StartRecordEventType();
  static RecordEventType END_RECORD_EVENT = new EndRecordEventType();
  static RecordEventType LINE_COMPLETION_EVENT_TYPE = new LineCompletionEventType();

  abstract void write(byte[] bytes, int start, int length, Charset charset, FlatContentReceiver receiver) 
  ;

  abstract boolean isEmptyLine(byte[] bytes, int start, int length, ByteTrimmer byteTrimmer);

  static final class StartLineEventType extends RecordEventType {
    final void write(byte[] buffer, int start, int length, Charset charset, FlatContentReceiver receiver) {
    }

    final boolean isEmptyLine(byte[] bytes, int start, int length, ByteTrimmer byteTrimmer) {
      return true;
    }
  }

  static final class DataEventType extends RecordEventType {
    final void write(byte[] buffer, int start, int length, Charset charset, FlatContentReceiver receiver) {
      RecordInput recordInput = new ByteRecordInput(buffer, start, length, charset);
      receiver.data(recordInput);
    }

    final boolean isEmptyLine(byte[] bytes, int start, int length, ByteTrimmer byteTrimmer) {
      boolean empty;
      //System.out.println(getClass().getName()+".isEmptyLine:  Data: " + new String(bytes,start,length));
      if (length == 0) {
        empty = true;
      } else if (byteTrimmer.countLeadingWhitespace(bytes,start,length) == length) {
        empty = true;
      } else {
        empty = false;
      }
      //System.out.println(getClass().getName()+".isEmptyLine = " + empty);
      return empty;
    }
  }

  static final class LineContinuationEventType extends RecordEventType {
    final void write(byte[] buffer, int start, int length, Charset charset, FlatContentReceiver receiver) {
      receiver.lineContinuation(buffer, start, length);
    }

    final boolean isEmptyLine(byte[] bytes, int start, int length, ByteTrimmer byteTrimmer) {
      return true;
    }
  }

  static final class IgnorableWhitespaceEventType extends RecordEventType {
    final void write(byte[] buffer, int start, int length, Charset charset, FlatContentReceiver receiver) {
      receiver.ignorableWhitespace(buffer, start, length);
    }

    final boolean isEmptyLine(byte[] bytes, int start, int length, ByteTrimmer byteTrimmer) {
      return true;
    }
  }

  static final class StartRecordEventType extends RecordEventType {
    final void write(byte[] buffer, int start, int length, Charset charset, FlatContentReceiver receiver) {
      receiver.startRecord();
      if (length > 0) {
        receiver.delimiter(buffer, start, length);
      }
    }

    final boolean isEmptyLine(byte[] bytes, int start, int length, ByteTrimmer byteTrimmer) {
      return true;
    }
  }

  static final class CommentLineEventType extends RecordEventType {
    final void write(byte[] buffer, int start, int length, Charset charset, FlatContentReceiver receiver) {
      receiver.commentLine(buffer, start, length);
    }

    final boolean isEmptyLine(byte[] bytes, int start, int length, ByteTrimmer byteTrimmer) {
      return false;
    }
  }

  static final class EndRecordEventType extends RecordEventType {
    final void write(byte[] buffer, int start, int length, Charset charset, FlatContentReceiver receiver) {
      if (length > 0) {
        receiver.delimiter(buffer, start, length);
      }
      //System.out.println (getClass().getName()+".write before endRecord");
      receiver.endRecord();
      //System.out.println (getClass().getName()+".write after endRecord");
    }

    final boolean isEmptyLine(byte[] bytes, int start, int length, ByteTrimmer byteTrimmer) {
      return true;
    }
  }

  static final class LineCompletionEventType extends RecordEventType {
    final void write(byte[] buffer, int start, int length, Charset charset, FlatContentReceiver receiver) {
      receiver.startRecord();
      RecordInput recordInput = new ByteRecordInput(buffer, start, length, charset);
      receiver.data(recordInput);
    }

    final boolean isEmptyLine(byte[] bytes, int start, int length, ByteTrimmer byteTrimmer) {
      boolean empty;
      if (length == 0) {
        empty = true;
      } else if (byteTrimmer.countLeadingWhitespace(bytes,start,length) == length) {
        empty = true;
      } else {
        empty = false;
      }
      return empty;
    }
  }
}



