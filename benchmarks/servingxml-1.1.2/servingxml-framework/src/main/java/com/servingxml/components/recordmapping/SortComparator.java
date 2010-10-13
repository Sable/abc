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

package com.servingxml.components.recordmapping;

import java.util.Comparator;

import com.servingxml.app.Flow;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */


class SortComparator implements Comparator {
  private final Sort[] sorts;

  public SortComparator(Sort[] sorts) {
    this.sorts = sorts;
  }

  public int compare(Object o1, Object o2) {
    Flow flow1 = (Flow)o1;
    Flow flow2 = (Flow)o2;

    int diff = 0;
    for (int i = 0; diff == 0 && i < sorts.length; ++i) {
      Sort sort = sorts[i];
      diff = sort.compare(flow1.getRecord(),flow2.getRecord());
    }
    return diff;
  }
}

