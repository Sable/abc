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

package com.servingxml.components.content.dynamic;

import com.servingxml.util.Asserter;

/**
 * This class implements an equality relation between two arrays.
 * Here, two arrays are regarded as equal if they share the same
 * elements, irrespective of order.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ArrayIdentifier implements Identifier {
  private static final String sourceClass = ArrayIdentifier.class.getName();

  public boolean equalTo(Object o1, Object o2) {
    final String sourceMethod = "equalTo";

    Asserter.assertTrue(sourceClass,sourceMethod,"arg1 is array",o1.getClass().isArray());
    Asserter.assertTrue(sourceClass,sourceMethod,"arg2 is array",o2.getClass().isArray());

    boolean isEqual = true;

    Object[] a1 = (Object[])o1;
    Object[] a2 = (Object[])o2;

    isEqual = (a1.length == a2.length);

    for (int i = 0; isEqual && i < a1.length; ++i) {
      Object o = a1[i];
      //  Try adjacent element first
      isEqual = o.equals(a2[i]);
      if (!isEqual) {
        for (int j = 0; !isEqual && j < a2.length; ++j) {
          if (i != j) {
            isEqual = o.equals(a2[j]);
          }
        }
      }
    }
    return isEqual;
  }
}

