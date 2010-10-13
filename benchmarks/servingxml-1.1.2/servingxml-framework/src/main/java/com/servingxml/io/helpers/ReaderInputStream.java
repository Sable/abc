/**
 *  Copyright (c) 1997, 1998 James Clark
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  ``Software''), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED ``AS IS'', WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL JAMES CLARK BE LIABLE FOR ANY CLAIM, DAMAGES OR
 *  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 *  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *  
 *  Except as contained in this notice, the name of James Clark shall
 *  not be used in advertising or otherwise to promote the sale, use or
 *  other dealings in this Software without prior written authorization
 *  from James Clark.
 *   
 */ 

//package com.jclark.xml.sax;
package com.servingxml.io.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import com.servingxml.util.ServingXmlException;

/**
 * An InputStream of the UTF-16 encoding of a Reader.
 *
 * @version $Revision: 1.1 $ $Date: 2000/01/26 04:32:11 $
 */
public class ReaderInputStream extends InputStream {
  private Reader reader;
  private static final int BUF_SIZE = 4096;
  private char[] buf = new char[BUF_SIZE];
  private int bufIndex = 0;
  private int bufEnd = 1;
  /* true if we have read the first nibble of the character at bufIndex
     but not yet read the second */
  private boolean nibbled = false;
  private static final Charset charset = Charset.forName("UTF-16");

  public ReaderInputStream(Reader reader) {
    this.reader = reader;
    buf[0] = '\ufeff';
  }

  public synchronized int read() throws IOException {
    if (nibbled) {
      nibbled = false;
      return buf[bufIndex++] & 0xff;
    }
    while (bufIndex == bufEnd) {
      bufIndex = 0;
      bufEnd = reader.read(buf, 0, buf.length);
      if (bufEnd < 0) {
        bufEnd = 0;
        return -1;
      }
    }
    nibbled = true;
    return buf[bufIndex] >> 8;
  }

  public synchronized int read(byte b[], int off, int len) throws IOException {
    if (len <= 0)
      return 0;
    int startOff = off;
    if (nibbled) {
      nibbled = false;
      if (b != null)
        b[off] = (byte)(buf[bufIndex] & 0xff);
      bufIndex++;
      off++;
      len--;
    }
    while (len > 0) {
      if (bufIndex == bufEnd) {
        bufIndex = 0;
        bufEnd = reader.read(buf, 0, buf.length);
        if (bufEnd < 0) {
          bufEnd = 0;
          if (off != startOff)
            break;
          return -1;
        }
        if (bufEnd == 0)
          return off - startOff;
      }
      if (len == 1) {
        if (b != null)
          b[off] = (byte)(buf[bufIndex] >> 8);
        off++;
        nibbled = true;
        break;
      }
      if (b != null) {
        b[off++] = (byte)(buf[bufIndex] >> 8);
        b[off++] = (byte)(buf[bufIndex] & 0xff);
      } else
        off += 2;
      len -= 2;
      bufIndex++;
    }
    return off - startOff;
  }

  public Charset getCharset() {
    return charset;
  }

  public synchronized void close() {
    ServingXmlException badDispose = null;
    try {
      reader.close();
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(),e);
    }
    if (badDispose != null) {
      throw badDispose;
    }
  }
}
