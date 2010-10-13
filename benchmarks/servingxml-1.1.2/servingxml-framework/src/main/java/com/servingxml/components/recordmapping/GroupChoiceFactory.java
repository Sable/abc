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

import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.xml.XsltEvaluator;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.util.xml.XsltEvaluatorFactoryImpl;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.util.PrefixMap;                 
import com.servingxml.app.ServiceContext;

/**
 * Implements a factory class for creating new <tt>MapXml</tt> instances.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                        
class GroupChoiceFactory implements MapXmlFactory {
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final XsltConfiguration xsltConfiguration;
  private final MapXmlFactory[] childFactories;                   
  private final XsltEvaluator evaluator;

  public GroupChoiceFactory(ConfigurationContext context, XsltConfiguration xsltConfiguration,
    MapXmlFactory[] childFactories) {
    this.xsltConfiguration = xsltConfiguration;
    this.childFactories = childFactories;

    XsltEvaluatorFactory xsltEvaluatorFactory = new XsltEvaluatorFactoryImpl();
    for (int i = 0; i < childFactories.length; ++i) {
      MapXmlFactory childFactory = childFactories[i];
      String mode = "field."+i;
      childFactory.addToXsltEvaluator(mode, xsltEvaluatorFactory);
    }

    this.evaluator = xsltEvaluatorFactory.createXsltEvaluator(context.getQnameContext(), 
      xsltConfiguration);
  }

  public MapXml createMapXml(ServiceContext context) {

    MapXml[] children = new MapXml[childFactories.length];
    for (int i = 0; i < childFactories.length; ++i) {
      MapXmlFactory childFactory = childFactories[i];
      children[i] = childFactory.createMapXml(context);
    }
    evaluator.setUriResolverFactory(context.getUriResolverFactory());
    MapXml recordMap = new GroupChoice(children, evaluator);

    return recordMap;
  }

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory xsltEvaluatorFactory) {
  }

  public boolean isGroup() {
    return true;
  }

  public boolean isRecord() {
    return false;
  }
}
