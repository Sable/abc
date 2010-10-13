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

package com.servingxml.components.regex;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

import com.servingxml.util.ServingXmlException;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class JavaPatternMatcher implements PatternMatcher {

  private final Pattern pattern;

  public JavaPatternMatcher(String stringPattern, boolean caseSensitive, boolean useRegex) {
    try {
      int options = 0;
      if (!caseSensitive) {
        options |= (Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE); 
      }
      if (!useRegex) {
        options |= Pattern.LITERAL;
      }
      this.pattern = Pattern.compile(stringPattern, options);
    } catch (PatternSyntaxException e) {
      String message = "Invalid regular expression \"" + stringPattern + "\"";
      throw new ServingXmlException(message,e);
    }
  }

  public final boolean search(String input) {

    Matcher matcher = pattern.matcher(input);
    return matcher.lookingAt();
  }

  public final boolean match(String input) {

    Matcher matcher = pattern.matcher(input);
    return matcher.matches();
  }

  public final boolean matchAndReplace(String input, 
                                       String format, StringBuffer output) {

    Matcher matcher = pattern.matcher(input);
    boolean matched = matcher.matches();
    if (matched) {
      String s = matcher.replaceAll(format);
      output.append(s);
    }

    return matched;
  }

  public void searchAndReplace(String input, String format, StringBuffer output) {
    //System.out.println("pattern="+pattern.pattern()+",input="+input+",format="+format);
    Matcher matcher = pattern.matcher(input);
    String s = matcher.replaceAll(format);
    output.append(s);
/*
    while (matcher.find()) {
      //System.out.println("found");
      matcher.appendReplacement(output, format);
    }
    matcher.appendTail(output);
*/
  }
}

