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

package com.servingxml.components.parameter;

import org.xml.sax.XMLFilter;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.components.content.Content;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.Name;
import com.servingxml.util.NameTest;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;

public class WithParametersImpl implements WithParameters {
  private final NameTest parameters;
  private final NameTest exceptParameters;

  public WithParametersImpl(NameTest parameters, NameTest exceptParameters) {
    this.parameters = parameters;
    this.exceptParameters = exceptParameters;
  }

  public boolean accept(Name parameterName) {
    boolean result = parameters.matches(parameterName) && !exceptParameters.matches(parameterName);
    //System.out.println(getClass().getName()+".accept " + parameterName + ", result=" + result);
    return result;
  }
}

