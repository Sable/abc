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

package com.servingxml.components.recordmapping;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.recordio.RecordPipelineAppender;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.ContentPrefilter;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.util.xml.XsltEvaluatorFactoryImpl;

/**
 * The <code>RecordContentAssembler</code> implements an assembler for
 * assembling <code>Content</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RecordContentAssembler {
  
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private RecordMappingFactory recordMapFactory = null;
  private Name documentName = Name.EMPTY;
  private XsltConfiguration xsltConfiguration;
  private RecordPipelineAppender[] recordPipelineAppenders = RecordPipelineAppender.EMPTY_ARRAY;

  public void setName(Name documentName) {
    this.documentName = documentName;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }
  
  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(RecordPipelineAppender[] recordPipelineAppenders) {
    this.recordPipelineAppenders = recordPipelineAppenders;
  }

  public void injectComponent(RecordMappingFactory recordMapFactory) {

    this.recordMapFactory = recordMapFactory;
  }

  public Content assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    if (recordMapFactory == null) {
      if (documentName.isEmpty()) {
        Name name = new QualifiedName("","document");
        recordMapFactory = new DefaultRecordMappingFactory(parameterDescriptors, context.getQnameContext(), xsltConfiguration, name);
      } else {
        recordMapFactory = new DefaultRecordMappingFactory(parameterDescriptors, context.getQnameContext(), xsltConfiguration, documentName);
      }
    }

    XsltEvaluatorFactory xsltEvaluatorFactory = new XsltEvaluatorFactoryImpl();
    recordMapFactory.addToXsltEvaluator("field.0", xsltEvaluatorFactory);

    Content contentFactory = new RecordContent(recordPipelineAppenders, recordMapFactory, 
      xsltConfiguration.getOutputPropertyFactories());
    if (parameterDescriptors.length > 0) {
      contentFactory = new ContentPrefilter(contentFactory,parameterDescriptors);
    }

    return contentFactory;
  }
}

