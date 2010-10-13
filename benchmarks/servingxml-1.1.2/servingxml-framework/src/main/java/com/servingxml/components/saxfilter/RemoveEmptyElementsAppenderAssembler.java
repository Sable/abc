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

package com.servingxml.components.saxfilter;

import org.xml.sax.XMLFilter;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.NameTest;
import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;
import com.servingxml.components.content.Content;

public class RemoveEmptyElementsAppenderAssembler {
  
  private String elementQnames = "";
  private String exceptElementQnames = "";
  private String allDescendents = TrueFalseEnum.FALSE.toString();

  public void setElements(String elementQnames) {
    this.elementQnames = elementQnames;
  }

  public void setExcept(String exceptElementQnames) {
    this.exceptElementQnames = exceptElementQnames;
  }

  public void setExceptElements(String exceptElementQnames) {
    this.exceptElementQnames = exceptElementQnames;
  }

  public void setAllDescendents(String allDescendents) {
    this.allDescendents = allDescendents;
  }

  public Content assemble(ConfigurationContext context) {

    TrueFalseEnum recurseIndicator;
    try {
      recurseIndicator = TrueFalseEnum.parse(allDescendents);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
        context.getElement().getTagName(), "allDescendents");
      e = e.supplementMessage(message);
      throw e;
    }

    NameTest elements = NameTest.parse(context.getQnameContext(), elementQnames);
    NameTest except = NameTest.parse(context.getQnameContext(), exceptElementQnames);
    Content filterFactory = new RemoveEmptyElementsAppender(elements,except, 
                                                                         recurseIndicator.booleanValue());
    return filterFactory;
  }
}


class RemoveEmptyElementsAppender extends AbstractXmlFilterAppender implements Content {
  private final NameTest elements;
  private final NameTest except;
  private final boolean allDescendents;

  public RemoveEmptyElementsAppender(NameTest elements, NameTest except, 
                                         boolean allDescendents) {
    this.elements = elements;
    this.except = except;
    this.allDescendents = allDescendents;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
  XmlFilterChain pipeline) {
    XMLFilter filter = createXmlFilter(context, flow);
    pipeline.addXmlFilter(filter);
  }

  public XMLFilter createXmlFilter(ServiceContext context, Flow flow) {
    XMLFilter filter = new RemoveEmptyElements(elements, except, allDescendents);
    return filter;
  }
}

