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

package samples.books;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.Cacheable;
import com.servingxml.components.content.dynamic.AttributeSet;
import com.servingxml.components.content.dynamic.ContentWriter;
import com.servingxml.components.content.dynamic.DynamicContentHandler;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class CategoryHandler implements DynamicContentHandler, Cacheable {
  private static final String sourceClass = CategoryHandler.class.getName();

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

  //  All data members in a DynamicContentHandler should be declared as final
  //  and initialized in the constructor.
  //  The onRequest method must be reentrant
  private final HashMap categoryMap;
  private final String namespace = "";

  public CategoryHandler() {
    categoryMap = new HashMap();
    categoryMap.put("S", new Category("S","Science"));
    categoryMap.put("C", new Category("C","Computing"));
    categoryMap.put("CHILD", new Category("CHILD","Children"));
    categoryMap.put("F", new Category("F","Fiction"));
    categoryMap.put("P", new Category("P","Philospohy"));
    categoryMap.put("R", new Category("R","Religion"));
  }
  
  public void onRequest(ServiceContext context, Book parameters, 
  ContentWriter contentWriter) {

    final String sourceMethod = "onRequest";
    
    context.trace(sourceClass,sourceMethod,"parameters = " + parameters.getCategory(),Level.FINER);

    //AttributeSet attributes = contentWriter.newAttributeSet();
    //attributes.addAttribute(namespace,"desc","Miscellaneous categories");
    contentWriter.startElement(namespace,"categories");             
    //contentWriter.insertValue("A list of categories");
      
    String id = parameters.getCategory();
    if (id != null) {
      context.trace(sourceClass,sourceMethod,"category = " + id,Level.FINER);
    } else {
      context.trace(sourceClass,sourceMethod,"category is NULL",Level.FINER);
    }

    if (id != null && id.length() != 0) {
      if (id.equals("all")) {
        Collection values = categoryMap.values();
        Iterator iter = values.iterator();
        while (iter.hasNext()) {
          Category category = (Category)iter.next();
          context.trace(sourceClass,sourceMethod,"desc = " + category.getDescription(),Level.FINE);
          processCategory(category,contentWriter);
        }
      } else {
        Category category = (Category)categoryMap.get(id);
        context.trace(sourceClass,sourceMethod,"id = " + id + ", desc = " + category.getDescription(),Level.FINE);
        processCategory(category,contentWriter);
      }
    }

    contentWriter.endElement(namespace,"categories");
  }

  private void processCategory(Category category, ContentWriter contentWriter) {
    AttributeSet attributes = contentWriter.newAttributeSet();
    attributes.addAttribute(namespace,"code",category.getCode());
    attributes.addAttribute(namespace,"desc",category.getDescription());
    contentWriter.startEndElement(namespace,"category",attributes); 
  }

  public long getLastModified(Book key, long timestamp, long elapsed) {

    // changed if older than 10 seconds
    return elapsed > 10000 ? timestamp : timestamp + elapsed;  
  }
}
