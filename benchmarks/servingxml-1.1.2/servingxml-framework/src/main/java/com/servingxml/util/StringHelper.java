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
 * The <code>StringHelper</code> provides utility methods for strings
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class StringHelper {

  public static String toString(String[] values, String sep, String quoteMark) {
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < values.length; ++i) {
      if (i > 0) {
        buffer.append(sep);
      }
      buffer.append(quoteMark);
      buffer.append(values[i]);
      buffer.append(quoteMark);
    }
    return buffer.toString();
  }

  public static final String trim(String value, boolean trimLeading, boolean trimTrailing) {

    if (trimLeading && trimTrailing) {
      value = value.trim();
    } else if (trimLeading) {
      value = trimLeading(value);
    } else if (trimTrailing) {
      value = trimTrailing(value);
    }
    return value;
  }

  public static final String trimTrailing(String value) {
    boolean done = false;
    int length = value.length();
    while (!done && length > 0) {
      if (!Character.isWhitespace(value.charAt(length-1))) {
        done = true;
      } else {
        --length;
      }
    }
    return length == value.length() ? value : value.substring(0,length);
  }

  public static final String trimLeading(String value) {
    boolean done = false;
    int start = 0;
    int length = value.length();
    while (!done && start < length) {
      if (!Character.isWhitespace(value.charAt(start))) {
        done = true;
      } else {
        ++start;
      }
    }
    return start == 0 ? value : value.substring(start);
  }

  public static final String translateEscapeChars(String s) {
    StringBuilder buf = new StringBuilder();
    char prev = ' ';
    for (int i = 0; i < s.length(); ++i) {
      char c = s.charAt(i);
      if (prev == '\\') {
        if (c == 't') {
          buf.append('\t');
        } else if (c == 'r') {
          buf.append('\r');
        } else if (c == 'n') {
          buf.append('\n');
        } else {
          buf.append(prev);
          buf.append(c);
        }
        prev = c;
      } else {
        if (c == '\\' && i+1 < s.length()) {
        } else {
          buf.append(c);
        }
        prev = c;
      }
    }
    return buf.toString();
  }

  public final static boolean contains(String s, char[] value) {
    boolean isContained = false;
    if (value.length > 0 && value.length <= s.length()) {
      int end = s.length() - value.length + 1;
      for (int i = 0; !isContained && i < end; ++i) {
        char c = s.charAt(i);
        if (c == value[0]) {
          boolean found = true;
          for (int j = 1; found && j < value.length && j < s.length(); ++j) {
            char d = s.charAt(i+j);
            if (d != value[j]) {
              found = false;
            }
          }
          if (found) {
            isContained = true;
          }
        }
      }
    }
    return isContained;
  }

  public static final String constructNameFromValue(String tag) {

    boolean started = false;
    boolean whitespace = false;
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < tag.length(); ++i) {
      char c = tag.charAt(i);
      if (!started) {
        if (Character.isWhitespace(c)) {
          continue;
        }
        if (Character.isDigit(c)) {
          buf.append("_");
          buf.append(c);
          started = true;
        } else if (Character.isLetter(c) || c == '_' || c == '-') {
          buf.append(c);
          started = true;
        }
      } else {
        if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.') {
          if (whitespace) {
            buf.append("_");
            whitespace = false;
          }
          buf.append(c);
        } else if (Character.isWhitespace(c)) {
          whitespace = true;
        }
      }
    }
    return buf.toString();
  }

  public static boolean isWhitespaceOrEmpty(String s) {
    int len = s.length();
    int st = 0;

    while ((st < len) && (s.charAt(st) <= ' ')) {
      st++;
    }
    return st == len ? true : false;
  }
}
