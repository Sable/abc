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

import com.servingxml.app.ServiceContext;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.app.ParameterDescriptor;

/**
 * A command for mapping a field in a flat file to an element or attribute
 * in an XML stream.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SubtreeXmlFlattenerFactory implements ShredXmlFactory {
  private final ParameterDescriptor[] parameterDescriptors;
  private final RestrictedMatchPattern expr;
  private final ShredXmlFactory childFlattenerFactory;

  public SubtreeXmlFlattenerFactory(ParameterDescriptor[] parameterDescriptors,
    RestrictedMatchPattern expr, ShredXmlFactory childFlattenerFactory) {
    this.parameterDescriptors = parameterDescriptors;
    this.expr = expr;
    this.childFlattenerFactory = childFlattenerFactory;
  }

  public ShredXml createShredXml(ServiceContext context, Flow flow) {
    ShredXml childFlattener = childFlattenerFactory.createShredXml(context, flow);

    ShredXml flattener = new SubtreeXmlFlattener(parameterDescriptors, expr, childFlattener);
    return flattener;
  }

  public SubtreeFieldMap createSubtreeFieldMap(Name fieldName, String matchExpr) {
    return null;
  }
}
