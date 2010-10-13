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

import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;
import com.servingxml.util.record.Record;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordWriter;
import com.servingxml.util.xml.XsltChooser;
import com.servingxml.app.Environment;

/**
 * The <code>FlatRecordTypeChoice</code> implements a <code>FlatRecordTypeChoice</code>.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatRecordTypeChoice implements FlatRecordType {
  private final Environment env;
  private final FlatRecordType defaultRecordType;
  private final FlatRecordTypeSelection[] flatRecordTypeSelections;
  private final XsltChooser chooser;

  public FlatRecordTypeChoice(Environment env,
                              FlatRecordType defaultRecordType, 
                              FlatRecordTypeSelection[] flatRecordTypeSelections, 
                              XsltChooser chooser) {
    this.env = env;
    this.defaultRecordType = defaultRecordType;
    this.flatRecordTypeSelections = flatRecordTypeSelections;
    this.chooser = chooser;
  }

  public FlatRecordReader createFlatRecordReader() {

    FlatRecordReader[] recordReaderOptions = new FlatRecordReader[flatRecordTypeSelections.length];
    for (int i = 0; i < recordReaderOptions.length; ++i) {
      recordReaderOptions[i] = flatRecordTypeSelections[i].getFlatRecordType().createFlatRecordReader();
    }

    FlatRecordReader defaultRecordReader = defaultRecordType.createFlatRecordReader();
    FlatRecordReader flatRecordReader = new FlatRecordReaderChoice(env.getQnameContext().getPrefixMap(), 
                                                                   defaultRecordReader, 
                                                                   recordReaderOptions, chooser);
    return flatRecordReader;
  }

  public FlatRecordWriter createFlatRecordWriter() {

    FlatRecordWriter[] recordWriterOptions = new FlatRecordWriter[flatRecordTypeSelections.length];
    for (int i = 0; i < recordWriterOptions.length; ++i) {
      recordWriterOptions[i] = flatRecordTypeSelections[i].getFlatRecordType().createFlatRecordWriter();
    }

    FlatRecordWriter flatRecordWriter = new FlatRecordWriterChoice(env.getQnameContext().getPrefixMap(),
                                                                   recordWriterOptions, chooser);
    return flatRecordWriter;
  }

  public Record getDefaultRecord(ServiceContext context, Flow flow) {
    return Record.EMPTY;
  }

  public boolean isText() {
    boolean result = defaultRecordType.isText();
    for (int i = 0; result && i < flatRecordTypeSelections.length; ++i) {
      if (!flatRecordTypeSelections[i].getFlatRecordType().isText()) {
        result = false;
      }
    }

    return result;
  }

  public boolean isBinary() {
    boolean result = defaultRecordType.isBinary();
    for (int i = 0; result && i < flatRecordTypeSelections.length; ++i) {
      if (!flatRecordTypeSelections[i].getFlatRecordType().isBinary()) {
        result = false;
      }
    }

    return result;
  }

  public boolean isFixedLength() {
    boolean result = true;
    for (int i = 0; result && i < flatRecordTypeSelections.length; ++i) {
      if (!flatRecordTypeSelections[i].getFlatRecordType().isFixedLength()) {
        result = false;
      }
    }

    return result;
  }
}
