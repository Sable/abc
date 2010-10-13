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

package com.servingxml.components.directoryreader;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;

import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.common.TrueFalseEnum;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.recordio.RecordReaderFactoryPrefilter;
import com.servingxml.components.recordio.RecordReaderFactory;

/**
 * The <code>DirectoryReaderFactoryAssembler</code> implements an assembler for
 * assembling directory reader factories.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class DirectoryReaderFactoryAssembler {

  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private String directory = null;
  private String recurse = TrueFalseEnum.FALSE.toString();
  private long maxItems = Long.MAX_VALUE;
  private FileFilter fileFilter;

  public DirectoryReaderFactoryAssembler() {
    this.fileFilter = new FileFilter() {
      public boolean accept(File pathname) {
        return true;
      }
    };
  }

  public void setRecurse(String recurse) {
    this.recurse = recurse;
  }

  public void setMaxItems(long maxItems) {
    this.maxItems = maxItems;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void injectComponent(FileFilter fileFilter) {
    this.fileFilter = fileFilter;
  }

  public RecordReaderFactory assemble(ConfigurationContext context) {

    if (directory == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_REQUIRED,
                                                                 context.getElement().getTagName(), "directory");
      throw new ServingXmlException(message);
    }
    SubstitutionExpr dirResolver = SubstitutionExpr.parseString(context.getQnameContext(),directory);

    TrueFalseEnum recurseIndicator;
    try {
      recurseIndicator = TrueFalseEnum.parse(recurse);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
                                                                 context.getElement().getTagName(), "recurse");
      e = e.supplementMessage(message);
      throw e;
    }

    File parent = null;
    try {
      URI uri = new URI(context.getQnameContext().getBase());
      String scheme = uri.getScheme();
      if (scheme != null && scheme.equals("file")) {
        File file = new File(uri);
        parent = file.isDirectory() ? file : file.getParentFile();
      }
    } catch (Exception e) {
      parent = null;
    }

    RecordReaderFactory readerFactory = new DirectoryReaderFactory(
                                                                  dirResolver, parent, recurseIndicator.booleanValue(), maxItems, fileFilter);
    if (parameterDescriptors.length > 0) {
      readerFactory = new RecordReaderFactoryPrefilter(readerFactory,parameterDescriptors);
    }
    return readerFactory;
  }
}

