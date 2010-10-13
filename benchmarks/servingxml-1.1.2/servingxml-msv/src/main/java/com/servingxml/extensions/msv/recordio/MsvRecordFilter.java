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

package com.servingxml.extensions.msv.recordio;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFilter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.AbstractRecordFilter;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.sun.msv.verifier.ValidityViolation;

public class MsvRecordFilter extends AbstractRecordFilter implements ErrorHandler {

  private final PrefixMap prefixMap;
  private final Schema schema;
  private Verifier verifier;
  private VerifierFilter filter;

  public MsvRecordFilter(PrefixMap prefixMap, Schema schema) {
    this.prefixMap = prefixMap;
    this.schema = schema;
    try {
      verifier = schema.newVerifier();
      filter = verifier.getVerifierFilter();
      filter.setErrorHandler(this);
    } catch (VerifierConfigurationException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (SAXException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void writeRecord(ServiceContext context, Flow flow) {

    //System.out.println(getClass().getName()+".writeRecord enter");
    Record record = flow.getRecord();
    boolean validated = false;
    try {
      XMLReader xmlReader = record.createXmlReader(prefixMap);
      filter.setParent(xmlReader);
      filter.parse("");
      validated = true;
    } catch (Exception e) {
      String message = MsvHelper.makeMessage(e);
      discardRecord(context,flow,new ServingXmlException(message));
    }
    if (validated) {
      //  ServingXmlException must be propogated up if this fails
      getRecordWriter().writeRecord(context, flow);
    }
    //System.out.println(getClass().getName()+".writeRecord leave");
  }

  public void warning (SAXParseException exception)
  throws SAXException {

    throw exception;
  }

  public void error(SAXParseException exception)
  throws SAXException {
    throw exception;
  }

  public void fatalError(SAXParseException exception)
  throws SAXException {
    throw exception;
  }
}
