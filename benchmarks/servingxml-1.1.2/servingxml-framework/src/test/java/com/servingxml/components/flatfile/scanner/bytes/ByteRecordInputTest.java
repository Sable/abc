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

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.nio.charset.Charset;

import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.layout.FlatRecordReceiverAdaptor;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.RepeatDelimiter;
import com.servingxml.components.flatfile.options.SegmentDelimiter;
import com.servingxml.components.flatfile.options.FlatFileOptionsImpl;
import com.servingxml.util.record.Record;

import junit.framework.TestCase;

public class ByteRecordInputTest extends TestCase {

  public ByteRecordInputTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
  }

  public void testStudents() throws Exception {
    System.out.println(getClass().getName()+".testStudents");

    String inputString="JANE^ENGL^C-~MATH^A+|1972^BLUE^CHICAGO^IL~ATLANTA^GA";
    byte[] input = inputString.getBytes();

    Delimiter repeatDelimiter = new RepeatDelimiter("^");
    Delimiter segmentDelimiter = new SegmentDelimiter("|");

    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};
    Delimiter[] segmentDelimiters = new Delimiter[]{segmentDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);
    flatFileOptions.setSegmentDelimiters(segmentDelimiters);

    RecordInput recordInput = new ByteRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void testInvoice() throws Exception {
    String inputString="12|2007-02-02|DocType|} "
  + "SndId|SndName|SndAddress|SndZip|} "
  + "RecId|RecName|RecAddress|RecZip|} "
  + "~ "
  + "1|Item 1|2|Kg|150|300|} "
  + "2|Item 2|2|Kg|350|700|} "
  + "3|Item 3|1|$|50|50|} "
  + "4|Item 4|10|Unt|30|100|} "
  + "~ "
  + "1|Ref 1|Doc 1|Text|} "
  + "2|Ref 2|Doc 2|Text|} "
  + "~ "
  + "1|Disc 1|Item 1|} "
  + "2|Disc 2|Item 2|} "
  + "3|Disc 3|Item 3|} "
  + "~ "
  + "Code a|Value 1|} "
  + "Code z|Value 3|} "
  + "Code x|Value 2|} "
  + "Code w|Value 2|} "
  + "Code y|Value 1|}"
  + "~";

    byte[] input = inputString.getBytes();

    Delimiter repeatDelimiter = new RepeatDelimiter("}");
    Delimiter segmentDelimiter = new SegmentDelimiter("~");

    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};
    Delimiter[] segmentDelimiters = new Delimiter[]{segmentDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);
    flatFileOptions.setSegmentDelimiters(segmentDelimiters);
    flatFileOptions.setOmitFinalRepeatDelimiter(false);

    RecordInput recordInput = new ByteRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void testNested() throws Exception {
    System.out.println("testNested");

    String inputString="{a0 b0{a1 b1{a2 b2} {c2 d2}}}";
    byte[] input = inputString.getBytes();

    Delimiter repeatDelimiter = new RepeatDelimiter("{","}");

    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);

    RecordInput recordInput = new ByteRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void testNoSegmentDelimiters() throws Exception {
    System.out.println("testNoSegmentDelimiters");

    String inputString="JANE^ENGL^C-~MATH^A+";
    byte[] input = inputString.getBytes();

    Delimiter repeatDelimiter = new RepeatDelimiter("^");

    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);

    RecordInput recordInput = new ByteRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void testRepeatingBracketed() throws Exception {
    String inputString="{a}{b}{c}{d}";
    byte[] input = inputString.getBytes();

    Delimiter repeatDelimiter = new RepeatDelimiter("{","}");

    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);

    RecordInput recordInput = new ByteRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void testWrappedRepeatingBracketed() throws Exception {
    String inputString="{{a}{b}{c}{d}}";
    byte[] input = inputString.getBytes();

    Delimiter segmentDelimiter = new SegmentDelimiter("{","}");
    Delimiter repeatDelimiter = new RepeatDelimiter("{","}");

    Delimiter[] segmentDelimiters = new Delimiter[]{segmentDelimiter};
    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);
    flatFileOptions.setSegmentDelimiters(segmentDelimiters);

    RecordInput recordInput = new ByteRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void testSegmentDelimiter() throws Exception {
    String inputString="{{a}{b}{c}{d}}";
    byte[] input = inputString.getBytes();

    Delimiter segmentDelimiter = new SegmentDelimiter("{","}");

    Delimiter[] segmentDelimiters = new Delimiter[]{segmentDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setSegmentDelimiters(segmentDelimiters);

    RecordInput recordInput = new ByteRecordInput(input,0,input.length,Charset.defaultCharset());

    //assertTrue(recordInput.done());

  }

  public void testTransaction() throws Exception {
    String inputString =
"TRAN:trantype=PRIN#origin=Toronto#"
+ "{TRANREF:reftype=TREF#ref=1234567890123456B#}"
+ "{TRANREF:reftype=FOTR#ref=1234567890123456#}"
+ "{DRIVER:drivertype=OPER#drivercode=SELL#}"
+ "{DRIVER:drivertype=STRM#drivercode=FOP#}"
+ "{INSTR:instrtype=PINS#instrreftype=INT#instrref=PTEQTY1#qty=1000000#}"
+ "{INSTR:instrtype=TCCY#instrreftype=ISO#instrref=USD#}"
+ "{INSTR:instrtype=SINS#instrreftype=ISO#instrref=USD#}"
+ "{PARTY:partytype=COMP#partyreftype=X3#partyref=CMP1#}"
+ "{PARTY:partytype=SECP#partyreftype=X3#partyref=PTSECP1#}"
+ "{DATE:datetype=TDAT#date=01-Jan-2007#}"
+ "{DATE:datetype=VDAT#date=01-Jan-2007#}"
+ "{CHARGE:chargetype=COMM#calctype=NONE#chargeamount=50.00#instrreftype=ISO#instrref=USD}"
+ "{PRICE:ratetype=TPRC#price=4.9#multdiv=X1#pricetype=YP#}"
+ "";
    byte[] input = inputString.getBytes();

    Delimiter segmentDelimiter = new SegmentDelimiter("{","}");
    Delimiter repeatDelimiter = new RepeatDelimiter("{","}");

    Delimiter[] segmentDelimiters = new Delimiter[]{segmentDelimiter};
    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,true);
    flatFileOptions.setCountPositionsInBytes(true);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);
    flatFileOptions.setSegmentDelimiters(segmentDelimiters);

    RecordInput recordInput = new ByteRecordInput(input, 0, input.length,
                                                           Charset.defaultCharset());

  }
}




