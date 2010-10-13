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

package com.servingxml.components.saxsink;

import com.servingxml.app.ServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.io.saxsink.SaxSink;
import com.servingxml.components.streamsink.StreamSinkFactory;

/**
 * A <code>SaxSinkFactory</code> defines an interface for applying additional formatting 
 * to transformed XML content and sending the result to an output stream.
 *
 * <p>All data members in a SaxSinkFactory implementation should be declared as final
 * and initialized in the constructor.
 * The <code>serialize</code> method must be reentrant.</p>
  *
 * 
 * @author  Daniel A. Parker
 */

public interface SaxSinkFactory {
  public static final SaxSinkFactory NULL = new NullSaxSinkFactory();
  public static final SaxSinkFactory DEFAULT = new DefaultSaxSinkFactory();

  SaxSink createSaxSink(ServiceContext context, Flow flow); 
}
