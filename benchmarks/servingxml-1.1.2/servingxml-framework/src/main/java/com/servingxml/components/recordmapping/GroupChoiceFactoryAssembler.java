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

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.xsltconfig.XsltConfiguration;

/**
 * The <code>GroupChoiceFactoryAssembler</code> implements an assembler for
 * assembling <code>GenerateElementFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class GroupChoiceFactoryAssembler {

  private MapXmlFactory[] childFactories = new MapXmlFactory[0];
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private XsltConfiguration xsltConfiguration;

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(MapXmlFactory[] childFactories) {
    this.childFactories = childFactories;
    //for (int i = 0; i < childFactories.length; ++i) {
      //System.out.println(getClass().getName()+".injectComponent MapXmlFactory[] " + childFactories[i].getClass().getName());
    //}
  }

  public MapXmlFactory assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    MapXmlFactory recordMapFactory = new GroupChoiceFactory(context, xsltConfiguration, childFactories);
    if (parameterDescriptors.length > 0) {
      recordMapFactory = new MapXmlFactoryPrefilter(recordMapFactory,parameterDescriptors);
    }
    return recordMapFactory;
  }
}
