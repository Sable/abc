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

package com.servingxml.util.system;

import java.util.Iterator;
import java.util.Properties;
import java.util.Map;
import java.net.URL;
import java.io.InputStream;

import com.servingxml.util.ServingXmlException;

/**
 * There is a single instance of a <code>SystemConfiguration</code>
 * which may be obtained through its static <code>getInstance()</code> method.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SystemConfiguration {
  private static SystemConfiguration instance = new SystemConfiguration();

  private final Logger logger;
  private final SystemContext systemContext;

  public static SystemConfiguration getInstance() {
    return instance;
  }
  private SystemConfiguration() {
    try {
      String configFilename = "servingxml.properties";
      ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
      URL configUrl = parentLoader.getResource(configFilename);
      Properties properties = new Properties();
      if (configUrl != null) {
        InputStream is = configUrl.openStream(); 
        properties.load(is);
        Iterator<Map.Entry<Object,Object>> iter = properties.entrySet().iterator();
        while (iter.hasNext()) {
          Map.Entry entry = iter.next();
          //System.out.println((String)entry.getKey() + "=" + (String)entry.getValue());
          System.setProperty((String)entry.getKey(),(String)entry.getValue());
        }

      }
    } catch (Exception e) {
    }
    Logger newLogger;
    try {
      LoggerFactory loggerFactory = LoggerFactory.createInstance();
      newLogger = loggerFactory.createLogger();
    } catch (ServingXmlException e) {
      newLogger = new DefaultLogger();
    }
    this.logger = newLogger;
    this.systemContext = new SystemContext(logger);
  }

  public static Logger getLogger() {
    return getInstance().logger;
  }

  public static RuntimeContext getSystemContext() {
    return getInstance().systemContext;
  }
}
