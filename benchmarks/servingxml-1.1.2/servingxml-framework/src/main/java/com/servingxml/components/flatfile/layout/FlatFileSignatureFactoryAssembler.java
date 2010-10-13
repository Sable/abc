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
import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.InstanceFactory;

/**
 * The <code>FlatFileSignatureAssembler</code> implements an assembler for
 * assembling <code>FlatFile</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileSignatureFactoryAssembler {

  private static final Name CRC_METHOD = new QualifiedName("crc");
  private static final Name SIZE_METHOD = new QualifiedName("size");
  private static final Name CUSTOM_METHOD = new QualifiedName("custom");
                                                       
  private Name recordTypeName = Name.EMPTY;
  private Name fieldName = Name.EMPTY;
  private Name methodName = Name.EMPTY;
  private String validate = TrueFalseEnum.TRUE.toString();
  private Class javaClass = null;

  public FlatFileSignatureFactoryAssembler() {
  }

  public void setValidate(String validate) {
    this.validate = validate;
  }

  public void setRecordType(Name recordTypeName) {

    this.recordTypeName = recordTypeName;
  }

  public void setField(Name fieldName) {

    this.fieldName = fieldName;
  }

  public void setMethod(Name methodName) {

    this.methodName = methodName;
  }

  public void setClass(Class javaClass) {
    this.javaClass = javaClass;
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

    final TrueFalseEnum validateIndicator;
    try {
      validateIndicator = TrueFalseEnum.parse(validate);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
        context.getElement().getTagName(), "validate");
      e = e.supplementMessage(message);
      throw e;
    }

    FlatFileSignatureFactory signatureFactory;

    if (methodName.isEmpty()) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,context.getElement().getTagName(),
                                                                 "method");
      throw new ServingXmlException(message);
    } else if (methodName.equals(CRC_METHOD)) {
      signatureFactory = new FlatFileSignatureFactory() {
          public FlatFileSignature createFlatFileSignature() {
            SignatureMethod method = new CrcSignatureMethod();
            return new FlatFileSignatureImpl(recordTypeName, fieldName, method, validateIndicator.booleanValue());
          }
      };
    } else if (methodName.equals(SIZE_METHOD)) {
      signatureFactory = new FlatFileSignatureFactory() {
          public FlatFileSignature createFlatFileSignature() {
            SignatureMethod method = new SizeSignatureMethod();
            return new FlatFileSignatureImpl(recordTypeName, fieldName, method, validateIndicator.booleanValue());
          }
      };
    } else if (methodName.equals(CUSTOM_METHOD)) {
      if (javaClass == null) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
          context.getElement().getTagName(),"class");
        throw new ServingXmlException(message);
      }

      final InstanceFactory instanceFactory = new InstanceFactory(javaClass, SignatureMethod.class);

      signatureFactory = new FlatFileSignatureFactory() {
          public FlatFileSignature createFlatFileSignature() {
            SignatureMethod method = (SignatureMethod)instanceFactory.createInstance();
            return new FlatFileSignatureImpl(recordTypeName, fieldName, method, validateIndicator.booleanValue());
          }
      };
    } else {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_CHOICE_REQUIRED,context.getElement().getTagName(),
                                                                 "crc, size, custom");
      throw new ServingXmlException(message);
    }

    return signatureFactory;
  }
}

