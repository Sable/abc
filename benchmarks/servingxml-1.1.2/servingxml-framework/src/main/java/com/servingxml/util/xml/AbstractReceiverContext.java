package com.servingxml.util.xml;

import javax.xml.datatype.DatatypeFactory;

import org.xml.sax.Attributes;

import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Dictionary;
import com.servingxml.util.PrefixMap;

public abstract class AbstractReceiverContext implements ReceiverContext {
  private final Name elementName;
  private final MutableNameTable nameTable;
  private final int currentElementSym;
  private final Attributes attributes;
  private final Dictionary<String,String> prefixMap;

  public AbstractReceiverContext(Name elementName, Attributes attributes, Dictionary<String,String> prefixMap,
    MutableNameTable nameTable) {
    this.elementName = elementName;
    this.attributes = attributes;
    this.prefixMap = prefixMap;
    this.nameTable = nameTable;
    this.currentElementSym = nameTable.getSymbol(elementName);
  }                                         

  public int getCurrentElementSymbol() {
    return currentElementSym;
  }

  public Attributes getCurrentElementAttributes() {
    return attributes;
  }

  public Dictionary<String,String> getPrefixMap() {
    return prefixMap;
  }

  public Name createName(String qname) {
    String namespaceUri = "";
    String localName = "";
    String prefix = "";

    if (qname != null) {
      int pos = qname.indexOf(":");
      if (pos < 0) {
        localName = qname;
      } else {
        prefix = qname.substring(0, pos);
        localName = qname.substring(pos + 1);
        namespaceUri = prefixMap.get(prefix);
      }
    }
    Name name = new QualifiedName(namespaceUri, localName);
    return name;
  }

  public int getSymbol(Name name) {
    return nameTable.getSymbol(name);
  }

  public MutableNameTable getNameTable() {
    return nameTable;
  }

  public Name getCurrentElementName() {
    return elementName;
  }

  public ReceiverContext createChildContext(Name elementName, Attributes attributes, Dictionary<String,String> prefixMap) {
    return new ChildReceiverContext(elementName, attributes, prefixMap, this);
  }
}
