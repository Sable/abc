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

package com.servingxml.ioc.components; 

import java.net.URL;

import com.servingxml.util.QnameContext;
import com.servingxml.util.ServingXmlException;        
import com.servingxml.util.Name;
import com.servingxml.util.UrlHelper;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;

public class StringConverter {

  public Object convertString(QnameContext context, String value, Class type) {

    Object o = null;
    if (type.isAssignableFrom(String.class)) {
      o = value;
    } else if (type.isPrimitive()) {
      if (type.isAssignableFrom(Integer.TYPE)) {
        o = new Integer(value);
      } else if (type.isAssignableFrom(Long.TYPE)) {
        o = new Long(value);
      } else if (type.isAssignableFrom(Double.TYPE)) {
        o = new Double(value);
      } else if (type.isAssignableFrom(Boolean.TYPE)) {
        o = new Boolean(value);
      } else if (type.isAssignableFrom(Float.TYPE)) {
        o = new Float(value);
      } else if (type.isAssignableFrom(Character.TYPE)) {
        o = new Character(value.charAt(0));
      }
    } else {
      if (type.isAssignableFrom(Name.class)) {
        Name name = context.createName(value);
        o = name;
      } else if (type.isAssignableFrom(Class.class)) {
        try {
          o = Thread.currentThread().getContextClassLoader().loadClass(value);
        } catch (java.lang.ClassNotFoundException e){
          String s = MessageFormatter.getInstance().getMessage(ServingXmlMessages.CLASS_NOT_FOUND,
                                                               value);
          throw new ServingXmlException(s,e);
        }
      } else if (type.isAssignableFrom(URL.class)) {
        String href = value;
        if (href.length() == 0) {
          throw new ServingXmlException("Unable to resolve document.  href is empty.");
        }

        try {
          String base = context.getBase();
          URL baseUrl = UrlHelper.createUrl(base);
          URL fileUrl = (baseUrl == null) ? new URL(href) : new URL(baseUrl,href);
          o = fileUrl;
        } catch (java.net.MalformedURLException e) {
          throw new ServingXmlException(e.getMessage(),e);
        }
      } else if (type.isAssignableFrom(Integer.class)) {
        o = new Integer(value);
      } else if (type.isAssignableFrom(Long.class)) {
        o = new Long(value);
      } else if (type.isAssignableFrom(Double.class)) {
        o = new Double(value);
      } else if (type.isAssignableFrom(Character.class)) {
        o = new Character(value.charAt(0));
      }
    }
    if (o == null) {
      throw new ServingXmlException("Cannot convert attribute");
    }
    
    return o;
  }
}
