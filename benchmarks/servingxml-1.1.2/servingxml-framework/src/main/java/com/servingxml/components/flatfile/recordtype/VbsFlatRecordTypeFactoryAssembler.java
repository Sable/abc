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

package com.servingxml.components.flatfile.recordtype;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;
import com.servingxml.util.Name;

//  Revisit

/**
 * The <code>FlatRecordTypeFactoryAssembler</code> implements an assembler for
 * assembling flat file record type objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class VbsFlatRecordTypeFactoryAssembler extends FlatFileOptionsFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private FlatRecordFieldFactory[] sdwFieldTypeFactories = new FlatRecordFieldFactory[0];
  private RecordCombinationFactory[] recordCombinationFactories = new RecordCombinationFactory[0];

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(RecordCombinationFactory[] recordCombinationFactories) {

    this.recordCombinationFactories = recordCombinationFactories;
  }

  public void injectComponent(FlatRecordFieldFactory[] sdwFieldTypeFactories) {

    this.sdwFieldTypeFactories = sdwFieldTypeFactories;
  }

  public FlatRecordTypeFactory assemble(ConfigurationContext context) {
    if (recordCombinationFactories.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "sx:combineSegments");
      throw new ServingXmlException(message);
    }
 
    if (sdwFieldTypeFactories.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"sx:delimitedField or sx:positionalField");
    }
    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    FlatRecordTypeFactory recordTypeFactory = new VbsFlatRecordTypeFactory(sdwFieldTypeFactories,
                                                                                recordCombinationFactories,
                                                                                flatFileOptionsFactory);
    if (parameterDescriptors.length > 0) {
      recordTypeFactory = new FlatRecordTypeFactoryPrefilter(recordTypeFactory,
                                                             parameterDescriptors);
    }
    return recordTypeFactory;
  }
}
