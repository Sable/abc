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

import java.nio.charset.Charset;

import java.io.IOException;
import java.util.List;

import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.flatfile.options.DelimiterExtractor;
import com.servingxml.components.flatfile.FlatRecordReader;
import com.servingxml.components.flatfile.FlatRecordReceiver;
import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.RecordReceiver;

public interface RecordInput {
  byte[] toByteArray();
  char[] toCharArray();
  boolean done();
  int readBytes(byte[] bytes) throws IOException;
  String readString(int length) throws IOException;
  String readString(int maxLength, FlatFileOptions flatFileOptions)
  throws IOException;               

  String[] readStringArray(int maxLength, FlatFileOptions flatFileOptions) 
  throws IOException;

  int getPosition();
  int getLast();
  void setPosition(int index) throws IOException;
  Charset getCharset();
  void wipe() throws IOException;

  void readRepeatingGroup2(ServiceContext context, 
                           Flow flow, 
                           int count, 
                           FlatFileOptions flatFileOptions,
                           DelimiterExtractor[] recordDelimiters, 
                           int recordDelimiterStart, 
                           int recordDelimiterCount, 
                           int maxRecordWidth,
                           FlatRecordReader flatRecordReader,
                           RecordReceiver recordReceiver);

  RecordInput readSegment(FlatFileOptions flatFileOptions);

  RecordInput readSegment(int segmentLength);

  RecordInput concatenate(RecordInput lhs);

  RecordInput concatenate(RecordInput lhs, int beginIndex);

  String toString();

  int length();

  int start();
}

