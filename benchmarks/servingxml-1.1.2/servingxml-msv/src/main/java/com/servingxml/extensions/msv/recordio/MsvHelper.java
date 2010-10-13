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

package com.servingxml.extensions.msv.recordio;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierFilter;
import org.iso_relax.verifier.VerifierConfigurationException;

import com.sun.msv.verifier.ValidityViolation;
import com.sun.msv.verifier.ErrorInfo;

public class MsvHelper {

  public static final String makeMessage(Exception spe) {
    String s;

    if (spe instanceof ValidityViolation) {
      ValidityViolation vv = (ValidityViolation)spe;
      ErrorInfo info = vv.getErrorInfo();
      if (info instanceof ErrorInfo.BadText) {
        ErrorInfo.BadText e = (ErrorInfo.BadText)info;
        s = "Invalid value \"" + e.literal + "\" - " + spe.getMessage();
      } else if (info instanceof ErrorInfo.BadTagName) {
        ErrorInfo.BadTagName e = (ErrorInfo.BadTagName)info;
        s = "Invalid tag name in element " + e.qName + " - " + spe.getMessage();
      } else if (info instanceof ErrorInfo.BadAttribute) {
        ErrorInfo.BadAttribute e = (ErrorInfo.BadAttribute)info;
        s = "Error in element " + e.qName + ", attribute " + e.attQName + " - " + spe.getMessage();
      } else if (info instanceof ErrorInfo.MissingAttribute) {
        ErrorInfo.MissingAttribute e = (ErrorInfo.MissingAttribute)info;
        s = "Missing attribute in element " + e.qName + " - " + spe.getMessage();
      } else if (info instanceof ErrorInfo.IncompleteContentModel) {
        ErrorInfo.IncompleteContentModel e = (ErrorInfo.IncompleteContentModel)info;
        s = "Incomplete content model in element " + e.qName + " - " + spe.getMessage();
      } else if (info instanceof ErrorInfo.ElementErrorInfo) {
        ErrorInfo.ElementErrorInfo e = (ErrorInfo.ElementErrorInfo)info;
        s = "Error in element " + e.qName + " - " + spe.getMessage();
      } else {
        s = "Error - " + spe.getMessage();
      }
    } else {
      s = "Error - " + spe.getMessage();
    }
    return s;
  }
}
