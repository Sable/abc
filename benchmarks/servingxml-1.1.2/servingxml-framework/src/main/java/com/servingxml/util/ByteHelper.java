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

public class ByteHelper {

  /**
  * Extracts the lower 4 bits of a byte.
  * @param b The byte
  * @return  The lower 4 bits of the supplied byte.
  */

  public static final byte lowNibble(byte b) { 
    return (byte)(b & 0xF);
  }

  /**
  * Extracts the upper 4 bits of a byte.
  * @param b The byte.
  * @return  The upper 4 bits of the supplied byte.
  */

  public static final byte highNibble(byte b) 
  {
    return lowNibble((byte)(b >> 4 ));
  }

  /**
  * Makes a byte from a low nibble and a high nibble.
  * @param lo The lower four bits of the byte.
  * @param hi The upper four bits of the byte.
  * @return The byte.
  */

  public static final byte makeByte(byte lo, byte hi) { 
    return (byte)(lo | (byte)(hi << 4));
  }
}; 
