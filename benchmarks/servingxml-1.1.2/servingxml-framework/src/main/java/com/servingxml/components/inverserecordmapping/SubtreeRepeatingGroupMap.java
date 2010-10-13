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

package com.servingxml.components.inverserecordmapping;

import com.servingxml.util.Name;
import com.servingxml.util.xml.Matchable;
import com.servingxml.util.xml.RepeatingGroupMatchable;
import com.servingxml.util.xml.ParameterParser;

/**
 * Defines an interface for a subtree field map.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SubtreeRepeatingGroupMap implements SubtreeFieldMap {

  private final String matchExpr;
  private final Name fieldName;
  private final String matchExpr2;
  private final Name recordTypeName;
  private final SubtreeFieldMap[] subtreeFieldMaps;

  public SubtreeRepeatingGroupMap(String matchExpr, Name fieldName, String matchExpr2, 
    Name recordTypeName, SubtreeFieldMap[] subtreeFieldMaps) {

    this.matchExpr = matchExpr;
    this.fieldName = fieldName;
    this.matchExpr2 = matchExpr2;
    this.recordTypeName = recordTypeName;
    this.subtreeFieldMaps = subtreeFieldMaps;
  }

  public Matchable createMatchable(String mode) {
    Matchable[] matchables = new Matchable[subtreeFieldMaps.length];
    for (int i = 0; i < subtreeFieldMaps.length; ++i) {
      String mode2 = mode+"."+i;
      SubtreeFieldMap fieldMap = subtreeFieldMaps[i];
     //System.out.println(getClass().getName()+".assemble fieldMap is " + fieldMap.getClass().getName());
      Matchable matchable = fieldMap.createMatchable(mode2);
     //System.out.println(getClass().getName()+".assemble matchable is " + matchable.getClass().getName());
      matchables[i] = matchable;
    }

    Matchable matchable2 = new RepeatingGroupMatchable(mode, matchExpr, fieldName, 
      matchExpr2, recordTypeName, matchables);
    return matchable2;
  }

  public void putParameters(ParameterParser paramParser) {
    for (int i = 0; i < subtreeFieldMaps.length; ++i) {
      subtreeFieldMaps[i].putParameters(paramParser);
    }
  }
}
