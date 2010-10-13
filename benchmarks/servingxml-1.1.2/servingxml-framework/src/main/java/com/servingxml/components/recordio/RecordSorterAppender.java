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

package com.servingxml.components.recordio;

import java.util.Comparator;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;

/**
 *
 * 
 * @author  Daniel A. Parker
 */

public class RecordSorterAppender extends AbstractRecordFilterAppender     
implements RecordFilterAppender {
  private final Comparator comparator;

  public RecordSorterAppender(Comparator comparator) {
    this.comparator = comparator;
  }

  public void appendToRecordPipeline(ServiceContext context, Flow flow,
                                     RecordFilterChain pipeline) {

    RecordSorter sorter = new RecordSorter(comparator);
    pipeline.addRecordFilter(sorter);
  }
}

