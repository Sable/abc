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

package com.servingxml.components.flatfile.scanner.bytes;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.net.URL;
import java.util.zip.CRC32;
import java.nio.charset.Charset;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

import junit.framework.TestCase;

import com.servingxml.app.AppContext;
import com.servingxml.app.DefaultAppContext;
import com.servingxml.app.DefaultServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.app.FlowImpl;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatContentReceiver;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordReceiver;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.FlatFileOptionsImpl;
import com.servingxml.components.flatfile.options.WhitespaceByteDelimiterExtractor;
import com.servingxml.components.flatfile.recordtype.AnnotationRecordReader;
import com.servingxml.components.flatfile.scanner.FlatFileScanner;
import com.servingxml.util.CharsetHelper;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.RecordReceiver;
import com.servingxml.util.system.Logger;
import com.servingxml.util.record.Record;
import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.util.MutableNameTable;
import com.servingxml.app.Environment;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.app.ParameterDescriptor;

public class FixedLineLengthByteScannerTest  extends TestCase {
  private WhitespaceByteDelimiterExtractor wsDelimiter;

  private Logger logger = SystemConfiguration.getLogger();
  private AppContext appContext;
  private MutableNameTable nameTable = new NameTableImpl();
  private ServiceContext context;
  private Flow flow;

  public FixedLineLengthByteScannerTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    wsDelimiter = WhitespaceByteDelimiterExtractor.newInstance(Charset.defaultCharset());
    SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    SimpleIocContainer resources = new SimpleIocContainer(nameTable, transformerFactory);
    appContext = new DefaultAppContext("",resources,logger);
    context = new DefaultServiceContext(appContext,"",logger);
    Environment env = new Environment(ParameterDescriptor.EMPTY_PARAMETER_DESCRIPTOR_ARRAY,new SimpleQnameContext());
    flow = new FlowImpl(env, context, Record.EMPTY, Record.EMPTY);
  }

  public void testBookOrders() throws Exception {
    String filename = "book_orders-non_delimited.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //System.out.println(getClass().getName()+".testBookOrders enter");

    //InputStream is = getClass().getResourceAsStream( "/book_orders-non_delimited.txt" );

    FlatContentReceiverImpl receiver = new FlatContentReceiverImpl();

    FlatRecordReader headerTrailerReader = new AnnotationRecordReader(100);

    Charset charset = Charset.defaultCharset();

    FlatRecordReader[] headerReaders = new FlatRecordReader[]{headerTrailerReader};
    FlatRecordReader[] trailerReaders = new FlatRecordReader[]{headerTrailerReader};
    FlatFileOptions flatFileOptions = new FlatFileOptionsImpl(charset,true,true);
    FlatFileScanner reader = new FixedLineLengthByteScanner(headerReaders, trailerReaders, 
                                                            flatFileOptions);
    reader.scan(context, flow, is, receiver);
    is.close();
    int totalRecords = receiver.headerCount + receiver.recordCount + receiver.trailerCount;
    assertTrue("header:  1 == " + receiver.headerCount, receiver.headerCount == 1);
    assertTrue("body:  4 == " + receiver.recordCount, receiver.recordCount == 4);
    assertTrue("trailer:  1 == " + receiver.trailerCount, receiver.trailerCount == 1);
    assertTrue("total:  6 == " + totalRecords, totalRecords == 6);

    long crc1 = calculateCRC(url);
    long crc2 = receiver.crc.getValue();
    assertTrue("" + crc1 + " = " + crc2, crc1 == crc2);
    //System.out.println(getClass().getName()+".testBookOrders leave");
  }

  private long calculateCRC(URL url) throws Exception {
    CRC32 crc = new CRC32();

    BufferedInputStream is = null;
    try {
      is = new BufferedInputStream(url.openStream());
      byte[] buf = new byte[512];
      boolean eof = false;
      while (!eof) {
        int len = is.read(buf, 0, 512);
        if (len == -1) {
          eof = true;
        } else {
          crc.update(buf, 0, len);
        }
      }
    } finally {
      is.close();
    }

    return crc.getValue();
  }

  static class FlatContentReceiverImpl implements FlatContentReceiver {
    CRC32 crc = new CRC32();
    boolean isHeader = false;
    boolean isTrailer = false;
    boolean isBody = false;
    int recordCount = 0;
    int headerCount = 0;
    int trailerCount = 0;
    int recordLevel = 0;

    public FlatContentReceiverImpl() {
    }

    public void endHeader() {
      //System.out.println ("endHeader");
      isHeader = false;
    }
    public void endBody() {
      //System.out.println ("endBody");
      isBody = false;
    }

    public void startRecord() {
      //System.out.println ("startRecord");
      ++recordLevel;
      //System.out.println ("recordLevel = " + recordLevel);
      if (isBody) {
        //System.out.println ("isBody, recordLevel = " + recordLevel);
        if (recordLevel == 1) {
          ++recordCount;
        }
      } else if (isHeader) {
        ++headerCount;
      } else if (isTrailer) {
        ++trailerCount;
      }
    }

    public void delimiter(byte[] data, int start, int length) {
      //System.out.println ("delimiter=" + new String(data, start, length) + ". " + length);
      crc.update(data, start, length);
    }

    public void endRecord() {
      //System.out.println ("endRecord");
      --recordLevel;
    }

    public void data(RecordInput recordInput) {
      //System.out.println ("data=" + new String(data, start, length) + ". " + length + ", recordCount = " + recordCount);
      try {
        byte[] bytes = new byte[100];
        recordInput.readBytes(bytes);
        crc.update(bytes);
      } catch (IOException e) {
        throw new ServingXmlException(e.getMessage(), e);
      }
    }

    public void commentLine(byte[] data, int start, int length) {
      //System.out.println ("commentLine=" + new String(data, start, length) + ". " + length + ", recordCount = " + recordCount);
      crc.update(data, start, length);
    }

    public void lineContinuation(byte[] data, int start, int length) {
      //System.out.println ("lineContinutation=" + new String(data, start, length) + ". " + length);
      crc.update(data, start, length);
    }
    public void ignorableWhitespace(byte[] data, int start, int length) {
      //System.out.println ("ignorableWhitespace=" + new String(data, start, length) + ". " + length);
      crc.update(data, start, length);
    }
    public void startFlatFile() {
      //System.out.println ("startFlatFile");
    }
    public void endFlatFile() {
      //System.out.println ("endFlatFile recordCount = " + recordCount);
    }
    public void startHeader() {
      //System.out.println ("startHeader");
      isHeader = true;
    }
    public void startBody() {

      //System.out.println ("startBody");
      isBody = true;
    }
    public void startTrailer() {
      //System.out.println ("startTrailer");
      isTrailer = true;
    }
    public void endTrailer() {
      //System.out.println ("endTrailer");
      isTrailer = false;
    }
  }

  public class FlatRecordReceiverImpl implements FlatRecordReceiver {
    public void startFlatFile() {
      //System.out.println ("startFlatFile");
    }
    public void endFlatFile() {
      //System.out.println ("endFlatFile");
    }
    public void headerRecord(RecordInput recordInput) {
      //System.out.println ("headerRecord " + new String(data, start, length));
    }
    public void bodyRecord(RecordInput recordInput) {
    }
    public void trailerRecord(RecordInput recordInput) {
      //System.out.println ("trailerRecord " + new String(data, start, length));
    }
  }
}




