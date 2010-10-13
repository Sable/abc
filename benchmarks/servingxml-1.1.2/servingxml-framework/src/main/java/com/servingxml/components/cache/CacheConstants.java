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

package com.servingxml.components.cache;

import com.servingxml.util.SystemConstants;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public interface CacheConstants {
  static final String SERVINGXML_NS_URI=SystemConstants.SERVINGXML_NS_URI;
  static final String FREE_MEMORY = "free-memory";
  static final String HEAP_SIZE = "heap-size";
  static final String MEMORY_SIGNATURE_INTERVAL = "memory-checker-interval";
  static final String MEMORY_SIGNATURE_PRIORITY = "memory-checker-priority";
  static final String CHANGED_SIGNATURE_INTERVAL = "changed-checker-interval";
  static final String CHANGED_SIGNATURE_PRIORITY = "changed-checker-priority";

  static final String CACHE_MANAGER = "cacheManager";
}
