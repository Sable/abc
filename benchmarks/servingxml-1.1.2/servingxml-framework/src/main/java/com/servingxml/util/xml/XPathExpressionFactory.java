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
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.QnameContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.DefaultFieldType;
import com.servingxml.util.record.FieldType;

/**
 *                 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XPathExpressionFactory {

  private final String matchExpr;
  private final String selectExpr;
  private final Templates templates;
  private final Name recordTypeName;
  private final Name[] parameterNames;
  private final TransformerFactory transformerFactory;
  private final PrefixMap prefixMap;

  public XPathExpressionFactory(QnameContext context, String selectExpr, String xsltVersion,
    TransformerFactory transformerFactory) {
    this(context,"/*",selectExpr,xsltVersion,transformerFactory);
  }    

  public XPathExpressionFactory(QnameContext context, String matchExpr, String selectExpr, String xsltVersion,
    TransformerFactory transformerFactory) {
    this.transformerFactory = transformerFactory;
    this.prefixMap = context.getPrefixMap();
    
    this.matchExpr = matchExpr;
    this.selectExpr = selectExpr;

    if (selectExpr == null || selectExpr.length() == 0) {
      selectExpr = "*";
    }

    Name FIELD_NAME = new QualifiedName("F");
    ParameterParser paramParser = new ParameterParser(context.getPrefixMap());
    paramParser.parseParameters(selectExpr);
    this.parameterNames = paramParser.getNames();
    String[] parameterQnames = paramParser.getQnames();

    Matchable matchable = new MatchableImpl(matchExpr, FIELD_NAME, selectExpr);
    Matchable[] matchables = new Matchable[]{matchable};
    this.recordTypeName = new QualifiedName("R");

    FieldType fieldType = new DefaultFieldType(FIELD_NAME);
    FieldType[] fieldTypes = new FieldType[]{fieldType};

    XmlRecordTransformReader reader = new XmlRecordTransformReader(recordTypeName, "/*", 
                                                                   matchables, context.getPrefixMap(), xsltVersion,
      parameterQnames);

    Source styleSource = new SAXSource(reader,new InputSource());
    try {
      this.templates = transformerFactory.newTemplates(styleSource);

    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }

  }

  public XPathExpression createXPathExpression() {
    
    return new XPathExpression(prefixMap, templates, parameterNames, recordTypeName, matchExpr, selectExpr);
  }
}

