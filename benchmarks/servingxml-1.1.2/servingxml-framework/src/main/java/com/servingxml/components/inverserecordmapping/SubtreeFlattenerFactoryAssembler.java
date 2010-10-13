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

package com.servingxml.components.inverserecordmapping;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.expr.ExpressionException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.Matchable;
import com.servingxml.util.xml.XmlRecordTransformReader;
import com.servingxml.util.xml.ParameterParser;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.xml.Matchable;
import com.servingxml.util.xml.XmlRecordTransformReader;
import com.servingxml.util.PrefixMap;                 

//  Test
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.URIResolver; 
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import org.xml.sax.InputSource;


/**
 * An assembler class for creating <tt>SubtreeFlattener</tt> instances.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SubtreeFlattenerFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private String matchExpr = "/*";
  private Name recordTypeName = Name.EMPTY;
  private SubtreeFieldMap[] subtreeFieldMaps = new SubtreeFieldMap[0];
  private XsltConfiguration xsltConfiguration;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void setMatch(String matchExpr) {
    this.matchExpr = matchExpr;
  }

  public void setRecordType(Name recordTypeName) {
    this.recordTypeName = recordTypeName;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void injectComponent(SubtreeFieldMap[] subtreeFieldMaps) {
    this.subtreeFieldMaps = subtreeFieldMaps;
  }

  public ShredXmlFactory assemble(ConfigurationContext context) {

    try {
      if (xsltConfiguration == null) {
        xsltConfiguration = XsltConfiguration.getDefault();
      }

      //System.out.println(getClass().getName()+".assemble");

      if (recordTypeName.isEmpty()) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                           context.getElement().getTagName(),"recordType");
        throw new ServingXmlException(message);
      }
      PrefixMap prefixMap = context.getQnameContext().getPrefixMap();
      ParameterParser paramParser = new ParameterParser(prefixMap);

      for (int i = 0; i < subtreeFieldMaps.length; ++i) {
        subtreeFieldMaps[i].putParameters(paramParser);
      }
      Name[] parameterNames = paramParser.getNames();
      String[] parameterQnames = paramParser.getQnames();


      Matchable[] matchables = new Matchable[subtreeFieldMaps.length];
      for (int i = 0; i < subtreeFieldMaps.length; ++i) {
        SubtreeFieldMap fieldMap = subtreeFieldMaps[i];
       //System.out.println(getClass().getName()+".assemble fieldMap is " + fieldMap.getClass().getName());
        String mode = "field."+i;
        Matchable matchable = fieldMap.createMatchable(mode);
       //System.out.println(getClass().getName()+".assemble matchable is " + matchable.getClass().getName());
        matchables[i] = matchable;
      }
      XmlRecordTransformReader reader = new XmlRecordTransformReader(recordTypeName, 
        matchExpr, matchables, prefixMap, xsltConfiguration.getVersion(), parameterQnames);
      TransformerFactory transformerFactory = xsltConfiguration.getTransformerFactory();
      Source styleSource = new SAXSource(reader,new InputSource());
      Templates templates = transformerFactory.newTemplates(styleSource);

      ShredXmlFactory subtreeRecordMap = new SubtreeFlattenerFactory(templates, matchExpr, recordTypeName,
                                               subtreeFieldMaps, parameterNames);
      if (parameterDescriptors.length > 0) {
        subtreeRecordMap = new ShredXmlFactoryPrefilter(subtreeRecordMap,parameterDescriptors);
      }
      return subtreeRecordMap;
    } catch (TransformerException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}


