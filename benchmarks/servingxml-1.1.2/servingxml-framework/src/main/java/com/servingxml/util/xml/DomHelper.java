/**
 *  ServingXML
 *  
 *  Copyright (C) 2004  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  See terms of license at gnu.org.
 *
 */ 

package com.servingxml.util.xml;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;

import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.PrefixMap;
import com.servingxml.util.PrefixMapImpl;

/**
 * Contains static helper methods for querying a DOM object.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DomHelper {
  public static String XMLNS_NS_URI = "http://www.w3.org/2000/xmlns/";

  public static final String XMLNS = "xmlns";

  private DomHelper() {
  }

  /**
  * Returns the first child element of the given node, null if there are none.
  */
  public static final Element getFirstChildElement(Node node) {
    Iterator iter = createChildElementIterator(node);
    return iter.hasNext() ? (Element)iter.next() : null;
  }

  /**
  * Returns the first child element of the given node that has the specified 
  * namespaceUri and localName, null if there are none.
  */
  public static final Element getFirstChildElement(Node node,
                                                   String namespaceUri, String localName) {
    Iterator iter = createChildElementIterator(node, namespaceUri, localName);
    return iter.hasNext() ? (Element)iter.next() : null;
  }

  /**
  * Returns true if the specified element contains a child element with the 
  * specified namespaceUri and localName, false otherwise.
  */
  public static final boolean containsChildElement(Element sectionNode,
                                                   String namespaceUri, String localName) {
    Iterator iter = createChildElementIterator(sectionNode, namespaceUri, localName);
    return iter.hasNext();
  }

  /**
  * Returns an iterator over all child elements of the given node that have the 
  * the specified namespaceUri.
  */
  public static final Iterator createChildElementIterator(Node sectionNode, 
                                                          String namespaceUri) {

    return new SameNsChildElementIterator(sectionNode,namespaceUri);
  }

  /**
  * Returns an iterator over all child elements of the given node that have the 
  * the specified namespaceUri and localName.
  */
  public static final Iterator createChildElementIterator(Node sectionNode, 
                                                          String namespaceUri, String localName) {

    return new ChildElementIterator(sectionNode,namespaceUri,localName);
  }

  /**
  * Returns an iterator over all child elements of the given node.
  */
  public static Iterator createChildElementIterator(final Node sectionNode) {

    Iterator iterator = new Iterator() {
      NodeList nodeList = sectionNode.getChildNodes();
      int index = 0;

      public boolean hasNext() {
        while (index < nodeList.getLength()) {
          if (nodeList.item(index).getNodeType() == Node.ELEMENT_NODE) {
            break;
          }
          ++index;
        }
        return index < nodeList.getLength();
      }
      public Object next() {
        while (index < nodeList.getLength()) {
          if (nodeList.item(index).getNodeType() == Node.ELEMENT_NODE) {
            break;
          }
          ++index;
        }
        return nodeList.item(index++);
      }
      public void remove() {
      }
    };

    return iterator;
  }

  /**
  * Returns the text value of an element node.
  */
  public static String getInnerText(Element element) {
    NodeList childNodes = element.getChildNodes();
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < childNodes.getLength(); ++i) {
      Node child = childNodes.item(i);
      if (child.getNodeType() == Node.TEXT_NODE) {
        String value = child.getNodeValue().trim();
        buf.append(value);
      } else if (child.getNodeType() == Node.ENTITY_NODE) {
        Entity entity = (Entity)child;
      } else if (child.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
        EntityReference entity = (EntityReference)child;
      }
    }

    return buf.toString();
  }

  /**
  * Returns the text value of an element node.
  */
  public static String getValue(Element element) {
    NodeList childNodes = element.getChildNodes();
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < childNodes.getLength(); ++i) {
      Node child = childNodes.item(i);
      if (child.getNodeType() == Node.TEXT_NODE) {
        String value = child.getNodeValue();
        buf.append(value);
      } else if (child.getNodeType() == Node.ENTITY_NODE) {
        Entity entity = (Entity)child;
      } else if (child.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
        EntityReference entity = (EntityReference)child;
      }
    }

    return buf.toString();
  }

  public static final boolean areEqual(Element element,
                                       String namespaceUri2, String localName2) {
    return areEqual(element.getNamespaceURI(),element.getLocalName(),
                    namespaceUri2, localName2);
  }

  public static final boolean areEqual(String namespaceUri1, String localName1,
                                       String namespaceUri2, String localName2) {
    if (namespaceUri1 == null) {
      namespaceUri1 = "";
    }
    if (namespaceUri2 == null) {
      namespaceUri2 = "";
    }
    return namespaceUri1.equals(namespaceUri2) && localName1.equals(localName2);
  }

  public static String getPrefix(String qname) {
    String prefix = "";
    int pos = qname.indexOf(":");
    if (pos != -1) {
      prefix = qname.substring(0, pos);
    }
    return prefix;
  }

  public static Name createName(String qname, Element element) {

    String namespaceUri = "";
    String localName = "";
    String prefix = "";
    if (qname != null) {
      int pos = qname.indexOf(":");
      if (pos < 0) {
        localName = qname;
        //namespaceUri = defaultNS;
      } else {
        prefix = qname.substring(0, pos);
        localName = qname.substring(pos + 1);
        namespaceUri = getNamespaceUri(prefix,element);
      }
    }
    Name name = new QualifiedName(namespaceUri, localName);
    return name;
  }

  public static String getNamespaceUri(String prefix, Element element) {
    String namespaceUri = "";
    boolean done = false;
    while (!done) {
      Attr attr = element.getAttributeNodeNS(XMLNS_NS_URI,prefix);
      if (attr != null) {
        namespaceUri = attr.getValue();
        done = true;
      } else {
        Node n = element.getParentNode();
        if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
          done = true;
        } else {
          element = (Element)n;
        }
      }
    }
    return namespaceUri;
  }

  public static PrefixMap createPrefixMap(Element element) {
    return createPrefixMap(element,null);
  }

  public static PrefixMap createPrefixMap(Element element, PrefixMap parent) {
    NamedNodeMap attrs = element.getAttributes();

    PrefixMapImpl prefixMap = new PrefixMapImpl(parent);
    for (int i = 0; i < attrs.getLength(); i++) {
      Attr a = (Attr)attrs.item(i);
      String name = a.getName();
      if (name.startsWith("xmlns:")) {
        String prefix = name.substring(6);
        prefixMap.setPrefixMapping(prefix, a.getNodeValue());
      } else if (name.equals("xmlns")) {
        String prefix = "";
        prefixMap.setPrefixMapping(prefix, a.getNodeValue());
      }
    }
    return prefixMap;
  }

  public static final String getScopedAttribute(String qname, Element element) {
    String value = null;

    boolean done = false;
    while (!done) {
      Attr attr = element.getAttributeNode(qname);
      if (attr != null) {
        value = attr.getValue();
        done = true;
      } else {
        Node n = element.getParentNode();
        if (n == null || n.getNodeType() != Node.ELEMENT_NODE) {
          done = true;
        } else {
          element = (Element)n;
        }
      }
    }
    return value;
  }

  public static final String getAttribute(String qname, Element element) {
    String value = null;

    Attr attr = element.getAttributeNode(qname);
    if (attr != null) {
      value = attr.getValue();
    }

    return value;
  }

  public static String escapeMarkup(Element parent) {
    StringBuilder stringBuilder = new StringBuilder();
    markupToString(parent, "&lt;", "&gt;", stringBuilder);
    return stringBuilder.toString();
  }
  public static String preserveMarkup(Element parent) {
    StringBuilder stringBuilder = new StringBuilder();
    markupToString(parent, "<", ">", stringBuilder);
    return stringBuilder.toString();
  }

  private static void markupToString(Element parent, String lt, String gt, StringBuilder stringBuilder) {
    NodeList children = parent.getChildNodes();
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < children.getLength(); ++i) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.TEXT_NODE) {
        escapeValue(child.getNodeValue(), stringBuilder);
      } else if (child.getNodeType() == Node.ENTITY_NODE) {
        //System.out.println("entity="+child.getNodeValue());
      } else if (child.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element)child;
        stringBuilder.append(lt);
        stringBuilder.append(element.getTagName());
        NamedNodeMap attrs = element.getAttributes();
        for (int j = 0; j < attrs.getLength(); j++) {
          stringBuilder.append(" ");
          Attr a = (Attr)attrs.item(j);
          String name = a.getName();
          String value = a.getNodeValue();
          stringBuilder.append(name);
          stringBuilder.append("=");
          stringBuilder.append("\"");
          escapeAttributeValue(value, stringBuilder);
          stringBuilder.append("\"");
        }
        stringBuilder.append(gt);
        int saveLength = stringBuilder.length();
        markupToString(element, lt, gt, stringBuilder);
        if (stringBuilder.length() == saveLength) {
          stringBuilder.insert(saveLength-1,'/');
        } else {
          stringBuilder.append(lt);
          stringBuilder.append("/");
          stringBuilder.append(element.getTagName());
          stringBuilder.append(gt);
        }
      }
    }
  }

  private static void escapeValue(String value, StringBuilder stringBuilder) {
    for (int i = 0; i < value.length(); ++i) {
      char c = value.charAt(i); 
      switch (c) {
        case '<':
          stringBuilder.append("&lt;");
          break;
        case '>':
          stringBuilder.append("&gt;");
          break;
        case '&':
          stringBuilder.append("&amp;");
          break;
        default:
          stringBuilder.append(c);
      }
    }
  }

  private static void escapeAttributeValue(String value, StringBuilder stringBuilder) {
    for (int i = 0; i < value.length(); ++i) {
      char c = value.charAt(i); 
      switch (c) {
        case '<':
          stringBuilder.append("&lt;");
          break;
        case '>':
          stringBuilder.append("&gt;");
          break;
        case '&':
          stringBuilder.append("&amp;");
          break;
        case '\"':
          stringBuilder.append("&#34;");
          break;
        default:
          stringBuilder.append(c);
      }
    }
  }
}

