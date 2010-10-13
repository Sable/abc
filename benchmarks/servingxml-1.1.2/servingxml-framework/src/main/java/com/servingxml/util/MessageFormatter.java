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

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

public class MessageFormatter {

  private ResourceBundle resourceBundle = new ServingXmlResources();

  private static MessageFormatter instance = new MessageFormatter();

  public static MessageFormatter getInstance() {
    return instance;
  }

  public String getMessage(String key) {
    return getMessage(key, SystemConstants.EMPTY_STRING_ARRAY);
  }

  public String getMessage(String key, String arg1) {
    return getMessage(key, new String[]{arg1});
  }

  public String getMessage(String key, String arg1, String arg2) {
    return getMessage(key, new String[]{arg1, arg2});
  }

  public String getMessage(String key, String arg1, String arg2, String arg3) {
    return getMessage(key, new String[]{arg1, arg2, arg3});
  }

  public String getMessage(String key, String arg1, String arg2, String arg3, String arg4) {
    return getMessage(key, new String[]{arg1, arg2, arg3, arg4});
  }

  public String getMessage(String key, String[] args) {
    String message = null;
    try {
      message = resourceBundle.getString(key);
      message = MessageFormat.format(message, (Object[])args);
    } catch (java.util.MissingResourceException e) {
      message = makeDefaultMessage(key, args);
    }
    return message;
  } 

  private String makeDefaultMessage(String key, String[] args)
  {
    return key + " " + StringHelper.toString(args, ",", "");
  }
}
