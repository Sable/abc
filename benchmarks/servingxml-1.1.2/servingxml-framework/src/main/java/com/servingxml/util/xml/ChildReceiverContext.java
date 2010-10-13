package com.servingxml.util.xml;

import javax.xml.datatype.DatatypeFactory;

import org.xml.sax.Attributes;

import com.servingxml.util.Name;
import com.servingxml.util.Dictionary;

public class ChildReceiverContext extends AbstractReceiverContext implements ReceiverContext {
  private final ReceiverContext parent;

  public ChildReceiverContext(Name elementName, Attributes attributes, Dictionary<String,String> prefixMap,
    ReceiverContext parent) {
    super(elementName, attributes, prefixMap, parent.getNameTable());
    this.parent = parent;
  }

  public int getParentElementSymbol() {
    return parent.getCurrentElementSymbol();
  }

  public Name getParentElementName() {
    return parent.getCurrentElementName();
  }

  public int getLevel() {
    return parent.getLevel()+1;
  }

  public DatatypeFactory getDatatypeFactory() {
    return parent.getDatatypeFactory();
  }
}
