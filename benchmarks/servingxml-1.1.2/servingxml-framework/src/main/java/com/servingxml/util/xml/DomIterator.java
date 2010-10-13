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

package com.servingxml.util.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;

import com.servingxml.util.PrefixMap;

/**
 * Contains static helper methods for iterating over a DOM object.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DomIterator {

  public static abstract class AttributeCommand {
    public void doPrefixMapping(Element element, String prefix, String namespaceUri) {
    }
    public void doAttribute(Element element, String namespaceUri, String localName, 
    String qname, String value) {
    }
    public void doEntityReference(Element element, EntityReference entityReference) {
    }
  }

  public static abstract class ChildCommand {
    public void doText(Element parent, String value) {
    }
    public void doElement(Element parent, Element element) {
    }
    public void doEntity(Element parent, Entity entity) {
    }
    public void doEntityReference(Element parent, EntityReference entityReference) {
    }
  }

  public final static void toEveryAttribute(Element element, 
  AttributeCommand command) {
    NamedNodeMap nodeList = element.getAttributes();
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Attr attr = (Attr)nodeList.item(i);
      String name = attr.getName();
      if (attr.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
        command.doEntityReference(element, (EntityReference)attr);
      } else if (name.startsWith("xmlns:")) {
        String prefix = name.substring(6);
        command.doPrefixMapping(element,prefix,attr.getNodeValue());
      } else if (name.equals("xmlns")) {
        command.doPrefixMapping(element,"",attr.getNodeValue());
      } else {
        command.doAttribute(element,attr.getNamespaceURI(),attr.getLocalName(),
          attr.getName(), attr.getNodeValue());
      }
    }
  }

  public final static void toEveryChild(Element parent, ChildCommand command) {
    toEveryChild(parent,command,true);
  }

  public final static void toEveryChild(Element parent, ChildCommand command, boolean stripWhitespace) {
    NodeList nodeList = parent.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Node node = nodeList.item(i);
      toChild(parent,command,stripWhitespace,node);
    }
  }

  public final static void toEveryChild(Element parent, ChildCommand command, 
  String namespaceUri, String localName, boolean stripWhitespace) {
    NodeList nodeList = parent.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Node node = nodeList.item(i);
      if (DomHelper.areEqual(namespaceUri,localName,
        node.getNamespaceURI(),node.getLocalName())) {
        toChild(parent,command,stripWhitespace,node);
      }
    }
  }

  public final static void toEveryChild(Element parent, ChildCommand command, 
  String namespaceUri, String localName) {
    toEveryChild(parent,command,namespaceUri,localName,true);
  }

  private final static void toChild(Element parent, ChildCommand command, 
  boolean stripWhitespace, Node node) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      command.doElement(parent, (Element)node);
    } else if (node.getNodeType() == Node.TEXT_NODE) {
      String value = node.getNodeValue();
      if (stripWhitespace) {
        command.doText(parent, value.trim());
      } else {
        command.doText(parent, value);
      }
    } else if (node.getNodeType() == Node.ENTITY_NODE) {
      command.doEntity(parent, (Entity)node);
    } else if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
      command.doEntityReference(parent, (EntityReference)node);
    }
  }
}
