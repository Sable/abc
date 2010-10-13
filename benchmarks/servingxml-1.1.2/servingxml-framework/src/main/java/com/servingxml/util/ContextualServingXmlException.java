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

public class ContextualServingXmlException extends ServingXmlException {

  private static final long serialVersionUID = 1L;

  private final ServingXmlException tail;
  private final String context;

  public ContextualServingXmlException(String context, ServingXmlException tail) {
    super("");
    this.context = context;
    this.tail = tail;
   //System.out.println(getClass().getName()+".cons");
  }

  public String getMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    getContext(sb);
    sb.append("]");
    getMessage(sb);
    return sb.toString();
  }

  public void getContext(StringBuilder sb) {
    sb.append(context);
    if (tail != null) {
      if (sb.length() > 0) {
        sb.append(",");
      }
      tail.getContext(sb);
    }
  }

  public void getMessage(StringBuilder sb) {
    tail.getMessage(sb);
  }

  public String toString() {
    return getMessage();
  }
}

