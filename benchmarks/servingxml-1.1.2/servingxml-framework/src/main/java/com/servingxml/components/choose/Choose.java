/**
 *  ServingXML
 *  
 *  Copyright (C) 2006  Daniel Parker
 *    daniel.parker@servingxml.com 
 * 
 * Includes contributed code by Gordon Zhang for string interface support
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


package com.servingxml.components.choose;

import com.servingxml.components.content.Content;
import com.servingxml.components.recordio.RecordFilter;
import com.servingxml.components.recordio.RecordFilterAppender;
import com.servingxml.components.recordio.RecordFilterChain;
import com.servingxml.components.recordio.RecordPipeline;
import com.servingxml.components.recordmapping.MapXml;
import com.servingxml.components.recordmapping.MapXmlFactory;
import com.servingxml.components.string.StringFactory;
import com.servingxml.components.task.Task;
import com.servingxml.io.cache.Key;
import com.servingxml.io.saxsource.XmlReaderSaxSource;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.Record;    
import com.servingxml.util.xml.XsltEvaluatorFactory;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface Choose 
extends Task, Content, RecordFilterAppender, MapXmlFactory, StringFactory {
}

