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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class DecomposeRecordAppenderAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Name onFieldName = Name.EMPTY;
  private Name ofRecordTypeName = Name.EMPTY;
  private Name newRecordTypeName = Name.EMPTY;

  public void setOfRecordType(Name ofRecordTypeName) {
    this.ofRecordTypeName = ofRecordTypeName;
  }

  public void setCompositeRecordType(Name ofRecordTypeName) {
    this.ofRecordTypeName = ofRecordTypeName;
  }

  public void setRepeatingGroupField(Name onFieldName) {
    this.onFieldName = onFieldName;
  }

  public void setRepeatingGroup(Name onFieldName) {
    this.onFieldName = onFieldName;
  }

  public void setField(Name onFieldName) {
    this.onFieldName = onFieldName;
  }

  public void setOnField(Name onFieldName) {
    this.onFieldName = onFieldName;
  }

  public void setRecordType(Name ofRecordTypeName) {
    this.ofRecordTypeName = ofRecordTypeName;
  }

  public void setNewRecordType(Name newRecordTypeName) {
    this.newRecordTypeName = newRecordTypeName;
  }

  public void setSubrecordType(Name newRecordTypeName) {
    this.newRecordTypeName = newRecordTypeName;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public RecordFilterAppender assemble(ConfigurationContext context) {
    if (ofRecordTypeName.isEmpty()) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                             context.getElement().getTagName(),
                                                             "compositeRecordType");
      throw new ServingXmlException(msg);
    }
    if (onFieldName.isEmpty()) {
      String msg = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                             context.getElement().getTagName(),
                                                             "repeatingGroupField");
      throw new ServingXmlException(msg);
    }

    RecordFilterAppender recordFilterAppender = new DecomposeRecordAppender(ofRecordTypeName, onFieldName, newRecordTypeName);

    if (parameterDescriptors.length > 0) {
      recordFilterAppender = new RecordFilterAppenderPrefilter(recordFilterAppender,parameterDescriptors);
    }
    return recordFilterAppender;
  }
}


