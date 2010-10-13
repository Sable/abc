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

import java.lang.reflect.InvocationTargetException;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SupplementaryServingXmlException extends ServingXmlException {

  private static final long serialVersionUID = 1L;

  private final ServingXmlException tail;

  public SupplementaryServingXmlException(String s, ServingXmlException tail) {
    super(s);
    this.tail = tail;
  }

  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    getMessage(sb);
    getSupplementaryMessage(sb);
    return sb.toString();
  }

  public void getMessage(StringBuilder sb) {
    if (tail != null) {
      tail.getMessage(sb);
      sb.append(". ");
    }
    sb.append(getMessage());
  }

  public void getSupplementaryMessage(StringBuilder sb) {
    if (tail != null) {
      tail.getSupplementaryMessage(sb);
      sb.append(". ");
    }
    sb.append(getMessage());
  }

  public String toString() {
    return getMessage();
  }
}

