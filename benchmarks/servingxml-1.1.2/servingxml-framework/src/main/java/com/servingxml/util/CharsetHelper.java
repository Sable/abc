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

package com.servingxml.util;

import java.nio.charset.Charset;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;

public abstract class CharsetHelper {

  private static Charset defaultCharset = null;

  private CharsetHelper() {
  }

  public static byte[] stringToBytes(String s) {
    Charset charset = getDefaultCharset();
    return stringToBytes(s,charset);
  }

  public static byte[] stringToBytes(String s, String encoding) {
    Charset charset = Charset.forName(encoding);
    return stringToBytes(s,charset);
  }

  public static byte[] stringToBytes(String s, Charset charset) {
    byte[] bytes;

    if (charset == null) {
      bytes = s.getBytes();
    } else {
      CharBuffer cb = CharBuffer.wrap(s);
      ByteBuffer bb = charset.encode(cb); 
      bytes = new byte[bb.limit()];
      bb.get(bytes);
    }

    return bytes;
  }

  public static byte[] charactersToBytes(char[] characters, Charset charset) {
    if (charset == null) {
      charset = getDefaultCharset();
    }
    CharBuffer cb = CharBuffer.wrap(characters);
    ByteBuffer bb = charset.encode(cb); 
    byte[] bytes = new byte[bb.limit()];
    bb.get(bytes);

    return bytes;
  }

  public static byte[] charactersToBytes(char[] characters, int start, int length, Charset charset) {
    if (charset == null) {
      charset = getDefaultCharset();
    }
    CharBuffer cb = CharBuffer.wrap(characters, start, length);
    ByteBuffer bb = charset.encode(cb); 
    byte[] bytes = new byte[bb.limit()];
    bb.get(bytes);

    return bytes;
  }

  public static char[] bytesToCharacters(byte[] bytes, Charset charset) {
    if (charset == null) {
      charset = getDefaultCharset();
    }
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    CharBuffer cb = charset.decode(bb);
    char[] characters = new char[cb.limit()];
    cb.get(characters);

    return characters;
  }

  public static char[] bytesToCharacters(byte[] bytes, int start, int length, Charset charset) {
    if (charset == null) {
      charset = getDefaultCharset();
    }
    ByteBuffer bb = ByteBuffer.wrap(bytes, start, length);
    CharBuffer cb = charset.decode(bb);
    char[] characters = new char[cb.limit()];
    cb.get(characters);

    return characters;
  }

  public static String bytesToString(byte[] bytes, Charset charset) {
    char[] characters;
    if (charset == null) {
      charset = getDefaultCharset();
    }
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    CharBuffer cb = charset.decode(bb);

    return cb.toString();
  }

  public static String bytesToString(byte[] bytes, int start, int length, Charset charset) {
    char[] characters;
    if (charset == null) {
      charset = getDefaultCharset();
    }
    ByteBuffer bb = ByteBuffer.wrap(bytes, start, length);
    CharBuffer cb = charset.decode(bb);

    return cb.toString();
  }

  public static synchronized Charset getDefaultCharset() {
    if (defaultCharset == null) {
      String name = new OutputStreamWriter(System.out).getEncoding();
      defaultCharset = Charset.forName(name);
    }
    return defaultCharset;
  }
}
