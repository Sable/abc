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

package com.servingxml.components.content.dynamic;

/**
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

/**
 * The <code>DynamicContentHandler</code> interface is a tagging interface that
 * identifies an object as a source of dynamic content.  
 * The methods on a <code>DynamicContentHandler</code> instance are
 * invoked by the ServingXML framework through reflection.
 *<p>
 * A <code>DynamicContentHandler</code> implementation services the request with a 
 *  method of the form
 *<p><pre>
 *    public void onRequest(ServiceContext context, Object parameters, 
 *    ContentWriter contentWriter);
 *</pre></p>
 * where the first parameter is a {@link com.servingxml.app.ServiceContext},
 * the second parameter may be any user defined interface that
 * follows bean naming conventions for accessors, and the third parameter wraps a SAX 2
 * <code>ContentHandler</code>.
 *</p>
 * 
 * <p>
 * The <code>onRequest</code> method is responsible for getting dynamic XML content 
 * (e.g. a document, results from a database query.) It can return dynamic XML 
 * content through a SAX 2 <code>ContentHandler</code>. This content may be
 * cached depending on the settings for caching parameters in the resources script. 
 * </p>
 * <p>
 * The classes responsible for fielding requests for documents implement
 * this interface.  These <code>DynamicContentHandler</code> classes are registered by name 
 * in a resources script.
 * </p>
 * <p>
 * When there is a request for a document associated with a <code>DynamicContentHandler</code>,
 * the handler's <code>handleRequest</code> method is called with a request context,
 * parameters and a SAX 2 <code>ContentHandler</code> to receive XML back. The handler could
 * respond to the event by obtaining content from an external data source and feeding 
 * start/end elements back to the <code>ContentHandler</code>. 
 * </p>
 *
 * <p>The com.servingxml.content package contains a number of helper classes
 * that simplify the action of feeding start/end elements to the <code>ContentHandler</code>.
 * See {@link com.servingxml.components.content.dynamic.ContentWriter},
 * {@link com.servingxml.components.content.dynamic.AttributeSet}, 
 *</p>
 *
 * <p>All data members in a <code>DynamicContentHandler</code> implementation should be declared as final
 * and initialized in the constructor.
 * The <code>onRequest</code> method must be reentrant.</p>
 * 
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface DynamicContentHandler {
}
