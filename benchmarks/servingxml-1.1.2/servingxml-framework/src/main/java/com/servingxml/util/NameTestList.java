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

package com.servingxml.util;

/**
 *
 *  01/05/15
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * 
 */

public class NameTestList extends NameTest {

  private final NameTest nameToken;
  private final NameTest tail;

  public NameTestList(NameTest nameToken, NameTest tail) {
    this.nameToken = nameToken;
    this.tail = tail;
  }

  public boolean matches(String namespaceUri, String localName) {
    boolean matches = nameToken.matches(namespaceUri,localName);
    if (!matches && tail != null) {
      matches = tail.matches(namespaceUri,localName);
    }
    return matches;
  }
}
