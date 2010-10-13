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

import com.servingxml.components.saxsink.SaxSinkFactory;
import com.servingxml.components.streamsink.DefaultStreamSinkFactory;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import org.apache.fop.apps.MimeConstants;
import com.servingxml.expr.substitution.SubstitutionExpr;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class FoSerializerFactoryAssembler {
  private StreamSinkFactory sinkFactory = new DefaultStreamSinkFactory();
  private String mimeType = MimeConstants.MIME_PDF; 

  public void setMimeType(String mimeType) { 
    this.mimeType = mimeType; 
  } 

  public void injectComponent(StreamSinkFactory sinkFactory) {
    this.sinkFactory = sinkFactory;
  }

  public SaxSinkFactory assemble(ConfigurationContext context) {

    try {
      SubstitutionExpr mimeTypeResolver = SubstitutionExpr.parseString(context.getQnameContext(),mimeType);
      SaxSinkFactory saxSinkFactory = new FoSerializerFactory(sinkFactory, mimeTypeResolver);

      return saxSinkFactory;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}
