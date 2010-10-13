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

/**
 * A <code>MapXmlFactoryAdaptor</code> adapts a Content to a RecordMappingFactory.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MapXmlFactoryAdaptor implements MapXmlFactory {     

  private final Content[] contentFactories;

  public MapXmlFactoryAdaptor(Content contentFactory) {

    this.contentFactories = new Content[]{contentFactory};
  }

  public MapXmlFactoryAdaptor(Content[] contentFactories) {

    this.contentFactories = contentFactories;
  }

  public MapXml createMapXml(ServiceContext context) {

    MapXml recordMap = new ContentRecordMap(contentFactories);

    return recordMap;
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

