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

package com.servingxml.components.service;

import com.servingxml.app.Flow;
import com.servingxml.app.ServiceContext;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.components.task.AbstractTask;
import com.servingxml.components.task.Task;
import com.servingxml.app.Service;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.Name;
import com.servingxml.components.streamsource.StreamSourceFactory;
import com.servingxml.io.streamsource.StreamSource;
import com.servingxml.io.streamsink.StreamSink;
import com.servingxml.components.streamsink.StreamSinkFactory;
import com.servingxml.components.common.NameSubstitutionExpr;

/**
 * The <tt>RunService</tt> class implements the action of running a service.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RunService extends AbstractTask implements Task  {

  private final NameSubstitutionExpr serviceIdResolver;
  private final StreamSourceFactory sourceFactory;
  private final StreamSinkFactory sinkFactory;

  public RunService(NameSubstitutionExpr serviceIdResolver, StreamSourceFactory sourceFactory, StreamSinkFactory sinkFactory) {
    this.serviceIdResolver = serviceIdResolver;
    this.sourceFactory = sourceFactory;
    this.sinkFactory = sinkFactory;
  }

  public void execute(ServiceContext context, Flow flow) {
    if (sourceFactory != null) {
      StreamSource source = sourceFactory.createStreamSource(context, flow);
      flow = flow.replaceDefaultStreamSource(context, source);
    }
    if (sinkFactory != null) {
      StreamSink sink = sinkFactory.createStreamSink(context,flow);
      flow = flow.replaceDefaultStreamSink(context, sink);
    }
    Name serviceId = serviceIdResolver.evaluateName(flow.getParameters(),flow.getRecord());
    Service service = (Service)context.getAppContext().getResources().lookupServiceComponent(Service.class,serviceId.toUri());
    if (service == null) {
      throw new ServingXmlException("Cannot find service " + serviceId);
    }

    service.execute(context, flow);
  }
}

