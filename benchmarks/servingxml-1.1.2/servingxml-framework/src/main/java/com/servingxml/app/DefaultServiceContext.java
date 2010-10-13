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

import com.servingxml.components.content.DefaultUriResolverFactory;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.system.AbstractRuntimeContext;
import com.servingxml.util.system.Logger;
import com.servingxml.util.system.SystemConfiguration;
import com.servingxml.util.xml.DefaultTransformerErrorListener;
import com.servingxml.util.xml.UriResolverFactory;
import java.util.Hashtable;
import java.util.Locale;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.ErrorListener;

/**
 * This class implements a <code>ServiceContext</code> interface.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DefaultServiceContext extends ServiceContext {

  private final String user;
  private final AppContext appContext;
  private final String lang = "en";
  private final Locale locale = new Locale("en","");
  private final MutableNameTable nameTable;
  private final UriResolverFactory uriResolverFactory;
  private final ErrorListener transformerErrorListener;

  public DefaultServiceContext(AppContext appContext, String user, Logger logger) {
    super(logger);

    this.user = user;
    this.appContext = appContext;                    
    this.nameTable = new NameTableImpl(appContext.getResources().getNameTable());
    this.uriResolverFactory = new DefaultUriResolverFactory(this);
    this.transformerErrorListener = new DefaultTransformerErrorListener(this);
    appContext.getTransformerFactory().setErrorListener(transformerErrorListener);
  }

  public DefaultServiceContext(AppContext appContext, String user) {
    super(SystemConfiguration.getLogger());

    this.user = user;
    this.appContext = appContext;
    this.nameTable = new NameTableImpl(appContext.getResources().getNameTable());
    this.uriResolverFactory = new DefaultUriResolverFactory(this);
    this.transformerErrorListener = new DefaultTransformerErrorListener(this);
    appContext.getTransformerFactory().setErrorListener(transformerErrorListener);
  }

  public AppContext getAppContext() {
    return appContext;
  }

  public MutableNameTable getNameTable() {
    return nameTable;
  }

  public void forward(String url) {
  }

  public String getUser() {
    return user;
  }

  public String getAppName() {
    return appContext.getAppName();
  }

  public Locale getLocale() {
    return locale;
  }

  public String getLang() {
    return lang;
  }
/*
  public Name createName(String namespaceUri, String localName) {
    return nameTable.createName(namespaceUri, localName);
  }
*/
  public SAXTransformerFactory getTransformerFactory() {
    return appContext.getTransformerFactory();
  }

  public UriResolverFactory getUriResolverFactory() {
    return uriResolverFactory;
  }

  public ErrorListener getTransformerErrorListener() {
    return transformerErrorListener;
  }
}

