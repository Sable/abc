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

package com.servingxml.extensions.edtftpj.recordio;

import com.servingxml.extensions.edtftpj.connect.FtpClient;
import com.servingxml.app.ParameterDescriptor;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.components.recordio.RecordReaderFactoryPrefilter;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;
import com.servingxml.components.common.TrueFalseEnum;

/**
 * The <code>FtpDirectoryReaderFactoryAssembler</code> implements an assembler for
 * assembling <code>FtpDirectoryReaderFactory</code> objects.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class FtpDirectoryReaderFactoryAssembler {
  
  private ParameterDescriptor[] parameterDescriptors = ParameterDescriptor.EMPTY_ARRAY;
  private FtpClient ftpClient = null;
  private String directory = "";
  private String recurse = TrueFalseEnum.FALSE.toString();
  private long maxItems = Long.MAX_VALUE;
  
  public FtpDirectoryReaderFactoryAssembler() {
  }

  public void injectComponent(ParameterDescriptor[] parameterDescriptors) {

    this.parameterDescriptors = parameterDescriptors;
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

  public void injectComponent(FtpClient ftpClient) {
    this.ftpClient = ftpClient;
  }

  public RecordReaderFactory assemble(ConfigurationContext context) {

    if (ftpClient == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,context.getElement().getTagName(),"edt:ftpClient");
      throw new ServingXmlException(message);
    }

    TrueFalseEnum recurseIndicator;
    try {
      recurseIndicator = TrueFalseEnum.parse(recurse);
    } catch (ServingXmlException e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,
        context.getElement().getTagName(), "recurse");
      e = e.supplementMessage(message);
      throw e;
    }

    try {
      SubstitutionExpr dirResolver = SubstitutionExpr.parseString(context.getQnameContext(),directory);

      RecordReaderFactory readerFactory = new FtpDirectoryReaderFactory(
        ftpClient, dirResolver, recurseIndicator.booleanValue(),maxItems);
      if (parameterDescriptors.length > 0) {
        readerFactory = new RecordReaderFactoryPrefilter(readerFactory,parameterDescriptors);
      }
      return readerFactory;
    } catch (ServingXmlException e) {
      throw e.contextualizeMessage(context.getElement().getTagName());
    } catch (Exception e) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ERROR,
        context.getElement().getTagName(), e.getMessage());
      throw new ServingXmlException(message,e);
    }
  }
}

