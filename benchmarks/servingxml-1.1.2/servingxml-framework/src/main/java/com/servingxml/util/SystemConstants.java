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

package com.servingxml.util;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface SystemConstants {
  static final String VERSION = "1.1.2";

  static final String SERVINGXML_IOC_NS_URI="http://www.servingxml.com/ioc";
  static final String SERVINGXML_NS_URI="http://www.servingxml.com/core";
  static final String SERVINGXML_NS_PREFIX="sx";
  static final String SERVINGXMLX_NS_URI="servingxmlx";                 
  static final String SERVINGXMLX_NS_PREFIX="sxx";
  static final String XSL_NS_URI = "http://www.w3.org/1999/XSL/Transform";
  static final String SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
  static final String XSL_NS_PREFIX = "xsl";
  static final String CLASS_NAME = "class";

  static final String NS = "ns";
  static final String ID = "id";
  static final String NAME = "name";
  static final String KEY = "key";
  static final String BASE = "base";
  static final String XML_BASE = "xml:base";
  static final String REF = "ref";
  
  static final String YES = "yes";
  static final String NO = "no";
  static final String ACCEPT = "accept";
  
  static final String ERROR = "error";
  static final String VALUE = "value";
  static final String[] EMPTY_STRING_ARRAY = new String[0];
  static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  static final char[] EMPTY_CHAR_ARRAY = new char[0];
  static final Name PARAMETERS_TYPE_NAME = new QualifiedName(SERVINGXML_NS_URI,"parameters");
  static final Name MESSAGE_NAME = new QualifiedName(SERVINGXML_NS_URI,"message");
  static final Name RECORD_COUNT_NAME = new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"recordCount");
  static final Name BATCH_COUNT_NAME = new QualifiedName(SystemConstants.SERVINGXML_NS_URI,"batchCount");

  public static final Attributes EMPTY_ATTRIBUTES = new AttributesImpl();
}
