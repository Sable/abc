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


import javax.xml.transform.Templates;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.util.xml.XmlRecordTransformReader;

import javax.xml.transform.URIResolver; 

/**                                 
 * This class supports mapping a stream of SAX events to a record stream.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SubtreeFlattenerFactory implements ShredXmlFactory {
  private final Templates templates;
  private final String matchExpr2;
  private final Name recordTypeName;
  private final SubtreeFieldMap[] subtreeFieldMaps;
  private final Name[] parameterNames;

  public SubtreeFlattenerFactory(Templates templates, String matchExpr2, Name recordTypeName, 
                              SubtreeFieldMap[] subtreeFieldMaps, 
    Name[] parameterNames) {
    this.templates = templates;
    this.matchExpr2 = matchExpr2;
    this.recordTypeName = recordTypeName;
    this.subtreeFieldMaps = subtreeFieldMaps;
    this.parameterNames = parameterNames;
  }

  public ShredXml createShredXml(ServiceContext context, Flow flow) {

    return new SubtreeFlattener(matchExpr2,recordTypeName,templates,parameterNames);
  }

  public SubtreeFieldMap createSubtreeFieldMap(Name fieldName, String matchExpr) {

    return new SubtreeRepeatingGroupMap(matchExpr,fieldName,matchExpr2,recordTypeName,subtreeFieldMaps);
  }
}

