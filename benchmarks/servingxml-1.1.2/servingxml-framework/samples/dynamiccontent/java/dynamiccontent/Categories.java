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

package dynamiccontent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.dynamic.AttributeSet;
import com.servingxml.components.content.dynamic.ContentWriter;
import com.servingxml.components.content.dynamic.DynamicContentHandler;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class Categories implements DynamicContentHandler {

  static class Category {
    private final String code;
    private final String description;

    public Category(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }
  }

  private final HashMap categoryMap;
  private final String namespace = "";

  public Categories() {
    categoryMap = new HashMap();
    categoryMap.put("S", new Category("S","Science"));
    categoryMap.put("C", new Category("C","Computing"));
    categoryMap.put("CHILD", new Category("CHILD","Children"));
    categoryMap.put("F", new Category("F","Fiction"));
    categoryMap.put("P", new Category("P","Philospohy"));
    categoryMap.put("R", new Category("R","Religion"));
  }
  
  //  The onRequest method must be reentrant
  public void onRequest(ServiceContext context, Book parameters, 
  ContentWriter contentWriter) {

    contentWriter.startElement(namespace,"categories");             
      
    String id = parameters.getCategory();

    if (id != null && id.length() != 0) {
      if (id.equals("all")) {
        Collection values = categoryMap.values();
        
        for (Iterator iter = values.iterator(); iter.hasNext();) {
          Category category = (Category)iter.next();
          writeCategory(category,contentWriter);
        }
      } else {
        Category category = (Category)categoryMap.get(id);
        writeCategory(category,contentWriter);
      }
    }

    contentWriter.endElement(namespace,"categories");
  }

  private void writeCategory(Category category, ContentWriter contentWriter) {
    AttributeSet attributes = contentWriter.newAttributeSet();
    attributes.addAttribute(namespace,"code",category.getCode());
    attributes.addAttribute(namespace,"desc",category.getDescription());
    contentWriter.startEndElement(namespace,"category",attributes); 
  }
}
