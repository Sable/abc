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

package com.servingxml.components.flatfile.layout;

import java.io.IOException;
import java.nio.charset.Charset;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.ByteRecordOutput;
import com.servingxml.components.flatfile.CharRecordOutput;
import com.servingxml.components.flatfile.FlatRecordWriter;
import com.servingxml.components.flatfile.RecordOutput;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.RecordDelimiter;
import com.servingxml.components.flatfile.recordtype.FlatRecordType;
import com.servingxml.components.recordio.AbstractRecordWriter;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.CharsetHelper;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.ParameterBuilder;
import com.servingxml.util.record.Record;

/**
 *
 * 
 * @author  Daniel A. Parker
 */                                             

public class FlatFileWriter extends AbstractRecordWriter implements RecordWriter {

  private final Delimiter recordDelimiter;
  private final FlatRecordType flatRecordType;
  private final StreamSinkFactory sinkFactory;
  private FlatFilePostprocessor postprocessor = null;  
  private long recordCount = 0L;
  private FlatRecordWriter bodyFlatRecordWriter = null;
  private final Charset charset;
  private RecordOutput recordOutput = null;
  private final FlatFileOptions flatFileOptions;
  private final FlatFileHeader header;
  private final FlatFileBody body;
  private final FlatFileTrailer trailer;
  private final FlatFileSignatureFactory[] signatureFactories;

  public FlatFileWriter(FlatFileOptions flatFileOptions, 
    FlatFileHeader header, FlatFileBody body, FlatFileTrailer trailer, 
    FlatFileSignatureFactory[] signatureFactories,
    StreamSinkFactory sinkFactory) {

    this.header = header;
    this.body = body;
    this.trailer = trailer;
    this.flatRecordType = body.getFlatRecordType();
    this.flatFileOptions = flatFileOptions;
    this.signatureFactories = signatureFactories;
    this.charset = flatFileOptions.getDefaultCharset();
    this.sinkFactory = sinkFactory;

    //System.out.println(getClass().getName()+".cons Checking for system delimiter " + recordDelimiters.length);
    Delimiter[] recordDelimiters = flatFileOptions.getRecordDelimiters();
    //System.out.println(getClass().getName()+".cons recordDelimiters len=" + recordDelimiters.length);
    int index = -1;
    boolean found = false;
    if (recordDelimiters.length > 0) {
      String lineSep = System.getProperty("line.separator");
      if (lineSep != null) {
        for (int i = recordDelimiters.length-1; !found && i >= 0; --i) {
          Delimiter delimiter = recordDelimiters[i];
          if (delimiter.forWriting()) {
            index = i;
            if (delimiter.equalsString(lineSep)) {
              //System.out.println(getClass().getName()+".cons system delimiter found" );
              found = true;
            }
          }
        }
      }
      recordDelimiter = recordDelimiters[index];
    } else {
      recordDelimiter = RecordDelimiter.NULL;
    }
    //System.out.println(getClass().getName()+".cons record delimiter index = " + index);
  }

  public FlatFilePostprocessor createFlatFilePostprocessor(StreamSink sink) {
    FlatFilePostprocessor postprocessor;
    if (signatureFactories.length > 0) {
      FlatFileSignature[] signers = createSigners();
      postprocessor = new SigningFlatFilePostprocessor(flatFileOptions, header, body, 
        trailer, signers, sink);
    } else {
      if (flatFileOptions.isCountPositionsInBytes()) {
        postprocessor = new ByteFlatFilePostprocessor(sink, flatFileOptions.isFlushRecordOnWrite());
      } else {
        postprocessor = new CharFlatFilePostprocessor(sink, flatFileOptions.isFlushRecordOnWrite());
      }
    }
    return postprocessor;
  }

  private FlatFileSignature[] createSigners() {
    FlatFileSignature[] signers = new FlatFileSignature[signatureFactories.length];
    for (int i = 0; i < signatureFactories.length; ++i) {
      signers[i] = signatureFactories[i].createFlatFileSignature();
    }
    return signers;
  }

  public void startRecordStream(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".startRecordStream before ");
    try {
      StreamSink sink = sinkFactory.createStreamSink(context, flow);
      char padChar = flatFileOptions.getPadCharacter();

      if (flatFileOptions.isCountPositionsInBytes()) {
        byte[] pa = CharsetHelper.charactersToBytes(new char[]{padChar}, charset);
        byte padByte = pa[0];
        this.recordOutput = new ByteRecordOutput(charset, padByte);
      } else {
        this.recordOutput = new CharRecordOutput(charset, padChar);
      }

      this.postprocessor = createFlatFilePostprocessor(sink);
      bodyFlatRecordWriter = flatRecordType.createFlatRecordWriter();

      FlatRecordType[] metaRecords = header.createFlatRecordTypes(context, flow, flatFileOptions);
      for (int i = 0; i < metaRecords.length; ++i) {
        recordOutput.clear();
        FlatRecordType metaRecordType = metaRecords[i];
        Record metaRecord = metaRecordType.getDefaultRecord(context, flow);
        Flow newFlow = flow.replaceRecord(context, metaRecord);
        FlatRecordWriter recordWriter = metaRecordType.createFlatRecordWriter(); 
        recordWriter.writeRecord(context, newFlow, recordOutput);
        if (flatFileOptions.isLineDelimited()) {
          //System.out.println(getClass().getName()+".startRecordStream writing delimiter " + recordDelimiter);
          recordDelimiter.writeEndDelimiterTo(recordOutput);
        }
        postprocessor.write(recordOutput);
      }
      postprocessor.beginData();
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    //System.out.println(getClass().getName()+".startRecordStream after ");
  }

  public void endRecordStream(ServiceContext context, Flow flow) {
    try {
      postprocessor.endData();

      ParameterBuilder paramBuilder = new ParameterBuilder(flow.getParameters());
      paramBuilder.setLong(SystemConstants.RECORD_COUNT_NAME, recordCount);
      Record newParameters = paramBuilder.toRecord();
      flow = flow.replaceParameters(context, newParameters);

      FlatRecordType[] metaRecords = trailer.createFlatRecordTypes(context, flow, flatFileOptions);

      for (int i = 0; i < metaRecords.length; ++i) {
        recordOutput.clear();
        FlatRecordType metaRecordType = metaRecords[i];
        Record metaRecord = metaRecordType.getDefaultRecord(context, flow);
        Flow newFlow = flow.replaceRecord(context, metaRecord);
        FlatRecordWriter recordWriter = metaRecordType.createFlatRecordWriter(); 
        recordWriter.writeRecord(context, newFlow, recordOutput);
        if (flatFileOptions.isLineDelimited()) {
          //System.out.println(getClass().getName()+".endRecordStream writing delimiter " + recordDelimiter);
          recordDelimiter.writeEndDelimiterTo(recordOutput);
        }
        postprocessor.write(recordOutput);
      }
      postprocessor.signFile(context, flow, recordCount);
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    try {
      close();
    } catch (Exception t) {
      //  Don't care                                       
    }
  }

  public void close() {
    if (postprocessor != null) {
      postprocessor.close();
      postprocessor = null;
    }
  }

  public void writeRecord(ServiceContext context, Flow flow) {
    //System.out.println(getClass().getName()+".writeRecord before ");
    try {
      recordOutput.clear();
      FlatRecordWriter flatRecordWriter = bodyFlatRecordWriter.resolveFlatRecordWriter(context,flow);
      if (flatRecordWriter != null) {
        flatRecordWriter.writeRecord(context, flow, recordOutput);
        if (flatFileOptions.isLineDelimited()) {
          //System.out.println(getClass().getName()+".writeRecord writing delimiter!!! " + recordDelimiter);
          recordDelimiter.writeEndDelimiterTo(recordOutput);
        }
        postprocessor.write(recordOutput);
        ++recordCount;
      }
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      ServingXmlException sxe = new ServingXmlException(e.getMessage(), e);
      throw sxe;
    }
    //System.out.println(getClass().getName()+".writeRecord after ");
  }
}

