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

package com.servingxml.components.flatfile.layout;

import com.servingxml.util.record.Value;

/**
* Receive notification of the raw data from the body of a flat file (excluding header and trailer)
* as the file is being read or written.
* A file signature may be calculated from the raw data.
* 
* <p>When the file is being written, <code>data</code> is called one or more times, followed by <code>getSignature</code>.</p>
* 
* <p>When the file is being read, <code>data</code> is called one or more times, followed by <code>validate</code>.
* A previously computed signature value is passed to <code>validate</code>, to compare against.</p>
* 
* @author  Daniel A. Parker
*/

public interface SignatureMethod {

  /**
  * Receive notification of raw data from body of flat file.
  * 
  * <p>Data may be received in a single chunk or in multiple chunks.</p>
  * @param bytes the bytes from the body of the flat file.
  * @param start the start position in the array
  * @param length the number of characters to read from the array
  */

  void data(byte[] bytes, int start, int length)
  ;

  /**
  * Receive notification to validate the computed file signature against its expected value
  * @param expectedValue the expected value of the file signature
  */

  void validate(Value expectedValue);

  /**
  * Gets the value of the signature calculated in the <code>data</code> method
  * @return the signature value
  */

  Value getSignature();
}


