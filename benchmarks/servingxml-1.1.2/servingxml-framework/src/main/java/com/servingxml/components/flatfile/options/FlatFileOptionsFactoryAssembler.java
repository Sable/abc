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

import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.Alignment;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.quotesymbol.QuoteSymbol;
import com.servingxml.util.Alignment;

public abstract class FlatFileOptionsFactoryAssembler {
  
  private DelimiterFactory[] recordDelimiterFactories = null;
  private SegmentDelimiterFactory[] segmentDelimiterFactories = null;
  private FieldDelimiterFactory[] fieldDelimiterFactories = null;
  private RepeatDelimiterFactory[] repeatDelimiterFactories = null;
  private SubfieldDelimiterFactory[] subfieldDelimiterFactories = null;
  private NameDelimiterFactory[] nameDelimiterFactories = null;
  private QuoteSymbol quoteSymbol = null;
  private String flushRecordOnWrite = "";
  private String trimLeading = "";
  private String trimTrailing = "";
  private String trimLeadingWithinQuotes = "";
  private String trimTrailingWithinQuotes = "";
  private String quoteValue = "";
  private String omitFinalFieldDelimiter = "";
  private String omitFinalRepeatDelimiter = "";
  private String ignoreTrailingEmptyLines = "";
  private String ignoreEmptyLines = "";
  private String lineDelimited = "";
  private String countPositionsInBytes = "";
  private int indexBase = -1;

  private boolean alwaysQuote = false;
  private String padCharacter = "";
  private Alignment alignment = null;
  private CommentStarter[] commentStarters = new CommentStarter[0];

  public void injectComponent(RecordDelimiterFactory[] recordDelimiterFactories) {

    this.recordDelimiterFactories = recordDelimiterFactories;
  }

  public void injectComponent(SegmentDelimiterFactory[] segmentDelimiterFactories) {

    this.segmentDelimiterFactories = segmentDelimiterFactories;
  }

  public void injectComponent(RepeatDelimiterFactory[] repeatDelimiterFactories) {

    this.repeatDelimiterFactories = repeatDelimiterFactories;
  }

  public void injectComponent(FieldDelimiterFactory[] fieldDelimiterFactories) {
    this.fieldDelimiterFactories = fieldDelimiterFactories;
  }

  public void injectComponent(NameDelimiterFactory[] nameDelimiterFactories) {
    this.nameDelimiterFactories = nameDelimiterFactories;
  }

  public void injectComponent(SubfieldDelimiterFactory[] subfieldDelimiterFactories) {

    this.subfieldDelimiterFactories = subfieldDelimiterFactories;
  }

  public void injectComponent(QuoteSymbol quoteSymbol) {

    this.quoteSymbol = quoteSymbol;
  }

  public void injectComponent(CommentStarter[] commentStarters) {

    this.commentStarters = commentStarters;
  }

  public void setFlushRecordOnWrite(String value) {
    this.flushRecordOnWrite = value;
    //System.out.println(getClass().getName()+".setFlushRecordOnWrite value=" + value);
  }

  public void setIndexBase(int indexBase) {
    this.indexBase = indexBase;
  }

  public void setZeroBased(boolean zeroBased) {
    this.indexBase = zeroBased ? 0 : 1;
  }

  public void setTrim(String value) {
    this.trimLeading = value;
    this.trimTrailing = value;
  }

  public void setTrimLeading(String trimLeading) {
    this.trimLeading = trimLeading;
  }

  public void setTrimTrailing(String trimTrailing) {
    this.trimTrailing = trimTrailing;
  }

  public void setTrimWithinQuotes(String value) {
    //System.out.println(getClass().getName()+".setTrimWithinQuotes trimLeadingWithinQuotes="+trimLeadingWithinQuotes + ", trimLeadingWithinQuotes="+trimLeadingWithinQuotes);
    this.trimLeadingWithinQuotes = value;
    this.trimTrailingWithinQuotes = value;
  }

  public void setTrimLeadingWithinQuotes(String trimLeadingWithinQuotes) {
    this.trimLeadingWithinQuotes = trimLeadingWithinQuotes;
  }

  public void setTrimTrailingWithinQuotes(String trimTrailingWithinQuotes) {
    this.trimTrailingWithinQuotes = trimTrailingWithinQuotes;
  }

  public void setTrimQuoted(String value) {
    //System.out.println(getClass().getName()+".setTrimWithinQuotes trimLeadingWithinQuotes="+trimLeadingWithinQuotes + ", trimLeadingWithinQuotes="+trimLeadingWithinQuotes);
    this.trimLeadingWithinQuotes = value;
    this.trimTrailingWithinQuotes = value;
  }

  public void setCountPositionsInBytes(String countPositionsInBytes) {
    this.countPositionsInBytes = countPositionsInBytes;
  }


  public void setQuote(String value) {
    this.quoteValue = value;
  }

  public void setOmitFinalFieldDelimiter(String omitFinalFieldDelimiter) {
    this.omitFinalFieldDelimiter = omitFinalFieldDelimiter;
  }

  public void setIgnoreTrailingEmptyLines(String ignoreTrailingEmptyLines) {
    this.ignoreTrailingEmptyLines = ignoreTrailingEmptyLines;
  }

  public void setIgnoreEmptyLines(String ignoreEmptyLines) {
    this.ignoreEmptyLines = ignoreEmptyLines;
  }

  public void setLineDelimited(String lineDelimited) {
    this.lineDelimited = lineDelimited;
  }

  public void setOmitFinalRepeatDelimiter(String omitFinalRepeatDelimiter) {
    this.omitFinalRepeatDelimiter = omitFinalRepeatDelimiter;
  }

  public void setAlwaysQuote(String value) {
    if (value.length() > 0) {
      if (value.equals(SystemConstants.YES)) {
        alwaysQuote = true;
      }
    }
  }

  public void setPadCharacter(String padCharacter) {
    this.padCharacter = padCharacter;
  }

  public void setJustify(String value) {
    alignment = Alignment.parse(value);
  }

  public FlatFileOptionsFactory assembleFlatFileOptions(ConfigurationContext context) {
    //System.out.println(getClass().getName()+".assemble enter");

    FlatFileOptionsFactory flatFileOptionsFactory = new FlatFileOptionsFactory();

    if (commentStarters.length > 0) {
      CommentStarter commentStarter;
      if (commentStarters.length == 1) {
        commentStarter = commentStarters[0];
      } else {
        commentStarter = new MultipleCommentSymbol(commentStarters);
      }
      flatFileOptionsFactory.setCommentStarter(commentStarter);
    }

    //System.out.println(getClass().getName()+".assembleFlatFileOptions");
    //for (int i = 0; i < recordDelimiterFactories.length; ++i) {
      //System.out.println(recordDelimiterFactories[i].getClass().getName());
    //}
    if (recordDelimiterFactories != null) {
      flatFileOptionsFactory.setRecordDelimiterFactories(recordDelimiterFactories);
    }
    if (segmentDelimiterFactories != null) {
      flatFileOptionsFactory.setSegmentDelimiterFactories(segmentDelimiterFactories);
    }
    if (repeatDelimiterFactories != null) {
      flatFileOptionsFactory.setRepeatDelimiterFactories(repeatDelimiterFactories);
    }
    if (fieldDelimiterFactories != null) {
      flatFileOptionsFactory.setFieldDelimiterFactories(fieldDelimiterFactories);
    }
    if (nameDelimiterFactories != null) {
      flatFileOptionsFactory.setNameDelimiterFactories(nameDelimiterFactories);
    }
    if (subfieldDelimiterFactories != null) {
      flatFileOptionsFactory.setSubfieldDelimiterFactories(subfieldDelimiterFactories);
    }
    if (quoteSymbol != null) {
      flatFileOptionsFactory.setQuoteSymbol(quoteSymbol);
    }

    if (countPositionsInBytes.length() > 0) {
      TrueFalseEnum countPositionsInBytesIndicator;
      try {
        countPositionsInBytesIndicator = TrueFalseEnum.parse(countPositionsInBytes);
        flatFileOptionsFactory.setCountPositionsInBytes(countPositionsInBytesIndicator);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "countPositionsInBytes");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (flushRecordOnWrite.length() > 0) {
      TrueFalseEnum flushRecordOnWriteIndicator;
      try {
        flushRecordOnWriteIndicator = TrueFalseEnum.parse(flushRecordOnWrite);
        flatFileOptionsFactory.setFlushRecordOnWrite(flushRecordOnWriteIndicator);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "flushRecordOnWrite");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (trimLeading.length() > 0) {
      TrueFalseEnum trimLeadingIndicator;
      try {
        trimLeadingIndicator = TrueFalseEnum.parse(trimLeading);
        flatFileOptionsFactory.setTrimLeading(trimLeadingIndicator);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "trimLeading");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (trimTrailing.length() > 0) {
      TrueFalseEnum trimTrailingIndicator;
      try {
        trimTrailingIndicator = TrueFalseEnum.parse(trimTrailing);
        flatFileOptionsFactory.setTrimTrailing(trimTrailingIndicator);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "trimTrailing");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (trimLeadingWithinQuotes.length() > 0) {
      TrueFalseEnum trimLeadingWithinQuotesIndicator;
      try {
        trimLeadingWithinQuotesIndicator = TrueFalseEnum.parse(trimLeadingWithinQuotes);
        flatFileOptionsFactory.setTrimLeadingWithinQuotes(trimLeadingWithinQuotesIndicator);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "trimLeadingWithinQuotes");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (trimTrailingWithinQuotes.length() > 0) {
      TrueFalseEnum trimTrailingWithinQuotesIndicator;
      try {
        trimTrailingWithinQuotesIndicator = TrueFalseEnum.parse(trimTrailingWithinQuotes);
        flatFileOptionsFactory.setTrimTrailingWithinQuotes(trimTrailingWithinQuotesIndicator);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "trimTrailingWithinQuotes");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (omitFinalFieldDelimiter.length() > 0) {
      TrueFalseEnum omitFinalFieldDelimiterEnum;
      try {
        omitFinalFieldDelimiterEnum = TrueFalseEnum.parse(omitFinalFieldDelimiter);
        flatFileOptionsFactory.setOmitFinalFieldDelimiter(omitFinalFieldDelimiterEnum);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "omitFinalFieldDelimiter");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (indexBase >= 0) {
      flatFileOptionsFactory.setIndexBase(indexBase);
    }

    if (omitFinalRepeatDelimiter.length() > 0) {
      TrueFalseEnum omitFinalRepeatDelimiterEnum;
      try {
        omitFinalRepeatDelimiterEnum = TrueFalseEnum.parse(omitFinalRepeatDelimiter);
        flatFileOptionsFactory.setOmitFinalRepeatDelimiter(omitFinalRepeatDelimiterEnum);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "omitFinalRepeatDelimiter");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (ignoreTrailingEmptyLines.length() > 0) {
      TrueFalseEnum ignoreTrailingEmptyLinesEnum;
      try {
        ignoreTrailingEmptyLinesEnum = TrueFalseEnum.parse(ignoreTrailingEmptyLines);
        flatFileOptionsFactory.setIgnoreTrailingEmptyLines(ignoreTrailingEmptyLinesEnum);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "ignoreTrailingEmptyLines");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (ignoreTrailingEmptyLines.length() > 0) {
      TrueFalseEnum ignoreTrailingEmptyLinesEnum;
      try {
        ignoreTrailingEmptyLinesEnum = TrueFalseEnum.parse(ignoreTrailingEmptyLines);
        flatFileOptionsFactory.setIgnoreEmptyLines(ignoreTrailingEmptyLinesEnum);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "ignoreTrailingEmptyLines");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (lineDelimited.length() > 0) {
      TrueFalseEnum lineDelimitedEnum;
      try {
        lineDelimitedEnum = TrueFalseEnum.parse(lineDelimited);
        flatFileOptionsFactory.setLineDelimited(lineDelimitedEnum);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "lineDelimited");
        e = e.supplementMessage(message);
        throw e;
      }
    }

    if (alwaysQuote) {
      quoteValue = QuoteEnum.ALWAYS.toString();
    }
    if (quoteValue.length() > 0) {
      try {
        QuoteEnum quoteEnum = QuoteEnum.parse(quoteValue);
        flatFileOptionsFactory.setQuote(quoteEnum);
      } catch (ServingXmlException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
          context.getElement().getTagName(), "quote");
        e = e.supplementMessage(message);
        throw e;
      }
    }
    if (padCharacter.length() > 0) {
      flatFileOptionsFactory.setPadCharacter(padCharacter);
    }
    if (alignment != null) {
      flatFileOptionsFactory.setAlignment(alignment);
    }
    //System.out.println(getClass().getName()+".assemble leave");
    return flatFileOptionsFactory;
  }
}

