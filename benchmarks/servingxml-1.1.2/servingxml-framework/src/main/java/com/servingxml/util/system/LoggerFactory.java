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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.FieldPosition;

import com.servingxml.util.InstanceFactory;

/**
 * Factory for creating a loggable context.
 *
 * <p>This class provides a factory method for creating new <code>Logger</code> 
 * objects.</p>
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * @see Logger
 */

public abstract class LoggerFactory {
  private static final String PROPERTY_KEY = "com.servingxml.util.system.Logger";

  protected LoggerFactory() {
  }

  protected static LoggerFactory createInstance() {
    LoggerFactory loggerFactory = null;

    // Use the system property first
    String loggerClass = System.getProperty(PROPERTY_KEY);
    if (loggerClass != null) {
      InstanceFactory instanceFactory = new InstanceFactory(loggerClass, Logger.class);
      loggerFactory = new CustomLoggerFactory(instanceFactory);
    }
    if (loggerFactory == null) {
      loggerFactory = new DefaultLoggerFactory();
    }

    return loggerFactory;
  }

  public abstract Logger createLogger();
}

class CustomLoggerFactory extends LoggerFactory {
  private final InstanceFactory instanceFactory;

  CustomLoggerFactory(InstanceFactory instanceFactory) {
    this.instanceFactory = instanceFactory;
  }

  public Logger createLogger() {
    Logger logger = null;
    try {
      logger = (Logger)instanceFactory.createInstance();
    } catch (Exception e) {
      logger = new DefaultLogger();
    }
    return logger;
  }
}


