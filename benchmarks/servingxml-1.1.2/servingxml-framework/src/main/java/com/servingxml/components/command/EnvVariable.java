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

package com.servingxml.components.command;

import java.util.Properties;

/**
 * An <code>EnvVariable</code> object encapsulates an 
 * environmental varaible. 
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */
                                    
public class EnvVariable {

  private final String name;
  private final String value;

  public EnvVariable(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public static Properties toProperties(EnvVariable[] outputProperties) {
    Properties properties = new Properties();
    for (int i = 0; i < outputProperties.length; ++i) {
      EnvVariable property = outputProperties[i];
      properties.setProperty(property.getName(),property.getValue());
    }
    return properties;
  }
}
