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

package com.servingxml.components.flatfile.options;

import java.io.IOException;

import com.servingxml.util.CharArrayBuilder;
import com.servingxml.components.flatfile.options.CharBuffer;

public final class MultipleCharCommentStarterChecker extends CommentStarterCharChecker {

  private final CommentStarterCharChecker[] commentStarterCheckers;

  public MultipleCharCommentStarterChecker(CommentStarterCharChecker[] commentStarterCheckers) {
    this.commentStarterCheckers = commentStarterCheckers;
  }

  public final boolean checkCommentSymbol(CharBuffer recordBuffer, CharArrayBuilder charArrayBuilder) 
  throws IOException {
    boolean done = false;
    for (int i = 0; !done && i < commentStarterCheckers.length; ++i) {
      done = commentStarterCheckers[i].checkCommentSymbol(recordBuffer,charArrayBuilder);
    }
    return done;
  }
}
