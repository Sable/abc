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

import java.util.ArrayList;
import java.util.Iterator;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.content.dynamic.AttributeSet;
import com.servingxml.components.content.dynamic.ContentWriter;
import com.servingxml.components.content.dynamic.DynamicContentHandler;

/**
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class BookCatalog implements DynamicContentHandler {

  static class BookEntry {
    private final String bookId;
    private final String categoryCode;
    private final String title;
    private final String publisher;
    private final String author;
    private final double price;
    private final int quantity;
    private final double tax;

    public BookEntry(String bookId, String categoryCode, String title, String publisher, 
    String author, double price, int quantity, double tax) {
      this.bookId = bookId;
      this.categoryCode = categoryCode;
      this.title = title;
      this.publisher = publisher;
      this.author = author;
      this.price = price;
      this.quantity = quantity;
      this.tax = tax;
    }

    public String getBookId() {
      return bookId;
    }

    public String getCategoryCode() {
      return categoryCode;
    }

    public String getTitle() {
      return title;
    }

    public String getPublisher() {
      return publisher;
    }

    public String getAuthor() {
      return author;
    }

    public double getPrice() {
      return price;
    }

    public int getQuantity() {
      return quantity;
    }

    public double getTax() {
      return tax;
    }
  }

  private final String namespace = "";
  private final ArrayList bookList;

  public BookCatalog() {
    bookList = new ArrayList();
    bookList.add(new BookEntry("1", "S","Number, the Language of Science", null, "Danzig", 5.95, 3, 0.0));
    bookList.add(new BookEntry("2", "F","Fierce Invalids Home From Hot Climates", "Bantam BookEntrys", "Robbins, Tom", 19.95, 5, 0.0));
    bookList.add(new BookEntry("3", "F","Pulp", "Black Sparrow Press", "Bukowski, Charles", 17.95, 5, 0.0));
    bookList.add(new BookEntry("4", "S","Language & the Science of Number", null, "Danzig", 8.95, 5, 0.0));
    bookList.add(new BookEntry("5", "S","Evolution of Complexity in Animal Culture", null, "Bonner", 5.95, 2, 0.0));
    bookList.add(new BookEntry("6", "CHILD","Harry Potter and Philosopher's Stone", "Raincoast BookEntrys", "J.K. Rowling", 31.50, 0, 0.0));
    bookList.add(new BookEntry("7", "CHILD","Harry Potter and the Chamber of Secrets", "Raincoast BookEntrys", "J.K. Rowling", 31.50, 0, 0.0));
    bookList.add(new BookEntry("8", "CHILD","Harry Potter and the Prisoner of Azkaban", "Raincoast BookEntrys", "J.K. Rowling", 31.50, 0, 0.0));
    bookList.add(new BookEntry("9", "CHILD","Harry Potter and the Goblet of Fire", "Raincoast BookEntrys", "J.K. Rowling", 31.50, 0, 0.0));
    bookList.add(new BookEntry("10", "F","When We Were Very Young", null, "Milne, A. A.", 12.50, 1, 7.5));
    bookList.add(new BookEntry("11", "C","Java Servlet Programming", "O'Reilly", "Hunter, Jason", 65.95, 12, 0.0));
    bookList.add(new BookEntry("12", "C","Design Patterns", "Addison Wesley", "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", 49.95, 2, 12.5));
    bookList.add(new BookEntry("13", "C","creative html design", "New Riders", "Linda Weinman, William Weinman", 39.99, 2, 12.5));
    bookList.add(new BookEntry("14", "P","The Book of Nothing", "Johnathan Cape", "Barrow, John D.", 33.25, 2, 12.5));
    bookList.add(new BookEntry("15", "R","The Jesus Mysteries","Harmony BookEntrys", "Freke, Timothy, Peter Gandy", 33.25, 2, 12.5));
  }

  //  The onRequest method must be reentrant
  public void onRequest(ServiceContext context, Book parameters, 
  ContentWriter contentWriter) {
    
    String categoryCode = parameters.getCategory();
    if (categoryCode == null) {
      categoryCode = "";
    }
    AttributeSet attributes = contentWriter.newAttributeSet();
    attributes.addAttribute(namespace,"cat",categoryCode);
    contentWriter.startElement(namespace,"books",attributes);

    if (categoryCode.length() > 0) {
      
      for (Iterator iter = bookList.iterator(); iter.hasNext();) {
        BookEntry bookEntry = (BookEntry)iter.next();
        if (bookEntry.getCategoryCode().equals(categoryCode)) {
          writeBook(bookEntry,contentWriter);
        }
      }
    }

    contentWriter.endElement(namespace,"books");
  }

  private void writeBook(BookEntry bookEntry, ContentWriter contentWriter) {

    AttributeSet itemAttributes = contentWriter.newAttributeSet();
    itemAttributes.addAttribute(namespace,"bookId",bookEntry.getBookId());
    itemAttributes.addAttribute(namespace,"cat",bookEntry.getCategoryCode());
    contentWriter.startElement(namespace,"book",itemAttributes); 
      contentWriter.insertElement(namespace,"title", bookEntry.getTitle());
      contentWriter.insertElement(namespace,"author", bookEntry.getAuthor());
      if (bookEntry.getPublisher() != null) {
        contentWriter.insertElement(namespace,"publisher", bookEntry.getPublisher());
      }
      contentWriter.insertElement(namespace,"price", bookEntry.getPrice());
      contentWriter.insertElement(namespace,"quantity", bookEntry.getQuantity());
    contentWriter.endElement(namespace,"book");
  }
}

