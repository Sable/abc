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

package com.servingxml.components.string;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.datatype.DatatypeFactory;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;

import com.servingxml.app.ServiceContext;
import com.servingxml.util.record.Record;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;

/**
 * The <code>LineNumber</code> class implements the <code>StringFactory</code> interface.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class LineNumber implements StringFactory {

  public LineNumber() {
  }

  public String createString(ServiceContext context, Flow flow) {
    int lineNumber = flow.getCurrentLineNumber();
    //System.out.println(getClass().getName()+".writeRecord "+Integer.toString(flow.getCurrentLineNumber()));
    return lineNumber >= 0 ? Integer.toString(lineNumber) : "";
  }

  public void createString(ServiceContext context, Flow flow, StringBuilder stringBuilder) {
    String s = createString(context,flow);
    stringBuilder.append(s);
  }
}

