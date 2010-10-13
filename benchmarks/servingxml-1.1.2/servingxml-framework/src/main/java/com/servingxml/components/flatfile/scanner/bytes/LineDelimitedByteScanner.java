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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatContentReceiver;
import com.servingxml.components.flatfile.options.ByteBuffer;
import com.servingxml.components.flatfile.options.ByteTrimmer;
import com.servingxml.components.flatfile.options.CommentStarterByteChecker;
import com.servingxml.components.flatfile.options.ByteDelimiterExtractor;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.QuoteSymbolByteChecker;
import com.servingxml.components.flatfile.scanner.FlatFileScanner;
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.ServingXmlException;

public class LineDelimitedByteScanner implements FlatFileScanner {
  private final int headerRecordCount;
  private final int trailerRecordCount;
  private final ByteDelimiterExtractor[] recordDelimiterExtractors;
  private final QuoteSymbolByteChecker quoteSymbolChecker;
  private final CommentStarterByteChecker commentStarterChecker;      
  private final ByteTrimmer byteTrimmer;
  private final boolean ignoreTrailingEmptyLines;
  private final boolean ignoreEmptyLines;
  private final RecordEventBufferQueue eventBufferQueue;
  private final RecordEventBufferQueue recycleQueue;
  private final Charset charset;

  private FlatContentReceiver receiver = null;
  private boolean inQuotes = false;

  public LineDelimitedByteScanner(int headerRecordCount, int trailerRecordCount, 
                                   FlatFileOptions flatFileOptions) {
    this.headerRecordCount = headerRecordCount;
    this.trailerRecordCount = trailerRecordCount;
    this.recordDelimiterExtractors = flatFileOptions.getRecordByteDelimiterExtractors();
    this.quoteSymbolChecker = flatFileOptions.getQuoteSymbolByteChecker();
    this.commentStarterChecker = flatFileOptions.getCommentStarterByteChecker();
    this.ignoreEmptyLines = flatFileOptions.isIgnoreEmptyLines();
    this.ignoreTrailingEmptyLines = flatFileOptions.isIgnoreEmptyLines() ? true : flatFileOptions.isIgnoreTrailingEmptyLines();
    this.byteTrimmer = flatFileOptions.getByteTrimmer();
    this.eventBufferQueue = new RecordEventBufferQueue();
    this.recycleQueue = new RecordEventBufferQueue();
    this.charset = flatFileOptions.getDefaultCharset();
  }

  public void scan(ServiceContext context, Flow flow,
                   InputStream is, FlatContentReceiver receiver) {

    this.receiver = receiver;
    //System.out.println(getClass().getName()+".scan");
    ByteBuffer recordBuffer = new ByteBufferImpl(new BufferedInputStream(is));

    receiver.startFlatFile();            
    readRecords(recordBuffer);
    receiver.endFlatFile();
  }

  private void readRecords(ByteBuffer recordBuffer) {
    if (!recordBuffer.done()) {
      try {
        if (headerRecordCount > 0) {
          receiver.startHeader();
          read(recordBuffer, headerRecordCount);
          for (int i = 0; !eventBufferQueue.isEmpty() && i < headerRecordCount; ++i) {
            RecordEventBuffer eventBuffer = eventBufferQueue.dequeue();
            eventBuffer.write(receiver);
            recycleQueue.enqueue(eventBuffer);
          }
          receiver.endHeader();
        }

        int lookAheadCount = trailerRecordCount + 1;

        receiver.startBody();

        boolean done = false;
        while (!done) {
          read(recordBuffer, lookAheadCount);
          if (eventBufferQueue.size() <= trailerRecordCount) {
            done = true;
          } else {
            RecordEventBuffer eventBuffer = eventBufferQueue.dequeue();
            eventBuffer.write(receiver);
            recycleQueue.enqueue(eventBuffer);
          }
        }
        receiver.endBody();
        if (trailerRecordCount > 0) {
          receiver.startTrailer();
          for (int i = 0; !eventBufferQueue.isEmpty() && i < trailerRecordCount; ++i) {
            RecordEventBuffer eventBuffer = eventBufferQueue.dequeue();
            eventBuffer.write(receiver);
            recycleQueue.enqueue(eventBuffer);
          }
          receiver.endTrailer();
        }

      } catch (IOException e) {
        throw new ServingXmlException(e.getMessage(), e);
      } catch (Exception e) {
        throw new ServingXmlException(e.getMessage(), e);
      }
    }
    //System.out.println (getClass().getName()+".readRecords leave");
  }

  private void read(ByteBuffer recordBuffer, int count) 
  throws IOException {
    read2(recordBuffer, count);
    while (!recordBuffer.done() && eventBufferQueue.tail() != null && eventBufferQueue.tail().isEmptyLine(byteTrimmer)) {
      read2(recordBuffer, eventBufferQueue.size()+1);
    }
    if (ignoreTrailingEmptyLines) {
      while (!eventBufferQueue.isEmpty() && eventBufferQueue.tail().isEmptyLine(byteTrimmer)) {
        recycleQueue.enqueue(eventBufferQueue.pop());
      }
    }
  }

  private void read2(ByteBuffer recordBuffer, int count) 
  throws IOException {

    int linesNeeded = count - eventBufferQueue.size();

    if (!recordBuffer.done()) {
      if (linesNeeded > 0) {
        for (int i = 0; !recordBuffer.done() && i < linesNeeded; ++i) {
          RecordEventBuffer eventBuffer;
          if (!recycleQueue.isEmpty()) {
            eventBuffer = recycleQueue.dequeue();
            eventBuffer.clear();
          } else {
            eventBuffer = new RecordEventBuffer(charset);
          }

          ByteArrayBuilder byteArrayBuilder = eventBuffer.byteArrayBuilder();
          boolean eol = false;
          boolean done = false;
          boolean bol = true;                    
          recordBuffer.clear();
          recordBuffer.next();
          while (!recordBuffer.done() && !eol) {
            if (bol) {
              eventBuffer.startLine(byteArrayBuilder.length());
              //System.out.println("before checkSpace pos=" + byteArrayBuilder.length());
              int startPos = byteArrayBuilder.length();
              byteTrimmer.checkSpace(recordBuffer,byteArrayBuilder);
              //System.out.println("after checkSpace pos=" + byteArrayBuilder.length());
              if (commentStarterChecker.checkCommentSymbol(recordBuffer, byteArrayBuilder)) {
                //System.out.println("Comment symbol found");
                eventBuffer.startComment(startPos);
                readCommentLine(recordBuffer, byteArrayBuilder);
                eventBuffer.endComment(byteArrayBuilder.length());
                continue;
              } else {
                bol = false;
              }
            }
            if (!recordBuffer.done() && !eol) {
              if (inQuotes && quoteSymbolChecker.foundEscapedQuoteSymbol(recordBuffer,byteArrayBuilder)) {
              } else if (quoteSymbolChecker.foundQuoteSymbol(recordBuffer,byteArrayBuilder)) {
                inQuotes = !inQuotes;
              } else if (inQuotes) {
                byteArrayBuilder.append(recordBuffer.current());
                recordBuffer.next();
              } else {
                done = false; // new
                for (int j = 0; !done && j < recordDelimiterExtractors.length; ++j) {
                  ByteDelimiterExtractor recordDelimiter = recordDelimiterExtractors[j];
                  int startPos = byteArrayBuilder.length();
                  if (recordDelimiter.testStart(recordBuffer,byteArrayBuilder)) {
                    eventBuffer.startRecord(startPos, byteArrayBuilder.length());
                    readToEnd(recordBuffer, recordDelimiter, eventBuffer);
                    eol = true;
                    done = true;
                    eventBufferQueue.enqueue(eventBuffer);
                  } else if (recordDelimiter.testContinuation(recordBuffer,byteArrayBuilder)) {
                    done = true;
                    eventBuffer.lineContinuation(startPos, byteArrayBuilder.length());
                  } else if (recordDelimiter.foundEndDelimiter(recordBuffer,byteArrayBuilder)) {
                    eol = true;
                    done = true;
                    eventBuffer.completeLine(startPos,byteArrayBuilder.length());
                    //System.out.println("Checking for empty line " + ignoreEmptyLines);
                    if (ignoreEmptyLines && eventBuffer.isEmptyLine(byteTrimmer)) {
                      //System.out.println("Ignoring line!");
                      eventBuffer.ignoreLine();
                    }
                    eventBufferQueue.enqueue(eventBuffer);
                  }
                }
                if (!done) {
                  byteArrayBuilder.append(recordBuffer.current());
                  recordBuffer.next();
                }
              }
            }
            if (recordBuffer.done() && !eol) {
              if (!eventBuffer.isEmpty()) {
                eventBuffer.eol(byteArrayBuilder.length());
                eventBufferQueue.enqueue(eventBuffer);
              }
            }
          }
        }
      }
    }
  }

  private void readToEnd(ByteBuffer recordBuffer, ByteDelimiterExtractor recordDelimiter, RecordEventBuffer eventBuffer) throws IOException {

    readToEnd(recordBuffer, recordDelimiter, eventBuffer, 1);
  }

  private void readToEnd(ByteBuffer recordBuffer, ByteDelimiterExtractor recordDelimiter, 
                         RecordEventBuffer eventBuffer, int level) 
  throws IOException {
    ByteArrayBuilder byteArrayBuilder = eventBuffer.byteArrayBuilder();
    boolean bol = false;

    boolean eol = false;
    while (!recordBuffer.done() && !eol) {
      int startPos = byteArrayBuilder.length();
      if (bol) {
        byteTrimmer.checkSpace(recordBuffer,byteArrayBuilder);
        if (commentStarterChecker.checkCommentSymbol(recordBuffer, byteArrayBuilder)) {
          //System.out.println("Comment symbol found");
          eventBuffer.startComment(startPos);
          readCommentLine(recordBuffer, byteArrayBuilder);
          eventBuffer.endComment(byteArrayBuilder.length());
          continue;
        }
        bol = false;
      }
      if (inQuotes && quoteSymbolChecker.foundEscapedQuoteSymbol(recordBuffer,byteArrayBuilder)) {
      } else if (quoteSymbolChecker.foundQuoteSymbol(recordBuffer,byteArrayBuilder)) {
        inQuotes = !inQuotes;               
      } else if (inQuotes) {
        byteArrayBuilder.append(recordBuffer.current());
        recordBuffer.next();
      } else if (recordDelimiter.testStart(recordBuffer,byteArrayBuilder)) {
        readToEnd(recordBuffer, recordDelimiter, eventBuffer, level+1);
      } else if (level > 1 && recordDelimiter.foundEndDelimiter(recordBuffer,byteArrayBuilder)) {
        eol = true;
      } else if (level == 1 && recordDelimiter.foundEndDelimiter(recordBuffer,byteArrayBuilder)) {
        //System.out.println (getClass().getName()+".readToEnd before endRecord");
        //System.out.println(getClass().getName()+"readToEnd foundEndDelimiter true");
        eventBuffer.endRecord(startPos,byteArrayBuilder.length());
        //System.out.println (getClass().getName()+".readToEnd after endRecord");
        eol = true;
      } else {
        boolean foundEol = false; // new
        for (int j = 0; !foundEol && j < recordDelimiterExtractors.length; ++j) {
          ByteDelimiterExtractor eolDelimiter = recordDelimiterExtractors[j];
          if (eolDelimiter.foundEndDelimiter(recordBuffer,byteArrayBuilder)) {
            foundEol = true;
            bol = true;
          }
        }
        if (!foundEol) {
          byteArrayBuilder.append(recordBuffer.current());
          recordBuffer.next();
        }
      }
    }
  }

  private void readCommentLine(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {
    int startComment = byteArrayBuilder.length();
    boolean eol = false;
    while (!recordBuffer.done() && !eol) {
      int startPos = byteArrayBuilder.length();
      for (int j = 0; !eol && j < recordDelimiterExtractors.length; ++j) {
        ByteDelimiterExtractor recordDelimiter = recordDelimiterExtractors[j];
        if (recordDelimiter.foundEndDelimiter(recordBuffer,byteArrayBuilder)) {
          eol = true;
        }
      }
      if (!eol) {
        byteArrayBuilder.append(recordBuffer.current());
        recordBuffer.next();
      }
    }
  }
}


