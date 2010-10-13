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

import java.util.Iterator;     

public class CommandLineIterator implements Iterator {
  private final String[] args;
  private int index = 0;

  public CommandLineIterator(String[] args) {
    this.args = args;
  }

  public boolean hasNext() {
    return index < args.length ? true : false;
  }

  public Object next() {
    return nextArg();
  }

  public CommandLine.Arg nextArg() {
    CommandLine.Arg arg;

    String s = args[index];
    int nameEnd = s.indexOf('=');
    if (nameEnd != -1) {
      String name = s.substring(0,nameEnd);
      String value = "";
      if (name.length() < s.length()) {
        value = s.substring(nameEnd+1);
      }
      arg = new CommandLine.Arg(name,value);
      ++index;
    } else {
      arg = new CommandLine.Arg(s);
      ++index;
    }
    return arg;
  }

  public void remove() {
  }
}
