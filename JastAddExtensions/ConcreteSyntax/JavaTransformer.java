/*
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2005-2008, Torbjorn Ekman
 * All rights reserved.
 */

import AST.*;

class JavaTransformer extends Frontend {

  public static void main(String args[]) {
    compile(args);
  }

  public static boolean compile(String args[]) {
    return new JavaTransformer().process(
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

  protected void processNoErrors(CompilationUnit unit) {
    unit.addLayout();
    //unit.transformation();
    System.out.println(unit.layout());
    //CompilationUnit u = (CompilationUnit)unit.transformation();
    //System.out.println(u.layout());
    //program.addCompilationUnit(u);
    //System.out.println(u.layout());
    //System.out.println(u.dumpTree());
    //u.print();
    //System.out.println(unit.print());
    //unit.addLayout();
    //System.out.println(unit.layout());
  }


  protected String name() { return "JavaChecker"; }
  protected String version() { return "R20071015"; }
}
