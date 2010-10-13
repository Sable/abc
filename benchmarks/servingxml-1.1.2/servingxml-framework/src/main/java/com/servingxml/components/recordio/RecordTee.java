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

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.ServingXmlException;

/**
 *
 * 
 * @author  Daniel A. Parker
 */


class RecordTee extends AbstractRecordFilter {
  private final RecordFilter recordFilter;

  public RecordTee(RecordFilter recordFilter) {
    this.recordFilter = recordFilter;
  }

  public void writeRecord(ServiceContext context, Flow flow) 
  {
    recordFilter.writeRecord(context, flow);
    getRecordWriter().writeRecord(context, flow);
  }

  public void startRecordStream(ServiceContext context, Flow flow) 
  {
    super.startRecordStream(context, flow);
    recordFilter.startRecordStream(context, flow);
  }

  public void endRecordStream(ServiceContext context, Flow flow) 
  {
    recordFilter.endRecordStream(context, flow);
    super.endRecordStream(context, flow);
  }

  public void close() {
    ServingXmlException badDispose = null;
    try {
      super.close();
    } catch (ServingXmlException e) {
      badDispose = e;
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(),e);
    }
    try {
      recordFilter.close();
    } catch (ServingXmlException e) {
      badDispose = e;
    } catch (Exception e) {
      badDispose = new ServingXmlException(e.getMessage(),e);
    }
    if (badDispose != null) {
      throw badDispose;
    }
  }
}

