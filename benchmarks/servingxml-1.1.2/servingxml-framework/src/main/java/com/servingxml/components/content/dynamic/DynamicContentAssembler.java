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

import java.lang.reflect.Method;

import org.w3c.dom.Element;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.Cacheable;
import com.servingxml.components.content.Content;
import com.servingxml.components.content.ContentPrefilter;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.util.InstanceFactory;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.components.xsltconfig.XsltConfiguration;

/**
 * The <code>DynamicContentAssembler</code> implements an assembler for
 * assembling dynamic <code>Content</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DynamicContentAssembler {
  static private final String KEY = "key";

  private static final String ON_REQUEST = "onRequest";
  private Name documentName = Name.EMPTY;
  private Class javaClass = null;
  private XsltConfiguration xsltConfiguration;
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
  }

  public void setName(Name documentName) {
    this.documentName = documentName;
  }

  public void injectComponent(XsltConfiguration xsltConfiguration) {
    this.xsltConfiguration = xsltConfiguration;
  }

  public void setHandlerClass(Class javaClass) {
    this.javaClass = javaClass;
  }

  public void setClassName(Class javaClass) {
    this.javaClass = javaClass;
  }

  public void setClass(Class javaClass) {
    this.javaClass = javaClass;
  }

  public Content assemble(ConfigurationContext context) {

    if (xsltConfiguration == null) {
      xsltConfiguration = XsltConfiguration.getDefault();
    }

    Element documentElement = context.getElement();

    if (javaClass == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                                 context.getElement().getTagName(),"class");
      throw new ServingXmlException(message);
    }

    //  key
    KeyIdentifierImpl identifier = new KeyIdentifierImpl();
    Element keyNode = DomHelper.getFirstChildElement(documentElement,
                                                     SystemConstants.SERVINGXML_NS_URI,KEY);
    if (keyNode != null) {
      identifier.initialize(keyNode);
    }
    if (identifier == null) {
      throw new ServingXmlException("Identifier cannot be null.");
    }
    InstanceFactory handlerMaker = new InstanceFactory(javaClass,DynamicContentHandler.class);
    Object handler = handlerMaker.createInstance();
    if (!(handler instanceof DynamicContentHandler)) {
      throw new ServingXmlException("DynamicContentHandler " + javaClass + " does not implement DynamicContentHandler.");
    }

    Class[] parameterTypes = {ServiceContext.class,null,ContentWriter.class};
    //Method handlerMethod = Reflection.findMethod(handler.getClass(),ON_REQUEST,parameterTypes);
    Method handlerMethod;
    try {
      handlerMethod = handler.getClass().getMethod(ON_REQUEST,parameterTypes);
    } catch (SecurityException exception) {
      throw new ServingXmlException("Cannot find method " + javaClass + " onRequest(ServiceContext, MyParameters, ContentWriter)");
    } catch (NoSuchMethodException exception) {
      throw new ServingXmlException("Cannot find method " + javaClass + " onRequest(ServiceContext, MyParameters, ContentWriter)");
    }
    DynamicChangeable dynamicExpirable = null;
    if (handler instanceof Cacheable) {
      dynamicExpirable = new DynamicChangeableProxy((Cacheable)handler);
    } else {
      dynamicExpirable = DynamicChangeable.ALWAYS_CHANGED;
    }
    Class[] argTypes = handlerMethod.getParameterTypes();
    Class parametersType = argTypes[1];
    if (!parametersType.isInterface()) {
      throw new ServingXmlException("Parameters argument of " + javaClass + "." + ON_REQUEST + " is not an interface.");
    }

    RecordMetaDataFactory parameterMetaDataFactory = RecordMetaDataFactory.createInstance();
    parameterMetaDataFactory.addInterface(parametersType);
    RecordMetaData recordMetaData = parameterMetaDataFactory.createRecordMetaData();

    Content contentFactory = new DynamicContent(documentName, 
                                                (DynamicContentHandler)handler, handlerMethod, recordMetaData, 
                                                dynamicExpirable, identifier, xsltConfiguration.getOutputPropertyFactories());
    if (parameterDescriptors.length > 0) {
      contentFactory = new ContentPrefilter(contentFactory,parameterDescriptors);
    }
    return contentFactory;
  }
}



