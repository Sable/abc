/**
 *  ServingXML
 *  
 *  Copyright (C) 2008  Daniel Parker
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

package com.servingxml.util.xml;

import javax.xml.transform.URIResolver;
import com.servingxml.util.record.Record;
import com.servingxml.util.PrefixMap;

/**
 * A <code>UriResoverFactory</code> defines an interface for a 
 * factory for creating a <code>URIResolver</code> object. 
 *
 * 
 * @author  Daniel A. Parker
 */

public abstract class UriResolverFactory {
  private static final UriResolverFactory instance = new DefaultUriResolverFactory();

  public static UriResolverFactory getDefault() {
    return instance;
  } 

  public abstract URIResolver createUriResolver(PrefixMap prefixMap, String baseUri, Record parameters, 
                                                URIResolver defaultResolver);

  static final class DefaultUriResolverFactory extends UriResolverFactory {

    public URIResolver createUriResolver(PrefixMap prefixMap, String baseUri, Record parameters, 
                                         URIResolver defaultResolver) {
      return defaultResolver;
    }
  }
}
