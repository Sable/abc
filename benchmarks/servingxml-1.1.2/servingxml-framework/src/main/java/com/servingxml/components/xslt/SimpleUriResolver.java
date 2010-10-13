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

package com.servingxml.components.xslt;

import java.net.URL;

import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import com.servingxml.util.UrlHelper;
import com.servingxml.util.ServingXmlException;

/**
* This class provides the service of converting a URI into an InputSource.
* It is used to get stylesheet modules referenced by xsl:import and xsl:include.
*
* @author Daniel Parker
*/

public class SimpleUriResolver implements URIResolver {

  public SimpleUriResolver() {
  }

  public Source resolve(String href, String base)
  throws TransformerException {

    String relativeURI = href;

    URL url;        
    try {
      url = UrlHelper.createUrl(href,base);
    } catch (ServingXmlException e) {
      throw new TransformerException("Malformed URL " + href + "(base " + base + ")", e);
    }

    Source source = new StreamSource(url.toString());

    return source;
  }
}

