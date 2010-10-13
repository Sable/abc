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

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.components.flatfile.recordtype.FlatRecordTypeFactory;
import com.servingxml.components.flatfile.recordtype.AnnotationRecordFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;

/**
 * The <code>FlatFileHeaderAssembler</code> implements an assembler for
 * assembling <code>FlatFileHeader</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileHeaderAssembler extends FlatFileOptionsFactoryAssembler {
                                                       
  private int lineCount = 0;
  private FlatRecordTypeFactory[] metaRecordFactories = new FlatRecordTypeFactory[0];
  private int recordLength = -1;
  
  public void setLineCount(int lineCount) {
    this.lineCount = lineCount;
  }

  public void setRecordLength(int recordLength) {
    this.recordLength = recordLength;
  }
  
  public void injectComponent(FlatRecordTypeFactory[] metaRecordFactories) {
    this.metaRecordFactories = metaRecordFactories;
  }
  
  public FlatFileHeader assemble(ConfigurationContext context) {
    
    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    if (lineCount < metaRecordFactories.length) {
      lineCount = metaRecordFactories.length;
    } else if (lineCount > metaRecordFactories.length) {
      FlatRecordTypeFactory[] old = metaRecordFactories;
      metaRecordFactories = new FlatRecordTypeFactory[lineCount];
      for (int i = 0; i < old.length; ++i) {
        metaRecordFactories[i] = old[i];
      }
      for (int i = old.length; i < lineCount; ++i) {
        metaRecordFactories[i] = new AnnotationRecordFactory("", 0, recordLength, flatFileOptionsFactory);
      }
    }
    
    FlatFileHeader header = new FlatFileHeader(metaRecordFactories);
    return header;
  }
}
