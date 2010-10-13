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
 * Implements a family of expirables.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */ 

public class ExpirableFamily implements Expirable {
  private static int INITIAL_SIZE = 20;

  private Expirable[] expirables;
  private int count;

  public ExpirableFamily() {
    expirables = new Expirable[INITIAL_SIZE];
    count = 0;
  }

  public void addExpirable(Expirable expirable) {
    if (count == expirables.length) {
      Expirable[] oldExpirables = expirables;
      int length = oldExpirables.length*2;
      if (length < INITIAL_SIZE) {
        length = INITIAL_SIZE;
      }
      expirables = new Expirable[length];
      if (count > 0) {
        System.arraycopy(oldExpirables,0,expirables,0,count);
      }
    }
  }

  public long getLastModified(long timestamp) {
    long lastModified = 0;
    for (int i = 0; i < count; ++i) {
      Expirable expirable = expirables[i];
      long componentLastModified = expirable.getLastModified(timestamp);
      if (componentLastModified == -1) {
        lastModified = -1;
        break;
      }
      if (componentLastModified > lastModified) {
        lastModified = componentLastModified;
      }
    }
    return lastModified;
  }

  public boolean hasExpired(long timestamp) {
    boolean hasExpired = false;
    for (int i = 0; i < count; ++i) {
      Expirable expirable = expirables[i];
      if (expirable.hasExpired(timestamp)) {
        hasExpired = true;
        break;
      }
    }
    return hasExpired;
  }

  public boolean immediateExpiry() {
    boolean immediate = false;
    for (int i = 0; i < count; ++i) {
      Expirable expirable = expirables[i];
      if (expirable.immediateExpiry()) {
        immediate = true;
        break;
      }
    }
    return immediate;
  }
}
