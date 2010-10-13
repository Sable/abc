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

public interface Result {
  int compareTo(Result rhs);
  int compareTo(boolean b);
  int compareTo(String s);
  int compareTo(double d);
  boolean asBoolean();
}

class BooleanResult implements Result {
  private final boolean b;

  BooleanResult(boolean b) {
    this.b = b;
  }

  public boolean asBoolean() {
    return b;
  }

  public int compareTo(Result rhs) {
    return -rhs.compareTo(b);
  }

  public int compareTo(boolean b2) {
    return b == b2 ? 0 : 1;
  }

  public int compareTo(String s2) {
    boolean test = s2.length() == 0;
    return b == test ? 0 : 1;
  }

  public int compareTo(double d2) {
    return b ? 0 : 1;
  }
}

class StringResult implements Result {
  private final String s;

  StringResult(String s) {
    this.s = s;
  }

  public boolean asBoolean() {
    return s.length() > 0;
  }

  public int compareTo(Result rhs) {
    return -rhs.compareTo(s);
  }

  public int compareTo(boolean b2) {
    return asBoolean() == b2 ? 0 : 1;
  }

  public int compareTo(String s2) {
    return s.compareTo(s2);
  }

  public int compareTo(double d2) {
    double d = Double.parseDouble(s.trim());
    return Double.compare(d,d2);
  }
}

class DoubleResult implements Result {
  private final double d;
  private final int pos;

  DoubleResult(double d, int pos) {
    this.d = d;
    this.pos = pos;
  }

  public boolean asBoolean() {
    return (double)pos == d;
  }

  public int compareTo(Result rhs) {
    return -rhs.compareTo(d);
  }

  public int compareTo(boolean b2) {
    return asBoolean() == b2 ? 0 : 1;
  }

  public int compareTo(String s2) {
    double d2 = Double.parseDouble(s2.trim());
    return Double.compare(d,d2);
  }

  public int compareTo(double d2) {
    return Double.compare(d,d2);
  }
}
