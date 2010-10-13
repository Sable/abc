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

package com.servingxml.components.flatfile.layout;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;
import com.servingxml.components.flatfile.recordtype.FlatRecordType;
import com.servingxml.components.flatfile.recordtype.FlatRecordTypeFactory;

/**
 * The <code>FlatFileBodyFactoryAssembler</code> implements an assembler for
 * assembling <code>FlatFileBodyFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileBodyFactoryAssembler extends FlatFileOptionsFactoryAssembler {
                                                       
  private FlatRecordTypeFactory flatRecordTypeFactory = null;

  public FlatFileBodyFactoryAssembler() {
  }
  
  public void injectComponent(FlatRecordTypeFactory flatRecordTypeFactory) {
    this.flatRecordTypeFactory = flatRecordTypeFactory;
  }

  public FlatFileBodyFactory assemble(ConfigurationContext context) {
    if (flatRecordTypeFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_CHOICE_REQUIRED,context.getElement().getTagName(),
                                                                 "sx:flatRecordType, sx:flatRecordTypeChoice");
      throw new ServingXmlException(message);
    }

    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    return new FlatFileBodyFactory(flatRecordTypeFactory, flatFileOptionsFactory);
  }
}                                                                 
