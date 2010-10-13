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

import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.util.Name;
import com.servingxml.components.string.Stringable;
import com.servingxml.util.xml.Selectable;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.util.QnameContext;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Environment;

/**
 * Implements a factory class for creating new <tt>MapXml</tt> instances.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                        
class GenerateElementFactory implements MapXmlFactory {
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final Environment env;
  private final NameSubstitutionExpr nameResolver;
  private final Stringable stringFactory;
  private final MapXmlFactory childFactories;

  public GenerateElementFactory(Environment env, NameSubstitutionExpr nameResolver, 
    Stringable stringFactory, MapXmlFactory childFactories) {
    this.env = env;
    this.nameResolver = nameResolver;
    this.stringFactory = stringFactory;
    this.childFactories = childFactories;
  }

  public MapXml createMapXml(ServiceContext context) {

    MapXml rm = childFactories.createMapXml(context);
    MapXml recordMap = new GenerateElement(env, nameResolver, EMPTY_ATTRIBUTES,stringFactory,rm);

    return recordMap;
  }

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory recordTemplatesFactory) {
    childFactories.addToXsltEvaluator(mode, recordTemplatesFactory);
  }

  public boolean isGroup() {
    return childFactories.isGroup() || childFactories.isRecord();
  }

  public boolean isRecord() {
    return false;
  }
}
