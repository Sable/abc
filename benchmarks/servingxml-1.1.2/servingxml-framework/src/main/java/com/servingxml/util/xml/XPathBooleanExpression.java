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

package com.servingxml.util.xml;

import javax.xml.transform.Source;
import javax.xml.transform.ErrorListener;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface XPathBooleanExpression {
  public static final XPathBooleanExpression ALWAYS_TRUE = new AlwaysTrue();
  public static final XPathBooleanExpression ALWAYS_FALSE = new AlwaysFalse();

  void setUriResolverFactory(UriResolverFactory uriResolverFactory);

  void setErrorListener(ErrorListener errorListener);

  boolean evaluate(Source source, Record parameters);

  static final class AlwaysTrue implements XPathBooleanExpression {
    public final boolean evaluate(Source source, Record parameters) {
      return true;
    }

    public void setUriResolverFactory(UriResolverFactory uriResolverFactory) {
    }

    public void setErrorListener(ErrorListener errorListener) {
    }
  }

  static final class AlwaysFalse implements XPathBooleanExpression {
    public final boolean evaluate(Source source, Record parameters) {
      return false;
    }

    public void setUriResolverFactory(UriResolverFactory uriResolverFactory) {
    }

    public void setErrorListener(ErrorListener errorListener) {
    }
  }
}



