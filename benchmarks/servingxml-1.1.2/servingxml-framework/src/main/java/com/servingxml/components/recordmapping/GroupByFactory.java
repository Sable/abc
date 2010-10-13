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

import com.servingxml.util.Name;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Environment;

/**
 * Implements a factory class for creating new <tt>InverseRecordContent</tt> instances.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */                                                             
                                                   
class GroupByFactory implements MapXmlFactory {

  private final Environment env;
  private final Name recordTypeName;
  private final SubstitutionExpr[] fieldNames;
  private final MapXmlFactory childFactory;

  public GroupByFactory(Environment env,
                        Name recordTypeName, SubstitutionExpr[] fieldNames,MapXmlFactory childFactory) {
    this.env = env;
    this.fieldNames = fieldNames;
    this.childFactory = childFactory;
    this.recordTypeName = recordTypeName;
  }

  public MapXml createMapXml(ServiceContext context) {
    MapXml child = childFactory.createMapXml(context);
    GroupRecognizer recognizer = new FieldGroupRecognizer(recordTypeName, fieldNames);
    MapXml recordMap = new GroupBy(env, recognizer, child);
    return recordMap;
  }

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory recordTemplatesFactory) {
  }

  public boolean isGroup() {
    return true;
  }

  public boolean isRecord() {
    return false;
  }
}                                 

