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

import com.servingxml.ioc.components.ConfigurationContext;

/**
 * Implements a <tt>CreateDateAssembler</tt> for assembling 
 * <tt>CreateDate</tt> objects. 
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CreateDateAssembler {

  private Stringable inputFactory = null;
  private String format = "";

  public void setFormat(String format) {
    this.format = format;
  }
  
  public StringFactory assemble(final ConfigurationContext context) {
    
    StringFactory converter = new CreateDate(format);

    return converter;
  }
}

