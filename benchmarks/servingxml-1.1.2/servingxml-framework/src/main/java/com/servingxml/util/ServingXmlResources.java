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

import java.util.ListResourceBundle;

public class ServingXmlResources extends ListResourceBundle implements ServingXmlMessages {

  public Object[][] getContents() {
    return contents;
  }
  static final Object[][] contents = {
    {COMPONENT_ERROR, "Error in element {0}.  {1}."},
    {COMPONENT_TRANSFORM_FAILED, "Error in element {0}.  Transform failed."},
    {COMPONENT_JDBC_CONNECTION_POOL, "Error in element {0}.  Creation of JDBC Connection Pool failed."},
    {COMPONENT_ATTRIBUTE_REQUIRED, "Error in element {0}.  The attribute \"{1}\" is required."},
    {COMPONENT_ATTRIBUTE_CHOICE_REQUIRED, "Error in element {0}.  One of the following attributes is required:  {1}."},
    {COMPONENT_ELEMENT_CHOICE_REQUIRED, "Error in element {0}. One of the following child elements is required: {1}."},                         
    {COMPONENT_ELEMENT_REQUIRED, "Error in element {0}. A {1} child element is required."},                         
    {COMPONENT_ELEMENT_SPECIALIZATION_REQUIRED, "Error in element {0}. A child element that specializes abstract element {1} is required, e.g. {2}."},                         
    {COMPONENT_LITERAL_CONTENT_REQUIRED, "Element {0} is empty, must contain literal content."},                   
    {COMPONENT_ATTRIBUTE_VALUE_INVALID, "Error in element {0}, attribute {1} has invalid value."},                  
    {COMPONENT_RECORD_MAPPING_GROUP_CHOICE_REQUIRED, "Error in element {0}.  Multiple record mapping group siblings must be enclosed in an sx:groupChoice element."},           
    {COMPONENT_EXPR_PARSE_FAILED, "Error in element {0}. The expression \"{1}\" is not recognized."},
    {COMPONENT_SUB_EXPR_PARSE_FAILED, "Error in element {0}. Substitution expression parse failed."},
    {COMPONENT_DUPLICATE_NAME, "Error in element {0}.  Failed attempting to add duplicate component name \"{1}\" of type {2}."},
    {RECORD_FIELD_NOT_FOUND, "Field \"{0}\" not found in record of type \"{1}\"."},                  
    {PARAMETER_NOT_FOUND, "Parameter \"{0}\" not found."},          
    {CRC_INTEGRITY_CHECK_FAILED, "CRC integrity check failed.  Expected {0}, found {1}."},          
    {SIZE_INTEGRITY_CHECK_FAILED, "Size integrity check failed.  Expected {0}, found {1}."},          
    {RECORD_COUNT_INTEGRITY_CHECK_FAILED, "Record count integrity check failed.  Expected {0}, found {1}."},          
    {CLASS_NOT_FOUND, "Class {0} not found."},                  
    {VALUE_UNKNOWN, "Value \"{0}\" is invalid, must be one of {1}."},           
    {RECORD_ERROR, "Error in \"{0}\" record on line {1}. {2}"}           
  };
}

