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

package com.servingxml.components.inverserecordmapping;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class InverseRecordMappingImpl implements InverseRecordMapping {

  private final ShredXmlFactory[] flattenerFactories;

  public InverseRecordMappingImpl(ShredXmlFactory[] flattenerFactories) {
    this.flattenerFactories = flattenerFactories;
  }

  public ShredXml createShredXml(ServiceContext context, Flow flow) {
    ShredXml[] children = new ShredXml[flattenerFactories.length];
    for (int i = 0; i < flattenerFactories.length; ++i) {
      children[i] = flattenerFactories[i].createShredXml(context, flow);
    }
    ShredXml flattener = new MultipleXmlFlattener(children);
    return flattener;
  }
}

