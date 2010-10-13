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
import com.servingxml.app.Flow;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.ParameterBuilder;
import com.servingxml.util.xml.DomHelper;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class DiscardAppenderAssembler {

  public RecordFilterAppender assemble(ConfigurationContext context) {
    Element paramElement = context.getElement();
    String innerText = DomHelper.getInnerText(paramElement);
    if (innerText != null) {
      innerText = innerText.trim();                 
    } else {
      innerText = "";
    }

    RecordFilterAppender recordFilterAppender = new DiscardAppender(innerText);
    return recordFilterAppender;
  }
}

class DiscardAppender extends AbstractRecordFilterAppender     
implements RecordFilterAppender {
  private final String message;
  
  public DiscardAppender(String message) {
    this.message = message;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
  RecordFilterChain pipeline) {

    RecordFilter recordFilter = new Discard(message); 
    pipeline.addRecordFilter(recordFilter);
  }
}

class Discard extends AbstractRecordFilter {
  private final String message;

  public Discard(String message) {
    this.message = message;
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    Record record = flow.getRecord();
    String[] args = new String[3];
    args[0] = record.getRecordType().getName().toString();
    if (flow.getCurrentLineNumber() != 0) {
      args[1] = Integer.toString(flow.getCurrentLineNumber());
    } else {
      args[1]="";
    }
    args[2] = message;
    String s = MessageFormatter.getInstance().getMessage(ServingXmlMessages.RECORD_ERROR,
                                                               args);
    ParameterBuilder paramBuilder = new ParameterBuilder(flow.getParameters());
    paramBuilder.setString(SystemConstants.MESSAGE_NAME, s);
    Record newParameters = paramBuilder.toRecord();
    Flow newFlow = flow.replaceParameters(context, newParameters);
    getDiscardWriter().writeRecord(context, newFlow);
  }
}
