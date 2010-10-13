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

import com.servingxml.components.content.Content;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.PrefixMap;
import com.servingxml.app.Environment;

/**
 * Implements a factory class for creating new <tt>MapXml</tt> instances.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                        
class RecordSubtreeMapFactory implements MapXmlFactory {
  private final Environment env;
  private final Content[] xmlComponents;

  public RecordSubtreeMapFactory(Environment env, Content[] xmlComponents) {
    this.env = env;
    this.xmlComponents = xmlComponents;
  }

  public MapXml createMapXml(ServiceContext context) {

    return new RecordSubtreeMap(env, xmlComponents);
  }

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory recordTemplatesFactory) {
  }

  public boolean isGroup() {
    return false;
  }

  public boolean isRecord() {
    return false;
  }
}
