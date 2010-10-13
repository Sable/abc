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

import java.nio.charset.Charset;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;

import junit.framework.TestCase;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.util.CharsetHelper;

public class RecordBlockTest extends TestCase {

  public RecordBlockTest(String name) {
    super(name);
  }

  public void testRecordBlock() throws Exception {
    byte[] data = new String("abcdefghijklmnopqrstuvwx").getBytes();
    InputStream is = new ByteArrayInputStream(data);

    RecordBlock block = new InputStreamRecordBlock(is, 8);
    assertTrue("1-a 0="+block.length(), block.length() == 0);
    assertTrue("1-b 0="+block.size(), block.size() == 0);
    assertTrue("1-c "+Integer.MAX_VALUE+"="+block.maxCapacity(), block.maxCapacity()==Integer.MAX_VALUE);
    //assertTrue("1-d false="+block.done(), !block.done());

    block.next(10);
    assertTrue("2-a 10="+block.length(), block.length()==10);
    assertTrue("2-b 16="+block.size(), block.size()==16);
    assertTrue("2-c "+Integer.MAX_VALUE+"="+block.maxCapacity(), block.maxCapacity()==Integer.MAX_VALUE);
    //assertTrue("2-d false="+block.done(), !block.done());

    block.remove(9);
    assertTrue("3-a 1="+block.length(), block.length()==1);
    assertTrue("3-b 7="+block.size(), block.size()==7);
    assertTrue("3-c "+Integer.MAX_VALUE+"="+block.maxCapacity(), block.maxCapacity()==Integer.MAX_VALUE);
    //assertTrue("3-d false="+block.done(), !block.done());

    block.next(10);                                                            
    assertTrue("4-a 11="+block.length(), block.length()==11);
    assertTrue("4-b 15="+block.size(), block.size()==15);
    assertTrue("4-c "+Integer.MAX_VALUE+"="+block.maxCapacity(), block.maxCapacity()==Integer.MAX_VALUE);
    //assertTrue("4-d false="+block.done(), !block.done());

    block.remove(9);
    assertTrue("5-a 2="+block.length(), block.length()==2);
    assertTrue("5-b 6="+block.size(), block.size()==6);
    assertTrue("5-c maxCapacity "+Integer.MAX_VALUE+"="+block.maxCapacity(), block.maxCapacity()==Integer.MAX_VALUE);
    //assertTrue("5-d false="+block.done(), !block.done());

    block.next(10);
    assertTrue("6-a 6="+block.length(), block.length()==6);
    assertTrue("6-b 6="+block.size(), block.size()==6);
    assertTrue("6-c 6="+block.maxCapacity(), 6 == block.maxCapacity());
    //assertTrue("6-d false="+block.done(), !block.done());

    block.remove(9);
    assertTrue("7-a 0="+block.length(), block.length()==0);
    assertTrue("7-b 0=="+block.size(), 0 == block.size());
    assertTrue("7-c 0=="+block.maxCapacity(), 0 == block.maxCapacity());
    //assertTrue("7-d true="+block.done(), block.done());

  }

  public void testDataBuffer() throws Exception {
    byte[] data = new String("abcdefghijklmnopqrstuvwx").getBytes();
    InputStream is = new ByteArrayInputStream(data);

    RecordBlock block = new InputStreamRecordBlock(is, 8);
    Charset charset = Charset.defaultCharset();

    RecordInput buffer = new FixedLengthByteRecordInput(block, charset);
    assertTrue("1", !buffer.done());
    String s = buffer.readString(2);
    assertTrue("ab="+s, s.equals("ab"));
    String t = buffer.readString(3);
    assertTrue("cde="+t, t.equals("cde"));
    buffer.wipe();

    assertTrue("2", !buffer.done());
    s = buffer.readString(2);
    assertTrue("fg="+s, s.equals("fg"));
    t = buffer.readString(3);
    assertTrue("hij="+t, t.equals("hij"));
    buffer.wipe();

    assertTrue("3", !buffer.done());
    s = buffer.readString(2);
    assertTrue("kl="+s, s.equals("kl"));
    t = buffer.readString(3);
    assertTrue("mno="+t, t.equals("mno"));
    buffer.wipe();

    assertTrue("4", !buffer.done());
    s = buffer.readString(2);
    assertTrue("pq="+s, s.equals("pq"));
    t = buffer.readString(3);
    assertTrue("rst="+t, t.equals("rst"));
    buffer.wipe();

    assertTrue("5", !buffer.done());
    s = buffer.readString(2);
    assertTrue("uv="+s, s.equals("uv"));
    t = buffer.readString(3);
    assertTrue("wxy="+t, t.equals("wx"));
    buffer.wipe();

    assertTrue("6", buffer.done());
  }

  public void testDataBuffer888() throws Exception {
    byte[] data = new String("abcdefghijklmnopqrstuvwx").getBytes();
    InputStream is = new ByteArrayInputStream(data);

    RecordBlock block = new InputStreamRecordBlock(is, 8);
    Charset charset = Charset.defaultCharset();

    RecordInput buffer = new FixedLengthByteRecordInput(block, charset);
    assertTrue("1", !buffer.done());
    String s = buffer.readString(4);
    assertTrue("abcd="+s, s.equals("abcd"));
    String t = buffer.readString(4);
    assertTrue("efgh="+t, t.equals("efgh"));
    buffer.wipe();

    assertTrue("2", !buffer.done());
    s = buffer.readString(2);
    assertTrue("ij="+s, s.equals("ij"));
    t = buffer.readString(6);
    assertTrue("klmnop="+t, t.equals("klmnop"));
    buffer.wipe();

    assertTrue("3", !buffer.done());
    s = buffer.readString(4);
    assertTrue("qrst="+s, s.equals("qrst"));
    t = buffer.readString(4);
    assertTrue("uvwx="+t, t.equals("uvwx"));
    buffer.wipe();

    assertTrue("4", buffer.done());
  }

  public void testDataBufferReserved() throws Exception {
    byte[] data = new String("abcdefghijklmnopqrstuvwx").getBytes();
    InputStream is = new ByteArrayInputStream(data);

    RecordBlock block = new InputStreamRecordBlock(is, 8);
    Charset charset = Charset.defaultCharset();
    block.setReserved(8);

    RecordInput buffer = new FixedLengthByteRecordInput(block, charset);
    assertTrue("1", !buffer.done());
    String s = buffer.readString(4);
    assertTrue("abcd="+s, s.equals("abcd"));
    String t = buffer.readString(4);
    assertTrue("efgh="+t, t.equals("efgh"));
    buffer.wipe();

    assertTrue("2", !buffer.done());
    s = buffer.readString(2);
    assertTrue("ij="+s, s.equals("ij"));
    t = buffer.readString(6);
    assertTrue("klmnop="+t, t.equals("klmnop"));
    buffer.wipe();

    assertTrue("3", buffer.done());
    block.setReserved(0);

    assertTrue("4", !buffer.done());

    s = buffer.readString(4);
    assertTrue("qrst="+s, s.equals("qrst"));
    t = buffer.readString(4);
    assertTrue("uvwx="+t, t.equals("uvwx"));
    buffer.wipe();
                                              
    assertTrue("5", buffer.done());
  }

  public void testDataBuffer26_2_22_2() throws Exception {
    byte[] data = new String("abcdefghijklmnopqrstuvwxyz").getBytes();
    InputStream is = new ByteArrayInputStream(data);

    RecordBlock block = new InputStreamRecordBlock(is, 3);
    Charset charset = Charset.defaultCharset();

    RecordInput buffer = new FixedLengthByteRecordInput(block, charset);

    assertTrue("1", !buffer.done());
    String s = buffer.readString(2);
    assertTrue("ab="+s, s.equals("ab"));

    assertTrue("2", !buffer.done());
    s = buffer.readString(22);
    assertTrue("cdefghijklmnopqrstuvwx="+s, s.equals("cdefghijklmnopqrstuvwx"));
    buffer.wipe();

    assertTrue("3", !buffer.done());
    s = buffer.readString(2);
    assertTrue("yz="+s, s.equals("yz"));

    assertTrue("4", buffer.done());
  }

  public void testDataBuffer26_30() throws Exception {
    byte[] data = new String("abcdefghijklmnopqrstuvwxyz").getBytes();
    InputStream is = new ByteArrayInputStream(data);

    RecordBlock block = new InputStreamRecordBlock(is, 5);
    Charset charset = Charset.defaultCharset();

    RecordInput buffer = new FixedLengthByteRecordInput(block, charset);

    assertTrue("1", !buffer.done());
    String s = buffer.readString(30);
    assertTrue("abcdefghijklmnopqrstuvwxyz="+s, s.equals("abcdefghijklmnopqrstuvwxyz"));

    assertTrue("end", buffer.done());
  }

  public void testSpecialChar() throws Exception {
    byte[] data = new String("311111114John Smith").getBytes();
    data[0] = (byte)3;
    data[8] = (byte)4;
    InputStream is = new ByteArrayInputStream(data);

    RecordBlock block = new InputStreamRecordBlock(is, 8);
    Charset charset = Charset.defaultCharset();

    RecordInput buffer = new FixedLengthByteRecordInput(block, charset);

    int position = buffer.getPosition();

    assertTrue("position:  0 = " + position, position == 0);

    assertTrue("1", !buffer.done());
    byte[] value = new byte[1];
    int length  = buffer.readBytes(value);
    assertTrue("1 = " + length, value.length == 1);
    assertTrue("03 = " + value[0], value[0] == (byte)3);

    buffer.setPosition(position);
    assertTrue("1", !buffer.done());
    value = new byte[1];
    length  = buffer.readBytes(value);
    assertTrue("1 = " + length, value.length == 1);
    assertTrue("03 = " + value[0], value[0] == (byte)3);

    String s = buffer.readString(7);
    assertTrue("1111111"+s, s.equals("1111111"));

    position = buffer.getPosition();
    assertTrue("1", !buffer.done());
    value = new byte[1];
    length  = buffer.readBytes(value);
    assertTrue("1 = " + length, value.length == 1);
    assertTrue("04 = " + value[0], value[0] == (byte)4);
    buffer.setPosition(position);

    assertTrue("1", !buffer.done());
    value = new byte[1];
    length  = buffer.readBytes(value);
    assertTrue("1 = " + length, value.length == 1);
    assertTrue("04 = " + value[0], value[0] == (byte)4);

    assertTrue("1", !buffer.done());
    s = buffer.readString(20);
    assertTrue("John Smith"+s, s.equals("John Smith"));

    assertTrue("end", buffer.done());
  }

  public void testBookOrders() throws Exception {
    byte[] data = new String("CAuthor                        Title                              Price InvoiceNo InvoiceDate       FCharles Bukowski              Factotum                           22.95 001  12/Mar/2005            FJonathan Lethem               Gun, with Occasional Music         17.99 002  13/Mar/2005            FAndrew Crumey                 Mr Mee                             22.00 003 14/June/2005            CSteven John Metsker           Building Parsers with Java         39.95 004 15/June/2005            This is a trailer record                                                                            ").getBytes();
    InputStream is = new BufferedInputStream(new ByteArrayInputStream(data));

    RecordBlock block = new InputStreamRecordBlock(is, 512);
    Charset charset = Charset.defaultCharset();

    RecordInput buffer = new FixedLengthByteRecordInput(block, charset);
    block.setReserved(100);

    String s = buffer.readString(1).trim();
    assertTrue("C="+s,s.equals("C"));
    s = buffer.readString(30).trim();
    s = buffer.readString(30).trim();
    s = buffer.readString(10).trim();
    s = buffer.readString(4).trim();
    s = buffer.readString(13).trim();
    s = buffer.readString(12).trim();

    s = buffer.readString(1).trim();
    assertTrue("F="+s,s.equals("F"));
    s = buffer.readString(30).trim();
    s = buffer.readString(30).trim();
    s = buffer.readString(10).trim();
    s = buffer.readString(4).trim();
    s = buffer.readString(13).trim();
    s = buffer.readString(12).trim();

    s = buffer.readString(1).trim();
    assertTrue("F="+s,s.equals("F"));
    s = buffer.readString(30).trim();
    s = buffer.readString(30).trim();
    s = buffer.readString(10).trim();
    s = buffer.readString(4).trim();
    s = buffer.readString(13).trim();
    s = buffer.readString(12).trim();

    s = buffer.readString(1).trim();
    assertTrue("F="+s,s.equals("F"));
    s = buffer.readString(30).trim();
    s = buffer.readString(30).trim();
    s = buffer.readString(10).trim();
    s = buffer.readString(4).trim();
    s = buffer.readString(13).trim();
    s = buffer.readString(12).trim();

    s = buffer.readString(1).trim();
    assertTrue("C="+s,s.equals("C"));
    s = buffer.readString(30).trim();
    s = buffer.readString(30).trim();
    s = buffer.readString(10).trim();
    s = buffer.readString(4).trim();
    s = buffer.readString(13).trim();
    s = buffer.readString(12).trim();
    assertTrue("end body", buffer.done());
    block.setReserved(0);
    assertTrue("begin trailer", !buffer.done());
    s = buffer.readString(100).trim();
    assertTrue("This is a trailer record="+s,s.equals("This is a trailer record"));
    assertTrue("end", buffer.done());
  }
}




