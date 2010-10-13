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

package com.servingxml.components.string;

import org.w3c.dom.Element;

import com.servingxml.util.ServingXmlException;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;

/**
 * The <code>StringEvaluator</code> implements a factory for
 * creating system <code>SqlStatement</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class StringEvaluator implements StringFactory {

  private final Stringable stringFactory;
  private final StringFactory tail;

  public StringEvaluator() {
    this.stringFactory = StringFactory.UNINITIALIZED;
    this.tail = null;
  }

  public StringEvaluator(Stringable stringFactory) {
    this.stringFactory = stringFactory;
    this.tail = null;
  }

  public StringEvaluator(Stringable stringFactory, StringFactory tail) {
    this.stringFactory = stringFactory;
    this.tail = tail;
  }


  public String createString(ServiceContext context, Flow flow) {
    StringBuilder stringBuilder = new StringBuilder();
    createString(context,flow,stringBuilder);
    return stringBuilder.toString();
  }

  public void createString(ServiceContext context, Flow flow, StringBuilder stringBuilder) {
    if (tail != null) {
      String s = tail.createString(context,flow);
      stringBuilder.append(s);
    }
    String s = stringFactory.createString(context,flow);
    stringBuilder.append(s);
  }
}

