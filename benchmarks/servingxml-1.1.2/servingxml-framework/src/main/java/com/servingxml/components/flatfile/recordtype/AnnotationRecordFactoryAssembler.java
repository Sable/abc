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

package com.servingxml.components.flatfile.recordtype;

import org.w3c.dom.Element;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;

/**
 * The <code>AnnotationRecordFactoryAssembler</code> implements an assembler for
 * assembling <code>FlatRecordType</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class AnnotationRecordFactoryAssembler extends FlatFileOptionsFactoryAssembler {
  
  private int width = -1;
  private int recordLength = -1;
  
  public void setRecordLength(int recordLength) {
    this.recordLength = recordLength;
  }

  public void setWidth(int width) {
    this.width = width;
  }
  
  public FlatRecordTypeFactory assemble(ConfigurationContext context) {

    Element element = context.getElement();
    String value = DomHelper.getInnerText(element);

    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    FlatRecordTypeFactory lineDescriptor = new AnnotationRecordFactory(value, width, recordLength, 
                                                                       flatFileOptionsFactory);
    
    return lineDescriptor;
  }
}                                        
