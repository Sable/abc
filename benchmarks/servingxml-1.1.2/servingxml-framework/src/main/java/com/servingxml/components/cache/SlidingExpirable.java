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

import com.servingxml.io.cache.Expirable;

/**
 * Defines an interface for a sliding expiration. 
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */


public class SlidingExpirable implements Expirable {
  private final long timestamp;
  private final long absoluteExpiration;
  
  public SlidingExpirable(long elapsed) {
    
    this.timestamp = System.currentTimeMillis();
    this.absoluteExpiration = timestamp + elapsed;
  }
  
  public boolean hasExpired(long ts) {
    
    return System.currentTimeMillis() >= absoluteExpiration;
  }
  
  public final long getLastModified(long elapsed) {
    long ts = timestamp+elapsed;
    
    return hasExpired(elapsed) ? elapsed : timestamp;
  }

  public boolean immediateExpiry() {
    return false;
  }
}

