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


/**
 * Factory for creating pattern matchers.
 *
 * <p>A <code>RegularExpressionFactory</code> instance may be used to create <code>PatternMatcher</code> objects.
 * There is a single instance of a <code>PatternMatcher</code>
 * which may be obtained through its static <code>getInstance()</code> method.</p>
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface PatternMatcherFactory {

  public PatternMatcherFactory DEFAULT = new JavaPatternMatcherFactory();

  PatternMatcher createPatternMatcher(String expression);

  PatternMatcher createPatternMatcher(String expression, boolean caseSensitive, boolean useRegex);

  static class JavaPatternMatcherFactory implements PatternMatcherFactory {
    public final PatternMatcher createPatternMatcher(String expression) {
      return new JavaPatternMatcher(expression, true, true);
    }
    public final PatternMatcher createPatternMatcher(String expression, boolean caseSensitive, boolean useRegex) {
      return new JavaPatternMatcher(expression, caseSensitive, useRegex);
    }
  }
}

