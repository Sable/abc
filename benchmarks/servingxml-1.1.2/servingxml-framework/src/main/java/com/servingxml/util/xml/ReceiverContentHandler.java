package com.servingxml.util.xml;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator; 
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Stack;
import com.servingxml.util.Name;
import com.servingxml.util.MutableDictionary;
import com.servingxml.util.DictionaryImpl;
import com.servingxml.util.Dictionary;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.PrefixMap;

public class ReceiverContentHandler implements ContentHandler {
  private Stack<ReceiverContext> contextStack;
  private Receiver receiver;
  private DatatypeFactory datatypeFactory;
  private Dictionary<String,String> parentPrefixMap;
  private MutableDictionary<String,String> currentPrefixMap;
  private MutableNameTable nameTable;

  public ReceiverContentHandler(MutableNameTable nameTable) {
    this.nameTable = nameTable;
    try {
      datatypeFactory = DatatypeFactory.newInstance();
    } catch (javax.xml.datatype.DatatypeConfigurationException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void parse(InputStream is, Receiver receiver) {

    try {
      this.contextStack = new Stack<ReceiverContext>();
      this.receiver = receiver;
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      parserFactory.setNamespaceAware(true);
      parserFactory.setValidating(false);
      SAXParser parser = parserFactory.newSAXParser();
      XMLReader reader = parser.getXMLReader();
      InputSource inputSource = new InputSource(new BufferedInputStream(is));
      reader.setContentHandler(this);
      reader.parse(inputSource);
    } catch (javax.xml.parsers.ParserConfigurationException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (IOException e) {
      throw new ServingXmlException(e.getMessage(),e);
    } catch (SAXException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  public void startDocument() throws SAXException {
    currentPrefixMap = new DictionaryImpl<String,String>();
  }

  public void endDocument() throws SAXException {
  }

  public void startElement(String namespaceUri, String localName, String qname, 
    Attributes atts) throws SAXException {
    try {
      parentPrefixMap = currentPrefixMap;
      currentPrefixMap = currentPrefixMap.createChildDictionary();
      ReceiverContext context;
      Name name = new QualifiedName(namespaceUri,localName);
      if (contextStack.empty()) {
        context = new RootReceiverContext(name, atts, datatypeFactory, parentPrefixMap, nameTable);
        receiver.bind(context);
      } else {
        ReceiverContext parentContext = contextStack.peek();
        context = parentContext.createChildContext(name, atts, parentPrefixMap);
      }
      receiver.startElement(context);
      contextStack.push(context);
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(),e);
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    try {
      ReceiverContext context = contextStack.peek();
      receiver.characters(context,ch,start,length);
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(),e);
    }
  }

  public void endElement(String namespaceUri, String localName, String qname)
  throws SAXException {
    try {
      ReceiverContext context = contextStack.pop();
      receiver.endElement(context);
    } catch (ServingXmlException e) {
      throw new SAXException(e.getMessage(),e);
    }
  }

  public void setDocumentLocator(Locator locator) {
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    //System.out.println(getClass().getName()+" " + prefix + " " + uri);
    currentPrefixMap.add(prefix,uri);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
  }

  public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
  }

  public void processingInstruction(String target, String data) throws SAXException {
  }

  public void skippedEntity(String name) throws SAXException {
  }

  public void warning (SAXParseException exception)
  throws SAXException {
    throw exception;
  }

  public void error(SAXParseException exception)
  throws SAXException {
    throw exception;
  }

  public void fatalError(SAXParseException exception)
  throws SAXException {
    throw exception;
  }
}
