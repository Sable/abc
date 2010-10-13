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

import javax.xml.transform.TransformerFactory;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.DefaultTransformerErrorListener;
import com.servingxml.util.PrefixMap;                 
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;
import com.servingxml.app.Environment;

/**
 * Assembler for assembling a <code>FlatRecordTypeChoiceFactory</code>.
 *
 *                      
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * @see FlatRecordTypeChoiceFactory
 */

public class FlatRecordTypeChoiceFactoryAssembler extends FlatFileOptionsFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private FlatRecordFieldFactory[] fieldTypeFactories = new FlatRecordFieldFactory[0];
  private FlatRecordTypeSelectionFactory[] flatRecordTypeSelectionFactories = new FlatRecordTypeSelectionFactory[0];

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(FlatRecordFieldFactory[] fieldTypeFactories) {

    this.fieldTypeFactories = fieldTypeFactories;
  }

  public void injectComponent(FlatRecordTypeSelectionFactory[] flatRecordTypeSelectionFactories) {
    this.flatRecordTypeSelectionFactories = flatRecordTypeSelectionFactories;
  }

  public FlatRecordTypeFactory assemble(final ConfigurationContext context) {
    Environment env = new Environment(parameterDescriptors,context.getQnameContext());

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (fieldTypeFactories.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"sx:delimitedField or sx:positionalField");
    }

    if (flatRecordTypeSelectionFactories.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_CHOICE_REQUIRED,context.getElement().getTagName(),
                                                                 "sx:when, sx:otherwise");
      throw new ServingXmlException(message);
    }

    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    TransformerFactory transformerFactory = xsltConfiguration.getTransformerFactory();
    transformerFactory.setErrorListener(new DefaultTransformerErrorListener(context));

    PrefixMap prefixMap = context.getQnameContext().getPrefixMap();

    FlatRecordTypeFactory flatRecordTypeFactory = new FlatRecordTypeChoiceFactory(env,
                                                                                  fieldTypeFactories, 
                                                                                  flatRecordTypeSelectionFactories,
                                                                                  xsltConfiguration, 
                                                                                  transformerFactory, 
                                                                                  prefixMap, 
                                                                                  flatFileOptionsFactory);
    return flatRecordTypeFactory;
  }
}


