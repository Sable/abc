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

import java.lang.reflect.InvocationTargetException;

import org.xml.sax.XMLReader;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ServingXmlException extends RuntimeException {
  static final long serialVersionUID = 1732939618562742663L;

  public ServingXmlException(String s) {
    super(s);
  }
  public ServingXmlException(String s, Throwable e) {
    super(s, e);
  }

  public void getContext(StringBuilder sb) {
  }

  public void getMessage(StringBuilder sb) {
    sb.append(getMessage());
  }

  public static ServingXmlException fromInvocationTargetException(InvocationTargetException e) {
    Throwable t = e.getTargetException();
    ServingXmlException pe;
    if (t != null) {
      if (t instanceof ServingXmlException) {
        pe = (ServingXmlException)t;
      } else if (t.getMessage() != null) {
        pe = new ServingXmlException(t.getMessage(),t);
      } else if (t.getCause() != null && t.getCause().getMessage() != null) {
        pe = new ServingXmlException(t.getCause().getMessage(),t.getCause());
      } else if (e.getMessage() != null) {
        pe = new ServingXmlException(e.getMessage(),e);
      } else {
        pe = new ServingXmlException("Unknown exception",t);
      }
    } else if (e.getMessage() != null) {
      pe = new ServingXmlException(e.getMessage(),e);
    } else {
      pe = new ServingXmlException("Unknown exception",e);
    }
    return pe;
  }

  public XMLReader createXmlReader() {
    return new ServingXmlFaultReader(ServingXmlFaultCodes.RECEIVER_CODE,getMessage());
  }

  public String toString() {
    return getMessage();
  }

  public void getSupplementaryMessage(StringBuilder sb) {
  }

  public ServingXmlException supplementMessage(String message) {
    return new SupplementaryServingXmlException(message, this);
  }

  public ServingXmlException contextualizeMessage(String context) {
    return new ContextualServingXmlException(context, this);
  }
}

