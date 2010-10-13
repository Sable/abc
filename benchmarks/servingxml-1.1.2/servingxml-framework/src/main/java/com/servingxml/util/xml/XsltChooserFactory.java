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

import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.PrefixMap;                 

/**
 * The <code>XsltChooserFactory</code> is a factory for 
 * creating an XSLT templates for evaluating tests. 
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XsltChooserFactory {
  private final TransformerFactory transformerFactory;
  private final String baseUri;
  private final Templates templates;
  private final Name[] parameterNames;
  private final PrefixMap prefixMap;

  public XsltChooserFactory(TransformerFactory transformerFactory, String baseUri,
                            String[] tests, PrefixMap prefixMap, String xsltVersion) {
    this.transformerFactory = transformerFactory;
    this.baseUri = baseUri;
    this.prefixMap = prefixMap;
    ParameterParser parameterParser = new ParameterParser(prefixMap);
    for (int i = 0; i < tests.length; ++i) {
      parameterParser.parseParameters(tests[i]);
    }
    parameterNames = parameterParser.getNames();
    String[] parameterQnames = parameterParser.getQnames();

    XsltChooseReader chooseReader = new XsltChooseReader(tests, prefixMap, parameterQnames, xsltVersion);

    try {
      Source styleSource = new SAXSource(chooseReader,new InputSource());
      this.templates = transformerFactory.newTemplates(styleSource);
      //{
      //  StringWriter writer = new StringWriter();
      //  Transformer transformer = transformerFactory.newTransformer();
      //  Result result = new StreamResult(writer);
      //  transformer.transform(styleSource,result);
      //System.out.println("XsltChooserFactory.createFlatRecordType " + writer.toString());
      //}
    } catch (TransformerException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public XsltChooser createXsltChooser() {

    return new XsltChooser(templates, parameterNames, prefixMap, baseUri);
  }
}
