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

package com.servingxml.extensions.msv.xmlpipeline.saxfilter;

import java.io.IOException;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

import org.xml.sax.XMLFilter;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierFilter;
import org.iso_relax.verifier.VerifierConfigurationException;

import com.sun.msv.verifier.ValidityViolation;

import com.servingxml.util.Name;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlFault;
import com.servingxml.util.ServingXmlFaultCodes;
import com.servingxml.util.ServingXmlFaultDetail;
import com.servingxml.util.record.Record;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.extensions.msv.recordio.MsvHelper;

public class MsvXmlFilter extends XMLFilterImpl implements XMLFilter {

  private final Schema schema;
  private Verifier verifier;
  private VerifierFilter filter;
  private final ErrorHandler errorHandler;

  public MsvXmlFilter(Schema schema, ErrorHandler errorHandler) {
    this.schema = schema;
    this.errorHandler = errorHandler;
    try {
      verifier = schema.newVerifier();
      filter = verifier.getVerifierFilter();
      //filter.setParent(this);
      filter.setErrorHandler(errorHandler);
    } catch (VerifierConfigurationException e) {
      throw new ServingXmlException(MsvHelper.makeMessage(e),e);
    } catch (SAXException e) {
      throw new ServingXmlException(MsvHelper.makeMessage(e),e);
    }
  }

  public void setContentHandler(ContentHandler handler) {
    //super.setContentHandler(handler);
    filter.setContentHandler(handler);
  }

  public void endDocument() throws SAXException {
  }

  public void setParent(XMLReader parent) {
    filter.setParent(parent);
  }

  public void parse(InputSource input) throws SAXException, IOException {
    //System.out.println(getClass().getName()+".parse enter");
    parse(input.getSystemId());
    //System.out.println(getClass().getName()+".parse leave");
  }

  public void parse(String systemId) throws SAXException, IOException {
    filter.parse(systemId);
  }
}
