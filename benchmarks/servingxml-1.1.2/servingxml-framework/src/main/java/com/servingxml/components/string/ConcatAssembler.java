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

import com.servingxml.components.common.ValueEvaluator;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;

/**
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ConcatAssembler {
  
  private String separator = "";
  private String quoteSymbol = "";
  private Stringable[] stringFactories = new Stringable[0];
  
  public void setSeparator(String separator) {
    this.separator = separator;
  }
  
  public void setQuoteSymbol(String quoteSymbol) {
    this.quoteSymbol = quoteSymbol;
  }

  public void injectComponent(Stringable[] stringFactories) {
    this.stringFactories = stringFactories;
  }

  public StringFactory assemble(ConfigurationContext context) {

    if (stringFactories.length == 0) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                                                                 context.getElement().getTagName(),
                                                                 "sx:stringable");
      throw new ServingXmlException(message);
    }

    ValueEvaluator valueEvaluator = new MultipleStringValueEvaluator(stringFactories);
    
    StringFactory stringFactory = new Concat(valueEvaluator,separator,quoteSymbol);

    return stringFactory;
  }
}
