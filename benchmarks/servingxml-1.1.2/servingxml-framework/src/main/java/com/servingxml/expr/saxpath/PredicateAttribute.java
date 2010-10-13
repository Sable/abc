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

package com.servingxml.expr.saxpath;

import org.xml.sax.Attributes;

import com.servingxml.util.record.Record;
import com.servingxml.util.Name;

public class PredicateAttribute extends PredicateTerm {
  private Name name;

  public PredicateAttribute(Name name) {
    this.name = name;
  }

  public Result evaluate(SaxPath path, Record parameters) {
    String s = stringValue(path,parameters);
    return new StringResult(s);
  }

  public String stringValue(SaxPath path, Record parameters) {
    String s = "";
    Attributes attributes = path.getAttributes();
    boolean result = false;
    int index = attributes.getIndex(name.getNamespaceUri(),name.getLocalName());
    if (index != -1) {
      s = attributes.getValue(index);
    }
    return s;
  }
}

