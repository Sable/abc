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

package com.servingxml.components.flatfile.options;

import java.io.IOException;
import java.nio.charset.Charset;

import com.servingxml.components.flatfile.options.ByteBuffer;
import com.servingxml.util.ByteArrayBuilder;
import com.servingxml.util.ByteArrayHelper;
import com.servingxml.util.CharsetHelper;
import com.servingxml.util.ServingXmlException;
import com.servingxml.components.flatfile.RecordOutput;

public abstract class ByteTrimmer {
  private static char[] spaceChars = {' '};
  private static char[] tabChars = {'\t'};
  private static char[] ffChars = {'\f'};
  private static char[] crChars = {'\r'};
  private static char[] lfChars = {'\n'};

  public abstract void writeTo(RecordOutput recordOutput);

  public abstract boolean checkSpace(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder)
  throws IOException;

  public abstract boolean checkWhitespace(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder)
  throws IOException;

  public abstract int countLeadingWhitespace(byte[] data, int start, int length);

  public boolean isAllWhitespace(byte[] data, int start, int length) {
    int count = countLeadingWhitespace(data, start, length);
    return count == length;
  }

  public abstract int countTrailingWhitespace(byte[] data, int start, int length);

  public static ByteTrimmer newInstance(Charset charset) {
    byte[] spaceBytes = CharsetHelper.charactersToBytes(spaceChars, charset);
    byte[] tabBytes = CharsetHelper.charactersToBytes(tabChars, charset);
    byte[] ffBytes = CharsetHelper.charactersToBytes(ffChars, charset);
    byte[] crBytes = CharsetHelper.charactersToBytes(crChars, charset);
    byte[] lfBytes = CharsetHelper.charactersToBytes(lfChars, charset);

    ByteTrimmer checker;
    if (spaceBytes.length == 1 && tabBytes.length == 1 && 
        ffBytes.length == 1 && crBytes.length == 1 && lfBytes.length == 1) {
      checker = new WhitespaceChecker1(spaceBytes[0], tabBytes[0], ffBytes[0], crBytes[0], lfBytes[0]);
    } else {
      checker = new WhitespaceCheckerN(spaceBytes, tabBytes, ffBytes, crBytes, lfBytes);
    }

    return checker;
  }

  static class WhitespaceCheckerN extends ByteTrimmer {
    private final byte[] spaceBytes;
    private final byte[] tabBytes;
    private final byte[] ffBytes;
    private final byte[] crBytes;
    private final byte[] lfBytes;

    public WhitespaceCheckerN(byte[] spaceBytes, byte[] tabBytes, byte[] ffBytes,
                              byte[] crBytes, byte[] lfBytes) {
      this.spaceBytes = spaceBytes;
      this.tabBytes = tabBytes;
      this.ffBytes = ffBytes;
      this.crBytes = crBytes;
      this.lfBytes = lfBytes;
    }

    public final void writeTo(RecordOutput recordOutput) {
      recordOutput.writeBytes(spaceBytes);
    }

    public final boolean checkSpace(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
    throws IOException {

      int startPosition = byteArrayBuilder.length();
      boolean done = false;
      while (!done && !recordBuffer.done()) {
        boolean white1 = recordBuffer.startsWith(spaceBytes);
        if (white1) {
          byteArrayBuilder.append(spaceBytes);
          recordBuffer.next(spaceBytes.length);
        } else {
          boolean white2 = recordBuffer.startsWith(tabBytes);
          if (white2) {
            byteArrayBuilder.append(tabBytes);
            recordBuffer.next(tabBytes.length);
          } else {
            done = true;
          }
        }
      }

      return byteArrayBuilder.length() > startPosition;
    }

    public final boolean checkWhitespace(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
    throws IOException {

      int startPosition = byteArrayBuilder.length();
      boolean done = false;
      while (!done && !recordBuffer.done()) {
        boolean white1 = recordBuffer.startsWith(spaceBytes);
        if (white1) {
          byteArrayBuilder.append(spaceBytes);
          recordBuffer.next(spaceBytes.length);
        } else {
          boolean white2 = recordBuffer.startsWith(tabBytes);
          if (white2) {
            byteArrayBuilder.append(tabBytes);
            recordBuffer.next(tabBytes.length);
          } else {
            boolean white3 = recordBuffer.startsWith(ffBytes);
            if (white3) {
              byteArrayBuilder.append(ffBytes);
              recordBuffer.next(ffBytes.length);
            } else {
              boolean white4 = recordBuffer.startsWith(crBytes);
              if (white4) {
                byteArrayBuilder.append(crBytes);
                recordBuffer.next(crBytes.length);
              } else {
                boolean white5 = recordBuffer.startsWith(lfBytes);
                if (white5) {
                  byteArrayBuilder.append(lfBytes);
                  recordBuffer.next(lfBytes.length);
                } else {
                  done = true;
                }
              }
            }
          }
        }
      }

      return byteArrayBuilder.length() > startPosition;
    }

    public final int countLeadingWhitespace(byte[] data, int start, int length) {

      int index = 0;
      boolean done = false;
      while (!done && index < length) {
        int white1 = ByteArrayHelper.startsWith(data, start+index, length-index, spaceBytes);
        if (white1 > 0) {
          index += white1;
        } else {
          int white2 = ByteArrayHelper.startsWith(data, start+index, length-index, tabBytes);
          if (white2 > 0) {
            index += white2;
          } else {
            int white3 = ByteArrayHelper.startsWith(data, start+index, length-index, ffBytes);
            if (white3 > 0) {
              index += white3;
            } else {
              int white4 = ByteArrayHelper.startsWith(data, start+index, length-index, crBytes);
              if (white4 > 0) {
                index += white4;
              } else {
                int white5 = ByteArrayHelper.startsWith(data, start+index, length-index, lfBytes);
                if (white5 > 0) {
                  index += white5;
                } else {
                  done = true;
                }
              }
            }
          }
        }
      }

      return index;
    }

    public final int countTrailingWhitespace(byte[] data, int start, int length) {

      int index = length;
      boolean done = false;
      while (!done && index > 0) {
        int white1 = ByteArrayHelper.startsWith(data, start+index-spaceBytes.length, spaceBytes.length, spaceBytes);
        if (white1 > 0) {
          index -= white1;
        } else {
          int white2 = ByteArrayHelper.startsWith(data, start+index-tabBytes.length, tabBytes.length, tabBytes);
          if (white2 > 0) {
            index -= white2;
          } else {
            int white3 = ByteArrayHelper.startsWith(data, start+index-ffBytes.length, ffBytes.length, ffBytes);
            if (white3 > 0) {
              index -= white3;
            } else {
              int white4 = ByteArrayHelper.startsWith(data, start+index-crBytes.length, crBytes.length, crBytes);
              if (white4 > 0) {
                index -= white4;
              } else {
                int white5 = ByteArrayHelper.startsWith(data, start+index-lfBytes.length, lfBytes.length, lfBytes);
                if (white5 > 0) {
                  index -= white5;
                } else {
                  done = true;
                }
              }
            }
          }
        }
      }

      return index;
    }
  }

  static class WhitespaceChecker1 extends ByteTrimmer {
    private final byte spaceByte;
    private final byte tabByte;
    private final byte ffByte;
    private final byte crByte;
    private final byte lfByte;

    public WhitespaceChecker1(byte spaceByte, byte tabByte, byte ffByte, byte crByte, byte lfByte) {
      this.spaceByte = spaceByte;
      this.tabByte = tabByte;
      this.ffByte = ffByte;
      this.crByte = crByte;
      this.lfByte = lfByte;
    }

    public final void writeTo(RecordOutput recordOutput) {
      recordOutput.writeByte(spaceByte);
    }

    public final boolean checkSpace(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
    throws IOException {

      //System.out.println("WhitespaceChecker1.checkSpace");
      boolean found = false;
      boolean done = false;
      while (!done && !recordBuffer.done()) {
        byte b = recordBuffer.current();
        //System.out.print(new String(new byte[]{b}) + ",");
        boolean result = (b == spaceByte || b == tabByte);
        if (result) {
          byteArrayBuilder.append(b);
          recordBuffer.next();
          found = true;
        } else {
          done = true;
        }
      }
      return found;
    }

    public final boolean checkWhitespace(ByteBuffer recordBuffer, ByteArrayBuilder byteArrayBuilder) 
    throws IOException {
      boolean found = false;
      boolean done = false;
      while (!done && !recordBuffer.done()) {
        byte b = recordBuffer.current();
        boolean result = (b == spaceByte || b == tabByte || b == ffByte || b == crByte || b == lfByte);
        if (result) {
          byteArrayBuilder.append(b);
          recordBuffer.next();
          found = true;
        } else {
          done = true;
        }
      }
      return found;
    }

    public final int countLeadingWhitespace(byte[] data, int start, int length) {

      int index = 0;
      boolean done = false;
      while (!done && index < length) {
        byte b = data[start+index];
        if (b == spaceByte || b == tabByte || b == ffByte || b == crByte || b == lfByte) {
          ++index;
        } else {
          done = true;
        }
      }

      return index;
    }

    public final int countTrailingWhitespace(byte[] data, int start, int length) {

      int index = length;
      boolean done = false;
      while (!done && index > 0) {
        byte b = data[start+index-1];
        if (b == spaceByte || b == tabByte || b == ffByte || b == crByte || b == lfByte) {
          --index;
        } else {
          done = true;
        }
      }

      return length - index;
    }
  }

}
