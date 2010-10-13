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

public class RepeatingGroupMatchable implements Matchable {

  private final String mode;
  private final String matchExpr;
  private final String matchExpr2;
  private final Name fieldName;
  private final Name recordTypeName;
  private final Matchable[] matchables;

  public RepeatingGroupMatchable(String mode, String matchExpr, Name fieldName, 
    String matchExpr2, Name recordTypeName, Matchable[] matchables) {
   //System.out.println(getClass().getName()+".cons matchExpr=" + matchExpr);
    this.mode= mode;
    this.matchExpr = matchExpr;
    this.matchExpr2 = matchExpr2;
    this.fieldName = fieldName;
    this.recordTypeName = recordTypeName;
    this.matchables = matchables;
  }

  public String getMatchExpression() {
    return matchExpr;
  }

  public void putTemplateContent(PrefixMap prefixMap, ContentHandler contentHandler) 
  throws IOException, SAXException {
   //System.out.println(getClass().getName()+".putTemplateContent matchExpr=" + matchExpr);

    AttributesImpl templatesAtts = new AttributesImpl();
    templatesAtts.addAttribute("", "mode", "mode", "CDATA", mode);
    if (matchExpr.length() > 0) {
      templatesAtts.addAttribute("", "select", "select", "CDATA", "*");
    }

    contentHandler.startElement(SystemConstants.XSL_NS_URI,"apply-templates","xsl:apply-templates",templatesAtts);
    contentHandler.endElement(SystemConstants.XSL_NS_URI,"apply-templates","xsl:apply-templates");
  }

  public void putTemplate(PrefixMap prefixMap, ContentHandler contentHandler) 
  throws IOException, SAXException {

   //System.out.println(getClass().getName()+".putTemplate");

    String fnamespaceUri = fieldName.getNamespaceUri();
    String flocalName = fieldName.getLocalName();
    String fprefix = prefixMap.getPrefix(fnamespaceUri);
    String fqName = flocalName;
    if (fprefix.length() > 0) {
      fqName = fprefix + ":" + fqName;
    }
    if (fqName == null) {
      throw new IOException("No prefix for namespace " + fnamespaceUri);
    }

    String rnamespaceUri = recordTypeName.getNamespaceUri();
    String rlocalName = recordTypeName.getLocalName();
    String rprefix = prefixMap.getPrefix(rnamespaceUri);
    String rqName = rlocalName;
    if (rprefix.length() > 0) {
      rqName = rprefix + ":" + rqName;
    }
    if (rqName == null) {
      throw new IOException("No prefix for namespace " + rnamespaceUri);
    }

    AttributesImpl templatesAtts = new AttributesImpl();
    templatesAtts.addAttribute("", "mode", "mode", "CDATA", mode);
    if (matchExpr2.length() > 0) {
      templatesAtts.addAttribute("", "match", "match", "CDATA", matchExpr);
    }
    contentHandler.startElement(SystemConstants.XSL_NS_URI,"template","xsl:template",templatesAtts);
    contentHandler.startElement(fnamespaceUri,flocalName,fqName,SystemConstants.EMPTY_ATTRIBUTES);
    contentHandler.startElement(rnamespaceUri,rlocalName,rqName,SystemConstants.EMPTY_ATTRIBUTES);
    for (int i = 0; i < matchables.length; ++i) {
      matchables[i].putTemplateContent(prefixMap, contentHandler);
    }
    contentHandler.endElement(rnamespaceUri,rlocalName,rqName);
    contentHandler.endElement(fnamespaceUri,flocalName,fqName);
    contentHandler.endElement(SystemConstants.XSL_NS_URI,"template","xsl:template");
    for (int i = 0; i < matchables.length; ++i) {
      matchables[i].putTemplate(prefixMap, contentHandler);
    }
  }

  public void putParameters(ParameterParser paramParser) {
    for (int i = 0; i < matchables.length; ++i) {
      matchables[i].putParameters(paramParser);
    }
  }
}
