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

package com.servingxml.util;

import org.xml.sax.XMLReader;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class ServingXmlFault extends ServingXmlException {
  static final long serialVersionUID = 8736874967183039804L;

  private final Name code;
  private final Name[] subcodes;
  private final ServingXmlFaultDetail detail;

  public ServingXmlFault(String reason) {
     super(reason);
     this.code = ServingXmlFaultCodes.RECEIVER_CODE;
     this.subcodes = new Name[0];
     this.detail = new ServingXmlFaultDetail();
  }

  public ServingXmlFault(Name code, String reason) {
     super(reason);
     this.code = code;
     this.subcodes = new Name[0];
     this.detail = new ServingXmlFaultDetail();
  }

  public ServingXmlFault(Name code, Name subcode, String reason) {
     super(reason);
     this.code = code;
     this.subcodes = new Name[]{subcode};
     this.detail = new ServingXmlFaultDetail();
  }

  public ServingXmlFault(Name code, String reason, ServingXmlFaultDetail detail) {
     super(reason);
     this.code = code;
     this.subcodes = new Name[0];
     this.detail = detail;
  }

  public ServingXmlFault(Name code, Name subcode, String reason, ServingXmlFaultDetail detail) {
     super(reason);
     this.code = code;
     this.subcodes = new Name[]{subcode};
     this.detail = detail;
  }

  public String getFaultString() {
    return super.getMessage();
  }

  public Name getFaultCode() {
    return code;
  }

  public XMLReader createXmlReader() {
    return new ServingXmlFaultReader(code,subcodes,getMessage(),detail);
  }
}

