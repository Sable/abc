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

package com.servingxml.components.flatfile.recordtype;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

import com.servingxml.util.ServingXmlException;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.util.Name;
import com.servingxml.app.Flow;
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.xml.XsltChooser;
import com.servingxml.components.flatfile.FlatRecordWriter;
import com.servingxml.components.flatfile.RecordOutput;
import com.servingxml.util.PrefixMap;

/**
 * The <code>FlatRecordWriter</code> object writes a header or trailer line
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatRecordWriterChoice implements FlatRecordWriter {
  private final PrefixMap prefixMap;
  private final XsltChooser chooser;
  private final FlatRecordWriter[] recordWriterOptions;

  public FlatRecordWriterChoice(PrefixMap prefixMap, 
                                FlatRecordWriter[] recordWriterOptions, 
                                XsltChooser chooser) {
    this.prefixMap = prefixMap;
    this.recordWriterOptions = recordWriterOptions;
    this.chooser = chooser;
  }

  public void writeRecord(ServiceContext context, Flow flow, RecordOutput recordOutput) {
    FlatRecordWriter flatRecordWriter = resolveFlatRecordWriter(context, flow);
    if (flatRecordWriter != null) {
      flatRecordWriter.writeRecord(context, flow, recordOutput);
    }
  }

  public FlatRecordWriter resolveFlatRecordWriter(ServiceContext context, Flow flow) {
    Record record = flow.getRecord();

    Source source = new SAXSource(record.createXmlReader(prefixMap),new InputSource());
    int index = chooser.choose(source,flow.getParameters());

    FlatRecordWriter flatRecordWriter = null;

    if (index >= 0 && index < recordWriterOptions.length) {
      flatRecordWriter = recordWriterOptions[index];
    }
    return flatRecordWriter;
  }
}
