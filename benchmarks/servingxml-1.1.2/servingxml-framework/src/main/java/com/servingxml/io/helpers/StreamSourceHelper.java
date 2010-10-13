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

package com.servingxml.io.helpers; 

import java.net.URL;
import java.nio.charset.Charset;

import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsource.DefaultJaxpStreamSource;
import com.servingxml.io.streamsource.InputStreamSourceAdaptor;
import com.servingxml.io.streamsource.StringStreamSource;
import com.servingxml.io.streamsource.url.UrlSource;
import com.servingxml.util.UrlHelper;
import com.servingxml.util.ServingXmlException;

public final class StreamSourceHelper {

  private StreamSourceHelper() {
  }

  public static StreamSource fromJaxpSource(javax.xml.transform.Source source) {
    StreamSource streamSource = null;

    if (source instanceof javax.xml.transform.stream.StreamSource) {
      javax.xml.transform.stream.StreamSource ssource = (javax.xml.transform.stream.StreamSource)source;
      if (ssource.getInputStream() != null) {
        if (ssource.getSystemId() != null) {
          streamSource = new InputStreamSourceAdaptor(ssource.getInputStream(), ssource.getSystemId());
        } else {
          streamSource = new InputStreamSourceAdaptor(ssource.getInputStream());
        }
      } else if (ssource.getReader() != null) {
        ReaderInputStream is = new ReaderInputStream(ssource.getReader());
        Charset charset = is.getCharset();
        if (ssource.getSystemId() != null) {
          streamSource = new InputStreamSourceAdaptor(is, ssource.getSystemId(), charset);
        } else {
          streamSource = new InputStreamSourceAdaptor(is, charset);
        }
      } else if (ssource.getSystemId() != null) {
        try {
          URL url = UrlHelper.createUrl(ssource.getSystemId());
          streamSource = new UrlSource(url);
        } catch (ServingXmlException e) {
          streamSource = null;
        }
      }
    }
    if (streamSource == null) {
      streamSource = new DefaultJaxpStreamSource(source);
    }
    return streamSource;
  }
}
