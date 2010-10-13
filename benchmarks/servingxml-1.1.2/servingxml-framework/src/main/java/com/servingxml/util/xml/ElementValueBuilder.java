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

package com.servingxml.util.xml;

import com.servingxml.util.Name;

public class ElementValueBuilder implements Receiver {
  private final Name name;
  private int selfSym;
  private String value;
  private final Name[] attributeNames;
  private String[] attributeValues;

  public ElementValueBuilder(Name name) {
    this.name = name;
    this.selfSym = Receiver.UNDEFINED_SYMBOL;
    this.attributeNames = new Name[0];
    this.attributeValues = new String[0];
  }

  public int getSymbol() {
    return selfSym;
  }

  public void bind(ReceiverContext context) {
    selfSym = context.getSymbol(name);
  }

  public void startElement(ReceiverContext context) {
  }

  public void characters(ReceiverContext context, char[] ch, int start, int length) {
    if (selfSym == context.getCurrentElementSymbol()) {
      value = new String(ch,start,length).trim();
    }
  }

  public void childReceived(int symbol) {
  }

  public void endElement(ReceiverContext context) {
    //System.out.println(getClass().getName()+".endElement value = " + value);
  }

  public String getString() {
    return value;
  }

  public Name getName() {
    return name;
  }

  public String getAttributeValue(int i) {
    return attributeValues[i];
  }
}
