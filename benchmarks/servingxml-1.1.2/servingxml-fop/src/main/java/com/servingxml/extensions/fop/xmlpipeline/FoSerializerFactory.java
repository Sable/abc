/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 *  MIME type extension contributed by Kenneth Westelinck
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

package com.servingxml.extensions.fop.xmlpipeline;

import javax.xml.transform.OutputKeys;

import org.apache.avalon.framework.logger.Logger;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.app.Flow;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.expr.substitution.SubstitutionExpr;

/**
 * 
 * @author  Daniel A. Parker
 */

public class FoSerializerFactory implements SaxSinkFactory {
  private final StreamSinkFactory sinkFactory;
  private final SubstitutionExpr mimeTypeExpr;

  public FoSerializerFactory(StreamSinkFactory sinkFactory, SubstitutionExpr mimeTypeExpr) { 
    this.sinkFactory = sinkFactory; 
    this.mimeTypeExpr = mimeTypeExpr; 
  } 

  public SaxSink createSaxSink(ServiceContext context, Flow flow) {
    try {
      StreamSink streamSink = sinkFactory.createStreamSink(context, flow);
      String mimeType = mimeTypeExpr.evaluateAsString(flow.getParameters(),flow.getRecord());

      streamSink.setOutputProperty(OutputKeys.MEDIA_TYPE,mimeType);
      return new FoSerializer(streamSink, mimeType);
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }
}
