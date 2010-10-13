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

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;

/**
 *
 * 
 * @author  Daniel A. Parker
 * @see SignatureMethod
 */

public interface FlatFileSignature {
  public static final FlatFileSignature[] EMPTY_ARRAY = new FlatFileSignature[0];

  void readMetaRecord(ServiceContext context, Record parameters, Record record);

  Record updateMetaRecord(ServiceContext context, Record parameters, Record record);

  void data(byte[] bytes, int start, int length);

  void validate(ServiceContext context, Record parameters);
}


