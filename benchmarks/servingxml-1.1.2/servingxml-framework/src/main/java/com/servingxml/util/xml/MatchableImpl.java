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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.util.SystemConstants;
import com.servingxml.util.Name;
import com.servingxml.util.PrefixMap;

/**
 * Implements an interface for a map from an XML fragment to a field.
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MatchableImpl implements Matchable {

  private final String mode;
  private final String matchExpr;
  private final Name name;
  private final String selectExpr;

  public MatchableImpl(String mode, String matchExpr, Name name, String selectExpr) {
    this.mode = mode;
    this.matchExpr = matchExpr;
    this.name = name;
    this.selectExpr = selectExpr;
  }

  public MatchableImpl(String mode, Name name, String selectExpr) {
    this.mode = mode;
    this.matchExpr = "/*";
    this.name = name;
    this.selectExpr = selectExpr;
  }

  public String getMatchExpression() {
    return matchExpr;
  }

  public void putTemplateContent(PrefixMap prefixMap, ContentHandler contentHandler) 
  throws IOException, SAXException {
    if (matchExpr.length() > 0 && !matchExpr.equals("/*")) {
      AttributesImpl applyTemplatesAtts = new AttributesImpl();
      applyTemplatesAtts.addAttribute("","mode", "mode","CDATA",mode);
      contentHandler.startElement(SystemConstants.XSL_NS_URI,"apply-templates","xsl:apply-templates",applyTemplatesAtts);
      contentHandler.endElement(SystemConstants.XSL_NS_URI,"apply-templates","xsl:apply-templates");
    } else {
      putTemplateContent2(prefixMap,contentHandler);
    }
  }

  private void putTemplateContent2(PrefixMap prefixMap, ContentHandler contentHandler) 
  throws IOException, SAXException {
    AttributesImpl valueOfAtts = new AttributesImpl();
    valueOfAtts.addAttribute("", "select", "select", "CDATA", selectExpr);

    String namespaceUri = name.getNamespaceUri();
    String localName = name.getLocalName();
    String prefix = prefixMap.getPrefix(namespaceUri);
    String qname = localName;
    if (prefix.length() > 0) {
      qname = prefix + ":" + qname;
    }
    if (qname == null) {
      throw new IOException("No prefix for namespace " + namespaceUri);
    }

    contentHandler.startElement(namespaceUri,localName,qname,SystemConstants.EMPTY_ATTRIBUTES);
    contentHandler.startElement(SystemConstants.XSL_NS_URI,"value-of","xsl:value-of",valueOfAtts);
    contentHandler.endElement(SystemConstants.XSL_NS_URI,"value-of","xsl:value-of");

    contentHandler.endElement(namespaceUri,localName,qname);
  }

  public void putTemplate(PrefixMap prefixMap, ContentHandler contentHandler) 
  throws IOException, SAXException {
    if (matchExpr.length() > 0 && !matchExpr.equals("/*")) {
      AttributesImpl fieldMatchAttributes = new AttributesImpl();
      fieldMatchAttributes.addAttribute("","match", "match","CDATA",matchExpr);
      fieldMatchAttributes.addAttribute("","mode", "mode","CDATA",mode);
      contentHandler.startElement(SystemConstants.XSL_NS_URI,"template","xsl:template",fieldMatchAttributes);
      putTemplateContent2(prefixMap, contentHandler);
      contentHandler.endElement(SystemConstants.XSL_NS_URI,"template","xsl:template");
    }
  }

  public void putParameters(ParameterParser paramParser) {
    paramParser.parseParameters(selectExpr);
  }
}
