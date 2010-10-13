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

import com.servingxml.app.Environment;
import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.AbstractRecordFilter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.sun.msv.verifier.ErrorInfo;
import com.sun.msv.verifier.ValidityViolation;

public class RecordValidator extends AbstractRecordFilter {

  private final Environment env;
  private final Schema schema;
  private Verifier verifier;
  private VerifierFilter filter;

  public RecordValidator(Environment env, Schema schema, ErrorHandler errorHandler) {
    this.env = env;
    this.schema = schema;
    try {
      verifier = schema.newVerifier();
      filter = verifier.getVerifierFilter();
      filter.setErrorHandler(errorHandler);
    } catch (VerifierConfigurationException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (SAXException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void writeRecord(ServiceContext context, Flow flow) {

    //System.out.println(getClass().getName()+".writeRecord " + getRecordWriter().getClass().getName());
    Record record = flow.getRecord();
    try {
      XMLReader xmlReader = record.createXmlReader(env.getQnameContext().getPrefixMap());
      filter.setParent(xmlReader);
      filter.parse("");
      getRecordWriter().writeRecord(context, flow);
    } catch (Exception e) {
      ServingXmlException reason = new ServingXmlException(e.getMessage(), e);
      super.discardRecord(context, flow, reason);
    }
    //System.out.println(getClass().getName()+".writeRecord leave");
  }
}
