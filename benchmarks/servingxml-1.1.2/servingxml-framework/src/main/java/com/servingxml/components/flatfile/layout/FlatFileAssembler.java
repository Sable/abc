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

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;

/**
 * The <code>FlatFileAssembler</code> implements an assembler for
 * assembling <code>FlatFile</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileAssembler extends FlatFileOptionsFactoryAssembler {
                                                       
  private FlatFileHeader header = new FlatFileHeader();
  private FlatFileTrailer trailer = new FlatFileTrailer();
  private FlatFileBodyFactory bodyFactory = null;                       
  private FlatFileSignatureFactory[] signatureFactories = FlatFileSignatureFactory.EMPTY_FLAT_FILE_SIGNATURE_FACTORY_ARRAY;

  public FlatFileAssembler() {
  }

  public void injectComponent(FlatFileHeader header) {

    this.header = header;
  }

  public void injectComponent(FlatFileTrailer trailer) {

    this.trailer = trailer;
  }

  public void injectComponent(FlatFileBodyFactory bodyFactory) {

    this.bodyFactory = bodyFactory;
  }

  public void injectComponent(FlatFileSignatureFactory[] signatureFactories) {
    this.signatureFactories = signatureFactories;
  }

  public FlatFile assemble(ConfigurationContext context) {

    //  record element
    if (bodyFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),
                                                                 "sx:flatFileBody");
      throw new ServingXmlException(message);
    }

    //System.out.println(getClass().getName()+".assemble enter");
    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);

    FlatFile flatFileFactory = new FlatFile(flatFileOptionsFactory, header, 
      trailer, bodyFactory, signatureFactories);
    //System.out.println(getClass().getName()+".assemble leave");

    return flatFileFactory;
  }
}

