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
import com.servingxml.util.Name;
import com.servingxml.components.string.Stringable;
import com.servingxml.util.xml.Selectable;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.QnameContext;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Environment;

/**                                      
 * Implements a factory class for creating <tt>InverseRecordMappingInlineContent</tt> instances.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class LiteralContent implements MapXmlFactory {

  private final Environment env;
  private final NameSubstitutionExpr nameResolver;
  private final PrefixMap prefixMap;
  private final AttributesImpl attributes;
  private final Stringable stringFactory;
  private final MapXmlFactory childFactories;

  public LiteralContent(Environment env, NameSubstitutionExpr nameResolver, PrefixMap prefixMap, AttributesImpl attributes,
  Stringable stringFactory, 
  MapXmlFactory childFactories) {
    this.env = env;
    this.nameResolver = nameResolver;
    this.prefixMap = prefixMap;
    this.attributes = attributes;
    this.stringFactory = stringFactory;
    this.childFactories = childFactories;
  }
  
  public MapXml createMapXml(ServiceContext context) {

    MapXml rm = childFactories.createMapXml(context);
    MapXml handler = new GenerateElement(env, nameResolver, attributes, stringFactory, rm);

    return handler; 
  }

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory recordTemplatesFactory) {
  }

  public boolean isGroup() {
    return childFactories.isGroup() || childFactories.isRecord();
  }

  public boolean isRecord() {
    return false;
  }

  // Test methods
  AttributesImpl getAttributes() {return attributes;}
  String getValue() {return "";}
}


