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

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.util.record.Record;
import com.servingxml.util.Name;
import com.servingxml.components.common.SimpleNameEvaluator;
import com.servingxml.components.common.NameSubstitutionExpr;
import com.servingxml.util.NameTest;
import com.servingxml.components.string.Stringable;
import com.servingxml.util.QualifiedName;
import com.servingxml.components.recordio.RecordAccepter;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;
import com.servingxml.util.QnameContext;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Environment;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DefaultRecordMappingFactory implements RecordMappingFactory {
  public static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final ParameterDescriptor[] parameterDescriptors;
  private final MapXmlFactory tailFactory;

  public DefaultRecordMappingFactory(ParameterDescriptor[] parameterDescriptors,
                                     QnameContext nameContext, XsltConfiguration xsltConfiguration, 
    Name documentName) {

    this.parameterDescriptors = parameterDescriptors;
    NameSubstitutionExpr nameResolver = new SimpleNameEvaluator(documentName);

    MapXmlFactory mapFactory = new DefaultFieldElementMapFactory(nameContext.getPrefixMap(),NameTest.ANY,NameTest.NONE);
    MapXmlFactory[] tailFactories = new MapXmlFactory[]{mapFactory};
    MultipleMapXmlFactory tailrmf = new MultipleMapXmlFactory(nameContext, xsltConfiguration, tailFactories);

    PrefixMap prefixMap = nameContext.getPrefixMap();
    Environment env = new Environment(parameterDescriptors,nameContext);
    MapXmlFactory onRecordContent = new GenerateElementFactory(env, new RecordTypeNameResolver(), Stringable.EMPTY, tailrmf);
    MapXmlFactory[] tailFactories2 = new MapXmlFactory[]{onRecordContent};

    MultipleMapXmlFactory tailrmf2 = new MultipleMapXmlFactory(nameContext, xsltConfiguration, tailFactories2);
    OnRecordFactory onRecordFactory = new OnRecordFactory(RecordAccepter.ALL,tailrmf2); 

    MapXmlFactory[] childFactories = new MapXmlFactory[]{onRecordFactory};

    MapXmlFactory rmf = new MultipleMapXmlFactory(nameContext, xsltConfiguration, childFactories);

    this.tailFactory = new LiteralContent(env, nameResolver, prefixMap, EMPTY_ATTRIBUTES, 
      Stringable.EMPTY, rmf);
  }

  public MapXml createMapXml(ServiceContext context) {
    MapXml tail = tailFactory.createMapXml(context);
    return tail;
  }
  
  //  TESTS

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory recordTemplatesFactory) {
  }

  public boolean isGroup() {
    return true;
  }

  public boolean isRecord() {
    return false;
  }
}

class RecordTypeNameResolver extends NameSubstitutionExpr {    
  private static final Name DEFAULT_NAME = new QualifiedName("record");

  public RecordTypeNameResolver() {
  }

  public boolean isExpression() {
    return false;
  }

  public Name getName() {
    return evaluateName(Record.EMPTY,Record.EMPTY);
  }

  public boolean hasName(Name name) {

    boolean result = false;
    if (evaluateName(Record.EMPTY,Record.EMPTY).equals(name)) {
      result = true;
    }
    return result;
  }

  public Name evaluateName(Record parameters, Record record) {
    Name name = record.getRecordType().getName();
    return name.isEmpty() ? DEFAULT_NAME : name;
  }
}

