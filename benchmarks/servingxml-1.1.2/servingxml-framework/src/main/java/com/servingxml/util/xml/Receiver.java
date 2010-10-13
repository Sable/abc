package com.servingxml.util.xml;

import com.servingxml.util.Name;

public interface Receiver {

  public static final int UNDEFINED_SYMBOL = -1;

  int getSymbol();
  void bind(ReceiverContext context);
  void startElement(ReceiverContext context);
  void endElement(ReceiverContext context);
  void characters(ReceiverContext context, char[] ch, int start, int length);
  Name getName();
  String getAttributeValue(int i);
  void childReceived(int symbol);
}
