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

package com.servingxml.components.choose;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ExtendedContentHandler;
import com.servingxml.components.recordmapping.MapXml;
import com.servingxml.components.recordmapping.GroupState;
import com.servingxml.util.xml.XsltChooser;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class AlternativeSimpleMapXmlContainer implements MapXml {
  private final MapXml[] recordMaps;
  private final XsltChooser chooser;
  private MapXml selection;

  public AlternativeSimpleMapXmlContainer(MapXml[] recordMaps, XsltChooser chooser) {
    this.recordMaps = recordMaps;
    this.chooser = chooser;
    this.selection = null;
  }

  public void writeRecord(ServiceContext context, Flow flow, 
                          Record previousRecord, Record nextRecord, 
                          ExtendedContentHandler handler, GroupState groupListener) {

    Source source = new SAXSource(flow.getDefaultSaxSource().createXmlReader(),new InputSource());
    int index = chooser.choose(source, flow.getParameters());
    if (index >= 0 && index < recordMaps.length) {
      MapXml recordMap = recordMaps[index];
      if (recordMap != null) {
        recordMap.writeRecord(context, flow,previousRecord,nextRecord,handler,groupListener);
      }
    }
  }

  public void groupStarted(ServiceContext context, Flow flow, 
                             Record previousRecord, Record nextRecord, 
                             ExtendedContentHandler handler, Record variables) {

    Source source = new SAXSource(flow.getDefaultSaxSource().createXmlReader(),new InputSource());
    int index = chooser.choose(source, flow.getParameters());
    if (index >= 0 && index < recordMaps.length) {
      MapXml recordMap = recordMaps[index];
      if (recordMap != null) {
        recordMap.groupStarted(context, flow,previousRecord,nextRecord,handler,variables);
        recordMap.groupStopped(context, flow,handler);
      }
    }
  }

  public void groupStopped(ServiceContext context, Flow flow, ExtendedContentHandler handler) {
    //for (int i = 0; i < recordMaps.length; ++i) {
    //  recordMaps[i].groupStopped(context, flow,handler);
    //}
  }

  public void addToAttributes(ServiceContext context, Flow flow, Record variables, 
                              AttributesImpl attributes) {
    //System.out.println(getClass().getName()+".addToAttributes Enter");

    Source source = new SAXSource(flow.getDefaultSaxSource().createXmlReader(),new InputSource());
    int index = chooser.choose(source, flow.getParameters());
    if (index >= 0 && index < recordMaps.length) {
      MapXml recordMap = recordMaps[index];
      if (recordMap != null) {
        recordMap.addToAttributes(context, flow, variables, attributes);
      }
    }
    //System.out.println(getClass().getName()+".addToAttributes Leave found = "+found);
  }

  public boolean isGrouping() {
    return selection == null ? false : selection.isGrouping();
  }

  public void flush(ServiceContext context, ExtendedContentHandler handler, GroupState groupListener) {
  }
}

