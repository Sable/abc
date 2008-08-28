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
    if(program.options().hasOption("-source_output") && program.options().hasOption("-d")) {
      for(Iterator iter = program.compilationUnitIterator(); iter.hasNext(); ) {
        CompilationUnit cu = (CompilationUnit)iter.next();
        if(cu.fromSource()) {
          String pathPrefix = program.options().getValueForOption("-d");
          String packagePath = cu.packageName().replace('.', java.io.File.separatorChar);
          String cuSourceName = cu.pathName().substring(cu.pathName().lastIndexOf(java.io.File.separator) + 1);
          int index = cuSourceName.indexOf(".");
          String suffix = "";
          if(index != -1) {
            suffix = cuSourceName.substring(index + 1);
            cuSourceName = cuSourceName.substring(0, index);
          }
          String pathName = pathPrefix + "/" + packagePath;
          if(suffix.equals("jrag") || suffix.equals("jadd") || suffix.equals(""))
            suffix = "java";
          if(cuSourceName.equals("")) {
            cuSourceName = cu.getTypeDecl(0).name();
          }
          String fileName = pathPrefix + "/" + packagePath + "/" + cuSourceName + "." + suffix;
          try {
            new File(pathName).mkdirs();
            PrintStream output = new PrintStream(new File(fileName));
            output.println(cu.toString());
            output.close();
          } catch(IOException e) {
            System.err.println(e.getMessage());
          }
        }
      }
    }
    else {
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
    }
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
    options.addKeyOption("-source_output");
  }

  protected void processArgs(String[] args) {
    super.processArgs(args);
    if(program.options().hasOption("-source_output") && !program.options().hasOption("-d"))
      System.out.println("Warning: -source_output is only valid together with the -d option");
  }
}
