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

package com.servingxml.components.recordmapping;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.xml.XPathBooleanExpressionFactory;

/**
 * Implements a group recognizer.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */                                                             
                                                   

public class GroupRecognizerImpl implements GroupRecognizer {
  private final GroupBreaker startGroupBreaker;
  private final GroupBreaker endGroupBreaker;
  
  public GroupRecognizerImpl(PrefixMap prefixMap, XPathBooleanExpressionFactory startTestFactory, 
                             XPathBooleanExpressionFactory endTestFactory) {
    this.startGroupBreaker = new GroupBreakerImpl(prefixMap, startTestFactory);
    this.endGroupBreaker = new GroupBreakerImpl(prefixMap, endTestFactory);
  }

  public GroupRecognizerImpl(GroupBreaker startGroupBreaker, GroupBreaker endGroupBreaker) {
    this.startGroupBreaker = startGroupBreaker;
    this.endGroupBreaker = endGroupBreaker;
  }
  
  public boolean startRecognized(ServiceContext context, Flow flow, Record previousRecord, Record currentRecord) {
    return startGroupBreaker.breakOn(context, flow, previousRecord,currentRecord);
  }

  public boolean endRecognized(ServiceContext context, Flow flow, Record currentRecord, Record nextRecord) {
    return endGroupBreaker.breakOn(context, flow, currentRecord,nextRecord);
  }
}
