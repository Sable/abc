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

package com.servingxml.components.string;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import javax.xml.datatype.DatatypeFactory;

/**
 * Implements a <tt>CurrentTimeAssembler</tt> for assembling <tt>CurrentTime</tt> objects. 
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CurrentTimeAssembler {
  
  public StringFactory assemble(final ConfigurationContext context) {

    DatatypeFactory datatypeFactory;
    try {
      datatypeFactory = DatatypeFactory.newInstance();
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
    StringFactory converter = new CurrentTime(datatypeFactory);
    return converter;
  }
}

