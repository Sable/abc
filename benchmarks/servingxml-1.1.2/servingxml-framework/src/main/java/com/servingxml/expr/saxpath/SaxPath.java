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

package com.servingxml.expr.saxpath;

import org.xml.sax.Attributes;

import com.servingxml.util.record.Record;
import com.servingxml.util.MutableNameTable;

public class SaxPath {
  private final String namespaceUri;
  private final String localName;
  private final String qname;
  private final int nameSymbol;
  private final Attributes atts;
  private final SaxPath parent;
  private final MutableNameTable nameTable;
  private final PathContext pathContext;
  private boolean match = false;

  public SaxPath(MutableNameTable nameTable, 
                        String namespaceUri, String localName, String qname, Attributes atts) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
    this.qname = qname;
    this.nameSymbol = nameTable.getSymbol(namespaceUri,localName);
    this.atts = atts;
    this.nameTable = nameTable;
    this.parent = null;
    this.pathContext = new PathContext();
  }

  public SaxPath(String namespaceUri, String localName, String qname, 
                        Attributes atts, SaxPath parent) {
    this.namespaceUri = namespaceUri;
    this.localName = localName;
    this.qname = qname;
    this.nameSymbol = parent.getNameTable().getSymbol(namespaceUri,
                                                               localName);
    this.atts = atts;
    this.parent = parent;
    this.nameTable = parent.getNameTable();
    this.pathContext = parent.getPathContext();
  }

  public String getLocalName() {
    return localName;
  }

  public String getNamespaceUri() {
    return namespaceUri;
  }

  public String getQname() {
    return qname;
  }

  public PathContext getPathContext() {
    return pathContext;
  }

  public int getPosition() {
    return pathContext.position;
  }

  public int getElementNameSymbol() {
    return nameSymbol;
  }

  public MutableNameTable getNameTable() {
    return nameTable;
  }

  public Attributes getAttributes() {
    return atts;
  }

  public SaxPath getParent() {
    return parent;
  }

  public boolean matchAttribute(String namespaceUri, String localName) {
    boolean found = false;
    for (int i = 0; !found && i < atts.getLength(); ++i) {
      if (atts.getURI(i).equals(namespaceUri) && atts.getLocalName(i).equals(localName)) {
        found = true;
      }
    }
    return found;
  }

  public void matchRelativePath(Record parameters, PathEntry tail) {

    boolean match = true;
    SaxPath context = this;

    while (match && tail != null) {
      //System.out.println("Symbol = " + tail[i] + ", path = " + context.getElementNameSymbol());
      if (!tail.matches(context, parameters)) {
        match = false;
      } else {
        context = context.getParent();
        tail = tail.getParent(context,parameters);
        if (tail != null) {
          if (context == null) {
            match = false;
          }
        }
      }
    }
    if (match) {
      ++pathContext.position;
    }

    this.match = match;
  }

  public void matchAbsolutePath(Record parameters, PathEntry tail) {

    boolean match = true;
    SaxPath context = this;

    //New
    if (tail == null && parent != null) {
      match = false;
    }

    while (match && tail != null) {
      //System.out.println("tail="+tail.getClass().getName());
      //System.out.println("Symbol = " + tail[i] + ", path = " + context.getElementNameSymbol());
      if (!tail.matches(context, parameters)) {
        match = false;
      } else {
        context = context.getParent();
        tail = tail.getParent(context,parameters);
        if (tail != null) {
          if (context == null) {
            match = false;
          }
        } else if (context != null) {
          match = false;
        }
      }
    }
    if (match) {
      ++pathContext.position;
    }

    this.match = match;
  }

  public boolean isMatched() {
    return match;
  }

  static class PathContext {
    int position = 1;
  }
}

