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

package com.servingxml.components.serializer;

import java.util.Enumeration;

import org.xml.sax.SAXException;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.property.OutputProperty;
import com.servingxml.components.property.OutputPropertyFactory;
import com.servingxml.app.Flow;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.components.streamsink.StreamSinkFactory;

/**
 * 
 * @author  Daniel A. Parker
 */

public class BatchedSerializerFactory implements SaxSinkFactory {
  private final SaxSinkFactory saxSinkFactory;
  private final RestrictedMatchPattern expr;
  private final long batchSize;
  private final int maxFiles;

  public BatchedSerializerFactory(SaxSinkFactory saxSinkFactory, RestrictedMatchPattern expr, long batchSize,
                                  int maxFiles) {
    this.saxSinkFactory = saxSinkFactory;
    this.expr = expr;
    this.batchSize = batchSize;
    this.maxFiles = maxFiles;
  }

  public SaxSink createSaxSink(ServiceContext context, Flow flow) {
    BatchedContentHandler handler = new BatchedContentHandler(context,flow,saxSinkFactory,expr,batchSize,maxFiles);
    return new BatchedSerializer(handler);
  }
}
