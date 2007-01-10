package abc.ja;

import abc.main.AbcExtension;
import abc.main.AbcTimer;
import abc.main.CompilerFailedException;
import abc.main.Debug;
import abc.main.options.OptionsParser;
import abc.weaving.aspectinfo.AbcClass;
import abc.weaving.aspectinfo.AbstractAdviceDecl;
import abc.weaving.aspectinfo.AdviceDecl;
import abc.weaving.matching.MethodAdviceList;
import abc.weaving.weaver.DeclareParentsConstructorFixup;
import abc.weaving.weaver.DeclareParentsWeaver;
import abc.weaving.weaver.IntertypeAdjuster;

import java.util.*;
import java.util.List;

import polyglot.types.SemanticException;
import polyglot.util.ErrorInfo;
import polyglot.util.ErrorQueue;
import polyglot.util.StdErrorQueue;
import polyglot.util.Position;
import polyglot.util.InternalCompilerError;
import soot.Scene;
import soot.SootMethod;
import abc.aspectj.visit.PatternMatcher;
import abc.ja.jrag.*;

import java.io.*;

public class CompileSequence extends abc.main.CompileSequence {
  public CompileSequence(AbcExtension ext) {
    super(ext);
  }

  private void addError(String s) {
    Position p = new Position("FileName", 10);
    try {
    int first = s.indexOf(':');
    int second = s.indexOf('\n');
    if(first != -1 && second != -1) {
      String fileName = s.substring(0, first);
      String sub = s.substring(first+1, second);
      String splitString = ",";
      if(sub.indexOf(':') != -1)
        splitString = ":";
      String pos[] = sub.split(splitString);
      int line = 0;
      try {
        line = Integer.parseInt(pos[0].trim());
      } catch (Exception e) {
      }
      int column = 0;
      try {
        column = Integer.parseInt(pos[1].trim());
      } catch (Exception e) {
      }
      if(column != 0)
        p = new Position(fileName, line, column);
      else
        p = new Position(fileName, line);
      s = s.substring(second+1, s.length());
    }
    } catch (Exception e) {
    }
    error_queue().enqueue(ErrorInfo.SEMANTIC_ERROR, s, p);
  }
  public ErrorQueue error_queue() {
    if(error_queue == null)
      error_queue = new StdErrorQueue(System.out, 100, "JastAdd");
    return error_queue;
  }

  // throw CompilerFailedException if there are errors
  // place errors in error_queue
  public void compile() throws CompilerFailedException, IllegalArgumentException {
    error_queue = abcExt.getErrorQueue();
    if(error_queue == null)
      error_queue = new StdErrorQueue(System.out, 100, "JastAdd");

    try {
      Collection c = new ArrayList();
      c.addAll(aspect_sources);
      c.add("-classpath");
      c.add(OptionsParser.v().classpath());
      String[] args = new String[c.size()];
      int index = 0;
      for(Iterator iter = c.iterator(); iter.hasNext(); index++) {
        String s = (String)iter.next();
        args[index] = s;
      }
      Program program = new Program();
      ASTNode.reset();

      program.initBytecodeReader(new abc.ja.bytecode.Parser());
      program.initJavaParser(
        new JavaParser() {
          public CompilationUnit parse(InputStream is, String fileName) throws IOException, beaver.Parser.Exception {
            return new abc.ja.parse.JavaParser().parse(is, fileName, error_queue);
          }
        }
      );
      // extract package name from a source file without parsing the entire file
      program.initPackageExtractor(new abc.ja.parse.JavaScanner());

      program.initOptions();
      program.addKeyValueOption("-classpath");
      program.addKeyOption("-verbose");
      program.addOptions(args);
      Collection files = program.files();

      try {
        for(Iterator iter = files.iterator(); iter.hasNext(); ) {
          String name = (String)iter.next();
          program.addSourceFile(name);
        }

        for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
          CompilationUnit unit = (CompilationUnit)iter.next();
          if(unit.fromSource()) {
            // abort if there were syntax or lexical errors
            if(error_queue().errorCount() > 0)
              throw new CompilerFailedException("There were errors.");
            Collection errors = new LinkedList();
            if(Program.verbose())
              System.out.println("Error checking " + unit.relativeName());
            long time = System.currentTimeMillis();
            unit.errorCheck(errors);
            time = System.currentTimeMillis()-time;
            if(Program.verbose())
              System.out.println("Error checking " + unit.relativeName() + " done in " + time + " ms");
            if(!errors.isEmpty()) {
              //System.out.println("Errors:");
              for(Iterator iter2 = errors.iterator(); iter2.hasNext(); ) {
                String s = (String)iter2.next();
                addError(s);
                //System.out.println(s);
              }
              throw new CompilerFailedException("There were errors.");
              //return;
            }
            else {
              //unit.java2Transformation();
            }
          }
        }
      } catch (ParseError e) {
        //System.err.println(e.getMessage());
        addError(e.getMessage());
        throw new CompilerFailedException("There were errors.");
      } /*catch (Exception e) {
        System.err.println(e.getMessage());
        e.printStackTrace();
      }*/

      program.generateIntertypeDecls();
      program.transformation();

      program.jimplify1();
      program.jimplify2();

      abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().buildAspectHierarchy();
      abc.main.AbcTimer.mark("Aspect inheritance");
      abc.main.Debug.phaseDebug("Aspect inheritance");

    } catch (Error /*polyglot.main.UsageError*/ e) {
      throw (IllegalArgumentException) new IllegalArgumentException("Polyglot usage error: "+e.getMessage()).initCause(e);
    }

    // Output the aspect info
    if (abc.main.Debug.v().aspectInfo)
      abc.main.Main.v().getAbcExtension().getGlobalAspectInfo().print(System.err);
  }
	
  public void weave() throws CompilerFailedException {
    try {
      // Perform the declare parents
      new DeclareParentsWeaver().weave();
      // FIXME: put re-resolving here, from declareparents weaver
      AbcTimer.mark("Declare Parents");
      Debug.phaseDebug("Declare Parents");
      Scene.v().setDoneResolving();

      // Adjust Soot types for intertype decls
      IntertypeAdjuster ita = new IntertypeAdjuster();
      ita.adjust();
      AbcTimer.mark("Intertype Adjuster");
      Debug.phaseDebug("Intertype Adjuster");

      // Retrieve all bodies
      for( Iterator clIt = abcExt.getGlobalAspectInfo().getWeavableClasses().iterator(); clIt.hasNext(); ) {
        final AbcClass cl = (AbcClass) clIt.next();
        if(Debug.v().showWeavableClasses) System.err.println("Weavable class: "+cl);
        for( Iterator methodIt = cl.getSootClass().getMethods().iterator(); methodIt.hasNext(); ) {
          final SootMethod method = (SootMethod) methodIt.next();
          try {
            if( !method.isConcrete() ) continue;
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
      ita.initialisers(); // weave the field initialisers into the constructors
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
      ita.removeFakeFields();
    } catch(SemanticException e) {
      abcExt.reportError(ErrorInfo.SEMANTIC_ERROR,e.getMessage(),e.position());
    }
  }
}
