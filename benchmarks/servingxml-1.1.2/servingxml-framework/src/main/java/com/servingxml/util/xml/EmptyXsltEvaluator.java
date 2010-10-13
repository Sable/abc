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

import javax.xml.transform.Templates;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;

import com.servingxml.util.Name;
import com.servingxml.util.QnameContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordContentHandler;
import com.servingxml.util.record.RecordReceiver;

/**
 * A <code>XsltEvaluator</code> implement an <code>XsltEvaluator</code> interface
 *
 * 
 * @author  Daniel A. Parker
 */

public class EmptyXsltEvaluator implements XsltEvaluator {
  private final Name recordTypeName;

  public EmptyXsltEvaluator(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public Record evaluate(Source source, Record parameters) {
    return Record.EMPTY;
  }

  public boolean isEmpty() {
    return true;
  }

  public void setUriResolverFactory(UriResolverFactory uriResolverFactory) {
  }
}

