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

package com.servingxml.components.flatfile.options;

import java.nio.charset.Charset;

import com.servingxml.components.quotesymbol.QuoteSymbol;
import com.servingxml.util.Alignment;
import com.servingxml.util.ServingXmlException;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface FlatFileOptions {

  Charset getDefaultCharset();

  Charset getCharset();

  ByteTrimmer getByteTrimmer();

  CharTrimmer getCharTrimmer();

  QuoteSymbolByteChecker getQuoteSymbolByteChecker();

  QuoteSymbolCharChecker getQuoteSymbolCharChecker();

  boolean isIgnoreTrailingEmptyLines();

  boolean isIgnoreEmptyLines();

  boolean isLineDelimited();

  CommentStarter getCommentStarter();

  CommentStarterByteChecker getCommentStarterByteChecker();

  CommentStarterCharChecker getCommentStarterCharChecker();

  Delimiter getRecordDelimiterForWriting();

  boolean isOmitFinalFieldDelimiter();

  boolean isOmitFinalRepeatDelimiter();

  Delimiter[] getRecordDelimiters();

  Delimiter[] getSegmentDelimiters();

  Delimiter[] getFieldDelimiters();

  Delimiter[] getNameDelimiters();

  Delimiter[] getRepeatDelimiters();

  Delimiter[] getSubfieldDelimiters();

  boolean isCountPositionsInBytes();

  boolean isFlushRecordOnWrite();

  boolean isTrimLeading();

  boolean isTrimTrailing();

  boolean isTrimLeadingWithinQuotes();

  boolean isTrimTrailingWithinQuotes();

  QuoteSymbol getQuoteSymbol();

  boolean isQuote();

  boolean isAlwaysQuote();

  Alignment getAlignment();

  char getPadCharacter();

  boolean useQuotes(String value);

  ByteDelimiterExtractor[] getRecordByteDelimiterExtractors();

  ByteDelimiterExtractor[] getSegmentByteDelimiterExtractors();

  ByteDelimiterExtractor[] getRepeatByteDelimiterExtractors();

  ByteDelimiterExtractor[] getNameByteDelimiterExtractors();

  ByteDelimiterExtractor[] getFieldByteDelimiterExtractors();

  ByteDelimiterExtractor[] getSubfieldByteDelimiterExtractors();

  CharDelimiterExtractor[] getRecordCharDelimiterExtractors();

  CharDelimiterExtractor[] getSegmentCharDelimiterExtractors();

  CharDelimiterExtractor[] getRepeatCharDelimiterExtractors();

  CharDelimiterExtractor[] getNameCharDelimiterExtractors();

  CharDelimiterExtractor[] getFieldCharDelimiterExtractors();

  CharDelimiterExtractor[] getSubfieldCharDelimiterExtractors();

  DelimiterExtractor[] getRecordDelimiterExtractors();

  DelimiterExtractor[] getSegmentDelimiterExtractors();

  DelimiterExtractor[] getRepeatDelimiterExtractors();

  DelimiterExtractor[] getNameDelimiterExtractors();

  DelimiterExtractor[] getFieldDelimiterExtractors();

  DelimiterExtractor[] getSubfieldDelimiterExtractors();

  int rebaseIndex(int index);

  int getIndexBase();
}

