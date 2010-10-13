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

package com.servingxml.components.recordmapping;

import com.servingxml.util.ServingXmlException;
import com.servingxml.util.xml.XsltEvaluator;
import com.servingxml.util.xml.XsltEvaluatorFactory;
import com.servingxml.util.xml.XsltEvaluatorFactoryImpl;
import com.servingxml.components.xsltconfig.XsltConfiguration;
import com.servingxml.util.PrefixMap;                 
import com.servingxml.util.QnameContext;
import com.servingxml.app.ServiceContext;

/**
 *                      
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MultipleMapXmlFactory implements MapXmlFactory {

  private final MapXmlFactory[] recordMappingFactories;
  private final int headerCount;
  private final int bodyCount;
  private final int trailerCount;
  private final int onRecordCount;
  private final XsltEvaluator xsltEvaluator;
  private int startRecordOrGroup;
  private int endRecordOrGroup;

  public MultipleMapXmlFactory(QnameContext context, XsltConfiguration xsltConfiguration,
    MapXmlFactory[] recordMappingFactories) {
    this.recordMappingFactories = recordMappingFactories;

    startRecordOrGroup = recordMappingFactories.length;
    endRecordOrGroup = recordMappingFactories.length;
    int groupCount = 0;                                         
    int onRecordCount = 0;                                         

    for (int i = 0; i < recordMappingFactories.length; ++i) {
      MapXmlFactory childFactory = recordMappingFactories[i];
      if (startRecordOrGroup == recordMappingFactories.length) {
        if (childFactory.isGroup() || childFactory.isRecord()) {
          startRecordOrGroup = i;
        }
      }
      if (childFactory.isGroup() || childFactory.isRecord()) {
        endRecordOrGroup = i + 1;
      }
      if (childFactory.isGroup()) {
        ++groupCount;
      }
      if (childFactory.isRecord()) {
        ++onRecordCount;
      }
    }

    XsltEvaluatorFactory xsltEvaluatorFactory = new XsltEvaluatorFactoryImpl();
    for (int i = 0; i < recordMappingFactories.length; ++i) {
      MapXmlFactory childFactory = recordMappingFactories[i];
      String mode = "field."+i;
      childFactory.addToXsltEvaluator(mode, xsltEvaluatorFactory);
    }
    this.xsltEvaluator = xsltEvaluatorFactory.createXsltEvaluator(context, xsltConfiguration);

    this.headerCount = startRecordOrGroup;
    this.bodyCount = endRecordOrGroup - startRecordOrGroup;
    this.trailerCount = recordMappingFactories.length - endRecordOrGroup;
    this.onRecordCount = onRecordCount;
  }

  public MapXml createMapXml(ServiceContext context) {

    xsltEvaluator.setUriResolverFactory(context.getUriResolverFactory());
    //System.out.println("MultipleMapXmlFactory.createMapXml");

    MapXml rm = null;

    if (headerCount == recordMappingFactories.length) {
      MapXml[] children = new MapXml[recordMappingFactories.length];
      for (int i = 0; i < recordMappingFactories.length; ++i) {
        MapXmlFactory childFactory = recordMappingFactories[i];
        //System.out.println(getClass().getName()+".createMapXml (simple) " + childFactory.getClass().getName());
        children[i] = childFactory.createMapXml(context);
      }
      //System.out.println(getClass().getName()+".createMapXml SimpleRecordMapContainer");
      rm = new SimpleRecordMapContainer(children, xsltEvaluator);
    } else {
      MapXml[] children1 = new MapXml[headerCount];
      MapXml[] children2 = new MapXml[bodyCount];
      MapXml[] children3 = new MapXml[trailerCount];

      int index1 = 0;
      int index2 = 0;
      int index3 = 0;
      for (int i = 0; i < recordMappingFactories.length; ++i) {
        MapXmlFactory childFactory = recordMappingFactories[i];
        if (i >= startRecordOrGroup && i < endRecordOrGroup) {
          children2[index2++] = childFactory.createMapXml(context);
          //System.out.println(getClass().getName()+".createMapXml 2 " + childFactory.getClass().getName());
        } else if (i < startRecordOrGroup) {
          children1[index1++] = childFactory.createMapXml(context);
          //System.out.println(getClass().getName()+".createMapXml 1 " + childFactory.getClass().getName());
        } else {
          children3[index3++] = childFactory.createMapXml(context);
          //System.out.println(getClass().getName()+".createMapXml 3 " + childFactory.getClass().getName());
        }
      }
      if (bodyCount == 1 || bodyCount == onRecordCount) {
        rm = new SimpleGroupingRecordMapContainer(children1, children2, children3, xsltEvaluator);
      }

      rm = new MultipleGroupingRecordMapContainer(children1, children2, children3, xsltEvaluator);
      //System.out.println("MultipleMapXmlFactory.createMapXml MultipleGroupingRecordMapContainer");
    }


    return rm;
  }

  public boolean isGroup() {
    boolean group = false;

    for (int i = 0; !group && i < recordMappingFactories.length; ++i) {
      MapXmlFactory childFactory = recordMappingFactories[i];
      if (childFactory.isGroup() || childFactory.isRecord()) {
        group = true;
      }
    }

    return group;
  }

  public boolean isRecord() {
    return false;
  }

  public void addToXsltEvaluator(String mode, XsltEvaluatorFactory xsltEvaluatorFactory) {
  }
}

