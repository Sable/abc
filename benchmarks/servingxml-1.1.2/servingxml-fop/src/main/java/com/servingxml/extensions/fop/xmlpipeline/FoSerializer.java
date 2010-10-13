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

import java.util.Properties;

import org.xml.sax.ContentHandler;             

import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.FOPException;

import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.ServingXmlException;
import com.servingxml.io.saxsink.SaxSink;

/**
 * Takes the transformed XML and applies the apache fop driver to produce pdf,
 * which is then written to the output stream.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FoSerializer implements SaxSink {
  private final ContentHandler contentHandler;
  private final StreamSink streamSink;

  public FoSerializer(StreamSink streamSink, String mimeType) {

    try {
      this.streamSink = streamSink;

      FopFactory fopFactory = FopFactory.newInstance();
      Fop fop = fopFactory.newFop(mimeType, streamSink.getOutputStream());
      contentHandler = fop.getDefaultHandler();
    } catch (FOPException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public ContentHandler getContentHandler() {
    return contentHandler;
  }

  public void close() {
    ServingXmlException badDispose = null;
    try {
      streamSink.close();
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(),e);
    }
    if (badDispose != null) {
      throw badDispose;
    }
  }

  public void setOutputProperties(Properties outputProperties) {
  }
}

