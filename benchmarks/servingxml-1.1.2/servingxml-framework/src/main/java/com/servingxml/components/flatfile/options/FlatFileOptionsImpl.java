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
import com.servingxml.components.flatfile.parsing.Token;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileOptionsImpl implements FlatFileOptions {

  private final Charset charset;
  private boolean ignoreTrailingEmptyLines;
  private boolean ignoreEmptyLines;
  private boolean lineDelimited;
  private CommentStarter commentStarter;
  private Delimiter[] recordDelimiters;
  private Delimiter[] segmentDelimiters;
  private Delimiter[] fieldDelimiters;
  private Delimiter[] nameDelimiters;
  private Delimiter[] repeatDelimiters;
  private Delimiter[] subfieldDelimiters;
  private boolean countPositionsInBytes;
  private boolean flushRecordOnWrite;
  private boolean trimLeading;
  private boolean trimTrailing;
  private boolean trimLeadingWithinQuotes;
  private boolean trimTrailingWithinQuotes;
  private QuoteSymbol quoteSymbol;
  private boolean quote;
  private boolean alwaysQuote;
  private boolean omitFinalFieldDelimiter;
  private boolean omitFinalRepeatDelimiter;
  private char padCharacter;
  private Alignment alignment;
  private QuoteSymbolByteChecker byteQuoteSymbolChecker;
  private QuoteSymbolCharChecker charQuoteSymbolChecker;
  private ByteTrimmer byteTrimmer;
  private CharTrimmer charTrimmer;

  private ByteDelimiterExtractor[] recordByteDelimiterExtractors;
  private ByteDelimiterExtractor[] segmentByteDelimiterExtractors;
  private ByteDelimiterExtractor[] repeatByteDelimiterExtractors;
  private ByteDelimiterExtractor[] nameByteDelimiterExtractors;
  private ByteDelimiterExtractor[] fieldByteDelimiterExtractors;
  private ByteDelimiterExtractor[] subfieldByteDelimiterExtractors;
  private CharDelimiterExtractor[] recordCharDelimiterExtractors;
  private CharDelimiterExtractor[] segmentCharDelimiterExtractors;
  private CharDelimiterExtractor[] repeatCharDelimiterExtractors;
  private CharDelimiterExtractor[] nameCharDelimiterExtractors;
  private CharDelimiterExtractor[] fieldCharDelimiterExtractors; 
  private CharDelimiterExtractor[] subfieldCharDelimiterExtractors;

  private DelimiterExtractor[] recordDelimiterExtractors;
  private DelimiterExtractor[] segmentDelimiterExtractors;
  private DelimiterExtractor[] repeatDelimiterExtractors;
  private DelimiterExtractor[] nameDelimiterExtractors;
  private DelimiterExtractor[] fieldDelimiterExtractors;
  private DelimiterExtractor[] subfieldDelimiterExtractors;
  private int indexBase;

  private CommentStarterByteChecker byteCommentStarterChecker;
  private CommentStarterCharChecker charCommentStarterChecker;

  public FlatFileOptionsImpl(Charset charset, boolean delimitedFile, boolean countPositionsInBytes) { 
    this.charset = charset;

    this.ignoreTrailingEmptyLines = true;
    this.ignoreEmptyLines = false;
    this.lineDelimited = true;
    this.flushRecordOnWrite = false;
    this.trimLeading = true;
    this.trimTrailing = true;
    this.trimLeadingWithinQuotes = false;
    this.trimTrailingWithinQuotes = false;
    this.countPositionsInBytes = countPositionsInBytes;
    this.omitFinalFieldDelimiter = true;
    this.omitFinalRepeatDelimiter = true;
    this.indexBase = 1;
    this.commentStarter = CommentStarter.NULL;
    this.byteCommentStarterChecker = CommentStarterByteChecker.NULL;
    this.charCommentStarterChecker = CommentStarterCharChecker.NULL;

    //RecordDelimiter CRLF = new RecordDelimiter(RecordDelimiter.CRLF, true, true);
    //RecordDelimiter LF = new RecordDelimiter(RecordDelimiter.LF, true, true);
    //this.recordDelimiters = new RecordDelimiter[]{CRLF,LF};

    this.recordDelimiters = new RecordDelimiter[]{RecordDelimiter.CRLF,RecordDelimiter.LF};
    this.segmentDelimiters = Delimiter.EMPTY_DELIMITER_ARRAY;
    this.fieldDelimiters = Delimiter.EMPTY_DELIMITER_ARRAY;
    this.nameDelimiters = Delimiter.EMPTY_DELIMITER_ARRAY;
    this.repeatDelimiters = Delimiter.EMPTY_DELIMITER_ARRAY;
    this.subfieldDelimiters = Delimiter.EMPTY_DELIMITER_ARRAY;

    this.recordByteDelimiterExtractors = makeByteDelimiterExtractors(recordDelimiters,charset); 
    this.segmentByteDelimiterExtractors = ByteDelimiterExtractor.EMPTY_ARRAY;
    this.repeatByteDelimiterExtractors = ByteDelimiterExtractor.EMPTY_ARRAY; 
    this.nameByteDelimiterExtractors = ByteDelimiterExtractor.EMPTY_ARRAY;    
    this.fieldByteDelimiterExtractors = ByteDelimiterExtractor.EMPTY_ARRAY;
    this.subfieldByteDelimiterExtractors = ByteDelimiterExtractor.EMPTY_ARRAY;

    this.recordCharDelimiterExtractors = makeCharDelimiterExtractors(recordDelimiters); 
    this.segmentCharDelimiterExtractors = CharDelimiterExtractor.EMPTY_ARRAY;
    this.repeatCharDelimiterExtractors = CharDelimiterExtractor.EMPTY_ARRAY; 
    this.nameCharDelimiterExtractors = CharDelimiterExtractor.EMPTY_ARRAY;    
    this.fieldCharDelimiterExtractors = CharDelimiterExtractor.EMPTY_ARRAY;
    this.subfieldCharDelimiterExtractors = CharDelimiterExtractor.EMPTY_ARRAY;

    this.recordDelimiterExtractors = makeDelimiterExtractors(recordDelimiters, charset); 
    this.segmentDelimiterExtractors = DelimiterExtractor.EMPTY_ARRAY;
    this.repeatDelimiterExtractors = DelimiterExtractor.EMPTY_ARRAY; 
    this.nameDelimiterExtractors = DelimiterExtractor.EMPTY_ARRAY;    
    this.fieldDelimiterExtractors = DelimiterExtractor.EMPTY_ARRAY;
    this.subfieldDelimiterExtractors = DelimiterExtractor.EMPTY_ARRAY;

    this.quoteSymbol = new QuoteSymbol('"', "\"\"");
    if (delimitedFile) {
      this.quote = true;
    } else {
      this.quote = false;
    }
    this.alwaysQuote = false;
    this.padCharacter = ' ';
    this.alignment = Alignment.LEFT;
    this.byteQuoteSymbolChecker = quoteSymbol.createQuoteSymbolChecker(charset);
    this.charQuoteSymbolChecker = quoteSymbol.createCharQuoteSymbolChecker();
    this.byteTrimmer = ByteTrimmer.newInstance(charset);
    this.charTrimmer = CharTrimmer.newInstance();
  }

  public FlatFileOptionsImpl(FlatFileOptions defaults) { 
    this.ignoreTrailingEmptyLines = defaults.isIgnoreTrailingEmptyLines();
    this.ignoreEmptyLines = defaults.isIgnoreEmptyLines();
    this.lineDelimited = defaults.isLineDelimited();
    this.commentStarter = defaults.getCommentStarter();
    this.recordDelimiters = defaults.getRecordDelimiters();
    this.segmentDelimiters = defaults.getSegmentDelimiters();
    this.fieldDelimiters = defaults.getFieldDelimiters();
    this.nameDelimiters = defaults.getNameDelimiters();
    this.repeatDelimiters = defaults.getRepeatDelimiters();
    this.subfieldDelimiters = defaults.getSubfieldDelimiters();
    this.countPositionsInBytes = defaults.isCountPositionsInBytes();
    this.flushRecordOnWrite = defaults.isFlushRecordOnWrite();
    this.trimLeading = defaults.isTrimLeading();
    this.trimTrailing = defaults.isTrimTrailing();
    this.trimLeadingWithinQuotes = defaults.isTrimLeadingWithinQuotes();
    this.trimTrailingWithinQuotes = defaults.isTrimTrailingWithinQuotes();
    this.quoteSymbol = defaults.getQuoteSymbol();
    this.quote = defaults.isQuote();
    this.alwaysQuote = defaults.isAlwaysQuote();
    this.omitFinalFieldDelimiter = defaults.isOmitFinalFieldDelimiter();
    this.omitFinalRepeatDelimiter = defaults.isOmitFinalRepeatDelimiter();
    this.padCharacter = defaults.getPadCharacter();
    this.alignment = defaults.getAlignment();
    this.charset = defaults.getCharset();
    this.byteQuoteSymbolChecker = defaults.getQuoteSymbolByteChecker();
    this.charQuoteSymbolChecker = defaults.getQuoteSymbolCharChecker();
    this.byteTrimmer = defaults.getByteTrimmer();
    this.charTrimmer = defaults.getCharTrimmer();

    this.recordByteDelimiterExtractors = defaults.getRecordByteDelimiterExtractors(); 
    this.segmentByteDelimiterExtractors = defaults.getSegmentByteDelimiterExtractors();
    this.repeatByteDelimiterExtractors = defaults.getRepeatByteDelimiterExtractors(); 
    this.nameByteDelimiterExtractors = defaults.getNameByteDelimiterExtractors();    
    this.fieldByteDelimiterExtractors = defaults.getFieldByteDelimiterExtractors();
    this.subfieldByteDelimiterExtractors = defaults.getSubfieldByteDelimiterExtractors();

    this.recordCharDelimiterExtractors = defaults.getRecordCharDelimiterExtractors(); 
    this.segmentCharDelimiterExtractors = defaults.getSegmentCharDelimiterExtractors();
    this.repeatCharDelimiterExtractors = defaults.getRepeatCharDelimiterExtractors(); 
    this.nameCharDelimiterExtractors = defaults.getNameCharDelimiterExtractors();    
    this.fieldCharDelimiterExtractors = defaults.getFieldCharDelimiterExtractors();
    this.subfieldCharDelimiterExtractors = defaults.getSubfieldCharDelimiterExtractors();

    this.recordDelimiterExtractors = defaults.getRecordDelimiterExtractors(); 
    this.segmentDelimiterExtractors = defaults.getSegmentDelimiterExtractors();
    this.repeatDelimiterExtractors = defaults.getRepeatDelimiterExtractors(); 
    this.nameDelimiterExtractors = defaults.getNameDelimiterExtractors();    
    this.fieldDelimiterExtractors = defaults.getFieldDelimiterExtractors();
    this.subfieldDelimiterExtractors = defaults.getSubfieldDelimiterExtractors();

    this.byteCommentStarterChecker = defaults.getCommentStarterByteChecker();
    this.charCommentStarterChecker = defaults.getCommentStarterCharChecker();
    this.indexBase = defaults.getIndexBase();
  }

  public Charset getDefaultCharset() {
    return charset == null ? Charset.defaultCharset() : charset;
  }

  public Charset getCharset() {
    return charset;
  }

  public ByteTrimmer getByteTrimmer() {
    return byteTrimmer;
  }

  public CharTrimmer getCharTrimmer() {
    return charTrimmer;
  }

  public QuoteSymbolByteChecker getQuoteSymbolByteChecker() {
    return isQuote() ? byteQuoteSymbolChecker : QuoteSymbolByteChecker.NULL; 
  }

  public QuoteSymbolCharChecker getQuoteSymbolCharChecker() {
    return isQuote() ? charQuoteSymbolChecker : QuoteSymbolCharChecker.NULL; 
  }

  public QuoteSymbol getQuoteSymbol() {
    return quoteSymbol;
  }

  public boolean isQuote() {
    return quote;
  }

  public void setQuote(boolean quote) {
    this.quote = quote;
  }

  public boolean isAlwaysQuote() {
    return alwaysQuote;
  }

  public void setAlwaysQuote(boolean alwaysQuote) {
    this.alwaysQuote = alwaysQuote;
  }

  public void setIndexBase(int indexBase) {
    this.indexBase = indexBase;
  }

  public int rebaseIndex(int index) {
    return index - indexBase;
  }

  public int getIndexBase() {
    return indexBase;
  }

  public void setQuoteSymbol(QuoteSymbol quoteSymbol) {
    this.quoteSymbol = quoteSymbol;
    this.byteQuoteSymbolChecker = quoteSymbol.createQuoteSymbolChecker(charset);
    this.charQuoteSymbolChecker = quoteSymbol.createCharQuoteSymbolChecker();
  }

  public boolean isIgnoreTrailingEmptyLines() {
    return ignoreTrailingEmptyLines;
  }

  public void setIgnoreTrailingEmptyLines(boolean ignoreTrailingEmptyLines) {
    this.ignoreTrailingEmptyLines = ignoreTrailingEmptyLines;
  }

  public boolean isIgnoreEmptyLines() {
    return ignoreEmptyLines;
  }

  public void setIgnoreEmptyLines(boolean ignoreEmptyLines) {
    this.ignoreEmptyLines = ignoreEmptyLines;
  }

  public boolean isLineDelimited() {
    return lineDelimited;
  }

  public void setLineDelimited(boolean lineDelimited) {
    this.lineDelimited = lineDelimited;
  }
  public boolean isOmitFinalFieldDelimiter() {
    return omitFinalFieldDelimiter;
  }

  public boolean isOmitFinalRepeatDelimiter() {
    return omitFinalRepeatDelimiter;
  }

  public void setOmitFinalRepeatDelimiter(boolean omitFinalRepeatDelimiter) {
    this.omitFinalRepeatDelimiter = omitFinalRepeatDelimiter;
  }

  public CommentStarter getCommentStarter() {
    return commentStarter;
  }

  public CommentStarterByteChecker getCommentStarterByteChecker() {
    return byteCommentStarterChecker;
  }

  public CommentStarterCharChecker getCommentStarterCharChecker() {
    return charCommentStarterChecker;
  }

  public void setCommentStarter(CommentStarter commentStarter) {
    this.commentStarter = commentStarter;
    this.byteCommentStarterChecker = commentStarter.createByteCommentStarterChecker(charset);
    this.charCommentStarterChecker = commentStarter.createCharCommentStarterChecker();
  }

  public Delimiter getRecordDelimiterForWriting() {
    int index = -1;
    boolean found = false;
    String lineSep = System.getProperty("line.separator");
    for (int i = recordDelimiters.length-1; !found && i >= 0; --i) {
      Delimiter delimiter = recordDelimiters[i];
      if (delimiter.forWriting()) {
        index = i;
        if (delimiter.equalsString(lineSep)) {
          //System.out.println(getClass().getName()+".cons system delimiter found" );
          found = true;
        }
      }
    }
    if (index == -1) {
      throw new ServingXmlException("Cannot find record delimiter for writing.");
    }
    Delimiter recordDelimiter = recordDelimiters[index];
    return recordDelimiter;
  }


  public void setOmitFinalFieldDelimiter(boolean omitFinalFieldDelimiter) {
    this.omitFinalFieldDelimiter = omitFinalFieldDelimiter;
  }

  public Delimiter[] getRecordDelimiters() {
    return recordDelimiters;
  }

  public void setRecordDelimiters(Delimiter[] recordDelimiters) {
    //System.out.println(getClass().getName()+".setRecordDelimiters");
    //for (int i = 0; i < recordDelimiters.length; ++i) {
      //System.out.println(recordDelimiters[i].getClass().getName());
    //}
    this.recordDelimiters = recordDelimiters;
    this.recordByteDelimiterExtractors = makeByteDelimiterExtractors(recordDelimiters, charset);
    this.recordCharDelimiterExtractors = makeCharDelimiterExtractors(recordDelimiters);

    this.recordDelimiterExtractors = makeDelimiterExtractors(recordDelimiters, charset);
  }

  public Delimiter[] getSegmentDelimiters() {
    return segmentDelimiters;
  }

  public void setSegmentDelimiters(Delimiter[] segmentDelimiters) {
    this.segmentDelimiters = segmentDelimiters;
    this.segmentByteDelimiterExtractors = makeByteDelimiterExtractors(segmentDelimiters, charset);
    this.segmentCharDelimiterExtractors = makeCharDelimiterExtractors(segmentDelimiters);

    this.segmentDelimiterExtractors = makeDelimiterExtractors(segmentDelimiters, charset);
  }

  public Delimiter[] getFieldDelimiters() {
    return fieldDelimiters;
  }

  public void setFieldDelimiters(Delimiter[] fieldDelimiters) {
    this.fieldDelimiters = fieldDelimiters;
    this.fieldByteDelimiterExtractors = makeByteDelimiterExtractors(fieldDelimiters, charset);
    this.fieldCharDelimiterExtractors = makeCharDelimiterExtractors(fieldDelimiters);

    this.fieldDelimiterExtractors = makeDelimiterExtractors(fieldDelimiters, charset);
  }

  public Delimiter[] getNameDelimiters() {
    return nameDelimiters;
  }

  public void setNameDelimiters(Delimiter[] nameDelimiters) {
    this.nameDelimiters = nameDelimiters;
    this.nameByteDelimiterExtractors = makeByteDelimiterExtractors(nameDelimiters, charset);
    this.nameCharDelimiterExtractors = makeCharDelimiterExtractors(nameDelimiters);

    this.nameDelimiterExtractors = makeDelimiterExtractors(nameDelimiters, charset);
  }

  public Delimiter[] getRepeatDelimiters() {
    return repeatDelimiters;
  }

  public void setRepeatDelimiters(Delimiter[] repeatDelimiters) {
    this.repeatDelimiters = repeatDelimiters;
    this.repeatByteDelimiterExtractors = makeByteDelimiterExtractors(repeatDelimiters, charset);
    this.repeatCharDelimiterExtractors = makeCharDelimiterExtractors(repeatDelimiters);

    this.repeatDelimiterExtractors = makeDelimiterExtractors(repeatDelimiters, charset);
  }

  public Delimiter[] getSubfieldDelimiters() {
    return subfieldDelimiters;
  }

  public void setSubfieldDelimiters(Delimiter[] subfieldDelimiters) {
    this.subfieldDelimiters = subfieldDelimiters;
    this.subfieldByteDelimiterExtractors = makeByteDelimiterExtractors(subfieldDelimiters, charset);
    this.subfieldCharDelimiterExtractors = makeCharDelimiterExtractors(subfieldDelimiters);

    this.subfieldDelimiterExtractors = makeDelimiterExtractors(subfieldDelimiters, charset);
  }

  public boolean isCountPositionsInBytes() {
    //System.out.println(getClass().getName()+".isCountPositionsInBytes countPositionsInBytes="+countPositionsInBytes);
    return countPositionsInBytes;
  }

  public void setCountPositionsInBytes(boolean countPositionsInBytes) {
    //System.out.println(getClass().getName()+".setCountPositionsInBytes countPositionsInBytes="+countPositionsInBytes);
    this.countPositionsInBytes = countPositionsInBytes;
  }

  public boolean isTrimLeading() {
    return trimLeading;
  }

  public boolean isTrimTrailing() {
    return trimTrailing;
  }

  public boolean isTrimLeadingWithinQuotes() {
    return trimLeadingWithinQuotes;
  }

  public boolean isTrimTrailingWithinQuotes() {
    return trimTrailingWithinQuotes;
  }

  public void setTrimLeading(boolean trimLeading) {
    this.trimLeading = trimLeading;
  }

  public void setTrimTrailing(boolean trimTrailing) {
    this.trimTrailing = trimTrailing;
  }

  public void setTrimLeadingWithinQuotes(boolean trimLeadingWithinQuotes) {
    //System.out.println(getClass().getName()+".setTrimLeadingWithinQuotes trimLeadingWithinQuotes="+trimLeadingWithinQuotes);

    this.trimLeadingWithinQuotes = trimLeadingWithinQuotes;
  }

  public void setTrimTrailingWithinQuotes(boolean trimTrailingWithinQuotes) {
    //System.out.println(getClass().getName()+".setTrimTrailingWithinQuotes trimTrailingWithinQuotes="+trimTrailingWithinQuotes);
    this.trimTrailingWithinQuotes = trimTrailingWithinQuotes;
  }

  public boolean isFlushRecordOnWrite() {
    return flushRecordOnWrite;
  }

  public void setFlushRecordOnWrite(boolean flushRecordOnWrite) {
    this.flushRecordOnWrite = flushRecordOnWrite;
  }

  public Alignment getAlignment() {
    return alignment;
  }

  public void setAlignment(Alignment alignment) {
    this.alignment = alignment;
  }

  public char getPadCharacter() {
    return padCharacter;
  }

  public void setPadCharacter(char padCharacter) {
    this.padCharacter = padCharacter;
  }

  public boolean useQuotes(String value) {
    //System.out.println(getClass().getName()+".useQuotes field delim count = " + fieldDelimiters.length);
    boolean use = alwaysQuote;
    if (!alwaysQuote && quote) {
      for (int i = 0; !use && i < recordDelimiters.length; ++i) {
        use = recordDelimiters[i].occursIn(value);
      }
      for (int i = 0; !use && i < segmentDelimiters.length; ++i) {
        use = segmentDelimiters[i].occursIn(value);
      }
      for (int i = 0; !use && i < fieldDelimiters.length; ++i) {
        //StringBuilder buf=new StringBuilder();
        //fieldDelimiters[i].writeEndDelimiterTo(buf);
        //System.out.println(getClass().getName()+".useQuotes field delim = " + buf);
        use = fieldDelimiters[i].occursIn(value);
      }
      //for (int i = 0; !use && i < nameDelimiters.length; ++i) {
      //  use = nameDelimiters[i].occursIn(value);
      //}
      for (int i = 0; !use && i < repeatDelimiters.length; ++i) {
        use = repeatDelimiters[i].occursIn(value);
      }
      for (int i = 0; !use && i < subfieldDelimiters.length; ++i) {
        use = subfieldDelimiters[i].occursIn(value);
      }
    }
    //System.out.println(getClass().getName()+".useQuotes |" + value + "| quote="+quote+", always=" + alwaysQuote + ", use=" + use);

    return use;
  }

  public ByteDelimiterExtractor[] getRecordByteDelimiterExtractors() {
    return recordByteDelimiterExtractors;
  }

  public ByteDelimiterExtractor[] getSegmentByteDelimiterExtractors() {
    return segmentByteDelimiterExtractors;
  }

  public ByteDelimiterExtractor[] getRepeatByteDelimiterExtractors() {
    return repeatByteDelimiterExtractors;
  }

  public ByteDelimiterExtractor[] getNameByteDelimiterExtractors() {
    return nameByteDelimiterExtractors;
  }

  public ByteDelimiterExtractor[] getFieldByteDelimiterExtractors() {
    return fieldByteDelimiterExtractors;
  }

  public ByteDelimiterExtractor[] getSubfieldByteDelimiterExtractors() {
    return subfieldByteDelimiterExtractors;
  }

  public CharDelimiterExtractor[] getRecordCharDelimiterExtractors() {
    return recordCharDelimiterExtractors;
  }

  public CharDelimiterExtractor[] getSegmentCharDelimiterExtractors() {
    return segmentCharDelimiterExtractors;
  }

  public CharDelimiterExtractor[] getRepeatCharDelimiterExtractors() {
    return repeatCharDelimiterExtractors;
  }

  public CharDelimiterExtractor[] getNameCharDelimiterExtractors() {
    return nameCharDelimiterExtractors;
  }

  public CharDelimiterExtractor[] getFieldCharDelimiterExtractors() {
    return fieldCharDelimiterExtractors;
  }

  public CharDelimiterExtractor[] getSubfieldCharDelimiterExtractors() {
    return subfieldCharDelimiterExtractors;
  }

  public DelimiterExtractor[] getRecordDelimiterExtractors() {
    return recordDelimiterExtractors;
  }

  public DelimiterExtractor[] getSegmentDelimiterExtractors() {
    return segmentDelimiterExtractors;
  }

  public DelimiterExtractor[] getRepeatDelimiterExtractors() {
    return repeatDelimiterExtractors;
  }

  public DelimiterExtractor[] getNameDelimiterExtractors() {
    return nameDelimiterExtractors;
  }

  public DelimiterExtractor[] getFieldDelimiterExtractors() {
    return fieldDelimiterExtractors;
  }

  public DelimiterExtractor[] getSubfieldDelimiterExtractors() {
    return subfieldDelimiterExtractors;
  }

  private ByteDelimiterExtractor[] makeByteDelimiterExtractors(Delimiter[] delimiters, Charset charset) {
    ByteDelimiterExtractor[] delimiterCheckers;
    delimiterCheckers = new ByteDelimiterExtractor[delimiters.length];
    for (int i = 0; i < delimiters.length; ++i) {
      delimiterCheckers[i] = delimiters[i].createByteDelimiterExtractor(charset);
    }
    return delimiterCheckers;
  }

  private CharDelimiterExtractor[] makeCharDelimiterExtractors(Delimiter[] delimiters) {
    CharDelimiterExtractor[] delimiterCheckers;
    delimiterCheckers = new CharDelimiterExtractor[delimiters.length];
    for (int i = 0; i < delimiters.length; ++i) {
      delimiterCheckers[i] = delimiters[i].createCharDelimiterExtractor();
    }
    return delimiterCheckers;
  }

  private DelimiterExtractor[] makeDelimiterExtractors(Delimiter[] delimiters, Charset charset) {
    DelimiterExtractor[] delimiterExtractors = new DelimiterExtractor[delimiters.length];
    for (int i = 0; i < delimiters.length; ++i) {
      ByteDelimiterExtractor byteDelimiterExtractor = delimiters[i].createByteDelimiterExtractor(charset);
      CharDelimiterExtractor charDelimiterExtractor = delimiters[i].createCharDelimiterExtractor();

      delimiterExtractors[i] = new DelimiterExtractor(byteDelimiterExtractor, charDelimiterExtractor);
    }
    return delimiterExtractors;
  }
}

