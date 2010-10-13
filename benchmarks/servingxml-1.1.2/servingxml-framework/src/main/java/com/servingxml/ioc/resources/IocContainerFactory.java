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

package com.servingxml.ioc.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.servingxml.ioc.components.ComponentDictionary;
import com.servingxml.ioc.components.ComponentTable;
import com.servingxml.ioc.components.ComponentTableBuilder;
import com.servingxml.ioc.components.ComponentTableImpl;
import com.servingxml.util.JarFileIterator;
import com.servingxml.util.MutableNameTable;
import com.servingxml.util.Name;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.UrlHelper;
import com.servingxml.util.record.Record;
import com.servingxml.util.xml.ReceiverContentHandler;

/**
 * A <code>IocContainerFactory</code> class creates instances of
 * {@link com.servingxml.ioc.resources.IocContainer}, e.g. 
* 
*  <pre> 
 * IocContainerFactory iocContainerFactory = new IocContainerFactory();
 * iocContainerFactory.loadComponentDefinitions();
 * 
 * IocContainer resources =
 *    iocContainerFactory.createIocContainer(configUrl,
 *                                           parameters);
 * resources = iocContainerFactory.createIocContainer(
 *                 resourcesUrl, parameters, resources);
 * </pre>
*  <p>
*  IocContainer objects encapsulate ServingXML resources
*  scripts.
*   </p>
*   <p>
 * A Java application can load any number of resources scripts
 * (mapping files), one, two, or more. In the example,
 * configUrl and resourcesUrl are both URLs that identify the
 * location of resources scripts. They can be absolute URLs, or
 * relative to an entry in the classpath. By convention, the
 * resources in the script identified by configUrl perform
 * configuration (e.g. configure the default XSLT processor),
 * for an example, refer to the servingxml.xml configuration
 * file which is used by the servingxml console app.
 * </p>
*  <p>
 * Note that the scripts are chained together. 
 * </p>
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class IocContainerFactory {
  private static final String COMPONENTS_MANIFEST_KEY = "ServingXML-Components";
  private static final Name componentTableName = new QualifiedName(SystemConstants.SERVINGXML_IOC_NS_URI,
    "components");

  private MutableNameTable nameTable;
  private ComponentTable componentTable;

  public IocContainerFactory() {
    componentTable = new ComponentTableImpl();
  }

  public ComponentDictionary getComponentDictionary() {
    return componentTable;
  }

  public MutableNameTable getNameTable() {
    return nameTable;
  }

  /**
   * Loads component defintions from metadata in the component jar
   * files found in the classpath.
   * 
   */

  public void loadComponentDefinitions() {

    this.nameTable = new NameTableImpl();

    //  Load component assemblers from resources in framework and extension jar files
    //  Passed key identifies manifest entry for locating resources.
    load(COMPONENTS_MANIFEST_KEY);
  }

  public void load(String manifestKey) {

    try {
      JarFileIterator.Command command = new RegisterComponentCommand();
      JarFileIterator.toEveryJarFile(manifestKey,command);
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }
    loadComponents();
  }

  /**
  * Loads component defintions from the supplied list of URL's for
  * component.xml files. You can use this version of 
  * <code>loadComponentDefinitions</code> in J2EE containers such 
  * as WebLogic that isolate the class loader. 
  * @param componentDefinitionUrls An array of URL objects 
    */

  public void loadComponentDefinitions(URL[] componentDefinitionUrls) {
    this.nameTable = new NameTableImpl();
    try {
      JarFileIterator.Command command = new RegisterComponentCommand();
      for (int i = 0; i < componentDefinitionUrls.length; ++i) {
        command.doEntry(componentDefinitionUrls[i]);
      }
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }

    load(componentDefinitionUrls);
    loadComponents();
  }

  //  Keysers Wonne
  public void load(URL[] componentDefinitionUrls) {
    try {
      JarFileIterator.Command command = new RegisterComponentCommand();
      for (int i = 0; i < componentDefinitionUrls.length; ++i) {
        command.doEntry(componentDefinitionUrls[i]);
      }
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(),e);
    }

    loadComponents();
  }

  //  Contribution by Keysers Wonne 
  private void loadComponents() {
    componentTable.initialize(nameTable);
  }

  /**
  * Loads component defintions from the supplied list of URL's for
  * component.xml files. You can use this version of 
  * <code>loadComponentDefinitions</code> in J2EE containers such 
  * as WebLogic that isolate the class loader. 
  * @param componentDefinitionUrls An array of Strings representing URLs.
  */

  public void loadComponentDefinitions(String[] componentDefinitionUrls) {

    //this.nameTable = new NameTableImpl();
    //this.componentTable = new ComponentAssemblerContainerImpl(nameTable);

    URL[] urls = new URL[componentDefinitionUrls.length];
    for (int i = 0; i < componentDefinitionUrls.length; ++i) {
      ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
      urls[i] = parentLoader.getResource(componentDefinitionUrls[i]);
      if (urls[i] == null) {
        urls[i] = ClassLoader.getSystemClassLoader().getResource(componentDefinitionUrls[i]);
        if (urls[i] == null) {
          throw new ServingXmlException("Cannot resolve URL from " + componentDefinitionUrls[i]);
        }
      }
    }

    loadComponentDefinitions(urls);
    //  Bug fix by Nicolas Denis 
  }             

  @Deprecated
  public IocContainer createIocContainer(String systemId) {
    return createIocContainer(systemId, Record.EMPTY);
  }

  /**
  * Creates an IocContainer from a URL.
  * @param systemId The URL.
  * @param parameters Configuartion parameters.
  */

  public IocContainer createIocContainer(String systemId, Record parameters) {
    //System.out.println(getClass().getName()+".createIocContainer (1)");

    URL resourcesUrl = UrlHelper.createUrl(systemId);

    SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    IocContainerImpl iocContainer = new IocContainerImpl(nameTable, componentTable, transformerFactory);
    iocContainer.loadResources(resourcesUrl, parameters);

    return iocContainer;
  }

  @Deprecated
  public IocContainer createIocContainer(String systemId, IocContainer parent) {
    return createIocContainer(systemId,Record.EMPTY,parent);
  }

  /**
  * Creates an IocContainer from a URL and a parent IocContainer.
  * @param systemId The URL.
  * @param parameters Configuartion parameters.
  * @param parent The parent IocContainer.
  */

  public IocContainer createIocContainer(String systemId, Record parameters, IocContainer parent) {
    //System.out.println(getClass().getName()+".createIocContainer (2)");

    URL resourcesUrl = UrlHelper.createUrl(systemId);

    IocContainerImpl iocContainer = new IocContainerImpl(componentTable, parent);
    iocContainer.loadResources(resourcesUrl, parameters);

    return iocContainer;
  }

  /**
  * Creates an IocContainer from a DOM {@link 
  * org.w3c.dom.Document}. 
  * @param resourcesDocument The Document.
  * @param parameters Configuartion parameters.
  */

  public IocContainer createIocContainer(Document resourcesDocument, Record parameters) {
    return createIocContainer(resourcesDocument,"",parameters);
  }

  /**
  * Creates an IocContainer from a DOM {@link 
  * org.w3c.dom.Document}. 
  * @param resourcesDocument The Document.
  * @param baseId A base URI against which relative URIs in the 
  *               document are resolved.
  * @param parameters Configuartion parameters.
  */

  public IocContainer createIocContainer(Document resourcesDocument, String baseId, Record parameters) {
    //System.out.println(getClass().getName()+".createIocContainer (2)");

    SAXTransformerFactory transformerFactory = (SAXTransformerFactory)TransformerFactory.newInstance();
    IocContainerImpl iocContainer = new IocContainerImpl(nameTable, componentTable, transformerFactory);
    iocContainer.loadResources(resourcesDocument, baseId, parameters);

    return iocContainer;
  }

  /**
  * Creates an IocContainer from a DOM {@link 
  * org.w3c.dom.Document} and a parent IocContainer. 
  * @param resourcesDocument The Document.
  * @param parameters Configuartion parameters.
  * @param parent The parent IocContainer.
  */

  public IocContainer createIocContainer(Document resourcesDocument, Record parameters, IocContainer parent) {
    return createIocContainer(resourcesDocument,"",parameters,parent);
  }

  /**
  * Creates an IocContainer from a DOM {@link 
  * org.w3c.dom.Document} and a parent IocContainer. 
  * @param resourcesDocument The Document.
  * @param baseId A base URI against which relative URIs in the 
  *               document are resolved.
  * @param parameters Configuartion parameters.
  * @param parent The parent IocContainer.
  */

  public IocContainer createIocContainer(Document resourcesDocument, String baseId, Record parameters, IocContainer parent) {
    //System.out.println(getClass().getName()+".createIocContainer (2)");

    IocContainerImpl iocContainer = new IocContainerImpl(componentTable, parent);
    iocContainer.loadResources(resourcesDocument, baseId, parameters);

    return iocContainer;
  }

  /**
  * Creates an IocContainer from an InputStream. 
  * @param is The InputStream.
  * @param parameters Configuartion parameters.
  */

  public IocContainer createIocContainer(InputStream is, 
                                         Record parameters) {
    return createIocContainer(is,"",parameters); 
  }

  /**
  * Creates an IocContainer from an InputStream. 
  * @param is The InputStream.
  * @param baseId A base URI against which relative URIs in the 
  *               document are resolved.
  * @param parameters Configuartion parameters.
  */

  public IocContainer createIocContainer(InputStream is, 
                                         String baseId, 
                                         Record parameters) {
    //System.out.println(getClass().getName()+".createIocContainer (2)");
    try {
      DocumentBuilderFactory resourcesBuilderFactory = DocumentBuilderFactory.newInstance();
      resourcesBuilderFactory.setValidating(false);
      resourcesBuilderFactory.setNamespaceAware(true);
      DocumentBuilder resourcesBuilder = resourcesBuilderFactory.newDocumentBuilder();
      Document resourcesDocument = resourcesBuilder.parse(is);

      IocContainer iocContainer = createIocContainer(resourcesDocument,baseId,parameters);

      return iocContainer;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  /**
  * Creates an IocContainer from an InputStream and a parent 
  * IocContainer. 
  * @param is The InputStream.
  * @param parameters Configuartion parameters.
  * @param parent The parent IocContainer.
  */

  public IocContainer createIocContainer(InputStream is, 
                                         Record parameters, 
                                         IocContainer parent) {
    return createIocContainer(is,"",parameters,parent);
  }

  /**
  * Creates an IocContainer from an InputStream and a parent 
  * IocContainer. 
  * @param is The InputStream.
  * @param baseId A base URI against which relative URIs in the 
  *               document are resolved.
  * @param parameters Configuartion parameters.
  * @param parent The parent IocContainer.
  */

  public IocContainer createIocContainer(InputStream is, 
                                         String baseId, 
                                         Record parameters, 
                                         IocContainer parent) {
    //System.out.println(getClass().getName()+".createIocContainer (2)");
    try {
      IocContainerImpl iocContainer = new IocContainerImpl(componentTable, parent);
      DocumentBuilderFactory resourcesBuilderFactory = DocumentBuilderFactory.newInstance();
      resourcesBuilderFactory.setValidating(false);
      resourcesBuilderFactory.setNamespaceAware(true);
      DocumentBuilder resourcesBuilder = resourcesBuilderFactory.newDocumentBuilder();
      Document resourcesDocument = resourcesBuilder.parse(is);
      iocContainer.loadResources(resourcesDocument, baseId, parameters);

      return iocContainer;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  public class RegisterComponentCommand implements JarFileIterator.Command {

    public void doEntry(URL url) {
      //System.out.println("ComponentAssemblerContainerImpl.doEntry " + url.toString());

      try {

        InputStream is = url.openConnection().getInputStream();

        ComponentTableBuilder componentTableBuilder = new ComponentTableBuilder(componentTableName,componentTable);
        ReceiverContentHandler parser = new ReceiverContentHandler(nameTable);
        parser.parse(is, componentTableBuilder);

      } catch (IOException e) {
        throw new ServingXmlException(e.getMessage(),e);
      }
    }
  }

  /**
  * Creates an IocContainer from a Reader. 
  * @param reader The Reader.
  * @param parameters Configuartion parameters.
  */

  public IocContainer createIocContainer(Reader reader, 
                                         Record parameters) {
    return createIocContainer(new InputSource(reader),"",parameters); 
  }

  /**
  * Creates an IocContainer from an InputStream. 
  * @param reader The Reader.
  * @param baseId A base URI against which relative URIs in the 
  *               document are resolved.
  * @param parameters Configuartion parameters.
  */

  public IocContainer createIocContainer(Reader reader, 
                                         String baseId, 
                                         Record parameters) {
    return createIocContainer(new InputSource(reader), baseId, parameters);
  }

  /**
  * Creates an IocContainer from an InputSource. 
  * @param is The InputSource.
  * @param baseId A base URI against which relative URIs in the 
  *               document are resolved.
  * @param parameters Configuartion parameters.
  */

  public IocContainer createIocContainer(InputSource is, 
                                         String baseId, 
                                         Record parameters) {
    //System.out.println(getClass().getName()+".createIocContainer (2)");
    try {
      DocumentBuilderFactory resourcesBuilderFactory = DocumentBuilderFactory.newInstance();
      resourcesBuilderFactory.setValidating(false);
      resourcesBuilderFactory.setNamespaceAware(true);
      DocumentBuilder resourcesBuilder = resourcesBuilderFactory.newDocumentBuilder();
      Document resourcesDocument = resourcesBuilder.parse(is);

      IocContainer iocContainer = createIocContainer(resourcesDocument,baseId,parameters);

      return iocContainer;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }

  /**
  * Creates an IocContainer from a Reader and a parent 
  * IocContainer. 
  * @param reader The Reader.
  * @param parameters Configuartion parameters.
  * @param parent The parent IocContainer.
  */

  public IocContainer createIocContainer(Reader reader, 
                                         Record parameters, 
                                         IocContainer parent) {
    return createIocContainer(new InputSource(reader),"",parameters,parent);
  }

  /**
  * Creates an IocContainer from a Reader and a parent 
  * IocContainer. 
  * @param reader The Reader.
  * @param baseId A base URI against which relative URIs in the 
  *               document are resolved.
  * @param parameters Configuartion parameters.
  * @param parent The parent IocContainer.
  */

  public IocContainer createIocContainer(Reader reader, 
                                         String baseId, 
                                         Record parameters, 
                                         IocContainer parent) {
    return createIocContainer(new InputSource(reader),baseId,parameters,parent);
  }

  /**
  * Creates an IocContainer from an InputSource and a parent 
  * IocContainer. 
  * @param is The InputSource.
  * @param baseId A base URI against which relative URIs in the 
  *               document are resolved.
  * @param parameters Configuartion parameters.
  * @param parent The parent IocContainer.
  */

  public IocContainer createIocContainer(InputSource is, 
                                         String baseId, 
                                         Record parameters, 
                                         IocContainer parent) {
    //System.out.println(getClass().getName()+".createIocContainer (2)");
    try {
      IocContainerImpl iocContainer = new IocContainerImpl(componentTable, parent);
      DocumentBuilderFactory resourcesBuilderFactory = DocumentBuilderFactory.newInstance();
      resourcesBuilderFactory.setValidating(false);
      resourcesBuilderFactory.setNamespaceAware(true);
      DocumentBuilder resourcesBuilder = resourcesBuilderFactory.newDocumentBuilder();
      Document resourcesDocument = resourcesBuilder.parse(is);
      iocContainer.loadResources(resourcesDocument, baseId, parameters);

      return iocContainer;
    } catch (ServingXmlException e) {
      throw e;
    } catch (Exception e) {
      throw new ServingXmlException(e.getMessage(), e);
    }
  }
}

