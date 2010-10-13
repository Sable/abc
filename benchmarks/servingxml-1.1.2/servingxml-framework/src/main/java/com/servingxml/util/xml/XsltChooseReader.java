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

import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.util.SystemConstants;
import com.servingxml.util.PrefixMap;

/**
 * A <code>XsltChooseReader</code> implement an <code>XMLReader</code> interface
 *
 * 
 * @author  Daniel A. Parker
 */

public class XsltChooseReader extends AbstractXmlReader {

  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final PrefixMap prefixMap;
  private final String[] tests;
  private final String[] parameterQnames;
  private final String xsltVersion;

  public XsltChooseReader(String[] tests, PrefixMap prefixMap, String[] parameterQnames, String xsltVersion) {
    this.prefixMap = prefixMap;
    this.tests = tests; 
    this.parameterQnames = parameterQnames;
    this.xsltVersion = xsltVersion;
  }

  public void parse(String systemId)
  throws IOException, SAXException {
    //System.out.println(getClass().getName()+".parse enter");

    contentHandler.startDocument();
    contentHandler.startPrefixMapping(SystemConstants.XSL_NS_PREFIX,SystemConstants.XSL_NS_URI);
    contentHandler.startPrefixMapping(SystemConstants.SERVINGXML_NS_PREFIX,SystemConstants.SERVINGXML_NS_URI);

    PrefixMap.PrefixMapping[] prefixDeclarations = prefixMap.getPrefixDeclarations();
    for (int i = 0; i < prefixDeclarations.length; ++i) {
      PrefixMap.PrefixMapping mapping = prefixDeclarations[i];
      String prefix = mapping.getPrefix();
      String namespaceUri = mapping.getNamespaceUri();                  
      contentHandler.startPrefixMapping(prefix,namespaceUri);
    }

    AttributesImpl versionAtts = new AttributesImpl();
    versionAtts.addAttribute("","version", "version","CDATA",xsltVersion);

    contentHandler.startElement(SystemConstants.XSL_NS_URI,"transform","xsl:transform",versionAtts);

    for (int i = 0; i < parameterQnames.length; ++i) {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("","name", "name","CDATA",parameterQnames[i]);
      contentHandler.startElement(SystemConstants.XSL_NS_URI,"param","xsl:param",attributes);
      contentHandler.endElement(SystemConstants.XSL_NS_URI,"param","xsl:param");
    }


    AttributesImpl templateAttributes = new AttributesImpl();
    templateAttributes.addAttribute("","match", "match","CDATA","/*");
    contentHandler.startElement(SystemConstants.XSL_NS_URI,"template","xsl:template",templateAttributes);
    contentHandler.startElement(SystemConstants.SERVINGXML_NS_URI,"result","sx:result",EMPTY_ATTRIBUTES);
    contentHandler.startElement(SystemConstants.XSL_NS_URI,"choose","xsl:choose",EMPTY_ATTRIBUTES);

    boolean done = false;
    for (int i = 0; !done && i <tests.length; ++i) {

      String testExpr = tests[i];
      if (testExpr.length() > 0) {
        AttributesImpl whenAttributes = new AttributesImpl();
        whenAttributes.addAttribute("","test", "test","CDATA",testExpr);
        contentHandler.startElement(SystemConstants.XSL_NS_URI,"when","xsl:when",whenAttributes);
        AttributesImpl nameAttributes = new AttributesImpl();
        nameAttributes.addAttribute("","name", "name","CDATA","index");
        String index = Integer.toString(i);
        contentHandler.startElement(SystemConstants.XSL_NS_URI,"attribute","xsl:attribute",nameAttributes);
        contentHandler.characters(index.toCharArray(),0,index.length());
        contentHandler.endElement(SystemConstants.XSL_NS_URI,"attribute","xsl:attribute");
        contentHandler.endElement(SystemConstants.XSL_NS_URI,"when","xsl:when");
      } else {
        contentHandler.startElement(SystemConstants.XSL_NS_URI,"otherwise","xsl:otherwise",EMPTY_ATTRIBUTES);
        AttributesImpl nameAttributes = new AttributesImpl();
        nameAttributes.addAttribute("","name", "name","CDATA","index");
        String index = Integer.toString(i);
        contentHandler.startElement(SystemConstants.XSL_NS_URI,"attribute","xsl:attribute",nameAttributes);
        contentHandler.characters(index.toCharArray(),0,index.length());
        contentHandler.endElement(SystemConstants.XSL_NS_URI,"attribute","xsl:attribute");
        contentHandler.endElement(SystemConstants.XSL_NS_URI,"otherwise","xsl:otherwise");
        done = true;
      }
    }

    contentHandler.endElement(SystemConstants.XSL_NS_URI,"choose","xsl:choose");
    contentHandler.endElement(SystemConstants.SERVINGXML_NS_URI,"result","sx:result");
    contentHandler.endElement(SystemConstants.XSL_NS_URI,"template","xsl:template");
    contentHandler.endElement(SystemConstants.XSL_NS_URI,"transform","xsl:transform");

    contentHandler.endDocument();

    //System.out.println(getClass().getName()+".parse leave");
  }
}

