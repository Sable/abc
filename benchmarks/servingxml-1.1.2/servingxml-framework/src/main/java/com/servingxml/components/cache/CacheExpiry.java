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

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CacheExpiry {

  public static final CacheExpiry IMMEDIATE_EXPIRY = new CacheExpiry(0L,true);
  public static final CacheExpiry NEVER_EXPIRES = new CacheExpiry(Long.MAX_VALUE,false);

  private final long delay;
  private final boolean changed;

  public CacheExpiry(long delay, boolean changed) {
    this.changed = changed;
    this.delay = delay;
  }

  public CacheExpiry() {
    this.changed = true;                                   
    this.delay = 0L;
  }
  
  public final long getInterval() {
    return delay;
  }

  public final boolean whenChanged() {
    return changed;
  }

  public String toString() {
    return "delay = " + delay + ", whenChanged = " + changed;
  }
}


