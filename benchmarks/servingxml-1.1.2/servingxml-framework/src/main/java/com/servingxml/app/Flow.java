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

package com.servingxml.app;

import com.servingxml.util.record.Record;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.saxsink.SaxSink;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public abstract class Flow {
  public abstract Record getParameters();

  public abstract StreamSource getDefaultStreamSource();

  public abstract SaxSource getDefaultSaxSource();   

  public abstract Record getRecord();

  public abstract StreamSink getDefaultStreamSink();

  public abstract SaxSink getDefaultSaxSink();

  public abstract int getCurrentLineNumber();

  public abstract Flow augmentParameters(ServiceContext context, ParameterDescriptor[] parameterDescriptors);

  public abstract Flow replaceParameters(ServiceContext context, Record parameters);

  public abstract Flow replaceRecord(ServiceContext context, Record record);

  public abstract Flow replaceRecord(ServiceContext context, Record record, int lineNumber);

  public abstract Flow replaceDefaultStreamSink(ServiceContext context, StreamSink streamSink);

  public abstract Flow replaceDefaultSaxSource(ServiceContext context, SaxSource saxSource);

  public abstract Flow replaceDefaultStreamSource(ServiceContext context, StreamSource streamSource);

  public abstract Flow replaceDefaultSaxSink(ServiceContext context, SaxSink SaxSink);
}

