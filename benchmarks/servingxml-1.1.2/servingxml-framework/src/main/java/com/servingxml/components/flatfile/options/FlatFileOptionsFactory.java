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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.components.quotesymbol.QuoteSymbol;
import com.servingxml.util.Alignment;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileOptionsFactory {

  private TrueFalseEnum ignoreTrailingEmptyLinesEnum = null;
  private TrueFalseEnum ignoreEmptyLinesEnum = null;
  private TrueFalseEnum lineDelimitedEnum = null;
  private DelimiterFactory[] recordDelimiterFactories = null;
  private SegmentDelimiterFactory[] segmentDelimiterFactories = null;
  private DelimiterFactory[] repeatDelimiterFactories = null;
  private FieldDelimiterFactory[] fieldDelimiterFactories = null;
  private NameDelimiterFactory[] nameDelimiterFactories = null;
  private SubfieldDelimiterFactory[] subfieldDelimiterFactories = null;
  private TrueFalseEnum countPositionsInBytesEnum = null;
  private TrueFalseEnum flushRecordOnWriteEnum = null;
  private TrueFalseEnum trimLeadingEnum = null;
  private TrueFalseEnum trimTrailingEnum = null;
  private TrueFalseEnum trimLeadingWithinQuotesEnum = null;
  private TrueFalseEnum trimTrailingWithinQuotesEnum = null;
  private QuoteSymbol quoteSymbol = null;
  private QuoteEnum quoteEnum = null;
  private TrueFalseEnum omitFinalFieldDelimiterEnum = null;
  private TrueFalseEnum omitFinalRepeatDelimiterEnum = null;
  private String padCharacter = ""; 
  private Alignment alignment = null;
  private CommentStarter commentStarter = null;
  private int indexBase = -1;

  public FlatFileOptionsFactory() {
    //System.out.println(getClass().getName()+".cons");
    //for (int i = 0; i < recordDelimiterFactories.length; ++i) {
      //System.out.println(recordDelimiterFactories[i].getClass().getName());
    //}
  }

  public void setIndexBase(int indexBase) {
    this.indexBase = indexBase;
  }

  public boolean hasRepeatDelimiters() {
    return repeatDelimiterFactories != null && repeatDelimiterFactories.length > 0;
  }

  public void setRecordDelimiterFactories(DelimiterFactory[] recordDelimiterFactories) {

    //System.out.println(getClass().getName()+".setRecordDelimiterFactories");
    //for (int i = 0; i < recordDelimiterFactories.length; ++i) {
      //System.out.println(recordDelimiterFactories[i].getClass().getName());
    //}
    this.recordDelimiterFactories = recordDelimiterFactories;
  }

  public void setCommentStarter(CommentStarter commentStarter) {
    this.commentStarter = commentStarter;
  }

  public CommentStarter getCommentStarter() {
    return commentStarter;
  }

  public DelimiterFactory[] getRecordDelimiterFactories() {
    return recordDelimiterFactories == null ? RecordDelimiterFactory.EMPTY_ARRAY : recordDelimiterFactories;
  }

  public FieldDelimiterFactory[] getFieldDelimiterFactories() {
    return fieldDelimiterFactories == null ? FieldDelimiterFactory.EMPTY_ARRAY : fieldDelimiterFactories;
  }

  public DelimiterFactory[] getSegmentDelimiterFactories() {
    return segmentDelimiterFactories == null ? SegmentDelimiterFactory.EMPTY_ARRAY : segmentDelimiterFactories;
  }

  public void setSegmentDelimiterFactories(SegmentDelimiterFactory[] segmentDelimiterFactories) {
    this.segmentDelimiterFactories = segmentDelimiterFactories;
  }

  public void setFieldDelimiterFactories(FieldDelimiterFactory[] fieldDelimiterFactories) {
    this.fieldDelimiterFactories = fieldDelimiterFactories;
  }

  public void setNameDelimiterFactories(NameDelimiterFactory[] nameDelimiterFactories) {
    this.nameDelimiterFactories = nameDelimiterFactories;
  }

  public void setRepeatDelimiterFactories(DelimiterFactory[] repeatDelimiterFactories) {
    this.repeatDelimiterFactories = repeatDelimiterFactories;
  }

  public void setSubfieldDelimiterFactories(SubfieldDelimiterFactory[] subfieldDelimiterFactories) {
    this.subfieldDelimiterFactories = subfieldDelimiterFactories;
  }

  public void setQuoteSymbol(QuoteSymbol quoteSymbol) {
    this.quoteSymbol = quoteSymbol;
  }

  public void setQuote(QuoteEnum quoteEnum) {
    this.quoteEnum = quoteEnum;
  }

  public void setAlignment(Alignment alignment) {
    this.alignment = alignment;
  }

  public void setPadCharacter(String padCharacter) {
    this.padCharacter = padCharacter;
  }

  public void setOmitFinalFieldDelimiter(TrueFalseEnum omitFinalFieldDelimiterEnum) {
    this.omitFinalFieldDelimiterEnum = omitFinalFieldDelimiterEnum;
  }

  public void setOmitFinalRepeatDelimiter(TrueFalseEnum omitFinalRepeatDelimiterEnum) {
    this.omitFinalRepeatDelimiterEnum = omitFinalRepeatDelimiterEnum;
  }

  public void setIgnoreTrailingEmptyLines(TrueFalseEnum ignoreTrailingEmptyLinesEnum) {
    this.ignoreTrailingEmptyLinesEnum = ignoreTrailingEmptyLinesEnum;
  }

  public void setIgnoreEmptyLines(TrueFalseEnum ignoreEmptyLinesEnum) {
    this.ignoreEmptyLinesEnum = ignoreEmptyLinesEnum;
  }

  public void setLineDelimited(TrueFalseEnum lineDelimitedEnum) {
    this.lineDelimitedEnum = lineDelimitedEnum;
  }

  public void setCountPositionsInBytes(TrueFalseEnum countPositionsInBytesEnum) {
    this.countPositionsInBytesEnum = countPositionsInBytesEnum;
  }

  public void setFlushRecordOnWrite(TrueFalseEnum flushRecordOnWriteEnum) {
    this.flushRecordOnWriteEnum = flushRecordOnWriteEnum;
    //System.out.println(getClass().getName()+".setFlushRecordOnWrite value=" + flushRecordOnWriteEnum);
  }

  public void setTrimLeading(TrueFalseEnum trimLeadingEnum) {
    this.trimLeadingEnum = trimLeadingEnum;
  }

  public void setTrimTrailing(TrueFalseEnum trimTrailingEnum) {
    this.trimTrailingEnum = trimTrailingEnum;
  }

  public void setTrimLeadingWithinQuotes(TrueFalseEnum trimLeadingWithinQuotesEnum) {
    //System.out.println(getClass().getName()+".setTrimLeadingWithinQuotes trimLeadingWithinQuotes="+trimLeadingWithinQuotesEnum);
    this.trimLeadingWithinQuotesEnum = trimLeadingWithinQuotesEnum;
  }

  public void setTrimTrailingWithinQuotes(TrueFalseEnum trimTrailingWithinQuotesEnum) {
    //System.out.println(getClass().getName()+".setTrimTrailingWithinQuotes trimTrailingWithinQuotes="+trimTrailingWithinQuotesEnum);
    this.trimTrailingWithinQuotesEnum = trimTrailingWithinQuotesEnum;
  }

  private boolean valueOf(TrueFalseEnum optionValue, boolean defaultValue) {
    boolean value = optionValue != null ? optionValue.booleanValue() : defaultValue;
    return value;
  }

  private Delimiter[] valueOf(ServiceContext context, Flow flow,
    DelimiterFactory[] delimiterFactories) {
    return valueOf(context, flow, delimiterFactories, Delimiter.EMPTY_DELIMITER_ARRAY);
  }

  private Delimiter[] valueOf(ServiceContext context, Flow flow,
    DelimiterFactory[] delimiterFactories, Delimiter[] defaultValue) {

    Delimiter[] delimiters;
    if (delimiterFactories != null) {
      delimiters = new Delimiter[delimiterFactories.length];
      for (int i = 0; i < delimiters.length; ++i) {
        delimiters[i] = delimiterFactories[i].createDelimiter(context, flow);
      }
    } else {
      delimiters = defaultValue;
    }
    //  Remove empty delimiters
    int count = 0;
    for (int i = 0; i < delimiters.length; ++i) {
      if (delimiters[i].isEmpty()) {
        ++count;
      }
    }
    if (count > 0) {
      Delimiter[] oldDelimiters = delimiters;
      delimiters = new Delimiter[oldDelimiters.length - count];
      int j = 0;
      for (int i = 0; i < oldDelimiters.length; ++i) {
        if (!oldDelimiters[i].isEmpty()) {
          delimiters[j++] = oldDelimiters[i];
        }
      }
    }
    return delimiters;
  }

  private Delimiter[] valueOf(Delimiter[] delimiters, Delimiter[] defaultValue) {

    return delimiters == null ? delimiters : defaultValue;
  }

  public FlatFileOptions createFlatFileOptions(ServiceContext context, Flow flow, FlatFileOptions defaults) {
    //System.out.println(getClass().getName()+".createFlatFileOptions");
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(defaults);
    //System.out.println(getClass().getName()+".createFlatFileOptions before initialize");
    initialize(context, flow, flatFileOptions);
    //System.out.println(getClass().getName()+".createFlatFileOptions after initialize");

    return flatFileOptions;
  }

  public FlatFileOptions createFlatFileOptions(ServiceContext context, Flow flow, boolean delimitedFile, 
                                               boolean countPositionsInBytes, Charset charset) {
    //if (charset == null) {
      //charset = Charset.defaultCharset();
    //}
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(charset, delimitedFile, countPositionsInBytes);
    initialize(context, flow, flatFileOptions);

    return flatFileOptions;
  }

  private void initialize(ServiceContext context, Flow flow, FlatFileOptionsImpl flatFileOptions) {
    //System.out.println(getClass().getName()+".initialize enter");
    //System.out.println(getClass().getName()+".initialize "+recordDelimiterFactories.length);
    //for (int i = 0; i < recordDelimiterFactories.length; ++i) {
      //System.out.println(recordDelimiterFactories[i].getClass().getName());
    //}
    if (recordDelimiterFactories != null) {
      Delimiter[] myRecordDelimiters = valueOf(context, flow, recordDelimiterFactories);
      //System.out.println(getClass().getName()+".initialize 2 "+myRecordDelimiters.length);
      //for (int i = 0; i < myRecordDelimiters.length; ++i) {
        //System.out.println(myRecordDelimiters[i].getClass().getName());
      //}
      flatFileOptions.setRecordDelimiters(myRecordDelimiters);
    }
    if (segmentDelimiterFactories != null) {
      Delimiter[] mySegmentDelimiters = valueOf(context, flow, segmentDelimiterFactories);
      flatFileOptions.setSegmentDelimiters(mySegmentDelimiters);
    }
    if (fieldDelimiterFactories != null) {
      Delimiter[] myFieldDelimiters = valueOf(context, flow, fieldDelimiterFactories);
      flatFileOptions.setFieldDelimiters(myFieldDelimiters);
    }
    if (nameDelimiterFactories != null) {
      Delimiter[] myNameDelimiters = valueOf(context, flow, nameDelimiterFactories);
      flatFileOptions.setNameDelimiters(myNameDelimiters);
    }
    if (repeatDelimiterFactories != null) {
      Delimiter[] myRepeatDelimiters = valueOf(context, flow, repeatDelimiterFactories);
      flatFileOptions.setRepeatDelimiters(myRepeatDelimiters);
    }
    if (subfieldDelimiterFactories != null) {
      Delimiter[] mySubfieldDelimiters = valueOf(context, flow, subfieldDelimiterFactories);
      flatFileOptions.setSubfieldDelimiters(mySubfieldDelimiters);
    }
    if (ignoreEmptyLinesEnum != null) {
      flatFileOptions.setIgnoreEmptyLines(ignoreEmptyLinesEnum.booleanValue());
    }
    if (ignoreTrailingEmptyLinesEnum != null) {
      flatFileOptions.setIgnoreTrailingEmptyLines(ignoreTrailingEmptyLinesEnum.booleanValue());
    }
    if (lineDelimitedEnum != null) {
      flatFileOptions.setLineDelimited(lineDelimitedEnum.booleanValue());
    }
    if (countPositionsInBytesEnum != null) {
      flatFileOptions.setCountPositionsInBytes(countPositionsInBytesEnum.booleanValue());
    }
    if (trimLeadingEnum != null) {
      flatFileOptions.setTrimLeading(trimLeadingEnum.booleanValue());
    }
    if (trimTrailingEnum != null) {
      flatFileOptions.setTrimTrailing(trimTrailingEnum.booleanValue());
    }
    if (trimLeadingWithinQuotesEnum != null) {
      flatFileOptions.setTrimLeadingWithinQuotes(trimLeadingWithinQuotesEnum.booleanValue());
    }
    if (trimTrailingWithinQuotesEnum != null) {
      flatFileOptions.setTrimTrailingWithinQuotes(trimTrailingWithinQuotesEnum.booleanValue());
    }
    if (flushRecordOnWriteEnum != null) {
      flatFileOptions.setFlushRecordOnWrite(flushRecordOnWriteEnum.booleanValue());
    }
    if (omitFinalFieldDelimiterEnum != null) {
      flatFileOptions.setOmitFinalFieldDelimiter(omitFinalFieldDelimiterEnum.booleanValue());
    }
    if (omitFinalRepeatDelimiterEnum != null) {
      flatFileOptions.setOmitFinalRepeatDelimiter(omitFinalRepeatDelimiterEnum.booleanValue());
    }

    if (quoteEnum != null) {
      flatFileOptions.setQuote(quoteEnum.always() || quoteEnum.auto());
      flatFileOptions.setAlwaysQuote(quoteEnum.always());
    }
    if (quoteSymbol != null) {
      flatFileOptions.setQuoteSymbol(quoteSymbol);
    }
    if (padCharacter.length() > 0) {
      flatFileOptions.setPadCharacter(padCharacter.charAt(0));
    }
    if (alignment != null) {
      flatFileOptions.setAlignment(alignment);
    }
    if (commentStarter != null) {
      flatFileOptions.setCommentStarter(commentStarter);
    }
    if (indexBase >= 0) {
      flatFileOptions.setIndexBase(indexBase);
    }
    //System.out.println(getClass().getName()+".initialize leave");
  }
}

