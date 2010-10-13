package com.servingxml.util.xml;

import javax.xml.datatype.DatatypeFactory;

import org.xml.sax.Attributes;

import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.Dictionary;

public class RootReceiverContext extends AbstractReceiverContext implements ReceiverContext {
  private final DatatypeFactory datatypeFactory;

  public RootReceiverContext(Name elementName, Attributes attributes, DatatypeFactory datatypeFactory, 
    Dictionary<String,String> prefixMap, MutableNameTable nameTable) {
    super(elementName, attributes, prefixMap, nameTable);
    this.datatypeFactory = datatypeFactory;
  }

  public int getParentElementSymbol() {
    return UNDEFINED_SYMBOL;
  }

  public Name getParentElementName() {
    return Name.EMPTY;
  }

  public int getLevel() {
    return 0;
  }

  public DatatypeFactory getDatatypeFactory() {
    return datatypeFactory;
  }
}
