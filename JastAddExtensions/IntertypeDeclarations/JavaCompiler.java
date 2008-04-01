/*
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2005-2008, Torbjorn Ekman
 * All rights reserved.
 */

import AST.*;

import java.util.*;
import java.io.*;

import AST.List;

public class JavaCompiler extends Frontend {

  public static void main(String args[]) {
    if(!compile(args))
      System.exit(1);
  }

  public static boolean compile(String args[]) {
    return new JavaCompiler().process(
       args,
       new BytecodeParser(),
       new JavaParser() {
          parser.JavaParser parser = new parser.JavaParser();
          public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
            return parser.parse(is, fileName);
          }
       }
    );
  }

  public boolean process(String[] args, BytecodeReader reader, JavaParser parser) {
    program.initBytecodeReader(reader);
    program.initJavaParser(parser);

    initOptions();
    processArgs(args);

    Collection files = program.files();

    try {
      for(Iterator iter = files.iterator(); iter.hasNext(); ) {
        String name = (String)iter.next();
        program.addSourceFile(name);
      }

      for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
        CompilationUnit unit = (CompilationUnit)iter.next();
        if(unit.fromSource()) {
          Collection errors = unit.parseErrors();
          if(!errors.isEmpty()) {
            processErrors(errors, unit);
            return false;
          }
        }
      }
      ArrayList errors = new ArrayList();
      Collection warnings = new ArrayList();
      program.errorCheck(errors, warnings);
      if(!errors.isEmpty()) {
        Collections.sort(errors);
        System.out.println("Errors:");
        for(Iterator iter2 = errors.iterator(); iter2.hasNext(); ) {
          System.out.println(iter2.next());
        }
        return false;
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
    program.generateIntertypeDecls();
    program.transformation();
  
    System.out.println(program.toString());
    return true;
  }

  protected String name() {
    return "Java1.4 + ITDs";
  }

}
