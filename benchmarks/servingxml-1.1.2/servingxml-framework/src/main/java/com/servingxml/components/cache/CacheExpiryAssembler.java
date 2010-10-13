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

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CacheExpiryAssembler implements CacheConstants {
  
  private boolean changed = false;
  private long delay = 0L;

  public void setChanged(String value) {
    if (value.length() != 0) {
      if (value.equals("yes")) {
        changed = true;
      } else {
        changed = false;
      }
    } else {
      changed = false;
    }
  }
  
  public void setDelay(long delay) {
    this.delay = delay;
  }
  
  public CacheExpiry assemble(ConfigurationContext context) {

    return new CacheExpiry(delay, changed);
  }
}

