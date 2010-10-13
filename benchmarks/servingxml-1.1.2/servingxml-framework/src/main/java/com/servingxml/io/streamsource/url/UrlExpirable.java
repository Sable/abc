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

package com.servingxml.io.streamsource.url;

import java.net.URL;

import com.servingxml.io.streamsource.StreamExpirable;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class UrlExpirable extends StreamExpirable {

  private final URL url;

  public UrlExpirable(URL url) {
    this.url = url;
  }

  public final long getLastModified() {


    long lastModified = -1;
    try {
      lastModified = url.openConnection().getLastModified();
    } catch (java.io.IOException e) {
      lastModified = -1;
    }
    return lastModified;
  }
}

