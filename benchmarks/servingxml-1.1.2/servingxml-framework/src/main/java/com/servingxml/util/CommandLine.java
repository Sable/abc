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
import java.util.Set;     
import java.util.HashSet;     
import java.util.List;     
import java.util.ArrayList;     

public class CommandLine {

  public static abstract class ParameterCommand {
    public void doParameter(String name, String[] values) {
    }
  }

  public void toEveryArgument(ParameterCommand command) {
    Set<String> parameterNameSet = parameterNameSet();
    Iterator<String> iter = parameterNameSet.iterator();
    while (iter.hasNext()) {
      String name = iter.next();
      String[] values = getParameterValues(name);
      command.doParameter(name,values);
    }
  }
  
  
  private final String[] args;

  public CommandLine(String[] args) {
    this.args = args;
  }

  public CommandLine() {
    this.args = SystemConstants.EMPTY_STRING_ARRAY;
  }

  public Set<String> parameterNameSet() {
    Set<String> set = new HashSet<String>();
    CommandLineIterator iter = new CommandLineIterator(args);
    while (iter.hasNext()) {
      CommandLine.Arg arg = iter.nextArg();
      if (arg.hasName() && !set.contains(arg.getName())) {
        set.add(arg.getName());
      }
    }
    return set;
  }

  public Iterator iterator() {
    return new CommandLineIterator(args);
  }
  
  public String getOption(String option) {
    boolean flag = false;
    String value = null;

    CommandLineIterator iter = new CommandLineIterator(args);
    while (iter.hasNext()) {
      Arg arg = iter.nextArg();
      if (arg.isOption() && arg.getValue().equals(option)) {
        flag = true;
        break;
      }
    }
    if (flag && iter.hasNext()) {
      Arg arg = iter.nextArg();
      value = arg.getValue();
    }

    return value;
  }
  
  public String[] getParameterValues(String name) {
    List<String> list = getParameterValueList(name);
    String[] a = new String[list.size()];
    a = (String[])list.toArray(a);
    return a;
  }
  
  public List<String> getParameterValueList(String name) {
    List<String> list = new ArrayList<String>();

    CommandLineIterator iter = new CommandLineIterator(args);
    while (iter.hasNext()) {
      Arg arg = iter.nextArg();
      if (arg.hasName() && arg.getName().equals(name)) {
        list.add(arg.getValue());
      }
    }

    return list;
  }
  
  public String getParameterValue(String name) {
    String s = null;      

    CommandLineIterator iter = new CommandLineIterator(args);
    while (iter.hasNext()) {
      Arg arg = iter.nextArg();
      if (arg.hasName() && arg.getName().equals(name)) {
        s = arg.getValue();
        break;
      }
    }

    return s;
  }
  
  public boolean isOption(String option) {
    boolean flag = false;

    CommandLineIterator iter = new CommandLineIterator(args);
    while (iter.hasNext()) {
      Arg arg = iter.nextArg();
      if (arg.isOption() && arg.getValue().equals(option)) {
        flag = true;
        break;
      }
    }

    return flag;
  }
  
  public boolean isOption(char option) {
    boolean flag = false;

    CommandLineIterator iter = new CommandLineIterator(args);
    while (iter.hasNext()) {
      Arg arg = iter.nextArg();
      String s = arg.getValue();
      int index = s.indexOf(option);
      if (index >= 0) {
        flag = true;
        break;
      }
    }

    return flag;
  }

  public static class Arg {
    private final String name;
    private final String value;
    private final boolean option;

    Arg(String name, String value) {
      this.name = name;
      this.value = value;
      this.option = false;
    }

    Arg(String value) {
      this.name = "";

      if (value.charAt(0) == '-') {
        this.value = value.substring(1);
        this.option = true;
      } else {
        this.value = value;
        this.option = false;
      }
    }

    public boolean isOption() {
      return option;
    }

    public boolean hasName() {
      return name.length() != 0;
    }

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }

    public String toString() {
      StringBuilder buf = new StringBuilder();
      if (name.length() > 0) {
        buf.append(name);
        buf.append(" eq ");
      }
      buf.append(value);
      return buf.toString();
    }
  }
}
