package com.servingxml.util.xml;

import javax.xml.datatype.DatatypeFactory;

import org.xml.sax.Attributes;

import com.servingxml.util.Name;
import com.servingxml.util.QnameFactory;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Dictionary;
import com.servingxml.util.PrefixMap;

public interface ReceiverContext extends QnameFactory {
  public final int UNDEFINED_SYMBOL = -1;

  int getSymbol(Name name);

  int getCurrentElementSymbol();

  Name getCurrentElementName();

  Name getParentElementName();

  int getParentElementSymbol();

  ReceiverContext createChildContext(Name childName, Attributes attributes, Dictionary<String,String> prefixMap);

  int getLevel();

  DatatypeFactory getDatatypeFactory();

  Attributes getCurrentElementAttributes();

  MutableNameTable getNameTable();

  Dictionary<String,String> getPrefixMap();
}
