
package AST;
import java.util.HashSet;import java.util.LinkedHashSet;import java.io.FileNotFoundException;import java.io.File;import java.util.*;import beaver.*;import java.util.ArrayList;import java.util.zip.*;import java.io.*;import sun.text.normalizer.UTF16;

    
  public class PathPart extends java.lang.Object {
    // Declared in ClassPath.jrag at line 263
    public InputStream is;

    // Declared in ClassPath.jrag at line 264
    protected String pathName;

    // Declared in ClassPath.jrag at line 265 
    protected String relativeName;

    // Declared in ClassPath.jrag at line 266
    protected String fullName;

    // Declared in ClassPath.jrag at line 267
    long age;

    // Declared in ClassPath.jrag at line 268
    Program program;

    // Declared in ClassPath.jrag at line 270
    
    protected PathPart() {
    }

    // Declared in ClassPath.jrag at line 273

    protected boolean isSource;

    // Declared in ClassPath.jrag at line 274
    protected String fileSuffix() {
      return isSource ? ".java" : ".class";
    }

    // Declared in ClassPath.jrag at line 278

    public static PathPart createSourcePath(String fileName, Program program) {
      PathPart p = createPathPart(fileName);
      if(p != null) {
        p.isSource = true;
        p.program = program;
      }
      return p;
    }

    // Declared in ClassPath.jrag at line 287

    public static PathPart createClassPath(String fileName, Program program) {
      PathPart p = createPathPart(fileName);
      if(p != null) {
        p.isSource = false;
        p.program = program;
      }
      return p;
    }

    // Declared in ClassPath.jrag at line 296

    private static PathPart createPathPart(String s) {
      try {
        File f = new File(s);
        if(f.isDirectory())
          return new FolderPart(f);
        else if(f.isFile())
          return new ZipFilePart(new ZipFile(f));
      } catch (IOException e) {
        // error in path
      }
      return null;
    }

    // Declared in ClassPath.jrag at line 310

    // is there a package with the specified name on this path part
    public boolean hasPackage(String name) { return false; }

    // Declared in ClassPath.jrag at line 314
    
    // select a compilation unit from a canonical name
    // returns true of the compilation unit exists on this path
    public boolean selectCompilationUnit(String canonicalName) throws IOException { return false; }

    // Declared in ClassPath.jrag at line 317

    // load the return currently selected compilation unit
    public CompilationUnit getCompilationUnit() {
      long startTime = System.currentTimeMillis();
      if(!isSource) {
        try {
          if(Program.verbose())
            System.out.print("Loading .class file: " + fullName + " ");

          CompilationUnit u = program.bytecodeReader.read(is, fullName, program);
          //CompilationUnit u = new bytecode.Parser(is, fullName).parse(null, null, program);
          u.setPathName(pathName);
          u.setRelativeName(relativeName);
          u.setFromSource(false);
          
          is.close();
          is = null;
          
          if(Program.verbose())
            System.out.println("in " + (System.currentTimeMillis() - startTime) + " ms");
          return u;
        } catch (Exception e) {
          throw new Error("Error loading " + fullName, e);
        }
      } 
      else {
        try {  
          if(Program.verbose())
            System.out.print("Loading .java file: " + fullName + " ");
            
          CompilationUnit u = program.javaParser.parse(is, fullName);
          is.close();
          is = null;
          
          u.setPathName(pathName);
          u.setRelativeName(relativeName);
          u.setFromSource(true);

          if(Program.verbose())
            System.out.println("in " + (System.currentTimeMillis() - startTime) + " ms");
          return u;
        } catch (Exception e) {
          System.err.println("Unexpected error of kind " + e.getClass().getName());
          throw new Error(fullName + ": " + e.getMessage(), e);
        }
      }
    }


}
