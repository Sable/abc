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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordReceiver;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.CharDelimiterExtractor;
import com.servingxml.components.flatfile.options.CharTrimmer;
import com.servingxml.components.flatfile.options.DelimiterExtractor;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.QuoteSymbolCharChecker;
import com.servingxml.util.CharArrayBuilder;
import com.servingxml.util.CharsetHelper;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.RecordReceiver;

public class CharRecordInput implements RecordInput {
  private final char[] data;
  private final int start;
  private final int length;
  private int inputOffset;
  private int endIndex;
  private final Charset charset;

  public CharRecordInput(char[] data, int start, int length, Charset charset) {
    this.data = data;
    this.start = start;
    this.length = length;
    this.inputOffset = 0;
    this.endIndex = 0;
    this.charset = (charset == null) ? Charset.defaultCharset() : charset;
  }

  public CharRecordInput(char[] data, Charset charset) {
    this.data = data;
    this.start = 0;
    this.length = data.length;
    this.inputOffset = 0;
    this.endIndex = 0;
    this.charset = (charset == null) ? Charset.defaultCharset() : charset;
  }

  public byte[] toByteArray() {
    return CharsetHelper.charactersToBytes(data,start,length, charset);
  }

  public char[] toCharArray() {
    char[] newData = new char[length];
    System.arraycopy(data,start,newData,0,length);
    return newData;
  }

  public boolean done() {
    return inputOffset >= length - start;
  }

  public int readBytes(byte[] value) {
    return 0;
  }

  public String readString(int width) {
    //System.out.println(getClass().getName()+".readString enter");
    int len = width <= length - inputOffset ? width : length - inputOffset;
    String s;
    if (len > 0) {
      //System.out.println(getClass().getName()+".readString width="+width+", length="+length+", inputOffset="
      //  +inputOffset + ", len="+len);
      s = new String(data, start+inputOffset, len);
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
    QuoteSymbolCharChecker quoteSymbolChecker = flatFileOptions.getQuoteSymbolCharChecker();
    CharDelimiterExtractor[] fieldCharDelimiterExtractors = flatFileOptions.getFieldCharDelimiterExtractors();
    boolean omitFinalFieldDelimiter = flatFileOptions.isOmitFinalFieldDelimiter();
    boolean trimLeading = flatFileOptions.isTrimLeading();
    boolean trimTrailing = flatFileOptions.isTrimTrailing();
    CharTrimmer charTrimmer = flatFileOptions.getCharTrimmer();

    int inputLength = (maxLength >= 0 && maxLength <= length - inputOffset) ? maxLength : length - inputOffset;
    int end = inputOffset + inputLength;

    boolean inQuotes = false;
    boolean delimiterFound = false;

    CharArrayBuilder charArrayBuilder = new CharArrayBuilder();

    //  Initialize endFieldPosition
    int endFieldPosition = length;
    if (maxLength >= 0) {
      endFieldPosition = inputOffset+maxLength < length ? inputOffset+maxLength : length;
    }

    //  Initialize leadingCount and leftLimitTrailingSpace
    int leadingCount = 0;
    if (trimLeading) {
      leadingCount = charTrimmer.countLeadingWhitespace(data, start+inputOffset, endFieldPosition-inputOffset);
    }
    int leftLimitTrailingSpace = leadingCount;

    while (!delimiterFound && inputOffset < end) {
      int n = 0;
      if (inQuotes) {
        n = quoteSymbolChecker.foundEscapedQuoteSymbol(data, start+inputOffset, end-inputOffset);
      }
      if (n > 0) {
        inputOffset += (n - quoteSymbolChecker.length());
        charArrayBuilder.append(data, start+inputOffset, quoteSymbolChecker.length());
        inputOffset += quoteSymbolChecker.length();
      } else {
        n = quoteSymbolChecker.foundQuoteSymbol(data, start+inputOffset, end-inputOffset);
        if (n > 0) {
          //System.out.println("readQuotedField-foundQuoteSymbol " + new String(data,start+inputOffset,length-inputOffset));
          inQuotes = !inQuotes;
          inputOffset += n;
        } else if (inQuotes) {
          charArrayBuilder.append(data[start+inputOffset]);
          //System.out.println("Appending " + new String(data,start+inputOffset,1) + ", buflen=" + charArrayBuilder.length());
          ++inputOffset;
          leftLimitTrailingSpace = charArrayBuilder.length();
        } else {
          boolean fieldDelimiterEscaped = false;
          for (int i = 0; !delimiterFound && !fieldDelimiterEscaped && i < fieldCharDelimiterExtractors.length; ++i) {
            int escapedLength = fieldCharDelimiterExtractors[i].readEscapedDelimiter(data, start+inputOffset, end-inputOffset, charArrayBuilder);
            if (escapedLength > 0) {
              inputOffset += escapedLength;
              fieldDelimiterEscaped = true;
            } else {
              int delimiterLength = fieldCharDelimiterExtractors[i].foundEndDelimiter(data, start+inputOffset, end-inputOffset);
              if (delimiterLength > 0) {
                inputOffset += delimiterLength;
                delimiterFound = true;
              }
            }
          }
          if (!delimiterFound && !fieldDelimiterEscaped) {
            charArrayBuilder.append(data[start+inputOffset]);
            //System.out.println("Appending " + new String(data,start+inputOffset,1) + ", buflen=" + charArrayBuilder.length());
            ++inputOffset;
          }
        }
      }
    }
    String value = null;
    if (delimiterFound || omitFinalFieldDelimiter) {
      value = "";
      int len = charArrayBuilder.length() - leadingCount;
      int trailingCount = 0;
      if (trimTrailing) {
        trailingCount = charTrimmer.countTrailingWhitespace(charArrayBuilder.buffer(), leftLimitTrailingSpace, 
                                                            charArrayBuilder.length()-leftLimitTrailingSpace);
        //System.out.println(getClass().getName() + ".readField "
        //  + "leftLimitTrailingSpace="+leftLimitTrailingSpace 
        //  + ", buflen=" + charArrayBuilder.length()
        //  + ", trailingCount=" + trailingCount
        //  + ", leadingCount=" + leadingCount);
      }
      if (len-trailingCount > 0) {
        //value = new String(charArrayBuilder.buffer(), leadingCount, len-trailingCount);
        value = new String(charArrayBuilder.toCharArray(),leadingCount,len-trailingCount);
        //System.out.println(getClass().getName() + ".readField "
        // + ".readField buffer=" + new String(charArrayBuilder.buffer()) + "." 
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
    QuoteSymbolCharChecker quoteSymbolChecker = flatFileOptions.getQuoteSymbolCharChecker();
    CharDelimiterExtractor[] fieldCharDelimiterExtractors = flatFileOptions.getFieldCharDelimiterExtractors();
    CharDelimiterExtractor[] subfieldCharDelimiterExtractors = flatFileOptions.getSubfieldCharDelimiterExtractors();
    boolean omitFinalFieldDelimiter = flatFileOptions.isOmitFinalFieldDelimiter();
    boolean trimLeading = flatFileOptions.isTrimLeading();
    boolean trimTrailing = flatFileOptions.isTrimTrailing();
    CharTrimmer charTrimmer = flatFileOptions.getCharTrimmer();

    int maxLen = (maxLength >= 0 && maxLength <= length - inputOffset) ? maxLength : length - inputOffset;
    int end = inputOffset + maxLen;

    boolean inQuotes = false;
    boolean delimiterFound = false;

    CharArrayBuilder charArrayBuilder = new CharArrayBuilder();
    ArrayList<String> valueList = new ArrayList<String>();

    //  Initialize endFieldPosition
    int endFieldPosition = length;
    if (maxLength >= 0) {
      endFieldPosition = inputOffset+maxLength < length ? inputOffset+maxLength : length;
    }

    //  Initialize leadingCount and leftLimitTrailingSpace
    int leadingCount = 0;
    if (trimLeading) {
      leadingCount = charTrimmer.countLeadingWhitespace(data, start+inputOffset, endFieldPosition-inputOffset);
    }
    int leftLimitTrailingSpace = leadingCount;

    while (!delimiterFound && inputOffset < end) {
      int n = 0;
      if (inQuotes) {
        n = quoteSymbolChecker.foundEscapedQuoteSymbol(data, start+inputOffset, end-inputOffset);
      }
      if (n > 0) {
        inputOffset += (n-quoteSymbolChecker.length());
        charArrayBuilder.append(data, start+inputOffset, quoteSymbolChecker.length());
        inputOffset += quoteSymbolChecker.length();
      } else {
        n = quoteSymbolChecker.foundQuoteSymbol(data, start+inputOffset, end-inputOffset);
        if (n > 0) {
          //System.out.println("readQuotedField-foundQuoteSymbol " + new String(data,start+inputOffset,length-inputOffset));
          inQuotes = !inQuotes;
          inputOffset += n;
        } else if (inQuotes) {
          charArrayBuilder.append(data[start+inputOffset]);
          //System.out.println("Appending " + new String(data,start+inputOffset,1) + ", buflen=" + charArrayBuilder.length());
          ++inputOffset;
          leftLimitTrailingSpace = charArrayBuilder.length();
        } else {
          boolean fieldDelimiterEscaped = false;
          for (int i = 0; !delimiterFound && !fieldDelimiterEscaped && i < fieldCharDelimiterExtractors.length; ++i) {
            int escapedLength = fieldCharDelimiterExtractors[i].readEscapedDelimiter(data, start+inputOffset, end-inputOffset, charArrayBuilder);
            if (escapedLength > 0) {
              inputOffset += escapedLength;
              fieldDelimiterEscaped = true;
            } else {
              int delimiterLength = fieldCharDelimiterExtractors[i].foundEndDelimiter(data, start+inputOffset, end-inputOffset);
              if (delimiterLength > 0) {
                inputOffset += delimiterLength;
                delimiterFound = true;
              }
            }
          }
          if (!delimiterFound && !fieldDelimiterEscaped) {
            boolean subfieldDone = false;
            boolean subfieldDelimiterEscaped = false;
            for (int i = 0; !subfieldDone && !subfieldDelimiterEscaped && i < subfieldCharDelimiterExtractors.length; ++i) {
              int escapedLength = subfieldCharDelimiterExtractors[i].readEscapedDelimiter(data, start+inputOffset, end-inputOffset, charArrayBuilder);
              if (escapedLength > 0) {
                inputOffset += escapedLength;
                fieldDelimiterEscaped = true;
              } else {
                int delimiterLength = subfieldCharDelimiterExtractors[i].foundEndDelimiter(data, start+inputOffset, end-inputOffset);
                if (delimiterLength > 0) {
                  //String s;
                  //if (charArrayBuilder.length() > 0) {
                  //  s = CharsetHelper.bytesToString(charArrayBuilder.buffer(), charArrayBuilder.start(),
                  //        charArrayBuilder.length(), charset);
                  //} else {
                  //  s = "";
                  //}
                  if (charArrayBuilder.length() > 0) {
                    //System.out.println(getClass().getName()+" addingValue");
                    extractValue(charTrimmer,trimTrailing,leadingCount,leftLimitTrailingSpace,charArrayBuilder,valueList);
                  } else {
                    // Adjacent delimiters, add empty value 
                    //System.out.println(getClass().getName()+" adding empty value");
                    valueList.add("");
                  }
                  charArrayBuilder.clear();
                  inputOffset += delimiterLength;
                  if (trimLeading) {
                    leadingCount = charTrimmer.countLeadingWhitespace(data, start+inputOffset, 
                                                                      endFieldPosition-inputOffset);
                  }
                  leftLimitTrailingSpace = leadingCount;
                  subfieldDone = true;
                }
              }
            }
            if (!subfieldDone && !subfieldDelimiterEscaped) {
              charArrayBuilder.append(data[start+inputOffset]);
              ++inputOffset;
            }
          }
        }
      }
    }

    String[] values = null;
    if (delimiterFound || omitFinalFieldDelimiter) {
      if (charArrayBuilder.length() > 0) {
        //String s = CharsetHelper.bytesToString(charArrayBuilder.buffer(), charArrayBuilder.start(),
        //             charArrayBuilder.length(), charset );
        //valueList.add(s);
        extractValue(charTrimmer,trimTrailing,leadingCount,leftLimitTrailingSpace,charArrayBuilder,valueList);
      }
      values = new String[valueList.size()];
      values = (String[])valueList.toArray(values); 
    }
    //System.out.println(getClass().getName()+" value count="+values.length);
    updateLast();
    return values;
  }

  private void extractValue(CharTrimmer charTrimmer, boolean trimTrailing, int leadingCount, 
                            int leftLimitTrailingSpace, CharArrayBuilder charArrayBuilder, List<String> valueList) {
    String value="";
    int len = charArrayBuilder.length() - leadingCount;
    int trailingCount = 0;
    if (trimTrailing) {
      trailingCount = charTrimmer.countTrailingWhitespace(charArrayBuilder.buffer(), leftLimitTrailingSpace, 
                                                          charArrayBuilder.length()-leftLimitTrailingSpace);
      //System.out.println("leftLimitTrailingSpace="+leftLimitTrailingSpace + ", buflen=" + charArrayBuilder.length()
      //  + ", trailingCount=" + trailingCount);
    }
    if (len-trailingCount > 0) {
      value = new String(charArrayBuilder.buffer(), leadingCount, len-trailingCount);
      //System.out.println("value = " + value);
      //value = StringHelper.trim(value, false, trimTrailing); 
    }
    valueList.add(value);
  }

  public int getPosition() {
    return inputOffset;
  }

  public int length() {
    return length;
  }

  public int start() {
    return start;
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

  public RecordInput readSegment(FlatFileOptions flatFileOptions) {

    QuoteSymbolCharChecker quoteSymbolChecker = flatFileOptions.getQuoteSymbolCharChecker();
    CharDelimiterExtractor[] segmentDelimiterExtractors = flatFileOptions.getSegmentCharDelimiterExtractors();
    CharTrimmer charTrimmer = flatFileOptions.getCharTrimmer();

    try {
      CharArrayBuilder charArrayBuilder = new CharArrayBuilder();
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
            if (!endDelimiterFound) {
              boolean segmentDelimiterEscaped = false;
              for (int j = 0; !endDelimiterFound && !segmentDelimiterEscaped && j < segmentDelimiterExtractors.length; ++j) {
                CharDelimiterExtractor segmentDelimiter = segmentDelimiterExtractors[j];
                int escapedLength = segmentDelimiter.readEscapedDelimiter(data, start+inputOffset, length-inputOffset, charArrayBuilder);
                if (escapedLength > 0) {
                  inputOffset += escapedLength;
                  segmentDelimiterEscaped = true;
                } else {
                  int delimiterLength = segmentDelimiter.testStart(data, start+inputOffset, length-inputOffset);
                  if (delimiterLength > 0) {
                    inputOffset += delimiterLength;
                    readToEndOfSegment(segmentDelimiter,charArrayBuilder);
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
              }
              if (!endDelimiterFound && !segmentDelimiterEscaped) {
                charArrayBuilder.append(data[start+inputOffset]);
                ++inputOffset;
              }
            }
          }
        }
      }
      RecordInput recordInput = new CharRecordInput(charArrayBuilder.toCharArray(),
                                                    charset);
      return recordInput;
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public int readCharacters(char[] value) throws IOException {
    int len = value.length <= length - inputOffset ? value.length : length - inputOffset;
    if (len > 0) {
      System.arraycopy(data, start+inputOffset, value, 0, len);
      inputOffset += len;
    }
    updateLast();
    return len;
  }

  public RecordInput readSegment(int segmentLength) {
    try {
      char[] data = new char[segmentLength];
      int length = readCharacters(data);
      RecordInput segmentInput = new CharRecordInput(data, 0, length, getCharset());
      return segmentInput;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void readToEndOfSegment(CharDelimiterExtractor segmentDelimiter, CharArrayBuilder charArrayBuilder) 
  throws IOException {

    //CharArrayBuilder charArrayBuilder = new CharArrayBuilder();
    int level = 1;

    //System.out.println (getClass().getName()+".readGroup enter");
    while (level > 0 && inputOffset < length) {
      int delimiterLength = segmentDelimiter.testStart(data, start+inputOffset, length-inputOffset);
      if (delimiterLength > 0) {
        charArrayBuilder.append(data,start+inputOffset,delimiterLength);
        inputOffset += delimiterLength;
        ++level;
      } else {
        delimiterLength = segmentDelimiter.foundEndDelimiter(data, start+inputOffset, length-inputOffset);
        if (delimiterLength > 0) {
          --level;
          if (level > 0) {
            charArrayBuilder.append(data,start+inputOffset,delimiterLength);
          }
          inputOffset += delimiterLength;
        } else {
          charArrayBuilder.append(data[start+inputOffset]);
          ++inputOffset;
        }
      }
    }
    //System.out.println (getClass().getName()+".readGroup leave");
  }


  public RecordInput concatenate(RecordInput ri) {
    RecordInput recordInput = this;
    char[] rhCharacters = ri.toCharArray();
    if (rhCharacters.length > 0) {
      int capacity = (length + rhCharacters.length)*2;
      char[] newBuffer = new char[capacity];
      System.arraycopy(data,start,newBuffer,0,length);
      System.arraycopy(rhCharacters,0,newBuffer,length,rhCharacters.length);
      recordInput = new CharRecordInput(newBuffer, 0, length+rhCharacters.length, 
                                        getCharset());
    }
    return recordInput;
  }

  public RecordInput concatenate(RecordInput ri, int beginIndex) {
    RecordInput recordInput = this;
    char[] rhCharacters = ri.toCharArray();
    if (rhCharacters.length - beginIndex > 0) {
      int capacity = (length + rhCharacters.length)*2;
      char[] newBuffer = new char[capacity];
      System.arraycopy(data,start,newBuffer,0,length);
      System.arraycopy(rhCharacters,beginIndex,newBuffer,length,rhCharacters.length-beginIndex);
      recordInput = new CharRecordInput(newBuffer, 0, length+rhCharacters.length-beginIndex, 
                                        getCharset());
    }
    return recordInput;
  }

  public String toString() {
    return new String(data,start,length);
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
      QuoteSymbolCharChecker quoteSymbolChecker = flatFileOptions.getQuoteSymbolCharChecker();
      CharDelimiterExtractor[] segmentCharDelimiterExtractors = flatFileOptions.getSegmentCharDelimiterExtractors();
      CharDelimiterExtractor[] repeatCharDelimiterExtractors = flatFileOptions.getRepeatCharDelimiterExtractors();
      CharTrimmer charTrimmer = flatFileOptions.getCharTrimmer();
      boolean omitFinalRepeatDelimiter = flatFileOptions.isOmitFinalRepeatDelimiter();

      CharArrayBuilder charArrayBuilder = new CharArrayBuilder();
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
            for (int j = 0; !startDelimiterFound && !segmentDelimiterEscaped && j < segmentCharDelimiterExtractors.length; ++j) {
              //System.out.println(getClass().getName()+" checking start segment delimiters");
              int delimiterLength = segmentCharDelimiterExtractors[j].testStart(data, start+inputOffset, length-inputOffset);
              if (delimiterLength > 0) {
                //System.out.println(getClass().getName()+" start segment delimiter found");
                inputOffset += delimiterLength;
                startDelimiterFound = true;
                charArrayBuilder.clear();
                readBracketedRepeatingGroup2(context, flow, quoteSymbolChecker, 
                                             segmentCharDelimiterExtractors[j],
                                             repeatCharDelimiterExtractors, 
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
              for (int j = 0; !delimiterFound && !segmentDelimiterEscaped && j < segmentCharDelimiterExtractors.length; ++j) {
                //System.out.println(getClass().getName()+" checking segment delimiter");
                int escapedLength = segmentCharDelimiterExtractors[j].readEscapedDelimiter(data, start+inputOffset, length-inputOffset, charArrayBuilder);
                if (escapedLength > 0) {
                  inputOffset += escapedLength;
                  segmentDelimiterEscaped = true;
                } else {
                  int delimiterLength = segmentCharDelimiterExtractors[j].foundEndDelimiter(data, start+inputOffset, length-inputOffset);
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
              for (int j = 0; !repeatDone && !repeatDelimiterEscaped && j < repeatCharDelimiterExtractors.length; ++j) {
                CharDelimiterExtractor repeatDelimiter = repeatCharDelimiterExtractors[j];
                int repeatDelimiterLength = repeatDelimiter.testStart(data, start+inputOffset, length-inputOffset);
                if (repeatDelimiterLength > 0) {
                  startRepeatDelimiterFound = true;
                  if (charArrayBuilder.length() > 0 && !charTrimmer.isAllWhitespace(charArrayBuilder.buffer(), 0, charArrayBuilder.length())) {
                    //System.out.println(getClass().getName()+".readRepeatingGroup 70 "+new String(charArrayBuilder.buffer(), 0, charArrayBuilder.length()));
                    //System.out.println (getClass().getName()+".readRepeatingGroup 80 before readRecord 10");
                    RecordInput recordInput = new CharRecordInput(charArrayBuilder.toCharArray(),
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
                      RecordInput recordInput = new CharRecordInput(charArrayBuilder.toCharArray(),
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
        RecordInput recordInput = new CharRecordInput(charArrayBuilder.toCharArray(),
                                                      charset);
        //children.add(recordInput);
        flatRecordReader.readRecord(context,flow,recordInput,recordDelimiters,recordDelimiterStart,recordDelimiterCount,
                                    maxRecordWidth, recordReceiver);
        //System.out.println (getClass().getName()+".readRepeatingGroup 180 after readRecord");
      }
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public void readBracketedRepeatingGroup2(ServiceContext context, Flow flow,
                                           QuoteSymbolCharChecker quoteSymbolChecker, 
                                           CharDelimiterExtractor segmentDelimiter,
                                           CharDelimiterExtractor[] repeatCharDelimiterExtractors, 
                                           int count, CharTrimmer charTrimmer, 
                                           DelimiterExtractor[] recordDelimiters,
                                           int recordDelimiterStart, 
                                           int recordDelimiterCount, 
                                           int maxRecordWidth,
                                           FlatRecordReader flatRecordReader,
                                           RecordReceiver recordReceiver) 
  throws IOException {

    CharArrayBuilder charArrayBuilder = new CharArrayBuilder();
    int counter = 0;
    boolean inQuotes = false;

    //CharArrayBuilder charArrayBuilder = new CharArrayBuilder();
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
            for (int j = 0; !repeatDone && j < repeatCharDelimiterExtractors.length; ++j) {
              CharDelimiterExtractor repeatDelimiter = repeatCharDelimiterExtractors[j];
              int repeatDelimiterLength = repeatDelimiter.testStart(data, start+inputOffset, length-inputOffset);
              if (repeatDelimiterLength > 0) {
                if (charArrayBuilder.length() > 0 && !charTrimmer.isAllWhitespace(charArrayBuilder.buffer(), 0, charArrayBuilder.length())) {
                  //System.out.println(new String(charArrayBuilder.buffer(), 0, charArrayBuilder.length()));
                  //System.out.println("Calling readRecord on " + flatRecordReader.getClass().getName());
                  //System.out.println (getClass().getName()+".readField before readRecord 10");
                  RecordInput recordInput = new CharRecordInput(charArrayBuilder.toCharArray(),
                                                                charset);
                  //children.add(recordInput);
                  flatRecordReader.readRecord(context,flow,recordInput,recordDelimiters,recordDelimiterStart,recordDelimiterCount,
                                              maxRecordWidth, recordReceiver);
                  //System.out.println (getClass().getName()+".readField after readRecord, before clear 10");
                  charArrayBuilder.clear();
                  //System.out.println (getClass().getName()+".readField after readRecord 10");
                }
                inputOffset += repeatDelimiterLength;
                readGroup2(context, flow, quoteSymbolChecker,repeatDelimiter,charTrimmer,recordDelimiters, recordDelimiterStart, recordDelimiterCount,
                           maxRecordWidth,
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
                  RecordInput recordInput = new CharRecordInput(charArrayBuilder.toCharArray(),
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
                         QuoteSymbolCharChecker quoteSymbolChecker, 
                         CharDelimiterExtractor repeatDelimiter, 
                         CharTrimmer charTrimmer, 
                         DelimiterExtractor[] recordDelimiters, 
                         int recordDelimiterStart, 
                         int recordDelimiterCount, 
                         int maxRecordWidth,
                         CharArrayBuilder charArrayBuilder,
                         FlatRecordReader flatRecordReader,
                         RecordReceiver recordReceiver) 
  throws IOException {
    //System.out.println(getClass().getName()+".readGroup start="+start() + ",length = " + length() );

    boolean inQuotes = false;

    //CharArrayBuilder charArrayBuilder = new CharArrayBuilder();
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
                  RecordInput recordInput = new CharRecordInput(charArrayBuilder.toCharArray(),
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
}




