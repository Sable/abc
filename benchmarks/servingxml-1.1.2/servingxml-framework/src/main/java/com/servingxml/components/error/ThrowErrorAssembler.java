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

package com.servingxml.components.error;

import org.w3c.dom.Element;

import com.servingxml.components.task.Task;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.string.Stringable;
import com.servingxml.components.string.StringLiteralFactory;
import com.servingxml.util.xml.DomHelper;

/**
 * Factory for creating a <tt>CatchError</tt> instance.
 * 
 * @see com.servingxml.components.error.CatchError
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ThrowErrorAssembler {
  private Stringable stringable = null;

  public void injectComponent(Stringable stringable) {
    this.stringable = stringable;
  }

  public Task assemble(ConfigurationContext context) {

    if (stringable == null) {
      Element inputElement = context.getElement();
      String innerText = DomHelper.getInnerText(inputElement);
      if (innerText != null) {
        innerText = innerText.trim();
        if (innerText.length() > 0) {
          stringable = new StringLiteralFactory(innerText);
        }
      }
    }

    return new ThrowError(stringable);
  }
}

