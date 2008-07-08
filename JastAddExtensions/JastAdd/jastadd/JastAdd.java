package jastadd;

import AST.*;

import java.util.*;
import java.io.*;

import AST.List;

public class JastAdd extends Frontend {
  public static void main(String args[]) {
    if(!new JastAdd().compile(args))
      System.exit(1);
  }
  public boolean compile(String[] args) {
    JastAdd jastAdd = this;
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
          if(program.options().hasOption("-print")) 
            System.out.println(cu);
          cu.getTypeDecl(i).generateClassfile();
        }
      }
    }
    //program.generateClassfile();
  }
  
  protected void initOptions() {
    super.initOptions();
    Options options = program.options();
    options.addKeyOption("-no_cache_cycle");
    options.addKeyOption("-no_visit_check");
    options.addKeyOption("-no_component_check");
    options.addKeyValueOption("-package");

    options.addKeyOption("-weave_inline");
    options.addKeyOption("-inh_in_astnode");
  }
}
