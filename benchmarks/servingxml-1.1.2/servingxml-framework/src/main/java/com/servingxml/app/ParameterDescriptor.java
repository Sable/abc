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

package com.servingxml.app;

import com.servingxml.util.Name;
import com.servingxml.util.record.RecordBuilder;

public interface ParameterDescriptor {
  public static final ParameterDescriptor[] EMPTY_ARRAY = new ParameterDescriptor[0];
  public static final ParameterDescriptor[] EMPTY_PARAMETER_DESCRIPTOR_ARRAY = EMPTY_ARRAY;

  Name getName();

  void addParametersTo(ServiceContext context, Flow flow, RecordBuilder parameterBuilder);
                    
  String[] getValues(ServiceContext context, Flow flow);
}
