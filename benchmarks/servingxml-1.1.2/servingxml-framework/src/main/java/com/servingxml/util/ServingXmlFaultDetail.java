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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ServingXmlFaultDetail {

  private final List<Entry> detail;

  public ServingXmlFaultDetail() {
     this.detail = new ArrayList<Entry>();
  }

  public void addEntry(Name name, String message) {
    detail.add(new Entry(name,message));
  }

  public int size() {
    return detail.size();
  }

  public Iterator<Entry> entries() {
    return detail.iterator();
  }
  
  public String toString() {
    Iterator iter = entries();
    StringBuilder buf = new StringBuilder();
    String nl = System.getProperty("line.separator");
    while (iter.hasNext()) {
      Entry entry = (Entry)iter.next();
      buf.append(entry.getMessage()+nl);
    }
    return buf.toString();
  }

  public static class Entry {

    private final Name name;
    private final String message;

    public Entry(Name name, String message) {
      this.name = name;
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    public Name getName() {
      return name;
    }
  }
}

