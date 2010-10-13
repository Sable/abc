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

package com.servingxml.components.application;

import com.servingxml.app.Application;
import com.servingxml.components.error.CatchError;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.app.Environment;

/**
 * The <code>ServiceImpl</code> implements
 * the methods in the <code>Service</code> interface.
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ApplicationImpl implements Application {

  private final Environment env;

  public ApplicationImpl(Environment env) {
    //System.out.println("parameterDescriptors count = " + parameterDescriptors.length);

    this.env = env;
  }

  public Environment getEnvironment() {
    return env;
  }
}
