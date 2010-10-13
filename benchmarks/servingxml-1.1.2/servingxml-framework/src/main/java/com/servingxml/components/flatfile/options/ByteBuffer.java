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

package com.servingxml.components.flatfile.options;

import java.io.IOException;

public interface ByteBuffer {
  int maxLength();

  int getPosition();

  void setPosition(int position);

  void next(int n) throws IOException;

  int getReserved();

  void setReserved(int reserved);

  void next() throws IOException;

  boolean done();

  byte current();

  byte[] buffer();

  int start();

  int length();

  boolean startsWith(byte[] prefix) throws IOException;

  //boolean startsWith(char[] prefix) throws IOException;

  void clear();
}
