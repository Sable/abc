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

import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.components.common.ChildEvaluator;
import com.servingxml.util.Name;
import com.servingxml.util.xml.Matchable;
import com.servingxml.util.xml.MatchableFactory;
import com.servingxml.util.xml.SelectableImpl;
import com.servingxml.util.xml.Selectable;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.util.QnameContext;
import com.servingxml.app.ServiceContext;

/**
 * Implements a factory class for creating new <tt>MapXml</tt> instances.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                        
class FieldElementSequenceMapFactory implements MapXmlFactory {
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final QnameContext nameContext;
  private final NameSubstitutionExpr nameResolver;
  private final ChildEvaluator containerEvaluator;
  private final MapXmlFactory childFactories;

  public FieldElementSequenceMapFactory(QnameContext nameContext, NameSubstitutionExpr nameResolver, ChildEvaluator containerEvaluator,
    MapXmlFactory childFactories) {
    this.nameContext = nameContext;
    this.nameResolver = nameResolver;
    this.containerEvaluator = containerEvaluator;
    this.childFactories = childFactories;
  }

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory recordTemplatesFactory) {
    containerEvaluator.addToXsltEvaluator(mode, recordTemplatesFactory);
  }

  public MapXml createMapXml(ServiceContext context) {

    MapXml rm = childFactories.createMapXml(context);
    MapXml recordMap = new FieldElementSequenceMap(nameContext, nameResolver, containerEvaluator, rm);

    return recordMap;
  }

  public boolean isGroup() {
    return false;
  }

  public boolean isRecord() {
    return false;
  }
}
