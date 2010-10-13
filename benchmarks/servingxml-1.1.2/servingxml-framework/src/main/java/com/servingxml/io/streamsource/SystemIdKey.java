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

import com.servingxml.io.cache.Key;

/**
 * This class provides an implementation of a key used to uniquely identify XML documents.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public final class SystemIdKey implements Key {

  private final String systemId;
  private final int hashValue;

  public SystemIdKey(String systemId) {
    this.systemId = systemId;
    this.hashValue = systemId.hashCode();
  }
  
  /**
   * Compares this key to the specified object.
   *
   * @param anObject the object being tested for equality.
   *
   * @return <code>true</code> if the two keys are equal, <code>false</code> otherwise.
   */

  public boolean equals(Object anObject) {
    boolean isEqual = true;
    if (anObject != this) {
      if (!(anObject instanceof SystemIdKey)) {
        isEqual = false;
      } else {
        SystemIdKey rhs = (SystemIdKey)anObject;
        if (!systemId.equals(rhs.systemId)) {
          isEqual = false;
        }
      }
    }

    return isEqual;
  }
  /**
   * Returns a hash code value for this key.
   *
   * @return a hash code value for this key.
   */

  public int hashCode() {
    return hashValue;
  }

  public String toString() {
    return systemId;
  }
}
