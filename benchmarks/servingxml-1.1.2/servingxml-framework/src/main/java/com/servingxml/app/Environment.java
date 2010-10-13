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

package com.servingxml.app;

import java.util.List;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.servingxml.app.Flow;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.QnameContext;
import com.servingxml.util.record.Record;

/**
 *
 * 
 * @author  Daniel A. Parker
 */


public class Environment {
  private final ParameterDescriptor[] parameterDescriptors;
  private final QnameContext nameContext;

  public Environment(ParameterDescriptor[] parameterDescriptors, QnameContext nameContext) {
    this.parameterDescriptors = parameterDescriptors;
    this.nameContext = nameContext;
  }

  public String qnameFor(Name name) {
    String qname = name.toQname(nameContext);
    return qname;
  }

  public void addPrefixMappingsTo(ContentHandler handler) throws SAXException {
    PrefixMap.PrefixMapping[] prefixDeclarations = nameContext.getPrefixMap().getLocalPrefixDeclarations();
    for (int i = 0; i < prefixDeclarations.length; ++i) {
      PrefixMap.PrefixMapping prefixMapping = prefixDeclarations[i];
      //System.out.println(getClass().getName()+".generateElement prefix="+prefixMapping.getPrefix() + ", ns = " + prefixMapping.getNamespaceUri());
      handler.startPrefixMapping(prefixMapping.getPrefix(), prefixMapping.getNamespaceUri());
    }
  }

  public Flow augmentParametersOf(ServiceContext context, Flow flow) {
    Flow newFlow = flow;
    if (parameterDescriptors.length > 0) {
      newFlow = flow.augmentParameters(context,parameterDescriptors);
    }
    return newFlow;
  }

  public ParameterDescriptor[] getParameterDescriptors() {
    return parameterDescriptors;
  }

  public QnameContext getQnameContext() {
    return nameContext;
  }
}
