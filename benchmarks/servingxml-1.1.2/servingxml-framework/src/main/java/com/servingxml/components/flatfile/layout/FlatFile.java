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

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.RecordWriter;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.components.flatfile.options.CommentStarter;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsFactory;
import com.servingxml.components.flatfile.scanner.bytes.FixedLineLengthByteScanner;
import com.servingxml.components.flatfile.scanner.characters.FixedLineLengthCharScanner;
import com.servingxml.components.flatfile.scanner.FlatFileScanner;
import com.servingxml.components.flatfile.scanner.bytes.LineDelimitedByteScanner;
import com.servingxml.components.flatfile.scanner.characters.LineDelimitedCharScanner;
import com.servingxml.components.flatfile.recordtype.FlatRecordType;
import com.servingxml.components.flatfile.FlatRecordReader;

/**
 * The <code>FlatFile</code> defines a flat file.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FlatFile {

  private final FlatFileHeader header;
  private final FlatFileTrailer trailer;
  private final FlatFileBodyFactory bodyFactory;
  private final FlatFileOptionsFactory flatFileOptionsFactory;
  private final FlatFileSignatureFactory[] signatureFactories;

  public FlatFile(FlatFileOptionsFactory flatFileOptionsFactory,
                  FlatFileHeader header,
                  FlatFileTrailer trailer, 
                  FlatFileBodyFactory bodyFactory, 
                  FlatFileSignatureFactory[] signatureFactories) {

    this.header = header;
    this.trailer = trailer;
    this.flatFileOptionsFactory = flatFileOptionsFactory;
    this.bodyFactory = bodyFactory;
    this.signatureFactories = signatureFactories;
  }

  public boolean isSignatures() {
    return signatureFactories.length > 0;
  }

  public RecordWriter createRecordWriter(ServiceContext context, Flow flow, StreamSinkFactory sinkFactory, FlatFileOptions defaultOptions) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context,flow,defaultOptions);
    FlatFileBody body = bodyFactory.createFlatFileBody(context,flow,flatFileOptions);
    return new FlatFileWriter(flatFileOptions, header, body, trailer, signatureFactories, sinkFactory);
  }

  public RecordReader createRecordReader(ServiceContext context, Flow flow, 
                                         StreamSource source, long fromRecord, long maxRecordCount, FlatFileOptions defaultOptions) {
    FlatFileOptions flatFileOptions = flatFileOptionsFactory.createFlatFileOptions(context,flow,defaultOptions);
    FlatFileBody body = bodyFactory.createFlatFileBody(context,flow,flatFileOptions);

    FlatRecordType[] headerTypes = header.createFlatRecordTypes(context, flow, flatFileOptions);
    FlatRecordType[] trailerTypes = trailer.createFlatRecordTypes(context, flow, flatFileOptions);

    boolean fixedRecordLength = true;
    FlatRecordReader[] headerReaders = new FlatRecordReader[headerTypes.length];
    for (int i = 0; i < headerTypes.length; ++i) {
      headerReaders[i] = headerTypes[i].createFlatRecordReader();
      if (!headerTypes[i].isFixedLength()) {
        fixedRecordLength = false;
      }
    }
    FlatRecordReader[] trailerReaders = new FlatRecordReader[trailerTypes.length];
    for (int i = 0; i < trailerTypes.length; ++i) {
      trailerReaders[i] = trailerTypes[i].createFlatRecordReader();
      if (!trailerTypes[i].isFixedLength()) {
        fixedRecordLength = false;
      }
    }
    if (!body.getFlatRecordType().isFixedLength()) {
      fixedRecordLength = false;
    }

    FlatFileSignature[] flatFileSignatures = getFlatFileSignatures();

    FlatRecordReader flatRecordReaderResolver = body.getFlatRecordType().createFlatRecordReader();

    FlatFileScanner flatFileScanner;

    if (!flatFileOptions.isLineDelimited() || fixedRecordLength) {
      if (flatFileOptions.isCountPositionsInBytes()) {
        flatFileScanner = new FixedLineLengthByteScanner(headerReaders,
                                                         trailerReaders, 
                                                         flatFileOptions);
      } else {
        flatFileScanner = new FixedLineLengthCharScanner(headerReaders,
                                                         trailerReaders, 
                                                         flatFileOptions);
      }
    } else {
      if (flatFileOptions.isCountPositionsInBytes()) {
        flatFileScanner = new LineDelimitedByteScanner(headerReaders.length,
                                                       trailerReaders.length, 
                                                       flatFileOptions);
      } else {
        flatFileScanner = new LineDelimitedCharScanner(headerReaders.length,
                                                       trailerReaders.length, 
                                                       flatFileOptions);
      }
    }
    RecordReader recordReader = new FlatFileReader(flatFileScanner, 
                                                   headerReaders, 
                                                   trailerReaders, 
                                                   flatRecordReaderResolver, 
                                                   flatFileSignatures, 
                                                   source, 
                                                   fromRecord, 
                                                   maxRecordCount,
                                                   flatFileOptions.isLineDelimited());

    return recordReader;
  }

  private FlatFileSignature[] getFlatFileSignatures() {
    FlatFileSignature[] signers = new FlatFileSignature[signatureFactories.length];
    for (int i = 0; i < signatureFactories.length; ++i) {
      signers[i] = signatureFactories[i].createFlatFileSignature();
    }
    return signers;
  }

  public boolean isFieldDelimited() {
    return bodyFactory.isFieldDelimited();
  }

  public boolean isBinary() {
    return bodyFactory.isBinary();
  }

  public boolean isText() {
    return bodyFactory.isText();
  }
}
