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

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Templates;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.InputSource;

import com.servingxml.util.Name;
import com.servingxml.util.QnameContext;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.RecordContentHandler;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.util.PrefixMap;

/**
 *                      
 *                                    
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XsltEvaluatorFactoryImpl implements XsltEvaluatorFactory {

  private final List<Matchable> matchableList;
  private final Name recordTypeName;

  public XsltEvaluatorFactoryImpl() {

    this.recordTypeName = new QualifiedName("record");
    this.matchableList = new ArrayList<Matchable>();
  }

  public void addMatchable(Matchable matchable) {
    //System.out.println(getClass().getName()+".addMatchable " + matchable.getMatchExpression());
    matchableList.add(matchable);
  }

  public XsltEvaluator createXsltEvaluator(QnameContext context, XsltConfiguration xsltConfiguration) {
    XsltEvaluator recordTemplates;

    ParameterParser paramParser = new ParameterParser(context.getPrefixMap());
    Matchable[] matchables = new Matchable[matchableList.size()];
    matchables= matchableList.toArray(matchables);
    if (matchables.length > 0) {
      for (int i = 0; i < matchables.length; ++i) {
        Matchable matchable = matchables[i];
        matchable.putParameters(paramParser);
      }
      Name[] parameterNames = paramParser.getNames();
      String[] parameterQnames = paramParser.getQnames();

      XmlRecordTransformReader transformReader = new XmlRecordTransformReader(recordTypeName,
                                                   "/*", matchables,context.getPrefixMap(),
        xsltConfiguration.getVersion(), parameterQnames);
      Source styleSource = new SAXSource(transformReader,new InputSource());

      try {
        TransformerFactory transformerFactory = xsltConfiguration.getTransformerFactory();

        //StringWriter writer = new StringWriter();
        //Result r = new StreamResult(writer);
        //Transformer t = transformerFactory.newTransformer();
        //t.transform(styleSource,r);
        //System.out.println(writer.toString());

        Templates templates = transformerFactory.newTemplates(styleSource);
        recordTemplates = new XsltEvaluatorImpl(context.getPrefixMap(),recordTypeName, templates, parameterNames);
      } catch (TransformerFactoryConfigurationError e) {
        throw new ServingXmlException(e.getMessage(),e);
      } catch (TransformerException e) {
        throw new ServingXmlException(e.getMessage(),e);
      }
    } else {
      recordTemplates = new EmptyXsltEvaluator(recordTypeName);
    }
    return recordTemplates;
  }
}

