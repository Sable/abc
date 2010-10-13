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

import com.servingxml.components.recordio.RecordAccepter;
import com.servingxml.components.recordio.RecordAccepterFactory;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.Name;
import com.servingxml.util.NamePath;
import com.servingxml.util.ServingXmlException;

/**
 * The <code>OnRecordFactoryAssembler</code> implements an assembler for
 * assembling <code>OnRecordFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class OnRecordFactoryAssembler {

  private MapXmlFactory[] fieldMappingFactories = new MapXmlFactory[0];
  private Name recordTypeName = Name.EMPTY;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;
  private String testExpr = "";

  public void setRecordType(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void setTest(String testExpr) {
    this.testExpr = testExpr;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }
  
  public void injectComponent(MapXmlFactory[] fieldMappingFactories) {
    this.fieldMappingFactories = fieldMappingFactories;
    //System.out.print(getClass().getName()+".injectComponent ");
    //for (int i = 0; i < fieldMappingFactories.length; ++i) {
    //System.out.print(fieldMappingFactories[i].getClass().getName() + " ");
    //}
    //System.out.println();
  }
  
  public MapXmlFactory assemble(ConfigurationContext context) {

    try {
      if (xsltConfiguration == null) {
        xsltConfiguration = XsltConfiguration.getDefault();
      }

      RecordAccepterFactory accepterFactory;
      if (testExpr.length() > 0) {
        accepterFactory = RecordAccepterFactory.newInstance(context.getQnameContext(), xsltConfiguration, testExpr);
      } else {
        accepterFactory = RecordAccepterFactory.newInstance(recordTypeName);
      }
      RecordAccepter accepter = accepterFactory.createRecordAccepter();

      MapXmlFactory rmf = new MultipleMapXmlFactory(context.getQnameContext(), xsltConfiguration, fieldMappingFactories);
      MapXmlFactory recordMapFactory = new OnRecordFactory(accepter, rmf);
      if (parameterDescriptors.length > 0) {
        recordMapFactory = new MapXmlFactoryPrefilter(recordMapFactory,parameterDescriptors);
      }
      return recordMapFactory;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}
