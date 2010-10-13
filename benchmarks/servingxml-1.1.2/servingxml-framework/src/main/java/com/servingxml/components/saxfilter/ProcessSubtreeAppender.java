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

package com.servingxml.components.saxfilter;

import org.xml.sax.XMLFilter;

import com.servingxml.app.ServiceContext;
import com.servingxml.expr.saxpath.RestrictedMatchPattern;
import com.servingxml.expr.ExpressionException;
import com.servingxml.components.task.Task;
import com.servingxml.app.Flow;
import com.servingxml.app.xmlpipeline.XmlFilterChain;
import com.servingxml.components.saxfilter.AbstractXmlFilterAppender;
import com.servingxml.components.content.Content;
import com.servingxml.app.Environment;

class ProcessSubtreeAppender extends AbstractXmlFilterAppender implements Content {
  private final Environment env;
  private final RestrictedMatchPattern expr;
  private final Task[] tasks;

  public ProcessSubtreeAppender(Environment env, RestrictedMatchPattern expr, Task[] tasks) {
    this.env = env;
    this.expr = expr;
    this.tasks = tasks;
  }

  public void appendToXmlPipeline(ServiceContext context, Flow flow,
  XmlFilterChain pipeline) {
    Flow newFlow = env.augmentParametersOf(context,flow);
    //System.out.println(getClass().getName()+".appendToXmlPipeline enter");
    XMLFilter filter = new ProcessSubtree(env, context, newFlow, expr, tasks);
    pipeline.addXmlFilter(filter);
  }
}


