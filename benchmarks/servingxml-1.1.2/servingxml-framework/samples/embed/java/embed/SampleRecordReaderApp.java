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

package embed;

import java.io.File;
import java.net.URL;

import com.servingxml.app.AppContext;
import com.servingxml.ioc.resources.IocContainerFactory;
import com.servingxml.app.DefaultAppContext;
import com.servingxml.app.DefaultServiceContext;
import com.servingxml.app.Service;
import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.AbstractRecordReader;
import com.servingxml.components.recordio.AbstractRecordReaderFactory;
import com.servingxml.components.recordio.RecordReader;
import com.servingxml.components.recordio.RecordReaderFactory;
import com.servingxml.components.recordio.RecordWriterFilterAdaptor;
import com.servingxml.app.Flow;
import com.servingxml.app.FlowImpl;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.io.streamsink.file.FileSink;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsource.file.FileSource;
import com.servingxml.ioc.resources.IocContainer;
import com.servingxml.ioc.resources.SimpleIocContainer;
import com.servingxml.util.CommandLine;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.ParameterBuilder;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;
import com.servingxml.util.system.SystemConfiguration;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class SampleRecordReaderApp {

  private static final String[] columnNames = {"category", "author", "title", "price"};

  private static final String[][] data = {
    {"F", "Charles Bukowski", "Factotum", "22.95"},
    {"F", "Jonathan Lethem", "Gun, with Occasional Music", "17.99"},
    {"F", "Andrew Crumey", "Mr Mee", "22.00"},
    {"C", "Steven John Metsker", "Building Parsers with Java", "39.95"},
  };

  public SampleRecordReaderApp() {
  }

  public static void main(String[] args) {

    try {
      IocContainerFactory iocContainerFactory = new IocContainerFactory();
      File file = new File("output/books.xml");
      StreamSink defaultStreamSink = new FileSink(file);
      String serviceUri = "books";
      String configHref = "";
      String resourcesHref = "resources-books.xml";          


      //  Locate configuration script servingxml.xml 
      //  in the classpath
      ClassLoader parentLoader = Thread.currentThread().getContextClassLoader();
      URL configUrl = parentLoader.getResource("servingxml.xml");
      configHref = configUrl.toString();

      try {
        iocContainerFactory.loadComponentDefinitions();
      } catch (ServingXmlException e) {
        SystemConfiguration.getSystemContext().error(e.getMessage());
        return;
      } catch (Exception e) {
        SystemConfiguration.getSystemContext().error(e.getMessage());
        e.printStackTrace(System.err);
        return;
      }
      Name myRecordReaderId = new QualifiedName("myBooksReader");

      IocContainer configuration = iocContainerFactory.createIocContainer(configHref);
      SimpleIocContainer myResources = new SimpleIocContainer(configuration);
      myResources.registerServiceComponent(
        RecordReaderFactory.class,
        myRecordReaderId.toUri(), new MyRecordReaderFactory(columnNames, data));
      IocContainer resources = iocContainerFactory.createIocContainer(resourcesHref, myResources);

      AppContext appContext = new DefaultAppContext("servingxml", resources);
      final ServiceContext serviceContext = new DefaultServiceContext(appContext, "servingxml");

      // Create parameters, if any
      final ParameterBuilder paramBuilder = new ParameterBuilder();
      Record parameters = paramBuilder.toRecord();
      Flow flow = new FlowImpl(parameters, StreamSource.NULL, defaultStreamSink);

      try {
        // Invoke the service
        Service service = (Service)appContext.getResources().lookupServiceComponent(Service.class,serviceUri);
        if (service == null) {
          throw new ServingXmlException("Cannot find service " + serviceUri);
        }
        service.execute(serviceContext, flow);
      } catch (ServingXmlException e) {
        serviceContext.error(e.getMessage());
        return;
      } catch (Exception e) {
        serviceContext.error(e.getMessage());
        e.printStackTrace(System.err);
        return;
      }

    } catch (Exception e) {
      SystemConfiguration.getSystemContext().error(e.getMessage());
      e.printStackTrace(System.err);
      return;
    } finally {
    }
  }

  static class MyRecordReaderFactory extends AbstractRecordReaderFactory {
    private final String[] columnNames;
    private final String[][] data;

    public MyRecordReaderFactory(String[] columnNames, String[][] data) {
      this.columnNames = columnNames;
      this.data = data;
    }

    protected RecordReader createRecordReader(ServiceContext context, Flow flow) {
      return new MyRecordReader(columnNames, data);
    }
  }

  static class MyRecordReader extends AbstractRecordReader {
    private static final Name BOOK_RECORD_TYPE = new QualifiedName("book");

    private final String[] columnNames;
    private final String[][] data;

    public MyRecordReader(String[] columnNames, String[][] data) {
      this.columnNames = columnNames;
      this.data = data;
    }

    public void readRecords(ServiceContext context, Flow flow) {
      RecordWriterFilterAdaptor writer = (RecordWriterFilterAdaptor)getRecordWriter();

      Name[] fieldNames = new Name[columnNames.length];
      for (int i = 0; i < fieldNames.length; ++i) {
        fieldNames[i] = new QualifiedName(columnNames[i]);
      }

      RecordBuilder recordBuilder = new RecordBuilder(BOOK_RECORD_TYPE);

      try {
        startRecordStream(context, flow);
        for (int i = 0; i < data.length; ++i) {
          for (int j = 0; j < fieldNames.length; ++j) {
            recordBuilder.setString(fieldNames[j],data[i][j]);
          }
          Record record = recordBuilder.toRecord();
          Flow newFlow = flow.replaceRecord(context, record);
          writeRecord(context, newFlow);
          recordBuilder.clear();
        }
        endRecordStream(context, flow);
      } finally {
        close();
      }
    }
  }
}

