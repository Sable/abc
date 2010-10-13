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
 * The <code>LineFormatter</code> formats a line of text
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class LineFormatter implements Formatter {
  private final Alignment alignment;        
  private final char padCharacter;
  private final int minLength;
  private final int maxLength;

  public LineFormatter(int length, Alignment alignment, char padCharacter) {
    this.minLength = length;
    this.maxLength = length;
    this.alignment = alignment;
    this.padCharacter = padCharacter;
  }

  public LineFormatter(int minLength, int maxLength, Alignment alignment, char padCharacter) {
    this.minLength = minLength;
    this.maxLength = maxLength;
    this.alignment = alignment;
    this.padCharacter = padCharacter;
  }
  
  public String format(String value) {

    String line = value;
    if (maxLength >= 0 && line.length() > maxLength) {
      line = line.substring(0,maxLength);
    } else if (line.length() < minLength) {
      StringBuilder buf = new StringBuilder(minLength);
      if (value.length() > minLength) {
        value = value.substring(0,minLength);
      }

      int n = minLength - value.length();
      int leftCount = 0;
      int rightCount = 0;

      if (alignment.intValue() == Alignment.RIGHT.intValue()) {
        leftCount = n;
      } else if (alignment.intValue() == Alignment.CENTER.intValue()) {
        leftCount = n/2;
        rightCount = n - leftCount;
      } else {
        rightCount = n;
      }

      if (leftCount > 0) {
        for (int i = 0; i < leftCount; ++i) {
          buf.append(padCharacter);
        }

      }
      buf.append(value);
      if (rightCount > 0) {
        for (int i = 0; i < rightCount; ++i) {
          buf.append(padCharacter);
        }

      }
      line = buf.toString();
    }
    
    return line;
  }
}
