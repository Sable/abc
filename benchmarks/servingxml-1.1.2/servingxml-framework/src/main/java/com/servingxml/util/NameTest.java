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

package com.servingxml.util;

import java.util.StringTokenizer;

/**
 *
 *  01/05/15
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 * 
 */

public abstract class NameTest {
  public static final NameTest ANY = new MatchesAny();
  public static final NameTest NONE = new MatchesNone();

  public boolean matches(Name name) {
    return matches(name.getNamespaceUri(),name.getLocalName());
  }

  public abstract boolean matches(String namespaceUri, String localName);

  public static NameTest parse(QnameContext context, String spaceSeparatedList) {
    StringTokenizer elementNameTokenizer = new StringTokenizer(spaceSeparatedList," ");
    NameTest tail = null;
    NameTest nameToken = NameTest.NONE;
    while (elementNameTokenizer.hasMoreTokens()) {
      String qname = elementNameTokenizer.nextToken();
      qname.trim();

      if (qname.length() == 0) {

      }
      if (qname.equals("*") || qname.equals("*:*")) {
        nameToken = NameTest.ANY;
      } else {
        String localName = "";
        String prefix = "";
        int pos = qname.indexOf(":");
        if (pos < 0) {
          localName = qname;
        } else {
          prefix = qname.substring(0, pos);
          localName = qname.substring(pos + 1);
        }
        if (prefix.equals("*")) {
          nameToken = new MatchesAnyNamespaceUri(localName);
        } else if (localName.equals("*")) {
          String namespaceUri = context.getPrefixMap().getNamespaceUri(prefix);
          if (namespaceUri == null) {
            namespaceUri = "";
          }
          nameToken = new MatchesAnyLocalName(namespaceUri);
        } else {
          String namespaceUri = context.getPrefixMap().getNamespaceUri(prefix);
          if (namespaceUri == null) {
            namespaceUri = "";
          }
          nameToken = new QualifiedNameToken(namespaceUri, localName);
        }
      }

      if (tail != null) {
        nameToken = new NameTestList(nameToken, tail);
      }
      tail = nameToken;
    }

    return nameToken;
  }

  static final class MatchesAny extends NameTest {
    public final boolean matches(String namespaceUri, String localName) {
      return true;
    }
  }

  static final class MatchesNone extends NameTest {
    public final boolean matches(String namespaceUri, String localName) {
      return false;
    }
  }

  static final class QualifiedNameToken extends NameTest {

    private final String namespaceUri;
    private final String localName;

    public QualifiedNameToken(String namespaceUri, String localName) {
      this.namespaceUri = namespaceUri;
      this.localName = localName;
    }

    public final boolean matches(String namespaceUri, String localName) {
      return this.namespaceUri.equals(namespaceUri) && this.localName.equals(localName);
    }
  }

  static final class MatchesAnyNamespaceUri extends NameTest {

    private final String localName;

    public MatchesAnyNamespaceUri(String localName) {
      this.localName = localName;
    }

    public final boolean matches(String namespaceUri, String localName) {
      return this.localName.equals(localName);
    }
  }

  static final class MatchesAnyLocalName extends NameTest {

    private final String namespaceUri;

    public MatchesAnyLocalName(String namespaceUri) {
      this.namespaceUri = namespaceUri;
    }

    public final boolean matches(String namespaceUri, String localName) {
      return this.namespaceUri.equals(namespaceUri);
    }
  }

}
