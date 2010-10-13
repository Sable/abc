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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.CRC32;
import java.nio.charset.Charset;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

import com.servingxml.app.AppContext;
import com.servingxml.app.DefaultAppContext;
import com.servingxml.app.DefaultServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.app.FlowImpl;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.flatfile.FlatContentReceiver;
import com.servingxml.components.flatfile.FlatRecordReceiver;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.layout.FlatRecordReceiverAdaptor;
import com.servingxml.components.flatfile.options.*;
import com.servingxml.components.flatfile.scanner.FlatFileScanner;
import com.servingxml.util.system.Logger;
import com.servingxml.util.record.Record;
import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.util.MutableNameTable;
import com.servingxml.app.Environment;
import com.servingxml.util.SimpleQnameContext;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.quotesymbol.QuoteSymbol;

import junit.framework.TestCase;

public class LineDelimitedByteScannerTest extends TestCase {
  private ByteTrimmer byteTrimmer;

  private Logger logger = SystemConfiguration.getLogger();
  private AppContext appContext;
  private MutableNameTable nameTable = new NameTableImpl();
  private ServiceContext context;
  private Flow flow;

  public LineDelimitedByteScannerTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
    byteTrimmer = ByteTrimmer.newInstance(Charset.defaultCharset());
    SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    SimpleIocContainer resources = new SimpleIocContainer(nameTable, transformerFactory);
    appContext = new DefaultAppContext("",resources,logger);
    context = new DefaultServiceContext(appContext,"",logger);
    Environment env = new Environment(ParameterDescriptor.EMPTY_PARAMETER_DESCRIPTOR_ARRAY,new SimpleQnameContext());
    flow = new FlowImpl(env, context, Record.EMPTY, Record.EMPTY);
  }

  public void xtestPlans() throws Exception {
    String filename = "plans.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/plans.txt" );
                                                                                        
    FlatContentReceiverImpl receiver = new FlatContentReceiverImpl();

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);

    FlatFileScanner reader = new LineDelimitedByteScanner(1, 0, flatFileOptions);
    reader.scan(context, flow, is, receiver);
    is.close();
    assertTrue("header:  1 == " + receiver.headerCount, receiver.headerCount == 1);
    assertTrue("body:  10 == " + receiver.recordCount, receiver.recordCount == 10);
    assertTrue("trailer:  0 == " + receiver.trailerCount, receiver.trailerCount == 0);

    long crc1 = calculateCRC(url);
    long crc2 = receiver.crc.getValue();
    assertTrue("" + crc1 + " = " + crc2, crc1 == crc2);
  }

  public void testHot() throws Exception {
    String filename = "hot.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/hot.txt" );
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);

    FlatContentReceiverImpl receiver = new FlatContentReceiverImpl();

    FlatFileScanner reader = new LineDelimitedByteScanner(0, 0, flatFileOptions);
    reader.scan(context, flow, is, receiver);
    is.close();
    assertTrue("header:  0 == " + receiver.headerCount, receiver.headerCount == 0);
    assertTrue("body:  17 == " + receiver.recordCount, receiver.recordCount == 17);
    assertTrue("trailer:  0 == " + receiver.trailerCount, receiver.trailerCount == 0);

    long crc1 = calculateCRC(url);
    long crc2 = receiver.crc.getValue();
    assertTrue("" + crc1 + " = " + crc2, crc1 == crc2);
  }

  public void testHot_EmptyLines() throws Exception {
    String filename = "hot-empty_lines.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/hot-empty_lines.txt" );

    FlatContentReceiverImpl receiver = new FlatContentReceiverImpl();
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setIgnoreEmptyLines(true);

    FlatFileScanner reader = new LineDelimitedByteScanner(0, 0, flatFileOptions);
    reader.scan(context, flow, is, receiver);
    is.close();
    assertTrue("header:  0 == " + receiver.headerCount, receiver.headerCount == 0);
    assertTrue("body:  17 == " + receiver.recordCount, receiver.recordCount == 17);
    assertTrue("trailer:  0 == " + receiver.trailerCount, receiver.trailerCount == 0);

    long crc1 = calculateCRC(url);
    long crc2 = receiver.crc.getValue();
    assertTrue("" + crc1 + " = " + crc2, crc1 == crc2);
  }

  public void test242() throws Exception {
    String filename = "bookorders-pos.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/bookorders-pos.txt" );

    FlatContentReceiverImpl receiver = new FlatContentReceiverImpl();
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);

    FlatFileScanner reader = new LineDelimitedByteScanner(2, 2, flatFileOptions);
    reader.scan(context, flow, is, receiver);
    is.close();
    assertTrue("header:  2 == " + receiver.headerCount, receiver.headerCount == 2);
    assertTrue("body:  4 == " + receiver.recordCount, receiver.recordCount == 4);
    assertTrue("trailer:  2 == " + receiver.trailerCount, receiver.trailerCount == 2);

    long crc1 = calculateCRC(url);
    long crc2 = receiver.crc.getValue();
    assertTrue("" + crc1 + " = " + crc2, crc1 == crc2);
  }

  public void testCountriesWithComment() throws Exception {
    String filename = "countries.csv";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/countries.csv" );

    FlatContentReceiverImpl receiver = new FlatContentReceiverImpl();
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setCommentStarter(new CommentStarterImpl("#"));
    flatFileOptions.setQuoteSymbol(new QuoteSymbol('"',"\"\""));

    FlatFileScanner reader = new LineDelimitedByteScanner(0, 0, flatFileOptions);
    reader.scan(context, flow, is, receiver);
    is.close();
    assertTrue("header:  0 == " + receiver.headerCount, receiver.headerCount == 0);
    //assertTrue("body:  4 == " + receiver.recordCount, receiver.recordCount == 4);
    //System.out.println ("recordCount = " + receiver.recordCount);
    assertTrue("trailer:  0 == " + receiver.trailerCount, receiver.trailerCount == 0);

    long crc1 = calculateCRC(url);
    long crc2 = receiver.crc.getValue();
    assertTrue("" + crc1 + " = " + crc2, crc1 == crc2);
  }

  public void test242b() throws Exception {
    String filename = "bookorders-pos.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/bookorders-pos.txt" );

    FlatRecordReceiverImpl receiver = new FlatRecordReceiverImpl();

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);

    FlatFileScanner reader = new LineDelimitedByteScanner(2, 2, flatFileOptions);
    reader.scan(context, flow, is, new FlatRecordReceiverAdaptor(receiver));
    is.close();
  }
/*
  public void testPropertiesFile() throws Exception {
    String filename = "messages.properties";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/messages.properties" );

    FlatContentReceiverImpl receiver = new FlatContentReceiverImpl();

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);

    FlatFileScanner reader = new LineDelimitedByteScanner(0, 0, flatFileOptions);
    reader.scan(is, receiver);
    is.close();
    assertTrue("header:  0 == " + receiver.headerCount, receiver.headerCount == 0);
    assertTrue("body:  32 == " + receiver.recordCount, receiver.recordCount == 32);
    assertTrue("trailer:  0 == " + receiver.trailerCount, receiver.trailerCount == 0);

    long crc1 = calculateCRC(url);
    long crc2 = receiver.crc.getValue();
    assertTrue("" + crc1 + " = " + crc2, crc1 == crc2);
  }
*/

  public void testPropertiesFileb() throws Exception {
    String filename = "messages.properties";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/messages.properties" );

    FlatRecordReceiverImpl receiver = new FlatRecordReceiverImpl();

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);

    FlatFileScanner reader = new LineDelimitedByteScanner(0, 0, flatFileOptions);
    //System.out.println ("BYTE RECEIVER");
    reader.scan(context, flow, is, new FlatRecordReceiverAdaptor(receiver));
    is.close();
  }

  public void test242Quote() throws Exception {
    String filename = "bookorders-pos2.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/bookorders-pos2.txt" );

    FlatContentReceiverImpl receiver = new FlatContentReceiverImpl();

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    QuoteSymbol quoteSymbol = new QuoteSymbol('"', "\"\"");
    flatFileOptions.setQuoteSymbol(quoteSymbol);

    FlatFileScanner reader = new LineDelimitedByteScanner(2, 2, flatFileOptions);
    reader.scan(context, flow, is, receiver);
    is.close();
    assertTrue("header:  2 == " + receiver.headerCount, receiver.headerCount == 2);
    assertTrue("body:  4 == " + receiver.recordCount, receiver.recordCount == 4);
    assertTrue("trailer:  2 == " + receiver.trailerCount, receiver.trailerCount == 2);

    long crc1 = calculateCRC(url);
    long crc2 = receiver.crc.getValue();
    assertTrue("" + crc1 + " = " + crc2, crc1 == crc2);
  }

  public void test242Quoteb() throws Exception {
    String filename = "bookorders-pos2.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/bookorders-pos2.txt" );

    FlatRecordReceiverImpl receiver = new FlatRecordReceiverImpl();
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    QuoteSymbol quoteSymbol = new QuoteSymbol('"', "\"\"");
    flatFileOptions.setQuoteSymbol(quoteSymbol);

    FlatFileScanner reader = new LineDelimitedByteScanner(2, 2, flatFileOptions);
    //System.out.println ("BYTE RECEIVER");
    reader.scan(context, flow, is, new FlatRecordReceiverAdaptor(receiver));
    is.close();
  }

  public void test1StartEnd1() throws Exception {
    String filename = "opdef.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/opdef.txt" );

    FlatContentReceiverImpl receiver = new FlatContentReceiverImpl();
    Delimiter delim1 = new RecordDelimiter("\n");
    Delimiter delim2 = new RecordDelimiter("{:","-}");
    Delimiter[] delims = new Delimiter[]{delim1,delim2};
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRecordDelimiters(delims);

    FlatFileScanner reader = new LineDelimitedByteScanner(1, 2, flatFileOptions);
    reader.scan(context, flow, is, receiver);
    is.close();
    assertTrue("header:  1 == " + receiver.headerCount, receiver.headerCount == 1);
    assertTrue("trailer:  2 == " + receiver.trailerCount, receiver.trailerCount == 2);
    assertTrue("body:  1 == " + receiver.recordCount, receiver.recordCount == 1);

    long crc1 = calculateCRC(url);
    long crc2 = receiver.crc.getValue();
    assertTrue("" + crc1 + " = " + crc2, crc1 == crc2);
  }

  public void test1StartEnd1b() throws Exception {
    String filename = "opdef.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/opdef.txt" );

    FlatRecordReceiverImpl receiver = new FlatRecordReceiverImpl();
    Delimiter delim1 = new RecordDelimiter("\n");
    Delimiter delim2 = new RecordDelimiter("{:","-}");
    Delimiter[] delims = new Delimiter[]{delim1,delim2};
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRecordDelimiters(delims);

    FlatFileScanner reader = new LineDelimitedByteScanner(1, 2, flatFileOptions);
    //System.out.println ("BYTE RECEIVER");
    reader.scan(context, flow, is, new FlatRecordReceiverAdaptor(receiver));
    is.close();
  }

  public void xtest3StartStartEndEnd0() throws Exception {
    String filename = "myspec.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/myspec.txt" );

    FlatContentReceiverImpl receiver = new FlatContentReceiverImpl();
    Delimiter delim1 = new RecordDelimiter("\n");
    Delimiter delim2 = new RecordDelimiter("{","}");
    Delimiter[] delims = new Delimiter[]{delim1,delim2};
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRecordDelimiters(delims);
    flatFileOptions.setCommentStarter(new CommentStarterImpl("#"));

    FlatFileScanner reader = new LineDelimitedByteScanner(3, 0, flatFileOptions);
    reader.scan(context, flow, is, receiver);
    is.close();
    assertTrue("header:  3 == " + receiver.headerCount, receiver.headerCount == 3);
    assertTrue("trailer:  0 == " + receiver.trailerCount, receiver.trailerCount == 0);
    assertTrue("body:  1 == " + receiver.recordCount, receiver.recordCount == 1);

    long crc1 = calculateCRC(url);
    long crc2 = receiver.crc.getValue();
    assertTrue("" + crc1 + " = " + crc2, crc1 == crc2);
  }

  public void xtest3StartStartEndEnd0b() throws Exception {
    String filename = "myspec.txt";
    URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    assertTrue("" + filename, url != null);
    InputStream is = url.openStream();

    //InputStream is = getClass().getResourceAsStream( "/myspec.txt" );

    FlatRecordReceiverImpl receiver = new FlatRecordReceiverImpl();

    Delimiter delim1 = new RecordDelimiter("\n");
    Delimiter delim2 = new RecordDelimiter("{","}");
    Delimiter[] delims = new Delimiter[]{delim1,delim2};
    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRecordDelimiters(delims);
    flatFileOptions.setCommentStarter(new CommentStarterImpl("#"));

    FlatFileScanner reader = new LineDelimitedByteScanner(3, 1, flatFileOptions);
    //System.out.println ("BYTE RECEIVER");
    reader.scan(context, flow, is, new FlatRecordReceiverAdaptor(receiver));
    is.close();
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
      if (isBody) {
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
      //System.out.println ("data=" + new String(data, start, length) + ". " + length);
      byte[] buffer = recordInput.toByteArray();
      crc.update(buffer, 0, buffer.length);
    }

    public void commentLine(byte[] data, int start, int length) {
      //System.out.println ("commentLine=" + new String(data, start, length) + ". " + length);
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
      //System.out.println ("endFlatFile");
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




