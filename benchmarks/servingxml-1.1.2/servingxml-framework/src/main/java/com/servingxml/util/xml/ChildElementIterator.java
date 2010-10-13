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

import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;


/**
 * An iterator for iterating over the child elements of a node.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

class ChildElementIterator implements Iterator {
  private final String namespaceUri;
  private final String localName;
  private NodeList nodeList;
  private int index = 0;

  /**
   * Constructs n iterator over all child elements of the given node that have the 
   * the specified namespaceUri and localName.
   */
  public ChildElementIterator(Node sectionNode, String namespaceUri, String localName) {
  
    this.nodeList = sectionNode.getChildNodes();
    int index = 0;
    this.namespaceUri = namespaceUri;
    this.localName = localName;
  }

  public boolean hasNext() {
    while (index < nodeList.getLength()) {
      if (nodeList.item(index).getNodeType() == Node.ELEMENT_NODE) {
        Node element = nodeList.item(index);
        if (DomHelper.areEqual(element.getNamespaceURI(),element.getLocalName(),
          namespaceUri,localName)) {
          break;
        }
      }
      ++index;
    }
    return index < nodeList.getLength();
  }

  public Object next() {
    return nodeList.item(index++);
  }
  public void remove() {
  }
}

class SameNsChildElementIterator implements Iterator {
  private final String namespaceUri;
  private NodeList nodeList;
  private int index = 0;

  /**
   * Constructs n iterator over all child elements of the given node that have the 
   * the specified namespaceUri.
   */
  public SameNsChildElementIterator(Node sectionNode, String namespaceUri) {
  
    this.nodeList = sectionNode.getChildNodes();
    int index = 0;
    this.namespaceUri = namespaceUri;
  }

  public boolean hasNext() {
    while (index < nodeList.getLength()) {
      if (nodeList.item(index).getNodeType() == Node.ELEMENT_NODE) {
        Node element = nodeList.item(index);
        if ((element.getNamespaceURI() == null && namespaceUri == null) || 
            (element.getNamespaceURI().equals(namespaceUri))) {
          break;
        }
      }
      ++index;
    }
    return index < nodeList.getLength();
  }

  public Object next() {
    return nodeList.item(index++);
  }
  public void remove() {
  }
}
 
