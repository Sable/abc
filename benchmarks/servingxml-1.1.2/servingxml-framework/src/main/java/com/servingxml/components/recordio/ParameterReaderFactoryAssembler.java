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

import com.servingxml.util.Name;
import com.servingxml.util.SystemConstants;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.app.ParameterDescriptor;

/**
 * The <code>ParameterReaderFactoryAssembler</code> implements an assembler for
 * assembling <code>ParameterReaderFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ParameterReaderFactoryAssembler {
  
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Name recordTypeName = SystemConstants.PARAMETERS_TYPE_NAME;
  
  public ParameterReaderFactoryAssembler() {
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }
  
  public void setRecordType(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public RecordReaderFactory assemble(ConfigurationContext context) {
    
    RecordReaderFactory readerFactory = new ParameterReaderFactory(
      recordTypeName);
    if (parameterDescriptors.length > 0) {
      readerFactory = new RecordReaderFactoryPrefilter(readerFactory,parameterDescriptors);
    }
    return readerFactory;
  }
}

