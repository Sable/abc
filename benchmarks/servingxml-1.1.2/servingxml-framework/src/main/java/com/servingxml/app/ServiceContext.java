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

package com.servingxml.app;

import java.util.Locale;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.ErrorListener;
import com.servingxml.util.system.RuntimeContext;
import com.servingxml.util.MutableNameTable;
import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.system.Logger;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.util.system.AbstractRuntimeContext;
import com.servingxml.util.xml.UriResolverFactory;

/**
 * A <tt>ServiceContext</tt> augments the {@link com.servingxml.util.system.RuntimeContext}
 * interface with additional methods that provide information about the context of a request.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */                                   

public abstract class ServiceContext extends AbstractRuntimeContext 
implements RuntimeContext {
  public abstract AppContext getAppContext();

  public ServiceContext(Logger logger) {
    super(logger);
  }

  public abstract MutableNameTable getNameTable();

  public abstract String getUser();

  public abstract UriResolverFactory getUriResolverFactory();

  public abstract ErrorListener getTransformerErrorListener();

  public abstract String getLang();

  public abstract Locale getLocale();

  public abstract SAXTransformerFactory getTransformerFactory();

  public static ServiceContext getDefault() {
    SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    NameTableImpl nameTable = new NameTableImpl();
    SimpleIocContainer resources = new SimpleIocContainer(nameTable, transformerFactory);
    AppContext appContext = new DefaultAppContext("",resources,SystemConfiguration.getLogger());
    ServiceContext context = new DefaultServiceContext(appContext,"",SystemConfiguration.getLogger());
    return context;
  }
}

