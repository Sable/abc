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
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.CharsetHelper;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.RecordDelimiter;
import com.servingxml.components.flatfile.recordtype.FlatRecordType;
import com.servingxml.components.flatfile.FlatRecordWriter;
import com.servingxml.components.flatfile.RecordOutput;
import com.servingxml.components.flatfile.ByteRecordOutput;
import com.servingxml.components.flatfile.RecordOutput;

/**
 * A <code>SigningFlatFilePostprocessor</code> postproccesses a flat file.
 *
 * 
 * @author  Daniel A. Parker
 */

public class SigningFlatFilePostprocessor implements FlatFilePostprocessor {
  private ByteArrayBuilder byteArrayBuilder = new ByteArrayBuilder();
  private boolean isData = false;
  private final FlatFileHeader header;
  private final FlatFileTrailer trailer;
  private final FlatFileSignature[] signers;
  private final Delimiter recordDelimiter;
  private final StreamSink sink;
  private Charset charset = null;
  private final FlatFileOptions flatFileOptions;

  public SigningFlatFilePostprocessor(FlatFileOptions flatFileOptions, 
    FlatFileHeader header, FlatFileBody body, FlatFileTrailer trailer,
    FlatFileSignature[] signers, StreamSink sink) {
    this.header = header;
    this.trailer = trailer;
    this.flatFileOptions = flatFileOptions;
    this.signers = signers;
    this.sink = sink;

    Delimiter[] recordDelimiters = body.getRecordDelimiters();

    boolean found = false;
    for (int i = 0; !found && i < recordDelimiters.length; ++i) {
      Delimiter delimiter = recordDelimiters[i];
      if (delimiter.equals(RecordDelimiter.SYSTEM)) {
        found = true;
      }
    }
    recordDelimiter = found ? RecordDelimiter.SYSTEM : recordDelimiters[0];
  }

  public void write(RecordOutput recordOutput)
  throws IOException {
    if (isData) {
      byte[] data = recordOutput.toByteArray();
      byteArrayBuilder.append(data, 0, data.length);
    }
  }

  public void close() {
  }

  public void beginData() {
    isData = true;
  }

  public void endData() {
    isData = false;
  }

  public void signFile(ServiceContext context, Flow flow, long recordCount) {
    OutputStream os = null;
    Record parameters = flow.getParameters();
    try {
      os = new BufferedOutputStream(sink.getOutputStream());
      if(sink.getCharset() != null){
        charset = sink.getCharset();
      }
      else{
        charset = Charset.defaultCharset();
      }

      for (int j = 0; j < signers.length; ++j) {
        signers[j].data(byteArrayBuilder.buffer(),0,byteArrayBuilder.length());
      }
      startRecordStream(context, flow, os);
      os.write(byteArrayBuilder.buffer(),0,byteArrayBuilder.length());
      endRecordStream(context, flow, os);
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } finally {
      if (os != null) {
        try {
          os.flush();
          sink.close();
        } catch (Exception e) {
        }
      }
    }
  }

  private void startRecordStream(ServiceContext context, Flow flow, OutputStream os) {
    try {
      Charset charset;
      if (sink.getCharset() != null) {
        charset = sink.getCharset();
      } else {
        charset = Charset.defaultCharset();
      }
      char padChar = flatFileOptions.getPadCharacter();
      byte[] pa = CharsetHelper.charactersToBytes(new char[]{padChar}, charset);
      byte padByte = pa[0];
      RecordOutput recordOutput = new ByteRecordOutput(charset, padByte);

      FlatRecordType[] metaRecords = header.createFlatRecordTypes(context, flow, flatFileOptions);
      for (int i = 0; i < metaRecords.length; ++i) {
        recordOutput.clear();
        FlatRecordType metaRecordType = metaRecords[i];
        Record metaRecord = metaRecordType.getDefaultRecord(context, flow);
        for (int j = 0; j < signers.length; ++j) {
          metaRecord = signers[j].updateMetaRecord(context, flow.getParameters(), metaRecord);
        }
        Flow newFlow = flow.replaceRecord(context, metaRecord);
        FlatRecordWriter recordWriter = metaRecordType.createFlatRecordWriter(); 
        recordWriter.writeRecord(context, newFlow, recordOutput);
        recordDelimiter.writeEndDelimiterTo(recordOutput);
        byte[] data = recordOutput.toByteArray();
        os.write(data, 0, data.length);
      }
      os.flush();
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  private void endRecordStream(ServiceContext context, Flow flow, OutputStream os) {
    try {
      os.flush();
      FlatRecordType[] metaRecords = trailer.createFlatRecordTypes(context, flow, flatFileOptions);
      Charset charset;
      if (sink.getCharset() != null) {
        charset = sink.getCharset();
      } else {
        charset = Charset.defaultCharset();
      }
      byte[] pa= CharsetHelper.stringToBytes(" ", charset);
      byte padByte = pa[0];
      RecordOutput recordOutput = new ByteRecordOutput(charset, padByte);

      for (int i = 0; i < metaRecords.length; ++i) {
        recordOutput.clear();
        FlatRecordType metaRecordType = metaRecords[i];
        Record metaRecord = metaRecordType.getDefaultRecord(context, flow);
        for (int j = 0; j < signers.length; ++j) {
          metaRecord = signers[j].updateMetaRecord(context, flow.getParameters(), metaRecord);
        }
        Flow newFlow = flow.replaceRecord(context, metaRecord);
        FlatRecordWriter recordWriter = metaRecordType.createFlatRecordWriter(); 
        recordWriter.writeRecord(context, newFlow, recordOutput);
        recordDelimiter.writeEndDelimiterTo(recordOutput);
        byte[] data = recordOutput.toByteArray();
        os.write(data, 0, data.length);
      }
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}

