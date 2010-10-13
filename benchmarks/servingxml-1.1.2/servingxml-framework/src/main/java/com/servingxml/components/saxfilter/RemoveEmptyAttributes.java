/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file attributeTest in compliance with the License. 
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

package com.servingxml.components.saxfilter;

import java.util.LinkedList;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.xml.sax.XMLFilter;

import com.servingxml.util.NameTest;

public class RemoveEmptyAttributes extends XMLFilterImpl implements XMLFilter {
  private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();

  private final NameTest elementTest;
  private final NameTest exceptElementTest;
  private final NameTest attributeTest;
  private final NameTest exceptAttributeTest;

  public RemoveEmptyAttributes(NameTest elementTest, NameTest exceptElementTest,
                               NameTest attributeTest, NameTest exceptAttributeTest) {
    this.elementTest = elementTest;
    this.exceptElementTest = exceptElementTest;
    this.attributeTest = attributeTest;
    this.exceptAttributeTest = exceptAttributeTest;
  }

  public void startElement(String namespaceUri, String localName, String qname, Attributes atts)
  throws SAXException {
	  // Remove empty attributeTest
	  if (elementTest.matches(namespaceUri,localName)  && !exceptElementTest.matches(namespaceUri,localName)) {
		  AttributesImpl newAtts = new AttributesImpl(atts);
		  boolean changed = false;
		  for (int i = 0; i < newAtts.getLength(); ++i) {
        if (attributeTest.matches(newAtts.getURI(i),newAtts.getLocalName(i)) && !exceptAttributeTest.matches(newAtts.getURI(i),newAtts.getLocalName(i))) {
          String value = newAtts.getValue(i);
          if (value != null && value.trim().length() == 0) {
            newAtts.removeAttribute(i--);
            changed = true;
          }
        }
		  }
		  if (changed)
			  atts = newAtts;
	  }
    super.startElement(namespaceUri,localName,qname,atts);
  }
}


