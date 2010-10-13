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

package com.servingxml.io.cache;


/**
 * Defines an interface for a expirable. 
 *
 *<p>
 * The <code>XXXExpirable</code> family (<code>DynamicChangeable</code>, 
 * <code>StreamExpirable</code>, etc.) provide an interface for 
 * last modified which is appropriate for a specific kind of resource
 * (dynamic, system, etc.).  Implementations of <code>Expirable</code>
 * will adapt the specialized <code>XXXExpirable</code> last modified interface to the 
 * common <code>Expirable</code> last modified interface.
 *</p>
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface Expirable {     

  /**
   * A <code>Expirable</code> object whose <code>hasExpired</code> method always returns <code>true</code>.
  */
  static final Expirable IMMEDIATE_EXPIRY = new ImmediateExpiry();
  /**
   * A <code>Expirable</code> object whose <code>hasExpired</code> method always returns <code>false</code>.
  */
  static final Expirable NEVER_EXPIRES = new NeverExpires();

  /**
   * Returns the last modified date.
   *
   *<p>
   * A concrete subclass must provide an implementation of this method to indicate whether
   * the cached resource has changed. It may be called often, 
   * so it should execute quickly.
   * </p>
   */
  long getLastModified(long timestamp);

  
  /**
   * Called by the cache manager when it is processing a validation event that 
   * this resource state can receive.
   * <p>
   * A concrete subclass must provide an implementation of this method to indicate whether
   * the cached resource has expired. It may be called often, 
   * so it should execute quickly.
  * </p>
   */
  boolean hasExpired(long timestamp);

  boolean immediateExpiry();
}


class ImmediateExpiry implements Expirable {

  public boolean hasExpired(long timestamp) {
    return true;
  }

  public final long getLastModified(long lastAccessed) {
    return -1;
  }

  public boolean immediateExpiry() {
    return true;
  }
}

class NeverExpires implements Expirable {
  
  public boolean hasExpired(long timestamp) {
    return false;
  }
  
  public final long getLastModified(long lastAccessed) {
    return 0;
  }

  public boolean immediateExpiry() {
    return false;
  }
}

