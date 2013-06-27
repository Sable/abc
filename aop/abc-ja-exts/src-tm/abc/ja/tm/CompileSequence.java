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

package abc.ja.tm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyglot.util.StdErrorQueue;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import abc.ja.tm.jrag.BytecodeParser;
import abc.ja.tm.jrag.CompilationUnit;
import abc.ja.tm.jrag.JavaParser;
import abc.ja.tm.jrag.Problem;
import abc.ja.tm.jrag.Program;
import abc.main.AbcExtension;
import abc.main.AbcTimer;
import abc.main.CompilerFailedException;
import abc.main.Debug;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbcFactory;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.weaver.DeclareParentsConstructorFixup;
import abc.weaving.weaver.IntertypeAdjuster;

public class CompileSequence extends abc.ja.eaj.CompileSequence {
  public CompileSequence(AbcExtension ext) {
    super(ext);
  }

  private void addError(Problem problem) {
    Position p;
    if(problem.column() != -1)
      p = new Position(problem.fileName(), problem.line(), problem.column());
    else
      p = new Position(problem.fileName(), problem.line());
    error_queue().enqueue(ErrorInfo.SEMANTIC_ERROR, problem.message(), p);
  }

  private void addWarning(Problem problem) {
	    Position p;
	    if(problem.column() != -1)
	      p = new Position(problem.fileName(), problem.line(), problem.column());
	    else
	      p = new Position(problem.fileName(), problem.line());
	    error_queue().enqueue(ErrorInfo.WARNING, problem.message(), p);
  }

  public ErrorQueue error_queue() {
    if(error_queue == null)
      error_queue = new StdErrorQueue(System.out, 100, "JastAdd");
    return error_queue;
  }

	public void setupParser(Program program) {
		// select parser/scanner
		program.initBytecodeReader(new BytecodeParser());
		program.initJavaParser(
	      new JavaParser() {
	        public CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception {
	          return new abc.ja.tm.parse.JavaParser().parse(is, fileName, error_queue);
	        }
	      }
	    );
	}


  // throw CompilerFailedException if there are errors
  // place errors in error_queue
  public void compile() throws CompilerFailedException, IllegalArgumentException {
    error_queue = abcExt.getErrorQueue();
    if(error_queue == null)
      error_queue = new StdErrorQueue(System.out, 100, "JastAdd");

    // Fetch all the weavable classes and put them in the right places
    Iterator<String> wcni = jar_classes.iterator();
    while (wcni.hasNext()) {
        String wcn = wcni.next();
        SootClass sootClass = Scene.v().loadClassAndSupport(wcn);
        sootClass.setApplicationClass();
        abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().addWeavableClass(AbcFactory.AbcClass(sootClass));
    }
    
    Program program = new Program();
    program.state().reset();

    setupParser(program);
    
    program.doCompileSequence(error_queue, aspect_sources, jar_classes);
  }
  
  public void weave() throws CompilerFailedException {
	  try {
		  // Perform the declare parents
		  //new DeclareParentsWeaver().weave();
		  // FIXME: put re-resolving here, from declareparents weaver
		  AbcTimer.mark("Declare Parents");
		  Debug.phaseDebug("Declare Parents");
		  Scene.v().setDoneResolving();

		  // Adjust Soot types for intertype decls
		  IntertypeAdjuster ita = new IntertypeAdjuster();
		  //ita.adjust();
		  AbcTimer.mark("Intertype Adjuster");
		  Debug.phaseDebug("Intertype Adjuster");

		  // Retrieve all bodies
		  for( Iterator clIt = abcExt.getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
			  final AbcClass cl = (AbcClass) clIt.next();
			  if(Debug.v().showWeavableClasses) System.err.println("Weavable class: "+cl);
			  for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
				  final SootMethod method = (SootMethod) methodIt.next();
				  try {
					  if( method == null || !method.isConcrete() ) continue;
					  // System.out.println("retrieve "+method+ " from "+cl);
					  method.retrieveActiveBody();
				  } catch(InternalCompilerError e) {
					  throw e;
				  } catch(Throwable e) {
					  throw new InternalCompilerError("Exception while processing "+method.getSignature(),e);
				  }
			  }
		  }
		  AbcTimer.mark("Jimplification");
		  Debug.phaseDebug("Jimplification");

		  //call hook for (optional) restructuring of methods
		  abcExt.doMethodRestructuring();

		  // Fix up constructors in binary classes with newly declared parents
		  new DeclareParentsConstructorFixup().weave();
		  AbcTimer.mark("Fix up constructor calls");
		  Debug.phaseDebug("Fix up constructor calls");

		  // FIXME XXX TODO Here be Welsh Dragons
		  //PatternMatcher.v().updateWithAllSootClasses();
		  // evaluate the patterns the third time (depends on re-resolving)
		  //PatternMatcher.v().recomputeAllMatches();
		  AbcTimer.mark("Update pattern matcher");
		  Debug.phaseDebug("Update pattern matcher");

		  // any references made by itd initialisers will appear in a delegate method,
		  // and thus have already been processed by j2j; all resolving ok.
		  //ita.initialisers(); // weave the field initialisers into the constructors
		  AbcTimer.mark("Weave Initializers");
		  Debug.phaseDebug("Weave Initializers");

		  if (!Debug.v().testITDsOnly) {
			  // Make sure that all the standard AspectJ shadow types are loaded
			  AbcTimer.mark("Load shadow types");
			  Debug.phaseDebug("Load shadow types");

			  // for each shadow in each weavable class, compute list of applicable advice
			  abcExt.getGlobalAspectInfo().computeAdviceLists();
			  AbcTimer.mark("Compute advice lists");
			  Debug.phaseDebug("Compute advice lists");                

			  if(Debug.v().printAdviceApplicationCount) {
				  int adviceApplCount=0;

				  for( Iterator clIt = abcExt.getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {

					  final AbcClass cl = (AbcClass) clIt.next();
					  for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
						  final SootMethod method = (SootMethod) methodIt.next(); 
						  MethodAdviceList list=abcExt.getGlobalAspectInfo().getAdviceList(method);
						  if (list==null)
							  continue;
						  List allAdvice=list.allAdvice();
						  adviceApplCount += allAdvice.size();                           	
					  }
				  }                   
				  System.out.println("Number of advice applications: " + adviceApplCount);
			  }
			  if(Debug.v().matcherTest) {
				  System.err.println("--- BEGIN ADVICE LISTS ---");
				  // print out matching information for testing purposes
				  for( Iterator clIt = abcExt.getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
					  final AbcClass cl = (AbcClass) clIt.next();
					  for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
						  final SootMethod method = (SootMethod) methodIt.next();
						  final StringBuffer sb=new StringBuffer(1000);
						  sb.append("method: "+method.getSignature()+"\n");
						  abcExt.getGlobalAspectInfo().getAdviceList(method).debugInfo(" ",sb);
						  System.err.println(sb.toString());
					  }
				  }         
				  System.err.println("--- END ADVICE LISTS ---");
			  }

			  if(abc.main.options.OptionsParser.v().warn_unused_advice()) {
				  for( Iterator adIt = abcExt.getGlobalAspectInfo().getAdviceDecls().iterator(); adIt.hasNext(); ) {
					  final AbstractAdviceDecl ad = (AbstractAdviceDecl) adIt.next();

					  if(ad instanceof AdviceDecl && ad.getApplWarning() != null)
						  abcExt.reportError(ErrorInfo.WARNING,
								  ad.getApplWarning(),
								  ad.getPosition());
				  }
			  }

			  //Weaver weaver = new Weaver();
			  abcExt.getWeaver().weave(); // timer marks inside weave() */
		  }
		  // the intertype adjuster has put dummy fields into interfaces,
		  // which now have to be removed
		  //ita.removeFakeFields();
	  } catch(SemanticException e) {
		  abcExt.reportError(ErrorInfo.SEMANTIC_ERROR,e.getMessage(),e.position());
	  }
  }
}
