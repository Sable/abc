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

package com.servingxml.components.sql;

import com.servingxml.util.xml.DomHelper;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.ioc.components.ConfigurationContext;

/**
 * The <code>SqlCommandAssembler</code> implements an assembler for
 * assembling system <code>SqlCommand</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SqlCommandAssembler {
  
  public SqlCommand assemble(final ConfigurationContext context) {

    String s = DomHelper.getInnerText(context.getElement());
    s = s.trim();
    if (s.length() == 0) {
      throw new ServingXmlException("Expected command");
    }
    SqlCommand query = new SqlCommand(s);

    return query;
  }
}

