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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.flatfile.options.DelimiterFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactoryAssembler;
import com.servingxml.components.flatfile.options.RecordDelimiterFactory;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.components.recordio.RecordReaderFactoryPrefilter;
import com.servingxml.components.streamsource.DefaultStreamSourceFactory;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;

/**
 * The <code>FlatFileReaderFactoryAssembler</code> implements an assembler for
 * assembling <code>FlatFileReaderFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFileReaderFactoryAssembler extends FlatFileOptionsFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private FlatFile flatFile = null;
  private StreamSourceFactory streamSourceFactory = new DefaultStreamSourceFactory();
  private long fromRecord = 1;
  private long maxRecordCount = Long.MAX_VALUE;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void setFromRecord(long fromRecord) {
    this.fromRecord = fromRecord;
  }

  public void setMaxRecordCount(long maxRecordCount) {
    this.maxRecordCount = maxRecordCount;
  }

  public void injectComponent(FlatFile flatFile) {
    this.flatFile = flatFile;
  }

  public void injectComponent(StreamSourceFactory streamSourceFactory) {
    this.streamSourceFactory = streamSourceFactory;
  }
  
  public RecordReaderFactory assemble(ConfigurationContext context) {

    if (streamSourceFactory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
        context.getElement().getTagName(),"sx:streamSource");
      throw new ServingXmlException(message);
    }

    RecordReaderFactory readerFactory;
    FlatFileOptionsFactory flatFileOptionsFactory = assembleFlatFileOptions(context);
    //if (flatFileOptionsFactory.getRecordDelimiterFactories().length == 0) {
      //flatFileOptionsFactory.setRecordDelimiterFactories(RecordDelimiterFactory.DEFAULT);
    //}
    if (flatFile == null) {
      //DelimiterFactory[] myRecordDelimiters = flatFileOptionsFactory.getRecordDelimiterFactories();
      //System.out.println(getClass().getName()+".initialize 1 "+myRecordDelimiters.length);
      //for (int i = 0; i < myRecordDelimiters.length; ++i) {
        //System.out.println(myRecordDelimiters[i].getClass().getName());
      //}
      //flatFileOptionsFactory.setRecordDelimiterFactories(RecordDelimiterFactory.DEFAULT);
      //myRecordDelimiters = flatFileOptionsFactory.getRecordDelimiterFactories();
      //System.out.println(getClass().getName()+".initialize 2 "+myRecordDelimiters.length);
      //for (int i = 0; i < myRecordDelimiters.length; ++i) {
        //System.out.println(myRecordDelimiters[i].getClass().getName());
      //}
      readerFactory = new DefaultFlatFileReaderFactory(flatFileOptionsFactory, streamSourceFactory, 
        fromRecord, maxRecordCount);
    } else {
      readerFactory = new FlatFileReaderFactory(flatFileOptionsFactory, streamSourceFactory,flatFile,fromRecord,maxRecordCount);
    }

    if (parameterDescriptors.length > 0) {
      readerFactory = new RecordReaderFactoryPrefilter(readerFactory,parameterDescriptors);
    }

    return readerFactory;
  }
}

