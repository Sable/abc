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

package com.servingxml.components.property;

import java.util.Properties;


/**
 * The <code>Property</code> class encapsulates a name/value 
 * pair. 
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class Property {

  private final String key;
  private final String value;

  public Property(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getName() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public static Properties toProperties(Property[] customProperties) {
    Properties properties = new Properties();
    for (int i = 0; i < customProperties.length; ++i) {
      Property property = customProperties[i];
      properties.setProperty(property.getName(),property.getValue());
    }
    return properties;
  }
}
