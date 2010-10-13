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

package com.servingxml.components.recordio;

import org.w3c.dom.Element;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.common.SubstitutionExprValueEvaluator;
import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.expr.substitution.LiteralSubstitutionExpr;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.app.Flow;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.record.Record;
import com.servingxml.util.system.LogLevelEnum;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.components.string.StringFactoryCompiler;
import com.servingxml.components.string.StringValueEvaluator;
import com.servingxml.components.string.StringFactory;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class LogAppenderAssembler {

  private String message = "";
  private String level = LogLevelEnum.ERROR.toString();

  public void setMessage(String message) {
    this.message = message;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {

    LogLevelEnum logLevelEnum;
    try {
      logLevelEnum = LogLevelEnum.parse(level);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
                                                                 context.getElement().getTagName(), "level");
      e = e.supplementMessage(message);
      throw e;
    }

    ValueEvaluator valueEvaluator;
    if (message.length() > 0) {
      SubstitutionExpr subExpr = SubstitutionExpr.parseString(context.getQnameContext(),message);
      valueEvaluator = new SubstitutionExprValueEvaluator(subExpr);
    } else {
      StringFactory stringFactory = StringFactoryCompiler.fromStringables(context, context.getElement());
      valueEvaluator = new StringValueEvaluator(stringFactory);
    }

    RecordFilterAppender recordFilterAppender = new LogAppender(valueEvaluator, logLevelEnum);
    return recordFilterAppender;
  }
}

class LogAppender extends AbstractRecordFilterAppender     
implements RecordFilterAppender {
  private final ValueEvaluator valueEvaluator;
  private final LogLevelEnum logLevelEnum;

  public LogAppender(ValueEvaluator valueEvaluator, LogLevelEnum logLevelEnum) {
    this.valueEvaluator = valueEvaluator;
    this.logLevelEnum = logLevelEnum;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
                                     RecordFilterChain pipeline) {

    RecordFilter recordFilter = new Log(valueEvaluator, logLevelEnum); 
    pipeline.addRecordFilter(recordFilter);
  }
}

class Log extends AbstractRecordFilter {
  private final ValueEvaluator valueEvaluator;
  private final LogLevelEnum logLevelEnum;

  public Log(ValueEvaluator valueEvaluator, LogLevelEnum logLevelEnum) {
    this.valueEvaluator = valueEvaluator;
    this.logLevelEnum = logLevelEnum;
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    String s = valueEvaluator.evaluateString(context, flow);
    logLevelEnum.log(context,s);
    super.writeRecord(context, flow);
  }
}
