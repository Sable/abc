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

import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.InstanceFactory;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class PatternMatcherFactoryAssembler {
  
  private Class patternMatcherClass = JavaPatternMatcher.class;

  public void setClass(Class patternMatcherClass) {
    this.patternMatcherClass = patternMatcherClass;
  }
  
  public PatternMatcherFactory assemble(ConfigurationContext context) {

    InstanceFactory instanceFactory = new InstanceFactory(patternMatcherClass, PatternMatcher.class);

    return new PatternMatcherFactoryImpl(instanceFactory);
  }
}


class PatternMatcherFactoryImpl implements PatternMatcherFactory {
  
  private static final Class[] CTOR_ARG_TYPES = new Class[]{String.class,Boolean.class,Boolean.class};

  private final InstanceFactory instanceFactory;

  public PatternMatcherFactoryImpl(InstanceFactory instanceFactory) {
    this.instanceFactory = instanceFactory;
  }

  public PatternMatcher createPatternMatcher(String expression) {
    return (PatternMatcher)instanceFactory.createInstance(CTOR_ARG_TYPES,
      new Object[]{expression, true, true});
  }

  public PatternMatcher createPatternMatcher(String expression, boolean caseSensitive, boolean useRegex) {
    return (PatternMatcher)instanceFactory.createInstance(CTOR_ARG_TYPES,
      new Object[]{expression, caseSensitive, useRegex});
  }
}

