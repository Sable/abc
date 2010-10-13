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

import com.servingxml.util.record.Record;

class AbsolutePath implements RestrictedMatchPattern {
  private final PathEntry tail;

  public AbsolutePath() {
    this.tail = null;
  }

  public AbsolutePath(RelativePath expr) {
    this.tail = expr.getTail();
  }

  PathEntry getTail() {
    return tail;
  }

  public boolean match(SaxPath path, Record parameters) {
    path.matchAbsolutePath(parameters, tail);
    return path.isMatched();
  }

  public String toString() {
    return "";
  }
}
