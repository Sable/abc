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

package com.servingxml.components.flatfile.scanner.characters;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatContentReceiver;
import com.servingxml.components.flatfile.options.CharBuffer;
import com.servingxml.components.flatfile.options.CharTrimmer;
import com.servingxml.components.flatfile.options.CommentStarterCharChecker;
import com.servingxml.components.flatfile.options.CharDelimiterExtractor;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.QuoteSymbolCharChecker;
import com.servingxml.components.flatfile.scanner.FlatFileScanner;
import com.servingxml.util.CharArrayBuilder;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.helpers.InputStreamHelper;

public class LineDelimitedCharScanner implements FlatFileScanner {
  private final int headerRecordCount;
  private final int trailerRecordCount;
  private final CharDelimiterExtractor[] recordCharDelimiterExtractors;
  private final QuoteSymbolCharChecker quoteSymbolChecker;
  private final CommentStarterCharChecker commentStarterChecker;      
  private final CharTrimmer charTrimmer;
  private final boolean ignoreTrailingEmptyLines;
  private final boolean ignoreEmptyLines;
  private final RecordEventBufferQueue eventBufferQueue;
  private final RecordEventBufferQueue recycleQueue;
  private final Charset sourceCharset;

  private FlatContentReceiver receiver = null;
  private boolean inQuotes = false;

  public LineDelimitedCharScanner(int headerRecordCount, int trailerRecordCount, 
                                  FlatFileOptions flatFileOptions) {
    this.headerRecordCount = headerRecordCount;
    this.trailerRecordCount = trailerRecordCount;
    this.recordCharDelimiterExtractors = flatFileOptions.getRecordCharDelimiterExtractors();
    this.quoteSymbolChecker = flatFileOptions.getQuoteSymbolCharChecker();
    this.commentStarterChecker = flatFileOptions.getCommentStarterCharChecker();
    this.ignoreEmptyLines = flatFileOptions.isIgnoreEmptyLines();
    this.ignoreTrailingEmptyLines = flatFileOptions.isIgnoreEmptyLines() ? true : flatFileOptions.isIgnoreTrailingEmptyLines();
    this.charTrimmer = flatFileOptions.getCharTrimmer();
    this.eventBufferQueue = new RecordEventBufferQueue();
    this.recycleQueue = new RecordEventBufferQueue();
    this.sourceCharset = flatFileOptions.getCharset();
  }

  public void scan(ServiceContext context, Flow flow,
                   InputStream is, FlatContentReceiver receiver) {

    this.receiver = receiver;

    BufferedInputStream bufferedIn = new BufferedInputStream(is);
    Charset charset = sourceCharset;
    //if (charset != null) {
      //System.out.println(charset.name());
    //}
    charset = InputStreamHelper.skipBOM(bufferedIn,charset);
    if (charset == null) {
      charset = Charset.defaultCharset();
    }

    Reader reader = new InputStreamReader(bufferedIn, charset);
    BufferedReader bufferedReader = new BufferedReader(reader);
    CharBuffer recordBuffer = new CharBufferImpl(bufferedReader);

    receiver.startFlatFile();            
    readRecords(recordBuffer, charset);
    receiver.endFlatFile();
  }

  private void readRecords(CharBuffer recordBuffer, Charset charset) {
    if (!recordBuffer.done()) {
      try {
        if (headerRecordCount > 0) {
          receiver.startHeader();
          read(recordBuffer, headerRecordCount, charset);
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
          read(recordBuffer, lookAheadCount, charset);
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

      } catch (ServingXmlException e) {
        throw e;
      } catch (IOException e) {
        throw new ServingXmlException(e.getMessage(),e);
      } catch (Exception e) {
        throw new ServingXmlException(e.getMessage(),e);
      }
    }
    //System.out.println (getClass().getName()+".readRecords leave");
  }

  private void read(CharBuffer recordBuffer, int count, Charset charset) 
  throws IOException {
    read2(recordBuffer, count, charset);
    while (!recordBuffer.done() && eventBufferQueue.tail() != null && eventBufferQueue.tail().isEmptyLine(charTrimmer)) {
      read2(recordBuffer, eventBufferQueue.size()+1, charset);
    }
    if (ignoreTrailingEmptyLines) {
      while (!eventBufferQueue.isEmpty() && eventBufferQueue.tail().isEmptyLine(charTrimmer)) {
        recycleQueue.enqueue(eventBufferQueue.pop());
      }
    }
  }

  private void read2(CharBuffer recordBuffer, int count, Charset charset) 
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

          CharArrayBuilder charArrayBuilder = eventBuffer.charArrayBuilder();
          boolean eol = false;
          boolean done = false;
          boolean bol = true;                    
          recordBuffer.clear();
          recordBuffer.next();
          while (!recordBuffer.done() && !eol) {
            if (bol) {
              eventBuffer.startLine(charArrayBuilder.length());
              //System.out.println("before checkSpace pos=" + charArrayBuilder.length());
              int startPos = charArrayBuilder.length();
              charTrimmer.checkSpace(recordBuffer,charArrayBuilder);
              //System.out.println("after checkSpace pos=" + charArrayBuilder.length());
              if (commentStarterChecker.checkCommentSymbol(recordBuffer, charArrayBuilder)) {
                //System.out.println("Comment symbol found");
                eventBuffer.startComment(startPos);
                readCommentLine(recordBuffer, charArrayBuilder);
                eventBuffer.endComment(charArrayBuilder.length());
                continue;
              } else {
                bol = false;
              }
            }
            if (!recordBuffer.done() && !eol) {
              if (inQuotes && quoteSymbolChecker.foundEscapedQuoteSymbol(recordBuffer,charArrayBuilder)) {
              } else if (quoteSymbolChecker.foundQuoteSymbol(recordBuffer,charArrayBuilder)) {
                inQuotes = !inQuotes;
              } else if (inQuotes) {
                charArrayBuilder.append(recordBuffer.current());
                recordBuffer.next();
              } else {
                done = false; // new
                boolean recordDelimiterEscaped = false;
                for (int j = 0; !done && !recordDelimiterEscaped && j < recordCharDelimiterExtractors.length; ++j) {
                  CharDelimiterExtractor recordDelimiter = recordCharDelimiterExtractors[j];
                  int startPos = charArrayBuilder.length();
                  if (recordDelimiter.testStart(recordBuffer,charArrayBuilder)) {
                    eventBuffer.startRecord(startPos, charArrayBuilder.length());
                    readToEnd(recordBuffer, recordDelimiter, eventBuffer);
                    eol = true;
                    done = true;
                    eventBufferQueue.enqueue(eventBuffer);
                  } else if (recordDelimiter.readEscapedDelimiter(recordBuffer,charArrayBuilder)) {
                    recordDelimiterEscaped = true;
                  } else if (recordDelimiter.testContinuation(recordBuffer,charArrayBuilder)) {
                    done = true;
                    eventBuffer.lineContinuation(startPos, charArrayBuilder.length());
                  } else if (recordDelimiter.foundEndDelimiter(recordBuffer,charArrayBuilder)) {
                    eol = true;
                    done = true;
                    eventBuffer.completeLine(startPos,charArrayBuilder.length());
                    //System.out.println("Checking for empty line " + ignoreEmptyLines);
                    if (ignoreEmptyLines && eventBuffer.isEmptyLine(charTrimmer)) {
                      //System.out.println("Ignoring line!");
                      eventBuffer.ignoreLine();
                    }
                    eventBufferQueue.enqueue(eventBuffer);
                  }
                }
                if (!done) {
                  charArrayBuilder.append(recordBuffer.current());
                  recordBuffer.next();
                }
              }
            }
            if (recordBuffer.done() && !eol) {
              if (!eventBuffer.isEmpty()) {
                eventBuffer.eol(charArrayBuilder.length());
                eventBufferQueue.enqueue(eventBuffer);
              }
            }
          }
        }
      }
    }
  }

  private void readToEnd(CharBuffer recordBuffer, CharDelimiterExtractor recordDelimiter, RecordEventBuffer eventBuffer) throws IOException {

    readToEnd(recordBuffer, recordDelimiter, eventBuffer, 1);
  }

  private void readToEnd(CharBuffer recordBuffer, CharDelimiterExtractor recordDelimiter, 
                         RecordEventBuffer eventBuffer, int level) 
  throws IOException {
    CharArrayBuilder charArrayBuilder = eventBuffer.charArrayBuilder();
    boolean bol = false;

    boolean eol = false;
    while (!recordBuffer.done() && !eol) {
      int startPos = charArrayBuilder.length();
      if (bol) {
        charTrimmer.checkSpace(recordBuffer,charArrayBuilder);
        if (commentStarterChecker.checkCommentSymbol(recordBuffer, charArrayBuilder)) {
          //System.out.println("Comment symbol found");
          eventBuffer.startComment(startPos);
          readCommentLine(recordBuffer, charArrayBuilder);
          eventBuffer.endComment(charArrayBuilder.length());
          continue;
        }
        bol = false;
      }
      if (inQuotes && quoteSymbolChecker.foundEscapedQuoteSymbol(recordBuffer,charArrayBuilder)) {
      } else if (quoteSymbolChecker.foundQuoteSymbol(recordBuffer,charArrayBuilder)) {
        inQuotes = !inQuotes;               
      } else if (inQuotes) {
        charArrayBuilder.append(recordBuffer.current());
        recordBuffer.next();
      } else if (recordDelimiter.testStart(recordBuffer,charArrayBuilder)) {
        readToEnd(recordBuffer, recordDelimiter, eventBuffer, level+1);
      } else if (level > 1 && recordDelimiter.foundEndDelimiter(recordBuffer,charArrayBuilder)) {
        eol = true;
      } else if (level == 1 && recordDelimiter.foundEndDelimiter(recordBuffer,charArrayBuilder)) {
        //System.out.println (getClass().getName()+".readToEnd before endRecord");
        //System.out.println(getClass().getName()+"readToEnd foundEndDelimiter true");
        eventBuffer.endRecord(startPos,charArrayBuilder.length());
        //System.out.println (getClass().getName()+".readToEnd after endRecord");
        eol = true;
      } else {
        boolean foundEol = false; // new
        for (int j = 0; !foundEol && j < recordCharDelimiterExtractors.length; ++j) {
          CharDelimiterExtractor eolDelimiter = recordCharDelimiterExtractors[j];
          if (eolDelimiter.foundEndDelimiter(recordBuffer,charArrayBuilder)) {
            foundEol = true;
            bol = true;
          }
        }
        if (!foundEol) {
          charArrayBuilder.append(recordBuffer.current());
          recordBuffer.next();
        }
      }
    }
  }

  private void readCommentLine(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    int startComment = charArrayBuilder.length();
    boolean eol = false;
    while (!recordBuffer.done() && !eol) {
      int startPos = charArrayBuilder.length();
      for (int j = 0; !eol && j < recordCharDelimiterExtractors.length; ++j) {
        CharDelimiterExtractor recordDelimiter = recordCharDelimiterExtractors[j];
        if (recordDelimiter.foundEndDelimiter(recordBuffer,charArrayBuilder)) {
          eol = true;
        }
      }
      if (!eol) {
        charArrayBuilder.append(recordBuffer.current());
        recordBuffer.next();
      }
    }
  }
}


