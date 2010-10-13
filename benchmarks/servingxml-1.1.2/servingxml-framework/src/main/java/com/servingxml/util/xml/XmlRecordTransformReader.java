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
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import com.servingxml.util.Name;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.PrefixMap;

/**
 * A <code>XmlRecordTransformReader</code> implement a SAX 2 <code>XMLReader</code> interface
 *
 * 
 * @author  Daniel A. Parker
 */

public class XmlRecordTransformReader extends AbstractXmlReader 
implements XMLReader {

  private final Name recordTypeName;
  private final PrefixMap prefixMap;
  private final String matchExpr;
  private final Matchable[] matchables;
  private final String[] parameterQnames;
  private final String xsltVersion;

  public XmlRecordTransformReader(Name recordTypeName, String matchExpr, 
    Matchable[] matchables, PrefixMap prefixMap, String xsltVersion,
    String[] parameterQnames) {
    this.prefixMap = prefixMap;
    this.recordTypeName = recordTypeName;
    this.matchExpr = matchExpr;
    this.matchables = matchables; 
    this.xsltVersion = xsltVersion;
    this.parameterQnames = parameterQnames;
  }

  public void parse(String systemId)
  throws IOException, SAXException {

    contentHandler.startDocument();
    contentHandler.startPrefixMapping(SystemConstants.XSL_NS_PREFIX,SystemConstants.XSL_NS_URI);
    contentHandler.startPrefixMapping(SystemConstants.SERVINGXML_NS_PREFIX,SystemConstants.SERVINGXML_NS_URI);

    PrefixMap.PrefixMapping[] prefixDeclarations = prefixMap.getPrefixDeclarations();
    for (int i = 0; i < prefixDeclarations.length; ++i) {
      PrefixMap.PrefixMapping mapping = prefixDeclarations[i];
      String prefix = mapping.getPrefix();
      //  Don't want default prefix mapping for records, if any
      if (prefix.length() > 0) {
        String namespaceUri = mapping.getNamespaceUri();                  
        contentHandler.startPrefixMapping(prefix,namespaceUri);
      }
    }

    AttributesImpl versionAtts = new AttributesImpl();
    versionAtts.addAttribute("","version", "version","CDATA",xsltVersion);

    contentHandler.startElement(SystemConstants.XSL_NS_URI,"transform","xsl:transform",versionAtts);

    for (int i = 0; i < parameterQnames.length; ++i) {
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("","name", "name","CDATA", parameterQnames[i]);
      contentHandler.startElement(SystemConstants.XSL_NS_URI,"param","xsl:param",attributes);
      contentHandler.endElement(SystemConstants.XSL_NS_URI,"param","xsl:param");
    }

    AttributesImpl fragmentMatchAttributes = new AttributesImpl();
    fragmentMatchAttributes.addAttribute("","match", "match","CDATA","/*");

    if (!matchExpr.equals("/*")) {
      contentHandler.startElement(SystemConstants.XSL_NS_URI,"template","xsl:template",fragmentMatchAttributes);
      contentHandler.startElement(SystemConstants.SERVINGXML_NS_URI,"records","sx:records",SystemConstants.EMPTY_ATTRIBUTES);
      AttributesImpl applyTemplatesAtts = new AttributesImpl();
      contentHandler.startElement(SystemConstants.XSL_NS_URI,"apply-templates","xsl:apply-templates",applyTemplatesAtts);
      contentHandler.endElement(SystemConstants.XSL_NS_URI,"apply-templates","xsl:apply-templates");
      contentHandler.endElement(SystemConstants.SERVINGXML_NS_URI,"records","sx:records");
      contentHandler.endElement(SystemConstants.XSL_NS_URI,"template","xsl:template");
      AttributesImpl recordMatchAttributes = new AttributesImpl();
      recordMatchAttributes.addAttribute("","match", "match","CDATA",matchExpr);
      recordMatchAttributes.addAttribute("","priority", "priority","CDATA","1");
      processRecord(recordMatchAttributes);
    } else {
      processRecord(fragmentMatchAttributes);
    }

    for (int i = 0; i < matchables.length; ++i) {
      Matchable matchable = matchables[i];   
      matchable.putTemplate(prefixMap, contentHandler);
    }

    contentHandler.endElement(SystemConstants.XSL_NS_URI,"transform","xsl:transform");

    contentHandler.endDocument();
  }

  private void processRecord(Attributes recordMatchAttributes)
  throws IOException, SAXException {

    contentHandler.startElement(SystemConstants.XSL_NS_URI,"template","xsl:template",recordMatchAttributes);

    String prefix = recordTypeName.getNamespaceUri().length() == 0 ? "" : 
      prefixMap.getPrefix(recordTypeName.getNamespaceUri());
    String recordQname = recordTypeName.getLocalName();
    if (prefix.length() > 0) {
      recordQname = prefix + ":" + recordQname;
    }

    contentHandler.startElement(recordTypeName.getNamespaceUri(),recordTypeName.getLocalName(),recordQname,SystemConstants.EMPTY_ATTRIBUTES);

    for (int i = 0; i < matchables.length; ++i) {
      Matchable matchable = matchables[i];
      matchable.putTemplateContent(prefixMap,contentHandler);
    }

    contentHandler.endElement(recordTypeName.getNamespaceUri(),recordTypeName.getLocalName(),recordQname);

    contentHandler.endElement(SystemConstants.XSL_NS_URI,"template","xsl:template");
  }
}

