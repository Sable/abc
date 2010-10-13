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

import java.io.File;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.Charset;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.util.Name;    
import com.servingxml.util.record.Record;
import com.servingxml.expr.substitution.SubstitutionExpr;

public class CommandSinkFactory implements StreamSinkFactory {
  private final SubstitutionExpr dirResolver;
  private final EnvVariableFactory[] envVariableFactories;
  private final Command command;
  private final CommandArg[] commandArgs;
  private final Charset charset;

  public CommandSinkFactory(SubstitutionExpr dirResolver, EnvVariableFactory[] envVariableFactories, 
                              Command command, CommandArg[] commandArgs, Charset charset) {
    this.dirResolver = dirResolver;
    this.envVariableFactories = envVariableFactories;
    this.command = command;
    this.commandArgs = commandArgs;
    this.charset = charset;
  }

  public StreamSink createStreamSink(ServiceContext context, Flow flow) {
    List<String> commandList = new ArrayList<String>();
    String cmd = command.evaluateString(context,flow);
    commandList.add(cmd);
    for (int i = 0; i < commandArgs.length; ++i) {
      String arg = commandArgs[i].evaluateString(context,flow);
      commandList.add(arg);
    }

    ProcessBuilder processBuilder = new ProcessBuilder(commandList);

    Map<String,String> environment = processBuilder.environment();
    for (int i = 0; i < envVariableFactories.length; ++i) {
      EnvVariable envVariable = envVariableFactories[i].createEnvVariable(context,flow);
      environment.put(envVariable.getName(), envVariable.getValue());
    }

    String directory = dirResolver.evaluateAsString(flow.getParameters(),flow.getRecord());
    if (directory.length() > 0) {
      File file = new File(directory);
      processBuilder.directory(file);
    }

    StreamSink streamSink = new CommandSink(processBuilder, charset);
    return streamSink;
  }

  public Charset getCharset() {
    return charset;
  }

  public Charset getCharset(Charset defaultCharset) {
    return charset != null ? charset : defaultCharset;
  }
}
