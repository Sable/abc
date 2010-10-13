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

package com.servingxml.ioc.components; 

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.lang.reflect.Array;

import org.w3c.dom.Element;

import com.servingxml.util.ServingXmlException;        
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.InstanceFactory;
import com.servingxml.ioc.components.ConfigurationContext;

public class ComponentAssemblerImpl implements ComponentAssembler {
  private static final String ASSEMBLE = "assemble";
  private static final Class[] PARAMETER_TYPES = new Class[]{ConfigurationContext.class};
  private static final String INJECT_COMPONENT = "injectComponent";

  private static final StringConverter stringConverter = new StringConverter();

  private final Class assemblerClass;
  private final Method assembleMethod;
  private final PropertyDescriptor[] attributeDescriptors;

  public ComponentAssemblerImpl(Class assemblerClass, Method assembleMethod, 
    PropertyDescriptor[] attributeDescriptors) {
    this.assemblerClass = assemblerClass;
    this.assembleMethod = assembleMethod;
    this.attributeDescriptors = attributeDescriptors;
  }

  public static ComponentAssembler newInstance(String assemblerClassName) {
    Class assemblerClass;
    try {
      assemblerClass = Thread.currentThread().getContextClassLoader().loadClass(assemblerClassName);
    } catch (java.lang.ClassNotFoundException e) {
      String message = "2 Cannot find assembler class " + assemblerClassName;
      throw new ServingXmlException(message);
    }

    List<PropertyDescriptor> descriptorList = new ArrayList<PropertyDescriptor>();
    try {
      //Method assembleMethod = Reflection.findMethod(assemblerClass,
      //                          ASSEMBLE, PARAMETER_TYPES);
      Method assembleMethod = assemblerClass.getMethod(ASSEMBLE, PARAMETER_TYPES);
      if (assembleMethod == null) {
        throw new ServingXmlException("Assmbler class " + assemblerClass.getName() + " must have method assemble");
      }

      BeanInfo info = Introspector.getBeanInfo(assemblerClass);
      PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
      for (int i = 0; i < descriptors.length; ++i) {
        PropertyDescriptor descriptor = descriptors[i];
        Method method = descriptor.getWriteMethod();
        if (method != null) {
          descriptorList.add(descriptor);
        }
      }
      PropertyDescriptor[] attributeDescriptors = new PropertyDescriptor[descriptorList.size()];
      attributeDescriptors = descriptorList.toArray(attributeDescriptors);
      return new ComponentAssemblerImpl(assemblerClass, assembleMethod, attributeDescriptors);
    } catch (Exception e) {
      ServingXmlException pe = new ServingXmlException(e.getMessage(),e);
      throw(pe);
    }
  }

  public Class getType() {
    return assembleMethod.getReturnType();
  }

  public boolean isAssemblerOf(Class type) {
    return type.isAssignableFrom(assembleMethod.getReturnType());
  }

  public Object assemble(ConfigurationContext context) {
    try {
      Element element = context.getElement();

      InstanceFactory assemblerFactory = new InstanceFactory(assemblerClass);
      Object componentAssembler = assemblerFactory.createInstance();
      ComponentInjector[] componentInjectors = getComponentInjectors(context);
      for (int i = 0; i < componentInjectors.length; ++i) {
        ComponentInjector injector = componentInjectors[i];
        injector.injectComponent(context,componentAssembler);
      }

      for (int i = 0; i < attributeDescriptors.length; ++i) {
        PropertyDescriptor descriptor = attributeDescriptors[i];
        String value = DomHelper.getAttribute(descriptor.getName(),element);
        if (value != null) {
          Class type = descriptor.getPropertyType();
          Object arg = stringConverter.convertString(context.getQnameContext(),value,type);
          if (arg == null) {
            throw new ServingXmlException("Cannot convert attribute");
          }

          Method method = descriptor.getWriteMethod();
          try {
            method.invoke(componentAssembler,new Object[]{arg});
          } catch (java.lang.reflect.InvocationTargetException e) {
            ServingXmlException sxe = ServingXmlException.fromInvocationTargetException(e);
            throw sxe;
          } catch (java.lang.IllegalAccessException e) {
            throw new ServingXmlException(e.getMessage(),e);
          }
        }
      }

      Object[] args = new Object[]{context};
      Object ref = assembleMethod.invoke(componentAssembler,args);
      return ref;
    } catch (InvocationTargetException e) {
      ServingXmlException sxe = ServingXmlException.fromInvocationTargetException(e);
      throw sxe;
    } catch (java.lang.IllegalAccessException e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
  }

  private ComponentInjector[] getComponentInjectors(ConfigurationContext context) {

    Object[] resources = getChildResources(context);

    Element contextElement = context.getElement();

    Method[] methods = assemblerClass.getMethods();

    ArrayList<ComponentInjector> componentInjectorList = new ArrayList<ComponentInjector>();
    for (int i = 0; i < methods.length; ++i) {
      Method method = methods[i];
      String methodName = method.getName();
      if (methodName.equals(INJECT_COMPONENT)) {
        //System.out.println(getClass().getName()+".getComponentInjectors Attempting to inject component");
        ComponentInjector componentInjector = getServiceComponentInjector(context, resources, method);
        //if (componentInjector != null) {
          //System.out.println(getClass().getName()+".getComponentInjectors It's a service component");
        //}
        if (componentInjector == null) {
          componentInjector = getConfigurationComponentInjector(context, method);
        }
        if (componentInjector != null) {
          componentInjectorList.add(componentInjector);
        }
      }
    }

    ComponentInjector[] componentInjectors = new ComponentInjector[componentInjectorList.size()];
    componentInjectors = (ComponentInjector[])componentInjectorList.toArray(componentInjectors);

    return componentInjectors;
  }

  private ComponentInjector getConfigurationComponentInjector(ConfigurationContext context, 
    Method method) {
    ConfigurationComponentInjector componentInjector = null;
    Class[] parameterTypes = method.getParameterTypes();
    Class propertyType = parameterTypes[0];
    Object configurationComponent = context.getConfigurationComponent(propertyType);
    if (configurationComponent != null) {
      componentInjector = new ConfigurationComponentInjector(method,configurationComponent);
    }
    return componentInjector;
  }

  private ComponentInjector getServiceComponentInjector(ConfigurationContext context, 
    Object[] resources, Method method) {
    //System.out.println(getClass().getName()+".getServiceComponentInjector assemberClass=" + assemblerClass.getName() +
     // ", number attribute descriptors = " + attributeDescriptors.length);

    ComponentInjector componentInjector = null;

    Element contextElement = context.getElement();

    Class[] parameterTypes = method.getParameterTypes();

    Class javaInterface = parameterTypes[0];
    //System.out.println(getClass().getName()+".getServiceComponentInjector " + javaInterface.getName());

    if (javaInterface.isArray()) {
      List<Object> argList = new ArrayList<Object>();

      Class componentType = javaInterface.getComponentType();
      //System.out.println(getClass().getName()+".getServiceComponentInjector componentType is " + componentType.getName());

      //System.out.println("Number of resources is "+resources.length);
      for (int j = 0; j < resources.length; ++j) {
        Object o = resources[j];
        //System.out.println("Resource is " + o.getClass().getName());
        if (componentType.isAssignableFrom(o.getClass())) {
          //System.out.println("is assignable");
          argList.add(o);
        }
      }

      if (argList.size() > 0) {
        Object[] args = (Object[])Array.newInstance(componentType,argList.size());
        args = argList.toArray(args);
        componentInjector = new ArrayServiceComponentInjector(method,args);
      }
    } else {
      Object arg = null;
      boolean done = false;
      //System.out.println("Number of resources is "+resources.length);
      for (int j = 0; !done && j < resources.length; ++j) {
        Object o = resources[j];
        //System.out.println("Resource is " + o.getClass().getName());
        if (javaInterface.isAssignableFrom(o.getClass())) {
          arg = o;
          done = true;
          //System.out.println("is assignable");
        }
      }
      if (arg != null) {
        componentInjector = new ScalarServiceComponentInjector(method,arg);
      }
    }

    return componentInjector;
  }

  private Object[] getChildResources(ConfigurationContext context) {

    Element contextElement = context.getElement();

    List resourceList = new ArrayList();
    for (Iterator iter = DomHelper.createChildElementIterator(contextElement); iter.hasNext();) {
      Element element = (Element)iter.next();
      //System.out.println(getClass().getName()+".getChildResources " + contextElement.getTagName() + " about to get service component for " + element.getTagName());
      Object instance = context.getServiceComponent(element);
      if (instance != null) {
        //System.out.println(getClass().getName()+".getChildResources but result is null");

        resourceList.add(instance);
      }
    }
    Object[] resources = new Object[resourceList.size()];
    resources = resourceList.toArray(resources);

    return resources;
  }
}

