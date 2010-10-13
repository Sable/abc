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

package com.servingxml.components.regex;

import java.io.StringReader;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.TestResult;
import junit.framework.TestFailure;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

import com.servingxml.util.MutableNameTable;
import com.servingxml.util.NameTableImpl;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.ioc.resources.MutableResourceTable;
import com.servingxml.ioc.resources.ResourceTableImpl;
import com.servingxml.ioc.resources.RootConfigurationContext;
import com.servingxml.ioc.resources.SimpleIocContainer;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ReplaceTest extends TestCase {
  
  private SimpleIocContainer resources;
  private MutableNameTable nameTable;
  private MutableResourceTable resourceTable;
  private Element iocContainerElement;

  public ReplaceTest(String name) {
    super(name);
  }
  protected void setUp() throws Exception {
/*
    resources = new SimpleIocContainer();
    nameTable = new NameTableImpl(resources.getNameTable());
    resourceTable = new ResourceTableImpl();

    String componentString = "<sx:resources xmlns:myns=\"http://mycompany.com/mynames/\"  ns=\"http://mycompany.com/mynames/\" xmlns:sx=\"http://www.servingxml.com/ServingXML\"><sx:replace pattern='' select=''></sx:replace></sx:resources>";

    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setValidating(false);
    builderFactory.setNamespaceAware(true);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();

    InputSource inputSource = new InputSource(new StringReader(componentString));
    Document doc = builder.parse(inputSource);
    iocContainerElement = doc.getDocumentElement();
*/    
  }

  public void testSubstitution() throws Exception {
/*
    RootConfigurationContext configContext = new RootConfigurationContext(
      nameTable,resourceTable,"base",iocContainerElement);
    
    Element substituteElement = DomHelper.getFirstChildElement(iocContainerElement);
    RegexReplacerBuilder factory = new RegexReplacerBuilder();
    RegexReplacer substituteAction = factory.assemble(configContext);
*/    
  }
}

