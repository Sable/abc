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

/**
 * An <code>RuntimeContext</code> defines an interface for 
 * obtaining information about the service context and for reporting errors
 * in that context. 
 *
 * 
 * @author  Daniel A. Parker
 */

public interface RuntimeContext {
  String getAppName();

  String getUser();

  void trace(String sourceClass, String sourceMethod, String message, Level level);
  
  void notice(String message);

  void warning(String message);

  void error(String message);

  void printStackTrace(Throwable t);
}

