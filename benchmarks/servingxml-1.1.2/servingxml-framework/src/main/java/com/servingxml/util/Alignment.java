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

/**
 * The <code>Alignment</code> formats a line of text
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class Alignment {
  
  public static final Alignment LEFT = new Alignment("left",3);
  public static final Alignment RIGHT = new Alignment("right",1);         
  public static final Alignment CENTER = new Alignment("center",2);
  
  private final String name;
  private final int value;
  
  public Alignment(String name, int value) {
    this.name = name;
    this.value = value;
  }
  
  public String toString() {
    return name;
  }
  
  public int intValue() {
    return value;
  }
  
  public static Alignment parse(String value) {
    Alignment alignment = null;
    
    if (value.length() > 0) {
      if (value.equals(RIGHT.toString())) {
        alignment = RIGHT;
      } else if (value.equals(CENTER.toString())) {
        alignment = CENTER;
      } else if (value.equals(LEFT.toString())) {
        alignment = LEFT;
      }
    }
    
    return alignment;
  }
}
