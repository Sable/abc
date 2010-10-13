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

import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.recordtype.FlatRecordType;

/**
 * The <code>FlatFileBodyFactory</code> 
 * defines a flat file record.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileBody {
                                                       
  private final FlatRecordType flatRecordType;
  private final FlatFileOptions flatFileOptions;

  public FlatFileBody(FlatRecordType flatRecordType, FlatFileOptions flatFileOptions) {

    this.flatRecordType = flatRecordType;
    this.flatFileOptions = flatFileOptions;
  }
                                                                    
  public FlatRecordType getFlatRecordType() {
    return flatRecordType;
  }

  public Delimiter[] getRecordDelimiters() {
    return flatFileOptions.getRecordDelimiters();
  }
}
