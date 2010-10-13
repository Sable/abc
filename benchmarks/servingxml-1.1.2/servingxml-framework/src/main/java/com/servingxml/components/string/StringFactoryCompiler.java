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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.ioc.components.ComponentAssembler;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.ioc.components.ServiceComponent;
import com.servingxml.util.Name;
import com.servingxml.util.QnameContext;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.StringHelper;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.Record;    
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.xml.DomIterator;

/**
 * A <code>StringFactory</code> defines an interface for 
 * creating a string. 
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public abstract class StringFactoryCompiler {

  public static StringFactory fromStringFactories(ConfigurationContext context, Element textElement) {
    return compile(context,textElement,StringFactory.class);
  }

  public static StringFactory fromStringables(ConfigurationContext context, Element textElement) {
    return compile(context,textElement,Stringable.class);
  }

  private static StringFactory compile(ConfigurationContext context, Element textElement, Class stringClass) {
    //System.out.println("StringFactory.fromStringFactories " + context.getElement().getTagName());

    TextElementCommand command = new TextElementCommand(context, null, stringClass);
    DomIterator.toEveryChild(textElement, command, false);

    StringFactory text = command.getStringFactory();
    if (text == null) {
      text = new StringEvaluator();
    }
    return text;
  }

  static class TextElementCommand extends DomIterator.ChildCommand {

    private final ConfigurationContext context;
    private StringFactory tail; 
    private final Class stringClass;

    TextElementCommand(ConfigurationContext context, StringFactory tail, Class stringClass) {
      this.context = context;
      this.tail = tail;
      this.stringClass = stringClass;
    }

    StringFactory getStringFactory() {
      return tail;
    }

    public void doText(Element parent, String value) {
      if (!StringHelper.isWhitespaceOrEmpty(value)) {
        StringFactory stringFactory = new StringLiteralFactory(value);
        tail = new StringEvaluator(stringFactory,tail);
      }
    }

    public void doElement(Element contextElement, Element componentElement) {
      String componentNamespaceUri = componentElement.getNamespaceURI();
      if (componentNamespaceUri == null) {
        componentNamespaceUri = "";
      }
      String componentLocalName = componentElement.getLocalName();
      //System.out.println("Component={"+componentElement.getNamespaceURI()+"}"+componentElement.getLocalName());
      //System.out.println("componentLocalName="+componentLocalName);
      int instanceSymbol = context.getNameTable().getSymbol(componentNamespaceUri, componentLocalName);
      //System.out.println(getClass().getName()+".assemble " + instanceSymbol);
      //context.getComponentDictionary().printDiagnostics(System.out,context.getNameTable());
      Class component = context.getComponentDictionary().getInterface(instanceSymbol);
      if (component != null) {
        //System.out.println("Found interface " + component.getClass().getName());
        if (stringClass.isAssignableFrom(component)) {
          //System.out.println("Component is assembler of Stringable");
          ConfigurationContext childContext = context.createInstance(componentElement);
          Object o = childContext.getServiceComponent(componentElement);
          if (o != null) {
            //System.out.println("Component created");
            Stringable stringable = (Stringable)o;
            tail = new StringEvaluator(stringable,tail);
          }
        }
      }
    }
  }
}
