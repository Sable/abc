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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

import com.servingxml.components.flatfile.options.ByteBuffer;

public class ByteBufferTest extends TestCase {

  public ByteBufferTest(String name) {
    super(name);
  }

  public void testRecordBuffer() throws Exception {
    //String filename = "hot.txt";
    //URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    //assertTrue("" + filename, url != null);
    //InputStream is = url.openStream();

    InputStream is = getClass().getResourceAsStream( "/hot.txt" );

    ByteBuffer buffer = new ByteBufferImpl(is);
    while (!buffer.done()) {
      buffer.next();
      if (!buffer.done()) {
        byte[] prefix = new String("BOH03").getBytes();
        if (buffer.startsWith(prefix)) {
          //System.out.println ();
        }
        //System.out.print(new String(new byte[]{buffer.current()}));
      }
    }
    //System.out.println ();
    is.close();
  }

  public void testRecordBuffer2() throws Exception {
    //String filename = "hot.txt";
    //URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    //assertTrue("" + filename, url != null);

    //InputStream is = url.openStream();
    InputStream is = getClass().getResourceAsStream( "/hot.txt" );

    ByteBuffer buffer = new ByteBufferImpl(is);
    while (!buffer.done()) {
      buffer.next(1);
      if (!buffer.done()) {
        byte[] prefix = new String("BOH03").getBytes();
        if (buffer.startsWith(prefix)) {
          //System.out.println ();
        }
        //System.out.print(new String(new byte[]{buffer.current()}));
      }
    }
    //System.out.println ();
    is.close();
  }

  public void testNewLine() throws Exception {
    //String filename = "hot.txt";
    //URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    //assertTrue("" + filename, url != null);

    //InputStream is = url.openStream();
    //System.out.println ();
    InputStream is = getClass().getResourceAsStream( "/hot.txt" );

    ByteBuffer buffer = new ByteBufferImpl(is);
    while (!buffer.done()) {
      buffer.next();
      if (!buffer.done()) {
        byte[] recordDelimiter = new String("\n").getBytes();
        if (buffer.startsWith(recordDelimiter)) {
          buffer.next(recordDelimiter.length);
          //System.out.print("#");
          buffer.clear();
        } else {
          //System.out.print(new String(new byte[]{buffer.current()}));
        }
      }
    }
    //System.out.println ();
    is.close();
  }

  public void testPositionMovement() throws Exception {
    byte[] data = new String("bcdefghi").getBytes();
    InputStream is = new ByteArrayInputStream(data);

    ByteBuffer buffer = new ByteBufferImpl(is);
    assertTrue("-1=="+buffer.getPosition(), buffer.getPosition() == -1);
    buffer.next();
    assertTrue("0=="+buffer.getPosition(), buffer.getPosition() == 0);
    assertTrue("b=="+ new String(new byte[]{buffer.current()}), buffer.current() == data[0]);

    for (int i = 0; !buffer.done() && i < 20; ++i) {
      //System.out.println ("before: position = " + buffer.getPosition() + ", length = " + buffer.length());
      buffer.next();
      //System.out.println ("after: position = " + buffer.getPosition() + ", length = " + buffer.length());
    }
    assertTrue("8==" + buffer.getPosition(),buffer.getPosition() == 8);
  }

  public void testPositionMovement3() throws Exception {
    byte[] data = new String("bcdefghi").getBytes();
    InputStream is = new ByteArrayInputStream(data);

    ByteBuffer buffer = new ByteBufferImpl(is);
    assertTrue("-1=="+buffer.getPosition(), buffer.getPosition() == -1);
    buffer.next();
    assertTrue("0=="+buffer.getPosition(), buffer.getPosition() == 0);
    assertTrue("b=="+ new String(new byte[]{buffer.current()}), buffer.current() == data[0]);

    for (int i = 0; !buffer.done() && i < 20; ++i) {
      buffer.next(3);
    }
    assertTrue("8==" + buffer.getPosition(),buffer.getPosition() == 8);
  }

  public void testPositionMovement9() throws Exception {
    byte[] data = new String("bcdefghi").getBytes();
    InputStream is = new ByteArrayInputStream(data);

    ByteBuffer buffer = new ByteBufferImpl(is);
    assertTrue("-1=="+buffer.getPosition(), buffer.getPosition() == -1);
    buffer.next();
    assertTrue("0=="+buffer.getPosition(), buffer.getPosition() == 0);
    assertTrue("b=="+ new String(new byte[]{buffer.current()}), buffer.current() == data[0]);

    for (int i = 0; !buffer.done() && i < 20; ++i) {
      buffer.next(9);
    }
    assertTrue("8==" + buffer.getPosition(),buffer.getPosition() == 8);
  }

  public void testReserved() throws Exception {
    byte[] data = new String("abcdefghij").getBytes();
    InputStream is = new ByteArrayInputStream(data,1,8);
    ByteBuffer buffer = new ByteBufferImpl(is);

    buffer.setReserved(6);

    for (int i = 0; !buffer.done() && i < 20; ++i) {
      buffer.next();
    }
    assertTrue("2==" + buffer.getPosition(),buffer.getPosition() == 2);
  }

  public void testReserved2() throws Exception {
    byte[] data = new String("abcdefghijkl").getBytes();
    InputStream is = new ByteArrayInputStream(data);

    ByteBuffer buffer = new ByteBufferImpl(is);

    buffer.setReserved(6);

    buffer.next(3);
    assertTrue("2="+buffer.getPosition(), buffer.getPosition() == 2);
    assertTrue("!done (2)", !buffer.done());

    buffer.next(3);
    assertTrue("5="+buffer.getPosition(), buffer.getPosition() == 5);
    assertTrue("!done (5)", !buffer.done());

    buffer.next(3);
    assertTrue("6="+buffer.getPosition(), buffer.getPosition() == 6);
    assertTrue("done (after 5)", buffer.done());

    buffer.setReserved(0);
    //System.out.println ("before clear:  position = " + buffer.getPosition() + ", length = " + buffer.length() + ", maxLength = " + buffer.maxLength());
    buffer.clear();
    //System.out.println ("after clear:  position = " + buffer.getPosition() + ", length = " + buffer.length() + ", maxLength = " + buffer.maxLength());
    buffer.next(3);
    assertTrue("2="+buffer.getPosition(), buffer.getPosition() == 2);
    assertTrue("!done (after clear)", !buffer.done());

    buffer.next(3);
    assertTrue("5="+buffer.getPosition(), buffer.getPosition() == 5);
    assertTrue("!done (second time after clear)", !buffer.done());

    buffer.next(3);
    assertTrue("done (finally)", buffer.done());
  }
}




