/* abc - The AspectBench Compiler
 * Copyright (C) 2008 Pavel Avgustinov
 * Copyright (C) 2008 Torbjorn Ekman
 * Copyright (C) 2008 Julian Tibble
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.ja.tmwpopt;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.util.StdErrorQueue;
import soot.Scene;
import soot.SootMethod;
import abc.ja.tmwpopt.jrag.BytecodeParser;
import abc.ja.tmwpopt.jrag.CompilationUnit;
import abc.ja.tmwpopt.jrag.JavaParser;
import abc.ja.tmwpopt.jrag.Program;
import abc.main.AbcExtension;
import abc.main.AbcTimer;
import abc.main.CompilerFailedException;
import abc.main.Debug;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.weaver.DeclareParentsConstructorFixup;
import abc.weaving.weaver.IntertypeAdjuster;

public class CompileSequence extends abc.ja.tm.CompileSequence {
  public CompileSequence(AbcExtension ext) {
    super(ext);
  }

  // throw CompilerFailedException if there are errors
  // place errors in error_queue
  public void compile() throws CompilerFailedException, IllegalArgumentException {
    error_queue = abcExt.getErrorQueue();
    if(error_queue == null)
      error_queue = new StdErrorQueue(System.out, 100, "JastAdd");

    Program program = new Program();
    program.state().reset();

    setupParser(program);
    
    program.doCompileSequence(error_queue, aspect_sources, jar_classes);
  }
  
	public void setupParser(Program program) {
		// select parser/scanner
		program.initBytecodeReader(new BytecodeParser());
		program.initJavaParser(
	      new JavaParser() {
	        public CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception {
	          return new abc.ja.tmwpopt.parse.JavaParser().parse(is, fileName, error_queue);
	        }
	      }
	    );
	}
  
}
