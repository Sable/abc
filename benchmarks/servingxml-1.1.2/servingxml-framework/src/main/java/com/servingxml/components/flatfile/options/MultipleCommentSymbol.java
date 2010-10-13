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

import java.nio.charset.Charset;

/**
 * The <code>CommentStarter</code> class encapsulates a comment character sequence.
 *
 * 
 * @author Daniel A. Parker (daniel.parker@servingxml.com)
 */

public class MultipleCommentSymbol implements CommentStarter {
                                                       
  private final CommentStarter[] commentSymbols;
             
  public MultipleCommentSymbol(CommentStarter[] commentSymbols) {
    this.commentSymbols = commentSymbols;
  }

  public CommentStarterByteChecker createByteCommentStarterChecker(Charset charset) {
    CommentStarterByteChecker[] checkers = new CommentStarterByteChecker[commentSymbols.length];
    for (int i = 0; i < checkers.length; ++i) {
      checkers[i] = commentSymbols[i].createByteCommentStarterChecker(charset);
    }

    return new MultipleCommentStarterChecker(checkers);
  }

  public CommentStarterCharChecker createCharCommentStarterChecker() {
    CommentStarterCharChecker[] checkers = new CommentStarterCharChecker[commentSymbols.length];
    for (int i = 0; i < checkers.length; ++i) {
      checkers[i] = commentSymbols[i].createCharCommentStarterChecker();
    }

    return new MultipleCharCommentStarterChecker(checkers);
  }
}
