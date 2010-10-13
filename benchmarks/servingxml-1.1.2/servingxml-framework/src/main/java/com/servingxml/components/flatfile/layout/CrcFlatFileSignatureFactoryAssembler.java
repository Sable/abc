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
import com.servingxml.util.Name;

/**
 * The <code>FlatFileSignatureAssembler</code> implements an assembler for
 * assembling <code>FlatFile</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CrcFlatFileSignatureFactoryAssembler {
                                                       
  private Name recordTypeName = Name.EMPTY;
  private Name fieldName = Name.EMPTY;

  public CrcFlatFileSignatureFactoryAssembler() {
  }

  public void setRecordType(Name recordTypeName) {

    this.recordTypeName = recordTypeName;
  }

  public void setField(Name fieldName) {

    this.fieldName = fieldName;
  }

  public FlatFileSignatureFactory assemble(ConfigurationContext context) {

    if (recordTypeName.isEmpty()) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),
                                                                 "recordType");
      throw new ServingXmlException(message);
    }

    if (fieldName.isEmpty()) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),
                                                                 "field");
      throw new ServingXmlException(message);
    }

    FlatFileSignatureFactory checkerFactory = new FlatFileSignatureFactory() {
        public FlatFileSignature createFlatFileSignature() {
          SignatureMethod method = new CrcSignatureMethod();
          return new FlatFileSignatureImpl(recordTypeName, fieldName, method, true);
        }
    };

    return checkerFactory;
  }
}

