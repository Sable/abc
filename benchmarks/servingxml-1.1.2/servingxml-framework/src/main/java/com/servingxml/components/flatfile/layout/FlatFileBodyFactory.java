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
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.recordtype.FlatRecordType;
import com.servingxml.components.flatfile.recordtype.FlatRecordTypeFactory;

/**
 * The <code>FlatFileBodyFactory</code> 
 * defines a flat file record.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileBodyFactory {
                                                       
  private final FlatRecordTypeFactory flatRecordTypeFactory;
  private final FlatFileOptionsFactory flatFileOptionsFactory;

  public FlatFileBodyFactory(FlatRecordTypeFactory flatRecordTypeFactory,
                             FlatFileOptionsFactory flatFileOptionsFactory) {

    this.flatRecordTypeFactory = flatRecordTypeFactory;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
  }
                                                                    
  public FlatFileBody createFlatFileBody(ServiceContext context, Flow flow, FlatFileOptions defaultOptions) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context, flow, defaultOptions);
    FlatRecordType flatRecordType = flatRecordTypeFactory.createFlatRecordType(context, flow, flatFileOptions);
    return new FlatFileBody(flatRecordType,flatFileOptions);
  }

  public boolean isFieldDelimited() {
    return flatRecordTypeFactory.isFieldDelimited();
  }

  public boolean isBinary() {
    return flatRecordTypeFactory.isBinary();
  }

  public boolean isText() {
    return flatRecordTypeFactory.isText();
  }
}
