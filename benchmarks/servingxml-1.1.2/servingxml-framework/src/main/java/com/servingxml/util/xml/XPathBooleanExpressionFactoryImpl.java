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

package com.servingxml.util.xml;

import java.text.MessageFormat;
import java.io.StringReader;
import java.io.Reader;
import java.util.ArrayList;

import javax.xml.transform.TransformerException;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Templates;

import org.xml.sax.SAXException;

import com.servingxml.util.PrefixMap;
import com.servingxml.util.record.Record;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.util.QnameContext;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class XPathBooleanExpressionFactoryImpl implements XPathBooleanExpressionFactory {

  private final PrefixMap prefixMap;
  private final BooleanExpressionTransformerFactory xpathFactory;
  private final String baseUri;
  private final TransformerFactory transformerFactory;

  public XPathBooleanExpressionFactoryImpl(QnameContext context, 
  String expression, String xsltVersion, String baseUri, TransformerFactory transformerFactory) {
    this(context,expression,"/*", xsltVersion, baseUri, transformerFactory);
  }    

  public XPathBooleanExpressionFactoryImpl(QnameContext context, 
  String expression, String current, String xsltVersion, String baseUri, TransformerFactory transformerFactory) {

    this.prefixMap = context.getPrefixMap();
    this.baseUri = baseUri;
    this.transformerFactory = transformerFactory;

    if (expression == null || expression.length() == 0) {
      throw new ServingXmlException("XPath expression cannot be empty or null.");
    }

    this.xpathFactory = new BooleanExpressionTransformerFactory(context, 
      expression, current, xsltVersion, transformerFactory);
  }

  public XPathBooleanExpression createXPathBooleanExpression() {
    return new XPathBooleanExpressionImpl(prefixMap, baseUri, xpathFactory);
  }
}
                
class BooleanExpressionTransformerFactory {
  private static final String testStyleFormat = 
    "<xsl:transform xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" {0} version=\"{1}\"> " +
    "<xsl:output method=\"text\"/>" +
    "{2}" +
    "<xsl:template match=\"{3}\"> " +
    "<xsl:if test=\"{4}\">" +                          
    "<success>true</success>" +
    "</xsl:if>" +
    "</xsl:template> " +
    "</xsl:transform>";
  
  private Name[] parameterNames;
  private Templates testTemplates;
  private final TransformerFactory transformerFactory;
  private final String expression;

  public BooleanExpressionTransformerFactory(QnameContext context,
  String expr, String current, String xsltVersion, TransformerFactory transformerFactory) {

    this.expression = escape(expr);
    this.transformerFactory = transformerFactory;

    ArrayList<Name> parameterNameList = new ArrayList<Name>();

    String prefixDeclarations = context.getPrefixMap().getPrefixDeclarationString();

    StringBuilder buf = new StringBuilder();
    boolean noMoreParams = false;
    int index = 0;
    while (!noMoreParams) {
      index = expression.indexOf('$',index);
      if (index == -1) {
        noMoreParams = true;
        continue;
      }
      ++index;
      int beginIndex = index;
      if (!Character.isLetter(expression.charAt(index))) {
        continue;
      }
      ++index;

      boolean doneParam = false;
      while (!noMoreParams && !doneParam) {
        if (index >= expression.length()) {
          noMoreParams = true;
          continue;
        }
        char c = expression.charAt(index);
        if (!(Character.isLetterOrDigit(c) || c == '_' || c == '-' || c==':')) {
          doneParam = true;
          continue;
        }
        ++index;
      }
      int endIndex = index;
      String qname = expression.substring(beginIndex,endIndex);
      Name paramName = context.createName(qname);
      parameterNameList.add(paramName);
      buf.append("<xsl:param name=\"");
      buf.append(qname);
      buf.append("\"/>");
    }

    this.parameterNames = new Name[parameterNameList.size()];
    this.parameterNames = parameterNameList.toArray(parameterNames);

    Object[] args = {prefixDeclarations, xsltVersion, buf.toString(), current, expression};
    String testStyle = MessageFormat.format(testStyleFormat,args);
    try {
      Reader reader = new StringReader(testStyle);
      this.testTemplates = transformerFactory.newTemplates(new StreamSource(reader,""));
    } catch (javax.xml.transform.TransformerException e) {
      final String message = "Failed to compile test style " + testStyle;
      throw new ServingXmlException(message,e);
    }
  }

  public final Transformer newTransformer(Record parameters) {

    try {
      final Transformer testStyleTransformer = testTemplates.newTransformer();

      for (int i = 0; i < parameterNames.length; ++i) {
        Name parameterName = parameterNames[i];
        String value = parameters.getString(parameterName);
        if (value != null) {
          testStyleTransformer.setParameter(parameterName.toString(),value);
        }
      }

      return testStyleTransformer;
    } catch (TransformerException te) {
      Throwable cause = te;
      if (te.getCause() != null) {
        cause = te.getCause();
        if (cause instanceof SAXException) {
          SAXException se = (SAXException)cause;
          if (se != null && se.getException() != null && se.getException().getMessage() != null) {
            cause = se.getException();
          }
        }
      }
      if (cause instanceof ServingXmlException) {
        throw (ServingXmlException)cause;
      } else {
        throw new ServingXmlException(cause.getMessage(),cause);
      }
    }
  }

  public String escape(String s) {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < s.length(); ++i) {
          char c = s.charAt(i);
          switch (c) {
          case '<':
              buf.append("&lt;");
              break;
          case '>':
              buf.append("&gt;");
              break;
          case '"':
              buf.append("&quot;");
              break;
          case '&':
              buf.append("&amp;");
              break;
          default:
              buf.append(c);
              break;
          }
      }
      return buf.toString();
  }
  

  public String getExpression() {
    return expression;
  }
}

