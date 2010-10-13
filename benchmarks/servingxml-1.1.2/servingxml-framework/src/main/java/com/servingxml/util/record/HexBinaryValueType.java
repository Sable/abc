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

package com.servingxml.util.record;

import java.sql.Types;

import com.servingxml.util.HexBin;

public class HexBinaryValueType implements ValueType {

  public Object[] fromStringArray(String[] sa) {
    Object[] a = new Object[0];
    for (int i = 0; i < a.length; ++i) {
      a[i] = HexBin.decode(sa[i]);
    }
    return a;
  }

  public Object fromString(String s) {
    Object o = HexBin.decode(s);
    return o;
  }

  public String toString(Object o) {
    String s = "";
    if (o != null) {
      s = HexBin.encode((byte[])o);
    }
    return s;
  }

  public Object getSqlValue(Object o) {
    return o;
  }

  public int getSqlType() {
    return Types.VARBINARY;
  }


}
