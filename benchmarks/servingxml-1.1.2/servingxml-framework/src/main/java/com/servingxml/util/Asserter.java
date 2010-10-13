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
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class Asserter {

  private Asserter() {
  }

  /**
   * Asserts that a condition is true.  If not, throws an Asserter.Failed exception.
   *
   * @param sourceClass the class name.
   * @param sourceMethod the method name.
   * @param message a message for the assertion
   * @param condition the condition
   */

  public static final void assertTrue(String sourceClass, String sourceMethod, 
  String message, boolean condition) {
    if ( !condition ) {
      final String s = "Assert AssertionException [" + sourceClass + "." + sourceMethod + "]" + message;
      throw new Asserter.AssertionException(s);
    }
  }

  public static class AssertionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AssertionException(String message) {
      super(message);
    }
  }
}

