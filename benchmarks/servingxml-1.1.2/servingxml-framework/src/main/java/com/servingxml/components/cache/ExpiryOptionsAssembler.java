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

package com.servingxml.components.cache;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.io.cache.RevalidationType;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ExpiryOptionsAssembler {
  
  private RevalidationType revalidationType = RevalidationType.FULL_REVALIDATION;
  
  public void setRevalidate(String value) {
    if (value.length() != 0) {
      revalidationType = new RevalidationType(value);
    }
  }

  public ExpiryOptions assemble(ConfigurationContext context) {

    return new ExpiryOptions(revalidationType);
  }
}

