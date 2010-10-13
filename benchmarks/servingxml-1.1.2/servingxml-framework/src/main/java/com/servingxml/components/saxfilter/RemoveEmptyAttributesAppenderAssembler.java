/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file attributeTest in compliance with the License. 
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

package com.servingxml.components.saxfilter;

import org.xml.sax.XMLFilter;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.NameTest;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;
import com.servingxml.components.content.Content;

public class RemoveEmptyAttributesAppenderAssembler {
  
  private String elementQnames = "";
  private String attributeQnames = "";
  private String exceptElementQnames = "";
  private String exceptAttributeQnames = "";

  public void setElements(String elementQnames) {
    this.elementQnames = elementQnames;
  }

  public void setAttributes(String attributeQnames) {
    this.attributeQnames = attributeQnames;
  }

  public void setExceptElements(String exceptElementQnames) {
    this.exceptElementQnames = exceptElementQnames;
  }

  public void setExceptAttributes(String exceptAttributeQnames) {
    this.exceptAttributeQnames = exceptAttributeQnames;
  }

  public Content assemble(ConfigurationContext context) {

    NameTest elementTest = NameTest.parse(context.getQnameContext(), elementQnames);
    NameTest attributeTest = NameTest.parse(context.getQnameContext(), attributeQnames);
    NameTest exceptElementTest = NameTest.parse(context.getQnameContext(), exceptElementQnames);
    NameTest exceptAttributeTest = NameTest.parse(context.getQnameContext(), exceptAttributeQnames);
    Content filterFactory = new RemoveEmptyAttributesAppender(elementTest,exceptElementTest,attributeTest,exceptAttributeTest);
    return filterFactory;
  }
}

class RemoveEmptyAttributesAppender extends AbstractXmlFilterAppender implements Content {
  private final NameTest elementTest;
  private final NameTest exceptElementTest;
  private final NameTest attributeTest;
  private final NameTest exceptAttributeTest;

  public RemoveEmptyAttributesAppender(NameTest elementTest, NameTest exceptElementTest,
                                       NameTest attributeTest, NameTest exceptAttributeTest) {
    this.elementTest = elementTest;
    this.exceptElementTest = exceptElementTest;
    this.attributeTest = attributeTest;
    this.exceptAttributeTest = exceptAttributeTest;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
  XmlFilterChain pipeline) {
    XMLFilter filter = createXmlFilter(context, flow);
    pipeline.addXmlFilter(filter);
  }

  public XMLFilter createXmlFilter(ServiceContext context, Flow flow) {
    XMLFilter filter = new RemoveEmptyAttributes(elementTest, exceptElementTest, attributeTest, exceptAttributeTest);
    return filter;
  }
}

