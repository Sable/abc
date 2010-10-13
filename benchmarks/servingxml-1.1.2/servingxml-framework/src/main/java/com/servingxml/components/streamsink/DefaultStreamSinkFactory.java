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

package com.servingxml.components.streamsink; 

import com.servingxml.app.ServiceContext;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.app.Flow;
import java.nio.charset.Charset;

public class DefaultStreamSinkFactory implements StreamSinkFactory {
  private final Charset charset;

  public DefaultStreamSinkFactory() {
    this.charset = null;
  }

  public DefaultStreamSinkFactory(Charset charset) {
    this.charset = charset;
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }

  public StreamSink createStreamSink(ServiceContext context, Flow flow) {
    return charset == null ? flow.getDefaultStreamSink() : 
      new DefaultStreamSink(flow.getDefaultStreamSink(), charset);
  }
}

