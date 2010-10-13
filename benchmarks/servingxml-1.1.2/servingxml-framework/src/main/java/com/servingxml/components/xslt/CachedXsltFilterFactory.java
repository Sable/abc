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

import javax.xml.transform.Templates;

import com.servingxml.app.ServiceContext;
import com.servingxml.io.saxsource.SaxSource;
import com.servingxml.io.cache.Cache;
import com.servingxml.io.cache.Key;
import com.servingxml.io.cache.CacheEntry;
import com.servingxml.io.cache.RevalidationType;
import com.servingxml.components.saxsource.SaxSourceFactory;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.common.UrlEvaluator;
import com.servingxml.components.content.Content;
import com.servingxml.components.parameter.WithParameters;
import com.servingxml.app.Environment;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class CachedXsltFilterFactory extends AbstractXsltFilterFactory implements Content {

  private final RevalidationType revalidationType;
  private final Cache cache;

  public CachedXsltFilterFactory(Environment env, SaxSourceFactory saxSourceFactory, UrlEvaluator docBaseResolver, WithParameters withParameters, 
                                  Cache cache, RevalidationType revalidationType) {
    super(env,saxSourceFactory,docBaseResolver, withParameters);

    this.cache = cache;
    this.revalidationType = revalidationType;
  }

  public Templates getTemplates(ServiceContext context, SaxSource source) {

    try {
      Templates templates;
      if (source.getExpirable().immediateExpiry()) {
        templates = super.getTemplates(context, source);
      } else {
        Key key = source.getKey();
        templates = (Templates)cache.get(key);
        if (templates == null) {                                                  
          templates = super.getTemplates(context, source);
          XsltExpirable styleExpirable = new XsltExpirable(context, source);
          if (!styleExpirable.immediateExpiry()) {
            cache.add(key,templates,styleExpirable,revalidationType);
          }
        }
      }
      return templates;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}

