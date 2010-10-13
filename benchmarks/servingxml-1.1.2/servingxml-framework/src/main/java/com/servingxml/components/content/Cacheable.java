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

package com.servingxml.components.content;

/**
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

/**
 * The <code>Cacheable</code> interface is a tagging interface that
 * identifies the output of a dynamic content handler as Cacheable.  
 * The methods on a <code>Cacheable</code> instance are
 * invoked by the ServingXML framework through reflection.

 * A <code>Cacheable</code> implementation services the request with a 
 *  method of the form
 * <p><pre>
 * public long getLastModified(Object key, 
 * long timestamp, long elapsed) {
 * </pre></p>
 * where the first parameter is a {@link com.servingxml.app.ServiceContext},
 * the second parameter may be any user defined interface that
 * follows bean naming conventions for accessors, 
 * and the third parameter is the timestamp when the dynamic content was created,
 * and the fourth parameter is the amount of time elapsed since then.
 * </p>
 */

public interface Cacheable {
}                      
