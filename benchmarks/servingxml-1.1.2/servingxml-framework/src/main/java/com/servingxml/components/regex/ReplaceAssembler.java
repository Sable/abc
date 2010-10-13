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

import org.w3c.dom.Element;

import com.servingxml.util.ServingXmlException;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.xml.DomHelper;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.string.Stringable;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.string.StringFactoryCompiler;

/**
 * Implements a <tt>ReplaceAssembler</tt> for assembling 
 * <tt>FindAndReplace</tt> or <tt>MatchAndReplace</tt> 
 * objects. 
 *
 *  
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ReplaceAssembler {

  private String pattern = null;
  private String searchFor = null;
  private String replaceWith = null;
  private PatternMatcherFactory patternMatcherFactory = PatternMatcherFactory.DEFAULT;

  public void setMatch(String pattern) {
    this.pattern = pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  public void setSearchFor(String searchFor) {
    this.searchFor = searchFor;
  }

  public void setReplaceWith(String replaceWith) {
    this.replaceWith = replaceWith;
  }

  public void injectComponent(PatternMatcherFactory patternMatcherFactory) {
    this.patternMatcherFactory = patternMatcherFactory;
  }
  
  public StringFactory assemble(final ConfigurationContext context) {
    
    if (pattern == null && searchFor == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_CHOICE_REQUIRED,
        context.getElement().getTagName(),"pattern, searchFor");
      throw new ServingXmlException(message);
    }

    if (replaceWith == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
        context.getElement().getTagName(),"replaceWith");
      throw new ServingXmlException(message);
    }

    StringFactory stringFactory = StringFactoryCompiler.fromStringables(context, context.getElement());

    StringFactory replacer;
    if (pattern == null) {
      PatternMatcher patternMatcher = patternMatcherFactory.createPatternMatcher(searchFor);
      replacer = new FindAndReplace(patternMatcher,stringFactory,replaceWith);
    } else {
      PatternMatcher patternMatcher = patternMatcherFactory.createPatternMatcher(pattern);
      replacer = new MatchAndReplace(patternMatcher,stringFactory,replaceWith); 
    }

    return replacer;
  }
}

