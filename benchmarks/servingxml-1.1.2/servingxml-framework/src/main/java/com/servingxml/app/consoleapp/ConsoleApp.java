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

package com.servingxml.app.consoleapp;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;

import com.servingxml.app.AppContext;
import com.servingxml.app.DefaultAppContext;
import com.servingxml.app.DefaultServiceContext;
import com.servingxml.app.Flow;
import com.servingxml.app.FlowImpl;
import com.servingxml.app.Service;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.streamsink.OutputStreamSinkAdaptor;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsink.StringStreamSink;
import com.servingxml.io.streamsink.file.FileSink;
import com.servingxml.io.streamsource.InputStreamSourceAdaptor;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsource.file.FileSource;
import com.servingxml.ioc.resources.IocContainer;
import com.servingxml.ioc.resources.IocContainerFactory;
import com.servingxml.util.CommandLine;
import com.servingxml.util.Name;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.SystemConstants;
import com.servingxml.util.record.ParameterBuilder;
import com.servingxml.util.record.Record;
import com.servingxml.util.system.SystemConfiguration;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ConsoleApp {

  public static void main(String[] args) {

    //System.setProperty("line.separator","\n");

    IocContainerFactory iocContainerFactory = new IocContainerFactory();
    StreamSource defaultStreamSource = new InputStreamSourceAdaptor(System.in);
    StreamSink defaultStreamSink = new OutputStreamSinkAdaptor(System.out);
    boolean trace = false;

    try {
      //  Start up
      CommandLine commandLine = new CommandLine(args);

      String serviceUri = null;
      String configHref = "";
      String resourcesHref = null;

      try {

        Iterator iter = commandLine.iterator();

        while (iter.hasNext()) {
          CommandLine.Arg arg = (CommandLine.Arg)iter.next();
          if (arg.isOption()) {
            if (arg.getValue().equals("version")) {
              System.out.println("ServingXML version " + SystemConstants.VERSION);
              return;
            } else if (arg.getValue().equals("help")) {
              printHelp(System.out);
              return;
            } else if (arg.getValue().equals("o")) {
              if (iter.hasNext()) {
                arg = (CommandLine.Arg)iter.next();
                String outputFilename = arg.getValue();
                File file = new File(outputFilename);
                defaultStreamSink =  new FileSink(file);
              }
            } else if (arg.getValue().equals("r")) {
              if (iter.hasNext()) {
                arg = (CommandLine.Arg)iter.next();
                resourcesHref = arg.getValue();
              }
            } else if (arg.getValue().equals("c")) {
              if (iter.hasNext()) {
                arg = (CommandLine.Arg)iter.next();
                configHref = arg.getValue();
              }
            } else if (arg.getValue().equals("i")) {
              if (iter.hasNext()) {
                arg = (CommandLine.Arg)iter.next();
                String inputFilename = arg.getValue();
                File file = new File(inputFilename);
                defaultStreamSource = new FileSource(file);
              }
            } else if (arg.getValue().equals("T")) {
              trace = true;
            }
          } else {
            serviceUri = arg.getValue();
            break;
          }
        }

        if (resourcesHref == null || resourcesHref.length() == 0) {
          System.err.println("Error:  resources script is required");
          System.err.println();
          printHelp(System.err);
          System.exit(1);
        }

        if (serviceUri == null || serviceUri.length() == 0) {
          System.err.println("Error:  service is required");
          System.err.println();
          printHelp(System.err);
          System.exit(1);
        }
      } catch (Exception e) {
        System.err.println("Error:  " + e.getMessage());
        if (trace) {
          e.printStackTrace(System.err);
        }
        return;
      }

      //  If no configuration script location supplied, look for one 
      //  called servingxml.xml in the classpath
      if (configHref == null || configHref.length() == 0) {
        ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
        URL configUrl = parentLoader.getResource("servingxml.xml");
        configHref = configUrl.toString();
      }

      try {
        iocContainerFactory.loadComponentDefinitions();
      } catch (ServingXmlException e) {
        SystemConfiguration.getSystemContext().error(e.getMessage());
        if (trace) {
          SystemConfiguration.getSystemContext().printStackTrace(e);
        }
        return;
      } catch (Exception e) {
        SystemConfiguration.getSystemContext().error(e.getMessage());
        if (trace) {
          SystemConfiguration.getSystemContext().printStackTrace(e);
        }
        return;
      }

      //  Create parameters
      final ParameterBuilder paramBuilder = new ParameterBuilder();

      CommandLine.ParameterCommand command = new CommandLine.ParameterCommand() {
        public void doParameter(String name, String[] values) {
          Name fieldName = Name.parse(name);
          paramBuilder.setStringArray(fieldName,values);
        }
      };

      commandLine.toEveryArgument(command);

      //  Invoke the service
      Record parameters = paramBuilder.toRecord();

      IocContainer resources = iocContainerFactory.createIocContainer(configHref, parameters);
      resources = iocContainerFactory.createIocContainer(resourcesHref, parameters, resources);

      AppContext appContext = new DefaultAppContext("servingxml", resources);
      final ServiceContext serviceContext = new DefaultServiceContext(appContext, "servingxml");
      Flow flow = new FlowImpl(parameters, defaultStreamSource, defaultStreamSink);

      //  Create a service context

      //  Service invocation
      try {
        Service service = (Service)appContext.getResources().lookupServiceComponent(Service.class,serviceUri);
        if (service == null) {
          throw new ServingXmlException("Cannot find service " + serviceUri);
        }
        service.execute(serviceContext, flow);
      } catch (ServingXmlException e) {
        serviceContext.error(e.getMessage());
        if (trace) {
          serviceContext.printStackTrace(e);
        }
        return;
      } catch (Exception e) {
        serviceContext.error(e.getMessage());
        if (trace) {
          serviceContext.printStackTrace(e);
        }
        return;

      }
    } catch (ServingXmlException e) {
      SystemConfiguration.getSystemContext().error(e.getMessage());
      if (trace) {
        SystemConfiguration.getSystemContext().printStackTrace(e);
      }
    } catch (Throwable e) {
      SystemConfiguration.getSystemContext().error(e.getMessage());
      if (trace) {
        SystemConfiguration.getSystemContext().printStackTrace(e);
      }
    } finally {
    }
  }

  public static void printHelp(PrintStream ps) {
    ps.println("Usage: servingxml [-options] service [param=value...]");
    ps.println();
    ps.println("where options include:");
    ps.println("\t-r\t\tresources script");
    ps.println("\t-i\t\tinput file");
    ps.println("\t\t\tThe default is stdin");
    ps.println("\t-o\t\toutput file");
    ps.println("\t\t\tThe default is stdout");
    ps.println("\t-c\t\toptional configuration script");
    ps.println("\t\t\tAn alternative is to provide a configuration");
    ps.println("\t\t\tfile named servingxml.xml in the classpath");
    ps.println("\t-help\t\tprint this help message");
    ps.println("\t-version\tprint version");
    ps.println("\t-T\t\tshow exception stack trace");
    ps.println();
    ps.println("Example:  servingxml -i countries.csv -o countries.xml");
    ps.println("\t\t-r resources-countries.xml countries-to-xml validate=false");
    ps.println();
    ps.println("Alternative: java -jar dir/servingxml.jar [-options] service [param=value...]");
  }

}

