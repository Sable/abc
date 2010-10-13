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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.LogManager;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ResourceBundle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.FieldPosition;

/**
 * Default system error handler.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * @see Logger
 */

public class DefaultLogger implements com.servingxml.util.system.Logger {
  private final Logger logger;
  
  public DefaultLogger() {
    logger = Logger.getLogger(this.getClass().getName(),null);
  }
  
  public final void trace(RuntimeContext context, String sourceClass, String sourceMethod, 
  String message, Level level) {

    logger.logp(level,sourceClass,sourceMethod,message);
  }

  public final void warning(RuntimeContext context, String message) {
    logger.warning(message);
  }

  public final void notice(RuntimeContext context, String message) {
    logger.info(message);
  }

  public final void error(RuntimeContext context, String message) {
    if (message == null) {
      message = "Error message is null.";
    }
    logger.severe(message);
  }

  public void printStackTrace(RuntimeContext context, Throwable t) {
    
    StringWriter writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);
    t.printStackTrace(printWriter);
    logger.severe(writer.toString());
  }
}

