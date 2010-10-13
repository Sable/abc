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

package com.servingxml.components.flatfile.scanner.characters;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.nio.charset.Charset;

import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.layout.FlatRecordReceiverAdaptor;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.RepeatDelimiter;
import com.servingxml.components.flatfile.options.SegmentDelimiter;
import com.servingxml.components.flatfile.options.FieldDelimiter;
import com.servingxml.components.flatfile.options.NameDelimiter;
import com.servingxml.components.flatfile.options.FlatFileOptionsImpl;
import com.servingxml.util.record.Record;

import junit.framework.TestCase;

public class CharRecordInputTest extends TestCase {

  public CharRecordInputTest(String name) {
    super(name);
  }

  public void setUp() throws Exception {
  }

  public void xtestStudents() throws Exception {
    System.out.println(getClass().getName()+".testStudents");
    String inputString="JANE^ENGL^C-~MATH^A+|1972^BLUE^CHICAGO^IL~ATLANTA^GA";
    char[] input = inputString.toCharArray();

    Delimiter repeatDelimiter = new RepeatDelimiter("^");
    Delimiter segmentDelimiter = new SegmentDelimiter("|");

    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};
    Delimiter[] segmentDelimiters = new Delimiter[]{segmentDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,false);
    flatFileOptions.setCountPositionsInBytes(false);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);
    flatFileOptions.setSegmentDelimiters(segmentDelimiters);

    RecordInput recordInput = new CharRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void xtestInvoice() throws Exception {
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

    char[] input = inputString.toCharArray();

    Delimiter repeatDelimiter = new RepeatDelimiter("}");
    Delimiter segmentDelimiter = new SegmentDelimiter("~");

    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};
    Delimiter[] segmentDelimiters = new Delimiter[]{segmentDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,false);
    flatFileOptions.setCountPositionsInBytes(false);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);
    flatFileOptions.setSegmentDelimiters(segmentDelimiters);
    flatFileOptions.setOmitFinalRepeatDelimiter(false);

    RecordInput recordInput = new CharRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void xtestNested() throws Exception {
    System.out.println("testNested");

    String inputString="{a0 b0{a1 b1{a2 b2} {c2 d2}}}";
    char[] input = inputString.toCharArray();

    Delimiter repeatDelimiter = new RepeatDelimiter("{","}");

    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,false);
    flatFileOptions.setCountPositionsInBytes(false);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);

    RecordInput recordInput = new CharRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void xtestNoSegmentDelimiters() throws Exception {
    System.out.println("testNoSegmentDelimiters");

    String inputString="JANE^ENGL^C-~MATH^A+";
    char[] input = inputString.toCharArray();

    Delimiter repeatDelimiter = new RepeatDelimiter("^");

    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,false);
    flatFileOptions.setCountPositionsInBytes(false);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);

    RecordInput recordInput = new CharRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void xtestRepeatingBracketed() throws Exception {
    String inputString="{a}{b}{c}{d}";
    char[] input = inputString.toCharArray();

    Delimiter repeatDelimiter = new RepeatDelimiter("{","}");

    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,false);
    flatFileOptions.setCountPositionsInBytes(false);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);

    RecordInput recordInput = new CharRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void xtestWrappedRepeatingBracketed() throws Exception {
    String inputString="{{a}{b}{c}{d}}";
    char[] input = inputString.toCharArray();

    Delimiter segmentDelimiter = new SegmentDelimiter("{","}");
    Delimiter repeatDelimiter = new RepeatDelimiter("{","}");

    Delimiter[] segmentDelimiters = new Delimiter[]{segmentDelimiter};
    Delimiter[] repeatDelimiters = new Delimiter[]{repeatDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,false);
    flatFileOptions.setCountPositionsInBytes(false);
    flatFileOptions.setRepeatDelimiters(repeatDelimiters);
    flatFileOptions.setSegmentDelimiters(segmentDelimiters);

    RecordInput recordInput = new CharRecordInput(input,0,input.length,Charset.defaultCharset());

  }

  public void xtestSegmentDelimiter() throws Exception {
    String inputString="{{a}{b}{c}{d}}";
    char[] input = inputString.toCharArray();

    Delimiter segmentDelimiter = new SegmentDelimiter("{","}");

    Delimiter[] segmentDelimiters = new Delimiter[]{segmentDelimiter};

    FlatFileOptionsImpl flatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,false);
    flatFileOptions.setCountPositionsInBytes(false);
    flatFileOptions.setSegmentDelimiters(segmentDelimiters);

    RecordInput recordInput = new CharRecordInput(input,0,input.length,Charset.defaultCharset());

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
    char[] input = inputString.toCharArray();

    Delimiter[] segmentDelimiters = new Delimiter[]{new SegmentDelimiter("{")};
    Delimiter[] repeatDelimiters = new Delimiter[]{new RepeatDelimiter("{","}")};
    Delimiter[] colonFieldDelimiters = new Delimiter[]{new FieldDelimiter(":")};
    Delimiter[] poundFieldDelimiters = new Delimiter[]{new FieldDelimiter("#")};
    Delimiter[] nameDelimiters = new Delimiter[]{new NameDelimiter("=")};

    FlatFileOptionsImpl baseFlatFileOptions = new FlatFileOptionsImpl(Charset.defaultCharset(),true,false);
    baseFlatFileOptions.setCountPositionsInBytes(false);
    baseFlatFileOptions.setRepeatDelimiters(repeatDelimiters);
    baseFlatFileOptions.setNameDelimiters(nameDelimiters);

    FlatFileOptionsImpl flatFileOptions1 = new FlatFileOptionsImpl(baseFlatFileOptions);
    flatFileOptions1.setFieldDelimiters(colonFieldDelimiters);

    FlatFileOptionsImpl flatFileOptions2 = new FlatFileOptionsImpl(baseFlatFileOptions);
    flatFileOptions2.setFieldDelimiters(colonFieldDelimiters);
    flatFileOptions2.setSegmentDelimiters(segmentDelimiters);

    FlatFileOptionsImpl flatFileOptions3 = new FlatFileOptionsImpl(baseFlatFileOptions);
    flatFileOptions3.setFieldDelimiters(poundFieldDelimiters);

    RecordInput recordInput = new CharRecordInput(input, 0, input.length,
                                                           Charset.defaultCharset());
    String s;
    RecordInput segmentInput;
  }
}




