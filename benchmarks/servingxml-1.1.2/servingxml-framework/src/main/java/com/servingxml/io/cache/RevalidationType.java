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

package com.servingxml.io.cache;

import java.util.StringTokenizer;

/**
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class RevalidationType {
  private static final String REVALIDATE_SYNCH = "synch";
  private static final String REVALIDATE_ASYNCH = "asynch";
  private static final String REVALIDATE_FULL = "full";
  
  public static final RevalidationType NO_REVALIDATION = new RevalidationType(false,false);
  public static final RevalidationType ASYNCH_REVALIDATION = new RevalidationType(true,false);
  public static final RevalidationType SYNCH_REVALIDATION = new RevalidationType(false,true);
  public static final RevalidationType FULL_REVALIDATION = new RevalidationType(true,true);

  private boolean asynchRevalidation = false;
  private boolean synchRevalidation = false;

  public RevalidationType() {
  }

  public RevalidationType(boolean asynchRevalidation, boolean synchRevalidation) {
    this.asynchRevalidation = asynchRevalidation;
    this.synchRevalidation = synchRevalidation;
  }

  public RevalidationType(String str) {
    StringTokenizer tokenizer = new StringTokenizer(str);
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      if (token.equals(REVALIDATE_ASYNCH)) {
        asynchRevalidation = true;
      } else if (token.equals(REVALIDATE_SYNCH)) {
        synchRevalidation = true;
      } else if (token.equals(REVALIDATE_FULL)) {
        asynchRevalidation = true;
        synchRevalidation = true;
      } 
    }
  }

  public boolean revalidateAsynch() {
    return asynchRevalidation;
  }

  public boolean revalidateSynch() {
    return synchRevalidation;
  }

  public String toString() {
    return "asynchRevalidation = " + asynchRevalidation + ", synchRevalidation = " + synchRevalidation;
  }
}
