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

package com.servingxml.components.flatfile.recordtype;

import java.io.IOException;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.StringHelper;
import com.servingxml.app.Flow;
import com.servingxml.components.flatfile.RecordInput;
import com.servingxml.components.flatfile.options.FlatFileOptions;
import com.servingxml.components.parameter.DefaultValue;
import com.servingxml.util.record.Record;
import com.servingxml.components.flatfile.options.Delimiter;
import com.servingxml.components.flatfile.options.DelimiterExtractor;

public class MultivaluedDelimitedFieldReader implements FlatRecordFieldReader {
  private final Name fieldName;
  private final int start;
  private final int maxFieldWidth;
  private final DefaultValue defaultValue;
  private final FlatFileOptions flatFileOptions;

  public MultivaluedDelimitedFieldReader(Name fieldName, int start, int maxFieldWidth, DefaultValue defaultValue, 
                                         FlatFileOptions flatFileOptions) {
    this.fieldName = fieldName;
    this.start = start;
    this.maxFieldWidth = maxFieldWidth;
    this.defaultValue = defaultValue;
    this.flatFileOptions = flatFileOptions;
  }

  public void readField(ServiceContext context, 
                        Flow flow,
                        final RecordInput recordInput, 
                        DelimiterExtractor[] recordDelimiters, 
                        int recordDelimiterStart, 
                        int recordDelimiterCount, 
                        int maxRecordWidth,
                        RecordBuilder recordBuilder) {

    try {

      //System.out.println(getClass().getName() + ".readField data=" + new String(data,start,length) + ".");

      int offset = flatFileOptions.rebaseIndex(start);
      if (offset >= 0) {
        recordInput.setPosition(offset);
      }

      String[] sa = recordInput.readStringArray(maxFieldWidth, flatFileOptions);
      if (sa == null || sa.length == 0 || (sa.length == 1 && sa[0].length() == 0)) {
        sa = defaultValue.evaluateStringArray(context,flow);
      } 
      if (sa != null) {
        for (int i = 0; i < sa.length; ++i) {
          sa[i] = StringHelper.trim(sa[i],flatFileOptions.isTrimLeadingWithinQuotes(),flatFileOptions.isTrimTrailingWithinQuotes());
        }
        recordBuilder.setStringArray(fieldName,sa);
      }
      
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public int getFixedEndPosition(Record parameters, Record currentRecord, int currentPosition) {
    return -1;
  }
}

