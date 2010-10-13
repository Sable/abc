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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordReceiver;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.ByteDelimiterExtractor;
import com.servingxml.components.flatfile.options.ByteTrimmer;
import com.servingxml.components.flatfile.options.DelimiterExtractor;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.QuoteSymbolByteChecker;
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.CharsetHelper;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.RecordReceiver;

public class ByteRecordInput implements RecordInput {
  private final byte[] data;
  private final int start;
  private final int length;
  private int inputOffset;
  private int endIndex;
  private final Charset charset;

  public ByteRecordInput(byte[] data, int start, int length, Charset charset) {
    //System.out.println(getClass().getName()+".cons start="+start+",length="+length);

    this.data = data;
    this.start = start;
    this.length = length;
    this.inputOffset = 0;
    this.endIndex = 0;
    this.charset = (charset == null) ? Charset.defaultCharset() : charset;
    //System.out.println(charset.toString());
  }

  public ByteRecordInput(byte[] data, Charset charset) {
    this.data = data;
    this.start = 0;
    this.length = data.length;
    this.inputOffset = 0;
    this.endIndex = 0;
    this.charset = (charset == null) ? Charset.defaultCharset() : charset;
  }

  public byte[] toByteArray() {
    byte[] newData = new byte[length];
    System.arraycopy(data,start,newData,0,length);
    return newData;
  }

  public char[] toCharArray() {
    return CharsetHelper.bytesToCharacters(data, start, length, charset);
  }

  public boolean done() {
    return inputOffset >= length - start;
  }

  public int readBytes(byte[] value) {
    int len = value.length <= length - inputOffset ? value.length : length - inputOffset;
    if (len > 0) {
      System.arraycopy(data, start+inputOffset, value, 0, len);
      inputOffset += len;
    }
    updateLast();
    return len;
  }

  public String readString(int width) {
    //System.out.println(getClass().getName()+".readString enter");
    int len = width <= length - inputOffset ? width : length - inputOffset;
    String s;
    if (len > 0) {
      //System.out.println(getClass().getName()+".readString width="+width+", length="+length+", inputOffset="
      //  +inputOffset + ", len="+len);
      s = CharsetHelper.bytesToString(data, start+inputOffset, len, charset);
      inputOffset += len;
    } else {
      s = "";
    }
    updateLast();
    //System.out.println(getClass().getName()+".readString inputOffset="+inputOffset);
    return s;
  }

  public String readString(int maxLength, FlatFileOptions flatFileOptions) 
  throws IOException {
    QuoteSymbolByteChecker quoteSymbolChecker = flatFileOptions.getQuoteSymbolByteChecker();
    ByteDelimiterExtractor[] fieldDelimiterExtractors = flatFileOptions.getFieldByteDelimiterExtractors();
    boolean omitFinalFieldDelimiter = flatFileOptions.isOmitFinalFieldDelimiter();
    boolean trimLeading = flatFileOptions.isTrimLeading();
    boolean trimTrailing = flatFileOptions.isTrimTrailing();
    ByteTrimmer byteTrimmer = flatFileOptions.getByteTrimmer();

    int maxLen = (maxLength >= 0 && maxLength <= length - inputOffset) ? maxLength : length - inputOffset;
    int end = inputOffset + maxLen;

    boolean inQuotes = false;
    boolean delimiterFound = false;

    ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();

    //  Initialize endFieldPosition
    int endFieldPosition = length;
    if (maxLength >= 0) {
      endFieldPosition = inputOffset+maxLength < length ? inputOffset+maxLength : length;
    }

    //  Initialize leadingCount and leftLimitTrailingSpace
    int leadingCount = 0;
    if (trimLeading) {
      leadingCount = byteTrimmer.countLeadingWhitespace(data, start+inputOffset, endFieldPosition-inputOffset);
    }
    int leftLimitTrailingSpace = leadingCount;

    while (!delimiterFound && inputOffset < end) {
      int n = 0;
      if (inQuotes) {
        n = quoteSymbolChecker.foundEscapedQuoteSymbol(data, start+inputOffset, end-inputOffset);
      }
      if (n > 0) {
        inputOffset += (n - quoteSymbolChecker.length());
        byteArrayBuilder.append(data, start+inputOffset, quoteSymbolChecker.length());
        inputOffset += quoteSymbolChecker.length();
      } else {
        n = quoteSymbolChecker.foundQuoteSymbol(data, start+inputOffset, end-inputOffset);
        if (n > 0) {
          //System.out.println("readQuotedField-foundQuoteSymbol " + new String(data,start+inputOffset,length-inputOffset));
          inQuotes = !inQuotes;
          inputOffset += n;
        } else if (inQuotes) {
          byteArrayBuilder.append(data[start+inputOffset]);
          //System.out.println("Appending " + new String(data,start+inputOffset,1) + ", buflen=" + byteArrayBuilder.length());
          ++inputOffset;
          leftLimitTrailingSpace = byteArrayBuilder.length();
        } else {
          for (int i = 0; !delimiterFound && i < fieldDelimiterExtractors.length; ++i) {
            int delimiterLength = fieldDelimiterExtractors[i].foundEndDelimiter(data, start+inputOffset, end-inputOffset);
            if (delimiterLength > 0) {
              inputOffset += delimiterLength;
              delimiterFound = true;
            }
          }
          if (!delimiterFound) {
            byteArrayBuilder.append(data[start+inputOffset]);
            //System.out.println("Appending " + new String(data,start+inputOffset,1) + ", buflen=" + byteArrayBuilder.length());
            ++inputOffset;
          }
        }
      }
    }
    String value = null;
    if (delimiterFound || omitFinalFieldDelimiter) {
      value = "";
      int len = byteArrayBuilder.length() - leadingCount;
      int trailingCount = 0;
      if (trimTrailing) {
        trailingCount = byteTrimmer.countTrailingWhitespace(byteArrayBuilder.buffer(), leftLimitTrailingSpace, 
                                                           byteArrayBuilder.length()-leftLimitTrailingSpace);
        //System.out.println(getClass().getName() + ".readField "
        //  + "leftLimitTrailingSpace="+leftLimitTrailingSpace 
        //  + ", buflen=" + byteArrayBuilder.length()
        //  + ", trailingCount=" + trailingCount
        //  + ", leadingCount=" + leadingCount);
      }
      if (len-trailingCount > 0) {
        value = CharsetHelper.bytesToString(byteArrayBuilder.buffer(), leadingCount, len-trailingCount, charset);
        //System.out.println(getClass().getName() + ".readField "
        // + ".readField buffer=" + new String(byteArrayBuilder.buffer()) + "." 
        // + ", value=" + value + "." + " leadingCount = " + leadingCount + ", len = " + len + ", trailingCount= " + trailingCount);
        //System.out.println("value = " + value);
        //value = StringHelper.trim(value, false, trimTrailing); 
      }
    }
    updateLast();
    return value;
  }

  public String[] readStringArray(int maxLength, FlatFileOptions flatFileOptions) 
  throws IOException {
    QuoteSymbolByteChecker quoteSymbolChecker = flatFileOptions.getQuoteSymbolByteChecker();
    ByteDelimiterExtractor[] fieldDelimiterExtractors = flatFileOptions.getFieldByteDelimiterExtractors();
    ByteDelimiterExtractor[] subfieldDelimiterExtractors = flatFileOptions.getSubfieldByteDelimiterExtractors();
    boolean omitFinalFieldDelimiter = flatFileOptions.isOmitFinalFieldDelimiter();
    boolean trimLeading = flatFileOptions.isTrimLeading();
    boolean trimTrailing = flatFileOptions.isTrimTrailing();
    ByteTrimmer byteTrimmer = flatFileOptions.getByteTrimmer();

    int maxLen = (maxLength >= 0 && maxLength <= length - inputOffset) ? maxLength : length - inputOffset;
    int end = inputOffset + maxLen;

    boolean inQuotes = false;
    boolean delimiterFound = false;

    ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
    ArrayList<String> valueList = new ArrayList<String>();

    //  Initialize endFieldPosition
    int endFieldPosition = length;
    if (maxLength >= 0) {
      endFieldPosition = inputOffset+maxLength < length ? inputOffset+maxLength : length;
    }

    //  Initialize leadingCount and leftLimitTrailingSpace
    int leadingCount = 0;
    if (trimLeading) {
      leadingCount = byteTrimmer.countLeadingWhitespace(data, start+inputOffset, endFieldPosition-inputOffset);
    }
    int leftLimitTrailingSpace = leadingCount;

    while (!delimiterFound && inputOffset < end) {
      int n = 0;
      if (inQuotes) {
        n = quoteSymbolChecker.foundEscapedQuoteSymbol(data, start+inputOffset, end-inputOffset);
      }
      if (n > 0) {
        inputOffset += (n-quoteSymbolChecker.length());
        byteArrayBuilder.append(data, start+inputOffset, quoteSymbolChecker.length());
        inputOffset += quoteSymbolChecker.length();
      } else {
        n = quoteSymbolChecker.foundQuoteSymbol(data, start+inputOffset, end-inputOffset);
        if (n > 0) {
          //System.out.println("readQuotedField-foundQuoteSymbol " + new String(data,start+inputOffset,length-inputOffset));
          inQuotes = !inQuotes;
          inputOffset += n;
        } else if (inQuotes) {
          byteArrayBuilder.append(data[start+inputOffset]);
          //System.out.println("Appending " + new String(data,start+inputOffset,1) + ", buflen=" + byteArrayBuilder.length());
          ++inputOffset;
          leftLimitTrailingSpace = byteArrayBuilder.length();
        } else {
          for (int i = 0; !delimiterFound && i < fieldDelimiterExtractors.length; ++i) {
            int delimiterLength = fieldDelimiterExtractors[i].foundEndDelimiter(data, start+inputOffset, end-inputOffset);
            if (delimiterLength > 0) {
              inputOffset += delimiterLength;
              delimiterFound = true;
            }
          }
          if (!delimiterFound) {
            boolean subfieldDone = false;
            for (int i = 0; !subfieldDone && i < subfieldDelimiterExtractors.length; ++i) {
              int delimiterLength = subfieldDelimiterExtractors[i].foundEndDelimiter(data, start+inputOffset, end-inputOffset);
              if (delimiterLength > 0) {
                //String s;
                //if (byteArrayBuilder.length() > 0) {
                //  s = CharsetHelper.bytesToString(byteArrayBuilder.buffer(), byteArrayBuilder.start(),
                //        byteArrayBuilder.length(), charset);
                //} else {
                //  s = "";
                //}
                if (byteArrayBuilder.length() > 0) {
                  //System.out.println(getClass().getName()+" addingValue");
                  extractValue(byteTrimmer,trimTrailing,leadingCount,leftLimitTrailingSpace,byteArrayBuilder,valueList);
                } else {
                  // Adjacent delimiters, add empty value 
                  //System.out.println(getClass().getName()+" adding empty value");
                  valueList.add("");
                }
                byteArrayBuilder.clear();
                inputOffset += delimiterLength;
                if (trimLeading) {
                  leadingCount = byteTrimmer.countLeadingWhitespace(data, start+inputOffset, 
                                                                   endFieldPosition-inputOffset);
                }
                leftLimitTrailingSpace = leadingCount;
                subfieldDone = true;
              }
            }
            if (!subfieldDone) {
              byteArrayBuilder.append(data[start+inputOffset]);
              ++inputOffset;
            }
          }
        }
      }
    }

    String[] values = null;
    if (delimiterFound || omitFinalFieldDelimiter) {
      if (byteArrayBuilder.length() > 0) {
        //String s = CharsetHelper.bytesToString(byteArrayBuilder.buffer(), byteArrayBuilder.start(),
        //             byteArrayBuilder.length(), charset );
        //valueList.add(s);
        extractValue(byteTrimmer,trimTrailing,leadingCount,leftLimitTrailingSpace,byteArrayBuilder,valueList);
      }
      values = new String[valueList.size()];
      values = (String[])valueList.toArray(values); 
    }
    //System.out.println(getClass().getName()+" value count="+values.length);
    updateLast();
    return values;
  }

  private void extractValue(ByteTrimmer byteTrimmer, boolean trimTrailing, int leadingCount, 
                            int leftLimitTrailingSpace, ByteArrayBuilder byteArrayBuilder, List<String> valueList) {
    String value="";
    int len = byteArrayBuilder.length() - leadingCount;
    int trailingCount = 0;
    if (trimTrailing) {
      trailingCount = byteTrimmer.countTrailingWhitespace(byteArrayBuilder.buffer(), leftLimitTrailingSpace, 
                                                         byteArrayBuilder.length()-leftLimitTrailingSpace);
      //System.out.println("leftLimitTrailingSpace="+leftLimitTrailingSpace + ", buflen=" + byteArrayBuilder.length()
      //  + ", trailingCount=" + trailingCount);
    }
    if (len-trailingCount > 0) {
      value = CharsetHelper.bytesToString(byteArrayBuilder.buffer(), leadingCount, len-trailingCount, charset);
      //System.out.println("value = " + value);
      //value = StringHelper.trim(value, false, trimTrailing); 
    }
    valueList.add(value);
  }

  public int getPosition() {
    return inputOffset;
  }

  public int getLast() {
    return endIndex;
  }

  public void setPosition(int index) {
    //System.out.println(getClass()+".setPosition index="+index);
    if (index >= 0) {
      if (index <= length) {
        inputOffset = index;
      } else {
        inputOffset = length;
      }
      updateLast();
    }
  }

  public void updateLast() {
    if (endIndex < inputOffset) {
      endIndex = inputOffset;
    }
  }

  public Charset getCharset() {
    return charset;
  }

  public void wipe() throws IOException {
  }

  public RecordInput readSegment(int segmentLength) {
    byte[] data = new byte[segmentLength];
    int length = readBytes(data);
    RecordInput segmentInput = new ByteRecordInput(data, 0, length, getCharset());
    return segmentInput;
  }

  public RecordInput readSegment(FlatFileOptions flatFileOptions) {

    QuoteSymbolByteChecker quoteSymbolChecker = flatFileOptions.getQuoteSymbolByteChecker();
    ByteDelimiterExtractor[] segmentDelimiterExtractors = flatFileOptions.getSegmentByteDelimiterExtractors();
    ByteTrimmer byteTrimmer = flatFileOptions.getByteTrimmer();

    try {
      ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
      boolean inQuotes = false;
      boolean endDelimiterFound = false;

      int counter = 0;
      while (!endDelimiterFound && inputOffset < length) {
        //System.out.println("inQuotes=" + inQuotes);
        int n = 0;
        if (inQuotes) {
          n = quoteSymbolChecker.foundEscapedQuoteSymbol(data, start+inputOffset, length-inputOffset);
        }
        if (n > 0) {
          int len = n;
          byteArrayBuilder.append(data, start+inputOffset, len);
          inputOffset += len;
        } else {
          n = quoteSymbolChecker.foundQuoteSymbol(data, start+inputOffset, length-inputOffset);
          //System.out.println(new String(data,start+inputOffset,length-inputOffset) + n);
          if (n > 0) {
            inQuotes = !inQuotes;
            byteArrayBuilder.append(data, start+inputOffset, n);
            inputOffset += n;
            // Need to leave quotes in here, for field reader
          } else if (inQuotes) {
            byteArrayBuilder.append(data[start+inputOffset]);
            //System.out.println("In quotes, buffering");
            ++inputOffset;
          } else {
            if (!endDelimiterFound) {
              for (int j = 0; !endDelimiterFound && j < segmentDelimiterExtractors.length; ++j) {
                ByteDelimiterExtractor segmentDelimiter = segmentDelimiterExtractors[j];
                int delimiterLength = segmentDelimiter.testStart(data, start+inputOffset, length-inputOffset);
                if (delimiterLength > 0) {
                  inputOffset += delimiterLength;
                  readToEndOfSegment(segmentDelimiter,byteArrayBuilder);
                  endDelimiterFound = true;
                } else {
                  delimiterLength = segmentDelimiter.foundEndDelimiter(data, start+inputOffset, length-inputOffset);
                  if (delimiterLength > 0) {
                    //System.out.println(getClass().getName()+".readField found repeat delimiter counter="+counter+", count=" +count);
                    inputOffset += delimiterLength;
                    endDelimiterFound = true;
                    ++counter;                         
                  }
                }
              }
              if (!endDelimiterFound) {
                byteArrayBuilder.append(data[start+inputOffset]);
                ++inputOffset;
              }
            }
          }
        }
      }
      RecordInput recordInput = new ByteRecordInput(byteArrayBuilder.toByteArray(),
                                                             charset);
      return recordInput;
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }


  public void readToEndOfSegment(ByteDelimiterExtractor segmentDelimiter, ByteArrayBuilder byteArrayBuilder) 
  throws IOException {

    //ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
    int level = 1;

    //System.out.println (getClass().getName()+".readGroup enter");
    while (level > 0 && inputOffset < length) {
      int delimiterLength = segmentDelimiter.testStart(data, start+inputOffset, length-inputOffset);
      if (delimiterLength > 0) {
        byteArrayBuilder.append(data,start+inputOffset,delimiterLength);
        inputOffset += delimiterLength;
        ++level;
      } else {
        delimiterLength = segmentDelimiter.foundEndDelimiter(data, start+inputOffset, length-inputOffset);
        if (delimiterLength > 0) {
          --level;
          if (level > 0) {
            byteArrayBuilder.append(data,start+inputOffset,delimiterLength);
          }
          inputOffset += delimiterLength;
        } else {
          byteArrayBuilder.append(data[start+inputOffset]);
          ++inputOffset;
        }
      }
    }
    //System.out.println (getClass().getName()+".readGroup leave");
  }


  public RecordInput concatenate(RecordInput ri) {
    RecordInput recordInput = this;
    byte[] rhBytes = ri.toByteArray();
    if (rhBytes.length > 0) {
      int capacity = (length + rhBytes.length)*2;
      byte[] newBuffer = new byte[capacity];
      System.arraycopy(data,start,newBuffer,0,length);
      System.arraycopy(rhBytes,0,newBuffer,length,rhBytes.length);
      recordInput = new ByteRecordInput(newBuffer, 0, length+rhBytes.length, 
                                                 getCharset());
    }
    return recordInput;
  }

  public RecordInput concatenate(RecordInput ri, int beginIndex) {
    RecordInput recordInput = this;
    byte[] rhBytes = ri.toByteArray();
    if (rhBytes.length-beginIndex > 0) {
      int capacity = (length + rhBytes.length)*2;
      byte[] newBuffer = new byte[capacity];
      System.arraycopy(data,start,newBuffer,0,length);
      System.arraycopy(rhBytes,beginIndex,newBuffer,length,rhBytes.length-beginIndex);
      recordInput = new ByteRecordInput(newBuffer, 0, length+rhBytes.length-beginIndex, 
                                                 getCharset());
    }
    return recordInput;
  }

  public String toString() {
    try {
      return new String(data,start,length,charset.name());
    } catch (java.io.UnsupportedEncodingException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void readRepeatingGroup2(ServiceContext context, 
                                  Flow flow, 
                                  int count, 
                                  FlatFileOptions flatFileOptions,
                                  DelimiterExtractor[] recordDelimiters, 
                                  int recordDelimiterStart, 
                                  int recordDelimiterCount, 
                                  int maxRecordWidth,
                                  FlatRecordReader flatRecordReader,
                                  RecordReceiver recordReceiver) {

    //System.out.println(getClass().getName()+".readRepeatingGroup 10 start="+start()+",length="+length() );
    try {
      QuoteSymbolByteChecker quoteSymbolChecker = flatFileOptions.getQuoteSymbolByteChecker();
      ByteDelimiterExtractor[] segmentByteDelimiterExtractors = flatFileOptions.getSegmentByteDelimiterExtractors();
      ByteDelimiterExtractor[] repeatByteDelimiterExtractors = flatFileOptions.getRepeatByteDelimiterExtractors();
      ByteTrimmer charTrimmer = flatFileOptions.getByteTrimmer();
      boolean omitFinalRepeatDelimiter = flatFileOptions.isOmitFinalRepeatDelimiter();

      ByteArrayBuilder charArrayBuilder = new ByteArrayBuilder();
      boolean inQuotes = false;
      boolean delimiterFound = false;
      boolean startDelimiterFound = false;
      boolean endDelimiterFound = false;
      boolean startRepeatDelimiterFound = false;

      int counter = 0;
      while (!delimiterFound && inputOffset < length && counter < count) {
        //System.out.println(getClass().getName()+".readRepeatingGroup 20 inQuotes=" + inQuotes +", start="+start()+",length="+length());
        int n = 0;
        if (inQuotes) {
          n = quoteSymbolChecker.foundEscapedQuoteSymbol(data, start+inputOffset, length-inputOffset);
        }
        if (n > 0) {
          int len = n;
          charArrayBuilder.append(data, start+inputOffset, len);
          inputOffset += len;
        } else {
          n = quoteSymbolChecker.foundQuoteSymbol(data, start+inputOffset, length-inputOffset);
          //System.out.println(getClass().getName()+".readRepeatingGroup 30 " + "start="+start+",inputOffset="+inputOffset+",length="+length);
          //System.out.println(getClass().getName()+".readRepeatingGroup 30 " + new String(data,start+inputOffset,length-inputOffset) + n);
          if (n > 0) {
            inQuotes = !inQuotes;
            charArrayBuilder.append(data, start+inputOffset, n);
            inputOffset += n;
            // Need to leave quotes in here, for field reader
          } else if (inQuotes) {
            charArrayBuilder.append(data[start+inputOffset]);
            //System.out.println(getClass().getName()+".readRepeatingGroup 40 " + "In quotes, buffering");
            ++inputOffset;
          } else {
            boolean segmentDelimiterEscaped = false;
            for (int j = 0; !startDelimiterFound && !segmentDelimiterEscaped && j < segmentByteDelimiterExtractors.length; ++j) {
              //System.out.println(getClass().getName()+" checking start segment delimiters");
              int delimiterLength = segmentByteDelimiterExtractors[j].testStart(data, start+inputOffset, length-inputOffset);
              if (delimiterLength > 0) {
                //System.out.println(getClass().getName()+" start segment delimiter found");
                inputOffset += delimiterLength;
                startDelimiterFound = true;
                charArrayBuilder.clear();
                readBracketedRepeatingGroup2(context, flow, quoteSymbolChecker, 
                                             segmentByteDelimiterExtractors[j],
                                             repeatByteDelimiterExtractors, 
                                             count, charTrimmer, 
                                             recordDelimiters, recordDelimiterStart, 
                                             recordDelimiterCount,
                                             maxRecordWidth,
                                             flatRecordReader, recordReceiver);
                delimiterFound = true;
                //System.out.println(getClass().getName()+".readRepeatingGroup 50 found segment delimiter counter="+counter+", count=" +count);
              }
            }
            if (!startDelimiterFound) {
              for (int j = 0; !delimiterFound && !segmentDelimiterEscaped && j < segmentByteDelimiterExtractors.length; ++j) {
                //System.out.println(getClass().getName()+" checking segment delimiter");
                int escapedLength = segmentByteDelimiterExtractors[j].readEscapedDelimiter(data, start+inputOffset, length-inputOffset, charArrayBuilder);
                if (escapedLength > 0) {
                  inputOffset += escapedLength;
                  segmentDelimiterEscaped = true;
                } else {
                  int delimiterLength = segmentByteDelimiterExtractors[j].foundEndDelimiter(data, start+inputOffset, length-inputOffset);
                  if (delimiterLength > 0) {
                    //System.out.println(getClass().getName()+" segment delimiter found");
                    inputOffset += delimiterLength;
                    delimiterFound = true;
                    //System.out.println(getClass().getName()+".readRepeatingGroup 60 found segment delimiter counter="+counter+", count=" +count);
                  }
                }
              }
            }
            if (!delimiterFound && !segmentDelimiterEscaped) {
              boolean repeatDone = false;
              boolean repeatDelimiterEscaped = false;
              for (int j = 0; !repeatDone && !repeatDelimiterEscaped && j < repeatByteDelimiterExtractors.length; ++j) {
                ByteDelimiterExtractor repeatDelimiter = repeatByteDelimiterExtractors[j];
                int repeatDelimiterLength = repeatDelimiter.testStart(data, start+inputOffset, length-inputOffset);
                if (repeatDelimiterLength > 0) {
                  startRepeatDelimiterFound = true;
                  if (charArrayBuilder.length() > 0 && !charTrimmer.isAllWhitespace(charArrayBuilder.buffer(), 0, charArrayBuilder.length())) {
                    //System.out.println(getClass().getName()+".readRepeatingGroup 70 "+new String(charArrayBuilder.buffer(), 0, charArrayBuilder.length()));
                    //System.out.println (getClass().getName()+".readRepeatingGroup 80 before readRecord 10");
                    RecordInput recordInput = new ByteRecordInput(charArrayBuilder.toByteArray(),
                                                                  charset);
                    //children.add(recordInput);
                    flatRecordReader.readRecord(context,flow,
                                                recordInput,
                                                recordDelimiters,
                                                recordDelimiterStart,
                                                recordDelimiterCount,
                                                maxRecordWidth,
                                                recordReceiver);
                    //System.out.println (getClass().getName()+".readRepeatingGroup 90 after readRecord, before clear 10");
                    charArrayBuilder.clear();
                    //System.out.println (getClass().getName()+".readRepeatingGroup 100 after readRecord 10");
                  }
                  inputOffset += repeatDelimiterLength;
                  readGroup2(context, flow, quoteSymbolChecker,repeatDelimiter,charTrimmer,
                             recordDelimiters, recordDelimiterStart, recordDelimiterCount, maxRecordWidth,
                             charArrayBuilder,flatRecordReader,recordReceiver);
                  repeatDone = true;
                } else {
                  int escapedLength = repeatDelimiter.readEscapedDelimiter(data, start+inputOffset, length-inputOffset, charArrayBuilder);
                  if (escapedLength > 0) {
                    inputOffset += escapedLength;
                    repeatDelimiterEscaped = true;
                  } else {
                    repeatDelimiterLength = repeatDelimiter.foundEndDelimiter(data, start+inputOffset, length-inputOffset);
                    if (repeatDelimiterLength > 0) {
                      //System.out.println(getClass().getName()+".readRepeatingGroup 110 found repeat delimiter counter="+counter+", count=" +count);
                      inputOffset += repeatDelimiterLength;
                      repeatDone = true;
                      ++counter;                         
                      //if (charArrayBuilder.length() > 0 && !charTrimmer.isAllWhitespace(charArrayBuilder.buffer(), 0, charArrayBuilder.length())) {
                      //System.out.println(getClass().getName()+".readRepeatingGroup 120 " + new String(charArrayBuilder.buffer(), 0, charArrayBuilder.length()));
                      //System.out.println (getClass().getName()+".readRepeatingGroup 130 before readRecord 50");
                      RecordInput recordInput = new ByteRecordInput(charArrayBuilder.toByteArray(),
                                                                    charset);
                      //children.add(recordInput);
                      flatRecordReader.readRecord(context,flow,
                                                  recordInput,
                                                  recordDelimiters,
                                                  recordDelimiterStart,
                                                  recordDelimiterCount,
                                                  maxRecordWidth,
                                                  recordReceiver);
                      //System.out.println (getClass().getName()+".readRepeatingGroup 140 after readRecord, before clear 50");
                      charArrayBuilder.clear();
                      //System.out.println (getClass().getName()+".readRepeatingGroup 150 after readRecord 50");
                      //}
                    }
                  }
                }
              }
              if (!repeatDone && !segmentDelimiterEscaped && !repeatDelimiterEscaped) {
                charArrayBuilder.append(data[start+inputOffset]);
                ++inputOffset;
              }
            }
          }
        }
      }

      //if (charArrayBuilder.length() > 0 && !charTrimmer.isAllWhitespace(charArrayBuilder.buffer(), 0, charArrayBuilder.length())) {
      //System.out.println(getClass().getName()+".readRepeatingGroup 160 finishing with charArrayBuilder counter="+counter+", count=" +count);
      //System.out.println (getClass().getName()+".readRepeatingGroup 170 before readRecord");
      //System.out.println("omitFinalRepeatDelimiter="+omitFinalRepeatDelimiter + ", startDelimiterFound="+startDelimiterFound);
      if (omitFinalRepeatDelimiter && !startRepeatDelimiterFound && charArrayBuilder.length() > 0) {
        RecordInput recordInput = new ByteRecordInput(charArrayBuilder.toByteArray(),
                                                      charset);
        //children.add(recordInput);
        flatRecordReader.readRecord(context,flow,recordInput,recordDelimiters,recordDelimiterStart,recordDelimiterCount,
                                    maxRecordWidth,
                                    recordReceiver);
        //System.out.println (getClass().getName()+".readRepeatingGroup 180 after readRecord");
      }
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public void readBracketedRepeatingGroup2(ServiceContext context, Flow flow,
                                           QuoteSymbolByteChecker quoteSymbolChecker, 
                                           ByteDelimiterExtractor segmentDelimiter,
                                           ByteDelimiterExtractor[] repeatByteDelimiterExtractors, 
                                           int count, ByteTrimmer charTrimmer, 
                                           DelimiterExtractor[] recordDelimiters,
                                           int recordDelimiterStart, 
                                           int recordDelimiterCount, 
                                           int maxRecordWidth,
                                           FlatRecordReader flatRecordReader,
                                           RecordReceiver recordReceiver) 
  throws IOException {

    ByteArrayBuilder charArrayBuilder = new ByteArrayBuilder();
    int counter = 0;
    boolean inQuotes = false;

    //ByteArrayBuilder charArrayBuilder = new ByteArrayBuilder();
    int level = 1;

    //System.out.println (getClass().getName()+".readGroup enter");
    while (level > 0 && inputOffset < length && counter < count) {
      int n = 0;
      if (inQuotes) {
        n = quoteSymbolChecker.foundEscapedQuoteSymbol(data, start+inputOffset, length-inputOffset);
      }
      if (n > 0) {
        int len = n;
        charArrayBuilder.append(data, start+inputOffset, len);
        inputOffset += len;
      } else {
        n = quoteSymbolChecker.foundQuoteSymbol(data, start+inputOffset, length-inputOffset);
        //System.out.println(new String(data,start+inputOffset,length-inputOffset) + n);
        if (n > 0) {
          inQuotes = !inQuotes;
          charArrayBuilder.append(data, start+inputOffset, n);
          inputOffset += n;
          // Need to leave quotes in here, for field reader
        } else if (inQuotes) {
          charArrayBuilder.append(data[start+inputOffset]);
          //System.out.println("In quotes, buffering");
          ++inputOffset;
        } else {
          int delimiterLength = segmentDelimiter.testStart(data, start+inputOffset, length-inputOffset);
          if (delimiterLength > 0) {
            ++level;
          } else {
            delimiterLength = segmentDelimiter.foundEndDelimiter(data, start+inputOffset, length-inputOffset);
            if (delimiterLength > 0) {
              --level;
              if (level == 0) {
                charArrayBuilder.clear();  //DAP
              } else {
                charArrayBuilder.append(data,inputOffset,delimiterLength);
              }
              inputOffset += delimiterLength;
            }
          }
          if (level > 0) {
            boolean repeatDone = false;
            for (int j = 0; !repeatDone && j < repeatByteDelimiterExtractors.length; ++j) {
              ByteDelimiterExtractor repeatDelimiter = repeatByteDelimiterExtractors[j];
              int repeatDelimiterLength = repeatDelimiter.testStart(data, start+inputOffset, length-inputOffset);
              if (repeatDelimiterLength > 0) {
                if (charArrayBuilder.length() > 0 && !charTrimmer.isAllWhitespace(charArrayBuilder.buffer(), 0, charArrayBuilder.length())) {
                  //System.out.println(new String(charArrayBuilder.buffer(), 0, charArrayBuilder.length()));
                  //System.out.println("Calling readRecord on " + flatRecordReader.getClass().getName());
                  //System.out.println (getClass().getName()+".readField before readRecord 10");
                  RecordInput recordInput = new ByteRecordInput(charArrayBuilder.toByteArray(),
                                                                charset);
                  //children.add(recordInput);
                  flatRecordReader.readRecord(context,flow,recordInput,recordDelimiters,recordDelimiterStart,recordDelimiterCount,
                                              maxRecordWidth, recordReceiver);
                  //System.out.println (getClass().getName()+".readField after readRecord, before clear 10");
                  charArrayBuilder.clear();
                  //System.out.println (getClass().getName()+".readField after readRecord 10");
                }
                inputOffset += repeatDelimiterLength;
                readGroup2(context, flow, quoteSymbolChecker,repeatDelimiter,charTrimmer,recordDelimiters, recordDelimiterStart, recordDelimiterCount, maxRecordWidth,
                             charArrayBuilder,flatRecordReader,recordReceiver);
                repeatDone = true;
              } else {
                repeatDelimiterLength = repeatDelimiter.foundEndDelimiter(data, start+inputOffset, length-inputOffset);
                if (repeatDelimiterLength > 0) {
                  //System.out.println(getClass().getName()+".readField found repeat delimiter counter="+counter+", count=" +count);
                  inputOffset += repeatDelimiterLength;
                  repeatDone = true;
                  ++counter;                         
                  //if (charArrayBuilder.length() > 0 && !charTrimmer.isAllWhitespace(charArrayBuilder.buffer(), 0, charArrayBuilder.length())) {
                  //System.out.println(new String(charArrayBuilder.buffer(), 0, charArrayBuilder.length()));
                  //System.out.println("Calling readRecord on " + flatRecordReader.getClass().getName());
                  //System.out.println (getClass().getName()+".readField before readRecord 50");
                  RecordInput recordInput = new ByteRecordInput(charArrayBuilder.toByteArray(),
                                                                charset);
                  //children.add(recordInput);
                  flatRecordReader.readRecord(context,flow,recordInput,recordDelimiters,recordDelimiterStart,recordDelimiterCount,
                                              maxRecordWidth, recordReceiver);
                  //System.out.println (getClass().getName()+".readField after readRecord, before clear 50");
                  charArrayBuilder.clear();
                  //System.out.println (getClass().getName()+".readField after readRecord 50");
                  //}
                } else {
                  ++inputOffset; //DAP
                }
              }
            }
            if (!repeatDone && inputOffset < length) {
              charArrayBuilder.append(data[start+inputOffset]);
              ++inputOffset;
            }
          }
        }
      }
    }
    //System.out.println (getClass().getName()+".readGroup leave");
  }

  public void readGroup2(ServiceContext context, Flow flow, 
                         QuoteSymbolByteChecker quoteSymbolChecker, 
                         ByteDelimiterExtractor repeatDelimiter, 
                         ByteTrimmer charTrimmer, 
                         DelimiterExtractor[] recordDelimiters, 
                         int recordDelimiterStart, 
                         int recordDelimiterCount, 
                         int maxRecordWidth,
                         ByteArrayBuilder charArrayBuilder,
                         FlatRecordReader flatRecordReader,
                         RecordReceiver recordReceiver) 
  throws IOException {
    //System.out.println(getClass().getName()+".readGroup start="+start() + ",length = " + length() );

    boolean inQuotes = false;

    //ByteArrayBuilder charArrayBuilder = new ByteArrayBuilder();
    int level = 1;

    while (level > 0 && inputOffset < length) {
      int n = 0;
      if (inQuotes) {
        n = quoteSymbolChecker.foundEscapedQuoteSymbol(data, start+inputOffset, length-inputOffset);
      }
      if (n > 0) {
        int len = n;
        charArrayBuilder.append(data, start+inputOffset, len);
        inputOffset += len;
      } else {
        n = quoteSymbolChecker.foundQuoteSymbol(data, start+inputOffset, length-inputOffset);
        //System.out.println(getClass().getName()+".readGroup "+new String(data,start+inputOffset,length-inputOffset) + n);
        if (n > 0) {
          inQuotes = !inQuotes;
          charArrayBuilder.append(data, start+inputOffset, n);
          inputOffset += n;
          // Need to leave quotes in here, for field reader
        } else if (inQuotes) {
          charArrayBuilder.append(data[start+inputOffset]);
          //System.out.println("In quotes, buffering");
          ++inputOffset;
        } else {
          int repeatDelimiterLength = repeatDelimiter.testStart(data, start+inputOffset, length-inputOffset);
          if (repeatDelimiterLength > 0) {
            if (level > 0) {
              charArrayBuilder.append(data,inputOffset,repeatDelimiterLength);
            }
            //System.out.println(getClass().getName()+".readgroup found start level="+level);
            inputOffset += repeatDelimiterLength;
            ++level;
          } else {
            repeatDelimiterLength = repeatDelimiter.foundEndDelimiter(data, start+inputOffset, length-inputOffset);
            if (repeatDelimiterLength > 0) {
              //System.out.println(getClass().getName()+".readgroup found end level="+level);
              --level;
              if (level == 0) {
                //System.out.println (getClass().getName()+".readGroup before readRecord");
                if (charArrayBuilder.length() > 0) {
                  RecordInput recordInput = new ByteRecordInput(charArrayBuilder.toByteArray(),
                                                                charset);
                  //children.add(recordInput);
                  flatRecordReader.readRecord(context,flow,recordInput,
                                              recordDelimiters,recordDelimiterStart,recordDelimiterCount,
                                              maxRecordWidth, recordReceiver);
                  //System.out.println (getClass().getName()+".readGroup after readRecord, before clear");
                  charArrayBuilder.clear();
                }
                //System.out.println (getClass().getName()+".readGroup after readRecord");
              } else {
                charArrayBuilder.append(data,inputOffset,repeatDelimiterLength);
              }
              inputOffset += repeatDelimiterLength;
            } else if (inputOffset < length) {
              charArrayBuilder.append(data[start+inputOffset]);
              ++inputOffset;
            }
          }
        }
      }
    }
    //System.out.println (getClass().getName()+".readGroup leave");
    //System.out.println("readGroup leave");
  }

  public int length() {
    return length;
  }

  public int start() {
    return start;
  }
}




