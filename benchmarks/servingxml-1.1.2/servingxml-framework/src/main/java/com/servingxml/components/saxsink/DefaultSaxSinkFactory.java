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

package com.servingxml.components.saxsink;

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.components.serializer.XsltSerializer;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.saxsink.SaxSink;

/**
 * 
 * @author  Daniel A. Parker
 */

public class DefaultSaxSinkFactory implements SaxSinkFactory {

  public DefaultSaxSinkFactory() {
  }

  public SaxSink createSaxSink(ServiceContext context, Flow flow) {
    return flow.getDefaultSaxSink();
  }
}
