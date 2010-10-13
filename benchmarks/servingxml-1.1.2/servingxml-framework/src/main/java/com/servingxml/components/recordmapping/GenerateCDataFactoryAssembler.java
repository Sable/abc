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

import java.util.Comparator;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.components.string.StringFactoryCompiler;
import com.servingxml.components.string.StringFactory;

/**
 * The <code>GenerateCDataFactoryAssembler</code> implements an assembler for
 * assembling <code>GenerateCDataFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class GenerateCDataFactoryAssembler {

  private MapXmlFactory[] childFactories = new MapXmlFactory[0];
  private XsltConfiguration xsltConfiguration;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private Sort[] sorts = new Sort[0];

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(Sort[] sorts) {

    this.sorts = sorts;
  }

  public void injectComponent(MapXmlFactory[] childFactories) {
    this.childFactories = childFactories;
  }

  public MapXmlFactory assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    StringFactory stringFactory = StringFactoryCompiler.fromStringables(context, context.getElement());
    StringFactory[] stringFactories = new StringFactory[]{stringFactory};

    MapXmlFactory rmf = new MultipleMapXmlFactory(context.getQnameContext(), xsltConfiguration, childFactories);

    if (sorts.length > 0) {
      Comparator comparator = new SortComparator(sorts);
      rmf = new SortGroupFactory(rmf,comparator);
    }
    MapXmlFactory recordMapFactory = new GenerateCDataFactory(stringFactories,rmf);
    if (parameterDescriptors.length > 0) {
      recordMapFactory = new MapXmlFactoryPrefilter(recordMapFactory,parameterDescriptors);
    }
    return recordMapFactory;
  }
}
