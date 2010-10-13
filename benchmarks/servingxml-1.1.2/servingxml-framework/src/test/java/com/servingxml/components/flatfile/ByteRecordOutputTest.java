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

package com.servingxml.components.flatfile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import com.servingxml.components.flatfile.options.ByteBuffer;

public class ByteRecordOutputTest extends TestCase {

  public ByteRecordOutputTest(String name) {
    super(name);
  }

  public void testByteRecordOutput() throws Exception {
    byte padByte = " ".getBytes()[0];
    RecordOutput recordOutput = new ByteRecordOutput(Charset.defaultCharset(), padByte);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    recordOutput.clear();
    recordOutput.writeString("ABW");
    recordOutput.writeString(",");
    recordOutput.writeString("ARUBA");
    recordOutput.writeString("\r\n");
    byte[] data = recordOutput.toByteArray();
    os.write(data, 0, data.length);
  
    recordOutput.clear();
    recordOutput.writeString("ADH");
    recordOutput.writeString(",");
    recordOutput.writeString("UNITED ARAB EMIRATES");
    recordOutput.writeString("\r\n");
    data = recordOutput.toByteArray();
    os.write(data, 0, data.length);

    recordOutput.clear();
    recordOutput.writeString("AGO");
    recordOutput.writeString(",");
    recordOutput.writeString("ANGOLA");
    recordOutput.writeString("\r\n");
    data = recordOutput.toByteArray();
    os.write(data, 0, data.length);

    String s = os.toString();
    System.out.println(s);
  }
}




