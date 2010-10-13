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

package com.servingxml.ioc.resources;

import javax.xml.transform.sax.SAXTransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.servingxml.ioc.components.ComponentDictionary;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.DomHelper;

/**
 * Root configuration context.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class TopLevelConfigurationContext extends AbstractConfigurationContext {

  private final Document document;

  public TopLevelConfigurationContext(Document document, MutableNameTable nameTable, 
    ComponentDictionary componentDictionary,
  String base, ResourceTable resourceTable, Record parameters) {
    super(parameters, nameTable, componentDictionary, base, resourceTable, document.getDocumentElement());
    this.document = document;
  }

  public TopLevelConfigurationContext(AbstractConfigurationContext parent,
  Document document, ResourceTable resourceTable) {
    super(parent,document.getDocumentElement(),resourceTable);
    this.document = document; 
  }

  public Document getDocument() {
    return document;
  }

  public SAXTransformerFactory getTransformerFactory() {
    return getParent().getTransformerFactory();
  }
}

