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

public class InputStreamRecordBlock extends AbstractRecordBlock implements RecordBlock {
  protected static final int BLOCK_LENGTH = 512;
  private final int blockLength;

  private final InputStream is;

  public InputStreamRecordBlock(InputStream is) {
    super(new byte[BLOCK_LENGTH],0,0,Integer.MAX_VALUE);
    this.blockLength = BLOCK_LENGTH;
    this.is = is;
  }

  public InputStreamRecordBlock(InputStream is, int blockLength) {
    super(new byte[blockLength],0,0,Integer.MAX_VALUE);
    this.blockLength = blockLength;
    this.is = is;
  }

  // Preconditions:
  //    !done()                   
  protected void read(int n) throws IOException {
    if (capacity < maxCapacity()) {
      int maxBytes = ((n+reserved)/blockLength)*blockLength + blockLength;
     //System.out.println(getClass().getName()+".read Enter:  n=" + n + ", maxBytes=" + maxBytes+ ", capacity="+capacity+",maxCapacity="+maxCapacity);
      grow(maxBytes);
      int bytesRead = is.read(buffer,start+capacity,maxBytes);
      if (bytesRead >= 0) {
        capacity += bytesRead;
        if (bytesRead < maxBytes) {
          maxCapacity = capacity;
        }
      } else {
        maxCapacity = capacity;
      }
     //System.out.println(getClass().getName()+".read Leave:  n=" + n + ", maxBytes=" + maxBytes+", bytesRead="+bytesRead + ", capacity="+capacity+",maxCapacity="+maxCapacity);
    }
  }
}
