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

public class GroupStateImpl implements GroupState {
  private final GroupState parent;
  private int level;

  public GroupStateImpl(GroupState parent) {
    this.parent = parent;
    level = -1;
  }

  public boolean wasStarted() {
    return level != INITIAL;
  }

  public void startGroup() {
    if (level < 0) {
      level = 0;
    }
    ++level;
  }

  public void endGroup() {
    --level;
  }
}

