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

package com.servingxml.io.streamsource;

import com.servingxml.io.cache.Expirable;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public abstract class StreamExpirable implements Expirable {

  /** 
   * Called by the servingxml framework to determine whether the XML content has changed.
   */
  public abstract long getLastModified();

  public long getLastModified(long timestamp) {
    return getLastModified();
  }

  public boolean hasExpired(long timestamp) {
    long lastModified = getLastModified();
    boolean changed = lastModified > timestamp || lastModified < 0;
    return changed;
  }

  public boolean immediateExpiry() {
    return false;
  }
}
