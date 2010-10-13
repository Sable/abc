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

package com.servingxml.io.helpers; 

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Reader;
import java.lang.IllegalArgumentException;
import java.nio.charset.Charset;

import com.servingxml.io.streamsource.DefaultJaxpStreamSource;
import com.servingxml.io.streamsource.InputStreamSourceAdaptor;
import com.servingxml.io.streamsource.StringStreamSource;
import com.servingxml.io.streamsource.url.UrlSource;
import com.servingxml.util.ServingXmlException;

public final class InputStreamHelper {

  private InputStreamHelper() {
  }

  public static Charset skipBOM(BufferedInputStream bufferedIn, Charset sourceCharset) {
    try {
      // http://opensource.adobe.com/svn/opensource/blazeds/trunk/modules/proxy/src/flex/messaging/services/http/proxy/ResponseFilter.java
      String charsetName = null;
      if (sourceCharset == null || sourceCharset.name().startsWith("UTF")) {
        bufferedIn.mark(4);
        // Check for BOM as InputStreamReader does not strip BOM in all cases.
        boolean hasBOM = false;
        int read = bufferedIn.read();
        if (read > 0) {
          // UTF-8 BOM is EF BB BF
          if (0xEF == (read & 0xFF)) {
            read = bufferedIn.read();
            if (0xBB == (read & 0xFF)) {
              read = bufferedIn.read();
              if (0xBF == (read & 0xFF)) {
                hasBOM = true;
                charsetName = "UTF-8";
              }
            }
          }
          // UTF-16 Little Endian BOM is FF FE
          // UTF-32 Little Endian BOM is FF FE 00 00 
          else if (0xFF == (read & 0xFF)) {
            read = bufferedIn.read();
            if (0xFE == (read & 0xFF)) {
              hasBOM = true;
              charsetName = "UTF16-LE";

              // Check two more bytes incase we have UTF-32
              bufferedIn.mark(2);
              read = bufferedIn.read();
              if (0x00 == (read & 0xFF)) {
                read = bufferedIn.read();
                if (0x00 == (read & 0xFF)) {
                  charsetName = "UTF32-LE";
                } else {
                  bufferedIn.reset();
                }
              } else {
                bufferedIn.reset();
              }
            }
          }
          // UTF-16 Big Endian BOM is FE FF
          else if (0xFE == (read & 0xFF)) {
            read = bufferedIn.read();
            if (0xFF == (read & 0xFF)) {
              hasBOM = true;
              charsetName = "UTF16-BE";
            }
          }
          // UTF-32 Big Endian BOM is 00 00 FE FF 
          else if (0x00 == (read & 0xFF)) {
            read = bufferedIn.read();
            if (0x00 == (read & 0xFF)) {
              read = bufferedIn.read();
              if (0xFE == (read & 0xFF)) {
                read = bufferedIn.read();
                if (0xFF == (read & 0xFF)) {
                  hasBOM = true;
                  charsetName = "UTF32-BE";
                }
              }
            }
          }

          // If we didn't find a BOM, all bytes should contribute to the content
          if (!hasBOM) {
            bufferedIn.reset();
          }
        }
      }
      return (sourceCharset != null || charsetName == null) ? sourceCharset : Charset.forName(charsetName);
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (IllegalArgumentException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }
}
