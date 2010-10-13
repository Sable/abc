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

package com.servingxml.components.content;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.common.NameSubstitutionExpr;

/**
 * The <code>DocumentSequenceAssembler</code> implements an assembler for
 * assembling system <code>Content</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DocumentSequenceAssembler {
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private String documentElementQname = "";
  private RecordReaderFactory recordReaderFactory;
  private Content[] contentFactories = Content.EMPTY_ARRAY;

  public void setWrapWith(String qname) {
    this.documentElementQname = qname;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(RecordReaderFactory recordReaderFactory) {
    this.recordReaderFactory = recordReaderFactory;
  }

  public void injectComponent(Content[] contentFactories) {
    this.contentFactories = contentFactories;
  }

  public Content assemble(ConfigurationContext context) {
    if (contentFactories.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                                                                 context.getElement().getTagName(),"sx:content");
      throw new ServingXmlException(message);
    }

    NameSubstitutionExpr documentElementNameEvaluator = NameSubstitutionExpr.parse(context.getQnameContext(),documentElementQname);

    Content docSeqFactory;
    if (recordReaderFactory == null) {
      docSeqFactory = new DocumentCollection(context.getQnameContext(), 
                                                    documentElementNameEvaluator, 
                                                    contentFactories);
    } else {
      docSeqFactory = new DocumentSequence(context.getQnameContext(), 
                                                  documentElementNameEvaluator, 
                                                  recordReaderFactory, 
                                                  contentFactories[0]);
    }


    if (parameterDescriptors.length > 0) {
      docSeqFactory = new ContentPrefilter(docSeqFactory,parameterDescriptors);
    }
    return docSeqFactory;
  }
}
