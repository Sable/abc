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
 * Implements a default <code>Key</code>
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public final class DefaultKey implements Key {
  private static final Object lock = new Object();
  private static int counter = 0;

  private final String value;
  private final int hashCode;

  private DefaultKey(String value) {
    this.value = value;
    this.hashCode = value.hashCode();
  }

  public boolean equals(Object anObject) {
    boolean areEqual = false;
    if (this == anObject) {
      areEqual = true;
    } else if (anObject instanceof DefaultKey) {
      DefaultKey rhs = (DefaultKey)anObject;
      if (rhs.value.equals(value)) {
        areEqual = true;
      }
    }
    return areEqual;
  }

  public int hashCode() {
    return hashCode;
  }

  public static Key newInstance() {

    int value = 0;
    synchronized (lock) {
      value = ++counter;
    }
    String s = DefaultKey.class.getName() + value;

    Key key = new DefaultKey(s);
    return key;
  }

  public String toString() {
    return value;
  }
}
