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

import java.io.Reader;
import java.io.IOException;

public class ReaderRecordBlock extends AbstractRecordBlock implements RecordBlock {
  protected static final int BLOCK_LENGTH = 512;
  private final int blockLength;

  private final Reader is;

  public ReaderRecordBlock(Reader is) {
    super(new char[BLOCK_LENGTH],0,0,Integer.MAX_VALUE);
    this.blockLength = BLOCK_LENGTH;
    this.is = is;
  }

  public ReaderRecordBlock(Reader is, int blockLength) {
    super(new char[blockLength],0,0,Integer.MAX_VALUE);
    this.blockLength = blockLength;
    this.is = is;
  }

  // Preconditions:
  //    !done()                   
  protected void read(int n) throws IOException {
    if (capacity < maxCapacity()) {
      int maxChars = ((n+reserved)/blockLength)*blockLength + blockLength;
     //System.out.println(getClass().getName()+".read Enter:  n=" + n + ", maxChars=" + maxChars+ ", capacity="+capacity+",maxCapacity="+maxCapacity);
      grow(maxChars);
      int bytesRead = is.read(buffer,start+capacity,maxChars);
      if (bytesRead >= 0) {
        capacity += bytesRead;
        if (bytesRead < maxChars) {
          maxCapacity = capacity;
        }
      } else {
        maxCapacity = capacity;
      }
     //System.out.println(getClass().getName()+".read Leave:  n=" + n + ", maxChars=" + maxChars+", bytesRead="+bytesRead + ", capacity="+capacity+",maxCapacity="+maxCapacity);
    }
  }
}
