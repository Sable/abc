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
import java.net.URL;

import com.servingxml.components.flatfile.options.ByteBuffer;
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.HexBin;

import junit.framework.TestCase;

public class BinaryFileTest extends TestCase {

  public BinaryFileTest(String name) {
    super(name);
  }

  public void testRecordBuffer() throws Exception {
    //String filename = "hot.txt";
    //URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    //assertTrue("" + filename, url != null);
    //InputStream is = url.openStream();

    InputStream is = getClass().getResourceAsStream( "/G6744V00.smf" );
    ByteArrayBuilder builder = new ByteArrayBuilder();

    boolean done = false;
    byte[] bytes = new byte[7682];
    while (!done) {
      int x = is.read(bytes);
      if (x == -1) {
        done = true;
      } else {
        String s = HexBin.encode(bytes,0,x);
        System.out.println(s);
      }
    }

    is.close();
  }
}




