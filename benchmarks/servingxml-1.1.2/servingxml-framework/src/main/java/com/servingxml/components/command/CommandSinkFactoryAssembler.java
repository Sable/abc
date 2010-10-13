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

package com.servingxml.components.command;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.w3c.dom.Element;

import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.expr.substitution.SubstitutionExpr;
import com.servingxml.ioc.components.ConfigurationContext;
import com.servingxml.util.MessageFormatter;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.ServingXmlMessages;

public class CommandSinkFactoryAssembler {
  private EnvVariableFactory[] envVariableFactories = new EnvVariableFactory[0];
  private Command command = null;
  private CommandArg[] commandArgs = new CommandArg[0];
  private String directory = "";
  private String charsetName = null;

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public void setEncoding(String charsetName) {
    this.charsetName = charsetName;
  }

  public void injectComponent(EnvVariableFactory[] envVariableFactories) {
    this.envVariableFactories = envVariableFactories;
  }

  public void injectComponent(Command command) {
    this.command = command;
  }

  public void injectComponent(CommandArg[] commandArgs) {
    this.commandArgs = commandArgs;
  }

  public StreamSinkFactory assemble(ConfigurationContext context) {

    if (command == null) {
      String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ELEMENT_REQUIRED,
                                                                 context.getElement().getTagName(),"sx:command");
      throw new ServingXmlException(message);
    }

    Charset charset = null;
    if (charsetName != null) {
      try {
        charset = Charset.forName(charsetName);
      } catch (IllegalCharsetNameException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,context.getElement().getTagName(),"charset");
        throw new ServingXmlException(message + ".  " + e.getMessage());
      } catch (UnsupportedCharsetException e) {
        String message = MessageFormatter.getInstance().getMessage(ServingXmlMessages.COMPONENT_ATTRIBUTE_VALUE_INVALID,context.getElement().getTagName(),"charset");
        throw new ServingXmlException(message + ".  " + e.getMessage());
      }
    }

    SubstitutionExpr dirResolver = SubstitutionExpr.parseString(context.getQnameContext(),directory);

    StreamSinkFactory sinkFactory = new CommandSinkFactory(dirResolver, envVariableFactories, command, 
                                                                 commandArgs, charset);

    return sinkFactory;
  }
}
