package jastadd;

import AST.*;

import java.util.*;
import java.io.*;

import AST.List;

public class JastAdd extends Frontend {
  public static void main(String args[]) {
    if(!compile(args))
      System.exit(1);
  }
  public static boolean compile(String[] args) {
    JastAdd jastAdd = new JastAdd();
    boolean result = jastAdd.process(
        args,
        new BytecodeParser(),
        new JavaParser() {
          public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
            return new parser.JavaParser().parse(is, fileName);
          }
        }
    );
    if(!result) return false;
    jastAdd.generate();
    return true;
  }

  public void generate() {
    program.generateIntertypeDecls();
    program.transformation();
    for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
      CompilationUnit cu = (CompilationUnit)iter.next();
      if(cu.fromSource()) {
        for(int i = 0; i < cu.getNumTypeDecl(); i++) {
          cu.getTypeDecl(i).generateClassfile();
        }
      }
    }
    //program.generateClassfile();
  }
  
  protected void initOptions() {
    super.initOptions();
    program.addKeyOption("-no_cache_cycle");
    program.addKeyOption("-no_visit_check");
    program.addKeyOption("-no_component_check");
    program.addKeyValueOption("-package");

    program.addKeyOption("-weave_inline");
    program.addKeyOption("-inh_in_astnode");
  }
}
